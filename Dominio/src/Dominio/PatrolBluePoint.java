package Dominio;

public class PatrolBluePoint extends Enemy{

	public PatrolBluePoint(Position position) {
		super(position, 1.0, 1.0, new PatrolMovement());

	}

}
