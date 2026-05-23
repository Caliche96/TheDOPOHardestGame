package Dominio;

import java.awt.Color;
import java.util.*;

/**
 * Clase que representa el juego.
 */
public class Game {

    private Level          currentLevel;
    private List<Player>   players;
    private GameMode       gameMode;
    private GameState      state;
    private int            remainingTime;
    private String         levelFile;

    /**
     * Constructor de la clase Game.
     * @param level El nivel del juego.
     * @param mode El modo de juego.
     */
    public Game(Level level, GameMode mode) {
        this.currentLevel  = level;
        this.gameMode      = mode;
        this.players       = new ArrayList<>();
        this.state         = new RunningState();
        this.remainingTime = level.getTimeLimit();
    }

    // ══════════════════════════════════════════════
    //  Métodos de Creación
    // ══════════════════════════════════════════════

    /**
     * Crea un nuevo juego con los parámetros especificados.
     * @param filePath Ruta del archivo del nivel.
     * @param levelName Nombre del nivel.
     * @param mode Modo de juego.
     * @return Una instancia de Game.
     * @throws GameException Si ocurre un error al crear el juego.
     */
    public static Game create(String filePath, String levelName, GameMode mode)
            throws GameException {
        Level level = LevelLoader.load(filePath, levelName);
        Game  game  = new Game(level, mode);
        game.levelFile = filePath;
        return game;
    }

    /**
     * Crea un juego a partir de un archivo de guardado.
     * @param path Ruta del archivo de guardado.
     * @return Una instancia de Game.
     * @throws GameException Si ocurre un error al crear el juego.
     */
    public static Game createFromSave(String path) throws GameException {
        GameSave save = loadGame(path);
        if (save == null)
            throw new GameException(GameException.SAVE_DATA_EMPTY);

        List<GameSave.PlayerSnapshot> snapshots = save.getPlayerSnapshots();
        if (snapshots == null || snapshots.isEmpty())
            throw new GameException(GameException.SAVE_DATA_NO_PLAYER);

        Level level = LevelLoader.load(save.getLevelFile(), "Nivel");
        Game  game  = new Game(level, save.getGameMode());
        game.levelFile = save.getLevelFile();

        game.setupPlayersFromSave(save);
        game.setRemainingTime(save.getRemainingTime());

        for (int[] pos : save.getCollectedCoinPositions()) {
            for (Coin c : level.getCoins()) {
                if (c.getPosition().getRow()    == pos[0] &&
                    c.getPosition().getColumn() == pos[1]) { c.collect(); break; }
            }
        }
        for (int[] pos : save.getConsumedElementPositions()) {
            for (SpecialElement el : level.getSpecialElements()) {
                if (el.getPosition().getRow()    == pos[0] &&
                    el.getPosition().getColumn() == pos[1]) { el.consume(); break; }
            }
        }
        return game;
    }

    // ══════════════════════════════════════════════
    //  CONFIGURACION DE JUGADORES
    // ══════════════════════════════════════════════

    /**
     * Crea jugadores con sus respectivos colores de borde.
     * borderColor2 se ignora en Single Player y PvM (máquina usa gris).
     */
    public void setupPlayers(String playerType,  Color borderColor1,
                              String player2Type, Color borderColor2,
                              String machineType) {
        int   cell   = GameConfig.CELL_SIZE;
        float size   = cell - 6f;
        float offset = (cell - size) / 2f;

        Position spawn1 = currentLevel.getDefaultSpawn();
        if (spawn1 == null) spawn1 = new Position(0, 0);
        float p1x = spawn1.getColumn() * cell + offset;
        float p1y = spawn1.getRow()    * cell + offset;

        addPlayer(buildPlayer(playerType, "Player 1", p1x, p1y, borderColor1));

        if (gameMode.isMultiplayer()) {
            Position spawn2 = findGoalSpawn(spawn1);
            float p2x = spawn2.getColumn() * cell + offset;
            float p2y = spawn2.getRow()    * cell + offset;

            if (gameMode == GameMode.PLAYER_VS_MACHINE) {
                MachineStrategy strategy = "Expert".equals(machineType)
                        ? new ExpertMachineStrategy()
                        : new RandomMachineStrategy();
                MachinePlayer machine = new MachinePlayer("Machine", p2x, p2y, strategy);
                machine.setBorderColor(Color.DARK_GRAY);
                addPlayer(machine);
            } else {
                String p2Type = (player2Type != null) ? player2Type : "Red";
                Color  p2Color = (borderColor2 != null) ? borderColor2 : Color.WHITE;
                addPlayer(buildPlayer(p2Type, "Player 2", p2x, p2y, p2Color));
            }
        }
    }

    /**
     * Construye un jugador con los parámetros especificados.
     * @param type Tipo de jugador.
     * @param name Nombre del jugador.
     * @param x Coordenada x de la posición.
     * @param y Coordenada y de la posición.
     * @param borderColor Color del borde del jugador.
     * @return Una instancia de Player.
     */
    private Player buildPlayer(String type, String name, float x, float y, Color borderColor) {
        Player p;
        switch (type) {
            case "Green": p = new GreenPlayer(name, x, y); break;
            case "Blue":  p = new BluePlayer(name, x, y);  break;
            default:      p = new RedPlayer(name, x, y);   break;
        }
        if (borderColor != null) p.setBorderColor(borderColor);
        return p;
    }

    /**
     * Encuentra la posición de spawn del objetivo.
     * @param p1Spawn Posición de spawn del Player 1.
     * @return La posición de spawn del objetivo o null si no se encuentra.
     */
    private Position findGoalSpawn(Position p1Spawn) {
        GameBoard board     = currentLevel.getBoard();
        int       targetRow = p1Spawn.getRow();
        for (int col = board.getColumns() - 1; col >= 0; col--) {
            if (board.getCell(targetRow, col).getType() == CellType.GOAL)
                return new Position(targetRow, col);
        }
        return new Position(targetRow, board.getColumns() - 1);
    }

    /**
     * Configura los jugadores a partir de un archivo de guardado.
     * @param save El archivo de guardado.
     */
    private void setupPlayersFromSave(GameSave save) {
        int   cell   = GameConfig.CELL_SIZE;
        float size   = cell - 6f;
        float offset = (cell - size) / 2f;

        for (GameSave.PlayerSnapshot snap : save.getPlayerSnapshots()) {
            float x = snap.getCol() * cell + offset;
            float y = snap.getRow() * cell + offset;
            Player p;
            if ("Machine".equals(snap.getType())) {
                p = new MachinePlayer("Machine", x, y, new RandomMachineStrategy());
                p.setBorderColor(Color.DARK_GRAY);
            } else {
                p = buildPlayer(snap.getType(), snap.getName(), x, y, Color.WHITE);
            }
            p.setDeaths(snap.getDeaths());
            p.setCollectedCoins(snap.getCollectedCoins());
            addPlayer(p);
        }
    }

    // ══════════════════════════════════════════════
    //  MÉTODOS PRINCIPALES
    // ══════════════════════════════════════════════

    /**
     * Actualiza el estado del juego.
     */
    public void update(){ 
        state.update(this);
    }
    
    /**
     * Mueve un jugador en la dirección especificada.
     * @param idx Índice del jugador.
     * @param dir Dirección a mover.
     */
    public void movePlayer(int idx, Direction dir){ 
        state.movePlayer(this, idx, dir); 
    }

    /**
     * Pausa el juego.
     */
    public void pause(){ 
        state.pause(this);                
    }

    /**
     * Reanuda el juego.
     */
    public void resume(){ 
        state.resume(this);
    }

    /**
     * Finaliza el juego.
     */
    public void finishGame(){ 
        state.finish(this);
    }

    /**
     * Hace que el juego termine con una victoria.
     */
    public void winGame(){ 
        setState(new WinState());
    }

    /**
     * Mueve un jugador internamente en la dirección especificada.
     * @param playerIndex Índice del jugador.
     * @param direccion Dirección a mover.
     */
    public void internalMovePlayer(int playerIndex, Direction direccion) {
        if (playerIndex < 0 || playerIndex >= players.size()) return;
        if (direccion == null) return;
        Player player = players.get(playerIndex);
        player.move(direccion, currentLevel.getBoard(), GameConfig.CELL_SIZE);
        checkSafeZone(player);
    }

    /**
     * Mueve los enemigos.
     */
    public void moveEnemies() {
        for (Enemy e : currentLevel.getEnemies()) e.update();
    }

    /**
     * Verifica las colisiones con los enemigos.
     */
    public void checkEnemyCollsion() {
        for (Player player : players) {
            int before = player.getDeaths();
            for (Enemy enemy : currentLevel.getEnemies())
                if (enemy.collides(player)) player.receiveHit();
            if (player.getDeaths() > before) restoreCoinsOnDeath(player);
        }
    }

    /**
     * Restaura las monedas cuando un jugador muere.
     * @param player El jugador que murió.
     */
    private void restoreCoinsOnDeath(Player player) {
        for (Coin coin : currentLevel.getCoins())
            if (coin.isCollected()) coin.restore();
        player.resetCoins();
    }

    /**
     * Verifica las colisiones con las monedas.
     */
    public void checkCoinCollision() {
        for (Player player : players)
            for (Coin coin : currentLevel.getCoins())
                if (!coin.isCollected() && coin.collides(player)) {
                    coin.collect(); coin.applyEffect(player); player.addCoin();
                }
    }

    /**
     * Verifica las colisiones con los elementos especiales.
     */
    public void checkSpecialElements() {
        for (SpecialElement el : currentLevel.getSpecialElements()) {
            if (!el.isActive()) continue;   // ignorar elementos ya consumidos
            for (Player player : players)
                if (el.collides(player)) el.applyEffect(this, player);
        }
    }

    /**
     * Verifica las colisiones entre jugadores.
     */
    public void checkPlayerCollisions() {
        if (gameMode == GameMode.SINGLE_PLAYER) return;
        for (int i = 0; i < players.size(); i++)
            for (int j = i + 1; j < players.size(); j++) {
                Player p1 = players.get(i), p2 = players.get(j);
                if (p1.collides(p2)) {
                    int d1 = p1.getDeaths(), d2 = p2.getDeaths();
                    p1.die(); p2.die();
                    if (p1.getDeaths() > d1) restoreCoinsOnDeath(p1);
                    if (p2.getDeaths() > d2) restoreCoinsOnDeath(p2);
                }
            }
    }

    /**
     * Verifica si un jugador está en una zona segura.
     * @param player El jugador a verificar.
     */
    public void checkSafeZone(Player player) {
        if (player.isInSafeZone(currentLevel.getBoard(), GameConfig.CELL_SIZE))
            player.setSpawnPoint(player.getX(), player.getY());
    }

    /**
     * Verifica si todos los jugadores han alcanzado la meta.
     */
    public void checkGoal() {
        if (!currentLevel.allCoinsCollected()) return;
        boolean allDone = true;
        for (int i = 0; i < players.size(); i++) {
            boolean done = (i == 0)
                    ? players.get(i).isInGoal(currentLevel.getBoard(), GameConfig.CELL_SIZE)
                    : players.get(i).isInSpawnZone(currentLevel.getBoard(), GameConfig.CELL_SIZE);
            if (!done) { allDone = false; }
        }
        if (allDone) winGame();
    }

    /**
     * Actualiza el temporizador del juego.
     */
    public void updateTimer() {
        if (--remainingTime <= 0) finishGame();
    }

    /**
     * Actualiza los jugadores máquina.
     */
    public void updateMachine() {
        for (Player p : players)
            if ("Machine".equals(p.getPlayerType()))
                ((MachinePlayer) p).update(this);
    }

    // ══════════════════════════════════════════════
    //  PERSISTENCIA
    // ══════════════════════════════════════════════

    /**
     * Guarda el estado del juego en un archivo.
     * @param path La ruta del archivo donde se guardará el juego.
     * @param filePath La ruta del archivo de configuración.
     * @throws GameException Si ocurre un error al guardar el juego.
     */
    public void saveGame(String path, String filePath) throws GameException {
        GameSave save = new GameSave(this, filePath);
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(
                new java.io.FileOutputStream(path))) {
            oos.writeObject(save);
        } catch (java.io.IOException e) {
            throw new GameException(GameException.SAVE_ERROR + ": " + e.getMessage());
        }
    }

    /**
     * Carga el estado del juego desde un archivo.
     * @param path La ruta del archivo desde el cual se cargará el juego.
     * @return El estado del juego cargado.
     * @throws GameException Si ocurre un error al cargar el juego.
     */
    public static GameSave loadGame(String path) throws GameException {
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.FileInputStream(path))) {
            return (GameSave) ois.readObject();
        } catch (java.io.FileNotFoundException e) {
            throw new GameException(GameException.LOAD_NOT_FOUND + ": " + path);
        } catch (java.io.InvalidClassException e) {
            throw new GameException(GameException.LOAD_INCOMPATIBLE + ": " + path);
        } catch (java.io.IOException e) {
            throw new GameException(GameException.LOAD_CORRUPT + ": " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new GameException(GameException.LOAD_UNKNOWN_FORMAT + ": " + path);
        }
    }
    
    // ══════════════════════════════════════════════
    //  MÉTODOS DE LAS BOMBAS
    // ══════════════════════════════════════════════

    /**
     * Verifica las colisiones entre bombas y enemigos.
     */
    public void checkBombEnemyCollision() {
        for (SpecialElement element : currentLevel.getSpecialElements()) {
            if (!element.isActive() || !element.isBomb()) continue;

            for (Enemy enemy : currentLevel.getEnemies()) {
                if (enemy.isActive() && bombCollidesEnemy(element, enemy)) {
                    enemy.destroy();
                    element.consume();
                }
            }
        }
    }

    /**
     * Verifica si una bomba colisiona con un enemigo.
     * @param element La bomba.
     * @param enemy El enemigo.
     * @return true si colisionan, false en caso contrario.
     */
    private boolean bombCollidesEnemy(SpecialElement element, Enemy enemy) {
        float bx = element.getPosition().getColumn() * GameConfig.CELL_SIZE;
        float by = element.getPosition().getRow() * GameConfig.CELL_SIZE;
        float bs = GameConfig.CELL_SIZE;

        return enemy.getX() < bx + bs &&
               enemy.getX() + enemy.getSize() > bx &&
               enemy.getY() < by + bs &&
               enemy.getY() + enemy.getSize() > by;
    }
    
    // ══════════════════════════════════════════════
    //  ACCESORES DEL TABLERO
    // ══════════════════════════════════════════════

    /**
     * Retorna el número de filas del tablero de juego.
     * @return El número de filas.
     */
    public int getBoardRows(){ 
        return currentLevel.getBoard().getRows();
    }

    /**
     * Retorna el número de columnas del tablero de juego.  
     * @return El número de columnas.
     */
    public int getBoardColumns(){ 
        return currentLevel.getBoard().getColumns(); 
    }

    /**
     * Retorna el arreglo 2D de celdas del tablero de juego.
     * @return El arreglo 2D de celdas.
     */
    public Cell[][] getCells(){ 
        return currentLevel.getBoard().getCells();
    }

    /**
     * Reetorna la celda en la posición especificada del tablero de juego.
     * @param row El índice de la fila.
     * @param col El índice de la columna.
     * @return La celda en la posición especificada.
     */
    public Cell getCell(int row, int col) { 
        return currentLevel.getBoard().getCell(row, col); 
    }

    /**
     * Retorna el tipo de la celda en la posición especificada del tablero de juego.
     * @param row El índice de la fila.
     * @param col El índice de la columna.
     * @return El tipo de la celda en la posición especificada.
     */
    public String getCellType(int row, int col) {
        return currentLevel.getBoard().getCell(row, col).getType().name();
    }

    /**
     * Mueve al jugador en la posición especificada según las direcciones dadas.
     * @param idx El índice del jugador.
     * @param up Indica si se mueve hacia arriba.
     * @param down Indica si se mueve hacia abajo.
     * @param left Indica si se mueve hacia la izquierda.
     * @param right Indica si se mueve hacia la derecha.
     */
    public void movePlayer(int idx, boolean up, boolean down, boolean left, boolean right) {
        Direction dir = null;
        if (up   && right) dir = Direction.UP_RIGHT;
        else if (up   && left)  dir = Direction.UP_LEFT;
        else if (down && right) dir = Direction.DOWN_RIGHT;
        else if (down && left)  dir = Direction.DOWN_LEFT;
        else if (up)            dir = Direction.UP;
        else if (down)          dir = Direction.DOWN;
        else if (left)          dir = Direction.LEFT;
        else if (right)         dir = Direction.RIGHT;
        if (dir != null) movePlayer(idx, dir);
    }

    // ══════════════════════════════════════════════
    //  ACCESORES DEL NIVEL
    // ══════════════════════════════════════════════

    /**
     * Retorna la lista de monedas en el nivel actual.
     * @return La lista de monedas.
     */
    public List<Coin> getCoins(){ 
        return currentLevel.getCoins();
    }

    /**
     * Retorna la lista de enemigos en el nivel actual.
     * @return La lista de enemigos.
     */
    public List<Enemy> getEnemies(){ 
        return currentLevel.getEnemies();
    }

    /**
     * Retorna la lista de elementos especiales en el nivel actual.
     * @return La lista de elementos especiales.
     */
    public List<SpecialElement> getSpecialElements() { 
        return currentLevel.getSpecialElements(); 
    }

    /**
     * Retorna el número total de monedas en el nivel actual.
     * @return El número total de monedas.
     */
    public int getTotalCoins(){ 
        return currentLevel.getCoins().size();
    }

    // ══════════════════════════════════════════════
    //  ACCESORES DE JUGADORES
    // ══════════════════════════════════════════════

    /**
     * Retorna el número de jugadores en el juego.
     * @return El número de jugadores.
     */
    public int getPlayerCount(){ 
        return players.size();
    }

    /**
     * Retorna la posición X del jugador en el índice especificado.
     * @param i El índice del jugador.
     * @return La posición X del jugador.
     */
    public float getPlayerX(int i){ 
        return players.get(i).getX();
    }

    /**
     * Retorna la posición Y del jugador en el índice especificado.
     * @param i El índice del jugador.
     * @return La posición Y del jugador.
     */
    public float getPlayerY(int i){ 
        return players.get(i).getY();
    }

    /**
     * Retorna el tamaño del jugador en el índice especificado.
     * @param i El índice del jugador.
     * @return El tamaño del jugador.
     */
    public float getPlayerSize(int i){ 
        return players.get(i).getSize();
    }

    /**
     * Retorna el número de muertes del jugador en el índice especificado.
     * @param i El índice del jugador.
     * @return El número de muertes del jugador.
     */
    public int getPlayerDeaths(int i){ 
        return players.get(i).getDeaths();
    }

    /**
     * Retorna el número de monedas recogidas por el jugador en el índice especificado.
     * @param i El índice del jugador.
     * @return El número de monedas recogidas por el jugador.
     */
    public String getPlayerType(int i){ 
        return players.get(i).getPlayerType();
    }

    /**
     * Retorna el color del borde del jugador en el índice especificado.
     * @param i El índice del jugador.
     * @return El color del borde del jugador.
     */
    public Color getPlayerBorderColor(int i){ 
        return players.get(i).getBorderColor();
    }

    /**
     * Retorna si el jugador en el índice especificado es una máquina.
     * @param i El índice del jugador.
     * @return true si el jugador es una máquina, false en caso contrario.
     */
    public boolean isPlayerMachine(int i){ 
        return "Machine".equals(getPlayerType(i));
    }

    /**
     * Retorna si el jugador en el índice especificado es invencible.
     * @param i El índice del jugador.
     * @return true si el jugador es invencible, false en caso contrario.
     */
    public boolean isPlayerInvincible(int i){ 
        return players.get(i).isInvincible();
    }

    /**
     * Retorna si el escudo del jugador en el índice especificado está activo.
     * @param i El índice del jugador.
     * @return true si el escudo está activo, false en caso contrario.
     */
    public boolean isPlayerShieldActive(int i){ 
        return players.get(i).isShieldActive();
    }

    /**
     * Llama a tick() en cada jugador para actualizar temporizadores internos
     * (ej. invencibilidad de GreenPlayer). Debe invocarse cada tick del game loop.
     */
    public void tickPlayers() {
        for (Player p : players) p.tick();
    }

    // ══════════════════════════════════════════════
    //  CONSULTAS DE ESTADO
    // ══════════════════════════════════════════════

    /**
     * Retorna si el juego está en ejecución.
     * @return true si el juego está en ejecución, false en caso contrario.
     */
    public boolean isRunning(){ 
        return state.isRunning();  
    }

    /**
     * Retorna si el juego está pausado.
     * @return true si el juego está pausado, false en caso contrario.
     */
    public boolean isPaused(){ 
        return state.isPaused();   
    }

    /**
     * Retorna si el juego ha terminado.
     * @return true si el juego ha terminado, false en caso contrario.
     */
    public boolean isGameOver(){ 
        return state.isGameOver(); 
    }

    /**
     * Retorna si el jugador ha ganado.
     * @return true si el jugador ha ganado, false en caso contrario.
     */
    public boolean isWin(){ 
        return state.isWin();      
    }

    // ══════════════════════════════════════════════
    //  CONSTANTES DE CONFIGURACIÓN
    // ══════════════════════════════════════════════

    /**
     * Retorna el tamaño de la celda.
     * @return El tamaño de la celda.
     */
    public static int getCellSize(){ 
        return GameConfig.CELL_SIZE;
    }

    /**
     * Retorna el límite de cuadros por segundo.
     * @return El límite de cuadros por segundo.
     */
    public static int getFps(){ 
        return GameConfig.FPS;
    }

    /**
     * Retorna el intervalo de movimiento de los enemigos.
     * @return El intervalo de movimiento de los enemigos.
     */
    public static int getEnemyMoveInterval(){ 
        return GameConfig.ENEMY_MOVE_INTERVAL; 
    }

    /**
     * Retorna el intervalo de movimiento del jugador.
     * @return El intervalo de movimiento del jugador.
     */
    public static int getPlayerMoveInterval(){ 
        return GameConfig.PLAYER_MOVE_INTERVAL;
    }

    // ══════════════════════════════════════════════
    //  CAMBIOS DE ESTADO Y GETTERS GENERALES
    // ══════════════════════════════════════════════

    /**
     * Establece el estado del juego.
     * @param state El nuevo estado del juego.
     */
    public void setState(GameState state){ 
        this.state = state;      
    }

    /**
     * Agrega un jugador a la lista de jugadores.
     * @param player El jugador a agregar.
     */
    public void addPlayer(Player player){ 
        players.add(player);      
    }

    /**
     * Establece el tiempo restante.
     * @param time El tiempo restante.
     */
    public void setRemainingTime(int time){ 
        this.remainingTime = time; 
    }

    /**
     * Retorna el nivel actual.
     * @return El nivel actual.
     */
    public Level getCurrentLevel(){ 
        return currentLevel;  
    }

    /**
     * Retorna la lista de jugadores.
     * @return La lista de jugadores.
     */
    public List<Player> getPlayers(){ 
        return players;
    }

    /**
     * Retorna el estado del juego.
     * @return El estado del juego.
     */
    public GameState getGameState() {
        return state;
    }

    /**
     * Retorna el tiempo restante.
     * @return El tiempo restante.
     */
    public int getRemainingTime() {
        return remainingTime;
    }

    /**
     * Retorna el modo de juego.
     * @return El modo de juego.
     */
    public GameMode getGameMode() {
        return gameMode;
    }

    /**
     * Retorna el archivo del nivel.
     * @return El archivo del nivel.
     */
    public String getLevelFile() {
        return levelFile;
    }

    /**
     * Retorna la cantidad de monedas en el nivel.
     * @return La cantidad de monedas en el nivel.
     */
    public int getCoinCount() {
        return currentLevel.getCoins().size();
    }

    /**
     * Retorna las monedas que tiene un jugador
     * @param playerIndex índice del jugador
     * @return numero de monedas que recogió el jugador
     */
    public int getPlayerCoins(int playerIndex) {
        return players.get(playerIndex).getCollectedCoins();
    }
    
    /**
     * Retorna si una moneda ha sido recogida.
     * @param index El índice de la moneda.
     * @return true si la moneda ha sido recogida, false en caso contrario.
     */
    public boolean isCoinCollected(int index) {
        return currentLevel.getCoins().get(index).isCollected();
    }

    /**
     * Retorna la fila de una moneda.
     * @param index El índice de la moneda.
     * @return La fila de la moneda.
     */
    public int getCoinRow(int index) {
        return currentLevel.getCoins().get(index).getPosition().getRow();
    }

    /**
     * Retorna la columna de una moneda.
     * @param index El índice de la moneda.
     * @return La columna de la moneda.
     */
    public int getCoinCol(int index) {
        return currentLevel.getCoins().get(index).getPosition().getColumn();
    }

    /**
     * Retorna si una moneda es una moneda de skin.
     * @param index El índice de la moneda.
     * @return true si la moneda es una moneda de skin, false en caso contrario.
     */
    public boolean isCoinSkin(int index) {
        return currentLevel.getCoins().get(index).isSkinCoin();
    }

    /**
     * Retorna la cantidad de elementos especiales en el nivel.
     * @return La cantidad de elementos especiales en el nivel.
     */
    public int getSpecialElementCount() {
        return currentLevel.getSpecialElements().size();
    }

    /**
     * Retorna si un elemento especial está activo.
     * @param index El índice del elemento especial.
     * @return true si el elemento especial está activo, false en caso contrario.
     */
    public boolean isSpecialElementActive(int index) {
        return currentLevel.getSpecialElements().get(index).isActive();
    }

    /**
     * Retorna la fila de un elemento especial.
     * @param index El índice del elemento especial.
     * @return La fila del elemento especial.
     */
    public int getSpecialElementRow(int index) {
        return currentLevel.getSpecialElements().get(index).getPosition().getRow();
    }

    /**
     * Retorna la columna de un elemento especial.
     * @param index El índice del elemento especial.
     * @return La columna del elemento especial.
     */
    public int getSpecialElementCol(int index) {
        return currentLevel.getSpecialElements().get(index).getPosition().getColumn();
    }

    /**
     * Retorna si un elemento especial es una bomba.
     * @param index El índice del elemento especial.
     * @return true si el elemento especial es una bomba, false en caso contrario.
     */
    public boolean isSpecialElementBomb(int index) {
        return currentLevel.getSpecialElements().get(index).isBomb();
    }

    /**
     * Retorna si un elemento especial es una fuente de vida.
     * @param index El índice del elemento especial.
     * @return true si el elemento especial es una fuente de vida, false en caso contrario.
     */
    public boolean isSpecialElementLifeSource(int index) {
        return currentLevel.getSpecialElements().get(index).isLifeSource();
    }

    /**
     * Retorna la cantidad de enemigos en el nivel.
     * @return La cantidad de enemigos en el nivel.
     */
    public int getEnemyCount() {
        return currentLevel.getEnemies().size();
    }

    /**
     * Retorna si un enemigo está activo.
     * @param index El índice del enemigo.
     * @return true si el enemigo está activo, false en caso contrario.
     */
    public boolean isEnemyActive(int index) {
        return currentLevel.getEnemies().get(index).isActive();
    }

    /**
     * Retorna la posición X de un enemigo.
     * @param index El índice del enemigo.
     * @return La posición X del enemigo.
     */
    public float getEnemyX(int index) {
        return currentLevel.getEnemies().get(index).getX();
    }

    /**
     * Retorna la posición Y de un enemigo.
     * @param index El índice del enemigo.
     * @return La posición Y del enemigo.
     */
    public float getEnemyY(int index) {
        return currentLevel.getEnemies().get(index).getY();
    }

    /**
     * Retorna el tamaño de un enemigo.
     * @param index El índice del enemigo.
     * @return El tamaño del enemigo.
     */
    public float getEnemySize(int index) {
        return currentLevel.getEnemies().get(index).getSize();
    }
}