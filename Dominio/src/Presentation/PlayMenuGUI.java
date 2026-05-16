package Presentation;

import java.awt.event.*;
import java.io.File;
import javax.swing.*;

public class PlayMenuGUI extends JFrame{
	
	private JButton btnPlayer;
	private JButton btnPLayerMachine;
	private JButton btnTwoPlayers;
	private JButton btnCredits;
	
	public PlayMenuGUI() {
		setTitle("Play Menu");
		setSize(600,500);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		prepareElements();
		prepareActions();
		setVisible(true);
	}
	
	private void prepareElements() {
		btnPlayer = new JButton();
		btnPlayer.setBounds(110,200,150,150);
		btnPlayer.setOpaque(false);
		btnPlayer.setContentAreaFilled(false);
		btnPlayer.setBorderPainted(false);
	}
	
	private void prepareActions() {
		btnPlayer.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				new SelectPlayerGUI();
				dispose();
			}
		});
		
		btnPLayerMachine.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null,"Player vs Machine: En construcción");
			}
		});
		
		btnTwoPlayers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "Player vs Player:En Construccion");
			}
		});
		
		btnCredits.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null,"Proyecto Realizado por:\n Carlos Duban Rojas Riveros" );
			}
		});
	}
}
