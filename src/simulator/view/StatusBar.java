package simulator.view;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

class StatusBar extends JPanel implements EcoSysObserver {
	private Controller _ctrl;
	private JLabel tiempoLabel;
	private JLabel numeroAnimalesLabel;
	private JLabel dimensionLabel;

	// TODO A�adir los atributos necesarios.
	StatusBar(Controller ctrl) {
		_ctrl = ctrl;
		initGUI();

		// registrar this como observador
		ctrl.addObserver(this);
	}

	private void initGUI() {
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.setBorder(BorderFactory.createBevelBorder(1));
		tiempoLabel = new JLabel("Tiempo: ");
		add(tiempoLabel);
		addSeparator();
		numeroAnimalesLabel = new JLabel("Animales: ");
		add(numeroAnimalesLabel);
		addSeparator();
		dimensionLabel = new JLabel("Dimensiones: ");
		add(dimensionLabel);
		;
	}

	// TODO el resto de m�todos van aqu�
	@Override
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		UpdateStatus(time, map, animals);
	}

	@Override
	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		UpdateStatus(time, map, animals);
	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
		UpdateStatus(time, map, animals);

	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		UpdateStatus(time, map, animals);
	}

	private void addSeparator() {
		JSeparator s = new JSeparator(JSeparator.VERTICAL);
		s.setPreferredSize(new Dimension(10, 20));
		this.add(s);
	}

	private void UpdateStatus(double time, MapInfo map, List<AnimalInfo> animals) {
		tiempoLabel.setText("Time: " + (String.format("%.3f", time)));
		numeroAnimalesLabel.setText("Total Animals: " + animals.size());
		dimensionLabel.setText(
				"Dimension: " + map.get_width() + "x" + map.get_height() + " " + map.get_rows() + "x" + map.get_cols());
	}
}
