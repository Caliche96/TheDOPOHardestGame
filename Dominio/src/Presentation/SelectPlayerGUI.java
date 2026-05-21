package Presentation;

import Dominio.GameMode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Pantalla de selección de personaje.
 *
 * En modos PvP y PvM se muestra dos veces:
 *  - Primera vez: Player 1 elige su personaje
 *  - Segunda vez: Player 2 elige el suyo
 *
 * En Single Player va directo a LevelSelectGUI tras la elección de P1.
 */
public class SelectPlayerGUI extends JFrame {

    // ──── Constantes de diseño ────
    private static final int WIDTH  = 600;
    private static final int HEIGHT = 500;

    private static final Color BG_TOP        = new Color(190, 200, 255);
    private static final Color BG_BOTTOM     = new Color(140, 155, 235);
    private static final Color RED_COLOR     = new Color(220, 50, 50);
    private static final Color GREEN_COLOR   = new Color(40, 170, 60);
    private static final Color BLUE_COLOR    = new Color(40, 100, 220);
    private static final Color BTN_BACK_COLOR = new Color(120, 120, 130);

    private static final Font TITLE_FONT       = new Font("Arial", Font.BOLD, 36);
    private static final Font PLAYER_NAME_FONT = new Font("Arial", Font.BOLD, 14);
    private static final Font STAT_FONT        = new Font("Arial", Font.PLAIN, 11);
    private static final Font BTN_SMALL_FONT   = new Font("Arial", Font.BOLD, 13);

    // ──── Componentes ────
    private JButton btnRedPlayer;
    private JButton btnGreenPlayer;
    private JButton btnBluePlayer;
    private JButton btnBack;
    private JPanel  backgroundPanel;

    // ──── Estado ────
    private final GameMode selectedMode;

    /**
     * Número del jugador que está eligiendo actualmente.
     * 1 = Player 1 eligiendo, 2 = Player 2 eligiendo.
     */
    private final int     playerNumber;

    /**
     * Tipo de personaje ya elegido por Player 1.
     * Solo tiene valor cuando playerNumber == 2.
     */
    private final String  player1Type;

    // ──── Constructor para Player 1 ────
    public SelectPlayerGUI(GameMode mode) {
        this(mode, 1, null);
    }

    // ──── Constructor general ────
    private SelectPlayerGUI(GameMode mode, int playerNumber, String player1Type) {
        this.selectedMode  = mode;
        this.playerNumber  = playerNumber;
        this.player1Type   = player1Type;

        setTitle("Select Player " + playerNumber);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        prepareElements();
        prepareActions();
        setVisible(true);
    }

    // ═══════════════════════════════════════
    //  PREPARAR ELEMENTOS
    // ═══════════════════════════════════════

    private void prepareElements() {
        backgroundPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, BG_TOP, 0, getHeight(), BG_BOTTOM);
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        setContentPane(backgroundPanel);

        // Título dinámico según qué jugador está eligiendo
        String titleText = playerNumber == 1 ? "SELECT PLAYER 1" : "SELECT PLAYER 2";
        JLabel lblTitle = new JLabel(titleText, SwingConstants.CENTER);
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(playerNumber == 1 ? new Color(30, 50, 140) : new Color(140, 30, 30));
        lblTitle.setBounds(0, 30, WIDTH, 45);
        backgroundPanel.add(lblTitle);

        // Subtítulo
        String sub = "Mode: " + getModeDisplayText();
        if (playerNumber == 2) sub += "  ·  P1: " + player1Type;
        JLabel lblMode = new JLabel(sub, SwingConstants.CENTER);
        lblMode.setFont(new Font("Arial", Font.ITALIC, 13));
        lblMode.setForeground(new Color(60, 60, 100));
        lblMode.setBounds(0, 75, WIDTH, 20);
        backgroundPanel.add(lblMode);

        // Tarjetas de personaje
        int cardWidth  = 150;
        int cardHeight = 260;
        int gap        = 25;
        int startX     = (WIDTH - (3 * cardWidth + 2 * gap)) / 2;
        int cardY      = 110;

        btnRedPlayer = createPlayerCard("RED", RED_COLOR,
                "Speed: 1.0", "Size: 1.0", "Normal — no special ability",
                startX, cardY, cardWidth, cardHeight);
        backgroundPanel.add(btnRedPlayer);

        btnGreenPlayer = createPlayerCard("GREEN", GREEN_COLOR,
                "Speed: 1.0", "Size: 1.0", "Shield — survives one extra hit",
                startX + cardWidth + gap, cardY, cardWidth, cardHeight);
        backgroundPanel.add(btnGreenPlayer);

        btnBluePlayer = createPlayerCard("BLUE", BLUE_COLOR,
                "Speed: 1.5", "Size: 1.5", "Fast & big — high risk, high reward",
                startX + 2 * (cardWidth + gap), cardY, cardWidth, cardHeight);
        backgroundPanel.add(btnBluePlayer);

        // Botón BACK
        btnBack = createSmallButton("← BACK", BTN_BACK_COLOR);
        btnBack.setBounds(20, HEIGHT - 80, 100, 35);
        backgroundPanel.add(btnBack);
    }

    // ═══════════════════════════════════════
    //  PREPARAR ACCIONES
    // ═══════════════════════════════════════

    private void prepareActions() {
        btnRedPlayer.addActionListener(e  -> selectPlayer("Red"));
        btnGreenPlayer.addActionListener(e -> selectPlayer("Green"));
        btnBluePlayer.addActionListener(e  -> selectPlayer("Blue"));
        btnBack.addActionListener(e -> goBack());
    }

    // ═══════════════════════════════════════
    //  NAVEGACIÓN
    // ═══════════════════════════════════════

    private void selectPlayer(String type) {
        if (playerNumber == 1 && selectedMode.isMultiplayer()) {
            // PvP o PvM: mostrar pantalla de selección para Player 2
            new SelectPlayerGUI(selectedMode, 2, type);
        } else if (playerNumber == 2) {
            // Ambos jugadores eligieron — ir a selección de nivel
            new LevelSelectGUI(selectedMode, player1Type, type);
        } else {
            // Single player — ir directo a selección de nivel
            new LevelSelectGUI(selectedMode, type, null);
        }
        dispose();
    }

    private void goBack() {
        if (playerNumber == 2) {
            // Volver a la selección de P1
            new SelectPlayerGUI(selectedMode);
        } else {
            new ModeSelectionPanel();
        }
        dispose();
    }

    // ═══════════════════════════════════════
    //  UTILIDADES
    // ═══════════════════════════════════════

    private String getModeDisplayText() {
        switch (selectedMode) {
            case SINGLE_PLAYER:     return "Single Player";
            case PLAYER_VS_MACHINE: return "Player vs Machine";
            case PLAYER_VS_PLAYER:  return "Player vs Player";
            default:                return selectedMode.toString();
        }
    }

    private JButton createPlayerCard(String name, Color playerColor,
                                     String stat1, String stat2, String ability,
                                     int x, int y, int w, int h) {
        JButton card = new JButton() {
            private boolean hovering = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovering = true;  repaint(); }
                    @Override public void mouseExited (MouseEvent e) { hovering = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color cardBg = hovering ? new Color(255, 255, 255, 230) : new Color(255, 255, 255, 180);
                g2.setColor(cardBg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));

                g2.setColor(hovering ? playerColor : playerColor.darker());
                g2.setStroke(new BasicStroke(hovering ? 3f : 2f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, 20, 20));

                int sq = 60, sqX = (getWidth() - sq) / 2, sqY = 25;
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRoundRect(sqX + 3, sqY + 3, sq, sq, 8, 8);
                g2.setColor(playerColor);
                g2.fillRoundRect(sqX, sqY, sq, sq, 8, 8);
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(sqX, sqY, sq, sq, 8, 8);
                g2.setColor(Color.WHITE);
                g2.fillOval(sqX + 15, sqY + 20, 12, 12);
                g2.fillOval(sqX + 33, sqY + 20, 12, 12);
                g2.setColor(Color.BLACK);
                g2.fillOval(sqX + 19, sqY + 24, 5, 5);
                g2.fillOval(sqX + 37, sqY + 24, 5, 5);

                g2.setColor(playerColor.darker());
                g2.setFont(PLAYER_NAME_FONT);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(name, (getWidth() - fm.stringWidth(name)) / 2, sqY + sq + 25);

                g2.setColor(new Color(60, 60, 80));
                g2.setFont(STAT_FONT);
                FontMetrics fmS = g2.getFontMetrics();
                int sy = sqY + sq + 50;
                g2.drawString(stat1, (getWidth() - fmS.stringWidth(stat1)) / 2, sy);
                g2.drawString(stat2, (getWidth() - fmS.stringWidth(stat2)) / 2, sy + 18);

                g2.setFont(new Font("Arial", Font.ITALIC, 10));
                drawWrappedText(g2, ability, 10, sy + 45, getWidth() - 20);
                g2.dispose();
            }
        };

        card.setBounds(x, y, w, h);
        card.setFocusPainted(false);
        card.setBorderPainted(false);
        card.setContentAreaFilled(false);
        card.setOpaque(false);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return card;
    }

    private void drawWrappedText(Graphics2D g2, String text, int x, int y, int maxWidth) {
        FontMetrics fm = g2.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int lineY = y;
        for (String word : words) {
            String test = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(test) > maxWidth) {
                String ls = line.toString();
                g2.drawString(ls, x + (maxWidth - fm.stringWidth(ls)) / 2, lineY);
                line = new StringBuilder(word);
                lineY += fm.getHeight();
            } else {
                if (line.length() > 0) line.append(" ");
                line.append(word);
            }
        }
        if (line.length() > 0) {
            String ls = line.toString();
            g2.drawString(ls, x + (maxWidth - fm.stringWidth(ls)) / 2, lineY);
        }
    }

    private JButton createSmallButton(String text, Color baseColor) {
        JButton button = new JButton(text) {
            private boolean hovering = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovering = true;  repaint(); }
                    @Override public void mouseExited (MouseEvent e) { hovering = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovering ? baseColor.brighter() : baseColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 2, getHeight() - 2, 14, 14));
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth()  - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        button.setFont(BTN_SMALL_FONT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SelectPlayerGUI(GameMode.PLAYER_VS_PLAYER));
    }
}