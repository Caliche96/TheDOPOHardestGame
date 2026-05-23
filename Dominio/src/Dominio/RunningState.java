package Dominio;

public class RunningState implements GameState {
	@Override public void update(Game game) {
		game.moveEnemies();
		game.checkCoinCollision();
		game.checkEnemyCollsion();
		game.checkSpecialElements();
		game.checkPlayerCollisions();
		game.checkGoal();
		game.updateTimer();
	}

	/**
	 * Mueve al jugador en la dirección especificada.
	 * @param game El juego.
	 * @param playerIndex El índice del jugador.
	 * @param direccion La dirección.
	 */
	@Override public void movePlayer(Game game, int playerIndex, Direction direccion) {
		game.internalMovePlayer(playerIndex, direccion);
	}

	/**
	 * Pausa el juego.
	 * @param game El juego.
	 */
	@Override public void pause(Game game){ 
		game.setState(new PausedState()); 
	}

	/**
	 * Reanuda el juego.
	 * @param game El juego.
	 */
	@Override public void resume(Game game) { }
	
	/**
	 * Finaliza el juego.
	 * @param game El juego.
	 */
	@Override public void finish(Game game) {
		game.setState(new GameOverState()); 
	}

	/**
	 * Verifica si el juego está en ejecución.
	 * @return true si el juego está en ejecución, false en caso contrario.
	 */
	@Override public boolean isRunning(){ 
		return true; 

	}
}