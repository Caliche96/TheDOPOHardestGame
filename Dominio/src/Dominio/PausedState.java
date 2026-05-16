package Dominio;

public class PausedState implements GameState {

	@Override
	public void update(Game game) {
		// No hará nada
		
	}

	@Override
	public void movePlayer(Game game, int playerIndex, Direction direccion) {
		//No hará nada
	}

	@Override
	public void pause(Game game) {
		// Ya esta pausado
		
	}

	@Override
	public void resume(Game game) {
		game.setState(new RunningState());
	}

	@Override
	public void finish(Game game) {
		game.setState(new GameOverState());
		
	}
	

}
