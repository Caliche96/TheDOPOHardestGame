package Presentation;

import Dominio.GameMode;
import java.awt.Color;

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

    // ──── Descripciones conocidas (se usan si el archivo existe) ────
    private static final java.util.Map<String, String> KNOWN_DESCRIPTIONS = new java.util.HashMap<>();
    static {
        KNOWN_DESCRIPTIONS.put("nivel1.txt", "Nivel 1 del Juego Original. Nivel de adaptación.");
        KNOWN_DESCRIPTIONS.put("nivel2.txt", "El Cruce — enemigos verticales y monedas normales. Dificultad media.");
        KNOWN_DESCRIPTIONS.put("nivel3.txt", "El Cierre — obstáculos internos, Diferentes enemigos y una SkinCoins. Dificultad media.");
    }

    // ──── Niveles cargados dinámicamente desde recursos/ ────
    private final String[] levelNames;
    private final String[] levelDescs;
    private final String[] levelFiles;

    // ──── Estado ────
    private final GameMode selectedMode;
    private final String   selectedPlayer;
    private final String   selectedPlayer2;
    private final String   machineType;
    private final Color    borderColor1;
    private final Color    borderColor2;

    // ──── Componentes ────
    private JPanel    backgroundPanel;
    private JScrollPane scrollPane;
    private JButton   btnBack;

    /**
     * Crea una nueva pantalla de selección de nivel.
     * @param mode Modo de juego.
     * @param playerType Tipo de jugador 1.
     * @param borderColor1 Color del borde del jugador 1.
     * @param player2Type Tipo de jugador 2.
     * @param borderColor2 Color del borde del jugador 2.
     * @param machineType Tipo de máquina.
     */
    public LevelSelectGUI(GameMode mode, String playerType, Color borderColor1, String player2Type, Color borderColor2, String machineType) {
        this.selectedMode    = mode;
        this.selectedPlayer  = playerType;
        this.selectedPlayer2 = player2Type;
        this.machineType     = machineType;
        this.borderColor1    = borderColor1;
        this.borderColor2    = borderColor2;

        // Cargar niveles disponibles dinámicamente
        String[][] loaded = loadAvailableLevels();
        this.levelNames = loaded[0];
        this.levelDescs = loaded[1];
        this.levelFiles = loaded[2];

        setTitle("Select Level");
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        prepareElements();
        prepareActions();
        setVisible(true);
    }

    /**
     * Escanea la carpeta recursos/ y devuelve todos los archivos nivelN.txt
     * ordenados por número. Así agregar un nivel es tan simple como
     * crear el archivo nivel4.txt, nivel5.txt, etc.
     */
    private String[][] loadAvailableLevels() {
        java.util.List<String> names = new java.util.ArrayList<>();
        java.util.List<String> descs = new java.util.ArrayList<>();
        java.util.List<String> files = new java.util.ArrayList<>();

        java.io.File folder = new java.io.File("recursos");
        if (folder.exists() && folder.isDirectory()) {
            java.io.File[] txts = folder.listFiles(
                (dir, name) -> name.matches("nivel\\d+\\.txt"));

            if (txts != null) {
                // Ordenar por número de nivel
                java.util.Arrays.sort(txts, (a, b) -> {
                    int na = extractNumber(a.getName());
                    int nb = extractNumber(b.getName());
                    return Integer.compare(na, nb);
                });

                for (java.io.File f : txts) {
                    int num = extractNumber(f.getName());
                    names.add("Nivel " + num);
                    descs.add(KNOWN_DESCRIPTIONS.getOrDefault(
                            f.getName(), "Nivel " + num + " — completa el nivel para avanzar."));
                    files.add("recursos/" + f.getName());
                }
            }
        }

        // Fallback si la carpeta no existe o no hay niveles
        if (names.isEmpty()) {
            names.add("Nivel 1");
            descs.add("No se encontraron niveles en recursos/");
            files.add("recursos/nivel1.txt");
        }

        return new String[][]{ names.toArray(new String[0]),
                               descs.toArray(new String[0]),
                               files.toArray(new String[0]) };
    }

    /** Extrae el número de un nombre de archivo como "nivel3.txt" → 3. */
    private int extractNumber(String filename) {
        try {
            return Integer.parseInt(filename.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ═══════════════════════════════════════
    //  PREPARAR ELEMENTOS
    // ═══════════════════════════════════════

    /**
     * Prepara los elementos de la interfaz de usuario.
     */
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

        // ── Subtítulo ──
        JLabel lblSub = new JLabel(
                getModeText() + "  ·  Player: " + selectedPlayer,
                SwingConstants.CENTER);
        lblSub.setFont(new Font("Arial", Font.ITALIC, 13));
        lblSub.setForeground(new Color(60, 60, 100));
        lblSub.setBounds(0, 65, WIDTH, 20);
        backgroundPanel.add(lblSub);

        // ── Panel de tarjetas con scroll ──
        int cardW  = 460;
        int cardH  = 80;
        int gap    = 18;
        int totalH = levelNames.length * (cardH + gap) + 20;

        JPanel cardsPanel = new JPanel(null);
        cardsPanel.setOpaque(false);
        cardsPanel.setPreferredSize(new Dimension(WIDTH, totalH));

        int cardX = (WIDTH - cardW) / 2;
        for (int i = 0; i < levelNames.length; i++) {
            final int index = i;
            JButton card = createLevelCard(index, cardW, cardH);
            card.setBounds(cardX, 10 + i * (cardH + gap), cardW, cardH);
            cardsPanel.add(card);
        }

        // ScrollPane transparente
        scrollPane = new JScrollPane(cardsPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBounds(0, 95, WIDTH, HEIGHT - 145);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        backgroundPanel.add(scrollPane);

        // ── Botón BACK ──
        btnBack = createSmallButton("← BACK", BTN_BACK_COLOR);
        btnBack.setBounds(20, HEIGHT - 75, 100, 35);
        backgroundPanel.add(btnBack);
    }

    // ═══════════════════════════════════════
    //  PREPARAR ACCIONES
    // ═══════════════════════════════════════

    private void prepareActions() {
        btnBack.addActionListener(e -> goBack());
    }

    // ═══════════════════════════════════════
    //  NAVEGACIÓN
    // ═══════════════════════════════════════

    private void selectLevel(int index) {
        new GamePanel(selectedMode, selectedPlayer, borderColor1, selectedPlayer2, borderColor2, machineType, levelFiles[index]);
        dispose();
    }

    private void goBack() {
        new SelectPlayerGUI(selectedMode);
        dispose();
    }

    // ═══════════════════════════════════════
    //  UTILIDADES
    // ═══════════════════════════════════════

    private String getModeText() {
        switch (selectedMode) {
            case SINGLE_PLAYER:     return "Single Player";
            case PLAYER_VS_MACHINE: return "Player vs Machine";
            case PLAYER_VS_PLAYER:  return "Player vs Player";
            default:                return selectedMode.toString();
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
                g2.drawString(levelNames[index], 80, 30);

                // Descripción
                g2.setColor(new Color(60, 60, 90));
                g2.setFont(LEVEL_DESC_FONT);
                g2.drawString(levelDescs[index], 80, 52);

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
                String playTxt = "Jugar";
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
}