package Dominio;

public class RedPlayer extends Player{

	public RedPlayer(String name, Position initialPosition) {
		super(name, initialPosition);
		speed=1.0;
		size=1.0;
	}

	@Override
	public void receiveHit() {
		die();
		
	}

}
