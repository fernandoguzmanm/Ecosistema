package simulator.factories;

import org.json.JSONObject;

import simulator.misc.Vector2D;
import simulator.model.Animal;
import simulator.model.SelectFirst;
import simulator.model.SelectionStrategy;
import simulator.model.Wolf;

public class WolfBuilder extends Builder<Animal> {
	private final Factory<SelectionStrategy> strategyFactory;

	public WolfBuilder(Factory<SelectionStrategy> strategyFactory) {
		super("wolf", "Wolf");
		this.strategyFactory = strategyFactory;
	}

	@Override
	protected Animal create_instance(JSONObject data) {
		SelectionStrategy mateStrategy = new SelectFirst();
		SelectionStrategy dangerStrategy = new SelectFirst();

		JSONObject mateStrategyData = data.optJSONObject("mate_strategy");
		JSONObject dangerStrategyData = data.optJSONObject("danger_strategy");

		// Verificar si hay datos de estrategia de apareamiento
		if (mateStrategyData != null) {
			mateStrategy = strategyFactory.create_instance(mateStrategyData);
		}

		// Verificar si hay datos de estrategia de peligro
		if (dangerStrategyData != null) {
			dangerStrategy = strategyFactory.create_instance(dangerStrategyData);
		}

		JSONObject posData = data.optJSONObject("pos");
		Vector2D pos = null;

		if (posData != null) {
			double x = posData.getDouble("x");
			double y = posData.getDouble("y");
			pos = new Vector2D(x, y);
		}

		return new Wolf(mateStrategy, dangerStrategy, pos);
	}
}
