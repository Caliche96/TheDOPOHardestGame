package Dominio;

import java.util.*;

public class Game {
	
	//Atributos de la clase controladora del dominio
	private Level currentLevel;
	private List<Player> players;
	
	
	private GameMode gameMode;
	private GameState state;
	private int remainingTime;
	//Métodos de la clase Controladora
	
	/**
	 * Constructor
	 */
    public Game(Level level, GameMode mode) {
        this.currentLevel=level;
        this.gameMode=mode;
        this.players= new ArrayList<>();
        this.state = new RunningState();
        this.remainingTime= level.getTimeLimit();
    }
    
    //Metodos Principales

	/**
	 * Actualiza el estado del juego.
	 */
    public void update() {
    	state.update(this);
    }
    
	/**
	 * Mueve al jugador especificado en la dirección dada.
	 * @param playerIndex índice del jugador a mover (0 para Player 1, 1 para Player 2 o máquina)
	 * @param direccion dirección del movimiento
	 */
    public void movePlayer(int playerIndex, Direction direccion) {
    	state.movePlayer(this, playerIndex, direccion);
    }
    
	/**
	 * Pausa el juego.
	 */
    public void pause() {
    	state.pause(this);
    }

	/**
	 * Reanuda el juego.
	 */
    public void resume() {
    	state.resume(this);
    }

	/**
	 * Finaliza el juego.
	 */
    public void finishGame() {
    	state.finish(this);
    }
    
	/**
	 * Mueve al jugador especificado en la dirección dada (método interno).
	 * @param playerIndex índice del jugador a mover
	 * @param direccion dirección del movimiento
	 */
    public void internalMovePlayer(int playerIndex, Direction direccion) {
    	if (playerIndex < 0 || playerIndex >= players.size()) return;
    	Player player = players.get(playerIndex);
    	player.move(direccion, currentLevel.getBoard(), GameConfig.CELL_SIZE);
    	checkSafeZone(player);
    }
    
    //Parte de Enemigos

	/**
	 * Mueve todos los enemigos del nivel.
	 */
    public void moveEnemies() {
    	for (Enemy enemy: currentLevel.getEnemies()) {
    		enemy.update();
    	}
    }
    
    
    //Colisiones
	/**
	 * Verifica las colisiones entre enemigos y jugadores.
	 */
    public void checkEnemyCollsion() {
		for (Player player : players) {
			for(Enemy enemy : currentLevel.getEnemies()) {
				if (enemy.collides(player)) {
					player.receiveHit();
				}
			}
		}
		
	}
    
    /**
	 * Verifica las colisiones entre monedas y jugadores.
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
	 * Verifica las colisiones entre elementos especiales y jugadores.
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
	
	//Colision entre jugadores
	/**
	 * Verifica las colisiones entre jugadores.
	 */
	public void checkPlayerCollisions() {
		if (gameMode== GameMode.SINGLE_PLAYER) {
			return;
		}
		
		for (int i=0; i<players.size(); i++) {
			for (int j =i+1; j<players.size(); j++) {
				Player p1= players.get(i);
				Player p2= players.get(j);
				
				if(p1.collides(p2)) {
					p1.die();
					p2.die();
				}
			}
		}
	}
	
	//Zonas Seguras
	/**
	 * Verifica si el jugador está en una zona segura.
	 * @param player jugador a verificar
	 */
	public void checkSafeZone(Player player) {
		if (player.isInSafeZone(currentLevel.getBoard(), GameConfig.CELL_SIZE)) {
			player.setSpawnPoint(player.getX(), player.getY());
		}
	}

	//Victoria
	/**
	 * Verifica si todos los jugadores han alcanzado el objetivo.
	 */
	public void checkGoal() {
		if (!currentLevel.allCoinsCollected()) {
			return;
		}

		boolean allPlayersFinished = true;

		for (int i = 0; i < players.size(); i++) {
			Player player = players.get(i);
			GameBoard board = currentLevel.getBoard();
			int cell = GameConfig.CELL_SIZE;

			boolean finished;
			if (i == 0) {
				// Player 1: debe llegar a la zona GOAL
				finished = player.isInGoal(board, cell);
			} else {
				// Player 2 / Máquina: debe llegar a la zona SPAWN del Player 1
				finished = player.isInSpawnZone(board, cell);
			}

			if (!finished) {
				allPlayersFinished = false;
			}
		}

		if (allPlayersFinished) {
			finishGame();
		}
	}
	
	//Tiempo
	/**
	 * Actualiza el temporizador del juego.
	 */
	public void updateTimer() {
		remainingTime --;
		if (remainingTime <=0) {
			finishGame();
			
		}
	}
		

	
	//Persistencia

	/**
	 * Serializa el estado actual del juego en un archivo .dat.
	 * @param path      ruta destino  (ej. "saves/partida.dat")
	 * @param levelFile ruta del .txt del nivel actual (para poder recargarlo al restaurar)
	 * @throws GameException si no se puede escribir el archivo
	 */
	public void saveGame(String path, String levelFile) throws GameException {
		GameSave save = new GameSave(this, levelFile);
		try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(
				new java.io.FileOutputStream(path))) {
			oos.writeObject(save);
		} catch (java.io.IOException e) {
			throw new GameException("No se pudo guardar la partida: " + e.getMessage());
		}
	}

	/**
	 * Deserializa un archivo .dat y restaura el estado del juego.
	 * Recarga el nivel desde su .txt, aplica las monedas recogidas,
	 * elementos consumidos y reposiciona a los jugadores.
	 * @param path ruta del archivo .dat
	 * @return GameSave con los datos restaurados (la presentación los usa para reconstruir el GamePanel)
	 * @throws GameException si el archivo no existe o está corrupto
	 */
	public static GameSave loadGame(String path) throws GameException {
		try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
				new java.io.FileInputStream(path))) {
			return (GameSave) ois.readObject();
		} catch (java.io.IOException | ClassNotFoundException e) {
			throw new GameException("No se pudo cargar la partida: " + e.getMessage());
		}
	}
	
	
	//Manejo de los jugadores
	/**
	 * Agrega un jugador a la lista de jugadores.
	 * @param player jugador a agregar
	 */
	public void addPlayer(Player player) {
		players.add(player);
	}

	/**
	 * Actualiza el movimiento del jugador máquina si existe.
	 * Debe llamarse cada tick desde GamePanel cuando el modo es PvM.
	 */
	public void updateMachine() {
		for (Player p : players) {
			if (p instanceof MachinePlayer) {
				((MachinePlayer) p).update(this);
			}
		}
	}
	
	
	//CAMBIOS DE ESTADO
	/**
	 * Establece el estado actual del juego.
	 * @param state nuevo estado del juego
	 */
	public void setState(GameState state) {
		this.state=state;
	}
	
	//GETTERS
	/**
	 * Obtiene el nivel actual del juego.
	 * @return nivel actual
	 */
	public Level getCurrentLevel() {
		return currentLevel;
	}
	
	/**
	 * Obtiene la lista de jugadores.
	 * @return lista de jugadores
	 */
	public List<Player> getPlayers(){
		return players;
	}
	
	/**
	 * Obtiene el estado actual del juego.
	 * @return estado actual
	 */
	public GameState getGameState() {
		return state;
	}

	/**
	 * Obtiene el tiempo restante del juego.
	 * @return tiempo restante
	 */
	public int getRemainingTime() {
		return remainingTime;
	}
	
	/**
	 * Obtiene el modo de juego actual.
	 * @return modo de juego actual
	 */
	public GameMode getGameMode() {
		return gameMode;
	}

	/**
	 * Establece el tiempo restante del juego.
	 * @param time nuevo tiempo restante
	 */
	public void setRemainingTime(int time) {
		this.remainingTime = time;
	}
	
}