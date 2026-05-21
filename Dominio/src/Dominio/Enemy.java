package Dominio;

/**
 * Clase abstracta que representa un enemigo del juego.
 *
 * Al igual que Player, se mueve en píxeles flotantes (x, y).
 * El MovementPattern maneja la dirección y el rebote contra paredes.
 */
public abstract class Enemy {

    protected float   x;
    protected float   y;
    protected float   speed;
    protected float   size;
    protected boolean active;
    protected MovementPattern movementPattern;
    protected GameBoard board;

    /**
     * Constructor de la clase Enemy.
     * @param x coordenada x inicial
     * @param y coordenada y inicial
     * @param speed velocidad de movimiento
     * @param size tamaño del enemigo
     * @param movementPattern patrón de movimiento
     */
    public Enemy(float x, float y, float speed, float size, MovementPattern movementPattern) {
        this.x               = x;
        this.y               = y;
        this.speed           = speed;
        this.size            = size;
        this.movementPattern = movementPattern;
        this.active          = true;
    }


    /**
     * Establece el tablero de juego para este enemigo.
     * @param board el tablero de juego
     */
    public void setBoard(GameBoard board) { this.board = board; }

    /**
     * Actualiza el estado del enemigo.
     */
    public void update() {
        if (active && board != null) {
            movementPattern.move(this, board);
        }
    }

    /**
     *Colisión AABB entre este enemigo y un jugador.
     */
    public boolean collides(Player player) {
        return x < player.getX() + player.getSize() &&
               x + size > player.getX()             &&
               y < player.getY() + player.getSize() &&
               y + size > player.getY();
    }

    /**
     *Destruye el enemigo.
     */
    public void destroy(){ 
        active = false; 
    }
        
     /**
      * Indica si el enemigo está activo.
      * @return
      */
     public boolean isActive(){ 
        return active; 
    }

     /**
      * Obtiene la coordenada x del enemigo.
      * @return
      */
    public float getX(){ 
        return x; 
    }

    /**
     * Obtiene la coordenada y del enemigo.
     * @return
     */
    public float getY(){ 
        return y; 
    }

    /**
     * Obtiene la velocidad del enemigo.
     * @return
     */
    public float getSpeed(){
        return speed; 
    }

    /**
     * Obtiene el tamaño del enemigo.
     * @return
     */
    public float getSize(){
        return size;
    }

    /**
     * Obtiene el patrón de movimiento del enemigo.
     * @return
     */
    public MovementPattern getMovementPattern(){
        return movementPattern; 
    }

    /**
     * Obtiene el tablero de juego para este enemigo.
     * @return
     */
    public GameBoard getBoard(){
        return board;
    }

    /**
     * Establece la coordenada x del enemigo.
     * @param x
     */
    public void setX(float x){
        this.x = x;
    }

    /**
     * Establece la coordenada y del enemigo.
     * @param y
     */
    public void setY(float y){
        this.y = y;
    }
    
    /**
     * Establece la velocidad del enemigo.
     * @param s
     */
    public void setSpeed(float s){
        this.speed = s;
    }

    /** @deprecated Usar getX()/getY(). Mantenido por compatibilidad con LevelLoader. */
    public Position getPosition() {
        return new Position((int)(y / GameConfig.CELL_SIZE), (int)(x / GameConfig.CELL_SIZE));
    }

    /** @deprecated Usar setX/setY. */
    public void setPosition(Position p) {
        this.x = p.getColumn() * GameConfig.CELL_SIZE;
        this.y = p.getRow()    * GameConfig.CELL_SIZE;
    }
}