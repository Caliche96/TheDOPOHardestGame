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
     * Crea un nuevo enemigo.
     * @param x Coordenada X de la posición inicial.
     * @param y Coordenada Y de la posición inicial.
     * @param speed Velocidad del enemigo.
     * @param size Tamaño del enemigo.
     * @param movementPattern Patrón de movimiento del enemigo.
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
     * Establece el tablero de juego para el enemigo.
     * @param board Tablero de juego.
     */
    public void setBoard(GameBoard board){ 
        this.board = board; 
    }

    /**
     * Actualiza el estado del enemigo en cada tick.
     */
    public void update() {
        if (active && board != null) {
            movementPattern.move(this, board);
        }
    }

    /**
     * Colisión AABB entre este enemigo y un jugador.
     */
    public boolean collides(Player player) {
        return x < player.getX() + player.getSize() &&
               x + size > player.getX()             &&
               y < player.getY() + player.getSize() &&
               y + size > player.getY();
    }

    /**
     * Destruye el enemigo.
     */
    public void destroy(){ 
        active = false; 
    }

    /**
     * Verifica si el enemigo está activo.
     * @return true si el enemigo está activo, false en caso contrario.
     */
    public boolean isActive(){ 
        return active; 
    }

    /**
     * Obtiene la coordenada X del enemigo.
     * @return La coordenada X del enemigo.
     */
    public float getX(){ 
        return x; 
    }

    /**
     * Obtiene la coordenada Y del enemigo.
     * @return La coordenada Y del enemigo.
     */
    public float getY(){ 
        return y; 
    }

    /**
     * Obtiene la velocidad del enemigo.
     * @return La velocidad del enemigo.
     */
    public float getSpeed(){ 
        return speed; 
    }

    /**
     * Obtiene el tamaño del enemigo.
     * @return El tamaño del enemigo.
     */
    public float getSize(){
        return size; 
    }

    /**
     * Obtiene el patrón de movimiento del enemigo.
     * @return El patrón de movimiento del enemigo.
     */
    public MovementPattern getMovementPattern(){ 
        return movementPattern; 
    }

    /**
     * Obtiene el tablero de juego para el enemigo.
     * @return El tablero de juego para el enemigo.
     */
    public GameBoard getBoard(){ 
        return board; 
    }

    /**
     * Establece la coordenada X del enemigo.
     * @param x La coordenada X del enemigo.
     */
    public void setX(float x){ 
        this.x = x; 
    }
    
    /**
     * Establece la coordenada Y del enemigo.
     * @param y La coordenada Y del enemigo.
     */
    public void setY(float y){ 
        this.y = y; 
    }

    /**
     * Establece la velocidad del enemigo.
     * @param s La velocidad del enemigo.
     */
    public void setSpeed(float s){
        this.speed = s; 
    }
    
}