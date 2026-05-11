package Presentation;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;

public class SelectPlayerGUI extends JFrame {
	private JButton btnRedPlayer;
	private JButton btnGreenPlayer;
	private JButton btnYellowPlayer;
	
	public SelectPlayerGUI() {
		setTitle("Escoja su Skin");
		setSize(600,500);
		setLocationRelativeTo(null);
		setLayout(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		prepareElements();
		prepareActions();
		
		setVisible(true);
		
	}
	
	private void prepareElements() {
		//Botón para Jugador de Rojo
		btnRedPlayer= new JButton();
		btnRedPlayer.setBounds(120,150,100,150);
		btnRedPlayer.setOpaque(false);
		btnRedPlayer.setContentAreaFilled(false);
		btnRedPlayer.setBorderPainted(false);
		
		//Botón para jugador de Verde
		btnGreenPlayer=new JButton();
		btnGreenPlayer.setBounds(250,150,100,150);
		btnGreenPlayer.setOpaque(false);
		btnGreenPlayer.setContentAreaFilled(false);
		btnGreenPlayer.setBorderPainted(false);
		
		//Botón para jugador de Amarillo
		btnYellowPlayer=new JButton();
		btnYellowPlayer.setBounds(350,150,100,150);
		btnYellowPlayer.setOpaque(false);
		btnYellowPlayer.setContentAreaFilled(false);
		btnYellowPlayer.setBorderPainted(false);
	}
	
	private void prepareActions() {
		btnRedPlayer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new LevelSelectGUI();
                dispose();
            }
        });

        btnGreenPlayer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new LevelSelectGUI();
                dispose();
            }
        });
        
        btnYellowPlayer.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		new LevelSelectGUI();
        		dispose();
        	}
        });
	}
}
