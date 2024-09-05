package simulator.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.json.JSONArray;
import org.json.JSONObject;

public class RegionManager implements AnimalMapView, Iterable<simulator.model.MapInfo.RegionData> {

	private int cols;
	private int rows;
	private int width;
	private int height;
	private int regionWidth;
	private int regionHeight;
	private Region[][] _regions;
	private Map<Animal, Region> _animal_region;

	// Constructora
	public RegionManager(int cols, int rows, int width, int height) {
		this.cols = cols;
		this.rows = rows;
		this.width = width;
		this.height = height;
		this.regionWidth = width / cols;
		this.regionHeight = height / rows;
		_regions = new Region[rows][cols];
		// Inicializamos cada region de la matriz a DefaultRegion
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				_regions[i][j] = new DefaultRegion();
			}
		}
		_animal_region = new HashMap<Animal, Region>();
	}

	// M�todos necesarios
	void set_region(int row, int col, Region r) {
		List<Animal> list_aux = _regions[row][col].getAnimals();
		for (Animal a : list_aux) {// A�adimos los animales anteriores
			r.add_animal(a);
			_animal_region.replace(a, _regions[row][col], r);
		}
		_regions[row][col] = r;
	}

	void register_animal(Animal a) {
		a.init(this);
		int row = (int) a.get_position().getY() / this.regionHeight;
		int col = (int) a.get_position().getX() / this.regionWidth;
		_regions[row][col].add_animal(a);
		_animal_region.put(a, _regions[row][col]);
	}

	void unregister_animal(Animal a) {
		Region region = _animal_region.get(a);
		if (region != null) {
			region.remove_animal(a);
			_animal_region.remove(a);
		}
	}

	void update_animal_region(Animal a) {
		int NewRow = (int) a.get_position().getY() / this.regionHeight;
		int NewCol = (int) a.get_position().getX() / this.regionWidth;
		Region newRegion = _regions[NewRow][NewCol];
		Region oldRegion = _animal_region.get(a);
		if (newRegion != oldRegion && oldRegion != null) {// oldregion no puede ser null
			oldRegion.remove_animal(a);
			newRegion.add_animal(a);
			_animal_region.replace(a, oldRegion, newRegion);
		}
	}

	public double get_food(Animal a, double dt) {
		Region region = _animal_region.get(a);
		double foodToReturn = 0.0;
		if (region != null) {
			foodToReturn = region.get_food(a, dt);
		}
		return foodToReturn;
	}

	void update_all_regions(double dt) {
		for (Region[] row : _regions) {
			for (Region region : row) {
				region.update(dt);
			}
		}
	}

	public List<Animal> get_animals_in_range(Animal a, Predicate<Animal> filter) {
		List<Animal> animalsInRange = new ArrayList<>();
		int row = (int) a.get_position().getX() / this.regionWidth;
		int col = (int) a.get_position().getY() / this.regionHeight;
		for (int i = Math.max(0, row - 1); i <= Math.min(rows - 1, row + 1); i++) {
			for (int j = Math.max(0, col - 1); j <= Math.min(cols - 1, col + 1); j++) {
				animalsInRange.addAll(_regions[i][j].getAnimals());
			}
		}
		animalsInRange.removeIf(animal -> animal == a || !filter.test(animal)
				|| animal.get_position().distanceTo(a.get_position()) > a.get_sight_range());
		return animalsInRange;
	}

	public JSONObject as_JSON() {
		JSONArray regionArray = new JSONArray();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				JSONObject regionJSON = new JSONObject();
				regionJSON.put("row", i);
				regionJSON.put("col", j);
				regionJSON.put("data", _regions[i][j].as_JSON());
				regionArray.put(regionJSON);
			}
		}
		JSONObject result = new JSONObject();
		result.put("regions", regionArray);
		return result;
	}

	@Override
	public int get_cols() {
		return this.cols;
	}

	@Override
	public int get_rows() {
		return this.rows;
	}

	@Override
	public int get_width() {
		return this.width;
	}

	@Override
	public int get_height() {
		return this.height;
	}

	@Override
	public int get_region_width() {
		return this.regionWidth;
	}

	@Override
	public int get_region_height() {
		return this.regionHeight;
	}

	@Override
	public Iterator<RegionData> iterator() {
		return new Iterador();
	}

	public class Iterador implements Iterator<RegionData> {
		int i = 0;
		int j = -1;

		@Override
		public boolean hasNext() {
			return i < rows - 1 || j < cols - 1;
		}

		@Override
		public RegionData next() {
			if (j < cols - 1) {
				j++;
			} else {
				j = 0;
				i++;
			}

			return new RegionData(i, j, _regions[i][j]);
		}

	}
}
