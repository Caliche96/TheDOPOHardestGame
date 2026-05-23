package Dominio;

/** Enemigo de movimiento vertical. */
public class VerticalSlider extends Enemy {
    /** Crea un slider vertical. */
    public VerticalSlider(float x, float y) {
        super(x, y, 3.0f, GameConfig.CELL_SIZE - 4f, new VerticalMovement());
    }

}