package Dominio;

import java.util.*;

/**
 * Estrategia experta para el jugador máquina.
 *
 * Usa BFS para encontrar el camino más corto hacia el objetivo.
 * Prioridad:
 *  1. Monedas no recogidas (priorizando las que el humano tiene más lejos)
 *  2. Zona SPAWN del Player 1 cuando todas las monedas están recogidas
 *
 * El camino BFS se recalcula solo cuando cambia el objetivo.
 * Cada dirección del path se mantiene hasta que la máquina
 * ha avanzado una celda completa en píxeles.
 */
public class ExpertMachineStrategy implements MachineStrategy {

    private static final Direction[] CARDINAL = {
        Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT
    };

    // ── Camino BFS ──
    private List<Direction> currentPath  = new ArrayList<>();
    private Position        lastTarget   = null;

    // ── Dirección actual y progreso en píxeles hacia la celda destino ──
    private Direction currentDir  = null;
    private float     movedPixels = 0f;

    /**
     * Decide la dirección a tomar para el jugador máquina.
     * @param machine el jugador máquina
     * @param game el juego actual
     * @return la dirección a tomar o null si no se mueve
     */
    @Override
    public Direction decideDirection(Player machine, Game game) {
        GameBoard board = game.getCurrentLevel().getBoard();
        int cell        = GameConfig.CELL_SIZE;

        Position machineCell = toCell(machine, cell);
        Position target      = findTarget(machine, game, board, cell);

        if (target == null) return null;

        // Recalcular BFS si cambió el objetivo
        if (!target.equals(lastTarget) || currentPath.isEmpty() && currentDir == null) {
            currentPath  = bfs(machineCell, target, board);
            lastTarget   = target;
            currentDir   = null;
            movedPixels  = 0f;
        }

        // Si terminamos de recorrer la celda actual, tomar la siguiente dirección
        if (currentDir == null || movedPixels >= cell) {
            if (currentPath.isEmpty()) return null;
            currentDir  = currentPath.remove(0);
            movedPixels = 0f;
        }

        // Acumular píxeles recorridos en esta dirección
        movedPixels += machine.getSpeed();

        return currentDir;
    }

    /**
     * Encuentra el objetivo para la máquina:
     * 1. Moneda no recogida más cercana a la máquina y más lejana al humano
     * 2. Zona SPAWN del Player 1 si no quedan monedas
     * @param machine jugador máquina
     * @param game juego actual
     * @param board tablero de juego
     * @param cell tamaño de celda en píxeles
     * @return la posición objetivo o null si no se encuentra ninguno
     */
    private Position findTarget(Player machine, Game game, GameBoard board, int cell) {
        List<Coin> coins     = game.getCurrentLevel().getCoins();
        Position machineCell = toCell(machine, cell);
        Player   human       = game.getPlayers().isEmpty() ? null : game.getPlayers().get(0);

        Position bestCoin  = null;
        double   bestScore = Double.MAX_VALUE;

        for (Coin coin : coins) {
            if (coin.isCollected()) continue;
            Position coinCell   = coin.getPosition();
            double distMachine  = manhattan(machineCell, coinCell);
            double distHuman    = (human != null)
                    ? manhattan(toCell(human, cell), coinCell)
                    : Double.MAX_VALUE;
            // Preferir monedas cerca de la máquina y lejos del humano
            double score = distMachine - 0.5 * distHuman;
            if (score < bestScore) {
                bestScore = score;
                bestCoin  = coinCell;
            }
        }

        if (bestCoin != null) return bestCoin;

        // Sin monedas → ir a la zona spawn del Player 1
        return findSpawnCell(board);
    }

    /**
     * Busca la posición de la zona SPAWN en el tablero.
     * @param board tablero de juego
     * @return la posición de la zona SPAWN o null si no se encuentra
     */
    private Position findSpawnCell(GameBoard board) {
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getColumns(); c++) {
                if (board.getCell(r, c).getType() == CellType.SPAWN_ZONE) {
                    return new Position(r, c);
                }
            }
        }
        return null;
    }

    /**
     * BFS para encontrar el camino más corto desde start hasta goal.
     * @param start posición inicial
     * @param goal posición objetivo
     * @param board tablero de juego
     * @return lista de direcciones para llegar al objetivo o vacía si no se encuentra camino
     */
    private List<Direction> bfs(Position start, Position goal, GameBoard board) {
        if (start.equals(goal)) return new ArrayList<>();

        Map<Position, Position>  parent      = new HashMap<>();
        Map<Position, Direction> directionTo = new HashMap<>();
        Queue<Position>          queue       = new LinkedList<>();

        parent.put(start, null);
        queue.add(start);

        while (!queue.isEmpty()) {      // BFS
            Position current = queue.poll();
            for (Direction d : CARDINAL) {
                Position next = step(current, d);
                if (!isWalkable(next, board) || parent.containsKey(next)) continue;
                parent.put(next, current);
                directionTo.put(next, d);
                queue.add(next);
                if (next.equals(goal)) {
                    return reconstructPath(start, next, parent, directionTo);
                }
            }
        }
        return new ArrayList<>();
    }

    /**
     * Reconstruye el camino de direcciones desde start hasta end usando los mapas de parent y directionTo.
     * @param start posición inicial 
     * @param end posición final
     * @param parent mapa de nodos padre para cada posición visitada
     * @param directionTo mapa de dirección tomada para llegar a cada posición visitada
     * @return lista de direcciones para llegar desde start hasta end
     */
    private List<Direction> reconstructPath(Position start, Position end,
            Map<Position, Position> parent, Map<Position, Direction> directionTo) {
        LinkedList<Direction> path = new LinkedList<>();
        Position current = end;
        while (!current.equals(start)) {
            path.addFirst(directionTo.get(current));
            current = parent.get(current);
        }
        return path;
    }

    /**
     * Calcula la posición resultante de dar un paso desde pos en la dirección d.
     * @param pos posición actual 
     * @param d dirección del paso
     * @return nueva posición después de dar el paso
     */
    private Position step(Position pos, Direction d) {
        int r = pos.getRow(), c = pos.getColumn();
        switch (d) {
            case UP:    return new Position(r - 1, c);
            case DOWN:  return new Position(r + 1, c);
            case LEFT:  return new Position(r, c - 1);
            case RIGHT: return new Position(r, c + 1);
            default:    return pos;
        }
    }

    /**
     * Verifica si la posición es transitable (dentro del tablero y sin paredes).
     * @param pos posición a verificar 
     * @param board tablero de juego 
     * @return true si la posición es transitable, false si es una pared o está fuera del tablero
     */
    private boolean isWalkable(Position pos, GameBoard board) {
        if (!board.isInside(pos)) return false;
        CellType t = board.getCell(pos.getRow(), pos.getColumn()).getType();
        return t != CellType.WALL && t != CellType.EMPTY;
    }

    /**
     * Convierte las coordenadas del jugador a una posición en la cuadrícula.
     * @param p jugador
     * @param cell tamaño de cada celda en la cuadrícula
     * @return posición en la cuadrícula
     */
    private Position toCell(Player p, int cell) {
        int col = (int)((p.getX() + p.getSize() / 2f) / cell);
        int row = (int)((p.getY() + p.getSize() / 2f) / cell);
        return new Position(row, col);
    }

    /**
     * Calcula la distancia de Manhattan entre dos posiciones.
     * @param a primera posición
     * @param b segunda posición
     * @return distancia de Manhattan
     */
    private double manhattan(Position a, Position b) {
        return Math.abs(a.getRow() - b.getRow()) + Math.abs(a.getColumn() - b.getColumn());
    }
}