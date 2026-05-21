package Test;

import Dominio.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas estáticas — verifican estado inicial y construcción de objetos.
 * No ejecutan lógica de juego; solo comprueban que los objetos se crean
 * correctamente con sus valores por defecto.
 */
@DisplayName("Pruebas Estáticas")
public class StaticTests {

    // ═══════════════════════════════════════════════════════
    //  PLAYER
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("Player — estado inicial")
    class PlayerStaticTests {

        @Test
        @DisplayName("RedPlayer se crea con valores correctos")
        void testRedPlayerInitialState() {
            RedPlayer player = new RedPlayer("Player1", 52f, 55f);
            assertEquals("Player1", player.getName());
            assertEquals(52f, player.getX(), 0.01f);
            assertEquals(55f, player.getY(), 0.01f);
            assertEquals(2.0f, player.getSpeed(), 0.01f);
            assertEquals(GameConfig.CELL_SIZE - 6f, player.getSize(), 0.01f);
            assertEquals(0, player.getDeaths());
            assertEquals(0, player.getCollectedCoins());
            assertTrue(player.isAlive());
        }

        @Test
        @DisplayName("GreenPlayer se crea con shield inactivo")
        void testGreenPlayerInitialState() {
            GreenPlayer player = new GreenPlayer("Player2", 0f, 0f);
            assertEquals(2.0f, player.getSpeed(), 0.01f);
            assertEquals(0, player.getDeaths());
            assertTrue(player.isAlive());
        }

        @Test
        @DisplayName("BluePlayer tiene mayor velocidad y tamaño")
        void testBluePlayerInitialState() {
            BluePlayer player = new BluePlayer("Player3", 0f, 0f);
            assertEquals(3.0f, player.getSpeed(), 0.01f);
            assertEquals(GameConfig.CELL_SIZE - 4f, player.getSize(), 0.01f);
        }

        @Test
        @DisplayName("MachinePlayer se crea con estrategia Random")
        void testMachinePlayerRandomStrategy() {
            MachinePlayer machine = new MachinePlayer("Machine", 0f, 0f, new RandomMachineStrategy());
            assertNotNull(machine.getStrategy());
            assertInstanceOf(RandomMachineStrategy.class, machine.getStrategy());
        }

        @Test
        @DisplayName("MachinePlayer se crea con estrategia Expert")
        void testMachinePlayerExpertStrategy() {
            MachinePlayer machine = new MachinePlayer("Machine", 0f, 0f, new ExpertMachineStrategy());
            assertInstanceOf(ExpertMachineStrategy.class, machine.getStrategy());
        }

        @Test
        @DisplayName("El spawn point inicial es igual a la posición inicial")
        void testSpawnPointEqualsInitialPosition() {
            RedPlayer player = new RedPlayer("P1", 100f, 200f);
            // Al morir, regresa a la posición inicial
            player.die();
            assertEquals(100f, player.getX(), 0.01f);
            assertEquals(200f, player.getY(), 0.01f);
        }
    }

    // ═══════════════════════════════════════════════════════
    //  ENEMY
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("Enemy — estado inicial")
    class EnemyStaticTests {

        @Test
        @DisplayName("BasicBluePoint se crea activo con velocidad correcta")
        void testBasicBluePointInitialState() {
            BasicBluePoint enemy = new BasicBluePoint(new Position(4, 5));
            assertTrue(enemy.isActive());
            assertEquals(4.5f, enemy.getSpeed(), 0.01f);
            assertEquals(GameConfig.CELL_SIZE - 4f, enemy.getSize(), 0.01f);
        }

        @Test
        @DisplayName("VerticalSlider se crea activo")
        void testVerticalSliderInitialState() {
            VerticalSlider enemy = new VerticalSlider(new Position(3, 3));
            assertTrue(enemy.isActive());
            assertEquals(4.5f, enemy.getSpeed(), 0.01f);
        }

        @Test
        @DisplayName("AcceleratedEnemy tiene velocidad triple")
        void testAcceleratedEnemySpeed() {
            AcceleratedEnemy enemy = new AcceleratedEnemy(new Position(2, 2));
            assertEquals(9.0f, enemy.getSpeed(), 0.01f);
        }

        @Test
        @DisplayName("Enemy se crea sin tablero asignado")
        void testEnemyNoBoardInitially() {
            BasicBluePoint enemy = new BasicBluePoint(new Position(0, 0));
            assertNull(enemy.getBoard());
        }

        @Test
        @DisplayName("Enemy.destroy() lo desactiva")
        void testEnemyDestroy() {
            BasicBluePoint enemy = new BasicBluePoint(new Position(0, 0));
            enemy.destroy();
            assertFalse(enemy.isActive());
        }

        @Test
        @DisplayName("Constructor con Position convierte correctamente a píxeles")
        void testEnemyPositionConversion() {
            int cell = GameConfig.CELL_SIZE;
            BasicBluePoint enemy = new BasicBluePoint(new Position(3, 5));
            assertEquals(5 * cell, enemy.getX(), 0.01f);
            assertEquals(3 * cell, enemy.getY(), 0.01f);
        }
    }

    // ═══════════════════════════════════════════════════════
    //  GAMEBOARD
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("GameBoard — estado inicial")
    class GameBoardStaticTests {

        @Test
        @DisplayName("GameBoard se crea con dimensiones correctas")
        void testGameBoardDimensions() {
            GameBoard board = new GameBoard(10, 20);
            assertEquals(10, board.getRows());
            assertEquals(20, board.getColumns());
        }

        @Test
        @DisplayName("GameBoard inicializa todas las celdas como EMPTY")
        void testGameBoardInitialCells() {
            GameBoard board = new GameBoard(5, 5);
            for (int r = 0; r < 5; r++) {
                for (int c = 0; c < 5; c++) {
                    assertEquals(CellType.EMPTY, board.getCell(r, c).getType());
                }
            }
        }

        @Test
        @DisplayName("setCell cambia correctamente el tipo de celda")
        void testSetCell() {
            GameBoard board = new GameBoard(5, 5);
            board.setCell(2, 3, CellType.WALKABLE);
            assertEquals(CellType.WALKABLE, board.getCell(2, 3).getType());
        }

        @Test
        @DisplayName("isInside devuelve true para posiciones válidas")
        void testIsInsideValid() {
            GameBoard board = new GameBoard(10, 20);
            assertTrue(board.isInside(new Position(0, 0)));
            assertTrue(board.isInside(new Position(9, 19)));
            assertTrue(board.isInside(new Position(5, 10)));
        }

        @Test
        @DisplayName("isInside devuelve false para posiciones fuera del tablero")
        void testIsInsideInvalid() {
            GameBoard board = new GameBoard(10, 20);
            assertFalse(board.isInside(new Position(-1, 0)));
            assertFalse(board.isInside(new Position(0, -1)));
            assertFalse(board.isInside(new Position(10, 0)));
            assertFalse(board.isInside(new Position(0, 20)));
        }

        @Test
        @DisplayName("isWall devuelve true para celdas WALL")
        void testIsWall() {
            GameBoard board = new GameBoard(5, 5);
            board.setCell(1, 1, CellType.WALL);
            assertTrue(board.isWall(new Position(1, 1)));
            assertFalse(board.isWall(new Position(0, 0)));
        }

        @Test
        @DisplayName("isWall devuelve true fuera del tablero")
        void testIsWallOutOfBounds() {
            GameBoard board = new GameBoard(5, 5);
            assertTrue(board.isWall(new Position(-1, 0)));
            assertTrue(board.isWall(new Position(10, 10)));
        }
    }

    // ═══════════════════════════════════════════════════════
    //  LEVEL
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("Level — estado inicial")
    class LevelStaticTests {

        @Test
        @DisplayName("Level se crea con listas vacías")
        void testLevelInitialState() {
            GameBoard board = new GameBoard(10, 20);
            Level level = new Level("Nivel 1", board, 2700);
            assertEquals("Nivel 1", level.getLevelName());
            assertEquals(2700, level.getTimeLimit());
            assertTrue(level.getEnemies().isEmpty());
            assertTrue(level.getCoins().isEmpty());
            assertTrue(level.getSpecialElements().isEmpty());
        }

        @Test
        @DisplayName("addEnemy agrega enemigos correctamente")
        void testAddEnemy() {
            GameBoard board = new GameBoard(10, 20);
            Level level = new Level("Test", board, 2700);
            level.addEnemy(new BasicBluePoint(new Position(3, 5)));
            assertEquals(1, level.getEnemies().size());
        }

        @Test
        @DisplayName("addCoin agrega monedas correctamente")
        void testAddCoin() {
            GameBoard board = new GameBoard(10, 20);
            Level level = new Level("Test", board, 2700);
            level.addCoin(new YellowCoin(new Position(4, 6)));
            assertEquals(1, level.getCoins().size());
        }

        @Test
        @DisplayName("allCoinsCollected es true si no hay monedas")
        void testAllCoinsCollectedEmpty() {
            GameBoard board = new GameBoard(10, 20);
            Level level = new Level("Test", board, 2700);
            assertTrue(level.allCoinsCollected());
        }

        @Test
        @DisplayName("allCoinsCollected es false si hay monedas sin recoger")
        void testAllCoinsCollectedFalse() {
            GameBoard board = new GameBoard(10, 20);
            Level level = new Level("Test", board, 2700);
            level.addCoin(new YellowCoin(new Position(4, 6)));
            assertFalse(level.allCoinsCollected());
        }
    }

    // ═══════════════════════════════════════════════════════
    //  COIN
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("Coin — estado inicial")
    class CoinStaticTests {

        @Test
        @DisplayName("YellowCoin se crea no recogida")
        void testYellowCoinInitialState() {
            YellowCoin coin = new YellowCoin(new Position(3, 4));
            assertFalse(coin.isCollected());
            assertEquals(3, coin.getPosition().getRow());
            assertEquals(4, coin.getPosition().getColumn());
        }

        @Test
        @DisplayName("SkinCoin se crea no recogida")
        void testSkinCoinInitialState() {
            SkinCoin coin = new SkinCoin(new Position(2, 2));
            assertFalse(coin.isCollected());
        }

        @Test
        @DisplayName("collect() marca la moneda como recogida")
        void testCollect() {
            YellowCoin coin = new YellowCoin(new Position(1, 1));
            coin.collect();
            assertTrue(coin.isCollected());
        }
    }

    // ═══════════════════════════════════════════════════════
    //  SPECIAL ELEMENTS
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("SpecialElement — estado inicial")
    class SpecialElementStaticTests {

        @Test
        @DisplayName("Bomb se crea activa")
        void testBombInitialState() {
            Bomb bomb = new Bomb(new Position(5, 5));
            assertTrue(bomb.isActive());
            assertEquals(5, bomb.getPosition().getRow());
        }

        @Test
        @DisplayName("LifeSource se crea activa")
        void testLifeSourceInitialState() {
            LifeSource ls = new LifeSource(new Position(2, 3));
            assertTrue(ls.isActive());
        }

        @Test
        @DisplayName("consume() desactiva el elemento")
        void testConsume() {
            Bomb bomb = new Bomb(new Position(1, 1));
            bomb.consume();
            assertFalse(bomb.isActive());
        }

        @Test
        @DisplayName("deactivated() desactiva el elemento")
        void testDeactivated() {
            LifeSource ls = new LifeSource(new Position(1, 1));
            ls.deactivated();
            assertFalse(ls.isActive());
        }
    }

    // ═══════════════════════════════════════════════════════
    //  POSITION
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("Position — igualdad")
    class PositionStaticTests {

        @Test
        @DisplayName("Dos posiciones iguales son equals")
        void testPositionEquals() {
            Position p1 = new Position(3, 4);
            Position p2 = new Position(3, 4);
            assertEquals(p1, p2);
        }

        @Test
        @DisplayName("Dos posiciones distintas no son equals")
        void testPositionNotEquals() {
            Position p1 = new Position(3, 4);
            Position p2 = new Position(3, 5);
            assertNotEquals(p1, p2);
        }

        @Test
        @DisplayName("Position con sí mismo es equals")
        void testPositionSelf() {
            Position p = new Position(1, 1);
            assertEquals(p, p);
        }
    }

    // ═══════════════════════════════════════════════════════
    //  GAMECONFIG
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("GameConfig — constantes")
    class GameConfigStaticTests {

        @Test
        @DisplayName("CELL_SIZE es 26")
        void testCellSize() {
            assertEquals(26, GameConfig.CELL_SIZE);
        }

        @Test
        @DisplayName("FPS es 30")
        void testFPS() {
            assertEquals(30, GameConfig.FPS);
        }

        @Test
        @DisplayName("DEFAULT_TIME_LIMIT es 2700 ticks")
        void testTimeLimit() {
            assertEquals(2700, GameConfig.DEFAULT_TIME_LIMIT);
        }

        @Test
        @DisplayName("PLAYER_MOVE_INTERVAL es 3")
        void testPlayerMoveInterval() {
            assertEquals(3, GameConfig.PLAYER_MOVE_INTERVAL);
        }

        @Test
        @DisplayName("ENEMY_MOVE_INTERVAL es 4")
        void testEnemyMoveInterval() {
            assertEquals(4, GameConfig.ENEMY_MOVE_INTERVAL);
        }
    }

    // ═══════════════════════════════════════════════════════
    //  GAMEMODE
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("GameMode — comportamiento del enum")
    class GameModeStaticTests {

        @Test
        @DisplayName("SINGLE_PLAYER no es multijugador")
        void testSinglePlayerNotMultiplayer() {
            assertFalse(GameMode.SINGLE_PLAYER.isMultiplayer());
            assertTrue(GameMode.SINGLE_PLAYER.isSinglePlayer());
        }

        @Test
        @DisplayName("PLAYER_VS_PLAYER es multijugador")
        void testPvPIsMultiplayer() {
            assertTrue(GameMode.PLAYER_VS_PLAYER.isMultiplayer());
            assertFalse(GameMode.PLAYER_VS_PLAYER.hasMachine());
        }

        @Test
        @DisplayName("PLAYER_VS_MACHINE tiene máquina")
        void testPvMHasMachine() {
            assertTrue(GameMode.PLAYER_VS_MACHINE.isMultiplayer());
            assertTrue(GameMode.PLAYER_VS_MACHINE.hasMachine());
        }

        @Test
        @DisplayName("SINGLE_PLAYER tiene 1 jugador")
        void testSinglePlayerCount() {
            assertEquals(1, GameMode.SINGLE_PLAYER.getPlayers());
        }

        @Test
        @DisplayName("PvP tiene 2 jugadores")
        void testPvPPlayerCount() {
            assertEquals(2, GameMode.PLAYER_VS_PLAYER.getPlayers());
        }
    }
}