package Dominio;

public class YellowCoin extends Coin {

	/** Crea una moneda amarilla. */
	public YellowCoin(Position position) {
		super(position);
	}

	/**
	 * Aplica el efecto de la moneda amarilla al jugador.
	 * @param player El jugador que recoge la moneda.
	 */
	@Override
	public void applyEffect(Player player) {
	}

}
