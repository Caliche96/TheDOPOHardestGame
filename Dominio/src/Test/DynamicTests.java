package Test;

import Dominio.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas dinámicas — verifican comportamiento en ejecución:
 * movimiento, colisiones, monedas, estados y lógica de juego.
 */
@DisplayName("Pruebas Dinámicas")
public class DynamicTests {

    // ═══════════════════════════════════════════════════════
    //  UTILIDADES COMPARTIDAS
    // ═══════════════════════════════════════════════════════

    /**
     * Construye un tablero 10×20 con zona spawn, walkable y goal
     * que replica la estructura básica del nivel 1.
     */
    private GameBoard buildTestBoard() {
        GameBoard board = new GameBoard(10, 20);
        for (int r = 2; r <= 7; r++) {
            for (int c = 0; c <= 2; c++) board.setCell(r, c, CellType.SPAWN_ZONE);
            for (int c = 3; c <= 16; c++) board.setCell(r, c, CellType.WALKABLE);
            for (int c = 17; c <= 19; c++) board.setCell(r, c, CellType.GOAL);
        }
        return board;
    }

    /** Construye un Level de prueba con el tablero base. */
    private Level buildTestLevel() {
        GameBoard board = buildTestBoard();
        Level level = new Level("Test", board, 2700);
        level.setDefaultSpawn(new Position(4, 0));
        return level;
    }

    // ═══════════════════════════════════════════════════════
    //  PLAYER — MOVIMIENTO
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("Player — movimiento en píxeles")
    class PlayerMovementTests {

        @Test
        @DisplayName("Mover a la derecha incrementa X")
        void testMoveRight() {
            GameBoard board = buildTestBoard();
            int cell = GameConfig.CELL_SIZE;
            RedPlayer player = new RedPlayer("P1", 3 * cell + 3f, 4 * cell + 3f);
            float initialX = player.getX();
            player.move(Direction.RIGHT, board, cell);
            assertTrue(player.getX() > initialX);
        }

        @Test
        @DisplayName("Mover a la izquierda decrementa X")
        void testMoveLeft() {
            GameBoard board = buildTestBoard();
            int cell = GameConfig.CELL_SIZE;
            RedPlayer player = new RedPlayer("P1", 5 * cell + 3f, 4 * cell + 3f);
            float initialX = player.getX();
            player.move(Direction.LEFT, board, cell);
            assertTrue(player.getX() < initialX);
        }

        @Test
        @DisplayName("Mover arriba decrementa Y")
        void testMoveUp() {
            GameBoard board = buildTestBoard();
            int cell = GameConfig.CELL_SIZE;
            RedPlayer player = new RedPlayer("P1", 5 * cell + 3f, 5 * cell + 3f);
            float initialY = player.getY();
            player.move(Direction.UP, board, cell);
            assertTrue(player.getY() < initialY);
        }

        @Test
        @DisplayName("Mover abajo incrementa Y")
        void testMoveDown() {
            GameBoard board = buildTestBoard();
            int cell = GameConfig.CELL_SIZE;
            RedPlayer player = new RedPlayer("P1", 5 * cell + 3f, 3 * cell + 3f);
            float initialY = player.getY();
            player.move(Direction.DOWN, board, cell);
            assertTrue(player.getY() > initialY);
        }

        @Test
        @DisplayName("El jugador no atraviesa paredes (WALL bloquea)")
        void testWallBlocks() {
            GameBoard board = new GameBoard(5, 5);
            board.setCell(2, 2, CellType.WALKABLE);
            board.setCell(2, 3, CellType.WALL);
            int cell = GameConfig.CELL_SIZE;
            // Jugador pegado al borde derecho de celda (2,2)
            RedPlayer player = new RedPlayer("P1", 2 * cell + 3f, 2 * cell + 3f);
            float x0 = player.getX();
            // Intentar moverse a la derecha hacia la pared
            for (int i = 0; i < 30; i++) player.move(Direction.RIGHT, board, cell);
            assertTrue(player.getX() < 3 * cell, "No debe haber atravesado la pared en x=" + 3 * cell);
        }

        @Test
        @DisplayName("El jugador no sale por el borde izquierdo (x < 0)")
        void testLeftBorderBlocks() {
            GameBoard board = buildTestBoard();
            int cell = GameConfig.CELL_SIZE;
            RedPlayer player = new RedPlayer("P1", cell * 0 + 3f, cell * 4 + 3f);
            // Intentar ir muy a la izquierda
            for (int i = 0; i < 20; i++) player.move(Direction.LEFT, board, cell);
            assertTrue(player.getX() >= 0, "El jugador no debe tener X negativa");
        }

        @Test
        @DisplayName("Movimiento diagonal incrementa X e Y simultáneamente")
        void testDiagonalMove() {
            GameBoard board = buildTestBoard();
            int cell = GameConfig.CELL_SIZE;
            RedPlayer player = new RedPlayer("P1", 4 * cell + 3f, 4 * cell + 3f);
            float x0 = player.getX(), y0 = player.getY();
            player.move(Direction.DOWN_RIGHT, board, cell);
            assertTrue(player.getX() > x0);
            assertTrue(player.getY() > y0);
        }

        @Test
        @DisplayName("La velocidad diagonal es ~0.707 de la velocidad recta")
        void testDiagonalSpeed() {
            GameBoard board = buildTestBoard();
            int cell = GameConfig.CELL_SIZE;
            RedPlayer p1 = new RedPlayer("P1", 5 * cell + 3f, 4 * cell + 3f);
            RedPlayer p2 = new RedPlayer("P2", 5 * cell + 3f, 4 * cell + 3f);
            p1.move(Direction.RIGHT, board, cell);
            p2.move(Direction.DOWN_RIGHT, board, cell);
            // La componente X diagonal debe ser menor que la recta
            assertTrue(p2.getX() - (5 * cell + 3f) < p1.getX() - (5 * cell + 3f));
        }
    }

    // ═══════════════════════════════════════════════════════
    //  PLAYER — MUERTE Y RESPAWN
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("Player — muerte y respawn")
    class PlayerDeathTests {

        @Test
        @DisplayName("die() incrementa el contador de muertes")
        void testDieIncrementsDeaths() {
            RedPlayer player = new RedPlayer("P1", 50f, 50f);
            player.die();
            assertEquals(1, player.getDeaths());
            player.die();
            assertEquals(2, player.getDeaths());
        }

        @Test
        @DisplayName("die() hace respawn en el punto de spawn")
        void testDieRespawns() {
            RedPlayer player = new RedPlayer("P1", 50f, 50f);
            player.setX(300f);
            player.setY(300f);
            player.die();
            assertEquals(50f, player.getX(), 0.01f);
            assertEquals(50f, player.getY(), 0.01f);
        }

        @Test
        @DisplayName("setSpawnPoint actualiza el punto de reaparición")
        void testSetSpawnPoint() {
            RedPlayer player = new RedPlayer("P1", 50f, 50f);
            player.setSpawnPoint(100f, 150f);
            player.die();
            assertEquals(100f, player.getX(), 0.01f);
            assertEquals(150f, player.getY(), 0.01f);
        }

        @Test
        @DisplayName("GreenPlayer sobrevive el primer golpe")
        void testGreenPlayerShield() {
            GreenPlayer player = new GreenPlayer("P1", 50f, 50f);
            float xBefore = player.getX();
            player.receiveHit();
            // No debe morir — posición igual
            assertEquals(xBefore, player.getX(), 0.01f);
            assertEquals(0, player.getDeaths());
        }

        @Test
        @DisplayName("GreenPlayer muere en el segundo golpe")
        void testGreenPlayerDiesOnSecondHit() {
            GreenPlayer player = new GreenPlayer("P1", 50f, 50f);
            player.receiveHit(); // primer golpe — shield
            player.receiveHit(); // segundo golpe — muere
            assertEquals(1, player.getDeaths());
        }

        @Test
        @DisplayName("RedPlayer muere en el primer golpe")
        void testRedPlayerDiesOnFirstHit() {
            RedPlayer player = new RedPlayer("P1", 50f, 50f);
            player.receiveHit();
            assertEquals(1, player.getDeaths());
        }
    }

    // ═══════════════════════════════════════════════════════
    //  PLAYER — MONEDAS
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("Player — monedas")
    class PlayerCoinTests {

        @Test
        @DisplayName("addCoin incrementa el contador")
        void testAddCoin() {
            RedPlayer player = new RedPlayer("P1", 50f, 50f);
            player.addCoin();
            assertEquals(1, player.getCollectedCoins());
            player.addCoin();
            assertEquals(2, player.getCollectedCoins());
        }

        @Test
        @DisplayName("resetCoins pone el contador a cero")
        void testResetCoins() {
            RedPlayer player = new RedPlayer("P1", 50f, 50f);
            player.addCoin();
            player.addCoin();
            player.resetCoins();
            assertEquals(0, player.getCollectedCoins());
        }

        @Test
        @DisplayName("setCollectedCoins establece el valor directamente")
        void testSetCollectedCoins() {
            RedPlayer player = new RedPlayer("P1", 50f, 50f);
            player.setCollectedCoins(5);
            assertEquals(5, player.getCollectedCoins());
        }
    }

    // ═══════════════════════════════════════════════════════
    //  COLISIONES ENTRE JUGADORES
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("Colisiones — jugador vs jugador")
    class PlayerCollisionTests {

        @Test
        @DisplayName("Dos jugadores en la misma posición colisionan")
        void testPlayersCollide() {
            RedPlayer p1 = new RedPlayer("P1", 50f, 50f);
            RedPlayer p2 = new RedPlayer("P2", 50f, 50f);
            assertTrue(p1.collides(p2));
        }

        @Test
        @DisplayName("Jugadores alejados no colisionan")
        void testPlayersDontCollide() {
            RedPlayer p1 = new RedPlayer("P1", 50f, 50f);
            RedPlayer p2 = new RedPlayer("P2", 200f, 200f);
            assertFalse(p1.collides(p2));
        }

        @Test
        @DisplayName("Colisión parcial (superposición pequeña)")
        void testPlayersPartialCollision() {
            float size = GameConfig.CELL_SIZE - 6f;
            RedPlayer p1 = new RedPlayer("P1", 50f, 50f);
            RedPlayer p2 = new RedPlayer("P2", 50f + size - 1f, 50f);
            assertTrue(p1.collides(p2));
        }
    }

    // ═══════════════════════════════════════════════════════
    //  COLISIONES ENEMIGO vs JUGADOR
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("Colisiones — enemigo vs jugador")
    class EnemyPlayerCollisionTests {

        @Test
        @DisplayName("Enemigo colisiona con jugador en misma posición")
        void testEnemyCollidesPlayer() {
            float x = 100f, y = 100f;
            BasicBluePoint enemy = new BasicBluePoint(new Position(0, 0));
            enemy.setX(x);
            enemy.setY(y);
            RedPlayer player = new RedPlayer("P1", x, y);
            assertTrue(enemy.collides(player));
        }

        @Test
        @DisplayName("Enemigo no colisiona con jugador alejado")
        void testEnemyDoesNotCollide() {
            BasicBluePoint enemy = new BasicBluePoint(new Position(0, 0));
            enemy.setX(50f);
            enemy.setY(50f);
            RedPlayer player = new RedPlayer("P1", 300f, 300f);
            assertFalse(enemy.collides(player));
        }
    }

    // ═══════════════════════════════════════════════════════
    //  COLISIONES MONEDA vs JUGADOR
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("Colisiones — moneda vs jugador")
    class CoinCollisionTests {

        @Test
        @DisplayName("Jugador encima de la moneda la recoge (colisión)")
        void testCoinCollides() {
            int cell = GameConfig.CELL_SIZE;
            // Moneda en celda (4, 5)
            YellowCoin coin = new YellowCoin(new Position(4, 5));
            // Jugador centrado sobre la misma celda
            float px = 5 * cell + 3f;
            float py = 4 * cell + 3f;
            RedPlayer player = new RedPlayer("P1", px, py);
            assertTrue(coin.collides(player));
        }

        @Test
        @DisplayName("Jugador lejos de la moneda no colisiona")
        void testCoinDoesNotCollide() {
            YellowCoin coin = new YellowCoin(new Position(4, 5));
            RedPlayer player = new RedPlayer("P1", 0f, 0f);
            assertFalse(coin.collides(player));
        }
    }

    // ═══════════════════════════════════════════════════════
    //  GAME — LÓGICA PRINCIPAL
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("Game — lógica principal")
    class GameLogicTests {

        @Test
        @DisplayName("Game inicia en RunningState")
        void testGameStartsRunning() {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            assertInstanceOf(RunningState.class, game.getGameState());
        }

        @Test
        @DisplayName("pause() cambia el estado a PausedState")
        void testPause() {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            game.pause();
            assertInstanceOf(PausedState.class, game.getGameState());
        }

        @Test
        @DisplayName("resume() desde pausa vuelve a RunningState")
        void testResume() {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            game.pause();
            game.resume();
            assertInstanceOf(RunningState.class, game.getGameState());
        }

        @Test
        @DisplayName("finishGame() cambia el estado a GameOverState")
        void testFinishGame() {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            game.finishGame();
            assertInstanceOf(GameOverState.class, game.getGameState());
        }

        @Test
        @DisplayName("updateTimer() decrementa el tiempo")
        void testUpdateTimer() {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            int initialTime = game.getRemainingTime();
            game.updateTimer();
            assertEquals(initialTime - 1, game.getRemainingTime());
        }

        @Test
        @DisplayName("updateTimer() termina el juego cuando llega a 0")
        void testTimerGameOver() {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            game.setRemainingTime(1);
            game.updateTimer();
            assertInstanceOf(GameOverState.class, game.getGameState());
        }

        @Test
        @DisplayName("checkCoinCollision recoge moneda cuando el jugador la pisa")
        void testCoinCollection() {
            Level level = buildTestLevel();
            int cell = GameConfig.CELL_SIZE;
            Position coinPos = new Position(4, 5);
            level.addCoin(new YellowCoin(coinPos));

            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            RedPlayer player = new RedPlayer("P1", 5 * cell + 3f, 4 * cell + 3f);
            game.addPlayer(player);

            game.checkCoinCollision();

            assertTrue(level.getCoins().get(0).isCollected());
            assertEquals(1, player.getCollectedCoins());
        }

        @Test
        @DisplayName("checkEnemyCollision mata al jugador al tocar enemigo")
        void testEnemyKillsPlayer() {
            Level level = buildTestLevel();
            float x = 100f, y = 130f;

            BasicBluePoint enemy = new BasicBluePoint(new Position(0, 0));
            enemy.setX(x);
            enemy.setY(y);
            level.addEnemy(enemy);
            enemy.setBoard(level.getBoard());

            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            RedPlayer player = new RedPlayer("P1", x, y);
            game.addPlayer(player);

            game.checkEnemyCollsion();
            assertEquals(1, player.getDeaths());
        }

        @Test
        @DisplayName("checkGoal no termina si no se recogieron todas las monedas")
        void testGoalRequiresCoins() {
            GameBoard board = buildTestBoard();
            Level level = new Level("Test", board, 2700);
            level.addCoin(new YellowCoin(new Position(4, 5)));

            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            int cell = GameConfig.CELL_SIZE;
            // Jugador en el goal
            RedPlayer player = new RedPlayer("P1", 17 * cell + 3f, 4 * cell + 3f);
            game.addPlayer(player);

            game.checkGoal();
            assertInstanceOf(RunningState.class, game.getGameState());
        }

        @Test
        @DisplayName("movePlayer en PausedState no mueve al jugador")
        void testMovePlayerWhilePaused() {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            int cell = GameConfig.CELL_SIZE;
            RedPlayer player = new RedPlayer("P1", 5 * cell + 3f, 4 * cell + 3f);
            game.addPlayer(player);

            float x0 = player.getX();
            game.pause();
            game.movePlayer(0, Direction.RIGHT);

            assertEquals(x0, player.getX(), 0.01f, "No debe moverse en pausa");
        }

        @Test
        @DisplayName("checkPlayerCollisions en PvP mata a ambos jugadores")
        void testPvPCollision() {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.PLAYER_VS_PLAYER);
            float x = 100f, y = 100f;
            RedPlayer p1 = new RedPlayer("P1", x, y);
            RedPlayer p2 = new RedPlayer("P2", x, y);
            game.addPlayer(p1);
            game.addPlayer(p2);

            game.checkPlayerCollisions();

            assertEquals(1, p1.getDeaths());
            assertEquals(1, p2.getDeaths());
        }

        @Test
        @DisplayName("checkPlayerCollisions en SINGLE_PLAYER no hace nada")
        void testSinglePlayerNoCollision() {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            RedPlayer p1 = new RedPlayer("P1", 100f, 100f);
            game.addPlayer(p1);

            game.checkPlayerCollisions();
            assertEquals(0, p1.getDeaths());
        }
    }

    // ═══════════════════════════════════════════════════════
    //  ENEMY — MOVIMIENTO
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("Enemy — movimiento y rebote")
    class EnemyMovementTests {

        @Test
        @DisplayName("BasicBluePoint se mueve horizontalmente sin tablero no explota")
        void testEnemyNoBoard() {
            BasicBluePoint enemy = new BasicBluePoint(new Position(4, 5));
            float x0 = enemy.getX();
            enemy.update(); // board es null, no debe mover
            assertEquals(x0, enemy.getX(), 0.01f);
        }

        @Test
        @DisplayName("BasicBluePoint se mueve con tablero asignado")
        void testEnemyMovesWithBoard() {
            GameBoard board = buildTestBoard();
            BasicBluePoint enemy = new BasicBluePoint(new Position(4, 5));
            enemy.setBoard(board);
            float x0 = enemy.getX();
            enemy.update();
            assertNotEquals(x0, enemy.getX(), 0.001f);
        }

        @Test
        @DisplayName("Enemy rebota al llegar a una pared")
        void testEnemyBounce() {
            GameBoard board = new GameBoard(5, 10);
            for (int c = 2; c <= 7; c++) board.setCell(2, c, CellType.WALKABLE);

            BasicBluePoint enemy = new BasicBluePoint(new Position(2, 2));
            enemy.setBoard(board);
            float prevX = enemy.getX();

            // Mover hasta que choca y rebota
            for (int i = 0; i < 30; i++) enemy.update();

            // Debe haber rebotado — su dirección cambió en algún momento
            // Verificamos que no salió del rango de celdas walkable
            float maxX = 8 * GameConfig.CELL_SIZE;
            assertTrue(enemy.getX() <= maxX, "No debe salir del tablero");
        }
    }

    // ═══════════════════════════════════════════════════════
    //  LEVEL LOADER
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("LevelLoader — carga de niveles")
    class LevelLoaderTests {

        @Test
        @DisplayName("LevelLoader devuelve null si el archivo no existe")
        void testLoadNonExistentFile() {
            Level level = LevelLoader.load("recursos/noexiste.txt", "Test");
            assertNull(level);
        }

        @Test
        @DisplayName("LevelLoader carga nivel1.txt correctamente")
        void testLoadLevel1() {
            Level level = LevelLoader.load("recursos/nivel1.txt", "Nivel 1");
            assertNotNull(level);
            assertEquals("Nivel 1", level.getLevelName());
            assertNotNull(level.getBoard());
            assertTrue(level.getBoard().getRows() > 0);
            assertTrue(level.getBoard().getColumns() > 0);
        }

        @Test
        @DisplayName("LevelLoader asigna el board a los enemigos")
        void testLoadAssignsBoard() {
            Level level = LevelLoader.load("recursos/nivel1.txt", "Nivel 1");
            assertNotNull(level);
            for (Enemy e : level.getEnemies()) {
                assertNotNull(e.getBoard(), "Cada enemigo debe tener board asignado");
            }
        }

        @Test
        @DisplayName("LevelLoader establece defaultSpawn")
        void testLoadSetsDefaultSpawn() {
            Level level = LevelLoader.load("recursos/nivel1.txt", "Nivel 1");
            assertNotNull(level);
            assertNotNull(level.getDefaultSpawn());
        }
    }

    // ═══════════════════════════════════════════════════════
    //  GAME SAVE
    // ═══════════════════════════════════════════════════════

    @Nested
    @DisplayName("GameSave — persistencia")
    class GameSaveTests {

        @Test
        @DisplayName("saveGame y loadGame restauran el tiempo restante")
        void testSaveLoadRemainingTime() throws Exception {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            game.setRemainingTime(1500);
            RedPlayer player = new RedPlayer("P1", 50f, 50f);
            game.addPlayer(player);

            String path = "saves/test_save.dat";
            new java.io.File("saves").mkdirs();
            game.saveGame(path, "recursos/nivel1.txt");

            GameSave save = Game.loadGame(path);
            assertEquals(1500, save.getRemainingTime());
            assertEquals(GameMode.SINGLE_PLAYER, save.getGameMode());
        }

        @Test
        @DisplayName("saveGame guarda las monedas recogidas")
        void testSaveCollectedCoins() throws Exception {
            Level level = buildTestLevel();
            level.addCoin(new YellowCoin(new Position(4, 5)));
            level.getCoins().get(0).collect();

            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            RedPlayer player = new RedPlayer("P1", 50f, 50f);
            game.addPlayer(player);

            String path = "saves/test_coins.dat";
            new java.io.File("saves").mkdirs();
            game.saveGame(path, "recursos/nivel1.txt");

            GameSave save = Game.loadGame(path);
            assertEquals(1, save.getCollectedCoinPositions().size());
        }

        @Test
        @DisplayName("loadGame lanza GameException si el archivo no existe")
        void testLoadNonExistentSave() {
            assertThrows(GameException.class, () -> Game.loadGame("saves/no_existe.dat"));
        }
    }
}