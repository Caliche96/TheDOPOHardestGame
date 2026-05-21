package Dominio;

import java.io.*;
import java.util.*;

/**
 * Parsea un archivo .txt y construye un objeto Level completo
 * con su GameBoard, enemigos, monedas y elementos especiales.
 *
 * Leyenda:
 *   # = vacío / pared (no caminable)
 *   . = walkable (zona ajedrezada)
 *   S = spawn zone (verde izquierda)
 *   F = goal zone  (verde derecha)
 *   Z = zona segura intermedia
 *   C = moneda normal  (YellowCoin)
 *   K = moneda skin    (SkinCoin)
 *   E = enemigo horizontal (BasicBluePoint)
 *   V = enemigo vertical   (VerticalSlider)
 *   A = enemigo acelerado  (AcceleratedEnemy)
 *   B = bomba              (Bomb)
 *   L = fuente de vida     (LifeSource)
 */
public class LevelLoader {

    /**
     * Carga un nivel desde un archivo .txt.
     * @param filePath  ruta al archivo (ej. "recursos/nivel1.txt")
     * @param levelName nombre del nivel
     * @return Level construido, o null si el archivo no existe
     */
    public static Level load(String filePath, String levelName) {
        List<String> lines = readLines(filePath);
        if (lines == null || lines.isEmpty()) {
            return null;
        }

        int rows    = lines.size();
        int columns = lines.get(0).length();

        GameBoard board = new GameBoard(rows, columns);
        Level level = new Level(levelName, board, GameConfig.DEFAULT_TIME_LIMIT);

        // Posición de spawn por defecto (primera S encontrada)
        Position defaultSpawn = new Position(0, 0);
        boolean spawnFound = false;

        for (int row = 0; row < rows; row++) {
            String line = lines.get(row);
            for (int col = 0; col < Math.min(columns, line.length()); col++) {
                char ch = line.charAt(col);
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
                            spawnFound = true;
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
                        level.addEnemy(new BasicBluePoint(pos));
                        break;
                    case 'V':
                        board.setCell(row, col, CellType.WALKABLE);
                        level.addEnemy(new VerticalSlider(pos));
                        break;
                    case 'A':
                        board.setCell(row, col, CellType.WALKABLE);
                        level.addEnemy(new AcceleratedEnemy(pos));
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

        // Asignar el tablero a cada enemigo para que su patrón de movimiento
        // pueda verificar colisiones con celdas (rebote en paredes, etc.)
        for (Enemy enemy : level.getEnemies()) {
            enemy.setBoard(board);
        }

        return level;
    }

    // ── Utilidad ──

    private static List<String> readLines(String filePath) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("LevelLoader: no se pudo leer " + filePath + " — " + e.getMessage());
            return null;
        }
        return lines;
    }
}