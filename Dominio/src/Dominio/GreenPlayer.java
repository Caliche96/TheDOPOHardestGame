package Dominio;

/**
 * Jugador verde (Clyde).
 *
 * Habilidad especial: absorbe el primer golpe sin morir.
 *  - Al recibir el primer golpe: escudo se consume, velocidad baja a HIT_SPEED.
 *  - Al recibir el segundo golpe: muere normalmente.
 *  - Al morir o usar el escudo: recibe IFRAMES ticks de invencibilidad
 *    para poder alejarse del enemigo antes de volver a ser vulnerable.
 *  - Al renacer: escudo y velocidad se restauran al estado inicial.
 */
public class GreenPlayer extends Player {

    // ── Constantes ──
    private static final float BASE_SPEED = 2.0f;   // velocidad normal
    private static final float HIT_SPEED  = 1.4f;   // velocidad tras absorber golpe
    private static final int   IFRAMES    = 45;      // 1.5 s a 30 FPS

    // ── Estado del escudo ──
    private boolean shieldActive;       // true = escudo disponible, false = ya usado
    private int     invincibilityTimer; // ticks restantes de invencibilidad

    /**
     * Constructor de la clase GreenPlayer.
     * @param name El nombre del jugador.
     * @param spawnX La coordenada X del punto de aparición.
     * @param spawnY La coordenada Y del punto de aparición.
     */
    public GreenPlayer(String name, float spawnX, float spawnY) {
        super(name, spawnX, spawnY, BASE_SPEED, GameConfig.CELL_SIZE - 6f);
        this.shieldActive       = true;
        this.invincibilityTimer = 0;
    }

    // ── Actualización por tick (decrementa invencibilidad) ──
    /**
     * Actualiza el estado del jugador por cada tick.
     */
    @Override
    public void tick() {
        if (invincibilityTimer > 0) invincibilityTimer--;
    }

    // ── Lógica de golpe ──

    /**
     * Recibe un golpe.
     */
    @Override
    public void receiveHit() {
        if (invincibilityTimer > 0) return;   // invencible → ignorar

        if (shieldActive) {
            // Primer golpe: activar efecto del escudo
            shieldActive       = false;
            speed              = HIT_SPEED;
            invincibilityTimer = IFRAMES;     // tiempo para alejarse del enemigo
        } else {
            // Segundo golpe: morir
            die();
        }
    }

    // ── Al morir: restaurar todo y dar invencibilidad breve al renacer ──

    /**
     * Hace que el jugador muera.
     */
    @Override
    public void die() {
        super.die();                           // deaths++, respawn al spawn point
        shieldActive       = true;             // escudo restaurado
        speed              = BASE_SPEED;       // velocidad restaurada
    }

    // ── Consultas de estado (para renderizado y fachada) ──

    /**
     * Activa el escudo del jugador.
     */
    @Override
    public void activateShield() {
        // Recarga el escudo incluso si ya estaba activo
        shieldActive       = true;
        speed              = BASE_SPEED;
        invincibilityTimer = IFRAMES;
    }

    /**
     * Indica si el jugador es invencible.
     * @return true si el jugador es invencible, false en caso contrario.
     */
    @Override public boolean isInvincible(){ 
        return invincibilityTimer > 0; 
    }

    /**
     * Indica si el escudo del jugador está activo.
     * @return true si el escudo está activo, false en caso contrario.
     */
    @Override public boolean isShieldActive(){ 
        return shieldActive; 
    }

    /**
     * Retorna el tipo de jugador.
     * @return El tipo de jugador.
     */
    @Override public String getPlayerType(){
        return "Green"; 
    }
}