package Dominio;

import java.util.Objects;

/**
 * Representa una posición en el tablero.
 */
public class Position {

    private int row;
    private int column;

    /**
     * Crea una nueva posición con la fila y columna especificadas.
     * @param row La fila de la posición.
     * @param column La columna de la posición.
     */
    public Position(int row, int column) {
        this.row    = row;
        this.column = column;
    }

    /**
     * Obtiene la fila de la posición.
     * @return La fila de la posición.
     */
    public int getRow(){ 
        return row;    
    }

    /**
     * Obtiene la columna de la posición.
     * @return La columna de la posición.
     */
    public int getColumn(){ 
        return column; 
    }

    /**
     * Dos posiciones son iguales si tienen la misma fila y columna.
     * @param obj El objeto a comparar.
     * @return true si las posiciones son iguales, false en caso contrario.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)             return true;
        if (obj == null)             return false;
        if (!(obj instanceof Position)) return false;
        Position other = (Position) obj;
        return row == other.row && column == other.column;
    }

    /**
     * CRÍTICO para HashMap/HashSet: debe ser consistente con equals().
     * Sin este método el BFS genera un bucle infinito → OutOfMemoryError.
     */
    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }
}