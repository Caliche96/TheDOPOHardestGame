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

    // ── Tiempo por defecto (segundos × 30 FPS) ──
    public static final int DEFAULT_TIME_LIMIT = 30; // 30 monedas como en la imagen

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
     *
     * Nivel 1 — replica la imagen del juego original:
     *   Columnas 0-2   : zona spawn (S) verde izquierda
     *   Columnas 3-16  : zona walkable ajedrezada con 3 enemigos horizontales
     *   Columnas 17-19 : zona goal (F) verde derecha
     *   Filas 0,1,8,9  : vacío/pared arriba y abajo de la zona central
     */
    public static final String[] LEVEL_1 = {
        "#####################",
        "#####################",
        "SSS..................FFF",
        "SSS..................FFF",
        "SSS...E.......E...E..FFF",
        "SSS..................FFF",
        "SSS..................FFF",
        "SSS..................FFF",
        "#####################",
        "#####################"
    };

    // Mapa corregido con exactamente 20 columnas × 10 filas
    public static final String[] NIVEL_1 = {
        "####################",
        "####################",
        "SSS.................F",
        "SSS.................F",
        "SSS..E.....E....E...F",
        "SSS.................F",
        "SSS.................F",
        "SSS.................F",
        "####################",
        "####################"
    };

    private GameConfig() {}
}