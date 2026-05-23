package Dominio;

/**
 * Patrón de movimiento horizontal en píxeles con rebote.
 * El enemigo se mueve a velocidad constante en X. Cuando el bounding box
 * toca una celda WALL, EMPTY, GOAL o SAFE_ZONE invierte la dirección.
 */
public class HorizontalMovement implements MovementPattern {

    private float direction; // 1.0 = derecha, -1.0 = izquierda

    /**
     * Constructor de la clase HorizontalMovement.
     */
    public HorizontalMovement() {
        direction = 1f;
    }

    /**
     * Mueve el enemigo según el patrón de movimiento horizontal.
     * @param enemy El enemigo a mover.
     * @param board El tablero del juego.
     */
    @Override
    public void move(Enemy enemy, GameBoard board) {
        int cell = GameConfig.CELL_SIZE;
        float newX = enemy.getX() + enemy.getSpeed() * direction;

        if (collidesHorizontal(newX, enemy.getY(), enemy.getSize(), board, cell)) {
            direction *= -1;
            newX = enemy.getX() + enemy.getSpeed() * direction;
        }

        if (!collidesHorizontal(newX, enemy.getY(), enemy.getSize(), board, cell)) {
            enemy.setX(newX);
        }
    }

    /**
     * Verifica si el enemigo colisiona con un obstáculo horizontalmente.
     * @param x coordenada X del enemigo
     * @param y coordenada Y del enemigo
     * @param size tamaño del enemigo
     * @param board tablero del juego
     * @param cell tamaño de la celda
     * @return true si hay colisión, false en caso contrario
     */
    private boolean collidesHorizontal(float x, float y, float size, GameBoard board, int cell) {
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