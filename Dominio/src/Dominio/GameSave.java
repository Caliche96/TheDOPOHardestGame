package Dominio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Snapshot serializable del estado del juego en un momento dado.
 * Se escribe/lee desde un archivo .dat mediante Game.saveGame() y Game.loadGame().
 *
 * Guarda:
 *  - Modo de juego
 *  - Nombre del archivo de nivel (.txt)
 *  - Tiempo restante
 *  - Estado de cada jugador (nombre, tipo, posición, muertes, monedas recogidas)
 *  - Monedas ya recogidas (sus posiciones)
 *  - Elementos especiales ya consumidos (sus posiciones)
 */
public class GameSave implements Serializable {

    private static final long serialVersionUID = 1L;

    // ──── Datos generales ────
    private GameMode gameMode;
    private String   levelFile;   // ej. "recursos/nivel1.txt"
    private int      remainingTime;

    // ──── Estado de jugadores ────
    private List<PlayerSnapshot> playerSnapshots;

    // ──── Monedas ya recogidas ────
    private List<int[]> collectedCoinPositions;   // cada int[] = {row, col}

    // ──── Elementos especiales ya consumidos ────
    private List<int[]> consumedElementPositions;

    // ═══════════════════════════════════════
    //  CONSTRUCTOR
    // ═══════════════════════════════════════

    /**
     * Constructor de la clase GameSave.
     * @param game El juego del cual se desea guardar el estado.
     * @param levelFile El nombre del archivo de nivel.
     */
    public GameSave(Game game, String levelFile) {
        this.gameMode      = game.getGameMode();
        this.levelFile     = levelFile;
        this.remainingTime = game.getRemainingTime();

        // ── Snapshot de jugadores ──
        playerSnapshots = new ArrayList<>();
        for (Player p : game.getPlayers()) {
            playerSnapshots.add(new PlayerSnapshot(p));
        }

        // ── Monedas recogidas ──
        collectedCoinPositions = new ArrayList<>();
        for (Coin c : game.getCurrentLevel().getCoins()) {
            if (c.isCollected()) {
                collectedCoinPositions.add(new int[]{
                        c.getPosition().getRow(),
                        c.getPosition().getColumn()
                });
            }
        }

        // ── Elementos consumidos ──
        consumedElementPositions = new ArrayList<>();
        for (SpecialElement el : game.getCurrentLevel().getSpecialElements()) {
            if (!el.isActive()) {
                consumedElementPositions.add(new int[]{
                        el.getPosition().getRow(),
                        el.getPosition().getColumn()
                });
            }
        }
    }

    // ═══════════════════════════════════════
    //  GETTERS
    // ═══════════════════════════════════════

    /**
     * Retorna el modo de juego.
     * @return El modo de juego.
     */
    public GameMode getGameMode(){ 
        return gameMode; 
    }

    /**
     * Retorna el nombre del archivo de nivel.
     * @return El nombre del archivo de nivel.
     */
    public String getLevelFile(){ 
        return levelFile; 
    }


    /**
     * Retorna el tiempo restante.
     * @return El tiempo restante.
     */
    public int getRemainingTime(){ 
        return remainingTime; 
    }

    /**
     * Retorna los snapshots de los jugadores.
     * @return Los snapshots de los jugadores.
     */
    public List<PlayerSnapshot> getPlayerSnapshots(){ 
        return playerSnapshots; 
    }

    /**
     * Retorna las posiciones de las monedas recogidas.
     * @return Las posiciones de las monedas recogidas.
     */
    public List<int[]> getCollectedCoinPositions(){ 
        return collectedCoinPositions; 
    }

    /**
     * Retorna las posiciones de los elementos especiales consumidos.
     * @return Las posiciones de los elementos especiales consumidos.
     */
    public List<int[]> getConsumedElementPositions(){ 
        return consumedElementPositions; 
    }

    // ═══════════════════════════════════════
    //  CLASE INTERNA: snapshot de un jugador
    // ═══════════════════════════════════════

    /**
     * Clase interna que representa un snapshot de un jugador.
     */
    public static class PlayerSnapshot implements Serializable {

        private static final long serialVersionUID = 1L;

        private String name;
        private String type;        // "Red", "Green", "Blue"
        private int    row;
        private int    col;
        private int    deaths;
        private int    collectedCoins;

        /**
         * Constructor de la clase PlayerSnapshot.
         * @param p El jugador del cual se toma el snapshot.
         */
        public PlayerSnapshot(Player p) {
            this.name           = p.getName();
            this.type           = resolveType(p);
            this.row            = (int)(p.getY() / GameConfig.CELL_SIZE);
            this.col            = (int)(p.getX() / GameConfig.CELL_SIZE);
            this.deaths         = p.getDeaths();
            this.collectedCoins = p.getCollectedCoins();
        }

        /**
         * Resuelve el tipo de jugador.
         * @param p El jugador.
         * @return El tipo de jugador.
         */
        private String resolveType(Player p) {
            return p.getPlayerType();
        }

        /**
         * Retorna el nombre del jugador.
         * @return El nombre del jugador.
         */
        public String getName(){ 
            return name; 
        }

        /**
         * Retorna el tipo de jugador.
         * @return El tipo de jugador.
         */
        public String getType(){ 
            return type; 
        }

        /**
         * Retorna la fila del jugador.
         * @return La fila del jugador.
         */
        public int getRow(){ 
            return row; 
        }

        /**
         * Retorna la columna del jugador.
         * @return La columna del jugador.
         */
        public int getCol(){ 
            return col; 
        }

        /**
         * Retorna el número de muertes del jugador.
         * @return El número de muertes del jugador.
         */
        public int getDeaths(){ 
            return deaths; 
        }

        /**
         * Retorna el número de monedas recogidas por el jugador.
         * @return El número de monedas recogidas por el jugador.
         */
        public int getCollectedCoins(){ 
            return collectedCoins; 
        }
    }
}