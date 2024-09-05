package simulator.control;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.Simulator;
import simulator.view.*;
import simulator.view.SimpleObjectViewer.ObjInfo;

public class Controller {

	private Simulator _sim;

	public Controller(Simulator sim) {
		_sim = sim;
	}

	public void load_data(JSONObject data) {

		set_regions(data);

		JSONArray animals = data.getJSONArray("animals");
		for (int i = 0; i < animals.length(); i++) {
			JSONObject animalSpec = animals.getJSONObject(i);
			int amount = animalSpec.getInt("amount");
			JSONObject animalData = animalSpec.getJSONObject("spec");
			for (int j = 0; j < amount; j++) {
				_sim.add_animal(animalData);
			}
		}
	}

	public void run(double t, double dt, boolean sv, OutputStream out) {
		PrintStream p = new PrintStream(out);
		SimpleObjectViewer view = null;
		if (sv) {
			MapInfo m = _sim.get_map_info();
			view = new SimpleObjectViewer("[ECOSYSTEM]", m.get_width(), m.get_height(), m.get_cols(), m.get_rows());
			view.update(to_animals_info(_sim.get_animals()), _sim.get_time(), dt);
		}
		p.println(_sim.as_JSON());
		while (_sim.get_time() <= t) {
			_sim.advance(dt);
			if (sv)
				view.update(to_animals_info(_sim.get_animals()), _sim.get_time(), dt);
		}
		p.println(_sim.as_JSON());
		if (sv)
			view.close();
	}

	private List<ObjInfo> to_animals_info(List<? extends AnimalInfo> animals) {
		List<ObjInfo> ol = new ArrayList<>(animals.size());
		for (AnimalInfo a : animals) {
			ol.add(new ObjInfo(a.get_genetic_code(), (int) a.get_position().getX(), (int) a.get_position().getY(), 8));
		}
		return ol;
	}

	public void reset(int cols, int rows, int width, int height) {
		_sim.reset(cols, rows, width, height);
	}

	public void set_regions(JSONObject rs) {
		if (rs.has("regions")) {
			JSONArray regions = rs.getJSONArray("regions");
			for (int i = 0; i < regions.length(); i++) {
				JSONObject regionSpec = regions.getJSONObject(i);
				int rowFrom = regionSpec.getJSONArray("row").getInt(0);
				int rowTo = regionSpec.getJSONArray("row").getInt(1);
				int colFrom = regionSpec.getJSONArray("col").getInt(0);
				int colTo = regionSpec.getJSONArray("col").getInt(1);
				JSONObject regionData = regionSpec.getJSONObject("spec");
				for (int row = rowFrom; row <= rowTo; row++) {
					for (int col = colFrom; col <= colTo; col++) {
						_sim.set_region(row, col, regionData);
					}
				}
			}
		}
	}

	public void advance(double dt) {
		_sim.advance(dt);
	}

	public void addObserver(EcoSysObserver o) {
		_sim.addObserver(o);
	}

	public void removeObserver(EcoSysObserver o) {
		_sim.removeObserver(o);
	}

}
