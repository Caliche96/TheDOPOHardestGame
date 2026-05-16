package Dominio;

public class WinState implements GameState {

	@Override
	public void update(Game game) {
		// Nivel Completado
		
	}

	@Override
	public void movePlayer(Game game, int playerIndex, Direction direccion) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause(Game game) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume(Game game) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finish(Game game) {
		game.setState(new GameOverState());
		
	}
	
	
	
}
