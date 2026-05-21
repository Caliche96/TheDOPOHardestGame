package Dominio;

/**
 * Patrón de movimiento vertical con rebote.
 *
 * El enemigo se mueve hacia arriba o abajo. Cuando la celda
 * siguiente es una pared, zona final, zona segura o está fuera del tablero,
 * invierte la dirección en lugar de avanzar.
 */
public class VerticalMovement implements MovementPattern {

    /** -1 = arriba, 1 = abajo */
    private int direction;

    public VerticalMovement() {
        direction = -1;
    }

    @Override
    public void move(Enemy enemy, GameBoard board) {
        Position current = enemy.getPosition();
        Position next = new Position(current.getRow() + direction, current.getColumn());

        if (shouldBounce(next, board)) {
            direction *= -1;
            next = new Position(current.getRow() + direction, current.getColumn());
        }

        if (!shouldBounce(next, board)) {
            enemy.setPosition(next);
        }
    }

    /**
     * Determina si el enemigo debe rebotar en la posición indicada.
     */
    private boolean shouldBounce(Position pos, GameBoard board) {
        if (!board.isInside(pos)) return true;
        CellType type = board.getCell(pos.getRow(), pos.getColumn()).getType();
        return type == CellType.WALL
            || type == CellType.EMPTY
            || type == CellType.GOAL
            || type == CellType.SAFE_ZONE;
    }
}