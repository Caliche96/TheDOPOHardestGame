package Dominio;

public class BasicBluePoint extends Enemy {

	/**
	 * Constructor de la clase BasicBluePoint
	 * 
	 * @param position Posición inicial del enemigo
	 */
	public BasicBluePoint(Position position) {
		super(position, 1.0, 1.0, new HorizontalMovement());
	}

}
