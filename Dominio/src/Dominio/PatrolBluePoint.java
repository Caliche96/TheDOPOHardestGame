package Dominio;

public class PatrolBluePoint extends Enemy{

	public PatrolBluePoint(float x, float y) {
		super(x, y, 7.0f, GameConfig.CELL_SIZE - 4f, new PatrolMovement());

	}

}
