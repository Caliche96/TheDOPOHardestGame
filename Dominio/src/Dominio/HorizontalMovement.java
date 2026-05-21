package Dominio;

public class HorizontalMovement implements MovementPattern{
	private int direction;
	
	/**
	 *  1= derecha, -1= izquierda
	 *  
	 */
	public HorizontalMovement() {
		direction=1;
	}

	@Override
	public void move(Enemy enemy,GameBoard board) {
		Position current = enemy.getPosition();
		Position next = new Position(current.getRow(), current.getColumn()+direction);
		if (shouldBounce(next, board)) {
            direction *= -1;
            // Recalcular con la dirección invertida
            next = new Position(current.getRow(), current.getColumn() + direction);
        }

        // Solo mover si la nueva posición es válida (por si está encerrado)
        if (!shouldBounce(next, board)) {
            enemy.setPosition(next);
        }
    }

    /**
     * Determina si el enemigo debe rebotar en la posición indicada.
     * Rebota si está fuera del tablero, es una pared, zona de meta o zona segura.
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
