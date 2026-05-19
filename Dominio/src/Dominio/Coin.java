package Dominio;

public abstract class Coin {
    protected Position position;
    protected boolean collected;

    /**
     * Constructor de la clase Coin
     * 
     * @param position Posición de la moneda
     */
    public Coin(Position position) {
        this.position = position;
        this.collected = false;
    }

    /**
     * Aplica el efecto de la moneda al jugador
     * 
     * @param player Jugador
     */
    public abstract void applyEffect(Player player);

    /**
     * Verifica si el jugador colisiona con la moneda
     * 
     * @param player Jugador
     * @return true si el jugador colisiona con la moneda, false en caso contrario
     */
    public boolean collides(Player player) {
        return position.equals(player.getPosition());
    }

    /**
     * Recolecta la moneda
     */
    public void collect() {
        collected = true;
    }

    /**
     * Verifica si la moneda ha sido recolectada
     * 
     * @return true si la moneda ha sido recolectada, false en caso contrario
     */
    public boolean isCollected() {
        return collected;
    }

    /**
     * Obtiene la posición de la moneda
     * 
     * @return Posición de la moneda
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Establece la posición de la moneda
     * 
     * @param position Nueva posición de la moneda
     */
    public void setPosition(Position position) {
        this.position = position;
    }
}