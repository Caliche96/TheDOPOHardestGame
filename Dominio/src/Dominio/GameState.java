package Dominio;


public interface GameState {
	void update(Game game);
	void movePlayer(Game game, int playerIndex, Direction direccion);
	void pause(Game game);
	void resume(Game game);
	void finish(Game game);
	
	/**
	 * Indica si el juego está en ejecución.
	 * @return true si el juego está en ejecución, false en caso contrario.
	 */
	default boolean isRunning(){ 
		return false;
	}

	/**
	 * Indica si el juego está pausado.
	 * @return true si el juego está pausado, false en caso contrario.
	 */
	default boolean isPaused(){
		return false;
	}
	/**
	 * Indica si el juego ha terminado.
	 * @return true si el juego ha terminado, false en caso contrario.
	 */
	default boolean isGameOver(){
		return false;
	}

	/**
	 * Indica si el juego ha sido ganado.
	 * @return true si el juego ha sido ganado, false en caso contrario.
	 */
	default boolean isWin(){
		return false;
	}
}
