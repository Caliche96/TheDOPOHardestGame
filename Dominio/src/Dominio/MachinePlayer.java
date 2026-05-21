package Dominio;

/**
 * Jugador controlado por la máquina.
 *
 * Extiende RedPlayer visualmente pero su movimiento
 * lo decide una MachineStrategy (Random o Expert).
 * En cada tick el GamePanel llama a update() para que
 * la máquina decida y ejecute su movimiento.
 */
public class MachinePlayer extends Player {

    private final MachineStrategy strategy;

    /**
     * @param name     nombre del jugador ("Machine")
     * @param spawnX   posición inicial X en píxeles
     * @param spawnY   posición inicial Y en píxeles
     * @param strategy estrategia de decisión (Random o Expert)
     */
    public MachinePlayer(String name, float spawnX, float spawnY, MachineStrategy strategy) {
        super(name, spawnX, spawnY, 2.0f, GameConfig.CELL_SIZE - 6f);
        this.strategy = strategy;
    }

    /**
     * Llama a la estrategia para decidir la dirección
     * y ejecuta el movimiento sobre el tablero.
     *
     * @param game instancia del juego para que la estrategia pueda leer el estado
     */
    public void update(Game game) {
        Direction direction = strategy.decideDirection(this, game);
        if (direction != null) {
            move(direction, game.getCurrentLevel().getBoard(), GameConfig.CELL_SIZE);
        }
    }

    @Override
    public void receiveHit() {
        die();
    }

    public MachineStrategy getStrategy() {
        return strategy;
    }
}