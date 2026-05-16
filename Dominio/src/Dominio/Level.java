package Dominio;

import java.util.*;

public class Level {
	//Atributos
    private GameBoard board;
    private List<Enemy> enemies;
    private List<Coin> coins;
    private List<SpecialElement> specialElements;
    private int timeLimit;
    private String levelName;
    private Position defaultSpawn;

    public Level(String levelName,GameBoard board, int timeLimit ) {
    	this.levelName=levelName;
    	this.board=board;
    	this.timeLimit=timeLimit;
    	this.enemies= new ArrayList<>();
    	this.coins= new ArrayList<>();
    	this.specialElements= new ArrayList<>();
    }

    //Enemigos
    
    public void addEnemy(Enemy enemy) {
    	enemies.add(enemy);
    }
    
    public void removeEnemy(Enemy enemy) {
    	enemies.remove(enemy);
    }
    
    //Monedas
    public void addCoin(Coin coin) {
    	coins.add(coin);
    }
    
    public void removeCoin(Coin coin) {
    	coins.remove(coin);
    }
    
    //Elementos Especiales
    public void addSpecialElement(SpecialElement element) {
    	specialElements.add(element);
    }
    
    public void removeSpecialElement(SpecialElement element) {
    	specialElements.remove(element);
    }
    
    //Validaciones
    public boolean allCoinsCollected() {
    	for (Coin coin: coins) {
    		if (!coin.isCollected()) {
    			return false;
    		}
    	}
    	return true;
    }
    
    //Getters
    public GameBoard getBoard() { 
    	return board; 
    }

    public List<Enemy> getEnemies() { 
    	return enemies; 
    }

    public List<Coin> getCoins() { 
    	return coins; 
    }
    
    public List<SpecialElement> getSpecialElements(){
    	return specialElements;
    }

    public int getTimeLimit() {
    	return timeLimit;
    }
    
    public String getLevelName() {
    	return levelName;
    }

    public Position getDefaultSpawn() {
        return defaultSpawn;
    }

    public void setDefaultSpawn(Position spawn) {
        this.defaultSpawn = spawn;
    }
    
    
    
}