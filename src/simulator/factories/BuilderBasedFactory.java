package simulator.factories;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class BuilderBasedFactory<T> implements Factory<T> {
	private Map<String, Builder<T>> _builders;
	private List<JSONObject> _builders_info;

	public BuilderBasedFactory() {
		_builders = new HashMap<>();
		_builders_info = new LinkedList<>();
	}

	public BuilderBasedFactory(List<Builder<T>> builders) {
		this();
		for (Builder<T> b : builders) {
			add_builder(b);
		}
	}

	public void add_builder(Builder<T> b) {
		_builders.put(b.get_type_tag(), b);
		_builders_info.add(b.get_info());
	}

	@Override
	public T create_instance(JSONObject info) {
		if (info == null) {
			throw new IllegalArgumentException("'info' cannot be null");
		}

		String type = info.getString("type");
		JSONObject data = info.optJSONObject("data");
		Builder<T> builder = _builders.get(type);
		if (builder != null) {
			return builder.create_instance(data != null ? data : new JSONObject());
		} else {
			throw new IllegalArgumentException("Unrecognized 'info':" + info.toString());
		}
	}

	@Override
	public List<JSONObject> get_info() {
		return Collections.unmodifiableList(_builders_info);
	}
}
