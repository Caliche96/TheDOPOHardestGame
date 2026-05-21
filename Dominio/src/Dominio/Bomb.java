package Dominio;

public class Bomb extends SpecialElement {

	/**
	 *Constructor de la clase Bomb
	 * 
	 *@param position Posición inicial de la bomba
	 */
	public Bomb(Position position) {
		super(position);
	}

	/**
	 *Método que aplica el efecto de la bomba
	 * 
	 *@param game   Juego
	 *@param player Jugador
	 */
	@Override
	public void applyEffect(Game game, Player player) {
		player.die();
		deactivated();
	}

}
