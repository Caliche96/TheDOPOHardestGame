package Dominio;

public class LifeSource extends SpecialElement {

	public LifeSource(Position position) {
		super(position);
	}

	@Override
	public void applyEffect(Game game, Player player) {
		player.addLife();
		deactivated();
		
	}

}
