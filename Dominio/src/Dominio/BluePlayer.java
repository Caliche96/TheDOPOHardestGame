package Dominio;

/**
 * Jugador azul. Más rápido y ligeramente más grande.
 * Puede recibir escudo temporal de una LifeSource.
 */
public class BluePlayer extends Player {

    private static final int IFRAMES = 45;
    private boolean shieldActive;
    private int     invincibilityTimer;


    /**
     * Crea un nuevo jugador azul.
     * @param name Nombre del jugador.
     * @param spawnX Coordenada X del punto de aparición.
     * @param spawnY Coordenada Y del punto de aparición.
     */
    public BluePlayer(String name, float spawnX, float spawnY) {
        super(name, spawnX, spawnY, 7.0f, GameConfig.CELL_SIZE - 4f);
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
     * Maneja el impacto recibido por el jugador.
     */
    @Override
    public void receiveHit() {
        if (invincibilityTimer > 0) return;
        if (shieldActive) {
            shieldActive= false;
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
     * Maneja la muerte del jugador.
     */
    @Override
    public void die() {
        super.die();
        shieldActive       = false;
    }

    /**
     * Consulta si el jugador es invencible.
     * @return true si el jugador es invencible, false en caso contrario.
     */
    @Override public boolean isInvincible(){ 
        return invincibilityTimer > 0; 
    }

    /**
     * Consulta si el jugador tiene el escudo activo.
     * @return true si el jugador tiene el escudo activo, false en caso contrario.
     */
    @Override public boolean isShieldActive(){
        return shieldActive; 
    }

    /**
     * Obtiene el tipo de jugador.
     * @return El tipo de jugador.
     */
    @Override public String  getPlayerType(){
        return "Blue"; 
    }
    
}