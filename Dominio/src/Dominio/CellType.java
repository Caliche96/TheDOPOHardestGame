package Dominio;

/**
 * Enum que representa el tipo de celda
 * WALKABLE: Celda transitable
 * WALL: Muro
 * GOAL: Meta
 * SAFE_ZONE: Zona segura
 * SPAWN_ZONE: Zona de spawn
 * EMPTY: Celda vacia
 */
public enum CellType {
	WALKABLE, WALL, GOAL, SAFE_ZONE, SPAWN_ZONE, EMPTY
}
