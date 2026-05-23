package Dominio;

import java.awt.Color;

/**
 * Clase abstracta que representa a un jugador del juego.
 *
 * El jugador se mueve en coordenadas de píxeles flotantes (x, y),
 * permitiendo posicionarse entre celdas para un movimiento fluido.
 * Las colisiones se detectan por AABB contra los bordes de las celdas del GameBoard.
 *
 * La velocidad base es en píxeles por tick. El tamaño visual del jugador
 * es (CELL_SIZE - 6) × (CELL_SIZE - 6) píxeles.
 */
public abstract class Player {

    // ──── Atributos ────
    protected String name;
    protected String skinType;
    /** Posición en píxeles (esquina superior izquierda del bounding box). */
    protected float x;
    protected float y;

    /** Posición de spawn en píxeles. */
    protected float spawnX;
    protected float spawnY;

    /** Velocidad en píxeles por tick. */
    protected float speed;

    /** Tamaño visual en píxeles (ancho y alto del cuadrado). */
    protected float size;

    protected int     deaths;
    protected int     collectedCoins;
    protected int     lives;
    protected boolean alive;
    protected Color   borderColor;
    protected Color   bodyColor;

    // ──── Constructor ────

    /**
     * @param name     nombre del jugador
     * @param spawnX   posición inicial X en píxeles
     * @param spawnY   posición inicial Y en píxeles
     * @param speed    velocidad en píxeles por tick
     * @param size     tamaño del cuadrado en píxeles
     */
    public Player(String name, float spawnX, float spawnY, float speed, float size) {
        this.name           = name;
        this.x              = spawnX;
        this.y              = spawnY;
        this.spawnX         = spawnX;
        this.spawnY         = spawnY;
        this.speed          = speed;
        this.size           = size;
        this.deaths         = 0;
        this.alive          = true;
        this.collectedCoins = 0;
        this.lives          = 0;
    }

    // ──── Movimiento ────

    /**
     * Mueve al jugador en la dirección indicada, respetando las paredes del tablero.
     * Usa detección AABB — comprueba cada esquina del bounding box del jugador.
     *
     * @param direction dirección del movimiento
     * @param board     tablero del nivel para verificar colisiones
     * @param cellSize  tamaño de celda en píxeles
     */
    public void move(Direction direction, GameBoard board, int cellSize) {
        float dx = 0, dy = 0;
        switch (direction) {
            case UP:         dy = -speed; break;
            case DOWN:       dy =  speed; break;
            case LEFT:       dx = -speed; break;
            case RIGHT:      dx =  speed; break;
            case UP_LEFT:    dx = -speed * 0.707f; dy = -speed * 0.707f; break;
            case UP_RIGHT:   dx =  speed * 0.707f; dy = -speed * 0.707f; break;
            case DOWN_LEFT:  dx = -speed * 0.707f; dy =  speed * 0.707f; break;
            case DOWN_RIGHT: dx =  speed * 0.707f; dy =  speed * 0.707f; break;
            default: break;
        }

        float newX = x + dx;
        if (!collidesWithWall(newX, y, board, cellSize)) {
            x = newX;
        }

        float newY = y + dy;
        if (!collidesWithWall(x, newY, board, cellSize)) {
            y = newY;
        }
    }

    /**
    * Verifica si el bounding box del jugador colisiona con una pared en la posición dada.
    * @param testX La coordenada X a probar.
    * @param testY La coordenada Y a probar.
    * @param board El tablero del juego para verificar las celdas.
    * @param cellSize El tamaño de las celdas en píxeles para calcular las posiciones de las esquinas.
    * @return true si hay colisión con una pared, false en caso contrario.
    */
    private boolean collidesWithWall(float testX, float testY, GameBoard board, int cellSize) {
        float[] cornersX = { testX, testX + size - 1, testX,            testX + size - 1 };
        float[] cornersY = { testY, testY,             testY + size - 1, testY + size - 1 };

        for (int i = 0; i < 4; i++) {
            int col = (int)(cornersX[i] / cellSize);
            int row = (int)(cornersY[i] / cellSize);

            // Bloquear en todos los bordes del tablero
            if (testX < 0 || testY < 0) return true;
            if (row < 0 || row >= board.getRows() || col < 0 || col >= board.getColumns()) {
                return true;
            }

            CellType type = board.getCell(row, col).getType();
            if (type == CellType.WALL || type == CellType.EMPTY) {
                return true;
            }
        }
        return false;
    }

    // ──── Vidas y muertes ────

    /**
     * Hace que el jugador muera y reinicia su posición.
     */
    public void die() {
        deaths++;
        respawn();
    }

    /**
     * Reinicia la posición del jugador.
     */
    public void respawn() {
        x     = spawnX;
        y     = spawnY;
        alive = true;
    }

    /**
     * Añade una vida al jugador.
     */
    public void addLife() { 
        lives++; 
    }

    /**
     * Activa un escudo temporal en el jugador.
     * Cualquier jugador puede tener escudo — ya sea por ser GreenPlayer
     * o por recoger una LifeSource.
     * Por defecto no hace nada; GreenPlayer lo sobreescribe.
     * RedPlayer y BluePlayer tienen su propia implementación básica.
     */
    public void activateShield() { }

    // ──── Monedas ────

    /* Añade una moneda al contador del jugador. Se llama desde Game cuando el jugador
     * recoge una moneda en el tablero. */
    public void addCoin(){ 
        collectedCoins++; 
    }

    /**
     * Reinicia el contador de monedas del jugador.
     */
    public void resetCoins(){ collectedCoins = 0; }

    // ──── Spawn ────
    /**
    * Establece un nuevo punto de spawn para el jugador.
    * @param spawnX La coordenada X del nuevo punto de spawn en píxeles.
    * @param spawnY La coordenada Y del nuevo punto de spawn en píxeles.
    */
    public void setSpawnPoint(float spawnX, float spawnY) {
        this.spawnX = spawnX;
        this.spawnY = spawnY;
    }

    // ──── Colisiones entre jugadores ────

    /**
     * Verifica si el bounding box de este jugador colisiona con el de otro jugador.
     * @param other el otro jugador a comparar
     * @return true si hay colisión, false en caso contrario
     */
    public boolean collides(Player other) {
        return x < other.x + other.size &&
               x + size > other.x       &&
               y < other.y + other.size &&
               y + size > other.y;
    }

    /**
     * Verifica si el centro del jugador está en la celda de meta.
     * @param board El tablero del juego para verificar la celda actual.
     * @param cellSize El tamaño de las celdas en píxeles para calcular la posición en el tablero.
     * @return true si el centro del jugador está en la celda de meta, false en caso contrario.
     */
    public boolean isInGoal(GameBoard board, int cellSize) {
        return getCellType(board, cellSize) == CellType.GOAL;
    }

    /**
     * Verifica si el centro del jugador está en la zona de spawn.
     * @param board El tablero del juego para verificar la celda actual.
     * @param cellSize El tamaño de las celdas en píxeles para calcular la posición en el tablero.
     * @return true si el centro del jugador está en la zona de spawn, false en caso contrario.
     */
    public boolean isInSpawnZone(GameBoard board, int cellSize) {
        return getCellType(board, cellSize) == CellType.SPAWN_ZONE;
    }

    /**
     * Verifica si el centro del jugador está en la zona segura.
     * @param board El tablero del juego para verificar la celda actual.
     * @param cellSize El tamaño de las celdas en píxeles para calcular la posición en el tablero.
     * @return true si el centro del jugador está en la zona segura, false en caso contrario.
     */
    public boolean isInSafeZone(GameBoard board, int cellSize) {
        return getCellType(board, cellSize) == CellType.SAFE_ZONE;
    }

    /**
    * Obtiene el tipo de celda en la que se encuentra el centro del jugador.
    * @param board El tablero del juego para verificar la celda actual.
    * @param cellSize El tamaño de las celdas en píxeles para calcular la posición en el tablero.
    * @return El tipo de celda en la que se encuentra el centro del jugador.
    */
    public CellType getCellType(GameBoard board, int cellSize) {
        int col = (int)((x + size / 2) / cellSize);
        int row = (int)((y + size / 2) / cellSize);
        if (row < 0 || row >= board.getRows() || col < 0 || col >= board.getColumns()) {
            return CellType.EMPTY;
        }
        return board.getCell(row, col).getType();
    }

    // ──── Habilidad especial ────
    /**
     * Recibe un golpe y actualiza el estado del jugador.
     */
    public abstract void receiveHit();

    /**
     * @return El tipo de jugador.
     */
    public abstract String getPlayerType();

    /**
     * Actualización por tick — cada subclase puede sobreescribir para
     * manejar temporizadores internos (ej. invencibilidad de GreenPlayer).
     * Por defecto no hace nada.
     */
    public void tick() { }

    /**
     * @return true si el jugador está en frames de invencibilidad.
     * Por defecto false; GreenPlayer lo sobreescribe.
     */
    public boolean isInvincible(){ 
        return false; 
    }

    /**
     * @return true si el escudo especial está disponible.
     * Por defecto false; GreenPlayer lo sobreescribe.
     */
    public boolean isShieldActive() {
        return false; 
    }

    // ──── Getters ────
    /**
     *  Obtiene el nombre del jugador.
     * @return El nombre del jugador.
     */
    public String getName(){ 
        return name; 
    }

    /**
     * Obtiene la posición X del jugador en píxeles.
     * @return La posición X del jugador en píxeles.
     */
    public float getX(){ 
        return x; 
    }

    /**
     * Obtiene la posición Y del jugador en píxeles.
     * @return La posición Y del jugador en píxeles.
     */
    public float getY(){ 
        return y; 
    }

    /**
     * Obtiene la velocidad del jugador.
     * @return La velocidad del jugador.
     */
    public float getSpeed(){ 
        return speed; 
    }

    /**
     * Obtiene el tamaño del jugador.
     * @return El tamaño del jugador.
     */
    public float getSize(){ 
        return size; 
    }

    /**
     * Obtiene el número de muertes del jugador.
     * @return El número de muertes del jugador.
     */
    public int getDeaths(){ 
        return deaths; 
    }

    /**
     * Obtiene el número de monedas recolectadas por el jugador.
     * @return El número de monedas recolectadas por el jugador.
     */
    public int getCollectedCoins(){ 
        return collectedCoins; 
    }

    /**
     * Obtiene el número de vidas del jugador.
     * @return El número de vidas del jugador.
     */
    public int getLives(){ 
        return lives; 
    }

    /**
    * Verifica si el jugador está vivo.
    * @return true si el jugador está vivo, false en caso contrario.
    */
    public boolean isAlive(){ 
        return alive; 
    }

    /**
     * Obtiene el color del borde del jugador.
     * @return El color del borde del jugador.
     */
    public Color getBorderColor(){ 
        return borderColor; 
    }

    /**
     * Obtiene el color del cuerpo del jugador.
     * @return El color del cuerpo del jugador.
     */
    public Color  getBodyColor() {
        return bodyColor;
    }

    // ──── Setters ────
    /**
     * Establece el color del borde del jugador.
     * @param c El color del borde del jugador.
     */
    public void setBorderColor(Color c){ 
        this.borderColor = c; 
    }

    /**
     * Establece el color del cuerpo del jugador.
     * @param c El color del cuerpo del jugador.
     */
    public void setBodyColor(Color c){ 
        this.bodyColor = c; 
    }

    /**
     * Establece la posición X del jugador en píxeles.
     * @param x La posición X del jugador en píxeles.
     */
    public void setX(float x){ 
        this.x = x; 
    }

    /**
     * Establece la posición Y del jugador en píxeles.
     * @param y La posición Y del jugador en píxeles.
     */
    public void setY(float y){ 
        this.y = y; 
    }
    /**
     * Establece el número de muertes del jugador.
     * @param d El número de muertes del jugador.
     */
    public void setDeaths(int d){ 
        this.deaths = d; 
    }

    /**
    * Establece el número de monedas recolectadas por el jugador.
    * @param c El número de monedas recolectadas por el jugador.
    */
    public void setCollectedCoins(int c){ 
        this.collectedCoins = c; 
    }

    /**
     * Establece la velocidad del jugador.
     * @param vel La velocidad del jugador.
     */
    public void setSpeed(float vel){ 
        this.speed=vel;
    }

    /**
     * Establece el tamaño del jugador.
     * @param tam El tamaño del jugador.
     */
    public void setSize(float tam){
        this.size=tam;
    }

    /**
     * Establece el tipo de piel del jugador.
     * @param newType El nuevo tipo de piel del jugador.
     */
	public void setSkinType(String newType) {
		this.skinType=newType;
	}
	
    /**
     * Obtiene el tipo de piel del jugador.
     * @return El tipo de piel del jugador.
     */
	public String getSkinType() {
		return skinType;
	}
}