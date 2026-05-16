package Dominio;

public class VerticalSlider extends Enemy {

	public VerticalSlider(Position position) {
		super(position, 1.0, 1.0, new VerticalMovement());
	}

}
