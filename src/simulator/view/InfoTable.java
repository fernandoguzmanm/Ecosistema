package simulator.view;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableModel;

public class InfoTable extends JPanel {
	String _title;
	TableModel _tableModel;

	InfoTable(String title, TableModel tableModel) {
		_title = title;
		_tableModel = tableModel;
		initGUI();
	}

	private void initGUI() {
		setLayout(new BorderLayout());

		Border borde = BorderFactory.createEtchedBorder();
		TitledBorder tituloBorde = BorderFactory.createTitledBorder(borde, _title);
		tituloBorde.setTitleJustification(tituloBorde.CENTER);
		setBorder(tituloBorde);

		JTable table = new JTable(_tableModel);
		JScrollPane scrollPane = new JScrollPane(table);
		add(scrollPane, BorderLayout.CENTER);

	}
}
