package Dominio;

public class GameConfig {
	public static final int DEFAULT_ROWS=15;
	public static final int DEFAULT_COLUMNS=15;
	public static final int DEFAULT_TIME_LIMIT=	180;
	
	/**
	 * 
	 * Como irá configurado que nivel y que se necesita 
	 * # = pared
     * . = vacío
     * S = zona inicial / spawn
     * F = zona final
     * Z = zona segura intermedia
     * C = moneda normal
     * K = moneda skin
     * E = enemigo básico
     * V = enemigo vertical
     * A = enemigo acelerado
     * B = bomba
     * L = fuente de vida
	 */
	
    public static final String[] LEVEL_1 = {
            "####################",
            "#S....C.......E...F#",
            "#..................#",
            "#....#####.........#",
            "#..................#",
            "#.......Z..........#",
            "#..................#",
            "#..V.........K.....#",
            "#..................#",
            "#.........#####....#",
            "#..................#",
            "#.....A............#",
            "#............B.....#",
            "#..........L.......#",
            "####################"
    };

    private GameConfig() {
        // Evita crear objetos de esta clase
    }
}
