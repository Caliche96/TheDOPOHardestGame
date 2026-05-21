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
     * Verifica si el bounding box del jugador en (testX, testY) colisiona con
     * alguna celda de tipo WALL o EMPTY en el tablero.
     * Comprueba las cuatro esquinas del cuadrado.
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

    public void die() {
        deaths++;
        respawn();
    }

    public void respawn() {
        x     = spawnX;
        y     = spawnY;
        alive = true;
    }

    public void addLife() { lives++; }

    // ──── Monedas ────

    public void addCoin()   { collectedCoins++; }
    public void resetCoins(){ collectedCoins = 0; }

    // ──── Spawn ────

    public void setSpawnPoint(float spawnX, float spawnY) {
        this.spawnX = spawnX;
        this.spawnY = spawnY;
    }

    // ──── Colisiones entre jugadores ────

    /**
     * Verifica colisión AABB entre este jugador y otro.
     */
    public boolean collides(Player other) {
        return x < other.x + other.size &&
               x + size > other.x       &&
               y < other.y + other.size &&
               y + size > other.y;
    }

    /**
     * Verifica si el centro del jugador está dentro de una celda de tipo goal.
     */
    public boolean isInGoal(GameBoard board, int cellSize) {
        return getCellType(board, cellSize) == CellType.GOAL;
    }

    public boolean isInSpawnZone(GameBoard board, int cellSize) {
        return getCellType(board, cellSize) == CellType.SPAWN_ZONE;
    }

    /**
     * Verifica si el centro del jugador está en una zona segura.
     */
    public boolean isInSafeZone(GameBoard board, int cellSize) {
        return getCellType(board, cellSize) == CellType.SAFE_ZONE;
    }

    /**
     * Devuelve el tipo de celda donde está el centro del jugador.
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
    public abstract void receiveHit();

    // ──── Getters ────
    public String getName()          { return name; }
    public float  getX()             { return x; }
    public float  getY()             { return y; }
    public float  getSpeed()         { return speed; }
    public float  getSize()          { return size; }
    public int    getDeaths()        { return deaths; }
    public int    getCollectedCoins(){ return collectedCoins; }
    public int    getLives()         { return lives; }
    public boolean isAlive()         { return alive; }
    public Color  getBorderColor()   { return borderColor; }
    public Color  getBodyColor()     { return bodyColor; }

    // ──── Setters ────
    public void setBorderColor(Color c)    { this.borderColor = c; }
    public void setBodyColor(Color c)      { this.bodyColor = c; }
    public void setX(float x)             { this.x = x; }
    public void setY(float y)             { this.y = y; }
    public void setDeaths(int d)          { this.deaths = d; }
    public void setCollectedCoins(int c)  { this.collectedCoins = c; }

    // ──── Compatibilidad con código existente ────
    /** @deprecated Usar getX() / getY() para movimiento en píxeles. */
    public Position getPosition() {
        return new Position((int)(y / GameConfig.CELL_SIZE), (int)(x / GameConfig.CELL_SIZE));
    }
}