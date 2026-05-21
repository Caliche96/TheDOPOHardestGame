package Dominio;

public abstract class SpecialElement {
	protected Position position;
	protected boolean active;
	
	public SpecialElement(Position position) {
		this.position=position;
		this.active=true;
	}
	
	//Efecto del elemento
	public abstract void applyEffect(Game game, Player player);
	
	//Colisiones
	public boolean collides(Player player) {
		float ex = position.getColumn() * GameConfig.CELL_SIZE;
		float ey = position.getRow()    * GameConfig.CELL_SIZE;
		float es = GameConfig.CELL_SIZE;
		return player.getX() < ex + es &&
		       player.getX() + player.getSize() > ex &&
		       player.getY() < ey + es &&
		       player.getY() + player.getSize() > ey;
	}
	
	//Estado
	public void deactivated() {
		active=false;
	}

	public void consume() {
		active=false;
	}
	
	public boolean isActive() {
		return active;
	}
	
	//Getters
	public Position getPosition() {
		return position;
	}
	
	
}