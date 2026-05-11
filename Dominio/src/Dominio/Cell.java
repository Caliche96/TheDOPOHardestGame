package Dominio;

public class Cell {
    private Position position;
    private CellType type;

    public Cell(Position position, CellType type) {
        this.position = position;
        this.type = type;
    }

    public boolean isWalkable() {
        return type == CellType.WALKABLE || type == CellType.START || type == CellType.GOAL;
    }

    public boolean isWall() {
        return type == CellType.WALL;
    }

    public boolean isStart() {
        return type == CellType.START;
    }

    public boolean isGoal() {
        return type == CellType.GOAL;
    }

    public Position getPosition() { return position; }
    public CellType getType() { return type; }
    public void setType(CellType type) { this.type = type; }
}