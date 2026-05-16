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
    private Game        game = null;
    private final GameMode  gameMode;
    private final String    playerType;
    private final String    levelFile;
    private       int       totalCoins;

    // ──── GameLoop ────
    private Timer gameLoop;

    // ──── Teclas presionadas ────
    private final boolean[] keys = new boolean[256];

    // ──── Panel de dibujo ────
    private DrawPanel drawPanel;

    // ──── Offset para centrar el tablero ────
    private int boardOffsetX;
    private int boardOffsetY;

    public GamePanel(GameMode mode, String playerType, String levelFile) {
        this.gameMode   = mode;
        this.playerType = playerType;
        this.levelFile  = levelFile;

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
        Position spawn = level.getDefaultSpawn();
        if (spawn == null) spawn = new Position(0, 0);

        Player p1 = createPlayer(playerType, "Player1", spawn);
        game.addPlayer(p1);

        if (gameMode.isMultiplayer()) {
            // Player 2 spawn desplazado una celda abajo
            Position spawn2 = new Position(
                    Math.min(spawn.getRow() + 1, level.getBoard().getRows() - 1),
                    spawn.getColumn());
            Player p2 = createPlayer("Red", "Player2", spawn2);
            game.addPlayer(p2);
        }
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
                if (code < keys.length) keys[code] = true;

                // Pausa
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

    // ═══════════════════════════════════════
    //  GAME LOOP
    // ═══════════════════════════════════════

    private void startGameLoop() {
        gameLoop = new Timer(TICK_MS, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processInput();
                game.update();
                checkGameOver();
                drawPanel.repaint();
            }
        });
        gameLoop.start();
    }

    private void processInput() {
        // ── Player 1: WASD + flechas ──
        if (keys[KeyEvent.VK_W] || keys[KeyEvent.VK_UP])    game.movePlayer(0, Direction.UP);
        if (keys[KeyEvent.VK_S] || keys[KeyEvent.VK_DOWN])  game.movePlayer(0, Direction.DOWN);
        if (keys[KeyEvent.VK_A] || keys[KeyEvent.VK_LEFT])  game.movePlayer(0, Direction.LEFT);
        if (keys[KeyEvent.VK_D] || keys[KeyEvent.VK_RIGHT]) game.movePlayer(0, Direction.RIGHT);

        // ── Player 2: solo flechas ──
        if (gameMode.isMultiplayer()) {
            if (keys[KeyEvent.VK_UP])    game.movePlayer(1, Direction.UP);
            if (keys[KeyEvent.VK_DOWN])  game.movePlayer(1, Direction.DOWN);
            if (keys[KeyEvent.VK_LEFT])  game.movePlayer(1, Direction.LEFT);
            if (keys[KeyEvent.VK_RIGHT]) game.movePlayer(1, Direction.RIGHT);
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

    private void showEndDialog() {
        boolean won = game.getGameState() instanceof WinState;
        String msg = won
                ? "¡Nivel completado!"
                : "Se acabó el tiempo. ¡Inténtalo de nuevo!";

        int option = JOptionPane.showOptionDialog(this, msg, "Fin del juego",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, new String[]{"Reintentar", "Menú principal"}, "Reintentar");

        if (option == 0) {
            new GamePanel(gameMode, playerType, levelFile);
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

            // MENU (izquierda)
            g2.drawString("MENU", 15, 32);

            // Monedas recogidas / total (centro)
            int collected = game.getPlayers().isEmpty() ? 0
                    : game.getPlayers().get(0).getCollectedCoins();
            String coinsText = collected + "/" + totalCoins;
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(coinsText, (WINDOW_W - fm.stringWidth(coinsText)) / 2, 32);

            // DEATHS (derecha)
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
            for (Enemy enemy : enemies) {
                if (!enemy.isActive()) continue;
                int px = boardOffsetX + enemy.getPosition().getColumn() * CELL + CELL / 2;
                int py = boardOffsetY + enemy.getPosition().getRow()    * CELL + CELL / 2;
                int r  = CELL / 2 - 3;

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
            for (int i = 0; i < players.size(); i++) {
                Player p = players.get(i);
                int px = boardOffsetX + p.getPosition().getColumn() * CELL + 3;
                int py = boardOffsetY + p.getPosition().getRow()    * CELL + 3;
                int sz = CELL - 6;

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
                new GamePanel(GameMode.SINGLE_PLAYER, "Red", "recursos/nivel1.txt"));
    }
}