package Dominio;

public class AcceleratedEnemy extends Enemy{

	public AcceleratedEnemy(Position position) {
		super(position, 2.0, 1.0, new HorizontalMovement());
	}

}
