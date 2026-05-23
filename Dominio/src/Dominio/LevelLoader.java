package Dominio;

import java.io.*;
import java.util.*;

public class LevelLoader {

    /**
     * Carga un nivel desde un archivo .txt.
     * @param filePath  ruta al archivo (ej. "recursos/nivel1.txt")
     * @param levelName nombre descriptivo del nivel
     * @return Level construido y validado
     * @throws GameException si el archivo no existe, está vacío,
     *                       no tiene permisos, o el mapa es inválido
     */
    public static Level load(String filePath, String levelName) throws GameException {
        // ── 1. Validar que el archivo existe y tiene permisos ──
        File file = new File(filePath);
        if (!file.exists()) {
            throw new GameException(GameException.LEVEL_NOT_FOUND + ": " + filePath);
        }
        if (!file.canRead()) {
            throw new GameException(GameException.LEVEL_NO_PERMISSION + ": " + filePath);
        }

        // ── 2. Leer líneas ──
        List<String> lines = readLines(filePath);  // lanza GameException si falla

        if (lines.isEmpty()) {
            throw new GameException(GameException.LEVEL_EMPTY + ": " + filePath);
        }

        // ── 3. Validar que el mapa tiene S y F ──
        boolean hasSpawn = false;
        boolean hasGoal  = false;
        for (String line : lines) {
            if (line.contains("S")) hasSpawn = true;
            if (line.contains("F")) hasGoal  = true;
        }
        if (!hasSpawn) {
            throw new GameException(GameException.LEVEL_NO_SPAWN + ": " + filePath);
        }
        if (!hasGoal) {
            throw new GameException(GameException.LEVEL_NO_GOAL + ": " + filePath);
        }

        // ── 4. Construir el tablero ──
        int rows    = lines.size();
        int columns = lines.get(0).length();
        int cell    = GameConfig.CELL_SIZE;

        GameBoard board = new GameBoard(rows, columns);
        Level level     = new Level(levelName, board, GameConfig.DEFAULT_TIME_LIMIT);

        Position defaultSpawn = new Position(0, 0);
        boolean  spawnFound   = false;

        for (int row = 0; row < rows; row++) {
            String line = lines.get(row);
            for (int col = 0; col < Math.min(columns, line.length()); col++) {
                char ch  = line.charAt(col);
                Position pos = new Position(row, col);

                switch (ch) {
                    case '#':
                        board.setCell(row, col, CellType.WALL);
                        break;
                    case '.':
                        board.setCell(row, col, CellType.WALKABLE);
                        break;
                    case 'S':
                        board.setCell(row, col, CellType.SPAWN_ZONE);
                        if (!spawnFound) {
                            defaultSpawn = pos;
                            spawnFound   = true;
                        }
                        break;
                    case 'F':
                        board.setCell(row, col, CellType.GOAL);
                        break;
                    case 'Z':
                        board.setCell(row, col, CellType.SAFE_ZONE);
                        break;
                    case 'C':
                        board.setCell(row, col, CellType.WALKABLE);
                        level.addCoin(new YellowCoin(pos));
                        break;
                    case 'K':
                        board.setCell(row, col, CellType.WALKABLE);
                        level.addCoin(new SkinCoin(pos));
                        break;
                    case 'E':
                        board.setCell(row, col, CellType.WALKABLE);
                        level.addEnemy(new BasicBluePoint(col * cell, row * cell));
                        break;
                    case 'V':
                        board.setCell(row, col, CellType.WALKABLE);
                        level.addEnemy(new VerticalSlider(col * cell, row * cell));
                        break;
                    case 'A':
                        board.setCell(row, col, CellType.WALKABLE);
                        level.addEnemy(new AcceleratedEnemy(col * cell, row * cell));
                        break;
                    case 'P':
                        board.setCell(row, col, CellType.WALKABLE);
                        level.addEnemy(new PatrolBluePoint(col * cell, row * cell));
                        break;
                    case 'B':
                        board.setCell(row, col, CellType.WALKABLE);
                        level.addSpecialElement(new Bomb(pos));
                        break;
                    case 'L':
                        board.setCell(row, col, CellType.WALKABLE);
                        level.addSpecialElement(new LifeSource(pos));
                        break;
                    default:
                        board.setCell(row, col, CellType.EMPTY);
                        break;
                }
            }
        }

        level.setDefaultSpawn(defaultSpawn);

        for (Enemy enemy : level.getEnemies()) {
            enemy.setBoard(board);
        }

        return level;
    }

    // ── Utilidad ──────────────────────────────────────────────

    /**
     * Lee las líneas no vacías de un archivo.
     * @throws GameException si ocurre un error de I/O
     */
    private static List<String> readLines(String filePath) throws GameException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            throw new GameException(GameException.LEVEL_READ_ERROR + ": " + e.getMessage());
        }
        return lines;
    }
}