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

    public void update() {
    	state.update(this);
    }
    
    public void movePlayer(int playerIndex, Direction direccion) {
    	state.movePlayer(this, playerIndex, direccion);
    }
    
    public void pause() {
    	state.pause(this);
    }

    public void resume() {
    	state.resume(this);
    }
    
    public void finishGame() {
    	state.finish(this);
    }
    
    //-----------METODOS INTERNOS-------

    public void internalMovePlayer(int playerIndex, Direction direccion) {
    	if (playerIndex <0 || playerIndex >= players.size()) {
    		return;
    	}
    	
    	Player player = players.get(playerIndex);
    	Position nextPosition= player.calculateNextPosition(direccion);
    	
    	if(!currentLevel.getBoard().isWall(nextPosition)) {
    		player.move(direccion);
    		checkSafeZone(player);
    	}
    }
    
    //Parte de Enemigos

    public void moveEnemies() {
    	for (Enemy enemy: currentLevel.getEnemies()) {
    		enemy.update();
    	}
    }
    
    
    //Colisiones
    public void checkEnemyCollsion() {
		for (Player player : players) {
			for(Enemy enemy : currentLevel.getEnemies()) {
				if (enemy.collides(player)) {
					player.receiveHit();
				}
			}
		}
		
	}
    
    
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

	//Verificar la utilidad de este método
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
	public void checkSafeZone(Player player) {
		Cell currentCell = currentLevel.getBoard().getCell(player.getPosition().getRow(), player.getPosition().getColumn());
		
		if(currentCell.getType()==CellType.SAFE_ZONE) {
			player.setSpawnPoint(player.getPosition());
		}
	}

	//Victoria
	public void checkGoal() {
		if(!currentLevel.allCoinsCollected()) {
			return;
		}
		
		boolean allPlayersFinished = true;
		
		for (Player player : players) {
			Cell currentCell = currentLevel.getBoard().getCell(player.getPosition().getRow(), player.getPosition().getColumn());
			
			if(currentCell.getType()!=CellType.GOAL) {
				allPlayersFinished= false;
			}
		}
		
		if(allPlayersFinished) {
			finishGame();
		}
			
	}
	
	//Tiempo

	public void updateTimer() {
		remainingTime --;
		if (remainingTime <=0) {
			finishGame();
			
		}
	}
		

	
	//Persistencia
	
	public void saveGame() {
		//Falta aún implementar toda esta parte
	}
	
	public void loadGame() {
		//Falta aún implementar toda esta parte
	}
	
	
	//Manejo de los jugadores
	
	public void addPlayer(Player player) {
		players.add(player);
	}
	
	
	//CAMBIOS DE ESTADO
	public void setState(GameState state) {
		this.state=state;
	}
	
	//GETTERS
	public Level getCurrentLevel() {
		return currentLevel;
	}
	
	public List<Player> getPlayers(){
		return players;
	}
	
	public GameState getGameState() {
		return state;
	}

	public int getRemainingTime() {
		return remainingTime;
	}
	
	public GameMode getGameMode() {
		return gameMode;
	}
	
}