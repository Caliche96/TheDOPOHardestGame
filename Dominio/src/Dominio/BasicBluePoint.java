package Dominio;

/** Enemigo horizontal básico. Velocidad moderada. */
public class BasicBluePoint extends Enemy {
    public BasicBluePoint(float x, float y) {
        super(x, y, 7.0f, GameConfig.CELL_SIZE - 4f, new HorizontalMovement());
    }
    /** @deprecated Usar constructor con píxeles. */
    public BasicBluePoint(Position p) {
        this(p.getColumn() * GameConfig.CELL_SIZE, p.getRow() * GameConfig.CELL_SIZE);
    }
}