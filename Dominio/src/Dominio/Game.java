package Dominio;

import java.util.*;

public class Game {

	// Atributos de la clase controladora del dominio
	private Level currentLevel;
	private List<Player> players;

	private GameMode gameMode;
	private GameState state;
	private int remainingTime;
	// Métodos de la clase Controladora

	/**
	 * Constructor de la clase Game
	 * 
	 * @param level Nivel del juego
	 * @param mode  Modo de juego
	 */
	public Game(Level level, GameMode mode) {
		this.currentLevel = level;
		this.gameMode = mode;
		this.players = new ArrayList<>();
		this.state = new RunningState();
		this.remainingTime = level.getTimeLimit();
	}

	// Metodos Principales

	/**
	 * Actualiza el estado del juego
	 */
	public void update() {
		state.update(this);
	}

	/**
	 * Mueve al jugador
	 * 
	 * @param playerIndex Índice del jugador
	 * @param direccion   Dirección del movimiento
	 */
	public void movePlayer(int playerIndex, Direction direccion) {
		state.movePlayer(this, playerIndex, direccion);
	}

	/**
	 * Pausa el juego
	 */
	public void pause() {
		state.pause(this);
	}

	/**
	 * Reanuda el juego
	 */
	public void resume() {
		state.resume(this);
	}

	/**
	 * Finaliza el juego
	 */
	public void finishGame() {
		state.finish(this);
	}

	// -----------METODOS INTERNOS-------

	/**
	 * Mueve al jugador internamente
	 * 
	 * @param playerIndex Índice del jugador
	 * @param direccion   Dirección del movimiento
	 */
	public void internalMovePlayer(int playerIndex, Direction direccion) {
		if (playerIndex < 0 || playerIndex >= players.size()) {
			return;
		}

		Player player = players.get(playerIndex);
		Position nextPosition = player.calculateNextPosition(direccion);

		if (!currentLevel.getBoard().isWall(nextPosition)) {
			player.move(direccion);
			checkSafeZone(player);
		}
	}

	// Parte de Enemigos
	/**
	 * Mueve a los enemigos
	 */
	public void moveEnemies() {
		for (Enemy enemy : currentLevel.getEnemies()) {
			enemy.update();
		}
	}

	// Colisiones
	/**
	 * Verifica las colisiones con los enemigos
	 */
	public void checkEnemyCollsion() {
		for (Player player : players) {
			for (Enemy enemy : currentLevel.getEnemies()) {
				if (enemy.collides(player)) {
					player.receiveHit();
				}
			}
		}

	}

	/**
	 * Verifica las colisiones con las monedas
	 */
	public void checkCoinCollision() {
		for (Player player : players) {
			for (Coin coin : currentLevel.getCoins()) {
				if (!coin.isCollected() && coin.collides(player)) {
					coin.collect();
					coin.applyEffect(player);
					player.addCoin();
				}
			}
		}
	}

	/**
	 * Verifica las colisiones con los elementos especiales
	 */
	public void checkSpecialElements() {
		for (SpecialElement element : currentLevel.getSpecialElements()) {
			for (Player player : players) {
				if (element.collides(player)) {
					element.applyEffect(this, player);
				}
			}
		}
	}

	// Colision entre jugadores
	/**
	 * Verifica las colisiones entre jugadores
	 */
	public void checkPlayerCollisions() {
		if (gameMode == GameMode.SINGLE_PLAYER) {
			return;
		}

		for (int i = 0; i < players.size(); i++) {
			for (int j = i + 1; j < players.size(); j++) {
				Player p1 = players.get(i);
				Player p2 = players.get(j);

				if (p1.collides(p2)) {
					p1.die();
					p2.die();
				}
			}
		}
	}

	/**
	 * Verifica las colisiones con las zonas seguras
	 * 
	 * @param player Jugador
	 */
	public void checkSafeZone(Player player) {
		Cell currentCell = currentLevel.getBoard().getCell(player.getPosition().getRow(),
				player.getPosition().getColumn());

		if (currentCell.getType() == CellType.SAFE_ZONE) {
			player.setSpawnPoint(player.getPosition());
		}
	}

	/**
	 * Verifica si todos los jugadores han llegado a la meta
	 */
	public void checkGoal() {
		if (!currentLevel.allCoinsCollected()) {
			return;
		}

		boolean allPlayersFinished = true;

		for (Player player : players) {
			Cell currentCell = currentLevel.getBoard().getCell(player.getPosition().getRow(),
					player.getPosition().getColumn());

			if (currentCell.getType() != CellType.GOAL) {
				allPlayersFinished = false;
			}
		}

		if (allPlayersFinished) {
			finishGame();
		}

	}

	/**
	 * Actualiza el tiempo restante
	 */
	public void updateTimer() {
		remainingTime--;
		if (remainingTime <= 0) {
			finishGame();

		}
	}

	/**
	 * Guarda el estado actual del juego
	 * 
	 * @param path      Ruta donde se guardará el juego
	 * @param levelFile Nombre del archivo del nivel actual
	 * @throws GameException Si ocurre un error al guardar el juego
	 */
	public void saveGame(String path, String levelFile) throws GameException {
		GameSave save = new GameSave(this, levelFile);
		try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(
				new java.io.FileOutputStream(path))) {
			oos.writeObject(save);
		} catch (Exception e) {
			throw new GameException("Error al guardar el juego" + e.getMessage());
		}
	}

	/**
	 * Carga el juego guardado
	 * 
	 * @param path Ruta donde se encuentra el juego
	 * @throws GameException Si ocurre un error al cargar el juego
	 */
	public static GameSave loadGame(String path) throws GameException {
		try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.FileInputStream(path))) {
			return (GameSave) ois.readObject();
		} catch (ClassNotFoundException | java.io.IOException e) {
			throw new GameException("Error al cargar el juego" + e.getMessage());
		}
	}

	/**
	 * Añade un jugador al juego
	 * 
	 * @param player Jugador a añadir
	 */
	public void addPlayer(Player player) {
		players.add(player);
	}

	/**
	 * Cambia el estado del juego
	 * 
	 * @param state Estado del juego
	 */
	public void setState(GameState state) {
		this.state = state;
	}

	/**
	 * Obtiene el nivel actual
	 * 
	 * @return Nivel actual
	 */
	public Level getCurrentLevel() {
		return currentLevel;
	}

	/**
	 * Obtiene la lista de jugadores
	 * 
	 * @return Lista de jugadores
	 */
	public List<Player> getPlayers() {
		return players;
	}

	/**
	 * Obtiene el estado del juego
	 * 
	 * @return Estado del juego
	 */
	public GameState getGameState() {
		return state;
	}

	/**
	 * Obtiene el tiempo restante
	 * 
	 * @return Tiempo restante
	 */
	public int getRemainingTime() {
		return remainingTime;
	}

	/**
	 * Obtiene el modo de juego
	 * 
	 * @return Modo de juego
	 */
	public GameMode getGameMode() {
		return gameMode;
	}

	public void setRemainingTime(int time) {
		this.remainingTime = time;
	}

}