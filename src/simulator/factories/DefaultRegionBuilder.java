package simulator.factories;

import java.util.Map;

import org.json.JSONObject;

import simulator.model.DefaultRegion;
import simulator.model.Region;

public class DefaultRegionBuilder extends Builder<Region> {
	public DefaultRegionBuilder() {
		super("default", "Default Region");
	}

	@Override
	protected void fill_in_data(JSONObject o) {

	}

	@Override
	protected Region create_instance(JSONObject data) {
		return new DefaultRegion();
	}

	public String toString() {
		return this.get_type_tag();
	}

}
