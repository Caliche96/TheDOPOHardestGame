package Dominio;

/**
 * Enemigo horizontal más rápido que BasicBluePoint.  
*/
public class AcceleratedEnemy extends Enemy {
    public AcceleratedEnemy(float x, float y) {
        super(x, y, 6.0f, GameConfig.CELL_SIZE - 4f, new HorizontalMovement());
    }
    /** @deprecated Usar constructor con píxeles. */
    public AcceleratedEnemy(Position p) {
        this(p.getColumn() * GameConfig.CELL_SIZE, p.getRow() * GameConfig.CELL_SIZE);
    }
}