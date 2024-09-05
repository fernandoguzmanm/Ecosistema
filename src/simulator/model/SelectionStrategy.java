package simulator.model;

import java.util.List;

//Elegir animales dentro de su campo visual
public interface SelectionStrategy {
	Animal select(Animal a, List<Animal> as);// si no hay animal devuelve null

}
