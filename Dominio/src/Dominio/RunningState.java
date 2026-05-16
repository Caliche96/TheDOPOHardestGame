package Dominio;

public class RunningState implements GameState{
	@Override
	public void update(Game game) {
		game.moveEnemies();
		game.checkCoinCollision();
		game.checkEnemyCollsion();
		game.checkSpecialElements();
		game.checkPlayerCollisions();
		game.checkGoal();
		game.updateTimer();
		
	}

	@Override
	public void movePlayer(Game game, int playerIndex, Direction direccion) {
		game.internalMovePlayer(playerIndex, direccion);
		
	}

	@Override
	public void pause(Game game) {
		game.setState(new PausedState());
	}

	@Override
	public void resume(Game game) {
		// Ya esta corriendo
	}

	@Override
	public void finish(Game game) {
		game.setState(new GameOverState());
		
	}
}
