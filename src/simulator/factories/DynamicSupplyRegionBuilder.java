package simulator.factories;

import org.json.JSONObject;

import simulator.model.DynamicSupplyRegion;
import simulator.model.Region;

public class DynamicSupplyRegionBuilder extends Builder<Region> {
	public DynamicSupplyRegionBuilder() {
		super("dynamic", "Dynamic Supply Region");
	}

	@Override
	protected void fill_in_data(JSONObject o) {
		o.put("factor", "food increase factor (optional, default 2.0)");
		o.put("food", "initial amount of food (optional, default 100.0)");
	}

	@Override
	protected Region create_instance(JSONObject data) {
		double factor = 2.0;
		double food = 1000.0;
		if (data.has("factor")) {
			factor = data.getDouble("factor");
		}
		if (data.has("food")) {
			food = data.getDouble("food");
		}
		return new DynamicSupplyRegion(food, factor);
	}

	public String toString() {
		return this.get_type_tag();
	}
}
