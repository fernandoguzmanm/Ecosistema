package simulator.view;

import simulator.model.Animal;
import simulator.model.AnimalInfo;
import simulator.model.MapInfo;
import simulator.model.State;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("serial")
public class MapViewer extends AbstractMapViewer {

	// Anchura/altura/ de la simulación -- se supone que siempre van a ser iguales
	// al tamaño del componente
	private int _width;
	private int _height;

	// Número de filas/columnas de la simulación
	private int _rows;
	private int _cols;

	// Anchura/altura de una región
	int _rwidth;
	int _rheight;

	// Mostramos sólo animales con este estado. Los posibles valores de _currState
	// son null, y los valores deAnimal.State.values(). Si es null mostramos todo.
	State _currState;
	int index;
	// En estos atributos guardamos la lista de animales y el tiempo que hemos
	// recibido la última vez para dibujarlos.
	volatile private Collection<AnimalInfo> _objs;
	volatile private Double _time;

	// Una clase auxilar para almacenar información sobre una especie
	private static class SpeciesInfo {
		private Integer _count;
		private Color _color;

		SpeciesInfo(Color color) {
			_count = 0;
			_color = color;
		}
	}

	// Un mapa para la información sobre las especies
	Map<String, SpeciesInfo> _kindsInfo = new HashMap<>();

	// El font que usamos para dibujar texto
	private Font _font = new Font("Arial", Font.BOLD, 12);

	// Indica si mostramos el texto la ayuda o no
	private boolean _showHelp;

	public MapViewer() {
		index = 0;
		initGUI();
	}

	private void initGUI() {

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyChar()) {
				case 'h':
					_showHelp = !_showHelp;
					repaint();
					break;
				case 's':
					// Cambiar _currState al siguiente (de manera circular). Después de null
					// viene el primero de Animal.State.values() y después del último viene null.
					if (index == State.values().length) {
						index = 0;
						_currState = null;
					} else {
						_currState = State.values()[index];
						index++;
					}
					repaint();
				default:
				}
			}

		});

		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent e) {
				requestFocus(); // Esto es necesario para capturar las teclas cuando el ratón está sobre este
								// componente.
			}
		});

		// Por defecto mostramos todos los animales
		_currState = null;

		// Por defecto mostramos el texto de ayuda
		_showHelp = true;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D gr = (Graphics2D) g;
		gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gr.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// Cambiar el font para dibujar texto
		g.setFont(_font);

		// Dibujar fondo blanco
		gr.setBackground(Color.WHITE);
		gr.clearRect(0, 0, _width, _height);

		// Dibujar los animales, el tiempo, etc.
		if (_objs != null)
			drawObjects(gr, _objs, _time);

		// TODO Mostrar el texto de ayuda si _showHelp es true. El texto a mostrar es el
		// siguiente (en 2 líneas):
		//
		// h: toggle help
		// s: show animals of a specific state
		gr.setColor(Color.red);
		if (_showHelp == true) {
			gr.drawString("h: toggle help", 10, 15);
			gr.drawString("s: show animals of a specific state", 10, 30);
		}

	}

	private boolean visible(AnimalInfo a) {
		return (a.get_state() == _currState) || _currState == null;
	}

	private void drawObjects(Graphics2D g, Collection<AnimalInfo> animals, Double time) {

		// Dibujar el grid de regiones
		for (int i = 0; i < _cols; i++)
			for (int j = 0; j < _rows; j++)
				drawCell(j, i, g);

		// Dibujar los animales
		for (AnimalInfo a : animals) {
			// Si no es visible saltamos la iteración
			if (!visible(a))
				continue;

			// La información sobre la especie de 'a'
			SpeciesInfo esp_info = _kindsInfo.get(a.get_genetic_code());

			// Si esp_info es null, añade una entrada correspondiente al mapa. Para el
			// color usa ViewUtils.get_color(a.get_genetic_code())
			if (esp_info == null) {
				esp_info = new SpeciesInfo(ViewUtils.get_color(a.get_genetic_code()));
				_kindsInfo.put(a.get_genetic_code(), esp_info);
			}

			// TODO Incrementar el contador de la especie (es decir el contador dentro de
			// tag_info)
			esp_info._count++;
			// TODO Dibijar el animal en la posicion correspondiente, usando el color
			// tag_info._color. Su tamaño tiene que ser relativo a su edad, por ejemplo
			// edad/2+2. Se puede dibujar usando fillRoundRect, fillRect o fillOval.
			g.setColor(esp_info._color);
			g.fillOval((int) a.get_position().getX(), (int) a.get_position().getY(), (int) a.get_age() / 2 + 2,
					(int) a.get_age() / 2 + 2);
		}

		// TODO Dibujar la etiqueta del estado visible, si no es null.
		if (_currState != null) {
			g.setColor(Color.blue);
			drawStringWithRect(g, 10, _height - 220, "State: " + _currState.toString());
		}
		// TODO Dibujar la etiqueta del tiempo. Para escribir solo 3 decimales puede
		// usar String.format("%.3f", time)
		g.setColor(Color.MAGENTA);
		drawStringWithRect(g, 10, _height - 240, "Time: " + (String.format("%.3f", _time)));
		// TODO Dibujar la información de todas la especies. Al final de cada
		// iteración
		// poner el contador de la especie correspondiente a 0 (para resetear el cuento)
		int base_height = _height - 260;
		for (Entry<String, SpeciesInfo> e : _kindsInfo.entrySet()) {
			g.setColor(e.getValue()._color);
			drawStringWithRect(g, 10, base_height, e.getKey() + ": " + (e.getValue()._count));
			base_height -= 20;
			e.getValue()._count = 0;
		}
	}

	// Un método que dibujar un texto con un rectángulo
	void drawStringWithRect(Graphics2D g, int x, int y, String s) {
		Rectangle2D rect = g.getFontMetrics().getStringBounds(s, g);
		g.drawString(s, x, y);
		g.drawRect(x - 1, y - (int) rect.getHeight(), (int) rect.getWidth() + 1, (int) rect.getHeight() + 5);
	}

	@Override
	public void update(List<AnimalInfo> objs, Double time) {
		// TODO Almacenar objs y time en los atributos correspondientes y llamar a
		// repaint() para redibujar el componente.
		this._objs = objs;
		this._time = time;
		repaint();
	}

	@Override
	public void reset(double time, MapInfo map, List<AnimalInfo> animals) {
		// TODO Actualizar los atributos _width, _height, _cols, _rows, etc.
		// Esto cambia el tamaño del componente, y así cambia el tamaño de la ventana
		// porque en MapWindow llamamos a pack() después de llamar a reset
		this._width = map.get_width();
		this._height = map.get_width();
		this._cols = map.get_cols();
		this._rows = map.get_rows();
		this._rheight = map.get_region_height();
		this._rwidth = map.get_region_width();
		setPreferredSize(new Dimension(map.get_width(), map.get_height()));
		// Dibuja el estado
		update(animals, time);
	}

	private void drawCell(int row, int col, Graphics g) {
		int x = col * _rwidth;
		int y = row * _rheight;
		g.setColor(Color.LIGHT_GRAY);
		g.drawRect(x, y, _rwidth, _rheight);
	}

}
