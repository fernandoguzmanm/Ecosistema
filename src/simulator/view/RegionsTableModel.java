package simulator.view;

import javax.swing.table.AbstractTableModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import simulator.control.Controller;
import simulator.model.*;
import simulator.model.MapInfo.RegionData;

class RegionsTableModel extends AbstractTableModel implements EcoSysObserver {

	private Controller _ctrl;
	private MapInfo _regions;
	private String[] _columnNames;
	private Diet[] _diets;
	private List<Object[]> _rowData;
	RegionsTableModel(Controller ctrl) {
		_ctrl = ctrl;
		_rowData = new ArrayList<>();
		_diets = Diet.values();
		_columnNames = new String[(_diets.length + 3)];
		_columnNames[0] = "Row";
		_columnNames[1] = "Col";
		_columnNames[2] = "Desc";
		for (int i = 3; i < _diets.length + 3; i++)
			_columnNames[i] = _diets[i - 3].toString();

		_ctrl.addObserver(this);
	}

	@Override
	public int getRowCount() {
		return _rowData.size();
	}

	@Override
	public int getColumnCount() {
		return _columnNames.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object[] data = _rowData.get(rowIndex);
		return data[columnIndex];
	}

	@Override
	public String getColumnName(int column) {
		return _columnNames[column];
	}

	@Override
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		updateData(map);
	}

	@Override
	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		updateData(map);
	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {

	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
		updateData(map);
	}

	@Override
	public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		updateData(map);
	}

	private void updateData(MapInfo map) {
		_regions = map;
		initializeData(Diet.values());
		fireTableDataChanged();
	}

	private void initializeData(Diet[] diets) {
		_rowData = new ArrayList<>();
		for (RegionData rd : _regions) {
			int[] dietCounts = new int[diets.length];

	   
	        rd.r().getAnimalsInfo().forEach(a -> {
	            Diet diet = a.get_diet();
	            for (int i = 0; i < diets.length; i++) {
	                if (diets[i] == diet) {
	                    dietCounts[i]++;
	                    break;
	                }
	            }
	        });
			List<Integer> numAnimals = new ArrayList<>();
			for (int count : dietCounts) {
	            numAnimals.add(count);
	        }

			Object[] values= new Object[_diets.length+3];
			  for (int i = 0; i < values.length; i++) {
			        if (i == 0) values[i] = rd.row();
			        else if (i == 1) values[i] = rd.col();
			        else if (i == 2) values[i] = rd.r().toString();
			        else values[i] = numAnimals.get(i-3);
			    }
            _rowData.add(values);
		}
	}

}
