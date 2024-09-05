package simulator.model;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Wolf extends Animal {
	public final static double INITIAL_SIGHT_RANGE = 50.0;
	public final static double INITIAL_SPEED = 60.0;
	public final static double MAX_AGE = 14.00;
	public final static double SPEED_PLUS = 0.007;
	public final static double DESIRE_PLUS = 30.0;
	public final static double ENERGY_PLUS = 18.0;
	public final static double MID_ENERGY = 50.0;
	public final static double SPEED_PLUS2 = 3.0;
	public final static double ENERGY_PLUS2 = 1.2;
	public final static double ENERGY_BIRTH = 10.0;
	public final static double MIN_DEST = 8.0;
	public final static double BABY_PROBABILITY = 0.9;
	public final static double ADD_ENERGY = 50.0;
	private Animal _hunt_target;
	private SelectionStrategy _hunting_strategy;

	public Wolf(SelectionStrategy mate_strategy, SelectionStrategy hunting_strategy, Vector2D pos) {
		super("Wolf", Diet.CARNIVORE, INITIAL_SIGHT_RANGE, INITIAL_SPEED, mate_strategy, pos);
		this._hunting_strategy = hunting_strategy;
		this._hunt_target = null;
	}

	protected Wolf(Wolf p1, Animal p2) {
		super(p1, p2);
		this._hunting_strategy = p1._hunting_strategy;
		this._hunt_target = null;
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
		case HUNGER:
			HungerBehavior(dt);
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
		if (this.get_energy() == 0.0 || this.get_age() > MAX_AGE)
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
		// 2
		if (this.get_energy() < MID_ENERGY) {
			this.set_state(State.HUNGER);// 2.1
		} else if (this._desire > MID_DESIRE) {
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
		if (_mate_target == null) {// Si no encuentra mate
			advance1(dt);
		} else {// 2
			_dest = _mate_target.get_position();// 2.1
			move(SPEED_PLUS2 * _speed * dt * Math.exp((_energy - MAX_ENERGY) * SPEED_PLUS));// 2.2
			_age += dt;// 2.3
			addEnergy(-(ENERGY_PLUS * ENERGY_PLUS2 * dt));// 2.4
			addDesire(DESIRE_PLUS * dt);// 2.5

			if (this._mate_target != null && this._pos.distanceTo(this._mate_target.get_position()) < MIN_DEST) {// 2.6
				resetDesire();// 2.6.1
				if (!this.is_pregnant() && Utils._rand.nextDouble() < BABY_PROBABILITY) {
					_baby = new Wolf(this, _mate_target);// 2.6.2
					addEnergy(-ENERGY_BIRTH);// 2.6.3
					this._mate_target = null;// 2.6.4
				}
			}
		}
		if (this.get_energy() < MID_ENERGY) {
			this.set_state(State.HUNGER);
		} else if (this._desire < MID_DESIRE)
			this.set_state(State.NORMAL);
	}

	private void HungerBehavior(double dt) {
		if (this._hunt_target == null || this._hunt_target.get_state() == State.DEAD
				|| !isVisible(this._hunt_target, this)) {// 1
			this._hunt_target = this._hunting_strategy.select(this,
					this._region_mngr.get_animals_in_range(this, (a) -> a.get_diet() == Diet.HERBIVORE));
		}
//2
		if (this._hunt_target == null) {//
			advance1(dt);
		} else {// 2.1
			_dest = this._hunt_target.get_position();
			move(SPEED_PLUS2 * _speed * dt * Math.exp((_energy - MAX_ENERGY) * SPEED_PLUS));// 2.2
			_age += dt;// 2.3
			addEnergy(-(ENERGY_PLUS * ENERGY_PLUS2 * dt));// 2.4
			addDesire(DESIRE_PLUS * dt);// 2.5

			if (_pos.distanceTo(_hunt_target.get_position()) < MIN_DEST) {// 2.6
				this._hunt_target._state = State.DEAD;// 2.6.1
				this._hunt_target = null;
				addEnergy(ADD_ENERGY);
			}
		}
		if (this.get_energy() > MID_ENERGY) {
			if (this._desire < MID_DESIRE)
				this.set_state(State.NORMAL);
			else
				this.set_state(State.MATE);
		}
	}

	public void set_state(State state) {
		_state = state;
		switch (_state) {
		case NORMAL:
			this._mate_target = null;
			this._hunt_target = null;
			break;
		case MATE:
			this._hunt_target = null;
			break;
		case HUNGER:
			this._mate_target = null;
			break;
		default:
			break;
		}
	}

	private void advance1(double dt) {
		if (_pos.distanceTo(_dest) < MIN_DEST) {// 1.1
			_dest = new Vector2D((Utils._rand.nextDouble(0, this._region_mngr.get_width())),
					Utils._rand.nextDouble(0, this._region_mngr.get_height()));
		}
		move(_speed * dt * Math.exp((_energy - MAX_ENERGY) * SPEED_PLUS));// 1.2
		_age += dt;// 1.3
		addEnergy(-(ENERGY_PLUS * dt));// 1.3
		addDesire(DESIRE_PLUS * dt);// 1.4
	}
}
