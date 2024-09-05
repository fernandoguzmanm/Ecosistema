package simulator.model;

import java.util.List;
import java.util.function.Predicate;

public interface AnimalMapView extends MapInfo, FoodSupplier {// Representa lo que un animal puede ver en el gestor de
																// regiones
	public List<Animal> get_animals_in_range(Animal e, Predicate<Animal> filter);

}