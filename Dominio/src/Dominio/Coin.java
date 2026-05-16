package Dominio;

public abstract class Coin {
    protected Position position;
    protected boolean collected;

    public Coin(Position position) {
        this.position = position;
        this.collected = false;
    }

    //Efecto de la moneda
    
    public abstract void applyEffect(Player player);
    
    //Colisiones
    
    public boolean collides(Player player) {
    	return position.equals(player.getPosition());
    }
    
    //Estado
    
    public void collect() {
        collected = true;
    }

    public boolean isCollected() {
        return collected;
    }

    //Getters

    public Position getPosition() { return position; }
}