package simulator.model;

import org.json.JSONObject;

//Proporcionar estado en formato JSON
public interface JSONable {
	default public JSONObject as_JSON() {
		return new JSONObject();
	}
}
