package Dominio;

public class AcceleratedEnemy extends Enemy {

	/**
	 * Constructor de la clase AcceleratedEnemy
	 * 
	 * @param position Posición inicial del enemigo
	 */
	public AcceleratedEnemy(Position position) {
		super(position, 2.0, 1.0, new HorizontalMovement());
	}

}
