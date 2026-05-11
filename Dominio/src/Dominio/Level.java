package Dominio;

import java.util.List;

public class Level {
    private Board board;
    private Position startPosition;
    private Goal goal;
    private List<Enemy> enemies;
    private List<Coin> coins;

    public Level(Board board, Position startPosition, Goal goal,
                 List<Enemy> enemies, List<Coin> coins) {
        this.board = board;
        this.startPosition = startPosition;
        this.goal = goal;
        this.enemies = enemies;
        this.coins = coins;
    }

    public Board getBoard() { return board; }

    public Position getStartPosition() { return startPosition; }

    public Goal getGoal() { return goal; }

    public List<Enemy> getEnemies() { return enemies; }

    public List<Coin> getCoins() { return coins; }

    public boolean allCoinsCollected() {
        for (Coin coin : coins) {
            if (!coin.isCollected()) return false;
        }
        return true;
    }
}