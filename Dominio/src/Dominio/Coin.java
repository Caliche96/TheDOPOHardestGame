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
        // Centro de la moneda en píxeles
        float coinX = position.getColumn() * GameConfig.CELL_SIZE + GameConfig.CELL_SIZE / 2f;
        float coinY = position.getRow()    * GameConfig.CELL_SIZE + GameConfig.CELL_SIZE / 2f;
        float r = GameConfig.CELL_SIZE / 4f;
        // Colisión círculo vs AABB del jugador
        float nearX = Math.max(player.getX(), Math.min(coinX, player.getX() + player.getSize()));
        float nearY = Math.max(player.getY(), Math.min(coinY, player.getY() + player.getSize()));
        float dx = coinX - nearX;
        float dy = coinY - nearY;
        return (dx * dx + dy * dy) < (r * r);
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