package Dominio;

public class Player {
    private Position position;
    private Position initialPosition;
    private int speed;

    public Player(Position initialPosition, int speed) {
        this.initialPosition = initialPosition.copy();
        this.position = initialPosition.copy();
        this.speed = speed;
    }

    public void move(Direction direction) {
        switch (direction) {
            case UP:    position.setY(position.getY() - speed); break;
            case DOWN:  position.setY(position.getY() + speed); break;
            case LEFT:  position.setX(position.getX() - speed); break;
            case RIGHT: position.setX(position.getX() + speed); break;
        }
    }

    public void reset() {
        this.position = initialPosition.copy();
    }

    public Position getPosition() { return position; }
    public int getSpeed() { return speed; }
}