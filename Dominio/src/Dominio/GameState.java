package Dominio;

public interface GameState {
	void update(Game game);
	void movePlayer(Game game, int playerIndex, Direction direccion);
	void pause(Game game);
	void resume(Game game);
	void finish(Game game);
}
