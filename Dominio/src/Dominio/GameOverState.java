package Dominio;

public class GameOverState implements GameState{

	@Override
	public void update(Game game) {
		//Juego terminado
	}

	@Override
	public void movePlayer(Game game, int playerIndex, Direction direccion) {
		//No se mueve
	}

	@Override
	public void pause(Game game) {
		//No aplica
	}

	@Override
	public void resume(Game game) {
		//No aplica
	}

	@Override
	public void finish(Game game) {
		//Ya terminó
	}

}
