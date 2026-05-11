package Dominio;

public class Enemy {
    private Position position;
    private MovementPattern movementPattern;

    public Enemy(Position position, MovementPattern movementPattern) {
        this.position = position;
        this.movementPattern = movementPattern;
    }

    public void update() {
        movementPattern.move(this);
    }

    public Position getPosition() {
        return position;
    }

    public boolean collidesWith(Player player) {
        return position.equals(player.getPosition());
    }

    public void setMovementPattern(MovementPattern movementPattern) {
        this.movementPattern = movementPattern;
    }
}