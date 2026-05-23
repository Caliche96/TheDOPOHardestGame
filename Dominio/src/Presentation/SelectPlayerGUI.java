package Presentation;

import Dominio.GameMode;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;

/**
 * Pantalla de selección de personaje y color de borde.
 *
 * Flujo en dos pasos:
 *  1. El jugador elige su skin (Rojo / Verde / Azul) — las tarjetas resaltan al hacer clic.
 *  2. Aparece el selector de color de borde (8 opciones) y el botón CONFIRM.
 *
 * En modos PvP y PvM se muestra dos veces (Player 1 y Player 2).
 */
public class SelectPlayerGUI extends JFrame {

    // ──── Dimensiones ────
    private static final int WIDTH  = 600;
    private static final int HEIGHT = 500;

    // ──── Paleta ────
    private static final Color BG_TOP         = new Color(190, 200, 255);
    private static final Color BG_BOTTOM      = new Color(140, 155, 235);
    private static final Color RED_COLOR      = new Color(220, 50,  50);
    private static final Color GREEN_COLOR    = new Color(40,  170, 60);
    private static final Color BLUE_COLOR     = new Color(40,  100, 220);
    private static final Color BTN_BACK_COLOR = new Color(120, 120, 130);
    private static final Color BTN_CONFIRM_COLOR = new Color(40, 160, 80);

    // ──── Colores de borde disponibles ────
    private static final Color[] BORDER_COLORS = {
        Color.WHITE,
        new Color(255, 215, 0),    // dorado
        new Color(255, 100, 0),    // naranja
        new Color(255, 80,  180),  // rosa
        new Color(160, 50,  220),  // violeta
        new Color(0,   200, 220),  // cyan
        new Color(120, 230, 30),   // lima
        new Color(30,  30,  30)    // negro
    };

    // ──── Fuentes ────
    private static final Font TITLE_FONT  = new Font("Arial", Font.BOLD,  36);
    private static final Font STAT_FONT   = new Font("Arial", Font.PLAIN, 11);
    private static final Font LABEL_FONT  = new Font("Arial", Font.BOLD,  13);

    // ──── Componentes ────
    private JButton   btnRedPlayer, btnGreenPlayer, btnBluePlayer;
    private JButton   btnBack, btnConfirm;
    private JButton[] borderSwatches;
    private JPanel    colorRow;
    private JPanel    backgroundPanel;

    // ──── Estado ────
    private final GameMode selectedMode;
    private final String   machineType;
    private final int      playerNumber;
    private final String   player1Type;
    private final Color    player1BorderColor;

    private String selectedType        = null;
    private Color  selectedBorderColor = null;
    private int    selectedSwatchIdx   = -1;

    // ══════════════════════════════════════════════
    //  CONSTRUCTORES PÚBLICOS
    // ══════════════════════════════════════════════

    public SelectPlayerGUI(GameMode mode) {
        this(mode, null, 1, null, null);
    }

    public SelectPlayerGUI(GameMode mode, String machineType) {
        this(mode, machineType, 1, null, null);
    }

    // ══════════════════════════════════════════════
    //  CONSTRUCTOR INTERNO GENERAL
    // ══════════════════════════════════════════════

    private SelectPlayerGUI(GameMode mode, String machineType, int playerNumber,
                             String player1Type, Color player1BorderColor) {
        this.selectedMode=mode;
        this.machineType=machineType;
        this.playerNumber=playerNumber;
        this.player1Type = player1Type;
        this.player1BorderColor = player1BorderColor;

        setTitle("Select Player " + playerNumber);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        prepareElements();
        prepareActions();
        setVisible(true);
    }

    // ══════════════════════════════════════════════
    //  PREPARAR ELEMENTOS
    // ══════════════════════════════════════════════

    private void prepareElements() {
        backgroundPanel = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, BG_TOP, 0, getHeight(), BG_BOTTOM);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        setContentPane(backgroundPanel);

        // ── Título ──
        String titleText = playerNumber == 1 ? "SELECT PLAYER 1" : "SELECT PLAYER 2";
        JLabel lblTitle = new JLabel(titleText, SwingConstants.CENTER);
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(playerNumber == 1 ? new Color(30, 50, 140) : new Color(140, 30, 30));
        lblTitle.setBounds(0, 20, WIDTH, 40);
        backgroundPanel.add(lblTitle);

        // ── Subtítulo ──
        String sub = "Mode: " + getModeDisplayText();
        if (playerNumber == 2) sub += "  ·  P1: " + player1Type;
        JLabel lblMode = new JLabel(sub, SwingConstants.CENTER);
        lblMode.setFont(new Font("Arial", Font.ITALIC, 13));
        lblMode.setForeground(new Color(60, 60, 100));
        lblMode.setBounds(0, 62, WIDTH, 18);
        backgroundPanel.add(lblMode);

        // ── Tarjetas de personaje ──
        int cardW = 150, cardH = 220, gap = 25;
        int startX = (WIDTH - (3 * cardW + 2 * gap)) / 2;
        int cardY  = 85;

        btnRedPlayer   = createPlayerCard("RED",   RED_COLOR,
                "Speed 1.0 · Size 1.0", "Normal",
                startX,                   cardY, cardW, cardH);
        btnGreenPlayer = createPlayerCard("GREEN", GREEN_COLOR,
                "Speed 1.0 · Size 1.0", "No le pasa nada con el primer golpe", // habilidad: ignora el primer golpe que reciba
                startX + cardW + gap,     cardY, cardW, cardH);
        btnBluePlayer  = createPlayerCard("BLUE",  BLUE_COLOR,
                "Speed 1.5 · Size 1.5", "Grande y rápido",
                startX + 2*(cardW + gap), cardY, cardW, cardH);

        backgroundPanel.add(btnRedPlayer);
        backgroundPanel.add(btnGreenPlayer);
        backgroundPanel.add(btnBluePlayer);

        // ── Selector de color de borde (oculto hasta elegir personaje) ──
        colorRow = buildColorRow();
        colorRow.setBounds(0, 318, WIDTH, 90);
        colorRow.setVisible(false);
        backgroundPanel.add(colorRow);

        // ── Botón CONFIRM (oculto hasta elegir color) ──
        btnConfirm = createSmallButton("CONFIRM", BTN_CONFIRM_COLOR);
        btnConfirm.setBounds(WIDTH / 2 - 80, 418, 160, 38);
        btnConfirm.setVisible(false);
        backgroundPanel.add(btnConfirm);

        // ── Botón BACK ──
        btnBack = createSmallButton("BACK", BTN_BACK_COLOR);
        btnBack.setBounds(20, HEIGHT - 55, 100, 34);
        backgroundPanel.add(btnBack);
    }

    // ══════════════════════════════════════════════
    //  FILA DE COLORES DE BORDE
    // ══════════════════════════════════════════════

    private JPanel buildColorRow() {
        JPanel panel = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                // Etiqueta
                g2.setColor(new Color(30, 30, 80));
                g2.setFont(new Font("Arial", Font.BOLD, 13));
                g2.drawString("Escoja el color del Borde:", 20, 22);
            }
        };
        panel.setOpaque(false);

        borderSwatches = new JButton[BORDER_COLORS.length];
        int swatchSize = 34;
        int totalW     = BORDER_COLORS.length * swatchSize + (BORDER_COLORS.length - 1) * 10;
        int swatchX    = (WIDTH - totalW) / 2;
        int swatchY    = 32;

        for (int i = 0; i < BORDER_COLORS.length; i++) {
            final int idx   = i;
            final Color col = BORDER_COLORS[i];

            JButton swatch = new JButton() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);

                    // Círculo de color
                    g2.setColor(col);
                    g2.fillOval(2, 2, getWidth() - 4, getHeight() - 4);

                    // Borde: grueso y negro si está seleccionado, gris si no
                    if (selectedSwatchIdx == idx) {
                        g2.setColor(new Color(30, 30, 30));
                        g2.setStroke(new BasicStroke(3.5f));
                    } else {
                        g2.setColor(new Color(120, 120, 130));
                        g2.setStroke(new BasicStroke(1.5f));
                    }
                    g2.drawOval(2, 2, getWidth() - 4, getHeight() - 4);

                    // Check si seleccionado
                    if (selectedSwatchIdx == idx) {
                        g2.setColor(col.equals(Color.WHITE) || isBright(col)
                                ? Color.DARK_GRAY : Color.WHITE);
                        g2.setFont(new Font("Arial", Font.BOLD, 14));
                        g2.drawString("X", getWidth() / 2 - 5, getHeight() / 2 + 6);
                    }
                    g2.dispose();
                }
            };

            swatch.setBounds(swatchX + i * (swatchSize + 10), swatchY, swatchSize, swatchSize);
            swatch.setContentAreaFilled(false);
            swatch.setBorderPainted(false);
            swatch.setFocusPainted(false);
            swatch.setCursor(new Cursor(Cursor.HAND_CURSOR));
            swatch.addActionListener(e -> onColorSelected(idx, col));

            borderSwatches[i] = swatch;
            panel.add(swatch);
        }
        return panel;
    }

    private boolean isBright(Color c) {
        return (c.getRed() * 299 + c.getGreen() * 587 + c.getBlue() * 114) / 1000 > 128;
    }

    // ══════════════════════════════════════════════
    //  PREPARAR ACCIONES
    // ══════════════════════════════════════════════

    private void prepareActions() {
        btnRedPlayer.addActionListener(e   -> onTypeSelected("Red",   btnRedPlayer));
        btnGreenPlayer.addActionListener(e -> onTypeSelected("Green", btnGreenPlayer));
        btnBluePlayer.addActionListener(e  -> onTypeSelected("Blue",  btnBluePlayer));

        btnBack.addActionListener(e    -> goBack());
        btnConfirm.addActionListener(e -> confirm());
    }

    // ══════════════════════════════════════════════
    //  LÓGICA DE SELECCIÓN
    // ══════════════════════════════════════════════

    private void onTypeSelected(String type, JButton sourceCard) {
        selectedType        = type;
        selectedBorderColor = null;
        selectedSwatchIdx   = -1;

        // Resaltar la tarjeta elegida
        btnRedPlayer.putClientProperty("selected",   btnRedPlayer   == sourceCard);
        btnGreenPlayer.putClientProperty("selected", btnGreenPlayer == sourceCard);
        btnBluePlayer.putClientProperty("selected",  btnBluePlayer  == sourceCard);
        btnRedPlayer.repaint();
        btnGreenPlayer.repaint();
        btnBluePlayer.repaint();

        // Mostrar el selector de color y repintar swatches
        colorRow.setVisible(true);
        if (borderSwatches != null)
            for (JButton sw : borderSwatches) sw.repaint();

        btnConfirm.setVisible(false);
    }

    private void onColorSelected(int idx, Color color) {
        selectedBorderColor = color;
        selectedSwatchIdx   = idx;

        if (borderSwatches != null)
            for (JButton sw : borderSwatches) sw.repaint();

        // Mostrar CONFIRM solo si ya tiene ambos datos
        btnConfirm.setVisible(selectedType != null);
    }

    private void confirm() {
        if (selectedType == null || selectedBorderColor == null) return;
        navigateForward(selectedType, selectedBorderColor);
    }

    // ══════════════════════════════════════════════
    //  NAVEGACIÓN
    // ══════════════════════════════════════════════

    private void navigateForward(String type, Color borderColor) {
        if (playerNumber == 1 && selectedMode.isMultiplayer()) {
            if (selectedMode == GameMode.PLAYER_VS_MACHINE) {
                // Máquina no elige — ir directo a niveles
                new LevelSelectGUI(selectedMode, type, borderColor, null, null, machineType);
            } else {
                // PvP: Player 2 también elige
                new SelectPlayerGUI(selectedMode, machineType, 2, type, borderColor);
            }
        } else if (playerNumber == 2) {
            new LevelSelectGUI(selectedMode, player1Type, player1BorderColor,
                               type, borderColor, machineType);
        } else {
            // Single player
            new LevelSelectGUI(selectedMode, type, borderColor, null, null, null);
        }
        dispose();
    }

    private void goBack() {
        if (playerNumber == 2) new SelectPlayerGUI(selectedMode, machineType);
        else                   new ModeSelectionPanel();
        dispose();
    }

    // ══════════════════════════════════════════════
    //  TARJETA DE PERSONAJE
    // ══════════════════════════════════════════════

    private JButton createPlayerCard(String name, Color playerColor,
                                     String stats, String ability,
                                     int x, int y, int w, int h) {
        JButton card = new JButton() {
            private boolean hovering = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovering = true;  repaint(); }
                    @Override public void mouseExited (MouseEvent e) { hovering = false; repaint(); }
                });
            }

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);

                boolean selected = Boolean.TRUE.equals(getClientProperty("selected"));

                // Fondo de la tarjeta
                Color cardBg = selected ? new Color(255, 255, 255, 240)
                             : hovering ? new Color(255, 255, 255, 210)
                             :            new Color(255, 255, 255, 170);
                g2.setColor(cardBg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 18, 18));

                // Borde: más grueso si está seleccionado
                g2.setColor(selected ? playerColor.darker() : (hovering ? playerColor : playerColor.darker()));
                g2.setStroke(new BasicStroke(selected ? 3.5f : hovering ? 2.5f : 1.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, 18, 18));

                // Cuadrado del personaje
                int sq = 55, sqX = (getWidth() - sq) / 2, sqY = 18;
                g2.setColor(new Color(0, 0, 0, 35));
                g2.fillRoundRect(sqX + 3, sqY + 3, sq, sq, 8, 8);
                g2.setColor(playerColor);
                g2.fillRoundRect(sqX, sqY, sq, sq, 8, 8);
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(sqX, sqY, sq, sq, 8, 8);
                // Ojos
                g2.setColor(Color.WHITE);
                g2.fillOval(sqX + 13, sqY + 18, 11, 11);
                g2.fillOval(sqX + 31, sqY + 18, 11, 11);
                g2.setColor(Color.BLACK);
                g2.fillOval(sqX + 17, sqY + 22, 4, 4);
                g2.fillOval(sqX + 35, sqY + 22, 4, 4);

                // Nombre
                g2.setColor(playerColor.darker());
                g2.setFont(new Font("Arial", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(name, (getWidth() - fm.stringWidth(name)) / 2, sqY + sq + 22);

                // Stats y habilidad
                g2.setColor(new Color(50, 50, 70));
                g2.setFont(STAT_FONT);
                FontMetrics fmS = g2.getFontMetrics();
                int ty = sqY + sq + 42;
                g2.drawString(stats,   (getWidth() - fmS.stringWidth(stats))   / 2, ty);
                g2.setFont(new Font("Arial", Font.ITALIC, 10));
                g2.setColor(new Color(80, 80, 100));
                drawWrapped(g2, ability, 8, ty + 18, getWidth() - 16);

                // Indicador de selección
                if (selected) {
                    g2.setColor(playerColor);
                    g2.setFont(new Font("Arial", Font.BOLD, 11));
                    String sel = "Jugador Seleccionado";
                    FontMetrics fmSel = g2.getFontMetrics();
                    g2.drawString(sel, (getWidth() - fmSel.stringWidth(sel)) / 2,
                                  getHeight() - 8);
                }
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

    // ══════════════════════════════════════════════
    //  UTILIDADES
    // ══════════════════════════════════════════════

    private void drawWrapped(Graphics2D g2, String text, int x, int y, int maxW) {
        FontMetrics fm = g2.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int ly = y;
        for (String w : words) {
            String test = line.length() == 0 ? w : line + " " + w;
            if (fm.stringWidth(test) > maxW) {
                String ls = line.toString();
                g2.drawString(ls, x + (maxW - fm.stringWidth(ls)) / 2, ly);
                line = new StringBuilder(w);
                ly  += fm.getHeight();
            } else {
                if (line.length() > 0) line.append(" ");
                line.append(w);
            }
        }
        if (line.length() > 0) {
            String ls = line.toString();
            g2.drawString(ls, x + (maxW - fm.stringWidth(ls)) / 2, ly);
        }
    }

    private String getModeDisplayText() {
        switch (selectedMode) {
            case SINGLE_PLAYER:     return "Single Player";
            case PLAYER_VS_MACHINE: return "Player vs Machine";
            case PLAYER_VS_PLAYER:  return "Player vs Player";
            default:                return selectedMode.toString();
        }
    }

    private JButton createSmallButton(String text, Color baseColor) {
        JButton btn = new JButton(text) {
            private boolean hov = false;
            { addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                @Override public void mouseExited (MouseEvent e) { hov = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov ? baseColor.brighter() : baseColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 2, getHeight() - 2, 12, 12));
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth()  - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(LABEL_FONT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SelectPlayerGUI(GameMode.PLAYER_VS_PLAYER));
    }
}