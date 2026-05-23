package Dominio;

/**
 * Representa el estado del juego cuando este ha terminado.
 */
public class GameOverState implements GameState {

	@Override public void update(Game game){ }
	@Override public void movePlayer(Game game, int playerIndex, Direction d) { }
	@Override public void pause(Game game){ }
	@Override public void resume(Game game){ }
	@Override public void finish(Game game){ }

	/**
	 * Retorna si el juego ha terminado.
	 * @return true si el juego ha terminado, false en caso contrario.
	 */
	@Override public boolean isGameOver(){ 
		return true; 
	}
}