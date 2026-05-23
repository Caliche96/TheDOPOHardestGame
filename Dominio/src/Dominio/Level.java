package Dominio;

import java.util.*;

/**
 * Clase que representa un nivel del juego.
 */
public class Level {
	//Atributos
    private GameBoard board;
    private List<Enemy> enemies;
    private List<Coin> coins;
    private List<SpecialElement> specialElements;
    private int timeLimit;
    private String levelName;
    private Position defaultSpawn;

    /**
     * Constructor de la clase Level.
     * @param levelName El nombre del nivel.
     * @param board El tablero del juego.
     * @param timeLimit El límite de tiempo para el nivel.
     */
    public Level(String levelName,GameBoard board, int timeLimit ) {
    	this.levelName=levelName;
    	this.board=board;
    	this.timeLimit=timeLimit;
    	this.enemies= new ArrayList<>();
    	this.coins= new ArrayList<>();
    	this.specialElements= new ArrayList<>();
    }

    //Enemigos
    /**
     * Agrega un enemigo al nivel.
     * @param enemy El enemigo a agregar.
     */
    public void addEnemy(Enemy enemy) {
    	enemies.add(enemy);
    }
    
    /**
     * Elimina un enemigo del nivel.
     * @param enemy El enemigo a eliminar.
     */
    public void removeEnemy(Enemy enemy) {
    	enemies.remove(enemy);
    }
    
    //Monedas
    /**
     * Agrega una moneda al nivel.
     * @param coin La moneda a agregar.
     */
    public void addCoin(Coin coin) {
    	coins.add(coin);
    }
    
    /**
     * Elimina una moneda del nivel.
     * @param coin La moneda a eliminar.
     */
    public void removeCoin(Coin coin) {
    	coins.remove(coin);
    }
    
    //Elementos Especiales
    /**
     * Agrega un elemento especial al nivel.
     * @param element El elemento especial a agregar.
     */
    public void addSpecialElement(SpecialElement element) {
    	specialElements.add(element);
    }
    
    /**
     * Elimina un elemento especial del nivel.
     * @param element El elemento especial a eliminar.
     */
    public void removeSpecialElement(SpecialElement element) {
    	specialElements.remove(element);
    }
    
    //Validaciones
    /**
     * Verifica si todas las monedas han sido recolectadas.
     * @return true si todas las monedas han sido recolectadas, false en caso contrario.
     */
    public boolean allCoinsCollected() {
    	for (Coin coin: coins) {
    		if (!coin.isCollected()) {
    			return false;
    		}
    	}
    	return true;
    }
    
    //Getters
    /**
     * Obtiene el tablero del nivel.
     * @return El tablero del nivel.
     */
    public GameBoard getBoard() { 
    	return board; 
    }

    /**
     * Obtiene la lista de enemigos del nivel.
     * @return La lista de enemigos del nivel.
     */
    public List<Enemy> getEnemies() { 
    	return enemies; 
    }

    /**
     * Obtiene la lista de monedas del nivel.
     * @return La lista de monedas del nivel.
     */
    public List<Coin> getCoins() { 
    	return coins; 
    }
    
    /**
     * Obtiene la lista de elementos especiales del nivel.
     * @return La lista de elementos especiales del nivel.
     */
    public List<SpecialElement> getSpecialElements(){
    	return specialElements;
    }

    /**
     * Obtiene el límite de tiempo para el nivel.
     * @return El límite de tiempo para el nivel.
     */
    public int getTimeLimit() {
    	return timeLimit;
    }
    
    /**
     * Obtiene el nombre del nivel.
     * @return El nombre del nivel.
     */
    public String getLevelName() {
    	return levelName;
    }

    /**
     * Obtiene la posición de aparición por defecto del nivel.
     * @return La posición de aparición por defecto del nivel.
     */
    public Position getDefaultSpawn() {
        return defaultSpawn;
    }

    /**
     * Establece la posición de aparición por defecto del nivel.
     * @param spawn La posición de aparición por defecto del nivel.
     */
    public void setDefaultSpawn(Position spawn) {
        this.defaultSpawn = spawn;
    }
}