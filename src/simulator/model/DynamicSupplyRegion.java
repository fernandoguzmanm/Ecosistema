package simulator.model;

import simulator.misc.Utils;

public class DynamicSupplyRegion extends Region {
	public final static double PARAM1 = 60.0;
	public final static double PARAM2 = 5.0;
	public final static double PARAM3 = 2.0;
	private double _food;
	private double _growthFactor;

	// Constructora
	public DynamicSupplyRegion(double initialFood, double growthFactor) {
		super();
		this._food = initialFood;
		this._growthFactor = growthFactor;
	}

	// Implementaci�n del m�todo get_food
	public double get_food(Animal a, double dt) {
		if (a.get_diet() == Diet.CARNIVORE) {
			return 0.0;
		} else {
			int numHerbivores = this.getNumHervibores();
			double foodToProvide = Math.min(_food,
					PARAM1 * Math.exp(-Math.max(0, numHerbivores - PARAM2) * PARAM3) * dt);
			_food -= foodToProvide;
			return foodToProvide;
		}
	}

	// M�todo update
	public void update(double dt) {
		if (Utils._rand.nextDouble() < 0.5) {
			_food += dt * _growthFactor;
		}
	}

	public String toString() {
		return "Dynamic region";
	}
}
