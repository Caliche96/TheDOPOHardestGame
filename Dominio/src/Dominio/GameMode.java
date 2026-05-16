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
	
	public int getPlayers() {
		return players;
	}
	
	public boolean isMultiplayer() {
		return this == PLAYER_VS_PLAYER || this== PLAYER_VS_MACHINE;
	}
	
	public boolean hasMachine() {
		return this== PLAYER_VS_MACHINE;
	}
	
	public boolean isSinglePlayer() {
		return this == SINGLE_PLAYER;
	}
}
