package Dominio;

import java.util.Random;

/**
 * Moneda especial que cambia las características del jugador.
 *
 * Al recogerla, el jugador adopta aleatoriamente las características
 * de otro tipo de jugador (velocidad y tamaño), manteniendo su borde
 * original como diferenciador visual.
 *
 * El efecto dura hasta que el jugador muere.
 */
public class SkinCoin extends Coin {

    private static final Random random = new Random();
    private static final String[] PLAYER_TYPES = { "Red", "Green", "Blue" };

    /**
     * Crea una moneda de piel.
     * @param position La posición de la moneda.
     */
    public SkinCoin(Position position) { super(position); }

    /**
     * Aplica el efecto de la moneda al jugador.
     * @param player El jugador.
     */
    @Override
    public void applyEffect(Player player) {
        // Elegir un tipo distinto al actual
        String current = player.getPlayerType();
        String newType;
        do {
            newType = PLAYER_TYPES[random.nextInt(PLAYER_TYPES.length)];
        } while (newType.equals(current));

        // Aplicar las características del nuevo tipo
        switch (newType) {
            case "Red":
                player.setSpeed(2.0f);
                player.setSize(GameConfig.CELL_SIZE - 6f);
                break;
            case "Green":
                player.setSpeed(2.0f);
                player.setSize(GameConfig.CELL_SIZE - 6f);
                break;
            case "Blue":
                player.setSpeed(7.0f);
                player.setSize(GameConfig.CELL_SIZE - 4f);
                break;
        }

        player.setSkinType(newType);
    }

    /**
     * Verifica si la moneda es de piel.
     * @return true si la moneda es de piel, false en caso contrario.
     */
    @Override public boolean isSkinCoin(){ 
        return true; 
    }
}