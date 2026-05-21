package Dominio;

public class BluePlayer extends Player {

    public BluePlayer(String name, float spawnX, float spawnY) {
        // Más rápido y ligeramente más grande
        super(name, spawnX, spawnY, 7.0f, GameConfig.CELL_SIZE - 4f);
    }

    @Override
    public void receiveHit() {
        die();
    }
}