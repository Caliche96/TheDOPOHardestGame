package Presentation;

import Dominio.Game;
import Dominio.GameException;
import Dominio.GameMode;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Pantalla principal de juego.
 * Única clase de dominio accedida: Game (fachada).
 */
public class GamePanel extends JFrame {

    // ──── Constantes de layout ────
    private static final int WINDOW_W = 600;
    private static final int WINDOW_H = 500;
    private static final int HUD_H    = 50;
    private static final int CELL     = 26;
    private static final int FPS      = 30;
    private static final int TICK_MS  = 1000 / FPS;

    // ──── Colores ────
    private static final Color COLOR_BG         = new Color(106, 140, 224);
    private static final Color COLOR_SPAWN      = new Color(144, 238, 144);
    private static final Color COLOR_GOAL       = new Color(144, 238, 144);
    private static final Color COLOR_WALKABLE_A = new Color(220, 225, 240);
    private static final Color COLOR_WALKABLE_B = new Color(200, 208, 230);
    private static final Color COLOR_SAFE       = new Color(180, 230, 180);  // zona segura intermedia
    private static final Color COLOR_BORDER     = Color.BLACK;
    private static final Color COLOR_HUD_BG     = Color.BLACK;
    private static final Color COLOR_HUD_TEXT   = Color.WHITE;
    private static final Color COLOR_PLAYER_1   = new Color(210, 30, 30);
    private static final Color COLOR_PLAYER_2   = new Color(30, 100, 210);
    private static final Color COLOR_ENEMY      = new Color(20, 20, 140);

    // ──── Única referencia al dominio ────
    private Game game;

    // ──── Metadatos de presentación ────
    private final GameMode gameMode;
    private final String   machineType;
    private       int      totalCoins;

    // ──── GameLoop ────
    private Timer gameLoop;

    // ──── Teclas presionadas ────
    private final boolean[] keys = new boolean[256];

    // ──── Panel de dibujo ────
    private DrawPanel drawPanel;

    // ──── Offset del tablero ────
    private int boardOffsetX;
    private int boardOffsetY;

    // ──── Menú ────
    private JButton    btnMenu;
    private JPopupMenu popupMenu;

    private static final String SAVES_DIR = "saves";
    private int enemyTickCounter = 0;

    // ──── Imágenes de elementos especiales ────
    private BufferedImage imgBomb;
    private BufferedImage imgLifeSource;
    private BufferedImage imgSkinCoin;

    // ══════════════════════════════════════════════
    //  CONSTRUCTOR — juego nuevo
    // ══════════════════════════════════════════════

    /**
     * Crea un nuevo panel de juego.
     * @param mode Modo de juego.
     * @param playerType Tipo de jugador 1.
     * @param borderColor1 Color del borde del jugador 1.
     * @param player2Type Tipo de jugador 2.
     * @param borderColor2 Color del borde del jugador 2.
     * @param machineType Tipo de máquina.
     * @param levelFile Ruta al archivo del nivel.
     */
    public GamePanel(GameMode mode, String playerType, Color borderColor1,
                     String player2Type, Color borderColor2,
                     String machineType, String levelFile) {
        this.gameMode    = mode;
        this.machineType = machineType;

        try {
            this.game = Game.create(levelFile, "Nivel", mode);
        } catch (GameException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(),
                    "Error al cargar nivel", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        game.setupPlayers(playerType, borderColor1, player2Type, borderColor2, machineType);
        initUI();
    }

    // ══════════════════════════════════════════════
    //  CONSTRUCTOR — juego restaurado desde save
    // ══════════════════════════════════════════════

    /**
     * Crea un nuevo panel de juego a partir de un juego restaurado.
     * @param restoredGame El juego restaurado.
     */
    public GamePanel(Game restoredGame) {
        this.game        = restoredGame;
        this.gameMode    = restoredGame.getGameMode();
        this.machineType = null;
        initUI();
    }

    // ══════════════════════════════════════════════
    //  INICIALIZACIÓN COMÚN
    // ══════════════════════════════════════════════

    /**
     * Inicializa la interfaz de usuario.
     */
    private void initUI() {
        this.totalCoins = game.getTotalCoins();
        int boardW      = game.getBoardColumns() * CELL;
        int boardH      = game.getBoardRows()    * CELL;
        boardOffsetX    = (WINDOW_W - boardW) / 2;
        boardOffsetY    = HUD_H + (WINDOW_H - HUD_H - boardH) / 2;

        loadSpecialElementImages();

        setTitle("The DOPO Hardest Game");
        setSize(WINDOW_W, WINDOW_H);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        prepareElements();
        prepareActions();
        setVisible(true);
        startGameLoop();
        SwingUtilities.invokeLater(() -> drawPanel.requestFocusInWindow());
    }

    // ══════════════════════════════════════════════
    //  IMÁGENES
    // ══════════════════════════════════════════════

    /**
     * Carga las imágenes de los elementos especiales desde la carpeta recursos/.
     * Si un archivo no existe o falla, el campo queda null y drawSpecialElements
     * usa el renderizado de texto como fallback.
     */
    private void loadSpecialElementImages() {
        imgBomb       = loadImage("recursos/bomb.png");
        imgLifeSource = loadImage("recursos/lifesource.png");
        imgSkinCoin   = loadImage("recursos/skinCoin.png");
    }

    /**
     * Carga una imagen desde la ruta especificada.
     * @param path Ruta al archivo de imagen.
     * @return La imagen cargada o null si falla.
     */
    private BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            System.err.println("GamePanel: no se pudo cargar imagen " + path);
            return null;
        }
    }

    // ══════════════════════════════════════════════
    //  PREPARAR ELEMENTOS
    // ══════════════════════════════════════════════

    /**
     * Prepara los elementos de la interfaz de usuario.
     */
    private void prepareElements() {
        drawPanel = new DrawPanel();
        drawPanel.setLayout(null);
        setContentPane(drawPanel);

        btnMenu = new JButton("MENU");
        btnMenu.setFont(new Font("Arial", Font.BOLD, 14));
        btnMenu.setForeground(Color.WHITE);
        btnMenu.setBackground(Color.BLACK);
        btnMenu.setBorderPainted(false);
        btnMenu.setFocusPainted(false);
        btnMenu.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMenu.setBounds(5, 10, 110, 30);
        drawPanel.add(btnMenu);

        popupMenu = new JPopupMenu();
        JMenuItem itemSave = new JMenuItem("Guardar partida");
        JMenuItem itemLoad = new JMenuItem("Abrir partida");
        JMenuItem itemQuit = new JMenuItem("Terminar partida");
        itemSave.setFont(new Font("Arial", Font.PLAIN, 14));
        itemLoad.setFont(new Font("Arial", Font.PLAIN, 14));
        itemQuit.setFont(new Font("Arial", Font.PLAIN, 14));
        popupMenu.add(itemSave);
        popupMenu.add(itemLoad);
        popupMenu.addSeparator();
        popupMenu.add(itemQuit);

        itemSave.addActionListener(e -> guardarPartida());
        itemLoad.addActionListener(e -> abrirPartida());
        itemQuit.addActionListener(e -> terminarPartida());
        btnMenu.addActionListener(e -> popupMenu.show(btnMenu, 0, btnMenu.getHeight()));
    }

    // ══════════════════════════════════════════════
    //  PREPARAR ACCIONES
    // ══════════════════════════════════════════════

    /**
     * Prepara las acciones del juego.
     * Aca se enceuntran los controladores de teclado y mouse para el panel de dibujo, que es el foco principal.
     * Controlador teclado movimineto jugadores y pausa, controlador mouse para mantener el foco en el panel al hacer click.
     */
    private void prepareActions() {
        drawPanel.setFocusable(true);
        drawPanel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { drawPanel.requestFocusInWindow(); }
        });
        drawPanel.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (code < keys.length) {
                    boolean was = keys[code];
                    keys[code] = true;
                    if (!was) {
                        boolean u = keys[KeyEvent.VK_W] || (gameMode != GameMode.PLAYER_VS_PLAYER && keys[KeyEvent.VK_UP]);
                        boolean d = keys[KeyEvent.VK_S] || (gameMode != GameMode.PLAYER_VS_PLAYER && keys[KeyEvent.VK_DOWN]);
                        boolean l = keys[KeyEvent.VK_A] || (gameMode != GameMode.PLAYER_VS_PLAYER && keys[KeyEvent.VK_LEFT]);
                        boolean r = keys[KeyEvent.VK_D] || (gameMode != GameMode.PLAYER_VS_PLAYER && keys[KeyEvent.VK_RIGHT]);
                        game.movePlayer(0, u, d, l, r);
                        if (gameMode.isMultiplayer()) {
                            game.movePlayer(1,
                                keys[KeyEvent.VK_UP], keys[KeyEvent.VK_DOWN],
                                keys[KeyEvent.VK_LEFT], keys[KeyEvent.VK_RIGHT]);
                        }
                    }
                }
                if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_P) togglePause();
            }
            @Override public void keyReleased(KeyEvent e) {
                int code = e.getKeyCode();
                if (code < keys.length) keys[code] = false;
            }
        });
        drawPanel.requestFocusInWindow();
    }

    // ══════════════════════════════════════════════
    //  GAME LOOP
    // ══════════════════════════════════════════════

    /**
     * Inicia el bucle del juego.
     */
    private void startGameLoop() {
        gameLoop = new Timer(TICK_MS, e -> {
            if (game.isRunning()) {
                game.tickPlayers();          // actualiza temporizadores internos (invencibilidad)
                processInput();
                if (gameMode == GameMode.PLAYER_VS_MACHINE) game.updateMachine();
                if (++enemyTickCounter >= Game.getEnemyMoveInterval()) {
                    game.moveEnemies();
                    enemyTickCounter = 0;
                }
                game.checkEnemyCollsion();
                game.checkCoinCollision();
                game.checkSpecialElements();
                game.checkBombEnemyCollision();
                game.checkPlayerCollisions();
                game.checkGoal();
                game.updateTimer();
                checkGameOver();
            }
            drawPanel.repaint();
        });
        gameLoop.start();
    }

    /**
     * Procesa la entrada del usuario.
     */
    private void processInput() {
        boolean u = keys[KeyEvent.VK_W] || (gameMode != GameMode.PLAYER_VS_PLAYER && keys[KeyEvent.VK_UP]);
        boolean d = keys[KeyEvent.VK_S] || (gameMode != GameMode.PLAYER_VS_PLAYER && keys[KeyEvent.VK_DOWN]);
        boolean l = keys[KeyEvent.VK_A] || (gameMode != GameMode.PLAYER_VS_PLAYER && keys[KeyEvent.VK_LEFT]);
        boolean r = keys[KeyEvent.VK_D] || (gameMode != GameMode.PLAYER_VS_PLAYER && keys[KeyEvent.VK_RIGHT]);
        game.movePlayer(0, u, d, l, r);
        if (gameMode.isMultiplayer()) {
            game.movePlayer(1,
                keys[KeyEvent.VK_UP], keys[KeyEvent.VK_DOWN],
                keys[KeyEvent.VK_LEFT], keys[KeyEvent.VK_RIGHT]);
        }
    }

    /**
     * Alterna el estado de pausa del juego.
     */
    private void togglePause() {
        if (game.isPaused()) game.resume(); else game.pause();
    }

    /**
     * Verifica si el juego ha terminado.
     */
    private void checkGameOver() {
        if (game.isGameOver() || game.isWin()) {
            gameLoop.stop();
            SwingUtilities.invokeLater(this::showEndDialog);
        }
    }

    // ══════════════════════════════════════════════
    //  GUARDAR / ABRIR PARTIDA
    // ══════════════════════════════════════════════

    /**
     * Termina la partida actual.
     */
    private void terminarPartida() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Seguro que quieres terminar la partida actual?\nPerderás el progreso no guardado.",
                "Terminar partida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            gameLoop.stop();
            new ModeSelectionPanel();
            dispose();
        }
    }

    /**
     * Guarda la partida actual.
     */
    private void guardarPartida() {
        boolean running = game.isRunning();
        if (running) game.pause();
        java.io.File savesDir = new java.io.File(SAVES_DIR);
        if (!savesDir.exists()) savesDir.mkdirs();
        JFileChooser chooser = new JFileChooser(savesDir);
        chooser.setDialogTitle("Guardar partida");
        chooser.setSelectedFile(new java.io.File("partida.dat"));
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Archivos de guardado (*.dat)", "dat"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            if (!path.endsWith(".dat")) path += ".dat";
            try {
                game.saveGame(path, game.getLevelFile());
                JOptionPane.showMessageDialog(this, "Partida guardada correctamente.",
                        "Guardado", JOptionPane.INFORMATION_MESSAGE);
            } catch (GameException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Error al guardar", JOptionPane.ERROR_MESSAGE);
            }
        }
        if (running) game.resume();
        drawPanel.requestFocusInWindow();
    }

    /**
     * Abre una partida guardada.
     */
    private void abrirPartida() {
        boolean running = game.isRunning();
        if (running) game.pause();
        java.io.File savesDir = new java.io.File(SAVES_DIR);
        if (!savesDir.exists()) savesDir.mkdirs();
        JFileChooser chooser = new JFileChooser(savesDir);
        chooser.setDialogTitle("Abrir partida guardada");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Archivos de guardado (*.dat)", "dat"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            try {
                Game restored = Game.createFromSave(path);
                gameLoop.stop();
                new GamePanel(restored);
                dispose();
            } catch (GameException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Error al cargar", JOptionPane.ERROR_MESSAGE);
                if (running) game.resume();
                drawPanel.requestFocusInWindow();
            }
        } else {
            if (running) game.resume();
            drawPanel.requestFocusInWindow();
        }
    }

    // ══════════════════════════════════════════════
    //  FIN DE PARTIDA
    // ══════════════════════════════════════════════

    /**
     * Muestra el diálogo de finalización del juego.
     */
    private void showEndDialog() {
        boolean won = game.isWin();
        String msg;
        if (gameMode == GameMode.PLAYER_VS_PLAYER && won) {
            int c1 = game.getPlayerCount() > 0 ? game.getPlayerCoins(0) : 0;
            int c2 = game.getPlayerCount() > 1 ? game.getPlayerCoins(1) : 0;
            if      (c1 > c2) msg = "¡PLAYER 1 GANA!\nMonedas — P1: " + c1 + "  P2: " + c2;
            else if (c2 > c1) msg = "¡PLAYER 2 GANA!\nMonedas — P1: " + c1 + "  P2: " + c2;
            else              msg = "¡EMPATE!\nAmbos recogieron " + c1 + " monedas.";
        } else if (won) {
            msg = "¡Nivel completado!";
        } else {
            msg = "Se acabó el tiempo. ¡Inténtalo de nuevo!";
        }

        String nextLevel = getNextLevelFile();
        String[] options = (won && nextLevel != null)
                ? new String[]{"Siguiente nivel", "Reintentar", "Menú principal"}
                : new String[]{"Reintentar", "Menú principal"};

        int opt = JOptionPane.showOptionDialog(this, msg, "Fin del juego",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]);

        String p1Type   = game.getPlayerCount() > 0 ? game.getPlayerType(0) : "Red";
        Color  p1Border = game.getPlayerCount() > 0 ? game.getPlayerBorderColor(0) : Color.WHITE;
        String p2Type   = (game.getPlayerCount() > 1 && !game.isPlayerMachine(1))
                          ? game.getPlayerType(1) : null;
        Color  p2Border = (game.getPlayerCount() > 1 && !game.isPlayerMachine(1))
                          ? game.getPlayerBorderColor(1) : null;
        String curLevel = game.getLevelFile();

        if (won && nextLevel != null) {
            if      (opt == 0) new GamePanel(gameMode, p1Type, p1Border, p2Type, p2Border, machineType, nextLevel);
            else if (opt == 1) new GamePanel(gameMode, p1Type, p1Border, p2Type, p2Border, machineType, curLevel);
            else               new ModeSelectionPanel();
        } else {
            if (opt == 0) new GamePanel(gameMode, p1Type, p1Border, p2Type, p2Border, machineType, curLevel);
            else          new ModeSelectionPanel();
        }
        dispose();
    }

    /**
     * Obtiene el archivo del siguiente nivel.
     * @return El nombre del archivo del siguiente nivel, o null si no hay más niveles.
     */
    private String getNextLevelFile() {
        java.io.File folder = new java.io.File("recursos");
        java.io.File[] txts = folder.listFiles((d, n) -> n.matches("nivel\\d+\\.txt"));
        if (txts == null) return null;
        java.util.Arrays.sort(txts, (a, b) -> {
            int na = Integer.parseInt(a.getName().replaceAll("[^0-9]", ""));
            int nb = Integer.parseInt(b.getName().replaceAll("[^0-9]", ""));
            return Integer.compare(na, nb);
        });
        String cur = game.getLevelFile();
        for (int i = 0; i < txts.length - 1; i++)
            if (("recursos/" + txts[i].getName()).equals(cur))
                return "recursos/" + txts[i + 1].getName();
        return null;
    }

    // ══════════════════════════════════════════════
    //  PANEL DE DIBUJO
    // ══════════════════════════════════════════════

    /**
     * Panel de dibujo del juego.
     */
    private class DrawPanel extends JPanel {

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            drawBackground(g2);
            drawHUD(g2);
            drawBoard(g2);
            drawCoins(g2);
            drawSpecialElements(g2);
            drawEnemies(g2);
            drawPlayers(g2);
            if (game.isPaused()) drawPauseOverlay(g2);
        }

        private void drawBackground(Graphics2D g2) {
            g2.setColor(COLOR_BG);
            g2.fillRect(0, HUD_H, WINDOW_W, WINDOW_H - HUD_H);
        }

        private void drawHUD(Graphics2D g2) {
            g2.setColor(COLOR_HUD_BG);
            g2.fillRect(0, 0, WINDOW_W, HUD_H);
            g2.setColor(COLOR_HUD_TEXT);

            // Monedas (centro)
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            FontMetrics fm = g2.getFontMetrics();
            int collected = game.getPlayerCount() == 0 ? 0 : game.getPlayerCoins(0);
            String coins  = collected + "/" + totalCoins;
            g2.drawString(coins, (WINDOW_W - fm.stringWidth(coins)) / 2, 22);

            // Tiempo (centro-abajo)
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics fmS = g2.getFontMetrics();
            int secs = game.getRemainingTime() / Game.getFps();
            String time = String.format("%d:%02d", secs / 60, secs % 60);
            g2.drawString(time, (WINDOW_W - fmS.stringWidth(time)) / 2, 42);

            // DEATHS (derecha)
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            int deaths = game.getPlayerCount() == 0 ? 0 : game.getPlayerDeaths(0);
            String deathsTxt = "DEATHS: " + deaths;
            g2.drawString(deathsTxt, WINDOW_W - fm.stringWidth(deathsTxt) - 15, 32);
        }

        private void drawBoard(Graphics2D g2) {
            int rows = game.getBoardRows(), cols = game.getBoardColumns();
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    int px = boardOffsetX + col * CELL;
                    int py = boardOffsetY + row * CELL;
                    switch (game.getCellType(row, col)) {
                        case "SPAWN_ZONE":
                            g2.setColor(COLOR_SPAWN); g2.fillRect(px, py, CELL, CELL);
                            g2.setColor(COLOR_BORDER); g2.drawRect(px, py, CELL, CELL); break;
                        case "GOAL":
                            g2.setColor(COLOR_GOAL); g2.fillRect(px, py, CELL, CELL);
                            g2.setColor(COLOR_BORDER); g2.drawRect(px, py, CELL, CELL); break;
                        case "SAFE_ZONE":
                            // Zona segura intermedia ('Z') — verde suave, distinto de spawn/goal
                            g2.setColor(COLOR_SAFE); g2.fillRect(px, py, CELL, CELL);
                            g2.setColor(COLOR_BORDER); g2.drawRect(px, py, CELL, CELL); break;
                        case "WALKABLE": {
                            Color chess = ((row + col) % 2 == 0) ? COLOR_WALKABLE_A : COLOR_WALKABLE_B;
                            g2.setColor(chess); g2.fillRect(px, py, CELL, CELL);
                            g2.setColor(COLOR_BORDER); g2.drawRect(px, py, CELL, CELL); break;
                        }
                        default:
                            g2.setColor(COLOR_BG); g2.fillRect(px, py, CELL, CELL); break;
                    }
                }
            }
            g2.setColor(COLOR_BORDER);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRect(boardOffsetX, boardOffsetY, cols * CELL, rows * CELL);
        }

        private void drawCoins(Graphics2D g2) {
            for (int i = 0; i < game.getCoinCount(); i++) {
                if (game.isCoinCollected(i)) continue;
                int px = boardOffsetX + game.getCoinCol(i) * CELL + CELL / 2;
                int py = boardOffsetY + game.getCoinRow(i) * CELL + CELL / 2;
                int r  = CELL / 4;

                if (game.isCoinSkin(i) && imgSkinCoin != null) {
                    int size = r * 2 + 2;
                    g2.drawImage(imgSkinCoin, px - r - 1, py - r - 1, size, size, null);
                } else {
                    g2.setColor(new Color(255, 215, 0));
                    g2.fillOval(px - r, py - r, r * 2, r * 2);
                    g2.setColor(COLOR_BORDER);
                    g2.drawOval(px - r, py - r, r * 2, r * 2);
                }
            }
        }

        private void drawSpecialElements(Graphics2D g2) {
            int imgSize = CELL - 2;

            for (int i = 0; i < game.getSpecialElementCount(); i++) {
                if (!game.isSpecialElementActive(i)) continue;

                int cellX = boardOffsetX + game.getSpecialElementCol(i) * CELL;
                int cellY = boardOffsetY + game.getSpecialElementRow(i) * CELL;
                int cx = cellX + CELL / 2;
                int cy = cellY + CELL / 2;
                int r  = CELL / 3;

                if (game.isSpecialElementBomb(i)) {
                    if (imgBomb != null) {
                        g2.drawImage(imgBomb,
                                cellX + (CELL - imgSize) / 2,
                                cellY + (CELL - imgSize) / 2,
                                imgSize, imgSize, null);
                    } else {
                        g2.setColor(Color.DARK_GRAY); g2.fillOval(cx-r, cy-r, r*2, r*2);
                        g2.setColor(Color.RED);
                        g2.setFont(new Font("Arial", Font.BOLD, 10));
                        g2.drawString("B", cx-4, cy+4);
                    }
                } else if (game.isSpecialElementLifeSource(i)) {
                    if (imgLifeSource != null) {
                        g2.drawImage(imgLifeSource,
                                cellX + (CELL - imgSize) / 2,
                                cellY + (CELL - imgSize) / 2,
                                imgSize, imgSize, null);
                    } else {
                        g2.setColor(new Color(255, 80, 80));
                        g2.setFont(new Font("Arial", Font.BOLD, 14));
                        g2.drawString("♥", cx-7, cy+5);
                    }
                }
            }
        }

        private void drawEnemies(Graphics2D g2) {
            for (int i = 0; i < game.getEnemyCount(); i++) {
                if (!game.isEnemyActive(i)) continue;
                int px = Math.round(game.getEnemyX(i) + game.getEnemySize(i) / 2f) + boardOffsetX;
                int py = Math.round(game.getEnemyY(i) + game.getEnemySize(i) / 2f) + boardOffsetY;
                int r  = Math.round(game.getEnemySize(i) / 2f);
                g2.setColor(COLOR_ENEMY); g2.fillOval(px-r, py-r, r*2, r*2);
                g2.setColor(Color.BLACK); g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(px-r, py-r, r*2, r*2);
            }
        }

        /**
         * Dibuja los jugadores con su color de cuerpo y su borde personalizado.
         * El número fue reemplazado por el borde de color como diferenciador.
         */
        private void drawPlayers(Graphics2D g2) {
            for (int i = 0; i < game.getPlayerCount(); i++) {
                float px = game.getPlayerX(i);
                float py = game.getPlayerY(i);
                float sz = game.getPlayerSize(i);
                int cx   = Math.round(px + sz / 2f) + boardOffsetX;
                int cy   = Math.round(py + sz / 2f) + boardOffsetY;
                int r    = Math.round(sz / 2f);

                boolean invincible   = game.isPlayerInvincible(i);
                boolean shieldActive = game.isPlayerShieldActive(i);

                // Flash durante invencibilidad: saltar frames pares de 80ms
                if (invincible && (System.currentTimeMillis() / 80) % 2 == 0) continue;

                Composite original = g2.getComposite();

                // Sombra
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillOval(cx - r + 2, cy - r + 2, r * 2, r * 2);

                // Cuerpo
                g2.setColor(getBodyColor(i));
                g2.fillOval(cx - r, cy - r, r * 2, r * 2);

                // Brillo
                g2.setColor(new Color(255, 255, 255, 80));
                g2.fillOval(cx - r + 2, cy - r + 2, r, r / 2);

                // Indicador de escudo para GreenPlayer
                if ("Green".equals(game.getPlayerType(i))) {
                    if (shieldActive) {
                        // Anillo verde brillante = escudo disponible
                        g2.setColor(new Color(60, 255, 100, 180));
                        g2.setStroke(new BasicStroke(2.5f));
                        g2.drawOval(cx - r - 3, cy - r - 3, (r + 3) * 2, (r + 3) * 2);
                    }
                    // Si el escudo se consumió, no se dibuja el anillo (sin necesidad de else)
                }

                // Borde personalizado del jugador
                Color borderColor = game.getPlayerBorderColor(i);
                if (borderColor == null) borderColor = Color.WHITE;
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(3.5f));
                g2.drawOval(cx - r, cy - r, r * 2, r * 2);

                g2.setComposite(original);
            }
        }

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

    // ══════════════════════════════════════════════
    //  UTILIDADES
    // ══════════════════════════════════════════════

    /** Color del cuerpo según el tipo de skin. */
    private Color getBodyColor(int playerIndex) {
        if (game.getPlayerCount() == 0) return COLOR_PLAYER_1;
        switch (game.getPlayerType(playerIndex)) {
            case "Green": return new Color(40, 170, 60);
            case "Blue":  return new Color(40, 100, 220);
            default:      return COLOR_PLAYER_1;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new GamePanel(GameMode.SINGLE_PLAYER, "Red", Color.WHITE,
                              null, null, null, "recursos/nivel1.txt"));
    }
}