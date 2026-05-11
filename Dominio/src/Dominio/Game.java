package Dominio;

public class Game {
    private int deaths;
    private boolean finished;
    private Player player;
    private Level currentLevel;

    public Game() {
        this.deaths = 0;
        this.finished = false;
    }

    public void startLevel(Level level) {
        this.currentLevel = level;
        this.player = new Player(level.getStartPosition(), 1);
        this.finished = false;
    }

    public void resetLevel() {
        if (currentLevel != null) {
            player.reset();
            // Reset all coins
            for (Coin coin : currentLevel.getCoins()) {
                // Re-instantiate or mark as not collected via reset method
            }
        }
    }

    public void movePlayer(Direction direction) {
        if (finished || currentLevel == null) return;

        Position current = player.getPosition().copy();
        player.move(direction);

        // Undo move if new position is a wall or outside board
        Board board = currentLevel.getBoard();
        if (board.isWall(player.getPosition())) {
            player.getPosition().setX(current.getX());
            player.getPosition().setY(current.getY());
        }
    }

    public void update() {
        if (finished || currentLevel == null) return;

        // Update all enemies
        for (Enemy enemy : currentLevel.getEnemies()) {
            enemy.update();
        }

        // Check enemy collision -> death
        for (Enemy enemy : currentLevel.getEnemies()) {
            if (enemy.collidesWith(player)) {
                deaths++;
                resetLevel();
                return;
            }
        }

        // Check coin collection
        for (Coin coin : currentLevel.getCoins()) {
            if (coin.collidesWith(player)) {
                coin.collect();
            }
        }

        // Check if player reached goal with all coins collected
        if (currentLevel.allCoinsCollected()
                && currentLevel.getGoal().isReachedBy(player)) {
            finished = true;
        }
    }

    public boolean isFinished() { return finished; }

    public int getDeaths() { return deaths; }

    public Player getPlayer() { return player; }

    public Level getCurrentLevel() { return currentLevel; }
}