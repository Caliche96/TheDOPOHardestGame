package Dominio;

public class GreenPlayer extends Player {
	
	private boolean shieldUse;

	public GreenPlayer(String name, Position initialPosition) {
		super(name, initialPosition);
		speed= 1.0;
		size=1.0;
		shieldUse=false;
	}

	@Override
	public void receiveHit() {
		if(!shieldUse) {
			shieldUse=true;
			speed=0.7;
		}
		else {
			die();
		}
	}

}
