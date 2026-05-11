package Dominio;

public class Coin {
    private Position position;
    private boolean collected;

    public Coin(Position position) {
        this.position = position;
        this.collected = false;
    }

    public void collect() {
        this.collected = true;
    }

    public boolean isCollected() {
        return collected;
    }

    public boolean collidesWith(Player player) {
        return !collected && position.equals(player.getPosition());
    }

    public Position getPosition() { return position; }
}