package Dominio;

/**
 * Clase que centraliza todas las excepciones del proyecto.
 * Cada constante representa un mensaje de error específico
 * que puede ocurrir en el dominio del juego.
 */
public class GameException extends Exception {

    // ── Carga de niveles ──
    public static String LEVEL_NOT_FOUND = "No se puede cargar el nivel: archivo no encontrado";
    public static String LEVEL_EMPTY = "No se puede cargar el nivel: el archivo está vacío";
    public static String LEVEL_READ_ERROR = "No se puede cargar el nivel: error de lectura";
    public static String LEVEL_NO_SPAWN = "Formato de nivel inválido: no se encontró zona de spawn (S)";
    public static String LEVEL_NO_GOAL = "Formato de nivel inválido: no se encontró zona de meta (F)";
    public static String LEVEL_NO_PERMISSION = "No se puede cargar el nivel: sin permisos de lectura";

    // ── Persistencia ──
    public static String SAVE_ERROR = "No se puede guardar la partida: error de escritura";
    public static String LOAD_NOT_FOUND = "No se puede cargar la partida: archivo no encontrado";
    public static String LOAD_CORRUPT = "No se puede cargar la partida: archivo corrupto o incompleto";
    public static String LOAD_INCOMPATIBLE = "No se puede cargar la partida: versión incompatible";
    public static String LOAD_UNKNOWN_FORMAT = "No se puede cargar la partida: formato no reconocido";

    // ── Restauración de partida ──
    public static String SAVE_DATA_EMPTY = "Datos de partida inválidos: el archivo de guardado está vacío";
    public static String SAVE_DATA_NO_PLAYER = "Datos de partida inválidos: no hay datos de jugadores";

    // ── Recursos ──
    public static String RESOURCE_NOT_FOUND = "Recurso no encontrado";

    // ── Movimiento y estado ──
    public static String INVALID_PLAYER_INDEX = "Movimiento inválido: índice de jugador fuera de rango";
    public static String INVALID_DIRECTION = "Movimiento inválido: dirección nula";

    public GameException(String message) {
        super(message);
    }
}