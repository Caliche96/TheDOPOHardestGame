package Presentation;

import Dominio.GameMode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Pantalla de selección de personaje (skin).
 * Flujo: ModeSelectionPanel → SelectPlayerGUI → LevelSelectGUI (pendiente)
 * 
 * Recibe el GameMode elegido y lo propaga a la siguiente pantalla.
 * Muestra 3 opciones de jugador: Rojo, Verde, Azul con sus características.
 */
public class SelectPlayerGUI extends JFrame {

    // ──── Constantes de diseño ────
    private static final int WIDTH = 600;
    private static final int HEIGHT = 500;

    private static final Color BG_TOP = new Color(190, 200, 255);
    private static final Color BG_BOTTOM = new Color(140, 155, 235);

    private static final Color RED_COLOR = new Color(220, 50, 50);
    private static final Color GREEN_COLOR = new Color(40, 170, 60);
    private static final Color BLUE_COLOR = new Color(40, 100, 220);
    private static final Color BTN_BACK_COLOR = new Color(120, 120, 130);

    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 36);
    private static final Font PLAYER_NAME_FONT = new Font("Arial", Font.BOLD, 14);
    private static final Font STAT_FONT = new Font("Arial", Font.PLAIN, 11);
    private static final Font BTN_SMALL_FONT = new Font("Arial", Font.BOLD, 13);

    // ──── Componentes ────
    private JButton btnRedPlayer;
    private JButton btnGreenPlayer;
    private JButton btnBluePlayer;
    private JButton btnBack;
    private JPanel backgroundPanel;

    // ──── Estado ────
    private GameMode selectedMode;

    public SelectPlayerGUI(GameMode mode) {
        this.selectedMode = mode;

        setTitle("Select Player");
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
        // Panel con gradiente
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
        backgroundPanel.setBounds(0, 0, WIDTH, HEIGHT);
        setContentPane(backgroundPanel);

        // ── Título ──
        JLabel lblTitle = new JLabel("SELECT PLAYER", SwingConstants.CENTER);
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(new Color(30, 50, 140));
        lblTitle.setBounds(0, 30, WIDTH, 45);
        backgroundPanel.add(lblTitle);

        // ── Subtítulo con el modo elegido ──
        String modeText = getModeDisplayText();
        JLabel lblMode = new JLabel("Mode: " + modeText, SwingConstants.CENTER);
        lblMode.setFont(new Font("Arial", Font.ITALIC, 13));
        lblMode.setForeground(new Color(60, 60, 100));
        lblMode.setBounds(0, 75, WIDTH, 20);
        backgroundPanel.add(lblMode);

        // ── Tarjetas de jugador ──
        int cardWidth = 150;
        int cardHeight = 260;
        int gap = 25;
        int totalWidth = 3 * cardWidth + 2 * gap;
        int startX = (WIDTH - totalWidth) / 2;
        int cardY = 110;

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

        // ── Botón BACK ──
        btnBack = createSmallButton("← BACK", BTN_BACK_COLOR);
        btnBack.setBounds(20, HEIGHT - 80, 100, 35);
        backgroundPanel.add(btnBack);
    }

    // ═══════════════════════════════════════
    //  PREPARAR ACCIONES
    // ═══════════════════════════════════════

    private void prepareActions() {

        btnRedPlayer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectPlayer("Red");
            }
        });

        btnGreenPlayer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectPlayer("Green");
            }
        });

        btnBluePlayer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectPlayer("Blue");
            }
        });

        btnBack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                goBack();
            }
        });
    }

    // ═══════════════════════════════════════
    //  NAVEGACIÓN
    // ═══════════════════════════════════════

    private void selectPlayer(String playerType) {
        new LevelSelectGUI(selectedMode, playerType);
        dispose();
    }

    private void goBack() {
        new ModeSelectionPanel();
        dispose();
    }

    // ═══════════════════════════════════════
    //  UTILIDADES
    // ═══════════════════════════════════════

    private String getModeDisplayText() {
        switch (selectedMode) {
            case SINGLE_PLAYER: return "Single Player";
            case PLAYER_VS_MACHINE: return "Player vs Machine";
            case PLAYER_VS_PLAYER: return "Player vs Player";
            default: return selectedMode.toString();
        }
    }

    /**
     * Crea una tarjeta visual de jugador que funciona como botón.
     * Incluye un cuadrado de color representando al personaje, su nombre y stats.
     */
    private JButton createPlayerCard(String name, Color playerColor,
                                      String stat1, String stat2, String ability,
                                      int x, int y, int w, int h) {
        JButton card = new JButton() {
            private boolean hovering = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovering = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovering = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fondo de tarjeta
                Color cardBg = hovering ? new Color(255, 255, 255, 230) : new Color(255, 255, 255, 180);
                g2.setColor(cardBg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));

                // Borde
                g2.setColor(hovering ? playerColor : playerColor.darker());
                g2.setStroke(new BasicStroke(hovering ? 3f : 2f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, 20, 20));

                // Cuadrado del personaje
                int squareSize = 60;
                int squareX = (getWidth() - squareSize) / 2;
                int squareY = 25;

                // Sombra
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRoundRect(squareX + 3, squareY + 3, squareSize, squareSize, 8, 8);

                // Cuerpo
                g2.setColor(playerColor);
                g2.fillRoundRect(squareX, squareY, squareSize, squareSize, 8, 8);

                // Borde negro
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(squareX, squareY, squareSize, squareSize, 8, 8);

                // Ojos
                g2.setColor(Color.WHITE);
                g2.fillOval(squareX + 15, squareY + 20, 12, 12);
                g2.fillOval(squareX + 33, squareY + 20, 12, 12);
                g2.setColor(Color.BLACK);
                g2.fillOval(squareX + 19, squareY + 24, 5, 5);
                g2.fillOval(squareX + 37, squareY + 24, 5, 5);

                // Nombre
                g2.setColor(playerColor.darker());
                g2.setFont(PLAYER_NAME_FONT);
                FontMetrics fmName = g2.getFontMetrics();
                g2.drawString(name, (getWidth() - fmName.stringWidth(name)) / 2, squareY + squareSize + 25);

                // Stats
                g2.setColor(new Color(60, 60, 80));
                g2.setFont(STAT_FONT);
                FontMetrics fmStat = g2.getFontMetrics();
                int statY = squareY + squareSize + 50;
                g2.drawString(stat1, (getWidth() - fmStat.stringWidth(stat1)) / 2, statY);
                g2.drawString(stat2, (getWidth() - fmStat.stringWidth(stat2)) / 2, statY + 18);

                // Habilidad
                g2.setFont(new Font("Arial", Font.ITALIC, 10));
                drawWrappedText(g2, ability, 10, statY + 45, getWidth() - 20);

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

    /**
     * Dibuja texto con salto de línea automático.
     */
    private void drawWrappedText(Graphics2D g2, String text, int x, int y, int maxWidth) {
        FontMetrics fm = g2.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int lineY = y;

        for (String word : words) {
            String test = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(test) > maxWidth) {
                String lineStr = line.toString();
                g2.drawString(lineStr, x + (maxWidth - fm.stringWidth(lineStr)) / 2, lineY);
                line = new StringBuilder(word);
                lineY += fm.getHeight();
            } else {
                if (line.length() > 0) line.append(" ");
                line.append(word);
            }
        }
        if (line.length() > 0) {
            String lineStr = line.toString();
            g2.drawString(lineStr, x + (maxWidth - fm.stringWidth(lineStr)) / 2, lineY);
        }
    }

    /**
     * Crea un botón pequeño estilizado (para BACK).
     */
    private JButton createSmallButton(String text, Color baseColor) {
        JButton button = new JButton(text) {
            private boolean hovering = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovering = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovering = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = hovering ? baseColor.brighter() : baseColor;
                g2.setColor(fill);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 2, getHeight() - 2, 14, 14));
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
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

    // ═══════════════════════════════════════
    //  MAIN (prueba independiente)
    // ═══════════════════════════════════════

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SelectPlayerGUI(GameMode.SINGLE_PLAYER));
    }
}