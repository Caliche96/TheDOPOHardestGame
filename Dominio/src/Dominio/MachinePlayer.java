package Dominio;

/**
 * Clase que representa al jugador máquina en el juego.
 */
public class MachinePlayer extends Player {

    private final MachineStrategy strategy;

    /**
     * Constructor de la clase MachinePlayer.
     * @param name El nombre del jugador máquina.
     * @param spawnX La coordenada X de aparición.
     * @param spawnY La coordenada Y de aparición.
     * @param strategy La estrategia del jugador máquina.
     */
    public MachinePlayer(String name, float spawnX, float spawnY, MachineStrategy strategy) {
        super(name, spawnX, spawnY, 2.0f, GameConfig.CELL_SIZE - 6f);
        this.strategy = strategy;
    }

    /**
     * Actualiza el estado del jugador máquina.
     * @param game El juego actual.
     */
    public void update(Game game) {
        Direction direction = strategy.decideDirection(this, game);
        if (direction != null) {
            move(direction, game.getCurrentLevel().getBoard(), GameConfig.CELL_SIZE);
        }
    }

    /** Asigna el tablero directamente al jugador máquina para pruebas. */
    public void setBoard(GameBoard board, int cellSize) { }

    private static final int IFRAMES = 45;          // 1.5 s a 30 FPS
    private boolean shieldActive;
    private int     invincibilityTimer;

    /** Actualiza el temporizador de invencibilidad. */
    @Override public void tick() { 
        if (invincibilityTimer > 0) invincibilityTimer--; 
    }

    /** Recibe un golpe y actualiza el estado del jugador. */
    @Override
    public void receiveHit() {
        if (invincibilityTimer > 0) return;
        if (shieldActive) { shieldActive = false; invincibilityTimer = IFRAMES; }
        else die();
    }

    /** Activa el escudo del jugador. */
    @Override
    public void activateShield() { 
        shieldActive = true; invincibilityTimer = IFRAMES; 
    }

    /** Verifica si el jugador es invencible. */
    @Override public boolean isInvincible(){ 
        return invincibilityTimer > 0; 
    }

    /** Verifica si el escudo del jugador está activo. */
    @Override public boolean isShieldActive() { 
        return shieldActive; 
    }

    /** Obtiene el tipo de jugador. */
    @Override public String getPlayerType() {
        return "Machine"; 
    }

    /** Obtiene la estrategia del jugador máquina. */
    public MachineStrategy getStrategy(){ 
        return strategy; 
    }
}