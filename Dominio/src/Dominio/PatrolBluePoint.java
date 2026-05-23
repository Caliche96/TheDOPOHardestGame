package Dominio;

/**
 * Clase que representa al punto de patrulla azul en el juego.
 */
public class PatrolBluePoint extends Enemy{

	/** Constructor de la clase PatrolBluePoint. */
	public PatrolBluePoint(float x, float y) {
		super(x, y, 3.5f, GameConfig.CELL_SIZE - 4f, new PatrolMovement());

	}

}
