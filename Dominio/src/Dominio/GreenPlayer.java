package Dominio;

public class GreenPlayer extends Player {

    private boolean shieldUsed;

    public GreenPlayer(String name, float spawnX, float spawnY) {
        super(name, spawnX, spawnY, 2.0f, GameConfig.CELL_SIZE - 6f);
        this.shieldUsed = false;
    }

    @Override
    public void receiveHit() {
        if (!shieldUsed) {
            shieldUsed = true;
            speed = 1.4f; // penalización de velocidad tras recibir el golpe
        } else {
            die();
        }
    }
}