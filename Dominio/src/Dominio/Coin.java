package Dominio;

/**
 * Clase abstracta que representa una moneda en el juego.
 */
public abstract class Coin {
    protected Position position;
    protected boolean collected;

    public Coin(Position position) {
        this.position = position;
        this.collected = false;
    }

    //Efecto de la moneda
    /**
     * Aplica el efecto de la moneda al jugador que la recoge.
     * @param player Jugador que recoge la moneda.
     */
    public abstract void applyEffect(Player player);
    
    //Colisiones
    /**
     * Verifica si la moneda colisiona con el jugador.
     * @param player Jugador con el que verificar colisión.
     * @return true si hay colisión, false en caso contrario.
     */
    public boolean collides(Player player) {
        // Centro de la moneda en píxeles
        float coinX = position.getColumn() * GameConfig.CELL_SIZE + GameConfig.CELL_SIZE / 2f;
        float coinY = position.getRow()    * GameConfig.CELL_SIZE + GameConfig.CELL_SIZE / 2f;
        float r = GameConfig.CELL_SIZE / 4f;
        // Colisión círculo vs AABB del jugador
        float nearX = Math.max(player.getX(), Math.min(coinX, player.getX() + player.getSize()));
        float nearY = Math.max(player.getY(), Math.min(coinY, player.getY() + player.getSize()));
        float dx = coinX - nearX;
        float dy = coinY - nearY;
        return (dx * dx + dy * dy) < (r * r);
    }
    
    /**
     * Estado de la moneda.
     */

    public void collect() {
        collected = true;
    }

    /**
     * Restaura la moneda.
     */
    public void restore() {
        collected = false;
    }

    /**
     * Verifica si la moneda ha sido recolectada.
     * @return true si la moneda ha sido recolectada, false en caso contrario.
     */
    public boolean isCollected() {
        return collected;
    }

    /**
     * Verifica si la moneda es una moneda de skin.
     * @return true si la moneda es una moneda de skin, false en caso contrario.
     */
    public boolean isSkinCoin() { return false; }

    //Getters

    /**
     * Obtiene la posición de la moneda.
     * @return La posición de la moneda.
     */
    public Position getPosition() { return position; }
}