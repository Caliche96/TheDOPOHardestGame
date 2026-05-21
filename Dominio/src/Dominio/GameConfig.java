package Dominio;

public class GameConfig {

    // ── Dimensiones del tablero ──
    public static final int ROWS    = 10;
    public static final int COLUMNS = 20;

    // ── Tamaño de celda en píxeles ──
    public static final int CELL_SIZE = 26;

    // ── Dimensiones de la ventana de juego ──
    public static final int WINDOW_WIDTH  = 600;
    public static final int WINDOW_HEIGHT = 500;
    public static final int HUD_HEIGHT    = 50;

    // ── Tiempo por defecto ──
    /** 1 minuto 30 segundos × 30 FPS = 2700 ticks. */
    public static final int DEFAULT_TIME_LIMIT = 2700;

    // ── Intervalos de movimiento ──
    /** Cada cuántos ticks se mueve el jugador (~10 movimientos/segundo). */
    public static final int PLAYER_MOVE_INTERVAL = 3;

    /** Cada cuántos ticks se mueve cada enemigo (~7.5 movimientos/segundo). */
    public static final int ENEMY_MOVE_INTERVAL = 4;

    // ── FPS del GameLoop ──
    public static final int FPS = 30;

    /**
     * Leyenda del mapa:
     *  # = pared / vacío (azul, no caminable)
     *  . = walkable (zona ajedrezada)
     *  S = spawn zone (verde, izquierda)
     *  F = goal zone  (verde, derecha)
     *  Z = zona segura intermedia
     *  C = moneda normal (YellowCoin)
     *  K = moneda skin   (SkinCoin)
     *  E = enemigo horizontal básico (BasicBluePoint)
     *  V = enemigo vertical          (VerticalSlider)
     *  A = enemigo acelerado         (AcceleratedEnemy)
     *  B = bomba                     (Bomb)
     *  L = fuente de vida            (LifeSource)
     **/
    
    private GameConfig() {}
}