package Dominio;

/**
 * Representa una bomba en el juego. Al activarse, mata al jugador que la activa y se desactiva.
 */
public class Bomb extends SpecialElement {

    public Bomb(Position position) { super(position); }

    @Override public void applyEffect(Game game, Player player) { player.die(); deactivated(); }
    @Override public boolean isBomb() { return true; }
}