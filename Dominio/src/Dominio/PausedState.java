package Dominio;

public class PausedState implements GameState {

	@Override public void update(Game game){ }
	@Override public void movePlayer(Game game, int playerIndex, Direction d){ }
	@Override public void pause(Game game){ }
	
	/** Resumen el juego. */
	@Override public void resume(Game game) { 
		game.setState(new RunningState()); 
	}

	/** Finaliza el juego. */
	@Override public void finish(Game game) { 
		game.setState(new GameOverState()); 
	}

	/** Devuelve true si el juego está pausado. */
	@Override public boolean isPaused(){ 
		return true; 
	}
}