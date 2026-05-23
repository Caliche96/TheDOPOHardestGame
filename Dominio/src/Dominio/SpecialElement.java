package Dominio;

public abstract class SpecialElement {
	protected Position position;
	protected boolean active;
	
	/**
	 * Crea un elemento especial.
	 * @param position La posición del elemento.
	 */
	public SpecialElement(Position position) {
		this.position=position;
		this.active=true;
	}
	
	/**
	 * Aplica el efecto del elemento especial al jugador.
	 * @param game El juego.
	 * @param player El jugador.
	 */
	public abstract void applyEffect(Game game, Player player);
	
	/**
	 * Verifica si el elemento especial colisiona con el jugador.
	 * @param player El jugador.
	 * @return true si colisiona, false en caso contrario.
	 */
	public boolean collides(Player player) {
		float ex = position.getColumn() * GameConfig.CELL_SIZE;
		float ey = position.getRow()    * GameConfig.CELL_SIZE;
		float es = GameConfig.CELL_SIZE;
		return player.getX() < ex + es &&
		       player.getX() + player.getSize() > ex &&
		       player.getY() < ey + es &&
		       player.getY() + player.getSize() > ey;
	}
	
	/**
	 * Desactiva el elemento especial.
	 */
	public void deactivated() {
		active=false;
	}

	/**
	 * Consume el elemento especial.
	 */
	public void consume() {
		active=false;
	}
	
	/**
	 * Verifica si el elemento especial está activo.
	 * @return true si está activo, false en caso contrario.
	 */
	public boolean isActive() {
		return active;
	}
	
	/**
	 * Obtiene la posición del elemento especial.
	 * @return La posición del elemento.
	 */
	public Position getPosition() {
		return position;
	}

	/**
	 * Verifica si el elemento especial es una bomba.
	 * @return true si es una bomba, false en caso contrario.
	 */
	public boolean isBomb() {
		return false;
	}

	/**
	 * Verifica si el elemento especial es una fuente de vida.
	 * @return true si es una fuente de vida, false en caso contrario.
	 */
	public boolean isLifeSource() {
		return false;
	}
	
	
}