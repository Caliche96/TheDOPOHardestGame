package Presentation;

import Dominio.GameMode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Pantalla de selección de modalidad de juego.
 * Flujo: TheDOPOHardestGameGUI → MenuGUI → ModeSelectionPanel → SelectPlayerGUI
 * 
 * Permite elegir entre:
 *  - PLAYER (un jugador)
 *  - PLAYER VS MACHINE (jugador contra máquina)
 *  - PLAYER VS PLAYER (dos jugadores)
 */
public class ModeSelectionPanel extends JFrame {

    // ──── Constantes de diseño ────
    private static final int WIDTH = 600;
    private static final int HEIGHT = 500;

    private static final Color BG_TOP = new Color(190, 200, 255);
    private static final Color BG_BOTTOM = new Color(140, 155, 235);

    private static final Color BTN_PLAYER_COLOR = new Color(220, 40, 40);
    private static final Color BTN_PVM_COLOR = new Color(30, 80, 200);
    private static final Color BTN_PVP_COLOR = new Color(20, 160, 50);

    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 36);
    private static final Font SUBTITLE_FONT = new Font("Arial", Font.PLAIN, 14);
    private static final Font BTN_FONT = new Font("Arial", Font.BOLD, 18);

    // ──── Componentes ────
    private JButton btnPlayer;
    private JButton btnPlayerVsMachine;
    private JButton btnPlayerVsPlayer;
    // ──── Panel con fondo degradado ────
    private JPanel backgroundPanel;

    public ModeSelectionPanel() {
        setTitle("Select Mode");
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        prepareElements();
        prepareActions();

        setVisible(true);
    }

    // ═══════════════════════════════════════
    //  PREPARAR ELEMENTOS GRÁFICOS
    // ═══════════════════════════════════════

    private void prepareElements() {
        // Panel de fondo con gradiente
        backgroundPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(
                        0, 0, BG_TOP,
                        0, getHeight(), BG_BOTTOM
                );
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        backgroundPanel.setBounds(0, 0, WIDTH, HEIGHT);
        setContentPane(backgroundPanel);

        // ── Título ──
        JLabel lblTitle = new JLabel("SELECT MODE", SwingConstants.CENTER);
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(new Color(30, 50, 140));
        lblTitle.setBounds(0, 40, WIDTH, 45);
        backgroundPanel.add(lblTitle);

        // ── Subtítulo ──
        JLabel lblSubtitle = new JLabel("Choose your game mode to continue", SwingConstants.CENTER);
        lblSubtitle.setFont(SUBTITLE_FONT);
        lblSubtitle.setForeground(new Color(60, 60, 100));
        lblSubtitle.setBounds(0, 85, WIDTH, 20);
        backgroundPanel.add(lblSubtitle);

        // ── Botones de modalidad ──
        int btnWidth = 300;
        int btnHeight = 60;
        int btnX = (WIDTH - btnWidth) / 2;
        int startY = 140;
        int gap = 20;

        btnPlayer = createStyledButton("PLAYER", BTN_PLAYER_COLOR);
        btnPlayer.setBounds(btnX, startY, btnWidth, btnHeight);
        backgroundPanel.add(btnPlayer);

        btnPlayerVsMachine = createStyledButton("PLAYER VS MACHINE", BTN_PVM_COLOR);
        btnPlayerVsMachine.setBounds(btnX, startY + btnHeight + gap, btnWidth, btnHeight);
        backgroundPanel.add(btnPlayerVsMachine);

        btnPlayerVsPlayer = createStyledButton("PLAYER VS PLAYER", BTN_PVP_COLOR);
        btnPlayerVsPlayer.setBounds(btnX, startY + 2 * (btnHeight + gap), btnWidth, btnHeight);
        backgroundPanel.add(btnPlayerVsPlayer);

        // ── Descripciones debajo de cada botón ──
        addDescription("Single player mode — collect coins and reach the goal!",
                btnX, startY + btnHeight, btnWidth);
        addDescription("Play against the machine — random or expert AI",
                btnX, startY + btnHeight + gap + btnHeight, btnWidth);
        addDescription("Two players — compete head to head!",
                btnX, startY + 2 * (btnHeight + gap) + btnHeight, btnWidth);

    }

    // ═══════════════════════════════════════
    //  PREPARAR ACCIONES
    // ═══════════════════════════════════════

    private void prepareActions() {

        btnPlayer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectMode(GameMode.SINGLE_PLAYER);
            }
        });

        btnPlayerVsMachine.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chooseMachineType();
            }
        });

        btnPlayerVsPlayer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectMode(GameMode.PLAYER_VS_PLAYER);
            }
        });

    }

    // ═══════════════════════════════════════
    //  NAVEGACIÓN
    // ═══════════════════════════════════════

    /**
     * Avanza a la pantalla de selección de personaje con el modo elegido.
     */
    private void selectMode(GameMode mode) {
        new SelectPlayerGUI(mode);
        dispose();
    }

    /**
     * Muestra diálogo para elegir el tipo de máquina (Aleatoria / Experta)
     * antes de avanzar con PvM.
     */
    private void chooseMachineType() {
        String[] options = {"Random", "Expert", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Select the machine difficulty:",
                "Player vs Machine",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0 || choice == 1) {
            String machineType = (choice == 1) ? "Expert" : "Random";
            new SelectPlayerGUI(GameMode.PLAYER_VS_MACHINE, machineType);
            dispose();
        }
    }

    // ═══════════════════════════════════════
    //  UTILIDADES DE DISEÑO
    // ═══════════════════════════════════════

    /**
     * Crea un botón estilizado con color personalizado, bordes redondeados y efecto hover.
     */
    private JButton createStyledButton(String text, Color baseColor) {
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

                Color fillColor = hovering ? baseColor.brighter() : baseColor;
                Color shadow = baseColor.darker().darker();

                // Sombra
                g2.setColor(new Color(shadow.getRed(), shadow.getGreen(), shadow.getBlue(), 80));
                g2.fill(new RoundRectangle2D.Float(3, 3, getWidth() - 4, getHeight() - 4, 20, 20));

                // Fondo del botón
                g2.setColor(fillColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 4, getHeight() - 4, 20, 20));

                // Brillo superior
                GradientPaint shine = new GradientPaint(
                        0, 0, new Color(255, 255, 255, 90),
                        0, getHeight() / 2, new Color(255, 255, 255, 0)
                );
                g2.setPaint(shine);
                g2.fill(new RoundRectangle2D.Float(2, 2, getWidth() - 8, getHeight() / 2 - 2, 18, 18));

                // Texto
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2 - 2;
                g2.drawString(getText(), textX, textY);

                g2.dispose();
            }
        };

        button.setFont(BTN_FONT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    /**
     * Agrega una etiqueta de descripción pequeña debajo de un botón.
     */
    private void addDescription(String text, int x, int y, int width) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.ITALIC, 11));
        lbl.setForeground(new Color(50, 50, 90, 180));
        lbl.setBounds(x, y + 1, width, 16);
        backgroundPanel.add(lbl);
    }

    // ═══════════════════════════════════════
    //  MAIN (para prueba independiente)
    // ═══════════════════════════════════════

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ModeSelectionPanel());
    }
}