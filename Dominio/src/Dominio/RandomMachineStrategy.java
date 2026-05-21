package Dominio;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Estrategia aleatoria para el jugador máquina.
 *
 * En cada tick elige una dirección al azar entre las que
 * no choquen con una pared. Si llega al goal sin haber
 * recogido todas las monedas, sigue moviéndose aleatoriamente
 * (la regla de negocio impide que el nivel termine).
 */
public class RandomMachineStrategy implements MachineStrategy {

    private static final Direction[] ALL_DIRECTIONS = {
        Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT,
        Direction.UP_LEFT, Direction.UP_RIGHT, Direction.DOWN_LEFT, Direction.DOWN_RIGHT
    };

    private final Random random = new Random();

    /** Cada cuántos ticks cambia de dirección. */
    private static final int CHANGE_INTERVAL = 8;
    private int tickCount = 0;
    private Direction currentDirection = Direction.RIGHT;

    @Override
    public Direction decideDirection(Player machine, Game game) {
        tickCount++;

        // Cambiar dirección cada CHANGE_INTERVAL ticks o si choca
        if (tickCount >= CHANGE_INTERVAL) {
            tickCount = 0;
            currentDirection = pickRandomValidDirection(machine, game);
        }

        return currentDirection;
    }

    /**
     * Elige una dirección aleatoria entre las que no producen colisión inmediata.
     * Si todas chocan, devuelve una al azar de todas formas.
     */
    private Direction pickRandomValidDirection(Player machine, Game game) {
        GameBoard board = game.getCurrentLevel().getBoard();
        int cell = GameConfig.CELL_SIZE;

        List<Direction> valid = new ArrayList<>();
        for (Direction d : ALL_DIRECTIONS) {
            float[] next = simulateMove(machine, d);
            if (!wouldCollide(next[0], next[1], machine.getSize(), board, cell)) {
                valid.add(d);
            }
        }

        if (valid.isEmpty()) return ALL_DIRECTIONS[random.nextInt(ALL_DIRECTIONS.length)];
        return valid.get(random.nextInt(valid.size()));
    }

    private float[] simulateMove(Player p, Direction d) {
        float speed = p.getSpeed() * 0.707f; // normalizado
        float dx = 0, dy = 0;
        switch (d) {
            case UP:         dy = -p.getSpeed(); break;
            case DOWN:       dy =  p.getSpeed(); break;
            case LEFT:       dx = -p.getSpeed(); break;
            case RIGHT:      dx =  p.getSpeed(); break;
            case UP_LEFT:    dx = -speed; dy = -speed; break;
            case UP_RIGHT:   dx =  speed; dy = -speed; break;
            case DOWN_LEFT:  dx = -speed; dy =  speed; break;
            case DOWN_RIGHT: dx =  speed; dy =  speed; break;
        }
        return new float[]{ p.getX() + dx, p.getY() + dy };
    }

    private boolean wouldCollide(float x, float y, float size, GameBoard board, int cell) {
        if (x < 0 || y < 0) return true;
        float[] cx = { x, x + size - 1, x,          x + size - 1 };
        float[] cy = { y, y,             y + size - 1, y + size - 1 };
        for (int i = 0; i < 4; i++) {
            int col = (int)(cx[i] / cell);
            int row = (int)(cy[i] / cell);
            if (row < 0 || row >= board.getRows() || col < 0 || col >= board.getColumns()) return true;
            CellType t = board.getCell(row, col).getType();
            if (t == CellType.WALL || t == CellType.EMPTY) return true;
        }
        return false;
    }
}