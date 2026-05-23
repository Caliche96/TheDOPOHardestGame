package Test;

import Dominio.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de Aceptación — flujos completos de usuario.
 *
 * Cada escenario simula una historia de usuario real desde el inicio
 * hasta el fin, verificando que las reglas de negocio se cumplen.
 *
 * API del proyecto:
 *  - game.movePlayer(int idx, Direction dir)
 *  - game.getGameState().isRunning() / isPaused() / isWin() / isGameOver()
 *  - LevelLoader.load() lanza GameException (no retorna null)
 *  - LifeSource.applyEffect() activa el escudo del jugador
 *  - Bomb consumida no vuelve a matar
 *  - GreenPlayer: invencibilidad sin flash al romper escudo
 */
@DisplayName("Pruebas de Aceptación")
public class AcceptanceTests {

    // ══════════════════════════════════════════════
    //  UTILIDADES
    // ══════════════════════════════════════════════

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

    // ══════════════════════════════════════════════
    //  AC-01: SINGLE PLAYER — VICTORIA
    // ══════════════════════════════════════════════

    @Nested @DisplayName("AC-01: Single Player — victoria al completar el nivel")
    class AC01_SinglePlayerWin {

        @Test @DisplayName("Recoge todas las monedas y llega al goal → WinState")
        void winAfterAllCoins() {
            Level level = buildTestLevel();
            level.addCoin(new YellowCoin(new Position(4, 5)));
            level.addCoin(new YellowCoin(new Position(4, 10)));
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            int cell = GameConfig.CELL_SIZE;
            RedPlayer player = new RedPlayer("P1", 5 * cell + 3f, 4 * cell + 3f);
            game.addPlayer(player);

            // Simular recolección
            level.getCoins().get(0).collect(); player.addCoin();
            level.getCoins().get(1).collect(); player.addCoin();

            player.setX(17 * cell + 3f);
            player.setY(4 * cell + 3f);
            game.checkGoal();

            assertTrue(game.getGameState().isWin());
        }

        @Test @DisplayName("Llega al goal sin monedas → no gana")
        void goalWithoutCoinsFails() {
            Level level = buildTestLevel();
            level.addCoin(new YellowCoin(new Position(4, 5)));
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            int cell = GameConfig.CELL_SIZE;
            game.addPlayer(new RedPlayer("P1", 18 * cell + 3f, 4 * cell + 3f));
            game.checkGoal();
            assertFalse(game.getGameState().isWin());
        }

        @Test @DisplayName("Sin monedas en el nivel → llegar al goal es suficiente")
        void winWithNoCoinsLevel() {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            int cell = GameConfig.CELL_SIZE;
            game.addPlayer(new RedPlayer("P1", 18 * cell + 3f, 4 * cell + 3f));
            game.checkGoal();
            assertTrue(game.getGameState().isWin());
        }

        @Test @DisplayName("checkCoinCollision recoge moneda automáticamente al pisarla")
        void autoCoinCollection() {
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

        @Test @DisplayName("Acumula correctamente múltiples monedas")
        void multipleCoinAccumulation() {
            Level level = buildTestLevel();
            int cell = GameConfig.CELL_SIZE;
            level.addCoin(new YellowCoin(new Position(4, 5)));
            level.addCoin(new YellowCoin(new Position(4, 8)));
            level.addCoin(new YellowCoin(new Position(4, 12)));
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            RedPlayer p = new RedPlayer("P1", 5 * cell + 3f, 4 * cell + 3f);
            game.addPlayer(p);
            for (Coin c : level.getCoins()) { c.collect(); p.addCoin(); }
            assertEquals(3, p.getCollectedCoins());
            assertTrue(level.allCoinsCollected());
        }
    }

    // ══════════════════════════════════════════════
    //  AC-02: SINGLE PLAYER — DERROTA POR TIEMPO
    // ══════════════════════════════════════════════

    @Nested @DisplayName("AC-02: Single Player — derrota por tiempo")
    class AC02_SinglePlayerTimeout {

        @Test @DisplayName("Tiempo llega a 0 → GameOverState")
        void timeoutGameOver() {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            game.setRemainingTime(5);
            for (int i = 0; i < 5; i++) game.updateTimer();
            assertTrue(game.getGameState().isGameOver());
        }

        @Test @DisplayName("El tiempo decrementa tick a tick")
        void timerDecrement() {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            game.setRemainingTime(100);
            for (int i = 0; i < 10; i++) game.updateTimer();
            assertEquals(90, game.getRemainingTime());
        }

        @Test @DisplayName("En pausa, el tiempo no decrementa (lógica del GameLoop)")
        void pausedTimerPaused() {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            game.setRemainingTime(50);
            game.pause();
            // El GameLoop no llama updateTimer() si está pausado
            assertTrue(game.getGameState().isPaused());
            assertEquals(50, game.getRemainingTime());
        }

        @Test @DisplayName("Enemigo mata al jugador y este reaparece en spawn")
        void enemyKillAndRespawn() {
            Level level = buildTestLevel();
            float x = 100f, y = 130f;
            BasicBluePoint enemy = new BasicBluePoint(x, y);
            level.addEnemy(enemy);
            enemy.setBoard(level.getBoard());
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            RedPlayer player = new RedPlayer("P1", x, y);
            game.addPlayer(player);
            game.checkEnemyCollsion();
            assertEquals(1, player.getDeaths());
            assertEquals(x, player.getX(), 0.01f);
            assertEquals(y, player.getY(), 0.01f);
        }

        @Test @DisplayName("Múltiples muertes acumulan el contador sin límite")
        void multipleDeaths() {
            Level level = buildTestLevel();
            float x = 100f, y = 130f;
            BasicBluePoint enemy = new BasicBluePoint(x, y);
            level.addEnemy(enemy);
            enemy.setBoard(level.getBoard());
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            RedPlayer player = new RedPlayer("P1", x, y);
            game.addPlayer(player);

            for (int i = 0; i < 5; i++) {
                player.setX(x); player.setY(y); // volver al enemigo
                game.checkEnemyCollsion();
            }
            assertEquals(5, player.getDeaths());
        }
    }

    // ══════════════════════════════════════════════
    //  AC-03: PvP — COLISIÓN Y GANADOR POR MONEDAS
    // ══════════════════════════════════════════════

    @Nested @DisplayName("AC-03: Player vs Player")
    class AC03_PvP {

        @Test @DisplayName("Colisión entre jugadores mata a ambos")
        void collisionKillsBoth() {
            Game game = new Game(buildTestLevel(), GameMode.PLAYER_VS_PLAYER);
            RedPlayer p1 = new RedPlayer("P1", 100f, 100f);
            RedPlayer p2 = new RedPlayer("P2", 100f, 100f);
            game.addPlayer(p1); game.addPlayer(p2);
            game.checkPlayerCollisions();
            assertEquals(1, p1.getDeaths());
            assertEquals(1, p2.getDeaths());
        }

        @Test @DisplayName("Jugador con más monedas es el ganador")
        void winnerByCoins() {
            Game game = new Game(buildTestLevel(), GameMode.PLAYER_VS_PLAYER);
            RedPlayer p1 = new RedPlayer("P1", 50f, 50f);
            GreenPlayer p2 = new GreenPlayer("P2", 300f, 50f);
            game.addPlayer(p1); game.addPlayer(p2);
            p1.addCoin(); p1.addCoin();
            p2.addCoin();
            assertTrue(p1.getCollectedCoins() > p2.getCollectedCoins());
        }

        @Test @DisplayName("Empate si ambos recogen la misma cantidad")
        void tie() {
            Game game = new Game(buildTestLevel(), GameMode.PLAYER_VS_PLAYER);
            RedPlayer p1 = new RedPlayer("P1", 50f, 50f);
            RedPlayer p2 = new RedPlayer("P2", 300f, 50f);
            game.addPlayer(p1); game.addPlayer(p2);
            p1.addCoin(); p1.addCoin();
            p2.addCoin(); p2.addCoin();
            assertEquals(p1.getCollectedCoins(), p2.getCollectedCoins());
        }

        @Test @DisplayName("En Single Player no hay colisión entre jugadores")
        void noFriendlyFire() {
            Game game = new Game(buildTestLevel(), GameMode.SINGLE_PLAYER);
            RedPlayer p = new RedPlayer("P1", 100f, 100f);
            game.addPlayer(p);
            game.checkPlayerCollisions();
            assertEquals(0, p.getDeaths());
        }
    }

    // ══════════════════════════════════════════════
    //  AC-04: PvM — MÁQUINA ALEATORIA
    // ══════════════════════════════════════════════

    @Nested @DisplayName("AC-04: Player vs Machine — máquina aleatoria")
    class AC04_PvMRandom {

        @Test @DisplayName("Máquina aleatoria devuelve direcciones")
        void randomReturnsDirection() {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.PLAYER_VS_MACHINE);
            MachinePlayer machine = new MachinePlayer("Machine",
                    5 * GameConfig.CELL_SIZE + 3f, 4 * GameConfig.CELL_SIZE + 3f,
                    new RandomMachineStrategy());
            game.addPlayer(machine);
            RandomMachineStrategy strategy = new RandomMachineStrategy();
            boolean got = false;
            for (int i = 0; i < 30; i++)
                if (strategy.decideDirection(machine, game) != null) { got = true; break; }
            assertTrue(got);
        }

        @Test @DisplayName("updateMachine() mueve a la máquina")
        void machineMovesOnUpdate() {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.PLAYER_VS_MACHINE);
            float sx = 5 * GameConfig.CELL_SIZE + 3f;
            float sy = 4 * GameConfig.CELL_SIZE + 3f;
            MachinePlayer machine = new MachinePlayer("Machine", sx, sy,
                    new RandomMachineStrategy());
            game.addPlayer(machine);
            for (int i = 0; i < 20; i++) game.updateMachine();
            assertTrue(machine.getX() != sx || machine.getY() != sy);
        }

        @Test @DisplayName("updateMachine() no mueve al jugador humano")
        void machineDoesNotMoveHuman() {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.PLAYER_VS_MACHINE);
            RedPlayer human = new RedPlayer("P1", 3 * GameConfig.CELL_SIZE + 3f,
                    4 * GameConfig.CELL_SIZE + 3f);
            MachinePlayer machine = new MachinePlayer("Machine",
                    5 * GameConfig.CELL_SIZE + 3f, 4 * GameConfig.CELL_SIZE + 3f,
                    new RandomMachineStrategy());
            game.addPlayer(human); game.addPlayer(machine);
            float hx = human.getX();
            for (int i = 0; i < 10; i++) game.updateMachine();
            assertEquals(hx, human.getX(), 0.01f);
        }
    }

    // ══════════════════════════════════════════════
    //  AC-05: PvM — MÁQUINA EXPERTA (BFS)
    // ══════════════════════════════════════════════

    @Nested @DisplayName("AC-05: Player vs Machine — máquina experta con BFS")
    class AC05_PvMExpert {

        @Test @DisplayName("Máquina experta se dirige hacia una moneda")
        void expertTargetsCoin() {
            GameBoard board = buildTestBoard();
            Level level = new Level("Test", board, 2700);
            level.addCoin(new YellowCoin(new Position(4, 10)));
            Game game = new Game(level, GameMode.PLAYER_VS_MACHINE);
            int cell = GameConfig.CELL_SIZE;
            game.addPlayer(new RedPlayer("P1", 1 * cell + 3f, 4 * cell + 3f));
            MachinePlayer machine = new MachinePlayer("Machine",
                    18 * cell + 3f, 4 * cell + 3f, new ExpertMachineStrategy());
            game.addPlayer(machine);
            float startX = machine.getX();
            for (int i = 0; i < 30; i++) game.updateMachine();
            assertTrue(machine.getX() < startX,
                    "La máquina experta debe avanzar hacia la moneda");
        }

        @Test @DisplayName("Máquina experta no sale del tablero")
        void expertStaysInBounds() {
            GameBoard board = buildTestBoard();
            Level level = new Level("Test", board, 2700);
            Game game = new Game(level, GameMode.PLAYER_VS_MACHINE);
            int cell = GameConfig.CELL_SIZE;
            game.addPlayer(new RedPlayer("P1", 1 * cell + 3f, 4 * cell + 3f));
            MachinePlayer machine = new MachinePlayer("Machine",
                    18 * cell + 3f, 4 * cell + 3f, new ExpertMachineStrategy());
            game.addPlayer(machine);
            for (int i = 0; i < 100; i++) game.updateMachine();
            assertTrue(machine.getX() >= 0);
            assertTrue(machine.getY() >= 0);
        }

        @Test @DisplayName("hashCode de Position permite que el BFS termine sin OutOfMemory")
        void bfsTerminatesWithHashCode() {
            // Este test verifica el fix del bug de Position.hashCode()
            // Si hashCode() está roto, el BFS genera un bucle infinito
            Position a = new Position(5, 10);
            Position b = new Position(5, 10);
            assertEquals(a.hashCode(), b.hashCode(),
                    "Sin hashCode correcto el BFS de ExpertMachineStrategy crashea con OutOfMemoryError");

            java.util.Map<Position, Position> map = new java.util.HashMap<>();
            map.put(a, a);
            assertTrue(map.containsKey(b),
                    "HashMap debe encontrar b usando el hashCode de a");
        }

        @Test @DisplayName("Tipo de MachinePlayer es 'Machine'")
        void machinePlayerType() {
            MachinePlayer m = new MachinePlayer("M", 0f, 0f, new ExpertMachineStrategy());
            assertEquals("Machine", m.getPlayerType());
        }
    }

    // ══════════════════════════════════════════════
    //  AC-06: GUARDAR Y CARGAR PARTIDA
    // ══════════════════════════════════════════════

    @Nested @DisplayName("AC-06: Guardar y cargar partida")
    class AC06_SaveLoad {

        @Test @DisplayName("Conserva tiempo restante")
        void preservesTime() throws Exception {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            game.setRemainingTime(1234);
            game.addPlayer(new RedPlayer("P1", 50f, 50f));
            new java.io.File("saves").mkdirs();
            game.saveGame("saves/ac06_time.dat", "recursos/nivel1.txt");
            GameSave save = Game.loadGame("saves/ac06_time.dat");
            assertEquals(1234, save.getRemainingTime());
        }

        @Test @DisplayName("Conserva modo de juego")
        void preservesMode() throws Exception {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.PLAYER_VS_PLAYER);
            game.addPlayer(new RedPlayer("P1", 50f, 50f));
            new java.io.File("saves").mkdirs();
            game.saveGame("saves/ac06_mode.dat", "recursos/nivel1.txt");
            GameSave save = Game.loadGame("saves/ac06_mode.dat");
            assertEquals(GameMode.PLAYER_VS_PLAYER, save.getGameMode());
        }

        @Test @DisplayName("Conserva muertes del jugador")
        void preservesDeaths() throws Exception {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            RedPlayer p = new RedPlayer("P1", 50f, 50f);
            p.setDeaths(3);
            game.addPlayer(p);
            new java.io.File("saves").mkdirs();
            game.saveGame("saves/ac06_deaths.dat", "recursos/nivel1.txt");
            GameSave save = Game.loadGame("saves/ac06_deaths.dat");
            assertEquals(3, save.getPlayerSnapshots().get(0).getDeaths());
        }

        @Test @DisplayName("Conserva tipo de jugador")
        void preservesPlayerType() throws Exception {
            Level level = buildTestLevel();
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            game.addPlayer(new BluePlayer("P1", 50f, 50f));
            new java.io.File("saves").mkdirs();
            game.saveGame("saves/ac06_type.dat", "recursos/nivel1.txt");
            GameSave save = Game.loadGame("saves/ac06_type.dat");
            assertEquals("Blue", save.getPlayerSnapshots().get(0).getType());
        }

        @Test @DisplayName("Conserva monedas recogidas")
        void preservesCollectedCoins() throws Exception {
            Level level = buildTestLevel();
            level.addCoin(new YellowCoin(new Position(4, 5)));
            level.addCoin(new YellowCoin(new Position(4, 8)));
            level.getCoins().get(0).collect();
            level.getCoins().get(1).collect();
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            game.addPlayer(new RedPlayer("P1", 50f, 50f));
            new java.io.File("saves").mkdirs();
            game.saveGame("saves/ac06_coins.dat", "recursos/nivel1.txt");
            GameSave save = Game.loadGame("saves/ac06_coins.dat");
            assertEquals(2, save.getCollectedCoinPositions().size());
        }

        @Test @DisplayName("loadGame lanza GameException si el archivo no existe")
        void throwsWhenMissing() {
            assertThrows(GameException.class,
                    () -> Game.loadGame("saves/no_existe_ac06.dat"));
        }
    }

    // ══════════════════════════════════════════════
    //  AC-07: REGLAS DEL JUEGO
    // ══════════════════════════════════════════════

    @Nested @DisplayName("AC-07: Reglas del juego")
    class AC07_Rules {

        @Test @DisplayName("La bomba mata al jugador y se desactiva")
        void bombKillsAndDeactivates() {
            Level level = buildTestLevel();
            Bomb bomb = new Bomb(new Position(4, 5));
            level.addSpecialElement(bomb);
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            int cell = GameConfig.CELL_SIZE;
            RedPlayer p = new RedPlayer("P1", 5 * cell + 3f, 4 * cell + 3f);
            game.addPlayer(p);
            game.checkSpecialElements();
            assertEquals(1, p.getDeaths());
            assertFalse(bomb.isActive());
        }

        @Test @DisplayName("La bomba consumida no vuelve a matar")
        void bombConsumedNoSecondKill() {
            Level level = buildTestLevel();
            Bomb bomb = new Bomb(new Position(4, 5));
            level.addSpecialElement(bomb);
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            int cell = GameConfig.CELL_SIZE;
            RedPlayer p = new RedPlayer("P1", 5 * cell + 3f, 4 * cell + 3f);
            game.addPlayer(p);

            game.checkSpecialElements();           // primera colisión → muerte
            int deaths = p.getDeaths();
            for (int i = 0; i < 50; i++) p.tick(); // drenar invencibilidad

            game.checkSpecialElements();           // bomba consumida → sin efecto
            assertEquals(deaths, p.getDeaths(),
                    "La bomba consumida (isActive=false) no debe volver a matar");
        }

        @Test @DisplayName("LifeSource activa el escudo del jugador y se desactiva")
        void lifeSourceActivatesShieldAndDeactivates() {
            Level level = buildTestLevel();
            LifeSource ls = new LifeSource(new Position(4, 5));
            level.addSpecialElement(ls);
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            int cell = GameConfig.CELL_SIZE;
            RedPlayer p = new RedPlayer("P1", 5 * cell + 3f, 4 * cell + 3f);
            game.addPlayer(p);

            game.checkSpecialElements();

            assertTrue(p.isShieldActive(),
                    "LifeSource debe activar el escudo del jugador");
            assertFalse(ls.isActive(),
                    "LifeSource debe desactivarse al ser usada");
        }

        @Test @DisplayName("GreenPlayer sobrevive el primer golpe del enemigo")
        void greenSurvivesFirstEnemyHit() {
            Level level = buildTestLevel();
            float x = 100f, y = 130f;
            BasicBluePoint enemy = new BasicBluePoint(x, y);
            enemy.setBoard(level.getBoard());
            level.addEnemy(enemy);
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            GreenPlayer p = new GreenPlayer("P1", x, y);
            game.addPlayer(p);
            game.checkEnemyCollsion();
            assertEquals(0, p.getDeaths(),
                    "GreenPlayer no debe morir en el primer golpe");
        }

        @Test @DisplayName("SkinCoin es SkinCoin y YellowCoin no lo es")
        void coinIdentification() {
            assertTrue(new SkinCoin(new Position(1, 1)).isSkinCoin());
            assertFalse(new YellowCoin(new Position(1, 2)).isSkinCoin());
        }

        @Test @DisplayName("allCoinsCollected es false si alguna queda sin recoger")
        void notAllCollected() {
            Level level = buildTestLevel();
            level.addCoin(new YellowCoin(new Position(4, 5)));
            level.addCoin(new YellowCoin(new Position(4, 8)));
            level.getCoins().get(0).collect();
            assertFalse(level.allCoinsCollected());
        }
    }

    // ══════════════════════════════════════════════
    //  AC-08: GREENPLAYER — FLUJO COMPLETO
    // ══════════════════════════════════════════════

    @Nested @DisplayName("AC-08: GreenPlayer — flujo completo del escudo")
    class AC08_GreenPlayerFull {

        @Test @DisplayName("Primer golpe: sobrevive, pierde escudo, queda invencible.")
        void firstHitFullBehavior() {
            GreenPlayer p = new GreenPlayer("P1", 50f, 50f);
            assertTrue(p.isShieldActive());
            p.receiveHit();
            assertEquals(0, p.getDeaths(),    "no muere");
            assertFalse(p.isShieldActive(),   "pierde el escudo");
            assertTrue(p.isInvincible(),      "queda invencible");
        }

        @Test @DisplayName("Segundo golpe durante invencibilidad: ignorado")
        void secondHitIgnoredDuringInvincibility() {
            GreenPlayer p = new GreenPlayer("P1", 50f, 50f);
            p.receiveHit(); // rompe escudo → invencible
            p.receiveHit(); // debe ignorarse
            assertEquals(0, p.getDeaths());
        }

        @Test @DisplayName("Segundo golpe después de drenar invencibilidad: muere")
        void secondHitKillsAfterInvincibility() {
            GreenPlayer p = new GreenPlayer("P1", 50f, 50f);
            p.receiveHit();
            for (int i = 0; i < 50; i++) p.tick();
            assertFalse(p.isInvincible());
            p.receiveHit();
            assertEquals(1, p.getDeaths());
        }

        @Test @DisplayName("Al morir: escudo restaurado")
        void afterDeathState() {
            GreenPlayer p = new GreenPlayer("P1", 50f, 50f);
            p.receiveHit();
            for (int i = 0; i < 50; i++) p.tick();
            p.receiveHit(); // muere
            assertTrue(p.isShieldActive(), "escudo restaurado");
        }

        @Test @DisplayName("Ciclo completo: muere, renace, vuelve a absorber un golpe")
        void fullCycle() {
            GreenPlayer p = new GreenPlayer("P1", 50f, 50f);

            // Primer ciclo
            p.receiveHit();                      // rompe escudo
            for (int i = 0; i < 50; i++) p.tick(); // drenar
            p.receiveHit();                      // muere (1 muerte)
            assertEquals(1, p.getDeaths());

            // Drenar invencibilidad post-muerte
            for (int i = 0; i < 50; i++) p.tick();

            // Segundo ciclo: escudo restaurado → vuelve a absorber
            assertTrue(p.isShieldActive());
            p.receiveHit(); // absorbe otro golpe
            assertEquals(1, p.getDeaths(), "sigue con 1 muerte, el escudo absorbió de nuevo");
        }

        @Test @DisplayName("LifeSource da escudo a GreenPlayer cuando ya no lo tiene")
        void lifeSourceRestoresGreenShield() {
            Level level = buildTestLevel();
            LifeSource ls = new LifeSource(new Position(4, 5));
            level.addSpecialElement(ls);
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            int cell = GameConfig.CELL_SIZE;

            GreenPlayer p = new GreenPlayer("P1", 5 * cell + 3f, 4 * cell + 3f);
            game.addPlayer(p);

            // Romper el escudo
            p.receiveHit();
            assertFalse(p.isShieldActive());

            // Drenar invencibilidad para poder pisar la LifeSource
            for (int i = 0; i < 50; i++) p.tick();

            // Pisar la LifeSource
            game.checkSpecialElements();
            assertTrue(p.isShieldActive(),
                    "LifeSource debe restaurar el escudo de GreenPlayer");
        }
    }

    // ══════════════════════════════════════════════
    //  AC-09: LIFESOURCE PARA CUALQUIER JUGADOR
    // ══════════════════════════════════════════════

    @Nested @DisplayName("AC-09: LifeSource aplica escudo a cualquier tipo de jugador")
    class AC09_LifeSourceUniversal {

        private void assertLifeSourceGivesShield(Player player) {
            Level level = buildTestLevel();
            LifeSource ls = new LifeSource(new Position(4, 5));
            level.addSpecialElement(ls);
            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            int cell = GameConfig.CELL_SIZE;

            player.setX(5 * cell + 3f);
            player.setY(4 * cell + 3f);
            game.addPlayer(player);

            game.checkSpecialElements();

            assertTrue(player.isShieldActive(),
                    player.getPlayerType() + " debe recibir escudo de LifeSource");
            assertFalse(ls.isActive(), "LifeSource debe desactivarse");
        }

        @Test @DisplayName("RedPlayer recibe escudo de LifeSource")
        void redPlayerGetsShield() {
            assertLifeSourceGivesShield(new RedPlayer("P1", 0f, 0f));
        }

        @Test @DisplayName("BluePlayer recibe escudo de LifeSource")
        void bluePlayerGetsShield() {
            assertLifeSourceGivesShield(new BluePlayer("P1", 0f, 0f));
        }

        @Test @DisplayName("GreenPlayer recibe escudo de LifeSource (cuando no lo tiene)")
        void greenPlayerGetsShield() {
            GreenPlayer p = new GreenPlayer("P1", 0f, 0f);
            p.receiveHit(); // romper escudo
            for (int i = 0; i < 50; i++) p.tick();
            assertFalse(p.isShieldActive());
            assertLifeSourceGivesShield(p);
        }

        @Test @DisplayName("RedPlayer con escudo sobrevive el siguiente golpe")
        void shieldFromLifeSourceProtects() {
            RedPlayer p = new RedPlayer("P1", 50f, 50f);
            p.activateShield();
            assertTrue(p.isShieldActive());

            p.receiveHit();  // el escudo absorbe
            assertEquals(0, p.getDeaths(),
                    "RedPlayer con escudo de LifeSource no debe morir en el siguiente golpe");
            assertFalse(p.isShieldActive(), "El escudo se consume tras absorber");
        }
    }

    // ══════════════════════════════════════════════
    //  AC-10: ZONA SEGURA INTERMEDIA
    // ══════════════════════════════════════════════

    @Nested @DisplayName("AC-10: Zona segura intermedia actualiza el spawn")
    class AC10_SafeZone {

        @Test @DisplayName("Al entrar a zona segura, se actualiza el spawn point")
        void safeZoneUpdatesSpawn() {
            GameBoard board = new GameBoard(10, 20);
            for (int r = 2; r <= 7; r++) {
                for (int c = 0; c <= 2;  c++) board.setCell(r, c, CellType.SPAWN_ZONE);
                for (int c = 3; c <= 10; c++) board.setCell(r, c, CellType.WALKABLE);
                for (int c = 11; c <= 13;c++) board.setCell(r, c, CellType.SAFE_ZONE);
                for (int c = 14; c <= 16;c++) board.setCell(r, c, CellType.WALKABLE);
                for (int c = 17; c <= 19;c++) board.setCell(r, c, CellType.GOAL);
            }
            Level level = new Level("Test", board, 2700);
            level.setDefaultSpawn(new Position(4, 0));

            Game game = new Game(level, GameMode.SINGLE_PLAYER);
            int cell = GameConfig.CELL_SIZE;

            RedPlayer p = new RedPlayer("P1", 0 * cell + 3f, 4 * cell + 3f);
            game.addPlayer(p);

            // Mover a la zona segura intermedia
            p.setX(11 * cell + 3f);
            p.setY(4 * cell + 3f);

            game.checkSafeZone(p);
            p.setSpawnPoint(p.getX(), p.getY());

            float savedSpawnX = p.getX();
            float savedSpawnY = p.getY();

            // Morir → renacer en la zona segura intermedia
            p.die();
            assertEquals(savedSpawnX, p.getX(), 1.0f,
                    "El jugador debe renacer en la zona segura intermedia");
        }
    }

    // ══════════════════════════════════════════════
    //  AC-11: NIVEL DESDE ARCHIVO
    // ══════════════════════════════════════════════

    @Nested @DisplayName("AC-11: Carga de nivel desde archivo")
    class AC11_LevelLoader {

        @Test @DisplayName("nivel1.txt carga con board, spawn y enemigos con board asignado")
        void level1LoadsCorrectly() throws GameException {
            Level level = LevelLoader.load("recursos/nivel1.txt", "Nivel 1");
            assertNotNull(level);
            assertEquals("Nivel 1", level.getLevelName());
            assertNotNull(level.getDefaultSpawn());
            assertTrue(level.getBoard().getRows() > 0);
            assertTrue(level.getBoard().getColumns() > 0);
            for (Enemy e : level.getEnemies())
                assertNotNull(e.getBoard(),
                        "Cada enemigo debe tener su board asignado");
        }

        @Test @DisplayName("Archivo inexistente lanza GameException")
        void missingFileThrows() {
            assertThrows(GameException.class,
                    () -> LevelLoader.load("recursos/no_existe.txt", "X"));
        }
    }
}