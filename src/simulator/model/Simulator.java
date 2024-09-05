package simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

import simulator.factories.Factory;

public class Simulator implements JSONable, Observable<EcoSysObserver> {

	private RegionManager regionManager;
	private List<Animal> animals;
	private List<EcoSysObserver> observadores;
	private double time;
	private Factory<Animal> animalsFactory;
	private Factory<Region> regionsFactory;

	// Constructora
	public Simulator(int cols, int rows, int width, int height, Factory<Animal> animalsFactory,
			Factory<Region> regionsFactory) {
		this.animalsFactory = animalsFactory;
		this.regionsFactory = regionsFactory;
		regionManager = new RegionManager(cols, rows, width, height);
		animals = new ArrayList<Animal>();
		time = 0.0;
		observadores = new ArrayList<EcoSysObserver>();
	}

	// M�todos necesarios
	void set_region(int row, int col, Region r) {
		regionManager.set_region(row, col, r);
	}

	public void set_region(int row, int col, JSONObject r_json) {
		Region r = regionsFactory.create_instance(r_json);
		set_region(row, col, r);
		notify_on_SetRegion(row, col, r);
	}

	private void add_animal(Animal a) {
		animals.add(a);
		regionManager.register_animal(a);

	}

	public void add_animal(JSONObject a_json) {
		Animal a = animalsFactory.create_instance(a_json);
		add_animal(a);
		notify_on_AddAnimal(a);
	}

	public MapInfo get_map_info() {
		return regionManager;
	}

	public List<? extends AnimalInfo> get_animals() {
		return Collections.unmodifiableList(animals);
	}

	public double get_time() {
		return time;
	}

	public void advance(double dt) {

		time += dt;

		List<Animal> deadAnimals = new ArrayList<>();
		for (Animal animal : animals) {
			animal.update(dt);
			regionManager.update_animal_region(animal);
			if (animal.get_state() == State.DEAD) {
				deadAnimals.add(animal);
			}
		}
		deadAnimals.forEach(regionManager::unregister_animal);
		animals.removeAll(deadAnimals);

		regionManager.update_all_regions(dt);

		List<Animal> babies = new ArrayList<>();
		for (Animal animal : animals) {
			if (animal.is_pregnant()) {
				babies.add(animal.deliver_baby());
			}
		}
		babies.forEach(this::add_animal);

		notify_on_advanced(dt);
	}

	public void reset(int cols, int rows, int width, int height) {
		animals.clear();
		regionManager = new RegionManager(cols, rows, width, height);
		time = 0.0;
		notify_on_Reset(height);
	}

	public JSONObject as_JSON() {
		JSONObject result = new JSONObject();
		result.put("time", time);
		result.put("state", regionManager.as_JSON());
		return result;
	}

	@Override
	public void addObserver(EcoSysObserver o) {
		if (!observadores.contains(o)) {
			observadores.add(o);
			notify_on_addObserver(o);
		}
	}

	@Override
	public void removeObserver(EcoSysObserver o) {
		if (observadores.contains(o)) {
			observadores.remove(o);
		}
	}

	private void notify_on_advanced(double dt) {
		List<AnimalInfo> animalsInfo = new ArrayList<>(animals);
		for (EcoSysObserver observer : observadores)
			observer.onAvanced(time, regionManager, animalsInfo, dt);
	}

	private void notify_on_addObserver(EcoSysObserver o) {
		List<AnimalInfo> animalsInfo = new ArrayList<>(animals);
		o.onRegister(time, regionManager, animalsInfo);
	}

	private void notify_on_Reset(int height) {
		List<AnimalInfo> animalsInfo = new ArrayList<>(animals);
		for (EcoSysObserver observer : observadores)
			observer.onReset(height, regionManager, animalsInfo);
	}

	private void notify_on_AddAnimal(AnimalInfo a) {
		List<AnimalInfo> animalsInfo = new ArrayList<>(animals);
		for (EcoSysObserver observer : observadores)
			observer.onAnimalAdded(time, regionManager, animalsInfo, a);
	}

	private void notify_on_SetRegion(int row, int col, RegionInfo r) {
		for (EcoSysObserver observer : observadores)
			observer.onRegionSet(row, col, regionManager, r);
	}
}
