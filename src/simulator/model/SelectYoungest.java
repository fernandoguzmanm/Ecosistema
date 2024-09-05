package simulator.model;

import java.util.List;

public class SelectYoungest implements SelectionStrategy {

	@Override
	public Animal select(Animal a, List<Animal> as) {
		Animal animal_youngest = null;
		double edad_min = Integer.MAX_VALUE;
		for (Animal animal : as) {
			if (animal.get_age() < edad_min) {
				animal_youngest = animal;
			}
		}
		return animal_youngest;
	}

}
