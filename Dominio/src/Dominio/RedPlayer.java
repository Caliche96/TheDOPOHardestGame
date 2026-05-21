package Dominio;

public class RedPlayer extends Player {

    public RedPlayer(String name, float spawnX, float spawnY) {
        super(name, spawnX, spawnY, 2.0f, GameConfig.CELL_SIZE - 6f);
    }

    @Override
    public void receiveHit() {
        die();
    }
}