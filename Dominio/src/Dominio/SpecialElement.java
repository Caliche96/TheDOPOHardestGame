package Dominio;

public abstract class SpecialElement {
	protected Position position;
	protected boolean active;

	public SpecialElement(Position position) {
		this.position = position;
		this.active = true;
	}

	// Efecto del elemento
	public abstract void applyEffect(Game game, Player player);

	// Colisiones
	public boolean collides(Player player) {
		return position.equals(player.getPosition());
	}

	// Estado
	public void deactivated() {
		active = false;
	}

	public boolean isActive() {
		return active;
	}

	// Getters
	public Position getPosition() {
		return position;
	}

	public void consume() {
		active = false;
	}

}
