package Dominio;

/**
 * Jugador rojo. Sin habilidad especial propia.
 * Puede recibir escudo temporal de una LifeSource.
 */
public class RedPlayer extends Player {

    private static final int IFRAMES = 45; // 1.5s a 30FPS
    private boolean shieldActive;
    private int     invincibilityTimer;

    /**
     * Crea un jugador rojo.
     * @param name El nombre del jugador.
     * @param spawnX La coordenada X del punto de aparición.
     * @param spawnY La coordenada Y del punto de aparición.
     */
    public RedPlayer(String name, float spawnX, float spawnY) {
        super(name, spawnX, spawnY, 2.0f, GameConfig.CELL_SIZE - 6f);
        this.shieldActive       = false;
        this.invincibilityTimer = 0;
    }

    /**
     * Actualiza el estado del jugador en cada tick.
     */
    @Override
    public void tick() {
        if (invincibilityTimer > 0) invincibilityTimer--;
    }

    /**
     * Recibe un golpe y actualiza el estado del jugador.
     */
    @Override
    public void receiveHit() {
        if (invincibilityTimer > 0) return;
        if (shieldActive) {
            shieldActive       = false;
            invincibilityTimer = IFRAMES;
        } else {
            die();
        }
    }

    /**
     * Activa el escudo del jugador.
     */
    @Override
    public void activateShield() {
        shieldActive       = true;
    }

    /**
     * Hace que el jugador muera.
     */
    @Override
    public void die() {
        super.die();
        shieldActive       = false;
    }

    /**
     * Verifica si el jugador es invencible.
     * @return true si el jugador es invencible, false en caso contrario.
     */
    @Override public boolean isInvincible(){ 
        return invincibilityTimer > 0; 
    }

    /**
     * Verifica si el escudo del jugador está activo.
     * @return true si el escudo está activo, false en caso contrario.
     */
    @Override public boolean isShieldActive(){ 
        return shieldActive; 
    }

    /**
     * Obtiene el tipo de jugador.
     * @return El tipo de jugador.
     */
    @Override public String  getPlayerType(){ 
        return "Red"; 
    }
}