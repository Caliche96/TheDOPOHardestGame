package Dominio;

public class GameBoard {
    private int rows;
    private int columns;
    private Cell[][] cells;


    public GameBoard(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        cells= new Cell[rows][columns];
        initializeBoard();
    }

    private void initializeBoard() {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                Position pos = new Position(row, column);
                cells[row][column] = new Cell(pos, CellType.EMPTY);
            }
        }
    }
    
    //Validaciones

    public boolean isInside(Position position) {
    	int row=position.getRow();
    	int column=position.getColumn();
        return row>=0 && row<rows &&  column>=0 && column<columns;
    }

    public boolean isWall(Position position) {
        if (!isInside(position)) {
        	return true;
        }
        return getCell(position.getRow(), position.getColumn()).isWall();
    }
    
    public boolean isWalkable(Position position) {
    	if(!isInside(position)){
    		return false;
    	}
    	
    	return getCell(position.getRow(), position.getColumn()).isWalkable();
    }

    //Celdas
    public Cell getCell(int row, int column) {
       return cells[row][column];
    }

    public void setCell(int row,int column, CellType type) {
        Position position = new Position(row, column);
        cells[row][column]= new Cell(position, type);
    }

    //Getters
    public int getRows() { 
    	return rows; 
    }
    
    public int getColumns() { 
    	return columns; 
    }
    
    public Cell[][] getCells(){
    	return cells;
    }
}