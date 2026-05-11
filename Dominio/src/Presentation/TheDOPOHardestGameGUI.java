package Presentation;

import javax.swing.*;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class TheDOPOHardestGameGUI extends JFrame {

	private JButton btnStart;
	private JLabel background;
	
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
	
	/**
     * Prepara todos los elementos gráficos del frame
     */
    private void prepareElements() {

        // Imagen de fondo
        File file = new File("recursos/InitialScreen.png");

        if (!file.exists()) {
            System.out.println(" No se encontró la imagen en: " + file.getAbsolutePath());
        }

        ImageIcon bgIcon = new ImageIcon(file.getAbsolutePath());
        Image scaledImage = bgIcon.getImage().getScaledInstance( 600,500, Image.SCALE_SMOOTH);
        
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        
        background = new JLabel(scaledIcon);
        background.setBounds(0,0,600,500);
        
        btnStart = new JButton();
        btnStart.setBounds(40, 330 , 180, 80);		
        btnStart.setOpaque(false);
        btnStart.setContentAreaFilled(false);
        btnStart.setBorderPainted(false);

        // Agregar en el orden correcto
        add(btnStart);
        add(background);
    }

    /**
     * Prepara todos los listeners y acciones de los elementos.
     */
    private void prepareActions() {

        btnStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                abrirMenuGUI();
            }
        });
    }

    /**
     * Método para cambiar a la siguiente interfaz.
     */
    private void abrirMenuGUI() {
        new MenuGUI(); 
        dispose();
    }

    /**
     * MÉTODO MAIN — Punto de entrada del programa
     */
    public static void main(String[] args) {
        new TheDOPOHardestGameGUI();
    }
}
