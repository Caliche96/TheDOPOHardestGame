package Dominio;

public class Cell {
    private Position position;
    private CellType type;

    /**
     * Constructor de la clase Cell
     * 
     * @param position Posición de la celda
     * @param type     Tipo de celda
     */
    public Cell(Position position, CellType type) {
        this.position = position;
        this.type = type;
    }

    /**
     * Verifica si la celda es transitable
     * 
     * @return true si la celda es transitable, false en caso contrario
     */
    public boolean isWalkable() {
        return type != CellType.WALL;
    }

    /**
     * Verifica si la celda es un muro
     * 
     * @return true si la celda es un muro, false en caso contrario
     */
    public boolean isWall() {
        return type == CellType.WALL;
    }

    /**
     * Verifica si la celda es una meta
     * 
     * @return true si la celda es una meta, false en caso contrario
     */
    public boolean isGoal() {
        return type == CellType.GOAL;
    }

    /**
     * Verifica si la celda es una zona segura
     * 
     * @return true si la celda es una zona segura, false en caso contrario
     */
    public boolean isSafeZone() {
        return type == CellType.SAFE_ZONE;
    }

    /**
     * Verifica si la celda es una zona de spawn
     * 
     * @return true si la celda es una zona de spawn, false en caso contrario
     */
    public boolean isSpawnZone() {
        return type == CellType.SPAWN_ZONE;
    }

    /**
     * Obtiene la posición de la celda
     * 
     * @return Posición de la celda
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Obtiene el tipo de la celda
     * 
     * @return Tipo de la celda
     */
    public CellType getType() {
        return type;
    }

    /**
     * Establece el tipo de la celda
     * 
     * @param type Nuevo tipo de la celda
     */
    public void setType(CellType type) {
        this.type = type;
    }
}