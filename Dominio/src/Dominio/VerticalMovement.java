package Dominio;

/**
 * Patrón de movimiento vertical en píxeles con rebote.
 */
public class VerticalMovement implements MovementPattern {

    private float direction; // -1.0 = arriba, 1.0 = abajo

    /**
     * Crea un patrón de movimiento vertical.
     */
    public VerticalMovement() {
        direction = -1f;
    }

    /**
     * Mueve al enemigo en el patrón vertical.
     * @param enemy El enemigo.
     * @param board El tablero de juego.
     */
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

    /**
     * Verifica si el enemigo colisiona con un obstáculo vertical.
     * @param x La coordenada x del enemigo.
     * @param y La coordenada y del enemigo.
     * @param size El tamaño del enemigo.
     * @param board El tablero de juego.
     * @param cell El tamaño de la celda.
     * @return true si colisiona, false en caso contrario.
     */
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