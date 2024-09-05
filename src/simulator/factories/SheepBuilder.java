package simulator.factories;

import org.json.JSONObject;
import simulator.misc.Vector2D;
import simulator.model.Animal;
import simulator.model.SelectFirst;
import simulator.model.SelectionStrategy;
import simulator.model.Sheep;

public class SheepBuilder extends Builder<Animal> {
	private final Factory<SelectionStrategy> strategyFactory;

	public SheepBuilder(Factory<SelectionStrategy> strategyFactory) {
		super("sheep", "Sheep");
		this.strategyFactory = strategyFactory;
	}

	@Override
	protected void fill_in_data(JSONObject o) {
	}

	@Override
	protected Animal create_instance(JSONObject data) {
		SelectionStrategy mateStrategy = new SelectFirst();
		SelectionStrategy dangerStrategy = new SelectFirst();

		// Obtener las estrategias de apareamiento y peligro si están presentes
		JSONObject mateStrategyData = data.optJSONObject("mate_strategy");
		JSONObject dangerStrategyData = data.optJSONObject("danger_strategy");

		if (mateStrategyData != null) {
			mateStrategy = strategyFactory.create_instance(mateStrategyData);
		}
		if (dangerStrategyData != null) {
			dangerStrategy = strategyFactory.create_instance(dangerStrategyData);
		}

		// Obtener los datos de posición si están presentes
		Vector2D pos = null;
		JSONObject posData = data.optJSONObject("pos");

		if (posData != null) {
			double x = posData.optDouble("x");
			double y = posData.optDouble("y");
			pos = new Vector2D(x, y);
		}
		return new Sheep(mateStrategy, dangerStrategy, pos);
	}
}
