package Dominio;

/**
 * Enemigo horizontal más rápido que BasicBluePoint.  
*/
public class AcceleratedEnemy extends Enemy {
    public AcceleratedEnemy(float x, float y) {
        super(x, y, 6.0f, GameConfig.CELL_SIZE - 4f, new HorizontalMovement());
    }

}