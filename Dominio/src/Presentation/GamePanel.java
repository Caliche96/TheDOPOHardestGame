package Presentation;

import Dominio.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Pantalla principal de juego.
 * Flujo: LevelSelectGUI → GamePanel
 *
 * Contiene:
 *  - HUD (50px arriba): MENU | nivel/monedas | DEATHS
 *  - Tablero centrado (20×10 celdas de 26px = 520×260px)
 *  - GameLoop a 30 FPS via javax.swing.Timer
 *  - Input: Player1 WASD + flechas | Player2 flechas
 */
public class GamePanel extends JFrame {

    // ──── Constantes de layout ────
    private static final int WINDOW_W  = 600;
    private static final int WINDOW_H  = 500;
    private static final int HUD_H     = 50;
    private static final int CELL      = 26;          // px por celda
    private static final int FPS       = 30;
    private static final int TICK_MS   = 1000 / FPS;

    // ──── Colores (fieles a la imagen) ────
    private static final Color COLOR_BG         = new Color(106, 140, 224);  // azul lavanda
    private static final Color COLOR_SPAWN      = new Color(144, 238, 144);  // verde claro
    private static final Color COLOR_GOAL       = new Color(144, 238, 144);  // verde claro
    private static final Color COLOR_WALKABLE_A = new Color(220, 225, 240);  // ajedrez claro
    private static final Color COLOR_WALKABLE_B = new Color(200, 208, 230);  // ajedrez oscuro
    private static final Color COLOR_SAFE       = new Color(180, 230, 180);
    private static final Color COLOR_BORDER     = Color.BLACK;
    private static final Color COLOR_HUD_BG     = Color.BLACK;
    private static final Color COLOR_HUD_TEXT   = Color.WHITE;
    private static final Color COLOR_PLAYER_1   = new Color(210, 30, 30);    // rojo
    private static final Color COLOR_PLAYER_2   = new Color(30, 100, 210);   // azul
    private static final Color COLOR_ENEMY      = new Color(20, 20, 140);    // azul oscuro

    // ──── Estado del juego ────
    private Game            game = null;
    private final GameMode  gameMode;
    private final String    playerType;
    private final String    player2Type;   // null en Single Player
    private final String    levelFile;
    private       int       totalCoins;

    // ──── Temporizadores individuales (ticks desde que empezó el nivel) ────
    private int p1Timer = 0;
    private int p2Timer = 0;
    private boolean p1Finished = false;
    private boolean p2Finished = false;

    // ──── GameLoop ────
    private Timer gameLoop;

    // ──── Teclas presionadas ────
    private final boolean[] keys = new boolean[256];

    // ──── Panel de dibujo ────
    private DrawPanel drawPanel;

    // ──── Offset para centrar el tablero ────
    private int boardOffsetX;
    private int boardOffsetY;

    // ──── Menú de archivo (MENU en HUD) ────
    private JButton  btnMenu;
    private JPopupMenu popupMenu;

    // ──── Carpeta de guardados ────
    private static final String SAVES_DIR = "saves";

    // ──── Contadores de tick para controlar velocidad ────
    private int playerTickCounter = 0;
    private int enemyTickCounter  = 0;

    // ──── Última dirección presionada (para movimiento suave) ────
    private Direction lastDirection  = null;
    private Direction lastDirection2 = null;
    private boolean   movedThisTick  = false;

    // ──── Posiciones visuales interpoladas (LERP) ────
    // Jugadores: posición en píxeles que se interpola suavemente hacia la celda lógica
    private float[] playerVisualX;   // x visual de cada jugador
    private float[] playerVisualY;   // y visual de cada jugador
    // Enemigos: igual
    private float[] enemyVisualX;
    private float[] enemyVisualY;

    /** Factor de interpolación LERP: 0.0 = sin movimiento, 1.0 = instantáneo. */
    private static final float LERP = 0.22f;

    public GamePanel(GameMode mode, String playerType, String player2Type, String levelFile) {
        this.gameMode    = mode;
        this.playerType  = playerType;
        this.player2Type = player2Type;
        this.levelFile   = levelFile;

        // ── Cargar nivel ──
        Level level = LevelLoader.load(levelFile, "Nivel 1");
        if (level == null) {
            JOptionPane.showMessageDialog(null, "No se pudo cargar el nivel: " + levelFile);
            dispose();
            return;
        }

        this.totalCoins = level.getCoins().size();

        // ── Crear juego ──
        this.game = new Game(level, mode);
        setupPlayers(level);

        // ── Calcular offset para centrar el tablero ──
        int boardW = level.getBoard().getColumns() * CELL;
        int boardH = level.getBoard().getRows()    * CELL;
        boardOffsetX = (WINDOW_W - boardW) / 2;
        boardOffsetY = HUD_H + (WINDOW_H - HUD_H - boardH) / 2;

        // ── Inicializar posiciones visuales (LERP) en la posición lógica inicial ──
        int numPlayers = game.getPlayers().size();
        playerVisualX = new float[numPlayers];
        playerVisualY = new float[numPlayers];
        for (int i = 0; i < numPlayers; i++) {
            Position p = game.getPlayers().get(i).getPosition();
            playerVisualX[i] = boardOffsetX + p.getColumn() * CELL + 3;
            playerVisualY[i] = boardOffsetY + p.getRow()    * CELL + 3;
        }

        int numEnemies = level.getEnemies().size();
        enemyVisualX = new float[numEnemies];
        enemyVisualY = new float[numEnemies];
        for (int i = 0; i < numEnemies; i++) {
            Position p = level.getEnemies().get(i).getPosition();
            enemyVisualX[i] = boardOffsetX + p.getColumn() * CELL + CELL / 2f;
            enemyVisualY[i] = boardOffsetY + p.getRow()    * CELL + CELL / 2f;
        }

        // ── Ventana ──
        setTitle("The DOPO Hardest Game");
        setSize(WINDOW_W, WINDOW_H);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        prepareElements();
        prepareActions();
        startGameLoop();

        setVisible(true);
    }

    // ═══════════════════════════════════════
    //  SETUP JUGADORES
    // ═══════════════════════════════════════

    private void setupPlayers(Level level) {
        GameBoard board = level.getBoard();

        // Spawn P1: primera celda SPAWN_ZONE encontrada (izquierda)
        Position spawn1 = level.getDefaultSpawn();
        if (spawn1 == null) spawn1 = new Position(0, 0);

        Player p1 = createPlayer(playerType, "Player 1", spawn1);
        game.addPlayer(p1);

        if (gameMode.isMultiplayer()) {
            // Spawn P2: última celda GOAL encontrada (derecha) — sentido opuesto
            Position spawn2 = findGoalSpawn(board, spawn1);
            String p2Type = (player2Type != null) ? player2Type : "Red";
            Player p2 = createPlayer(p2Type, "Player 2", spawn2);
            game.addPlayer(p2);
        }
    }

    /**
     * Busca una posición de spawn para Player 2 en el lado opuesto del tablero.
     * Encuentra la última celda tipo GOAL en la misma fila del spawn de P1.
     */
    private Position findGoalSpawn(GameBoard board, Position p1Spawn) {
        int targetRow = p1Spawn.getRow();
        // Buscar desde la derecha la primera celda GOAL en esa fila
        for (int col = board.getColumns() - 1; col >= 0; col--) {
            CellType type = board.getCell(targetRow, col).getType();
            if (type == CellType.GOAL) {
                return new Position(targetRow, col);
            }
        }
        // Fallback: esquina opuesta
        return new Position(targetRow, board.getColumns() - 1);
    }

    private Player createPlayer(String type, String name, Position pos) {
        switch (type) {
            case "Green": return new GreenPlayer(name, pos);
            case "Blue":  return new BluePlayer(name, pos);
            default:      return new RedPlayer(name, pos);
        }
    }

    // ═══════════════════════════════════════
    //  PREPARAR ELEMENTOS
    // ═══════════════════════════════════════

    private void prepareElements() {
        drawPanel = new DrawPanel();
        drawPanel.setLayout(null);
        setContentPane(drawPanel);

        // ── Botón MENU (sobre el HUD, izquierda) ──
        btnMenu = new JButton("MENU");
        btnMenu.setFont(new Font("Arial", Font.BOLD, 16));
        btnMenu.setForeground(Color.WHITE);
        btnMenu.setBackground(Color.BLACK);
        btnMenu.setBorderPainted(false);
        btnMenu.setFocusPainted(false);
        btnMenu.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMenu.setBounds(5, 10, 80, 30);
        drawPanel.add(btnMenu);

        // ── Popup menu ──
        popupMenu = new JPopupMenu();
        JMenuItem itemSave = new JMenuItem("💾  Guardar partida");
        JMenuItem itemLoad = new JMenuItem("📂  Abrir partida");
        itemSave.setFont(new Font("Arial", Font.PLAIN, 14));
        itemLoad.setFont(new Font("Arial", Font.PLAIN, 14));
        popupMenu.add(itemSave);
        popupMenu.add(itemLoad);

        itemSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { guardarPartida(); }
        });
        itemLoad.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { abrirPartida(); }
        });

        btnMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                popupMenu.show(btnMenu, 0, btnMenu.getHeight());
            }
        });
    }

    // ═══════════════════════════════════════
    //  PREPARAR ACCIONES (teclado)
    // ═══════════════════════════════════════

    private void prepareActions() {
        drawPanel.setFocusable(true);

        drawPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (code < keys.length) {
                    boolean wasPressed = keys[code];
                    keys[code] = true;

                    // Mover inmediatamente al presionar por primera vez (sin esperar el intervalo)
                    if (!wasPressed) {
                        Direction dir1 = getDirectionP1(code);
                        if (dir1 != null) {
                            game.movePlayer(0, dir1);
                            playerTickCounter = 0;
                        }
                        if (gameMode.isMultiplayer()) {
                            Direction dir2 = getDirectionP2(code);
                            if (dir2 != null) {
                                game.movePlayer(1, dir2);
                            }
                        }
                    }
                }

                if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_P) {
                    togglePause();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int code = e.getKeyCode();
                if (code < keys.length) keys[code] = false;
            }
        });

        drawPanel.requestFocusInWindow();
    }

    /**
     * Obtiene la dirección de P1 según el modo:
     * - PvP: solo WASD (flechas reservadas para P2)
     * - Single Player / PvM: WASD + flechas
     */
    private Direction getDirectionP1(int code) {
        boolean pvp = gameMode == GameMode.PLAYER_VS_PLAYER;
        if (code == KeyEvent.VK_W) return Direction.UP;
        if (code == KeyEvent.VK_S) return Direction.DOWN;
        if (code == KeyEvent.VK_A) return Direction.LEFT;
        if (code == KeyEvent.VK_D) return Direction.RIGHT;
        // Flechas solo si NO es PvP
        if (!pvp) {
            if (code == KeyEvent.VK_UP)    return Direction.UP;
            if (code == KeyEvent.VK_DOWN)  return Direction.DOWN;
            if (code == KeyEvent.VK_LEFT)  return Direction.LEFT;
            if (code == KeyEvent.VK_RIGHT) return Direction.RIGHT;
        }
        return null;
    }

    /**
     * Obtiene la dirección de P2 (solo flechas, solo en modo multijugador).
     */
    private Direction getDirectionP2(int code) {
        if (code == KeyEvent.VK_UP)    return Direction.UP;
        if (code == KeyEvent.VK_DOWN)  return Direction.DOWN;
        if (code == KeyEvent.VK_LEFT)  return Direction.LEFT;
        if (code == KeyEvent.VK_RIGHT) return Direction.RIGHT;
        return null;
    }

    // ═══════════════════════════════════════
    //  GAME LOOP
    // ═══════════════════════════════════════

    private void startGameLoop() {
        gameLoop = new Timer(TICK_MS, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // ── Movimiento del jugador (cada PLAYER_MOVE_INTERVAL ticks) ──
                playerTickCounter++;
                if (playerTickCounter >= GameConfig.PLAYER_MOVE_INTERVAL) {
                    processInput();
                    playerTickCounter = 0;
                }

                // ── Movimiento de enemigos (cada ENEMY_MOVE_INTERVAL ticks) ──
                enemyTickCounter++;
                if (enemyTickCounter >= GameConfig.ENEMY_MOVE_INTERVAL) {
                    game.moveEnemies();
                    enemyTickCounter = 0;
                }

                // ── Resto de la lógica (colisiones, timer, victoria) ──
                game.checkEnemyCollsion();
                game.checkCoinCollision();
                game.checkSpecialElements();
                game.checkPlayerCollisions();
                game.checkGoal();
                game.updateTimer();

                // ── Interpolación visual (LERP) ──
                updateVisualPositions();

                checkGameOver();
                drawPanel.repaint();
            }
        });
        gameLoop.start();
    }

    /**
     * Actualiza las posiciones visuales de jugadores y enemigos
     * interpolando suavemente hacia su posición lógica actual (LERP ease-out).
     *
     * Fórmula: visual += (destino - visual) * LERP
     * El resultado es un movimiento que empieza rápido y desacelera al llegar.
     */
    private void updateVisualPositions() {
        // ── Jugadores ──
        List<Player> players = game.getPlayers();
        for (int i = 0; i < players.size() && i < playerVisualX.length; i++) {
            Position p   = players.get(i).getPosition();
            float targetX = boardOffsetX + p.getColumn() * CELL + 3;
            float targetY = boardOffsetY + p.getRow()    * CELL + 3;
            playerVisualX[i] += (targetX - playerVisualX[i]) * LERP;
            playerVisualY[i] += (targetY - playerVisualY[i]) * LERP;
        }

        // ── Enemigos ──
        List<Enemy> enemies = game.getCurrentLevel().getEnemies();
        for (int i = 0; i < enemies.size() && i < enemyVisualX.length; i++) {
            Position p   = enemies.get(i).getPosition();
            float targetX = boardOffsetX + p.getColumn() * CELL + CELL / 2f;
            float targetY = boardOffsetY + p.getRow()    * CELL + CELL / 2f;
            enemyVisualX[i] += (targetX - enemyVisualX[i]) * LERP;
            enemyVisualY[i] += (targetY - enemyVisualY[i]) * LERP;
        }
    }

    private void processInput() {
        // Player 1
        Direction dir1 = null;
        if (keys[KeyEvent.VK_W]) dir1 = Direction.UP;
        else if (keys[KeyEvent.VK_S]) dir1 = Direction.DOWN;
        else if (keys[KeyEvent.VK_A]) dir1 = Direction.LEFT;
        else if (keys[KeyEvent.VK_D]) dir1 = Direction.RIGHT;
        // Flechas para P1 solo en Single Player o PvM
        else if (gameMode != GameMode.PLAYER_VS_PLAYER) {
            if      (keys[KeyEvent.VK_UP])    dir1 = Direction.UP;
            else if (keys[KeyEvent.VK_DOWN])  dir1 = Direction.DOWN;
            else if (keys[KeyEvent.VK_LEFT])  dir1 = Direction.LEFT;
            else if (keys[KeyEvent.VK_RIGHT]) dir1 = Direction.RIGHT;
        }
        if (dir1 != null) game.movePlayer(0, dir1);

        // Player 2 — solo flechas, solo en multijugador
        if (gameMode.isMultiplayer()) {
            Direction dir2 = null;
            if      (keys[KeyEvent.VK_UP])    dir2 = Direction.UP;
            else if (keys[KeyEvent.VK_DOWN])  dir2 = Direction.DOWN;
            else if (keys[KeyEvent.VK_LEFT])  dir2 = Direction.LEFT;
            else if (keys[KeyEvent.VK_RIGHT]) dir2 = Direction.RIGHT;
            if (dir2 != null) game.movePlayer(1, dir2);
        }
    }

    private void togglePause() {
        if (game.getGameState() instanceof PausedState) {
            game.resume();
        } else {
            game.pause();
        }
    }

    private void checkGameOver() {
        if (game.getGameState() instanceof GameOverState ||
            game.getGameState() instanceof WinState) {
            gameLoop.stop();
            showEndDialog();
        }
    }

    private void guardarPartida() {
        // Pausar mientras se guarda
        boolean estabaCorriendo = game.getGameState() instanceof RunningState;
        if (estabaCorriendo) game.pause();

        // Crear carpeta saves si no existe
        java.io.File savesDir = new java.io.File(SAVES_DIR);
        if (!savesDir.exists()) savesDir.mkdirs();

        // Diálogo para elegir nombre del archivo
        JFileChooser chooser = new JFileChooser(savesDir);
        chooser.setDialogTitle("Guardar partida");
        chooser.setSelectedFile(new java.io.File("partida.dat"));
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Archivos de guardado (*.dat)", "dat"));

        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            if (!path.endsWith(".dat")) path += ".dat";
            try {
                game.saveGame(path, levelFile);
                JOptionPane.showMessageDialog(this,
                        "Partida guardada correctamente.",
                        "Guardado", JOptionPane.INFORMATION_MESSAGE);
            } catch (GameException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(), "Error al guardar",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        if (estabaCorriendo) game.resume();
        drawPanel.requestFocusInWindow();
    }

    private void abrirPartida() {
        boolean estabaCorriendo = game.getGameState() instanceof RunningState;
        if (estabaCorriendo) game.pause();

        java.io.File savesDir = new java.io.File(SAVES_DIR);
        if (!savesDir.exists()) savesDir.mkdirs();

        JFileChooser chooser = new JFileChooser(savesDir);
        chooser.setDialogTitle("Abrir partida guardada");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Archivos de guardado (*.dat)", "dat"));

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            try {
                GameSave save = Game.loadGame(path);
                // Restaurar: abrir nuevo GamePanel con los datos del save
                gameLoop.stop();
                GamePanel restored = new GamePanel(
                        save.getGameMode(),
                        save.getPlayerSnapshots().get(0).getType(),
                        save.getPlayerSnapshots().size() > 1 ? save.getPlayerSnapshots().get(1).getType() : null,
                        save.getLevelFile());
                restored.restoreFromSave(save);
                dispose();
            } catch (GameException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(), "Error al cargar",
                        JOptionPane.ERROR_MESSAGE);
                if (estabaCorriendo) game.resume();
                drawPanel.requestFocusInWindow();
            }
        } else {
            if (estabaCorriendo) game.resume();
            drawPanel.requestFocusInWindow();
        }
    }

    /**
     * Aplica el estado de un GameSave sobre este GamePanel recién creado:
     * restaura tiempo restante, posiciones, muertes, monedas recogidas.
     */
    public void restoreFromSave(GameSave save) {
        // Tiempo restante
        game.setRemainingTime(save.getRemainingTime());

        // Jugadores
        List<GameSave.PlayerSnapshot> snapshots = save.getPlayerSnapshots();
        List<Player> players = game.getPlayers();
        for (int i = 0; i < Math.min(snapshots.size(), players.size()); i++) {
            GameSave.PlayerSnapshot snap = snapshots.get(i);
            players.get(i).setPosition(new Position(snap.getRow(), snap.getCol()));
            players.get(i).setDeaths(snap.getDeaths());
            players.get(i).setCollectedCoins(snap.getCollectedCoins());
        }

        // Marcar monedas como recogidas
        for (int[] pos : save.getCollectedCoinPositions()) {
            for (Coin c : game.getCurrentLevel().getCoins()) {
                if (c.getPosition().getRow() == pos[0] &&
                    c.getPosition().getColumn() == pos[1]) {
                    c.collect();
                    break;
                }
            }
        }

        // Marcar elementos especiales como consumidos
        for (int[] pos : save.getConsumedElementPositions()) {
            for (SpecialElement el : game.getCurrentLevel().getSpecialElements()) {
                if (el.getPosition().getRow() == pos[0] &&
                    el.getPosition().getColumn() == pos[1]) {
                    el.consume();
                    break;
                }
            }
        }
    }

    private void showEndDialog() {
        boolean won = game.getGameState() instanceof WinState;
        String msg;

        if (gameMode == GameMode.PLAYER_VS_PLAYER && won) {
            // Determinar ganador por monedas recogidas
            List<Player> players = game.getPlayers();
            int coins1 = players.size() > 0 ? players.get(0).getCollectedCoins() : 0;
            int coins2 = players.size() > 1 ? players.get(1).getCollectedCoins() : 0;

            if (coins1 > coins2) {
                msg = "¡PLAYER 1 GANA!\nMonedas — P1: " + coins1 + "  P2: " + coins2;
            } else if (coins2 > coins1) {
                msg = "¡PLAYER 2 GANA!\nMonedas — P1: " + coins1 + "  P2: " + coins2;
            } else {
                msg = "¡EMPATE!\nAmbos recogieron " + coins1 + " monedas.";
            }
        } else if (won) {
            msg = "¡Nivel completado!";
        } else {
            msg = "Se acabó el tiempo. ¡Inténtalo de nuevo!";
        }

        int option = JOptionPane.showOptionDialog(this, msg, "Fin del juego",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, new String[]{"Reintentar", "Menú principal"}, "Reintentar");

        if (option == 0) {
            new GamePanel(gameMode, playerType, player2Type, levelFile);
        } else {
            new ModeSelectionPanel();
        }
        dispose();
    }

    // ═══════════════════════════════════════
    //  PANEL DE DIBUJO
    // ═══════════════════════════════════════

    private class DrawPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            drawBackground(g2);
            drawHUD(g2);
            drawBoard(g2);
            drawCoins(g2);
            drawSpecialElements(g2);
            drawEnemies(g2);
            drawPlayers(g2);

            if (game.getGameState() instanceof PausedState) {
                drawPauseOverlay(g2);
            }
        }

        // ── Fondo azul ──
        private void drawBackground(Graphics2D g2) {
            g2.setColor(COLOR_BG);
            g2.fillRect(0, HUD_H, WINDOW_W, WINDOW_H - HUD_H);
        }

        // ── HUD ──
        private void drawHUD(Graphics2D g2) {
            g2.setColor(COLOR_HUD_BG);
            g2.fillRect(0, 0, WINDOW_W, HUD_H);

            g2.setColor(COLOR_HUD_TEXT);
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            FontMetrics fm = g2.getFontMetrics();

            // Monedas recogidas / total (centro)
            int collected = game.getPlayers().isEmpty() ? 0
                    : game.getPlayers().get(0).getCollectedCoins();
            String coinsText = collected + "/" + totalCoins;
            g2.drawString(coinsText, (WINDOW_W - fm.stringWidth(coinsText)) / 2, 22);

            // Tiempo restante en MM:SS (centro-abajo)
            int totalSecs = game.getRemainingTime() / GameConfig.FPS;
            int mins = totalSecs / 60;
            int secs = totalSecs % 60;
            String timeText = String.format("%d:%02d", mins, secs);
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics fmSmall = g2.getFontMetrics();
            g2.drawString(timeText, (WINDOW_W - fmSmall.stringWidth(timeText)) / 2, 42);

            // DEATHS (derecha)
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            int deaths = game.getPlayers().isEmpty() ? 0
                    : game.getPlayers().get(0).getDeaths();
            String deathsText = "DEATHS: " + deaths;
            g2.drawString(deathsText, WINDOW_W - fm.stringWidth(deathsText) - 15, 32);
        }

        // ── Tablero ──
        private void drawBoard(Graphics2D g2) {
            GameBoard board = game.getCurrentLevel().getBoard();
            Cell[][] cells  = board.getCells();

            for (int row = 0; row < board.getRows(); row++) {
                for (int col = 0; col < board.getColumns(); col++) {
                    int px = boardOffsetX + col * CELL;
                    int py = boardOffsetY + row * CELL;
                    Cell cell = cells[row][col];

                    switch (cell.getType()) {
                        case SPAWN_ZONE:
                            g2.setColor(COLOR_SPAWN);
                            g2.fillRect(px, py, CELL, CELL);
                            g2.setColor(COLOR_BORDER);
                            g2.drawRect(px, py, CELL, CELL);
                            break;

                        case GOAL:
                            g2.setColor(COLOR_GOAL);
                            g2.fillRect(px, py, CELL, CELL);
                            g2.setColor(COLOR_BORDER);
                            g2.drawRect(px, py, CELL, CELL);
                            break;

                        case WALKABLE:
                        case SAFE_ZONE: {
                            // Patrón ajedrezado
                            Color chess = ((row + col) % 2 == 0)
                                    ? COLOR_WALKABLE_A : COLOR_WALKABLE_B;
                            g2.setColor(chess);
                            g2.fillRect(px, py, CELL, CELL);
                            g2.setColor(COLOR_BORDER);
                            g2.drawRect(px, py, CELL, CELL);
                            break;
                        }

                        case WALL:
                        case EMPTY:
                        default:
                            // Fondo azul, sin borde
                            g2.setColor(COLOR_BG);
                            g2.fillRect(px, py, CELL, CELL);
                            break;
                    }
                }
            }

            // Borde exterior del tablero completo
            int boardW = board.getColumns() * CELL;
            int boardH = board.getRows()    * CELL;
            g2.setColor(COLOR_BORDER);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRect(boardOffsetX, boardOffsetY, boardW, boardH);
        }

        // ── Monedas ──
        private void drawCoins(Graphics2D g2) {
            List<Coin> coins = game.getCurrentLevel().getCoins();
            for (Coin coin : coins) {
                if (coin.isCollected()) continue;
                int px = boardOffsetX + coin.getPosition().getColumn() * CELL + CELL / 2;
                int py = boardOffsetY + coin.getPosition().getRow()    * CELL + CELL / 2;
                int r  = CELL / 4;

                if (coin instanceof SkinCoin) {
                    g2.setColor(new Color(255, 140, 0)); // naranja
                } else {
                    g2.setColor(new Color(255, 215, 0)); // dorado
                }
                g2.fillOval(px - r, py - r, r * 2, r * 2);
                g2.setColor(COLOR_BORDER);
                g2.drawOval(px - r, py - r, r * 2, r * 2);
            }
        }

        // ── Elementos especiales ──
        private void drawSpecialElements(Graphics2D g2) {
            List<SpecialElement> elements = game.getCurrentLevel().getSpecialElements();
            for (SpecialElement el : elements) {
                if (!el.isActive()) continue;
                int px = boardOffsetX + el.getPosition().getColumn() * CELL + CELL / 2;
                int py = boardOffsetY + el.getPosition().getRow()    * CELL + CELL / 2;
                int r  = CELL / 3;

                if (el instanceof Bomb) {
                    g2.setColor(Color.DARK_GRAY);
                    g2.fillOval(px - r, py - r, r * 2, r * 2);
                    g2.setColor(Color.RED);
                    g2.setFont(new Font("Arial", Font.BOLD, 10));
                    g2.drawString("B", px - 4, py + 4);
                } else if (el instanceof LifeSource) {
                    g2.setColor(new Color(255, 80, 80));
                    g2.setFont(new Font("Arial", Font.BOLD, 14));
                    g2.drawString("♥", px - 7, py + 5);
                }
            }
        }

        // ── Enemigos: círculos azul oscuro ──
        private void drawEnemies(Graphics2D g2) {
            List<Enemy> enemies = game.getCurrentLevel().getEnemies();
            int r = CELL / 2 - 3;
            for (int i = 0; i < enemies.size(); i++) {
                if (!enemies.get(i).isActive()) continue;
                if (i >= enemyVisualX.length) break;
                int px = Math.round(enemyVisualX[i]);
                int py = Math.round(enemyVisualY[i]);

                g2.setColor(COLOR_ENEMY);
                g2.fillOval(px - r, py - r, r * 2, r * 2);
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(px - r, py - r, r * 2, r * 2);
            }
        }

        // ── Jugadores: cuadrados de color con borde negro ──
        private void drawPlayers(Graphics2D g2) {
            List<Player> players = game.getPlayers();
            int sz = CELL - 6;
            for (int i = 0; i < players.size(); i++) {
                if (i >= playerVisualX.length) break;
                int px = Math.round(playerVisualX[i]);
                int py = Math.round(playerVisualY[i]);

                Color color = (i == 0) ? getPlayerColor() : COLOR_PLAYER_2;
                g2.setColor(color);
                g2.fillRect(px, py, sz, sz);
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRect(px, py, sz, sz);
            }
        }

        // ── Overlay de pausa ──
        private void drawPauseOverlay(Graphics2D g2) {
            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRect(0, HUD_H, WINDOW_W, WINDOW_H - HUD_H);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 40));
            String txt = "PAUSED";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(txt, (WINDOW_W - fm.stringWidth(txt)) / 2,
                    HUD_H + (WINDOW_H - HUD_H) / 2);
            g2.setFont(new Font("Arial", Font.PLAIN, 16));
            String sub = "Presiona P o ESC para continuar";
            FontMetrics fm2 = g2.getFontMetrics();
            g2.drawString(sub, (WINDOW_W - fm2.stringWidth(sub)) / 2,
                    HUD_H + (WINDOW_H - HUD_H) / 2 + 40);
        }
    }

    // ═══════════════════════════════════════
    //  UTILIDADES
    // ═══════════════════════════════════════

    private Color getPlayerColor() {
        switch (playerType) {
            case "Green": return new Color(40, 170, 60);
            case "Blue":  return new Color(40, 100, 220);
            default:      return COLOR_PLAYER_1;
        }
    }

    // ═══════════════════════════════════════
    //  MAIN (prueba independiente)
    // ═══════════════════════════════════════

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new GamePanel(GameMode.SINGLE_PLAYER, "Red", null, "recursos/nivel1.txt"));
    }
}