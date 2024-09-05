package simulator.view;

import javax.swing.*;
import org.json.JSONObject;
import org.json.JSONTokener;
import simulator.control.Controller;
import simulator.misc.Utils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;

public class ControlPanel extends JPanel {
	private Controller _ctrl;
	private ChangeRegionsDialog _changeRegionsDialog;
	private JToolBar _toolaBar;
	private JFileChooser _fc;
	private boolean _stopped = true;
	private JButton _quitButton;
	private JButton _loadButton;
	private JButton _mapButton;
	private JButton _changeRegionsButton;
	private JButton _runButton;
	private JButton _stopButton;
	private JSpinner _spinner;
	private JTextField _deltaTimeField;

	ControlPanel(Controller ctrl) {
		_ctrl = ctrl;
		initGUI();
	}

	private void initGUI() {
		setLayout(new BorderLayout());
		_toolaBar = new JToolBar();
		add(_toolaBar, BorderLayout.PAGE_START);

		// Load Button
		_loadButton = new JButton();
		_loadButton.setToolTipText("Load File");
		_loadButton.setIcon(new ImageIcon("resources/icons/open.png"));
		_loadButton.addActionListener((e) -> loadFile());
		_toolaBar.add(_loadButton);

		// Map Button
		_mapButton = new JButton();
		_mapButton.setToolTipText("Open Map");
		_mapButton.setIcon(new ImageIcon("resources/icons/viewer.png"));
		_mapButton.addActionListener((e) -> openMap());
		_toolaBar.add(_mapButton);

		_toolaBar.addSeparator();

		// Change Regions Button
		_changeRegionsButton = new JButton();
		_changeRegionsButton.setToolTipText("Change Regions");
		_changeRegionsButton.setIcon(new ImageIcon("resources/icons/regions.png"));
		_changeRegionsButton.addActionListener((e) -> openChangeRegionsDialog());
		_toolaBar.add(_changeRegionsButton);

		_toolaBar.addSeparator();

		// Run Button
		_runButton = new JButton();
		_runButton.setToolTipText("Run Simulation");
		_runButton.setIcon(new ImageIcon("resources/icons/run.png"));
		_runButton.addActionListener((e) -> runSimulation());
		_toolaBar.add(_runButton);

		// Stop Button
		_stopButton = new JButton();
		_stopButton.setToolTipText("Stop Simulation");
		_stopButton.setIcon(new ImageIcon("resources/icons/stop.png"));
		_stopButton.addActionListener((e) -> stopSimulation());
		_toolaBar.add(_stopButton);

		// Spinner
		_toolaBar.addSeparator();
		_toolaBar.add(new JLabel("Steps: "));
		_spinner = new JSpinner(new SpinnerNumberModel(10000, 0, 10000, 100));
		_spinner.setMaximumSize(new Dimension(70, 35));
		_spinner.setMinimumSize(new Dimension(70, 35));
		_spinner.setPreferredSize(new Dimension(70, 35));
		_toolaBar.add(_spinner);

		// Delta Time Field
		_deltaTimeField = new JTextField("0.03");
		JLabel delta_time = new JLabel("Delta Time: ");
		_deltaTimeField.setMaximumSize(new Dimension(70, 35));
		_deltaTimeField.setMinimumSize(new Dimension(70, 35));
		_deltaTimeField.setPreferredSize(new Dimension(70, 35));
		_toolaBar.addSeparator();
		_toolaBar.add(delta_time);
		_toolaBar.add(_deltaTimeField);

		// Quit Button
		_toolaBar.add(Box.createGlue());
		_toolaBar.addSeparator();
		_quitButton = new JButton();
		_quitButton.setToolTipText("Quit");
		_quitButton.setIcon(new ImageIcon("resources/icons/exit.png"));
		_quitButton.addActionListener((e) -> ViewUtils.quit(this));
		_toolaBar.add(_quitButton);

		_fc = new JFileChooser();
		_fc.setCurrentDirectory(new File(System.getProperty("user.dir") + "/resources/examples"));

		_changeRegionsDialog = new ChangeRegionsDialog(_ctrl);

		activateButtons(true);
	}

	private void loadFile() {
		int returnValue = _fc.showOpenDialog(ViewUtils.getWindow(this));
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = _fc.getSelectedFile();
			try {
				JSONObject jsonObject = new JSONObject(new JSONTokener(new FileInputStream(selectedFile)));
				int width = jsonObject.getInt("width");
				int height = jsonObject.getInt("height");
				int cols = jsonObject.getInt("cols");
				int rows = jsonObject.getInt("rows");

				_ctrl.reset(cols, rows, width, height);
				_ctrl.load_data(jsonObject);
			} catch (Exception e) {
				ViewUtils.showErrorMsg("Error al abrir el archivo: " + e.getMessage());
			}
		}
	}

	private void openMap() {
		MapWindow mapWindow = new MapWindow(new Frame(), _ctrl);
		mapWindow.setLocation(50, 50);
		mapWindow.setVisible(true);
	}

	private void openChangeRegionsDialog() {
		_changeRegionsDialog.open(ViewUtils.getWindow(this));
	}

	private void runSimulation() {
		_stopped = false;
		double dt = Double.parseDouble(_deltaTimeField.getText());
		int steps = (int) _spinner.getValue();
		run_sim(steps, dt);
	}

	private void run_sim(int n, double dt) {
		if (n > 0 && !_stopped) {
			try {
				long startTime = System.currentTimeMillis();
				_ctrl.advance(dt);
				long stepTimeMs = System.currentTimeMillis() - startTime;
				long delay = (long) (dt * 1000 - stepTimeMs);
				Thread.sleep(delay > 0 ? delay : 0);
				SwingUtilities.invokeLater(() -> run_sim(n - 1, dt));
			} catch (Exception e) {
				ViewUtils.showErrorMsg("Error during simulation: " + e.getMessage());
				_stopped = true;
				activateButtons(true);
			}
		} else {
			_stopped = true;
			activateButtons(true);
		}
	}

	private void stopSimulation() {
		_stopped = true;
		activateButtons(true);
	}

	private void activateButtons(boolean activate) {
		Component[] components = _toolaBar.getComponents();
		for (Component component : components) {
			if (component instanceof JButton && component != _stopButton) {
				component.setEnabled(activate);
			}
		}
	}
}