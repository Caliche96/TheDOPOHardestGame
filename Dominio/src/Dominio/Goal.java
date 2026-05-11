package Dominio;

public class Goal {
    private Position position;

    public Goal(Position position) {
        this.position = position;
    }

    public Position getPosition() { return position; }

    public boolean isReachedBy(Player player) {
        return position.equals(player.getPosition());
    }
}