package Dominio;

public interface MovementPattern {
	/**
	 * Mueve el enemigo un paso según el patrón.
	 * @param enemy	Enemigo a mover
	 * @param board	Tablero actual, usado para verificar la siguiente celda
	 */
	void move(Enemy enemy,GameBoard board);
}
