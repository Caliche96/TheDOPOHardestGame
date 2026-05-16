package Dominio;

public class Cell {
    private Position position;
    private CellType type;

    public Cell(Position position, CellType type) {
        this.position = position;
        this.type = type;
    }

    public boolean isWalkable() {
        return type != CellType.WALL;
    }

    public boolean isWall() {
        return type == CellType.WALL;
    }
    public boolean isGoal() {
        return type == CellType.GOAL;
    }
    public boolean isSafeZone() {
    	return type== CellType.SAFE_ZONE;
    }
    public boolean isSpawnZone() {
    	return type== CellType.SPAWN_ZONE;
    }

    //Getters
    public Position getPosition() { 
    	return position; 
    }
    
    public CellType getType() { 
    	return type; 
    }
    
    //Setters
    
    public void setType(CellType type) { 
    	this.type = type; 
    }
}