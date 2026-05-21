package Dominio;

public class GameBoard {
    private int rows;
    private int columns;
    private Cell[][] cells;

    /**
     * Constructor de la clase GameBoard.
     * @param rows número de filas del tablero
     * @param columns número de columnas del tablero
     */
    public GameBoard(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        cells= new Cell[rows][columns];
        initializeBoard();
    }

    /**
     * Busca la mejor posición para el jugador máquina.
     * Prioriza las monedas, luego la zona spawn.
     * @param board tablero de juego
     * @return posición objetivo para el jugador máquina
     */
    private void initializeBoard() {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                Position pos = new Position(row, column);
                cells[row][column] = new Cell(pos, CellType.EMPTY);
            }
        }
    }
    
    //Validaciones
    /**
     * Verifica si una posición está dentro de los límites del tablero.
     * @param position posición a verificar
     * @return true si la posición está dentro del tablero, false en caso contrario
     */
    public boolean isInside(Position position) {
    	int row=position.getRow();
    	int column=position.getColumn();
        return row>=0 && row<rows &&  column>=0 && column<columns;
    }

    /**
     * Verifica si una posición es un muro.
     * @param position posición a verificar
     * @return true si la posición es un muro, false en caso contrario
     */
    public boolean isWall(Position position) {
        if (!isInside(position)) {
        	return true;
        }
        return getCell(position.getRow(), position.getColumn()).isWall();
    }
    
    /**
     * Verifica si una posición es transitable.
     * @param position posición a verificar
     * @return true si la posición es transitable, false en caso contrario
     */
    public boolean isWalkable(Position position) {
    	if(!isInside(position)){
    		return false;
    	}
    	
    	return getCell(position.getRow(), position.getColumn()).isWalkable();
    }

    //Celdas
    /**
     * Obtiene la celda en una posición específica.
     * @param row fila de la celda
     * @param column columna de la celda
     * @return celda en la posición especificada
     */
    public Cell getCell(int row, int column) {
       return cells[row][column];
    }

    /**
     * Establece el tipo de una celda.
     * @param row fila de la celda
     * @param column columna de la celda
     * @param type nuevo tipo de la celda
     */
    public void setCell(int row,int column, CellType type) {
        Position position = new Position(row, column);
        cells[row][column]= new Cell(position, type);
    }

    //Getters
    /**
     * Obtiene el número de filas del tablero.
     * @return número de filas
     */
    public int getRows() { 
    	return rows; 
    }
    
    /**
     * Obtiene el número de columnas del tablero.
     * @return número de columnas
     */
    public int getColumns() { 
    	return columns; 
    }
    
    /**
     * Obtiene todas las celdas del tablero.
     * @return arreglo de celdas
     */
    public Cell[][] getCells(){
    	return cells;
    }
}