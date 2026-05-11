package Dominio;

public class Board {
    private int rows;
    private int columns;
    private Cell[][] grid;
    private Position goal;

    public Board(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.grid = new Cell[rows][columns];
        initializeGrid();
    }

    private void initializeGrid() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                Position pos = new Position(c, r);
                grid[r][c] = new Cell(pos, CellType.WALKABLE);
            }
        }
    }

    public boolean isInside(Position position) {
        return position.getX() >= 0 && position.getX() < columns
            && position.getY() >= 0 && position.getY() < rows;
    }

    public boolean isWall(Position position) {
        if (!isInside(position)) return true;
        return grid[position.getY()][position.getX()].isWall();
    }

    public Cell getCell(Position position) {
        if (!isInside(position)) return null;
        return grid[position.getY()][position.getX()];
    }

    public void setCell(Position position, CellType type) {
        if (isInside(position)) {
            grid[position.getY()][position.getX()].setType(type);
            if (type == CellType.GOAL) {
                this.goal = position.copy();
            }
        }
    }

    public int getRows() { return rows; }
    public int getColumns() { return columns; }
    public Position getGoal() { return goal; }
}