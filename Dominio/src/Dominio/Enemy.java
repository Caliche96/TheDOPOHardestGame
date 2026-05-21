package Dominio;

/**
 * Clase abstracta que representa un enemigo
 */
public abstract class Enemy {
    protected Position position;
    protected double speed;
    protected double size;
    protected boolean active;
    protected MovementPattern movementPattern;
    protected GameBoard board;

    /**
     * Constructor de la clase Enemy
     * 
     * @param initalposition  Posición inicial del enemigo
     * @param speed           Velocidad del enemigo
     * @param size            Tamaño del enemigo
     * @param movementPattern Patrón de movimiento del enemigo
     */
    public Enemy(Position initalposition, double speed, double size, MovementPattern movementPattern) {
        this.position = initalposition;
        this.speed = speed;
        this.size = size;
        this.movementPattern = movementPattern;
        this.active = true;
    }
    
    /**
     * Asigna el tablero al enemigo. Debe llamarse tras cargarlo con LevelLoader
     * Es necesario para que el patrón de movimiento verifique colisiones
     * @param board	tablero del nivel actual
     */
    public void setBoard(GameBoard board) {
    	this.board=board;
    }

    /**
     * Actualiza la posición del enemigo
     */
    public void update() {
        if (active && board !=null) {
            movementPattern.move(this,board);
        }
    }

    /**
     * Verifica si el jugador colisiona con el enemigo
     * 
     * @param player Jugador
     * @return true si el jugador colisiona con el enemigo, false en caso contrario
     */
    public boolean collides(Player player) {
        return position.equals(player.getPosition());
    }

    /**
     * Destruye al enemigo
     */
    public void destroy() {
        active = false;
    }

    /**
     * Verifica si el enemigo está activo
     * 
     * @return true si el enemigo está activo, false en caso contrario
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Obtiene la posición del enemigo
     * 
     * @return Posición del enemigo
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Obtiene la velocidad del enemigo
     * 
     * @return Velocidad del enemigo
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Obtiene el tamaño del enemigo
     * 
     * @return Tamaño del enemigo
     */
    public double getSize() {
        return size;
    }

    /**
     * Obtiene el patrón de movimiento del enemigo
     * 
     * @return Patrón de movimiento del enemigo
     */
    public MovementPattern getMovementPattern() {
        return movementPattern;
    }

    /**
     * Establece la posición del enemigo
     * 
     * @param position Nueva posición del enemigo
     */
    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * Establece la velocidad del enemigo
     * 
     * @param speed Nueva velocidad del enemigo
     */
    public void setSpeed(double speed) {
        this.speed = speed;
    }
}