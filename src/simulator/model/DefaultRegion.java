package simulator.model;

public class DefaultRegion extends Region {
	public final static double PARAM1 = 60.0;
	public final static double PARAM2 = 5.0;
	public final static double PARAM3 = 2.0;

	// Constructora por defecto
	public DefaultRegion() {
		super();
	}

	// Implementaci�n del m�todo get_food
	public double get_food(Animal a, double dt) {
		double food;
		if (a.get_diet() == Diet.CARNIVORE) {
			food = 0.0;
		} else {
			int numHerbivores = this.getNumHervibores();
			food = PARAM1 * Math.exp(-Math.max(0, numHerbivores - PARAM2) * PARAM3) * dt;
		}
		return food;
	}

	@Override
	public void update(double dt) {
		// Not do
	}

	public String toString() {
		return "Default region";
	}

}
