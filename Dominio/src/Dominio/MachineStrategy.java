package Dominio;

/**
 * Estrategia de decisión para el jugador máquina.
 * Usa el patrón Strategy — cada implementación define
 * cómo elige la dirección en cada tick.
 */
public interface MachineStrategy {

    /**
     * Decide la siguiente dirección del jugador máquina.
     *
     * @param machine  jugador máquina (Player 2)
     * @param game     estado actual del juego
     * @return dirección a mover, o null si no debe moverse este tick
     */
    Direction decideDirection(Player machine, Game game);
}