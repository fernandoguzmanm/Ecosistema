package simulator.model;

import java.util.List;

public class SelectClosest implements SelectionStrategy {

	@Override
	public Animal select(Animal a, List<Animal> as) {
		Animal animal_closest = null;
		double distancia_min = Integer.MAX_VALUE;
		for (Animal animal : as) {
			if (animal.get_position().distanceTo(a.get_position()) < distancia_min) {
				distancia_min = animal.get_position().distanceTo(a.get_position());
				animal_closest = animal;
			}
		}
		return animal_closest;
	}

}
