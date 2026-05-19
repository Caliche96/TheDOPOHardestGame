package Dominio;

import java.io.*;
import java.util.*;

public class GameSave implements Serializable {
    private static final long serialVersionUID = 1L;

    private GameMode gameMode;
    private String levelFile;
    private int remainingTime;
    private List<PlayerSnapshot> playerSnapshots;
    private List<int[]> collectedCoinPositions;
    private List<int[]> consumedElementPositions;

    public GameSave(Game game, String levelFile) {
        this.gameMode = game.getGameMode();
        this.levelFile = levelFile;
        this.remainingTime = game.getRemainingTime();
        playerSnapshots = new ArrayList<>();
        for (Player p : game.getPlayers()) {
            playerSnapshots.add(new PlayerSnapshot(p));
        }
        collectedCoinPositions = new ArrayList<>();
        for (Coin c : game.getCurrentLevel().getCoins()) {
            if (c.isCollected()) {
                collectedCoinPositions.add(new int[] {
                        c.getPosition().getRow(),
                        c.getPosition().getColumn()
                });
            }
        }

        consumedElementPositions = new ArrayList<>();
        for (SpecialElement el : game.getCurrentLevel().getSpecialElements()) {
            if (!el.isActive()) {
                consumedElementPositions.add(new int[] {
                        el.getPosition().getRow(),
                        el.getPosition().getColumn()
                });
            }
        }
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public String getLevelFile() {
        return levelFile;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public List<PlayerSnapshot> getPlayerSnapshots() {
        return playerSnapshots;
    }

    public List<int[]> getCollectedCoinPositions() {
        return collectedCoinPositions;
    }

    public List<int[]> getConsumedElementPositions() {
        return consumedElementPositions;
    }

    public static class PlayerSnapshot implements Serializable {

        private static final long serialVersionUID = 1L;
        private String name;
        private String type;
        private int row;
        private int col;

        private int deaths;

        private int collectedCoins;

        public PlayerSnapshot(Player p) {
            this.name = p.getName();
            this.type = resolveType(p);
            this.row = p.getPosition().getRow();
            this.col = p.getPosition().getColumn();
            this.deaths = p.getDeaths();
            this.collectedCoins = p.getCollectedCoins();
        }

        private String resolveType(Player p) {
            if (p instanceof GreenPlayer)
                return "Green";
            if (p instanceof BluePlayer)
                return "Blue";
            return "Red";
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        public int getDeaths() {
            return deaths;
        }

        public int getCollectedCoins() {
            return collectedCoins;
        }
    }
}
