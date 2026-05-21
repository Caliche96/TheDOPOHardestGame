package Dominio;

/**
 * Represnta las modalidades disponibles en el juego.
 */
public enum GameMode {
	SINGLE_PLAYER(1),
	PLAYER_VS_PLAYER(2),
	PLAYER_VS_MACHINE(2);
	
	private int players;
	GameMode(int players){
		this.players= players;
	}
	
	/**
	 * Obtiene el número de jugadores para el modo de juego.
	 * @return número de jugadores
	 */
	public int getPlayers() {
		return players;
	}
	
	/**
	 * Verifica si el modo de juego es multijugador.
	 * @return true si es multijugador, false en caso contrario
	 */
	public boolean isMultiplayer() {
		return this == PLAYER_VS_PLAYER || this== PLAYER_VS_MACHINE;
	}
	
	/**
	 * Verifica si el modo de juego incluye un jugador máquina.
	 * @return true si incluye un jugador máquina, false en caso contrario
	 */
	public boolean hasMachine() {
		return this== PLAYER_VS_MACHINE;
	}
	
	/**
	 * Verifica si el modo de juego es de un solo jugador.
	 * @return true si es de un solo jugador, false en caso contrario
	 */
	public boolean isSinglePlayer() {
		return this == SINGLE_PLAYER;
	}
}
