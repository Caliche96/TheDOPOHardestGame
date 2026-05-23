package Dominio;

/**
 * Elemento especial: Fuente de vida.
 *
 * Al pisarla el jugador recibe un escudo temporal igual al de GreenPlayer:
 * absorbe el siguiente golpe sin morir y otorga invencibilidad breve.
 *
 * La LifeSource NO desaparece — permanece en el tablero para que
 * cualquier jugador pueda recargar el escudo en el mismo lugar.
 */
public class LifeSource extends SpecialElement {

    /**
     * Constructor de la clase LifeSource.
     * @param position La posición de la fuente de vida.
     */
    public LifeSource(Position position) { super(position); }

    @Override
    public void applyEffect(Game game, Player player) {
        // Reutiliza la lógica de escudo definida en cada subclase de Player
        player.activateShield();
        // No llama a deactivated() — la fuente permanece en el tablero
    }

    @Override public boolean isLifeSource() { return true; }
}