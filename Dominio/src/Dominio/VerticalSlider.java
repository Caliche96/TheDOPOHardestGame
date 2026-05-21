package Dominio;

/** Enemigo de movimiento vertical. */
public class VerticalSlider extends Enemy {
    public VerticalSlider(float x, float y) {
        super(x, y, 3.0f, GameConfig.CELL_SIZE - 4f, new VerticalMovement());
    }
    /** @deprecated Usar constructor con píxeles. */
    public VerticalSlider(Position p) {
        this(p.getColumn() * GameConfig.CELL_SIZE, p.getRow() * GameConfig.CELL_SIZE);
    }
}