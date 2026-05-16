package Dominio;

public abstract class Enemy {
    protected Position position;
    protected double speed;
    protected double size;
    protected boolean active;
    protected MovementPattern movementPattern;

    public Enemy(Position initalposition, double speed, double size, MovementPattern movementPattern) {
        this.position = initalposition;
        this.speed= speed;
        this.size=size;
        this.movementPattern = movementPattern;
        this.active=true;
    }

    //Movimiento
    public void update() {
    	if(active) {
    		movementPattern.move(this);    
    	}
    }

    //Colisiones
    
    public boolean collides(Player player) {
    	return position.equals(player.getPosition());
    }
    
    //Estado
    
    public void destroy() {
    	active=false;
    }
    
    public boolean isActive() {
    	return active;
    }
    
    //Getters
    
    public Position getPosition() {
        return position;
    }

    public double getSpeed() {
    	return speed;
    }
    
    public double getSize() {
    	return size; 
    }
    
    public MovementPattern getMovementPattern() {
    	return movementPattern;
    }
    
    //Setters
    
    public void setPosition(Position position) {
    	this.position=position;
    }
    
    public void setSpeed(double speed) {
    	this.speed=speed;
    }
}