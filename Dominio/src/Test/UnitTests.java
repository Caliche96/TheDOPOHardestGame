package Test;

import Dominio.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas Unitarias — cobertura > 90% del dominio.
 * Cada clase se prueba de forma aislada.
 */
@DisplayName("Pruebas Unitarias")
public class UnitTests {

    // ══════════════════════════════════════════════
    //  UTILIDADES
    // ══════════════════════════════════════════════

    /** Tablero 10×20: fila 0–1 y 8–9 EMPTY, filas 2–7 con S/Walkable/F */
    private GameBoard buildTestBoard() {
        GameBoard board = new GameBoard(10, 20);
        for (int r = 2; r <= 7; r++) {
            for (int c = 0; c <= 2;  c++) board.setCell(r, c, CellType.SPAWN_ZONE);
            for (int c = 3; c <= 16; c++) board.setCell(r, c, CellType.WALKABLE);
            for (int c = 17; c <= 19;c++) board.setCell(r, c, CellType.GOAL);
        }
        return board;
    }

    private Level buildTestLevel() {
        GameBoard board = buildTestBoard();
        Level level = new Level("Test", board, 2700);
        level.setDefaultSpawn(new Position(4, 0));
        return level;
    }

    private Game buildGame() {
        return new Game(buildTestLevel(), GameMode.SINGLE_PLAYER);
    }

    // ══════════════════════════════════════════════
    //  POSITION
    // ══════════════════════════════════════════════

    @Nested @DisplayName("Position")
    class PositionTests {

        @Test @DisplayName("Mismos valores → iguales")
        void equals() { assertEquals(new Position(3, 4), new Position(3, 4)); }

        @Test @DisplayName("Valores distintos → no iguales")
        void notEquals() { assertNotEquals(new Position(3, 4), new Position(3, 5)); }

        @Test @DisplayName("getRow y getColumn correctos")
        void getters() {
            Position p = new Position(2, 7);
            assertEquals(2, p.getRow());
            assertEquals(7, p.getColumn());
        }

        @Test @DisplayName("hashCode consistente con equals — crítico para BFS")
        void hashCodeConsistency() {
            Position a = new Position(5, 10);
            Position b = new Position(5, 10);
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode(),
                    "hashCode debe ser igual para objetos iguales — sin esto el BFS explota");
        }

        @Test @DisplayName("Posiciones distintas generan hashCode distintos")
        void hashCodeDistinct() {
            assertNotEquals(new Position(1, 2).hashCode(), new Position(2, 1).hashCode());
        }

        @Test @DisplayName("equals con null retorna false")
        void equalsNull() { assertNotEquals(null, new Position(1, 1)); }

        @Test @DisplayName("equals con otro tipo retorna false")
        void equalsOtherType() { assertNotEquals("(1,1)", new Position(1, 1)); }
    }

    // ══════════════════════════════════════════════
    //  CELL
    // ══════════════════════════════════════════════

    @Nested @DisplayName("Cell")
    class CellTests {

        @Test @DisplayName("WALL no es caminable")
        void wallNotWalkable() {
            Cell c = new Cell(new Position(0,0), CellType.WALL);
            assertFalse(c.isWalkable());
            assertTrue(c.isWall());
        }

        @Test @DisplayName("EMPTY no es caminable")
        void emptyNotWalkable() {
            Cell c = new Cell(new Position(0,0), CellType.EMPTY);
            assertFalse(c.isWalkable(),
                    "EMPTY debe ser NO caminable (fondo azul, no es parte del mapa)");
        }

        @Test @DisplayName("WALKABLE es caminable")
        void walkable() {
            Cell c = new Cell(new Position(0,0), CellType.WALKABLE);
            assertTrue(c.isWalkable());
            assertFalse(c.isWall());
        }

        @Test @DisplayName("SPAWN_ZONE es caminable y es spawnZone")
        void spawnZone() {
            Cell c = new Cell(new Position(0,0), CellType.SPAWN_ZONE);
            assertTrue(c.isWalkable());
            assertTrue(c.isSpawnZone());
        }

        @Test @DisplayName("GOAL es caminable y es goal")
        void goal() {
            Cell c = new Cell(new Position(0,0), CellType.GOAL);
            assertTrue(c.isWalkable());
            assertTrue(c.isGoal());
        }

        @Test @DisplayName("SAFE_ZONE es caminable y es safeZone")
        void safeZone() {
            Cell c = new Cell(new Position(0,0), CellType.SAFE_ZONE);
            assertTrue(c.isWalkable());
            assertTrue(c.isSafeZone());
        }

        @Test @DisplayName("setType cambia el tipo")
        void setType() {
            Cell c = new Cell(new Position(0,0), CellType.EMPTY);
            c.setType(CellType.WALKABLE);
            assertEquals(CellType.WALKABLE, c.getType());
        }
    }

    // ══════════════════════════════════════════════
    //  GAMEBOARD
    // ══════════════════════════════════════════════

    @Nested @DisplayName("GameBoard")
    class GameBoardTests {

        @Test @DisplayName("Dimensiones correctas")
        void dimensions() {
            GameBoard b = new GameBoard(10, 20);
            assertEquals(10, b.getRows());
            assertEquals(20, b.getColumns());
        }

        @Test @DisplayName("Celdas iniciales son EMPTY")
        void initialEmpty() {
            GameBoard b = new GameBoard(3, 3);
            for (int r = 0; r < 3; r++)
                for (int c = 0; c < 3; c++)
                    assertEquals(CellType.EMPTY, b.getCell(r, c).getType());
        }

        @Test @DisplayName("setCell cambia el tipo correctamente")
        void setCell() {
            GameBoard b = new GameBoard(5, 5);
            b.setCell(2, 3, CellType.WALKABLE);
            assertEquals(CellType.WALKABLE, b.getCell(2, 3).getType());
        }

        @Test @DisplayName("isInside válido")
        void insideValid() {
            GameBoard b = new GameBoard(10, 20);
            assertTrue(b.isInside(new Position(0, 0)));
            assertTrue(b.isInside(new Position(9, 19)));
        }

        @Test @DisplayName("isInside inválido")
        void insideInvalid() {
            GameBoard b = new GameBoard(10, 20);
            assertFalse(b.isInside(new Position(-1, 0)));
            assertFalse(b.isInside(new Position(10, 0)));
            assertFalse(b.isInside(new Position(0, 20)));
        }

        @Test @DisplayName("isWall correcto")
        void isWall() {
            GameBoard b = new GameBoard(5, 5);
            b.setCell(1, 1, CellType.WALL);
            assertTrue(b.isWall(new Position(1, 1)));
            assertTrue(b.isWall(new Position(-1, 0)));  // fuera → wall
            assertFalse(b.isWall(new Position(0, 0)));
        }

        @Test @DisplayName("getCells devuelve el grid completo")
        void getCells() {
            GameBoard b = new GameBoard(3, 4);
            Cell[][] cells = b.getCells();
            assertEquals(3, cells.length);
            assertEquals(4, cells[0].length);
        }
    }

    // ══════════════════════════════════════════════
    //  GAMECONFIG
    // ══════════════════════════════════════════════

    @Nested @DisplayName("GameConfig")
    class GameConfigTests {
        @Test void cellSize()     { assertEquals(26,   GameConfig.CELL_SIZE);           }
        @Test void fps()          { assertEquals(30,   GameConfig.FPS);                 }
        @Test void timeLimit()    { assertEquals(2700, GameConfig.DEFAULT_TIME_LIMIT);  }
        @Test void playerInt()    { assertEquals(3,    GameConfig.PLAYER_MOVE_INTERVAL);}
        @Test void enemyInt()     { assertEquals(4,    GameConfig.ENEMY_MOVE_INTERVAL); }
    }

    // ══════════════════════════════════════════════
    //  GAMEMODE
    // ══════════════════════════════════════════════

    @Nested @DisplayName("GameMode")
    class GameModeTests {

        @Test @DisplayName("SINGLE_PLAYER")
        void single() {
            assertFalse(GameMode.SINGLE_PLAYER.isMultiplayer());
            assertTrue(GameMode.SINGLE_PLAYER.isSinglePlayer());
            assertFalse(GameMode.SINGLE_PLAYER.hasMachine());
            assertEquals(1, GameMode.SINGLE_PLAYER.getPlayers());
        }

        @Test @DisplayName("PLAYER_VS_PLAYER")
        void pvp() {
            assertTrue(GameMode.PLAYER_VS_PLAYER.isMultiplayer());
            assertFalse(GameMode.PLAYER_VS_PLAYER.hasMachine());
            assertEquals(2, GameMode.PLAYER_VS_PLAYER.getPlayers());
        }

        @Test @DisplayName("PLAYER_VS_MACHINE")
        void pvm() {
            assertTrue(GameMode.PLAYER_VS_MACHINE.isMultiplayer());
            assertTrue(GameMode.PLAYER_VS_MACHINE.hasMachine());
        }
    }

    // ══════════════════════════════════════════════
    //  GAMESTATE
    // ══════════════════════════════════════════════

    @Nested @DisplayName("GameState — patrón State")
    class GameStateTests {

        @Test @DisplayName("RunningState")
        void running() {
            GameState s = new RunningState();
            assertTrue(s.isRunning()); assertFalse(s.isPaused());
            assertFalse(s.isGameOver()); assertFalse(s.isWin());
        }

        @Test @DisplayName("PausedState")
        void paused() {
            GameState s = new PausedState();
            assertTrue(s.isPaused()); assertFalse(s.isRunning());
            assertFalse(s.isGameOver()); assertFalse(s.isWin());
        }

        @Test @DisplayName("GameOverState")
        void gameOver() {
            GameState s = new GameOverState();
            assertTrue(s.isGameOver()); assertFalse(s.isRunning());
            assertFalse(s.isPaused()); assertFalse(s.isWin());
        }

        @Test @DisplayName("WinState")
        void win() {
            GameState s = new WinState();
            assertTrue(s.isWin()); assertFalse(s.isRunning());
            assertFalse(s.isPaused()); assertFalse(s.isGameOver());
        }
    }

    // ══════════════════════════════════════════════
    //  GAMEEXCEPTION
    // ══════════════════════════════════════════════

    @Nested @DisplayName("GameException")
    class GameExceptionTests {

        @Test @DisplayName("Constructor conserva el mensaje")
        void message() {
            GameException ex = new GameException("error de prueba");
            assertEquals("error de prueba", ex.getMessage());
        }

        @Test @DisplayName("Constantes de nivel no son null")
        void levelConstants() {
            assertNotNull(GameException.LEVEL_NOT_FOUND);
            assertNotNull(GameException.LEVEL_EMPTY);
            assertNotNull(GameException.LEVEL_NO_SPAWN);
            assertNotNull(GameException.LEVEL_NO_GOAL);
            assertNotNull(GameException.LEVEL_NO_PERMISSION);
            assertNotNull(GameException.LEVEL_READ_ERROR);
        }

        @Test @DisplayName("Constantes de persistencia no son null")
        void persistenceConstants() {
            assertNotNull(GameException.SAVE_ERROR);
            assertNotNull(GameException.LOAD_NOT_FOUND);
            assertNotNull(GameException.LOAD_CORRUPT);
            assertNotNull(GameException.LOAD_INCOMPATIBLE);
            assertNotNull(GameException.LOAD_UNKNOWN_FORMAT);
        }

        @Test @DisplayName("Constantes de restauración no son null")
        void restoreConstants() {
            assertNotNull(GameException.SAVE_DATA_EMPTY);
            assertNotNull(GameException.SAVE_DATA_NO_PLAYER);
        }
    }

    // ══════════════════════════════════════════════
    //  PLAYER — RedPlayer
    // ══════════════════════════════════════════════

    @Nested @DisplayName("RedPlayer")
    class RedPlayerTests {

        @Test @DisplayName("Inicialización correcta")
        void init() {
            RedPlayer p = new RedPlayer("P1", 52f, 55f);
            assertEquals("P1", p.getName());
            assertEquals(52f, p.getX(), 0.01f);
            assertEquals(55f, p.getY(), 0.01f);
            assertEquals(2.0f, p.getSpeed(), 0.01f);
            assertEquals(GameConfig.CELL_SIZE - 6f, p.getSize(), 0.01f);
            assertEquals(0, p.getDeaths());
            assertEquals(0, p.getCollectedCoins());
            assertTrue(p.isAlive());
            assertEquals("Red", p.getPlayerType());
        }

        @Test @DisplayName("receiveHit → muere en el primer golpe")
        void firstHitDies() {
            RedPlayer p = new RedPlayer("P1", 50f, 50f);
            p.receiveHit();
            assertEquals(1, p.getDeaths());
        }

        @Test @DisplayName("die() incrementa muertes y hace respawn")
        void dieRespawn() {
            RedPlayer p = new RedPlayer("P1", 50f, 50f);
            p.setX(300f); p.setY(300f);
            p.die();
            assertEquals(1, p.getDeaths());
            assertEquals(50f, p.getX(), 0.01f);
            assertEquals(50f, p.getY(), 0.01f);
        }

        @Test @DisplayName("setSpawnPoint actualiza punto de reaparición")
        void spawnPoint() {
            RedPlayer p = new RedPlayer("P1", 50f, 50f);
            p.setSpawnPoint(100f, 150f);
            p.die();
            assertEquals(100f, p.getX(), 0.01f);
            assertEquals(150f, p.getY(), 0.01f);
        }

        @Test @DisplayName("addCoin y resetCoins")
        void coins() {
            RedPlayer p = new RedPlayer("P1", 0f, 0f);
            p.addCoin(); p.addCoin();
            assertEquals(2, p.getCollectedCoins());
            p.resetCoins();
            assertEquals(0, p.getCollectedCoins());
        }

        @Test @DisplayName("Movimiento derecha incrementa X")
        void moveRight() {
            GameBoard b = buildTestBoard();
            int cell = GameConfig.CELL_SIZE;
            RedPlayer p = new RedPlayer("P1", 3 * cell + 3f, 4 * cell + 3f);
            float x0 = p.getX();
            p.move(Direction.RIGHT, b, cell);
            assertTrue(p.getX() > x0);
        }

        @Test @DisplayName("Movimiento izquierda decrementa X")
        void moveLeft() {
            GameBoard b = buildTestBoard();
            int cell = GameConfig.CELL_SIZE;
            RedPlayer p = new RedPlayer("P1", 5 * cell + 3f, 4 * cell + 3f);
            float x0 = p.getX();
            p.move(Direction.LEFT, b, cell);
            assertTrue(p.getX() < x0);
        }

        @Test @DisplayName("Movimiento arriba decrementa Y")
        void moveUp() {
            GameBoard b = buildTestBoard();
            int cell = GameConfig.CELL_SIZE;
            RedPlayer p = new RedPlayer("P1", 5 * cell + 3f, 5 * cell + 3f);
            float y0 = p.getY();
            p.move(Direction.UP, b, cell);
            assertTrue(p.getY() < y0);
        }

        @Test @DisplayName("Movimiento abajo incrementa Y")
        void moveDown() {
            GameBoard b = buildTestBoard();
            int cell = GameConfig.CELL_SIZE;
            RedPlayer p = new RedPlayer("P1", 5 * cell + 3f, 4 * cell + 3f);
            float y0 = p.getY();
            p.move(Direction.DOWN, b, cell);
            assertTrue(p.getY() > y0);
        }

        @Test @DisplayName("Diagonal DOWN_RIGHT mueve en ambos ejes")
        void moveDiagonal() {
            GameBoard b = buildTestBoard();
            int cell = GameConfig.CELL_SIZE;
            RedPlayer p = new RedPlayer("P1", 4 * cell + 3f, 4 * cell + 3f);
            float x0 = p.getX(), y0 = p.getY();
            p.move(Direction.DOWN_RIGHT, b, cell);
            assertTrue(p.getX() > x0);
            assertTrue(p.getY() > y0);
        }

        @Test @DisplayName("No atraviesa paredes")
        void wallBlocks() {
            GameBoard b = new GameBoard(5, 5);
            b.setCell(2, 2, CellType.WALKABLE);
            b.setCell(2, 3, CellType.WALL);
            int cell = GameConfig.CELL_SIZE;
            RedPlayer p = new RedPlayer("P1", 2 * cell + 3f, 2 * cell + 3f);
            for (int i = 0; i < 30; i++) p.move(Direction.RIGHT, b, cell);
            assertTrue(p.getX() < 3 * cell);
        }

        @Test @DisplayName("No sale por el borde izquierdo")
        void leftBorder() {
            GameBoard b = buildTestBoard();
            int cell = GameConfig.CELL_SIZE;
            RedPlayer p = new RedPlayer("P1", 3f, cell * 4 + 3f);
            for (int i = 0; i < 20; i++) p.move(Direction.LEFT, b, cell);
            assertTrue(p.getX() >= 0);
        }

        @Test @DisplayName("Colisión AABB: misma posición → colisionan")
        void collidesTrue() {
            RedPlayer p1 = new RedPlayer("P1", 50f, 50f);
            RedPlayer p2 = new RedPlayer("P2", 50f, 50f);
            assertTrue(p1.collides(p2));
        }

        @Test @DisplayName("Colisión AABB: lejos → no colisionan")
        void collidesFalse() {
            RedPlayer p1 = new RedPlayer("P1", 50f, 50f);
            RedPlayer p2 = new RedPlayer("P2", 300f, 300f);
            assertFalse(p1.collides(p2));
        }

        @Test @DisplayName("isInvincible es false por defecto")
        void notInvincibleByDefault() {
            assertFalse(new RedPlayer("P1", 0f, 0f).isInvincible());
        }

        @Test @DisplayName("isShieldActive es false por defecto")
        void noShieldByDefault() {
            assertFalse(new RedPlayer("P1", 0f, 0f).isShieldActive());
        }

        @Test @DisplayName("activateShield activa el escudo")
        void activateShield() {
            RedPlayer p = new RedPlayer("P1", 0f, 0f);
            p.activateShield();
            assertTrue(p.isShieldActive());
        }

        @Test @DisplayName("tick() sin invencibilidad no cambia estado")
        void tickWithoutTimer() {
            RedPlayer p = new RedPlayer("P1", 0f, 0f);
            p.tick();
            assertFalse(p.isInvincible());
        }

        @Test @DisplayName("setBorderColor y getBorderColor")
        void borderColor() {
            RedPlayer p = new RedPlayer("P1", 0f, 0f);
            java.awt.Color gold = new java.awt.Color(255, 215, 0);
            p.setBorderColor(gold);
            assertEquals(gold, p.getBorderColor());
        }
    }

    // ══════════════════════════════════════════════
    //  PLAYER — BluePlayer
    // ══════════════════════════════════════════════

    @Nested @DisplayName("BluePlayer")
    class BluePlayerTests {

        @Test @DisplayName("Velocidad 7.0 y tamaño CELL-4")
        void init() {
            BluePlayer p = new BluePlayer("P1", 0f, 0f);
            assertEquals("Blue", p.getPlayerType());
            assertEquals(7.0f, p.getSpeed(), 0.1f);
            assertEquals(GameConfig.CELL_SIZE - 4f, p.getSize(), 0.1f);
        }

        @Test @DisplayName("receiveHit → muere en el primer golpe")
        void firstHitDies() {
            BluePlayer p = new BluePlayer("P1", 50f, 50f);
            p.receiveHit();
            assertEquals(1, p.getDeaths());
        }
    }

    // ══════════════════════════════════════════════
    //  PLAYER — GreenPlayer (escudo e invencibilidad)
    // ══════════════════════════════════════════════

    @Nested @DisplayName("GreenPlayer")
    class GreenPlayerTests {

        @Test @DisplayName("Nace con escudo activo")
        void initShieldActive() {
            GreenPlayer p = new GreenPlayer("P1", 50f, 50f);
            assertEquals("Green", p.getPlayerType());
            assertEquals(0, p.getDeaths());
            assertTrue(p.isShieldActive());
        }

        @Test @DisplayName("Primer golpe no mata — escudo absorbe")
        void firstHitSurvives() {
            GreenPlayer p = new GreenPlayer("P1", 50f, 50f);
            p.receiveHit();
            assertEquals(0, p.getDeaths());
        }

        @Test @DisplayName("Primer golpe desactiva el escudo")
        void firstHitConsumesShield() {
            GreenPlayer p = new GreenPlayer("P1", 50f, 50f);
            p.receiveHit();
            assertFalse(p.isShieldActive());
        }

        @Test @DisplayName("Primer golpe activa invencibilidad")
        void firstHitActivatesInvincibility() {
            GreenPlayer p = new GreenPlayer("P1", 50f, 50f);
            p.receiveHit();
            assertTrue(p.isInvincible());
        }

        @Test @DisplayName("Segundo golpe durante invencibilidad es ignorado")
        void secondHitDuringInvincibilityIgnored() {
            GreenPlayer p = new GreenPlayer("P1", 50f, 50f);
            p.receiveHit();   // rompe escudo → invencible
            p.receiveHit();   // debe ignorarse
            assertEquals(0, p.getDeaths());
        }

        @Test @DisplayName("Segundo golpe después de invencibilidad mata")
        void secondHitAfterInvincibilityDies() {
            GreenPlayer p = new GreenPlayer("P1", 50f, 50f);
            p.receiveHit();              // rompe escudo → invencible

            // Drenar todos los ticks de invencibilidad
            for (int i = 0; i < 50; i++) p.tick();

            assertFalse(p.isInvincible());
            p.receiveHit();              // ahora sí debe morir
            assertEquals(1, p.getDeaths());
        }

        @Test @DisplayName("die() restaura escudo y velocidad")
        void dieRestoresShieldAndSpeed() {
            GreenPlayer p = new GreenPlayer("P1", 50f, 50f);
            p.receiveHit();                     // rompe escudo
            for (int i = 0; i < 50; i++) p.tick(); // drenar invencibilidad
            p.receiveHit();                     // muere

            assertTrue(p.isShieldActive(), "El escudo debe restaurarse al morir");
        }

        @Test @DisplayName("tick() decrementa invencibilityTimer")
        void tickDecrements() {
            GreenPlayer p = new GreenPlayer("P1", 50f, 50f);
            p.receiveHit(); // 45 ticks
            for (int i = 0; i < 45; i++) p.tick();
            assertFalse(p.isInvincible());
        }
    }

    // ══════════════════════════════════════════════
    //  PLAYER — MachinePlayer
    // ══════════════════════════════════════════════

    @Nested @DisplayName("MachinePlayer")
    class MachinePlayerTests {

        @Test @DisplayName("Tipo 'Machine' y estrategia no nula")
        void init() {
            MachinePlayer m = new MachinePlayer("M", 0f, 0f, new RandomMachineStrategy());
            assertEquals("Machine", m.getPlayerType());
            assertNotNull(m.getStrategy());
        }

        @Test @DisplayName("receiveHit → muere (no tiene escudo)")
        void firstHitDies() {
            MachinePlayer m = new MachinePlayer("M", 50f, 50f, new RandomMachineStrategy());
            m.receiveHit();
            assertEquals(1, m.getDeaths());
        }
    }

    // ══════════════════════════════════════════════
    //  ENEMY
    // ══════════════════════════════════════════════

    @Nested @DisplayName("Enemy")
    class EnemyTests {

        @Test @DisplayName("BasicBluePoint: activo, velocidad 4.5, board null")
        void basicInit() {
            BasicBluePoint e = new BasicBluePoint(0f, 0f);
            assertTrue(e.isActive());
            assertEquals(4.5f, e.getSpeed(), 0.1f);
            assertEquals(GameConfig.CELL_SIZE - 4f, e.getSize(), 0.1f);
            assertNull(e.getBoard());
        }

        @Test @DisplayName("VerticalSlider: velocidad 3.0")
        void verticalInit() {
            VerticalSlider e = new VerticalSlider(0f, 0f);
            assertTrue(e.isActive());
            assertEquals(3.0f, e.getSpeed(), 0.1f);
        }

        @Test @DisplayName("AcceleratedEnemy: velocidad 6.0")
        void acceleratedInit() {
            AcceleratedEnemy e = new AcceleratedEnemy(0f, 0f);
            assertEquals(6.0f, e.getSpeed(), 0.1f);
        }

        @Test @DisplayName("destroy() desactiva el enemigo")
        void destroy() {
            BasicBluePoint e = new BasicBluePoint(0f, 0f);
            e.destroy();
            assertFalse(e.isActive());
        }

        @Test @DisplayName("update() sin board no mueve al enemigo")
        void updateWithoutBoard() {
            BasicBluePoint e = new BasicBluePoint(5 * GameConfig.CELL_SIZE, 4 * GameConfig.CELL_SIZE);
            float x0 = e.getX();
            e.update();
            assertEquals(x0, e.getX(), 0.01f);
        }

        @Test @DisplayName("update() con board mueve al enemigo")
        void updateWithBoard() {
            GameBoard board = buildTestBoard();
            BasicBluePoint e = new BasicBluePoint(5 * GameConfig.CELL_SIZE, 4 * GameConfig.CELL_SIZE);
            e.setBoard(board);
            float x0 = e.getX();
            e.update();
            assertNotEquals(x0, e.getX(), 0.001f);
        }

        @Test @DisplayName("Enemigo rebota y no sale del tablero")
        void bounce() {
            GameBoard board = new GameBoard(5, 10);
            for (int c = 2; c <= 7; c++) board.setCell(2, c, CellType.WALKABLE);
            BasicBluePoint e = new BasicBluePoint(2 * GameConfig.CELL_SIZE, 2 * GameConfig.CELL_SIZE);
            e.setBoard(board);
            for (int i = 0; i < 30; i++) e.update();
            assertTrue(e.getX() <= 8 * GameConfig.CELL_SIZE);
        }

        @Test @DisplayName("Colisión AABB enemigo-jugador en misma posición")
        void collidesPlayer() {
            BasicBluePoint e = new BasicBluePoint(100f, 100f);
            RedPlayer p = new RedPlayer("P1", 100f, 100f);
            assertTrue(e.collides(p));
        }

        @Test @DisplayName("Enemigo lejos no colisiona")
        void noCollision() {
            BasicBluePoint e = new BasicBluePoint(50f, 50f);
            RedPlayer p = new RedPlayer("P1", 300f, 300f);
            assertFalse(e.collides(p));
        }

        @Test @DisplayName("setBoard asigna el tablero")
        void setBoard() {
            BasicBluePoint e = new BasicBluePoint(0f, 0f);
            GameBoard board = buildTestBoard();
            e.setBoard(board);
            assertNotNull(e.getBoard());
        }
    }

    // ══════════════════════════════════════════════
    //  COIN
    // ══════════════════════════════════════════════

    @Nested @DisplayName("Coin")
    class CoinTests {

        @Test @DisplayName("YellowCoin: no recogida, no skinCoin")
        void yellowInit() {
            YellowCoin c = new YellowCoin(new Position(3, 4));
            assertFalse(c.isCollected());
            assertFalse(c.isSkinCoin());
        }

        @Test @DisplayName("SkinCoin: no recogida, es skinCoin")
        void skinInit() {
            SkinCoin c = new SkinCoin(new Position(2, 2));
            assertFalse(c.isCollected());
            assertTrue(c.isSkinCoin());
        }

        @Test @DisplayName("collect() marca como recogida")
        void collect() {
            YellowCoin c = new YellowCoin(new Position(1, 1));
            c.collect();
            assertTrue(c.isCollected());
        }

        @Test @DisplayName("Colisión moneda-jugador en misma celda")
        void collidesTrue() {
            int cell = GameConfig.CELL_SIZE;
            YellowCoin coin = new YellowCoin(new Position(4, 5));
            RedPlayer p = new RedPlayer("P1", 5 * cell + 3f, 4 * cell + 3f);
            assertTrue(coin.collides(p));
        }

        @Test @DisplayName("Moneda lejos no colisiona")
        void collidesFalse() {
            YellowCoin coin = new YellowCoin(new Position(4, 5));
            RedPlayer p = new RedPlayer("P1", 0f, 0f);
            assertFalse(coin.collides(p));
        }

        @Test @DisplayName("applyEffect de YellowCoin no cambia al jugador")
        void yellowApplyEffect() {
            YellowCoin coin = new YellowCoin(new Position(1, 1));
            RedPlayer p = new RedPlayer("P1", 0f, 0f);
            float speedBefore = p.getSpeed();
            coin.applyEffect(p);
            assertEquals(speedBefore, p.getSpeed(), 0.01f);
        }

        @Test @DisplayName("getPosition devuelve la posición correcta")
        void position() {
            YellowCoin c = new YellowCoin(new Position(3, 7));
            assertEquals(3, c.getPosition().getRow());
            assertEquals(7, c.getPosition().getColumn());
        }
    }

    // ══════════════════════════════════════════════
    //  SPECIAL ELEMENT
    // ══════════════════════════════════════════════

    @Nested @DisplayName("SpecialElement")
    class SpecialElementTests {

        @Test @DisplayName("Bomb: activa, isBomb, no isLifeSource")
        void bombInit() {
            Bomb b = new Bomb(new Position(5, 5));
            assertTrue(b.isActive());
            assertTrue(b.isBomb());
            assertFalse(b.isLifeSource());
        }

        @Test @DisplayName("LifeSource: activa, isLifeSource, no isBomb")
        void lifeSourceInit() {
            LifeSource ls = new LifeSource(new Position(2, 3));
            assertTrue(ls.isActive());
            assertTrue(ls.isLifeSource());
            assertFalse(ls.isBomb());
        }

        @Test @DisplayName("consume() desactiva el elemento")
        void consume() {
            Bomb b = new Bomb(new Position(1, 1));
            b.consume();
            assertFalse(b.isActive());
        }

        @Test @DisplayName("deactivated() desactiva el elemento")
        void deactivated() {
            LifeSource ls = new LifeSource(new Position(1, 1));
            ls.deactivated();
            assertFalse(ls.isActive());
        }

        @Test @DisplayName("Bomb.applyEffect mata al jugador y se desactiva")
        void bombKillsAndDeactivates() {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            Bomb bomb = new Bomb(new Position(4, 5));
            RedPlayer p = new RedPlayer("P1", 0f, 0f);
            game.addPlayer(p);
            bomb.applyEffect(game, p);
            assertEquals(1, p.getDeaths());
            assertFalse(bomb.isActive());
        }

        @Test @DisplayName("LifeSource.applyEffect activa escudo al jugador y se desactiva")
        void lifeSourceActivatesShield() {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            LifeSource ls = new LifeSource(new Position(4, 5));
            RedPlayer p = new RedPlayer("P1", 0f, 0f);
            game.addPlayer(p);
            ls.applyEffect(game, p);
            assertTrue(p.isShieldActive(),
                    "LifeSource debe dar escudo al jugador, independiente del tipo");
            assertFalse(ls.isActive());
        }

        @Test @DisplayName("Bomb consumida no mata al jugador de nuevo")
        void bombConsumedNoEffect() {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            Bomb bomb = new Bomb(new Position(4, 5));
            RedPlayer p = new RedPlayer("P1", 0f, 0f);
            game.addPlayer(p);

            bomb.applyEffect(game, p); // primera vez: muere + desactiva
            int deathsAfterFirst = p.getDeaths();

            // Drenar invencibilidad
            for (int i = 0; i < 50; i++) p.tick();

            bomb.applyEffect(game, p); // segunda vez: no debe hacer nada
            assertEquals(deathsAfterFirst, p.getDeaths(),
                    "La bomba consumida no debe matar de nuevo");
        }

        @Test @DisplayName("getPosition devuelve la posición del elemento")
        void position() {
            Bomb b = new Bomb(new Position(3, 8));
            assertEquals(3, b.getPosition().getRow());
            assertEquals(8, b.getPosition().getColumn());
        }
    }

    // ══════════════════════════════════════════════
    //  LEVEL
    // ══════════════════════════════════════════════

    @Nested @DisplayName("Level")
    class LevelTests {

        @Test @DisplayName("Listas vacías al crear")
        void initEmpty() {
            Level level = buildTestLevel();
            assertEquals("Test", level.getLevelName());
            assertEquals(2700, level.getTimeLimit());
            assertTrue(level.getEnemies().isEmpty());
            assertTrue(level.getCoins().isEmpty());
            assertTrue(level.getSpecialElements().isEmpty());
        }

        @Test @DisplayName("addEnemy agrega correctamente")
        void addEnemy() {
            Level level = buildTestLevel();
            level.addEnemy(new BasicBluePoint(0f, 0f));
            assertEquals(1, level.getEnemies().size());
        }

        @Test @DisplayName("addCoin agrega correctamente")
        void addCoin() {
            Level level = buildTestLevel();
            level.addCoin(new YellowCoin(new Position(4, 6)));
            assertEquals(1, level.getCoins().size());
        }

        @Test @DisplayName("addSpecialElement agrega correctamente")
        void addSpecial() {
            Level level = buildTestLevel();
            level.addSpecialElement(new Bomb(new Position(4, 5)));
            assertEquals(1, level.getSpecialElements().size());
        }

        @Test @DisplayName("allCoinsCollected: true si no hay monedas")
        void allCoinsNoCoins() { assertTrue(buildTestLevel().allCoinsCollected()); }

        @Test @DisplayName("allCoinsCollected: false si hay monedas sin recoger")
        void allCoinsFalse() {
            Level level = buildTestLevel();
            level.addCoin(new YellowCoin(new Position(4, 6)));
            assertFalse(level.allCoinsCollected());
        }

        @Test @DisplayName("allCoinsCollected: true si todas recogidas")
        void allCoinsTrue() {
            Level level = buildTestLevel();
            YellowCoin coin = new YellowCoin(new Position(4, 6));
            level.addCoin(coin);
            coin.collect();
            assertTrue(level.allCoinsCollected());
        }

        @Test @DisplayName("getBoard y getDefaultSpawn")
        void boardAndSpawn() {
            Level level = buildTestLevel();
            assertNotNull(level.getBoard());
            assertNotNull(level.getDefaultSpawn());
            assertEquals(4, level.getDefaultSpawn().getRow());
        }
    }

    // ══════════════════════════════════════════════
    //  GAME
    // ══════════════════════════════════════════════

    @Nested @DisplayName("Game")
    class GameTests {

        @Test @DisplayName("Inicia en RunningState")
        void initialState() {
            Game game = new Game(buildTestLevel(), GameMode.SINGLE_PLAYER);
            assertTrue(game.getGameState().isRunning());
        }

        @Test @DisplayName("pause() → PausedState")
        void pause() {
            Game game = new Game(buildTestLevel(), GameMode.SINGLE_PLAYER);
            game.pause();
            assertTrue(game.getGameState().isPaused());
        }

        @Test @DisplayName("resume() → RunningState")
        void resume() {
            Game game = new Game(buildTestLevel(), GameMode.SINGLE_PLAYER);
            game.pause();
            game.resume();
            assertTrue(game.getGameState().isRunning());
        }

        @Test @DisplayName("finishGame() → GameOverState")
        void finish() {
            Game game = new Game(buildTestLevel(), GameMode.SINGLE_PLAYER);
            game.finishGame();
            assertTrue(game.getGameState().isGameOver());
        }

        @Test @DisplayName("winGame() → WinState")
        void win() {
            Game game = new Game(buildTestLevel(), GameMode.SINGLE_PLAYER);
            game.winGame();
            assertTrue(game.getGameState().isWin());
        }

        @Test @DisplayName("Doble pause no cambia a otro estado")
        void doublePause() {
            Game game = new Game(buildTestLevel(), GameMode.SINGLE_PLAYER);
            game.pause(); game.pause();
            assertTrue(game.getGameState().isPaused());
        }

        @Test @DisplayName("updateTimer() decrementa el tiempo")
        void timerDecrement() {
            Game game = new Game(buildTestLevel(), GameMode.SINGLE_PLAYER);
            int t0 = game.getRemainingTime();
            game.updateTimer();
            assertEquals(t0 - 1, game.getRemainingTime());
        }

        @Test @DisplayName("updateTimer() termina el juego al llegar a 0")
        void timerGameOver() {
            Game game = new Game(buildTestLevel(), GameMode.SINGLE_PLAYER);
            game.setRemainingTime(1);
            game.updateTimer();
            assertTrue(game.getGameState().isGameOver());
        }

        @Test @DisplayName("movePlayer() mueve al jugador con Direction")
        void movePlayerDirection() {
            Game game = new Game(buildTestLevel(), GameMode.SINGLE_PLAYER);
            int cell = GameConfig.CELL_SIZE;
            RedPlayer p = new RedPlayer("P1", 5 * cell + 3f, 4 * cell + 3f);
            game.addPlayer(p);
            float x0 = p.getX();
            game.movePlayer(0, Direction.RIGHT);
            assertTrue(p.getX() > x0);
        }

        @Test @DisplayName("movePlayer() en Paused no mueve al jugador")
        void movePlayerPaused() {
            Game game = new Game(buildTestLevel(), GameMode.SINGLE_PLAYER);
            int cell = GameConfig.CELL_SIZE;
            RedPlayer p = new RedPlayer("P1", 5 * cell + 3f, 4 * cell + 3f);
            game.addPlayer(p);
            float x0 = p.getX();
            game.pause();
            game.movePlayer(0, Direction.RIGHT);
            assertEquals(x0, p.getX(), 0.01f);
        }

        @Test @DisplayName("checkCoinCollision recoge moneda al pisarla")
        void coinCollection() {
            Level level = buildTestLevel();
            int cell = GameConfig.CELL_SIZE;
            level.addCoin(new YellowCoin(new Position(4, 5)));
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            RedPlayer p = new RedPlayer("P1", 5 * cell + 3f, 4 * cell + 3f);
            game.addPlayer(p);
            game.checkCoinCollision();
            assertTrue(level.getCoins().get(0).isCollected());
            assertEquals(1, p.getCollectedCoins());
        }

        @Test @DisplayName("checkEnemyCollsion mata al jugador al tocarlo")
        void enemyKillsPlayer() {
            Level level = buildTestLevel();
            float x = 100f, y = 130f;
            BasicBluePoint enemy = new BasicBluePoint(x, y);
            level.addEnemy(enemy);
            enemy.setBoard(level.getBoard());
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            RedPlayer p = new RedPlayer("P1", x, y);
            game.addPlayer(p);
            game.checkEnemyCollsion();
            assertEquals(1, p.getDeaths());
        }

        @Test @DisplayName("checkSpecialElements: bomba activa mata, bomba consumida no mata")
        void bombConsumedNoKill() {
            Level level = buildTestLevel();
            int cell = GameConfig.CELL_SIZE;
            Bomb bomb = new Bomb(new Position(4, 5));
            level.addSpecialElement(bomb);
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            RedPlayer p = new RedPlayer("P1", 5 * cell + 3f, 4 * cell + 3f);
            game.addPlayer(p);

            game.checkSpecialElements();     // primera colisión: muere
            int deathsAfterFirst = p.getDeaths();
            for (int i = 0; i < 50; i++) p.tick();

            game.checkSpecialElements();     // bomba ya consumida: sin efecto
            assertEquals(deathsAfterFirst, p.getDeaths(),
                    "La bomba consumida no debe volver a matar");
        }

        @Test @DisplayName("checkPlayerCollisions en PvP mata a ambos")
        void pvpCollision() {
            Game game = new Game(buildTestLevel(), GameMode.PLAYER_VS_PLAYER);
            RedPlayer p1 = new RedPlayer("P1", 100f, 100f);
            RedPlayer p2 = new RedPlayer("P2", 100f, 100f);
            game.addPlayer(p1); game.addPlayer(p2);
            game.checkPlayerCollisions();
            assertEquals(1, p1.getDeaths());
            assertEquals(1, p2.getDeaths());
        }

        @Test @DisplayName("checkPlayerCollisions en SinglePlayer no hace nada")
        void singlePlayerNoCollision() {
            Game game = new Game(buildTestLevel(), GameMode.SINGLE_PLAYER);
            RedPlayer p = new RedPlayer("P1", 100f, 100f);
            game.addPlayer(p);
            game.checkPlayerCollisions();
            assertEquals(0, p.getDeaths());
        }

        @Test @DisplayName("checkGoal no gana si faltan monedas")
        void goalRequiresCoins() {
            Level level = buildTestLevel();
            level.addCoin(new YellowCoin(new Position(4, 5)));
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            int cell = GameConfig.CELL_SIZE;
            game.addPlayer(new RedPlayer("P1", 18 * cell + 3f, 4 * cell + 3f));
            game.checkGoal();
            assertFalse(game.getGameState().isWin());
        }

        @Test @DisplayName("tick() en GreenPlayer decrementa el timer de invencibilidad")
        void tickPlayers() {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            GreenPlayer p = new GreenPlayer("P1", 50f, 50f);
            game.addPlayer(p);
            p.receiveHit();  // activa 45 ticks de invencibilidad
            assertTrue(p.isInvincible());
            for (int i = 0; i < 45; i++) p.tick();
            assertFalse(p.isInvincible());
        }

        @Test @DisplayName("isInvincible del jugador tras recibir golpe con escudo")
        void playerInvincibleAfterShieldHit() {
            Game game = new Game(buildTestLevel(), GameMode.SINGLE_PLAYER);
            GreenPlayer p = new GreenPlayer("P1", 50f, 50f);
            game.addPlayer(p);
            assertFalse(p.isInvincible());
            p.receiveHit();
            assertTrue(p.isInvincible());
        }

        @Test @DisplayName("isShieldActive del jugador tras activateShield")
        void playerShieldActiveAfterActivate() {
            Game game = new Game(buildTestLevel(), GameMode.SINGLE_PLAYER);
            RedPlayer p = new RedPlayer("P1", 50f, 50f);
            game.addPlayer(p);
            assertFalse(p.isShieldActive());
            p.activateShield();
            assertTrue(p.isShieldActive());
        }

        @Test @DisplayName("getGameMode retorna el modo correcto")
        void getGameMode() {
            Game game = new Game(buildTestLevel(), GameMode.PLAYER_VS_PLAYER);
            assertEquals(GameMode.PLAYER_VS_PLAYER, game.getGameMode());
        }
    }

    // ══════════════════════════════════════════════
    //  GAME SAVE
    // ══════════════════════════════════════════════

    @Nested @DisplayName("GameSave")
    class GameSaveTests {

        @Test @DisplayName("saveGame y loadGame conservan tiempo, modo y tipo")
        void saveLoad() throws Exception {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            game.setRemainingTime(1234);
            game.addPlayer(new GreenPlayer("P1", 50f, 50f));

            new java.io.File("saves").mkdirs();
            game.saveGame("saves/unit_test.dat", "recursos/nivel1.txt");
            GameSave save = Game.loadGame("saves/unit_test.dat");

            assertEquals(1234, save.getRemainingTime());
            assertEquals(GameMode.SINGLE_PLAYER, save.getGameMode());
            assertEquals("Green", save.getPlayerSnapshots().get(0).getType());
        }

        @Test @DisplayName("saveGame conserva monedas recogidas")
        void saveCoins() throws Exception {
            Level level = buildTestLevel();
            level.addCoin(new YellowCoin(new Position(4, 5)));
            level.getCoins().get(0).collect();
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            game.addPlayer(new RedPlayer("P1", 50f, 50f));

            new java.io.File("saves").mkdirs();
            game.saveGame("saves/unit_coins.dat", "recursos/nivel1.txt");
            GameSave save = Game.loadGame("saves/unit_coins.dat");

            assertEquals(1, save.getCollectedCoinPositions().size());
        }

        @Test @DisplayName("loadGame lanza GameException si el archivo no existe")
        void loadMissing() {
            assertThrows(GameException.class,
                    () -> Game.loadGame("saves/no_existe_unit.dat"));
        }

        @Test @DisplayName("saveGame conserva muertes del jugador")
        void saveDeaths() throws Exception {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            RedPlayer p = new RedPlayer("P1", 50f, 50f);
            p.setDeaths(5);
            game.addPlayer(p);

            new java.io.File("saves").mkdirs();
            game.saveGame("saves/unit_deaths.dat", "recursos/nivel1.txt");
            GameSave save = Game.loadGame("saves/unit_deaths.dat");

            assertEquals(5, save.getPlayerSnapshots().get(0).getDeaths());
        }
    }

    // ══════════════════════════════════════════════
    //  LEVEL LOADER
    // ══════════════════════════════════════════════

    @Nested @DisplayName("LevelLoader")
    class LevelLoaderTests {

        @Test @DisplayName("Lanza GameException si el archivo no existe")
        void throwsWhenMissing() {
            assertThrows(GameException.class,
                    () -> LevelLoader.load("recursos/noexiste.txt", "Test"),
                    "LevelLoader debe lanzar GameException, no retornar null");
        }

        @Test @DisplayName("Carga nivel1.txt correctamente")
        void loadLevel1() throws GameException {
            Level level = LevelLoader.load("recursos/nivel1.txt", "Nivel 1");
            assertNotNull(level);
            assertEquals("Nivel 1", level.getLevelName());
            assertTrue(level.getBoard().getRows() > 0);
            assertTrue(level.getBoard().getColumns() > 0);
            assertNotNull(level.getDefaultSpawn());
            for (Enemy e : level.getEnemies())
                assertNotNull(e.getBoard());
        }
    }

    // ══════════════════════════════════════════════
    //  MACHINE STRATEGY
    // ══════════════════════════════════════════════

    @Nested @DisplayName("MachineStrategy")
    class MachineStrategyTests {

        @Test @DisplayName("RandomMachineStrategy devuelve una dirección válida")
        void randomReturnsDirection() {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.PLAYER_VS_MACHINE);
            MachinePlayer m = new MachinePlayer("M",
                    5 * GameConfig.CELL_SIZE + 3f, 4 * GameConfig.CELL_SIZE + 3f,
                    new RandomMachineStrategy());
            game.addPlayer(m);

            RandomMachineStrategy s = new RandomMachineStrategy();
            boolean got = false;
            for (int i = 0; i < 30; i++)
                if (s.decideDirection(m, game) != null) { got = true; break; }
            assertTrue(got);
        }

        @Test @DisplayName("updateMachine() mueve solo al jugador Machine")
        void updateMachineOnly() {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.PLAYER_VS_MACHINE);
            RedPlayer human = new RedPlayer("P1", 3 * GameConfig.CELL_SIZE + 3f,
                    4 * GameConfig.CELL_SIZE + 3f);
            MachinePlayer machine = new MachinePlayer("M",
                    5 * GameConfig.CELL_SIZE + 3f, 4 * GameConfig.CELL_SIZE + 3f,
                    new RandomMachineStrategy());
            game.addPlayer(human);
            game.addPlayer(machine);
            float hx = human.getX();
            for (int i = 0; i < 10; i++) game.updateMachine();
            assertEquals(hx, human.getX(), 0.01f);
        }

        @Test @DisplayName("ExpertMachineStrategy se dirige hacia la moneda")
        void expertTargetsCoin() {
            GameBoard board = buildTestBoard();
            Level level = new Level("Test", board, 2700);
            level.addCoin(new YellowCoin(new Position(4, 10)));
            Game game = new Game(level, GameMode.PLAYER_VS_MACHINE);
            int cell = GameConfig.CELL_SIZE;

            game.addPlayer(new RedPlayer("P1", 1 * cell + 3f, 4 * cell + 3f));
            MachinePlayer m = new MachinePlayer("M", 17 * cell + 3f, 4 * cell + 3f,
                    new ExpertMachineStrategy());
            game.addPlayer(m);
            float x0 = m.getX();
            for (int i = 0; i < 30; i++) game.updateMachine();
            assertTrue(m.getX() < x0,
                    "La máquina experta debe avanzar hacia la moneda (izquierda)");
        }
    }

    // ══════════════════════════════════════════════
    //  GAME — accesores del tablero vía getCurrentLevel
    // ══════════════════════════════════════════════

    @Nested @DisplayName("Game — accesores del tablero")
    class GameBoardAccessors {

        @Test @DisplayName("getCurrentLevel retorna el nivel y su board tiene dimensiones correctas")
        void boardDimensions() {
            Game game = buildGame();
            GameBoard board = game.getCurrentLevel().getBoard();
            assertEquals(10, board.getRows());
            assertEquals(20, board.getColumns());
        }

        @Test @DisplayName("getCurrentLevel retorna el nivel con la celda correcta por tipo")
        void cellTypes() {
            GameBoard board = buildGame().getCurrentLevel().getBoard();
            assertEquals(CellType.WALKABLE,   board.getCell(4, 8).getType());
            assertEquals(CellType.SPAWN_ZONE, board.getCell(4, 0).getType());
            assertEquals(CellType.GOAL,       board.getCell(4, 18).getType());
        }

        @Test @DisplayName("getCurrentLevel.getCoins, getEnemies, getSpecialElements")
        void levelLists() {
            Level level = buildTestLevel();
            level.addCoin(new YellowCoin(new Position(4, 5)));
            level.addEnemy(new BasicBluePoint(100f, 100f));
            level.addSpecialElement(new Bomb(new Position(4, 6)));
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            assertEquals(1, game.getCurrentLevel().getCoins().size());
            assertEquals(1, game.getCurrentLevel().getEnemies().size());
            assertEquals(1, game.getCurrentLevel().getSpecialElements().size());
        }

        @Test @DisplayName("getPlayers retorna lista con los jugadores añadidos")
        void getPlayers() {
            Game game = buildGame();
            game.addPlayer(new RedPlayer("P1", 0f, 0f));
            assertNotNull(game.getCurrentLevel());
            assertEquals(1, game.getPlayers().size());
        }

        @Test @DisplayName("setState cambia el estado directamente")
        void setState() {
            Game game = buildGame();
            game.setState(new PausedState());
            assertTrue(game.getGameState().isPaused());
        }
    }

    // ══════════════════════════════════════════════
    //  GAME — accesores de jugadores vía getPlayers()
    // ══════════════════════════════════════════════

    @Nested @DisplayName("Game — accesores de jugadores")
    class GamePlayerAccessors {

        private Game gameWith(Player p) {
            Game game = buildGame();
            game.addPlayer(p);
            return game;
        }

        @Test @DisplayName("getPlayers().size() retorna número de jugadores")
        void getPlayerCount() {
            Game game = buildGame();
            assertEquals(0, game.getPlayers().size());
            game.addPlayer(new RedPlayer("P1", 0f, 0f));
            assertEquals(1, game.getPlayers().size());
        }

        @Test @DisplayName("getPlayers().get(0).getX/Y retornan coordenadas")
        void getPlayerXY() {
            Game game = gameWith(new RedPlayer("P1", 52f, 55f));
            assertEquals(52f, game.getPlayers().get(0).getX(), 0.01f);
            assertEquals(55f, game.getPlayers().get(0).getY(), 0.01f);
        }

        @Test @DisplayName("getPlayers().get(0).getSize retorna tamaño")
        void getPlayerSize() {
            Game game = gameWith(new RedPlayer("P1", 0f, 0f));
            assertEquals(GameConfig.CELL_SIZE - 6f,
                    game.getPlayers().get(0).getSize(), 0.01f);
        }

        @Test @DisplayName("getPlayers().get(0).getDeaths retorna muertes")
        void getPlayerDeaths() {
            RedPlayer p = new RedPlayer("P1", 0f, 0f);
            p.setDeaths(3);
            assertEquals(3, gameWith(p).getPlayers().get(0).getDeaths());
        }

        @Test @DisplayName("getPlayers().get(0).getCollectedCoins retorna monedas")
        void getPlayerCoins() {
            RedPlayer p = new RedPlayer("P1", 0f, 0f);
            p.addCoin(); p.addCoin();
            assertEquals(2, gameWith(p).getPlayers().get(0).getCollectedCoins());
        }

        @Test @DisplayName("getPlayers().get(0).getPlayerType retorna el tipo")
        void getPlayerType() {
            assertEquals("Green",
                    gameWith(new GreenPlayer("P1", 0f, 0f)).getPlayers().get(0).getPlayerType());
            assertEquals("Blue",
                    gameWith(new BluePlayer("P1", 0f, 0f)).getPlayers().get(0).getPlayerType());
        }

        @Test @DisplayName("getPlayers().get(0).getBorderColor retorna el color de borde")
        void getPlayerBorderColor() {
            RedPlayer p = new RedPlayer("P1", 0f, 0f);
            p.setBorderColor(java.awt.Color.CYAN);
            assertEquals(java.awt.Color.CYAN,
                    gameWith(p).getPlayers().get(0).getBorderColor());
        }

        @Test @DisplayName("getPlayerType 'Machine' identifica al MachinePlayer")
        void machinePlayerType() {
            Game game = buildGame();
            game.addPlayer(new RedPlayer("P1", 0f, 0f));
            game.addPlayer(new MachinePlayer("M", 0f, 0f, new RandomMachineStrategy()));
            assertFalse("Machine".equals(game.getPlayers().get(0).getPlayerType()));
            assertTrue("Machine".equals(game.getPlayers().get(1).getPlayerType()));
        }
    }

    // ══════════════════════════════════════════════
    //  GAME — moveEnemies y estados delegados
    // ══════════════════════════════════════════════

    @Nested @DisplayName("Game — moveEnemies y estados delegados")
    class GameStaticAndEnemies {

        @Test @DisplayName("moveEnemies mueve todos los enemigos con board asignado")
        void moveEnemies() {
            Level level = buildTestLevel();
            int cell = GameConfig.CELL_SIZE;
            BasicBluePoint e = new BasicBluePoint(5 * cell, 4 * cell);
            e.setBoard(level.getBoard());
            level.addEnemy(e);
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            float x0 = e.getX();
            game.moveEnemies();
            assertNotEquals(x0, e.getX(), 0.001f);
        }

        @Test @DisplayName("getGameState refleja correctamente Running, Paused y GameOver")
        void stateDelegates() {
            Game game = buildGame();
            assertTrue(game.getGameState().isRunning());
            assertFalse(game.getGameState().isPaused());
            game.pause();
            assertTrue(game.getGameState().isPaused());
            assertFalse(game.getGameState().isRunning());
            game.resume();
            game.finishGame();
            assertTrue(game.getGameState().isGameOver());
            assertFalse(game.getGameState().isWin());
        }
    }

    // ══════════════════════════════════════════════
    //  GAMESTATE — transiciones completas
    // ══════════════════════════════════════════════

    @Nested @DisplayName("GameState — transiciones de estado completas")
    class GameStateTransitions {

        @Test @DisplayName("RunningState: finish → GameOverState")
        void runningFinish() {
            Game game = buildGame();
            game.finishGame();
            assertTrue(game.getGameState().isGameOver());
        }

        @Test @DisplayName("RunningState: win → WinState")
        void runningWin() {
            Game game = buildGame();
            game.winGame();
            assertTrue(game.getGameState().isWin());
        }

        @Test @DisplayName("RunningState: resume desde Running no cambia estado")
        void runningResume() {
            Game game = buildGame();
            game.resume();
            assertTrue(game.getGameState().isRunning());
        }

        @Test @DisplayName("PausedState: resume → RunningState")
        void pausedResume() {
            Game game = buildGame();
            game.pause(); game.resume();
            assertTrue(game.getGameState().isRunning());
        }

        @Test @DisplayName("PausedState: finish → GameOverState")
        void pausedFinish() {
            Game game = buildGame();
            game.pause(); game.finishGame();
            assertTrue(game.getGameState().isGameOver());
        }

        @Test @DisplayName("PausedState: pause desde Paused no cambia estado")
        void doublePause() {
            Game game = buildGame();
            game.pause(); game.pause();
            assertTrue(game.getGameState().isPaused());
        }

        @Test @DisplayName("WinState: pause es ignorado")
        void winIgnoresPause() {
            Game game = buildGame();
            game.winGame();
            game.pause();
            assertTrue(game.getGameState().isWin());
        }

        @Test @DisplayName("WinState: movePlayer es ignorado")
        void winIgnoresMove() {
            Game game = buildGame();
            int cell = GameConfig.CELL_SIZE;
            RedPlayer p = new RedPlayer("P1", 5 * cell + 3f, 4 * cell + 3f);
            game.addPlayer(p);
            game.winGame();
            float x0 = p.getX();
            game.movePlayer(0, Direction.RIGHT);
            assertEquals(x0, p.getX(), 0.01f);
        }

        @Test @DisplayName("GameOverState: resume es ignorado")
        void gameOverIgnoresResume() {
            Game game = buildGame();
            game.finishGame(); game.resume();
            assertTrue(game.getGameState().isGameOver());
        }

        @Test @DisplayName("GameOverState: movePlayer es ignorado")
        void gameOverIgnoresMove() {
            Game game = buildGame();
            int cell = GameConfig.CELL_SIZE;
            RedPlayer p = new RedPlayer("P1", 5 * cell + 3f, 4 * cell + 3f);
            game.addPlayer(p);
            game.finishGame();
            float x0 = p.getX();
            game.movePlayer(0, Direction.RIGHT);
            assertEquals(x0, p.getX(), 0.01f);
        }
    }

    // ══════════════════════════════════════════════
    //  SKINCOIN — applyEffect y colisión
    // ══════════════════════════════════════════════

    @Nested @DisplayName("SkinCoin — cobertura completa")
    class SkinCoinCoverage {

        @Test @DisplayName("applyEffect no lanza excepción")
        void applyEffect() {
            SkinCoin coin = new SkinCoin(new Position(1, 1));
            assertDoesNotThrow(() -> coin.applyEffect(new RedPlayer("P1", 0f, 0f)));
        }

        @Test @DisplayName("Colisión SkinCoin con jugador en misma celda")
        void collidesPlayer() {
            int cell = GameConfig.CELL_SIZE;
            SkinCoin coin = new SkinCoin(new Position(4, 5));
            assertTrue(coin.collides(new RedPlayer("P1", 5 * cell + 3f, 4 * cell + 3f)));
        }

        @Test @DisplayName("SkinCoin es recogida por checkCoinCollision")
        void collectedByGame() {
            Level level = buildTestLevel();
            int cell = GameConfig.CELL_SIZE;
            SkinCoin coin = new SkinCoin(new Position(4, 5));
            level.addCoin(coin);
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            game.addPlayer(new RedPlayer("P1", 5 * cell + 3f, 4 * cell + 3f));
            game.checkCoinCollision();
            assertTrue(coin.isCollected());
        }
    }

    // ══════════════════════════════════════════════
    //  BLUEPLAYER — movimiento en todas las direcciones
    // ══════════════════════════════════════════════

    @Nested @DisplayName("BluePlayer — movimiento y escudo")
    class BluePlayerMovement {

        @Test @DisplayName("BluePlayer se mueve más rápido que RedPlayer")
        void fasterThanRed() {
            GameBoard board = buildTestBoard();
            int cell = GameConfig.CELL_SIZE;
            BluePlayer blue = new BluePlayer("B", 3 * cell + 3f, 4 * cell + 3f);
            RedPlayer  red  = new RedPlayer("R",  3 * cell + 3f, 4 * cell + 3f);
            float bx = blue.getX(), rx = red.getX();
            blue.move(Direction.RIGHT, board, cell);
            red.move(Direction.RIGHT, board, cell);
            assertTrue(blue.getX() - bx > red.getX() - rx);
        }

        @Test @DisplayName("BluePlayer se mueve en todas las direcciones cardinales")
        void allCardinalDirections() {
            GameBoard board = buildTestBoard();
            int cell = GameConfig.CELL_SIZE;

            BluePlayer p = new BluePlayer("B", 8 * cell + 3f, 5 * cell + 3f);
            float y0 = p.getY();
            p.move(Direction.UP, board, cell);
            assertTrue(p.getY() < y0);

            p = new BluePlayer("B", 8 * cell + 3f, 3 * cell + 3f);
            float y1 = p.getY();
            p.move(Direction.DOWN, board, cell);
            assertTrue(p.getY() > y1);

            p = new BluePlayer("B", 8 * cell + 3f, 4 * cell + 3f);
            float x0 = p.getX();
            p.move(Direction.LEFT, board, cell);
            assertTrue(p.getX() < x0);
        }

        @Test @DisplayName("BluePlayer se mueve en diagonales")
        void diagonalDirections() {
            GameBoard board = buildTestBoard();
            int cell = GameConfig.CELL_SIZE;

            BluePlayer p = new BluePlayer("B", 8 * cell + 3f, 5 * cell + 3f);
            float x0 = p.getX(), y0 = p.getY();
            p.move(Direction.UP_LEFT, board, cell);
            assertTrue(p.getX() < x0 && p.getY() < y0);

            p = new BluePlayer("B", 3 * cell + 3f, 3 * cell + 3f);
            float x1 = p.getX(), y1 = p.getY();
            p.move(Direction.DOWN_RIGHT, board, cell);
            assertTrue(p.getX() > x1 && p.getY() > y1);

            p = new BluePlayer("B", 3 * cell + 3f, 5 * cell + 3f);
            float x2 = p.getX(), y2 = p.getY();
            p.move(Direction.UP_RIGHT, board, cell);
            assertTrue(p.getX() > x2 && p.getY() < y2);

            p = new BluePlayer("B", 8 * cell + 3f, 3 * cell + 3f);
            float x3 = p.getX(), y3 = p.getY();
            p.move(Direction.DOWN_LEFT, board, cell);
            assertTrue(p.getX() < x3 && p.getY() > y3);
        }

        @Test @DisplayName("activateShield permite a BluePlayer absorber un golpe")
        void shieldAbsorbsHit() {
            BluePlayer p = new BluePlayer("B", 50f, 50f);
            p.activateShield();
            p.receiveHit();
            assertEquals(0, p.getDeaths());
            assertFalse(p.isShieldActive());
        }
    }

    // ══════════════════════════════════════════════
    //  MACHINEPLAYER 
    // ══════════════════════════════════════════════

    @Nested @DisplayName("MachinePlayer — cobertura adicional")
    class MachinePlayerCoverage {

        @Test @DisplayName("setBoard no lanza excepción")
        void setBoard() {
            MachinePlayer m = new MachinePlayer("M", 0f, 0f, new RandomMachineStrategy());
            assertDoesNotThrow(() -> m.setBoard(buildTestBoard(), GameConfig.CELL_SIZE));
        }

        @Test @DisplayName("update con dirección null no mueve al jugador")
        void updateNullDirection() {
            MachineStrategy nullStrategy = (player, game) -> null;
            MachinePlayer m = new MachinePlayer("M",
                    5 * GameConfig.CELL_SIZE + 3f, 4 * GameConfig.CELL_SIZE + 3f,
                    nullStrategy);
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.PLAYER_VS_MACHINE);
            game.addPlayer(m);
            float x0 = m.getX();
            m.update(game);
            assertEquals(x0, m.getX(), 0.01f);
        }

        @Test @DisplayName("MachinePlayer tick no cambia estado por defecto")
        void machinePlayerTick() {
            MachinePlayer m = new MachinePlayer("M", 0f, 0f, new RandomMachineStrategy());
            assertFalse(m.isInvincible());
            m.tick();
            assertFalse(m.isInvincible());
        }
    }

    // ══════════════════════════════════════════════
    //  PATROLBLUEPOINT 
    // ══════════════════════════════════════════════

    @Nested @DisplayName("PatrolBluePoint — movimiento en ruta circular")
    class PatrolBluePointCoverage {

        @Test @DisplayName("Se crea activo")
        void init() {
            assertTrue(new PatrolBluePoint(0f, 0f).isActive());
        }

        @Test @DisplayName("update() con board mueve al patrullero")
        void updateMoves() {
            int cell = GameConfig.CELL_SIZE;
            GameBoard board = buildTestBoard();
            PatrolBluePoint p = new PatrolBluePoint(5 * cell, 4 * cell);
            p.setBoard(board);
            float x0 = p.getX(), y0 = p.getY();
            p.update();
            assertTrue(p.getX() != x0 || p.getY() != y0,
                    "PatrolBluePoint debe moverse en cada update");
        }

        @Test @DisplayName("update() completa el circuito y sigue activo")
        void loopCompletes() {
            int cell = GameConfig.CELL_SIZE;
            GameBoard board = buildTestBoard();
            PatrolBluePoint p = new PatrolBluePoint(5 * cell, 4 * cell);
            p.setBoard(board);
            for (int i = 0; i < 200; i++) p.update();
            assertTrue(p.isActive());
        }

        @Test @DisplayName("destroy() desactiva el patrullero")
        void destroy() {
            PatrolBluePoint p = new PatrolBluePoint(0f, 0f);
            p.destroy();
            assertFalse(p.isActive());
        }

        @Test @DisplayName("Colisión con jugador en misma posición")
        void collidesPlayer() {
            PatrolBluePoint p = new PatrolBluePoint(100f, 100f);
            assertTrue(p.collides(new RedPlayer("P1", 100f, 100f)));
        }

        @Test @DisplayName("Sin board, update no lanza excepción")
        void updateWithoutBoard() {
            PatrolBluePoint p = new PatrolBluePoint(0f, 0f);
            assertDoesNotThrow(p::update);
        }
    }

    // ══════════════════════════════════════════════
    //  VERTICALSLIDER — movimiento y rebote
    // ══════════════════════════════════════════════

    @Nested @DisplayName("VerticalSlider — movimiento vertical")
    class VerticalSliderCoverage {

        @Test @DisplayName("Se mueve en Y con board asignado")
        void movesVertically() {
            GameBoard board = buildTestBoard();
            int cell = GameConfig.CELL_SIZE;
            VerticalSlider e = new VerticalSlider(5 * cell, 4 * cell);
            e.setBoard(board);
            float y0 = e.getY();
            e.update();
            assertNotEquals(y0, e.getY(), 0.001f);
        }

        @Test @DisplayName("X permanece fijo durante el movimiento vertical")
        void xStaysFixed() {
            GameBoard board = buildTestBoard();
            int cell = GameConfig.CELL_SIZE;
            VerticalSlider e = new VerticalSlider(5 * cell, 4 * cell);
            e.setBoard(board);
            float x0 = e.getX();
            for (int i = 0; i < 20; i++) e.update();
            assertEquals(x0, e.getX(), 0.01f);
        }

        @Test @DisplayName("Rebota y no sale del tablero tras muchos ticks")
        void bounces() {
            GameBoard board = new GameBoard(6, 10);
            for (int c = 3; c <= 6; c++) {
                board.setCell(2, c, CellType.WALKABLE);
                board.setCell(3, c, CellType.WALKABLE);
                board.setCell(4, c, CellType.WALKABLE);
            }
            int cell = GameConfig.CELL_SIZE;
            VerticalSlider e = new VerticalSlider(5 * cell, 2 * cell);
            e.setBoard(board);
            for (int i = 0; i < 60; i++) e.update();
            assertTrue(e.getY() >= 0);
        }

        @Test @DisplayName("Colisión con jugador en misma posición")
        void collidesPlayer() {
            VerticalSlider e = new VerticalSlider(100f, 100f);
            assertTrue(e.collides(new RedPlayer("P1", 100f, 100f)));
        }
    }

    // ══════════════════════════════════════════════
    //  LEVELLOADER — cobertura adicional
    // ══════════════════════════════════════════════

    @Nested @DisplayName("LevelLoader — cobertura adicional")
    class LevelLoaderCoverage {

        @Test @DisplayName("Nivel cargado tiene celdas SPAWN_ZONE")
        void hasSpawnZone() throws GameException {
            Level level = LevelLoader.load("recursos/nivel1.txt", "Nivel 1");
            boolean found = false;
            for (Cell[] row : level.getBoard().getCells())
                for (Cell c : row)
                    if (c.getType() == CellType.SPAWN_ZONE) { found = true; break; }
            assertTrue(found);
        }

        @Test @DisplayName("Nivel cargado tiene celdas GOAL")
        void hasGoal() throws GameException {
            Level level = LevelLoader.load("recursos/nivel1.txt", "Nivel 1");
            boolean found = false;
            for (Cell[] row : level.getBoard().getCells())
                for (Cell c : row)
                    if (c.getType() == CellType.GOAL) { found = true; break; }
            assertTrue(found);
        }

        @Test @DisplayName("nivel2.txt carga correctamente")
        void loadLevel2() throws GameException {
            Level level = LevelLoader.load("recursos/nivel2.txt", "Nivel 2");
            assertNotNull(level);
            assertTrue(level.getBoard().getRows() > 0);
        }
    }

    // ══════════════════════════════════════════════
    //  GAMESAVE — cobertura adicional
    // ══════════════════════════════════════════════

    @Nested @DisplayName("GameSave — cobertura adicional")
    class GameSaveCoverage {

        @Test @DisplayName("Conserva tipo del jugador en snapshot")
        void preservesPlayerSnapshot() throws Exception {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            game.addPlayer(new BluePlayer("P1", 5 * GameConfig.CELL_SIZE + 3f,
                    4 * GameConfig.CELL_SIZE + 3f));
            new java.io.File("saves").mkdirs();
            game.saveGame("saves/cov_snapshot.dat", "recursos/nivel1.txt");
            GameSave save = Game.loadGame("saves/cov_snapshot.dat");
            assertEquals("Blue", save.getPlayerSnapshots().get(0).getType());
        }

        @Test @DisplayName("Conserva elementos especiales consumidos")
        void preservesConsumedElements() throws Exception {
            Level level = buildTestLevel();
            LifeSource ls = new LifeSource(new Position(4, 5));
            level.addSpecialElement(ls);
            ls.deactivated();
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            game.addPlayer(new RedPlayer("P1", 50f, 50f));
            new java.io.File("saves").mkdirs();
            game.saveGame("saves/cov_consumed.dat", "recursos/nivel1.txt");
            GameSave save = Game.loadGame("saves/cov_consumed.dat");
            assertEquals(1, save.getConsumedElementPositions().size());
        }

        @Test @DisplayName("getLevelFile del GameSave retorna la ruta del nivel")
        void getLevelFileFromSave() throws Exception {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            game.addPlayer(new RedPlayer("P1", 50f, 50f));
            new java.io.File("saves").mkdirs();
            game.saveGame("saves/cov_file.dat", "recursos/nivel1.txt");
            GameSave save = Game.loadGame("saves/cov_file.dat");
            assertEquals("recursos/nivel1.txt", save.getLevelFile());
        }
    }
}