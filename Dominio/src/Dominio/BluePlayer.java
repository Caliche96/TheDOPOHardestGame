package Dominio;

public class BluePlayer extends Player {

	/**
	 * Constructor de la clase BluePlayer
	 * 
	 * @param name            Nombre del jugador
	 * @param initialPosition Posición inicial del jugador
	 */
	public BluePlayer(String name, Position initialPosition) {
		super(name, initialPosition);
		speed = 1.5;
		size = 1.5;
	}

	/**
	 * Método que se ejecuta cuando el jugador recibe un impacto
	 */
	@Override
	public void receiveHit() {
		die();

	}

}
