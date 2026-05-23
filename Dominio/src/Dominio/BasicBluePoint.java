package Dominio;

/** Enemigo horizontal básico. Velocidad moderada. */
public class BasicBluePoint extends Enemy {
    public BasicBluePoint(float x, float y) {
        super(x, y, 4.5f, GameConfig.CELL_SIZE - 4f, new HorizontalMovement());
    }

}