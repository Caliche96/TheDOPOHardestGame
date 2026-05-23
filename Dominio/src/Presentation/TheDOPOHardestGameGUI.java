package Presentation;

import Dominio.Game;
import Dominio.GameException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Image;
import java.io.File;

/**
 * Pantalla de inicio del juego — primera ventana que ve el usuario.
 *
 * Única clase de dominio accedida: Game (fachada).
 *
 * Muestra la imagen InitialScreen.png como fondo con dos botones invisibles:
 *  - PLAY GAME  → abre ModeSelectionPanel
 *  - OPEN GAME  → carga una partida guardada (.dat) y abre GamePanel
 */
public class TheDOPOHardestGameGUI extends JFrame {

    private JButton btnStart;
    private JButton btnOpenGame;
    private JLabel  background;

    public TheDOPOHardestGameGUI() {
        setTitle("The DOPO Hardest Game");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        prepareElements();
        prepareActions();
        setVisible(true);
    }

    // ── Preparar elementos ──

    private void prepareElements() {
        File file = new File("recursos/InitialScreen.png");
        if (!file.exists()) {
            JOptionPane.showMessageDialog(null,
                    Game.class.getSimpleName() + ": recurso no encontrado — recursos/InitialScreen.png",
                    "Recurso no encontrado",
                    JOptionPane.WARNING_MESSAGE);
        }

        ImageIcon bgIcon  = new ImageIcon(file.getAbsolutePath());
        Image     scaled  = bgIcon.getImage().getScaledInstance(600, 500, Image.SCALE_SMOOTH);
        background = new JLabel(new ImageIcon(scaled));
        background.setBounds(0, 0, 600, 500);

        btnStart = createInvisibleButton();
        btnStart.setBounds(40, 330, 180, 100);

        btnOpenGame = createInvisibleButton();
        btnOpenGame.setBounds(330, 330, 180, 100);

        add(btnStart);
        add(btnOpenGame);
        add(background);
    }

    private JButton createInvisibleButton() {
        JButton btn = new JButton();
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        return btn;
    }

    // ── Acciones ──

    private void prepareActions() {
        btnStart.addActionListener(e -> {
            new ModeSelectionPanel();
            dispose();
        });

        btnOpenGame.addActionListener(e -> abrirPartidaGuardada());
    }

    /**
     * Carga una partida guardada y lanza GamePanel restaurado.
     * Toda la lógica de reconstrucción vive en Game.createFromSave().
     */
    private void abrirPartidaGuardada() {
        File savesDir = new File("saves");
        if (!savesDir.exists()) savesDir.mkdirs();

        JFileChooser chooser = new JFileChooser(savesDir);
        chooser.setDialogTitle("Abrir partida guardada");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Archivos de guardado (*.dat)", "dat"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        String path = chooser.getSelectedFile().getAbsolutePath();
        try {
            // Una sola llamada: carga, reconstruye y restaura el estado completo
            Game restoredGame = Game.createFromSave(path);
            new GamePanel(restoredGame);
            dispose();
        } catch (GameException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Error al cargar la partida",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new TheDOPOHardestGameGUI();
    }
}