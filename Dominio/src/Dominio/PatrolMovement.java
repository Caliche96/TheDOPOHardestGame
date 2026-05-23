package Dominio;

/**
 * Patrón de movimiento en patrulla: el enemigo recorre en sentido horario
 * las 4 esquinas de un cuadrado de 3×3 celdas centrado en su posición inicial.
 *
 * Las esquinas (en píxeles) se calculan una sola vez al primer uso:
 *
 *   [0] top-left  → [1] top-right
 *                         ↓
 *   [3] bot-left  ← [2] bot-right
 *
 * El enemigo avanza hacia el siguiente waypoint a velocidad constante.
 * Cuando llega (dentro de un margen de 1 px), pasa al siguiente.
 */
public class PatrolMovement implements MovementPattern {

    /** Índice del waypoint destino actual (0-3). */
    private int    targetIndex;

    /** Coordenadas pixel de las 4 esquinas del cuadrado de patrulla. */
    private float[] waypointX;
    private float[] waypointY;

    /** true cuando los waypoints ya fueron inicializados. */
    private boolean initialized;

    /**
     * Constructor de la clase PatrolMovement.
     */
    public PatrolMovement() {
        targetIndex = 1;          // arrancar yendo hacia la esquina top-right
        initialized = false;
    }

    /**
     * Mueve el enemigo según el patrón de movimiento en patrulla.
     * @param enemy El enemigo a mover.
     * @param board El tablero del juego.
     */
    @Override
    public void move(Enemy enemy, GameBoard board) {
        if (!initialized) {
            initWaypoints(enemy);
        }

        float tx = waypointX[targetIndex];
        float ty = waypointY[targetIndex];

        float dx = tx - enemy.getX();
        float dy = ty - enemy.getY();
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist <= enemy.getSpeed()) {
            // Llega exactamente al waypoint y avanza al siguiente
            enemy.setX(tx);
            enemy.setY(ty);
            targetIndex = (targetIndex + 1) % 4;
        } else {
            // Avanza un paso en la dirección del waypoint
            enemy.setX(enemy.getX() + enemy.getSpeed() * (dx / dist));
            enemy.setY(enemy.getY() + enemy.getSpeed() * (dy / dist));
        }
    }

    /**
     * Calcula las 4 esquinas del cuadrado 3×3 centrado en la posición
     * inicial del enemigo.  Se llama sólo la primera vez que move() corre,
     * garantizando que usamos las coordenadas reales de spawn.
     *
     * Esquina 0 = top-left  (col-1, row-1)
     * Esquina 1 = top-right (col+1, row-1)
     * Esquina 2 = bot-right (col+1, row+1)
     * Esquina 3 = bot-left  (col-1, row+1)
     */
    private void initWaypoints(Enemy enemy) {
        int cell = GameConfig.CELL_SIZE;

        // Celda central basada en la posición de spawn
        int centerCol = (int)(enemy.getX() / cell);
        int centerRow = (int)(enemy.getY() / cell);

        // Coordenada pixel de la esquina superior-izquierda de cada celda
        float left  = (centerCol - 1) * cell;
        float right = (centerCol + 1) * cell;
        float top   = (centerRow - 1) * cell;
        float bot   = (centerRow + 1) * cell;

        waypointX = new float[]{ left,  right, right, left  };
        waypointY = new float[]{ top,   top,   bot,   bot   };

        // El punto de partida es el waypoint 0 (top-left); empezamos yendo al 1
        targetIndex = 1;
        initialized  = true;
    }

    /** Permite reiniciar la patrulla (útil al recargar nivel). */
    public void reset() {
        initialized = false;
        targetIndex = 1;
    }
}