package simulator.model;

import org.json.JSONObject;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public abstract class Animal implements Entity, AnimalInfo {
	public final static double TOLERANCE = 0.1;
	public final static double ENERGY = 100.0;
	public final static double POS = 60.0;
	public final static double RANGE = 0.2;
	public final static double MIN_ENERGY = 0.0;
	public final static double MAX_ENERGY = 100.0;
	public final static double MIN_DESIRE = 0.0;
	public final static double MID_DESIRE = 65.0;
	public final static double MAX_DESIRE = 100.0;
	protected String _genetic_code;
	protected Diet _diet;
	protected State _state;
	protected Vector2D _pos;
	protected Vector2D _dest;
	protected double _energy;
	protected double _speed;
	protected double _age;
	protected double _desire;
	protected double _sight_range;
	protected Animal _mate_target;
	protected Animal _baby;
	protected AnimalMapView _region_mngr;
	protected SelectionStrategy _mate_strategy;

	protected Animal(String genetic_code, Diet diet, double sight_range, double init_speed,
			SelectionStrategy mate_strategy, Vector2D pos) {
		if (genetic_code == null || genetic_code.isEmpty())
			throw new IllegalArgumentException("Genetic code cannot be null or empty");
		if (sight_range <= 0 || init_speed <= 0)
			throw new IllegalArgumentException("Sight range and speed must be positive numbers");
		if (mate_strategy == null)
			throw new IllegalArgumentException("Mate strategy cannot be null");

		_genetic_code = genetic_code;
		_diet = diet;
		_sight_range = sight_range;
		_mate_strategy = mate_strategy;
		_pos = pos;
		_state = State.NORMAL;
		_energy = ENERGY;
		_desire = 0.0;
		_dest = null;
		_mate_target = null;
		_baby = null;
		_region_mngr = null;
		_speed = Utils.get_randomized_parameter(init_speed, TOLERANCE);
	}

	protected Animal(Animal p1, Animal p2) {
		_dest = null;
		_baby = null;
		_mate_target = null;
		_region_mngr = null;
		_state = State.NORMAL;
		_desire = 0.0;
		_genetic_code = p1._genetic_code;
		_diet = p1._diet;
		_mate_strategy = p2._mate_strategy;
		_energy = (p1._energy + p2._energy) / 2;
		_pos = p1._pos.plus(Vector2D.get_random_vector(-1, 1).scale(POS * (Utils._rand.nextGaussian() + 1)));
		_sight_range = Utils.get_randomized_parameter((p1._sight_range + p2._sight_range) / 2, RANGE);
		_speed = Utils.get_randomized_parameter((p1._speed + p2._speed) / 2, RANGE);
	}

	public void init(AnimalMapView reg_mngr) {
		_region_mngr = reg_mngr;
		if (_pos == null)
			_pos = new Vector2D(Utils._rand.nextDouble(0, (this._region_mngr.get_width())),
					Utils._rand.nextDouble(0, (this._region_mngr.get_height())));
		else
			ajustarPos();

		_dest = new Vector2D(Utils._rand.nextDouble(0, reg_mngr.get_width()),
				Utils._rand.nextDouble(0, reg_mngr.get_height()));
	}

	public Animal deliver_baby() {
		Animal baby = _baby;
		_baby = null;
		return baby;
	}

	protected void move(double speed) {
		_pos = _pos.plus(_dest.minus(_pos).direction().scale(speed));
	}

	public JSONObject as_JSON() {
		JSONObject obj = new JSONObject();
		obj.put("pos", new double[] { _pos.getX(), _pos.getY() });
		obj.put("gcode", _genetic_code);
		obj.put("diet", _diet.toString());
		obj.put("state", _state.toString());
		return obj;
	}

	public State get_state() {
		return _state;
	}

	public Vector2D get_position() {
		return _pos;
	}

	public String get_genetic_code() {
		return _genetic_code;
	}

	public Diet get_diet() {
		return _diet;
	}

	public double get_speed() {
		return _speed;
	}

	public double get_sight_range() {
		return _sight_range;
	}

	public double get_energy() {
		return _energy;
	}

	public double get_age() {
		return _age;
	}

	public Vector2D get_destination() {
		return _dest;
	}

	public boolean is_pregnant() {
		return _baby != null;
	}

	protected void resetDesire() {
		_desire = 0.0;
	}

	protected void ajustarPos() {
		while (_pos.getX() >= _region_mngr.get_width())
			_pos = new Vector2D(_pos.getX() - _region_mngr.get_width(), _pos.getY());
		while (_pos.getX() < 0)
			_pos = new Vector2D(_pos.getX() + _region_mngr.get_width(), _pos.getY());
		while (_pos.getY() >= _region_mngr.get_height())
			_pos = new Vector2D(_pos.getX(), _pos.getY() - _region_mngr.get_height());
		while (_pos.getY() < 0)
			_pos = new Vector2D(_pos.getX(), _pos.getY() + _region_mngr.get_height());
	}

	protected boolean isIn() {
		return _pos.getX() >= 0 && _pos.getX() < _region_mngr.get_width() && _pos.getY() >= 0
				&& _pos.getY() < _region_mngr.get_height();
	}

	protected void addEnergy(double e) {
		_energy += e;
		if (_energy < MIN_ENERGY)
			_energy = MIN_ENERGY;
		else if (_energy > MAX_ENERGY)
			_energy = MAX_ENERGY;
	}

	protected void addDesire(double d) {
		_desire += d;
		if (_desire < MIN_DESIRE)
			_desire = MIN_DESIRE;
		else if (_desire > MAX_DESIRE)
			_desire = MAX_DESIRE;
	}

	protected boolean isVisible(Animal animal, Animal a) {
		// Check if the animal is within the sheep's sight range
		boolean visible = false;
		if (animal != null)
			visible = a.get_position().distanceTo(animal.get_position()) <= a.get_sight_range();

		return visible;
	}
}
