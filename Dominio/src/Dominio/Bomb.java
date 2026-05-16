package Dominio;

public class Bomb extends SpecialElement {

	public Bomb(Position position) {
		super(position);
	}

	@Override
	public void applyEffect(Game game, Player player) {
		player.die();
		deactivated();
		
	}

}
