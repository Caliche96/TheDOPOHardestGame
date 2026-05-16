package Dominio;

import java.awt.Color;

public abstract class Player {
	//Atributos
    protected String name;
    protected Position position;
    protected Position spawnPoint;
    protected double speed;
    protected double size;
    protected int deaths;
    protected int collectedCoins;
    protected int lives;
    protected boolean alive;
    protected Color borderColor;
    protected Color bodyColor;
	
	/**
	 * Constructor
	 * @param initialPosition
	 * @param speed
	 */
	
    public Player(String name, Position initialPosition) {
        this.name=name;
        this.position=initialPosition;
        this.spawnPoint=initialPosition;
        this.deaths=0;
        this.alive=true;
        this.collectedCoins=0;
        this.lives=0;
        
        this.speed=1.0;
        this.size=1.0;
    }
    
    //Movimiento

    public void move(Direction direction) {
    	
    	position= calculateNextPosition(direction);
    }
    	
    public Position calculateNextPosition(Direction direction) {
    	
    	int row = position.getRow();
    	int column= position.getColumn();
    	
        switch (direction) {
            case UP:    row--; break;
            case DOWN:  row++; break;
            case LEFT:  column--; break;
            case RIGHT: column++; break;
            case UP_LEFT: row--; column--; break;
            case UP_RIGHT: row--; column++; break;
            case DOWN_LEFT: row++; column--; break;
            case DOWN_RIGHT: row++; column++; break;
        }
        return new Position(row,column);
    }
    
    //Vidas y muertes

    public void die() {
    	deaths++;
    	respawn();

    }
    
    public void respawn() {
    	position= spawnPoint;
    	alive=true;
    }
    
    public void addLife() {
    	lives++;
    }
    
    //Monedas
    
    public void addCoin() {
    	collectedCoins++;
    }
    
    public void resetCoins() {
    	collectedCoins=0;
    }
    
    //Spawn
    
    public void setSpawnPoint(Position newSpawn) {
    	spawnPoint=newSpawn;
    }
    
    //Colisiones
    
    public boolean collides(Player other) {
    	return position.equals(other.getPosition());
    }
    
    //Habilidad Especial
    public abstract void receiveHit();
    
    //Getters
    public String getName() {
    	return name;
    }
    
    public Position getPosition() { 
    	return position; 
    	}
    
    public double getSpeed() {
    	return speed;
    }
   
   public double getSize() {
	   return size;
   }

   public int getDeaths() {
	   return deaths;
   }

   public int getCollectedCoins() {
	   return collectedCoins;
   }

   public int getLives() {
	   return lives;
   }

   public boolean isAlive() {
	   return alive;
   }

   public Color getBorderColor() {
	   return borderColor;
   }

   public Color getBodyColor() {
	   return bodyColor;
   }

   //Setters
   public void setBorderColor(Color borderColor) {
	   this.borderColor=borderColor;
   }

   public void setBodyColor(Color bodyColor) {
	   this.bodyColor=bodyColor;
   }

}
