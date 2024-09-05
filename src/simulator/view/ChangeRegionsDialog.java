package simulator.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.InputStream;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;
import simulator.launcher.Main;

class ChangeRegionsDialog extends JDialog implements EcoSysObserver {
	private DefaultComboBoxModel<String> _regionsModel;
	private DefaultComboBoxModel<String> _fromRowModel;
	private DefaultComboBoxModel<String> _toRowModel;
	private DefaultComboBoxModel<String> _fromColModel;
	private DefaultComboBoxModel<String> _toColModel;
	private DefaultTableModel _dataTableModel;
	private Controller _ctrl;
	private List<JSONObject> _regionsInfo;
	private String[] _headers = { "Key", "Value", "Description" };

// TODO en caso de ser necesario, a�adir los atributos aqu�
	ChangeRegionsDialog(Controller ctrl) {
		super((Frame) null, true);
		_ctrl = ctrl;
		initGUI();
		_ctrl.addObserver(this);
	}

	private void initGUI() {
		setTitle("Change Regions");
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		setContentPane(mainPanel);

		JPanel ayudaPanel = new JPanel();
		JLabel ayudaLabel = new JLabel("<html>Seleccione una region para ver sus datos: </html>");
		ayudaPanel.add(ayudaLabel);
		mainPanel.add(ayudaPanel);

		JPanel panelTabla = new JPanel(); // Crea un nuevo JPanel para contener la tabla
		panelTabla.setLayout(new BorderLayout()); // Establece un diseño de borde para el panel
		mainPanel.add(panelTabla);

		JPanel comBoxPanel = new JPanel();
		mainPanel.add(comBoxPanel);

		JPanel buttonPanel = new JPanel();
		mainPanel.add(buttonPanel);

		_regionsInfo = Main._region_factory.get_info();
// _dataTableModel es un modelo de tabla que incluye todos los parametros de
// la region
		_dataTableModel = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
// TODO hacer editable solo la columna 1
				return column == 1;
			}
		};
		_dataTableModel.setColumnIdentifiers(_headers);
// TODO crear un JTable que use _dataTableModel, y a�adirlo al di�logo
// _regionsModel es un modelo de combobox que incluye los tipos de regiones
		JTable table = new JTable(_dataTableModel);
		panelTabla.add(new JScrollPane(table));
		_regionsModel = new DefaultComboBoxModel<>();
		// a�adir la descripci�n de todas las regiones a _regionsModel, para eso
		// usa la clave �desc� o �type� de los JSONObject en _regionsInfo,
		for (JSONObject regionInfo : _regionsInfo) {
			_regionsModel.addElement(regionInfo.getString("type"));
		}

// ya que estos nos dan informaci�n sobre lo que puede crear la factor�a.
// TODO crear un combobox que use _regionsModel y a�adirlo al di�logo.
		JComboBox<String> regionsComboBox = new JComboBox<>(_regionsModel);
		comBoxPanel.add(new JLabel("Region:"));
		comBoxPanel.add(regionsComboBox);

// TODO crear 4 modelos de combobox para _fromRowModel, _toRowModel,
// _fromColModel y _toColModel.

// TODO crear 4 combobox que usen estos modelos y a�adirlos al di�logo.
// TODO crear los botones OK y Cancel y a�adirlos al di�logo.

		_fromRowModel = new DefaultComboBoxModel<>();
		_toRowModel = new DefaultComboBoxModel<>();
		_fromColModel = new DefaultComboBoxModel<>();
		_toColModel = new DefaultComboBoxModel<>();

		JComboBox<String> fromRowComboBox = new JComboBox<>(_fromRowModel);
		JComboBox<String> toRowComboBox = new JComboBox<>(_toRowModel);
		JComboBox<String> fromColComboBox = new JComboBox<>(_fromColModel);
		JComboBox<String> toColComboBox = new JComboBox<>(_toColModel);

		comBoxPanel.add(new JLabel("From Row:"));
		comBoxPanel.add(fromRowComboBox);
		comBoxPanel.add(new JLabel("To Row:"));
		comBoxPanel.add(toRowComboBox);
		comBoxPanel.add(new JLabel("From Column:"));
		comBoxPanel.add(fromColComboBox);
		comBoxPanel.add(new JLabel("To Column:"));
		comBoxPanel.add(toColComboBox);

		JButton okButton = new JButton("OK");
		JButton cancelButton = new JButton("Cancel");

		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		setPreferredSize(new Dimension(700, 400)); // puedes usar otro tama�o
		pack();
		setResizable(false);
		setVisible(false);

		okButton.addActionListener(e -> {
			JSONObject region_type = _regionsInfo.get(regionsComboBox.getSelectedIndex());
			String rowFrom = _fromRowModel.getSelectedItem().toString();
			String rowTo = _toRowModel.getSelectedItem().toString();
			
			String colFrom = _fromColModel.getSelectedItem().toString();
			String colTo = _toColModel.getSelectedItem().toString();

			JSONArray regionsArray = new JSONArray();

			JSONObject data = load_JSON_file(getJSON());
			regionsArray.put(createRegionJSON(data, this._regionsModel.getSelectedItem().toString(), rowFrom, rowTo, colFrom, colTo));
			JSONObject regions = new JSONObject();
			regions.put("regions", regionsArray);
			try {
				_ctrl.set_regions(regions);
				setVisible(false);
			} catch (Exception ex) {
				ViewUtils.showErrorMsg(ex.getMessage());
			}
		});

		cancelButton.addActionListener(e -> {
			setVisible(false);
		});

		regionsComboBox.addActionListener(e -> {
			// Update the data table model when the region is changed
			JSONObject selectedRegionInfo = _regionsInfo.get(regionsComboBox.getSelectedIndex());
			JSONObject data = selectedRegionInfo.getJSONObject("data");

			_dataTableModel.setRowCount(0);
			for (String key : data.keySet()) {
				Object[] rowData = { key, "", data.get(key) };
				_dataTableModel.addRow(rowData);
			}
		});
		setPreferredSize(new Dimension(700, 400));
		pack();
		setResizable(false);
		setVisible(false);
	}

	public void open(Frame parent) {
		setLocation(//
				parent.getLocation().x + parent.getWidth() / 2 - getWidth() / 2, //
				parent.getLocation().y + parent.getHeight() / 2 - getHeight() / 2);
		pack();
		setVisible(true);
	}
	private static JSONObject load_JSON_file(String in) {
		return new JSONObject(new JSONTokener(in));
	}
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		regionSet(map);
	}

	@Override
	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		regionSet(map);

	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {

	}

	private void regionSet(MapInfo map) {
		_fromRowModel.removeAllElements();
		_toRowModel.removeAllElements();
		_fromColModel.removeAllElements();
		_toColModel.removeAllElements();

		for (int i = 0; i < map.get_rows(); i++) {
			_fromRowModel.addElement(String.valueOf(i));
			_toRowModel.addElement(String.valueOf(i));
		}

		for (int j = 0; j < map.get_cols(); j++) {
			_fromColModel.addElement(String.valueOf(j));
			_toColModel.addElement(String.valueOf(j));
		}
	}

	/*private JSONObject convertTableToJSON() {
		JSONObject regionData = new JSONObject();
		for (int i = 0; i < _dataTableModel.getRowCount(); i++) {
			Object keyObject = _dataTableModel.getValueAt(i, 0);
			Object valueObject = _dataTableModel.getValueAt(i, 1);
			if (keyObject != null && valueObject != null && !valueObject.toString().isEmpty()) {
				regionData.put(keyObject.toString(), valueObject.toString());
			}
		}
		return regionData;
	}*/
	public String getJSON() {
		StringBuilder s = new StringBuilder();
		s.append('{');
		for (int i = 0; i < _dataTableModel.getRowCount(); i++) {
			String k = _dataTableModel.getValueAt(i, 0).toString();
			String v = _dataTableModel.getValueAt(i, 1).toString();
			if (!v.isEmpty()) {
				s.append('"');
				s.append(k);
				s.append('"');
				s.append(':');
				s.append(v);
				s.append(',');
			}
		}

		if (s.length() > 1)
			s.deleteCharAt(s.length() - 1);
		s.append('}');

		return s.toString();
	}
	private JSONObject createRegionJSON(JSONObject regionData, String regionType, String rowFrom, String rowTo,
			String colFrom, String colTo) {
		JSONObject region = new JSONObject();
		
		JSONArray row = new JSONArray();
		row.put(rowFrom);
		row.put(rowTo);
		
		region.put("row", row);
		
		JSONArray col = new JSONArray();
		col.put(colFrom);
		col.put(colTo);
		
		region.put("col", col);
        
		JSONObject spec = new JSONObject();
		spec.put("type", regionType);
		spec.put("data", regionData);
		
		region.put("spec", spec);
		return region;
	}
}
