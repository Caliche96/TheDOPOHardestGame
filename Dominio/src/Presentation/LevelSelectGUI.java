package Presentation;

import Dominio.GameMode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Pantalla de selección de nivel.
 * Flujo: SelectPlayerGUI → LevelSelectGUI → GamePanel (pendiente)
 *
 * Recibe GameMode y playerType, muestra 3 niveles en lista vertical.
 * Al elegir uno carga el .txt correspondiente y abre el juego.
 */
public class LevelSelectGUI extends JFrame {

    // ──── Constantes de diseño ────
    private static final int WIDTH = 600;
    private static final int HEIGHT = 500;

    private static final Color BG_TOP    = new Color(190, 200, 255);
    private static final Color BG_BOTTOM = new Color(140, 155, 235);

    private static final Color CARD_AVAILABLE = new Color(255, 255, 255, 200);
    private static final Color CARD_BORDER    = new Color(60, 80, 180);
    private static final Color BTN_BACK_COLOR = new Color(120, 120, 130);

    private static final Font TITLE_FONT      = new Font("Arial", Font.BOLD, 36);
    private static final Font LEVEL_NAME_FONT = new Font("Arial", Font.BOLD, 16);
    private static final Font LEVEL_DESC_FONT = new Font("Arial", Font.PLAIN, 12);
    private static final Font BTN_PLAY_FONT   = new Font("Arial", Font.BOLD, 13);
    private static final Font BTN_BACK_FONT   = new Font("Arial", Font.BOLD, 13);

    // ──── Datos de los niveles ────
    private static final String[] LEVEL_NAMES = { "Nivel 1", "Nivel 2", "Nivel 3" };
    private static final String[] LEVEL_DESCS = {
        "El comienzo. Enemigos lentos y pocas monedas.",
        "La dificultad sube. Más enemigos y obstáculos.",
        "El verdadero reto. Velocidad máxima, sin piedad."
    };
    private static final String[] LEVEL_FILES = {
        "recursos/nivel1.txt",
        "recursos/nivel2.txt",
        "recursos/nivel3.txt"
    };

    // ──── Estado ────
    private final GameMode  selectedMode;
    private final String    selectedPlayer;
    private final String    selectedPlayer2;
    private final String    machineType;

    // ──── Componentes ────
    private JPanel backgroundPanel;
    private JButton btnBack;

    public LevelSelectGUI(GameMode mode, String playerType, String player2Type, String machineType) {
        this.selectedMode    = mode;
        this.selectedPlayer  = playerType;
        this.selectedPlayer2 = player2Type;
        this.machineType    = machineType;

        setTitle("Select Level");
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

        // Panel con gradiente de fondo
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

        // ── Título ──
        JLabel lblTitle = new JLabel("SELECT LEVEL", SwingConstants.CENTER);
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(new Color(30, 50, 140));
        lblTitle.setBounds(0, 20, WIDTH, 45);
        backgroundPanel.add(lblTitle);

        // ── Subtítulo: modo y personaje elegidos ──
        JLabel lblSub = new JLabel(
                getModeText() + "  ·  Player: " + selectedPlayer,
                SwingConstants.CENTER);
        lblSub.setFont(new Font("Arial", Font.ITALIC, 13));
        lblSub.setForeground(new Color(60, 60, 100));
        lblSub.setBounds(0, 65, WIDTH, 20);
        backgroundPanel.add(lblSub);

        // ── Tarjetas de nivel (lista vertical) ──
        int cardW  = 460;
        int cardH  = 80;
        int cardX  = (WIDTH - cardW) / 2;
        int startY = 105;
        int gap    = 18;

        for (int i = 0; i < LEVEL_NAMES.length; i++) {
            final int index = i;
            JButton card = createLevelCard(index, cardW, cardH);
            card.setBounds(cardX, startY + i * (cardH + gap), cardW, cardH);
            backgroundPanel.add(card);
        }

        // ── Botón BACK ──
        btnBack = createSmallButton("← BACK", BTN_BACK_COLOR);
        btnBack.setBounds(20, HEIGHT - 75, 100, 35);
        backgroundPanel.add(btnBack);
    }

    // ═══════════════════════════════════════
    //  PREPARAR ACCIONES
    // ═══════════════════════════════════════

    private void prepareActions() {
        btnBack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                goBack();
            }
        });
    }

    // ═══════════════════════════════════════
    //  NAVEGACIÓN
    // ═══════════════════════════════════════

    private void selectLevel(int index) {
        new GamePanel(selectedMode, selectedPlayer, selectedPlayer2, machineType, LEVEL_FILES[index]);
        dispose();
    }

    private void goBack() {
        new SelectPlayerGUI(selectedMode);  // regresa a selección P1
        dispose();
    }

    // ═══════════════════════════════════════
    //  UTILIDADES
    // ═══════════════════════════════════════

    private String getModeText() {
        switch (selectedMode) {
            case SINGLE_PLAYER:      return "Single Player";
            case PLAYER_VS_MACHINE:  return "Player vs Machine";
            case PLAYER_VS_PLAYER:   return "Player vs Player";
            default:                 return selectedMode.toString();
        }
    }

    /**
     * Crea una tarjeta horizontal de nivel con nombre, descripción y botón PLAY.
     */
    private JButton createLevelCard(int index, int w, int h) {

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

                // Fondo de tarjeta
                Color bg = hovering
                        ? new Color(230, 235, 255, 240)
                        : CARD_AVAILABLE;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 18, 18));

                // Borde
                g2.setColor(hovering ? CARD_BORDER.brighter() : CARD_BORDER);
                g2.setStroke(new BasicStroke(hovering ? 2.5f : 1.8f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, 18, 18));

                // Número de nivel (círculo izquierdo)
                int circleSize = 46;
                int circleX    = 16;
                int circleY    = (getHeight() - circleSize) / 2;
                g2.setColor(CARD_BORDER);
                g2.fillOval(circleX, circleY, circleSize, circleSize);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 18));
                String num = String.valueOf(index + 1);
                FontMetrics fmNum = g2.getFontMetrics();
                g2.drawString(num,
                        circleX + (circleSize - fmNum.stringWidth(num)) / 2,
                        circleY + (circleSize + fmNum.getAscent() - fmNum.getDescent()) / 2);

                // Nombre del nivel
                g2.setColor(new Color(30, 50, 140));
                g2.setFont(LEVEL_NAME_FONT);
                g2.drawString(LEVEL_NAMES[index], 80, 30);

                // Descripción
                g2.setColor(new Color(60, 60, 90));
                g2.setFont(LEVEL_DESC_FONT);
                g2.drawString(LEVEL_DESCS[index], 80, 52);

                // Botón PLAY (derecha)
                int playW = 70, playH = 32;
                int playX = getWidth() - playW - 14;
                int playY = (getHeight() - playH) / 2;
                Color playColor = hovering ? new Color(40, 160, 60) : new Color(30, 130, 50);
                g2.setColor(playColor);
                g2.fill(new RoundRectangle2D.Float(playX, playY, playW, playH, 12, 12));
                g2.setColor(Color.WHITE);
                g2.setFont(BTN_PLAY_FONT);
                FontMetrics fmPlay = g2.getFontMetrics();
                String playTxt = "PLAY ▶";
                g2.drawString(playTxt,
                        playX + (playW - fmPlay.stringWidth(playTxt)) / 2,
                        playY + (playH + fmPlay.getAscent() - fmPlay.getDescent()) / 2);

                g2.dispose();
            }
        };

        card.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectLevel(index);
            }
        });

        card.setFocusPainted(false);
        card.setBorderPainted(false);
        card.setContentAreaFilled(false);
        card.setOpaque(false);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return card;
    }

    /**
     * Crea un botón pequeño estilizado (para BACK).
     */
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

        button.setFont(BTN_BACK_FONT);
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
        SwingUtilities.invokeLater(() ->
                new LevelSelectGUI(GameMode.SINGLE_PLAYER, "Red", null, null));
    }
}