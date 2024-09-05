package simulator.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.table.AbstractTableModel;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;
import simulator.model.State;

class SpeciesTableModel extends AbstractTableModel implements EcoSysObserver {

	private Controller _ctrl;
	private Map<String, Map<State, Integer>> _speciesData;
	private State[] _states;
	private String[] _columnNames;
    private List<String>Species_List;//Lista de especies utilizada para la función de correción
	SpeciesTableModel(Controller ctrl) {
		_ctrl = ctrl;
		_speciesData = new HashMap<>();
		_states = State.values();
		Species_List = new ArrayList<>();
		_columnNames = new String[(_states.length + 1)];
		_columnNames[0] = "Species";
		for (int i = 1; i < _columnNames.length; i++)
			_columnNames[i] = _states[i - 1].toString();
		ctrl.addObserver(this);
	}

	@Override
	public int getRowCount() {
		return _speciesData.size();
	}

	@Override
	public int getColumnCount() {
		return _columnNames.length;
	}

	public String getColumnName(int col) {
		return _columnNames[col].toString();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		//FUNCION DE CORRECCIÓN(FALLA POR initializeData())
	    //////////////////////////////////////////////
		/*String specie = Species_List.get(rowIndex);
		String value = specie;
		if (columnIndex != 0) {
			value = _speciesData.get(specie).get(_states[columnIndex - 1]).toString();
		}

		return value;
		 */
		////////////////////////////////////////
		String[] species = new String[_speciesData.keySet().size()];
		int cont = 0;
		for (String e : _speciesData.keySet()) {
			species[cont] = e;
			cont++;
		}
		String specie = species[rowIndex];
		String value = specie;
		if (columnIndex != 0) {
			value = _speciesData.get(species[rowIndex]).get(_states[columnIndex - 1]).toString();
		}

		return value;
		
	}

	@Override
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		initializeData(animals);
	}

	@Override
	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		initializeData(animals);
	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
		initializeData(animals);
	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
		fireTableDataChanged();

	}

	@Override
	public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		initializeData(animals);
	}

	private void initializeData(List<AnimalInfo> animals) {
		//FUNCION DE CORRECCIÓN(FALLA)
		//////////////////////////////////////////////
		/*_speciesData.clear();
		for (AnimalInfo animal : animals) {
			String specie = animal.get_genetic_code();
			if(!Species_List.contains(specie)) {
				Species_List.add(specie);
				Map<State, Integer> states = new HashMap<>();
				  for (State state : _states) {
		                states.put(state, 0);
		            }
				_speciesData.put(specie, states);
			}
				
			Map<State, Integer> stateCounts = _speciesData.getOrDefault(specie, new HashMap<>());
			
				int count = stateCounts.getOrDefault(animal.get_state(), 0);
				stateCounts.put(animal.get_state(), count++);
				
					
			_speciesData.put(specie, stateCounts);
		}
		fireTableDataChanged();*/
		
		//////////////////////////////////////////
		
		_speciesData.clear();
		for (AnimalInfo animal : animals) {
			String specie = animal.get_genetic_code();
			Map<State, Integer> stateCounts = _speciesData.getOrDefault(specie, new HashMap<>());
			for (State state : _states) {
				int count = stateCounts.getOrDefault(state, 0);
				if (animal.get_state() == state) {
					count++;
				}
				stateCounts.put(state, count);
			}
			_speciesData.put(specie, stateCounts);
		}
		fireTableDataChanged();
		
	}
}
