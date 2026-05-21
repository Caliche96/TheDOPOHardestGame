package Dominio;

/**
 * Patrón de movimiento vertical en píxeles con rebote.
 */
public class VerticalMovement implements MovementPattern {

    private float direction; // -1.0 = arriba, 1.0 = abajo

    public VerticalMovement() {
        direction = -1f;
    }

    @Override
    public void move(Enemy enemy, GameBoard board) {
        int cell = GameConfig.CELL_SIZE;
        float newY = enemy.getY() + enemy.getSpeed() * direction;

        if (collidesVertical(enemy.getX(), newY, enemy.getSize(), board, cell)) {
            direction *= -1;
            newY = enemy.getY() + enemy.getSpeed() * direction;
        }

        if (!collidesVertical(enemy.getX(), newY, enemy.getSize(), board, cell)) {
            enemy.setY(newY);
        }
    }

    private boolean collidesVertical(float x, float y, float size, GameBoard board, int cell) {
        float[] cx = { x, x + size - 1, x, x + size - 1 };
        float[] cy = { y, y, y + size - 1, y + size - 1 };
        for (int i = 0; i < 4; i++) {
            int col = (int)(cx[i] / cell);
            int row = (int)(cy[i] / cell);
            if (!board.isInside(new Position(row, col))) return true;
            CellType t = board.getCell(row, col).getType();
            if (t == CellType.WALL || t == CellType.EMPTY) return true;
        }
        return false;
    }
}