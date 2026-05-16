package Dominio;

public class BluePlayer extends Player{

	public BluePlayer(String name, Position initialPosition) {
		super(name, initialPosition);
		speed=1.5;
		size=1.5;
	}

	@Override
	public void receiveHit() {
		die();
		
	}

}
