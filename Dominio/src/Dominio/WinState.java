package Dominio;

public class WinState implements GameState {

	@Override public void update(Game game){ }
	@Override public void movePlayer(Game game, int playerIndex, Direction d) { }
	@Override public void pause(Game game){ }
	@Override public void resume(Game game){ }
	@Override public void finish(Game game) { game.setState(new GameOverState()); }
	
	/**
	 * Verifica si el jugador ha ganado.
	 * @return true si ha ganado, false en caso contrario.
	 */
	@Override public boolean isWin(){ 
		return true; 
	}
}