package Presentation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MenuGUI extends JFrame {
	private JButton btnPlay;
    private JButton btnScores;
    private JButton btnHelp;
    private JButton btnCredits;

    private Image backgroundImage;

    public MenuGUI() {
        setTitle("Menu");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null); 

        

        prepareElements();
        prepareActions();

        setVisible(true);
    }

    /** 
     *  CREAR ELEMENTOS GRÁFICOS
     */
    private void prepareElements() {
    	
    	//Mensaje en la parte de Arriba de Elegir la modalidad
    	

        // BOTÓN PLAY
        btnPlay = new JButton("PLAYER");
        btnPlay.setBounds(220, 180, 160, 40);
        add(btnPlay);

        // BOTÓN SCORES
        btnScores = new JButton("PLAVER VS MACHINE");
        btnScores.setBounds(220, 230, 160, 40);
        add(btnScores);

        // BOTÓN HELP
        btnHelp = new JButton("PLAYER VS PLAYER");
        btnHelp.setBounds(220, 280, 160, 40);
        add(btnHelp);

        // BOTÓN CREDITS
        btnCredits = new JButton("CREDITS");
        btnCredits.setBounds(220, 330, 160, 40);
        add(btnCredits);
    }

    /**
     *  ACCIONES 
     */
    private void prepareActions() {

        // Acción general "En construcción"
        ActionListener constructionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,
                        "Esta sección está en construcción.",
                        "Información",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        };

        
        btnPlay.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                abrirPlayMenu();
            }
        });
        btnScores.addActionListener(constructionListener);
        btnHelp.addActionListener(constructionListener);
        btnCredits.addActionListener(constructionListener);
    }

    /** 
     *  PANEL PARA DIBUJAR FONDO
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }

    public static void main(String[] args) {
        new MenuGUI();
    }
    private void abrirPlayMenu() {
        new PlayMenuGUI();
        dispose();
    }
}
