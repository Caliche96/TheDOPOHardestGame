package Dominio;

public class HorizontalMovement implements MovementPattern{
	private int direction;
	
	public HorizontalMovement() {
		direction=1;
	}

	@Override
	public void move(Enemy enemy) {
		Position current = enemy.getPosition();
		Position next = new Position(current.getRow(), current.getColumn()+direction);
		enemy.setPosition(next);
	}

}
