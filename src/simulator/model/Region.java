package simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

public abstract class Region implements Entity, FoodSupplier, RegionInfo {
	protected List<Animal> animalList;

	// Constructora y atributos necesarios
	public Region() {
		animalList = new ArrayList<Animal>();// Incializa la lista
	}

	final void add_animal(Animal a) {
		animalList.add(a);
	}

	final void remove_animal(Animal a) {
		animalList.remove(a);
	}

	final List<Animal> getAnimals() {
		return Collections.unmodifiableList(animalList);

	}

	public final JSONObject as_JSON() {
		JSONObject json = new JSONObject();
		List<JSONObject> animalJsonList = new ArrayList<>();

		for (Animal animal : animalList) {
			animalJsonList.add(animal.as_JSON());
		}

		json.put("animals", animalJsonList);
		return json;
	}

	public int getNumAnimals() {
		return animalList.size();
	}

	public int getNumHervibores() {
		int count = 0;
		for (Animal a : animalList) {
			if (a.get_diet() == Diet.HERBIVORE)
				count++;
		}
		return count;
	}

	public int getNumCarnivores() {
		int count = 0;
		for (Animal a : animalList) {
			if (a.get_diet() == Diet.CARNIVORE)
				count++;
		}
		return count;
	}

	public List<AnimalInfo> getAnimalsInfo() {
		return new ArrayList<>(animalList); // se puede usar Collections.unmodifiableList(_animals);
	}

}
