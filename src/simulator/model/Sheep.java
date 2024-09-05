
package simulator.model;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Sheep extends Animal {
	public final static double INITIAL_SIGHT_RANGE = 40.0;
	public final static double INITIAL_SPEED = 35.0;
	public final static double SPEED_PLUS = 0.007;
	public final static double SPEED_PLUS2 = 2.0;
	public final static double MAX_AGE = 8.0;
	public final static double ENERGY_PLUS = 20.0;
	public final static double ENERGY_PLUS2 = 1.2;
	public final static double DESIRE_PLUS = 40.0;
	public final static double MIN_DEST = 8.0;
	public final static double BABY_PROBABILITY = 0.9;

	private Animal _danger_source;
	private SelectionStrategy _danger_strategy;

	public Sheep(SelectionStrategy mate_strategy, SelectionStrategy danger_strategy, Vector2D pos) {
		super("Sheep", Diet.HERBIVORE, INITIAL_SIGHT_RANGE, INITIAL_SPEED, mate_strategy, pos);
		_danger_strategy = danger_strategy;
		_danger_source = null;
	}

	protected Sheep(Sheep p1, Animal p2) {
		super(p1, p2);
		_danger_strategy = p1._danger_strategy;
		_danger_source = null;
	}

	@Override
	public void update(double dt) {
		// 1. Si el estado es DEAD no hacer nada (volver inmediatamente)
		if (_state == State.DEAD)
			return;
//2. Actualizar el objeto según el estado del animal (ver la descripción abajo).
		switch (_state) {
		case NORMAL:
			moveAndHandleState(dt);
			break;
		case MATE:
			mateBehavior(dt);
			break;
		case DANGER:
			dangerBehavior(dt);
			break;
		default:
			break;
		}
		// 3. Si la posición está fuera del mapa, ajustarla y cambiar su estado a NORMAL
		if (!isIn()) {
			this.ajustarPos();
			this.set_state(State.NORMAL);
		}
		// 4. Si _energy es 0.0 o _age es mayor de 8.0, cambia su estado a DEAD.
		if (this.get_energy() == MIN_ENERGY || this.get_age() > MAX_AGE)
			this.set_state(State.DEAD);
		// Si su estado no es DEAD, pide comida al gestor de regiones usando
		// get_food(this, dt) y añadela a
		// su _energy (manteniéndolo siempre entre 0.0 y 100.0)
		if (this.get_state() != State.DEAD) {
			addEnergy(this._region_mngr.get_food(this, dt));
		}
	}

	private void moveAndHandleState(double dt) {
		advance1(dt);
		// Change state
		if (_danger_source == null) {// 2.1
			_danger_source = _danger_strategy.select(this,
					this._region_mngr.get_animals_in_range(this, (a) -> a.get_diet() == Diet.CARNIVORE));
		} else {
			this.set_state(State.DANGER);
		}
		// 2.2
		if (_danger_source == null && _desire > MID_DESIRE) {
			this.set_state(State.MATE);
		}
	}

	private void mateBehavior(double dt) {
		if ((_mate_target != null && _mate_target.get_state() == State.DEAD) || !isVisible(_mate_target, this)) {// 1
			_mate_target = null;
		}
		if (_mate_target == null) {// 2
			this._mate_target = this._mate_strategy.select(this, this._region_mngr.get_animals_in_range(this,
					(a) -> a.get_genetic_code() == this.get_genetic_code()));
		}
		if (_mate_target == null) {
			advance1(dt);
		} else {// 2
			_dest = _mate_target.get_position();// 2.1
			move(SPEED_PLUS2 * _speed * dt * Math.exp((_energy - MAX_ENERGY) * SPEED_PLUS));// 2.2
			_age += dt;// 2.3
			addEnergy(-(ENERGY_PLUS * ENERGY_PLUS2 * dt));// 2.4
			addDesire(DESIRE_PLUS * dt);// 2.5

			if (_pos.distanceTo(_mate_target.get_position()) < MIN_DEST) {
				resetDesire();
				if (!this.is_pregnant() && Utils._rand.nextDouble() < BABY_PROBABILITY) {
					_baby = new Sheep(this, _mate_target);
				}
				_mate_target = null;
			}
		}

		if (_danger_source == null) {
			_danger_source = _danger_strategy.select(this,
					this._region_mngr.get_animals_in_range(this, (a) -> a.get_diet() == Diet.CARNIVORE));
		}

		if (_danger_source != null) {
			this.set_state(State.DANGER);
		} else if (_danger_source == null && _desire < MID_DESIRE) {
			this.set_state(State.NORMAL);
		}
	}

	private void dangerBehavior(double dt) {
		if (_danger_source != null && _danger_source.get_state() == State.DEAD) {// 1
			_danger_source = null;
		}

		if (_danger_source == null) {//
			advance1(dt);
		} else {// 2.1
			_dest = _pos.plus(_pos.minus(_danger_source.get_position()).direction());
			move(SPEED_PLUS2 * _speed * dt * Math.exp((_energy - MAX_ENERGY) * SPEED_PLUS));// 2.2
			_age += dt;// 2.3
			addEnergy(-(ENERGY_PLUS * ENERGY_PLUS2 * dt));// 2.4
			addDesire(DESIRE_PLUS * dt);// 2.5

			if (_danger_source == null || !isVisible(_danger_source, this)) {// 3
				_danger_source = _danger_strategy.select(this,
						this._region_mngr.get_animals_in_range(this, (a) -> a.get_diet() == Diet.CARNIVORE));// 3.1.1
			}

			if (_danger_source == null) {
				if (_desire < MID_DESIRE) {// 3.1.2
					this.set_state(State.NORMAL);
				} else {
					this.set_state(State.MATE);
				}
			}
		}
	}

	protected void resetDesire() {
		_desire = 0.0;
		if (_mate_target != null) {
			_mate_target._desire = 0.0;
		}
	}

	public void set_state(State state) {
		_state = state;
		switch (_state) {
		case NORMAL:
			this._mate_target = null;
			this._danger_source = null;
			break;
		case MATE:
			this._danger_source = null;
			break;
		case DANGER:
			this._mate_target = null;
			break;
		default:
			break;
		}
	}

	private void advance1(double dt) {
		if (_pos.distanceTo(_dest) < MIN_DEST) {// 1.1
			_dest = new Vector2D(Utils._rand.nextDouble(0, this._region_mngr.get_width()),
					Utils._rand.nextDouble(0, this._region_mngr.get_height()));
		}
		move(_speed * dt * Math.exp((_energy - MAX_ENERGY) * SPEED_PLUS));// 1.2
		_age += dt;// 1.3
		addEnergy(-(ENERGY_PLUS * dt));// 1.3
		addDesire(DESIRE_PLUS * dt);// 1.4
	}
}
