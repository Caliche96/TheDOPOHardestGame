package Dominio;

public class BasicBluePoint extends Enemy {

	public BasicBluePoint(Position position) {
		super(position, 1.0, 1.0, new HorizontalMovement());
	}

}
