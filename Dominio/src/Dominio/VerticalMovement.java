package Dominio;

public class VerticalMovement implements MovementPattern{
	
	private int direction;
	
	public VerticalMovement() {
		direction=-1;
	}
	@Override
	public void move(Enemy enemy) {
		Position current= enemy.getPosition();
		Position next=new Position(current.getRow()+direction, current.getColumn());
		enemy.setPosition(next);
		
	}

}
