package simulator.launcher;

import java.io.*;
import java.util.*;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONObject;
import org.json.JSONTokener;

import simulator.factories.*;
import simulator.misc.Utils;
import simulator.model.*;
import simulator.control.*;
import simulator.view.*;

public class Main {

	private enum ExecMode {
		BATCH("batch", "Batch mode"), GUI("gui", "Graphical User Interface mode");

		private String _tag;
		private String _desc;

		private ExecMode(String modeTag, String modeDesc) {
			_tag = modeTag;
			_desc = modeDesc;
		}

		public String get_tag() {
			return _tag;
		}

		public String get_desc() {
			return _desc;
		}
	}

	// default values for some parameters
	//
	private final static Double _default_time = 10.0;
	private final static Double _default_dt = 0.03;
	// some attributes to stores values corresponding to command-line parameters
	//
	private static Double _time = null;
	private static boolean _sv = false;
	private static Double _delta_time = null;
	private static String _in_file = null;
	private static String _out_file = null;
	private static ExecMode _mode = ExecMode.GUI;
	public static Factory<Animal> _animal_factory;
	public static Factory<Region> _region_factory;

	private static void parse_args(String[] args) {
		// define the valid command line options
		//
		Options cmdLineOptions = build_options();
		// parse the command line as provided in args
		//
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine line = parser.parse(cmdLineOptions, args);
			parse_delta_time_option(line);
			parse_help_option(line, cmdLineOptions);
			parse_mode_option(line);
			parse_in_file_option(line);
			parse_out_file_option(line);
			parse_sv_option(line);
			parse_time_option(line);

			// if there are some remaining arguments, then something wrong is
			// provided in the command line!
			//
			String[] remaining = line.getArgs();
			if (remaining.length > 0) {
				String error = "Illegal arguments:";
				for (String o : remaining)
					error += (" " + o);
				throw new ParseException(error);
			}
		} catch (ParseException e) {
			System.err.println(e.getLocalizedMessage());
			System.exit(1);
		}
	}

	private static Options build_options() {
		Options cmdLineOptions = new Options();
		// delta time
		cmdLineOptions.addOption(Option.builder("dt").longOpt("delta-time").hasArg().desc(
				"An real number representing the total simulation time in seconds. Default value: " + _default_dt + ".")
				.build());
		// help
		cmdLineOptions.addOption(Option.builder("h").longOpt("help").desc("Print this message.").build());
		// input
		cmdLineOptions.addOption(Option.builder("i").longOpt("input").hasArg().desc("A configuration file.").build());
		// mode
		cmdLineOptions.addOption(Option.builder("m").longOpt("mode").hasArg()
				.desc("Execution Mode. Possible values: 'batch' (Batchmode),"
						+ " 'gui' (Graphical User Interface mode)." + "Default value: 'gui'.")
				.build());
		// output
		cmdLineOptions.addOption(
				Option.builder("o").longOpt("output").hasArg().desc("Output file, where output is written.").build());
		// sv
		cmdLineOptions.addOption(
				Option.builder("sv").longOpt("simple-viewer").desc("Show the viewer window in console mode.").build());
		// time
		cmdLineOptions.addOption(Option.builder("t").longOpt("time").hasArg()
				.desc("An real number representing the total simulation time in seconds. Default value: "
						+ _default_time + ".")
				.build());
		return cmdLineOptions;
	}

	private static void parse_help_option(CommandLine line, Options cmdLineOptions) {
		if (line.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(Main.class.getCanonicalName(), cmdLineOptions, true);
			System.exit(0);
		}
	}

	private static void parse_mode_option(CommandLine line) throws ParseException {
		String StringMode = line.getOptionValue("m");
		if (StringMode != null) {
			if (StringMode.equals(ExecMode.GUI.get_tag()))
				_mode = ExecMode.GUI;
			else if (StringMode.equals(ExecMode.BATCH.get_tag()))
				_mode = ExecMode.BATCH;
			else
				throw new ParseException("This mode is not exited");
		}
	}

	private static void parse_in_file_option(CommandLine line) throws ParseException {
		_in_file = line.getOptionValue("i");
		if (_mode == ExecMode.BATCH && _in_file == null) {
			throw new ParseException("In batch mode an input configuration file is required");
		}
	}

	private static void parse_out_file_option(CommandLine line) throws ParseException {
		_out_file = line.getOptionValue("o");
		if (_mode == ExecMode.BATCH && _out_file == null) {
			throw new ParseException("In batch mode an output configuration file is required");
		}
	}

	private static void parse_time_option(CommandLine line) throws ParseException {
		String t = line.getOptionValue("t", _default_time.toString());
		try {
			_time = Double.parseDouble(t);
			assert (_time >= 0);
		} catch (Exception e) {
			throw new ParseException("Invalid value for time: " + t);
		}
	}

	private static void parse_sv_option(CommandLine line) throws ParseException {
		if (line.hasOption("sv")) {
			_sv = true;
		}

	}

	public static void parse_delta_time_option(CommandLine line) throws ParseException {
		String dt = line.getOptionValue("dt", _default_dt.toString());
		try {
			_delta_time = Double.parseDouble(dt);
			assert (_time >= 0);
		} catch (Exception e) {
			throw new ParseException("Invalid value for time: " + dt);
		}
	}

	public static void init_factories() {
		// Estrategias de selecci�n
		List<Builder<SelectionStrategy>> selection_strategy_builders = new ArrayList<>();
		selection_strategy_builders.add(new SelectFirstBuilder());
		selection_strategy_builders.add(new SelectClosestBuilder());
		selection_strategy_builders.add(new SelectYoungestBuilder());
		Factory<SelectionStrategy> selection_strategy_factory = new BuilderBasedFactory<SelectionStrategy>(
				selection_strategy_builders);

		// Animales
		List<Builder<Animal>> animal_builders = new ArrayList<>();
		animal_builders.add(new SheepBuilder(selection_strategy_factory));
		animal_builders.add(new WolfBuilder(selection_strategy_factory));
		// A�adir otros builders de animales seg�n sea necesario
		_animal_factory = new BuilderBasedFactory<Animal>(animal_builders);

		// Regiones
		List<Builder<Region>> region_builders = new ArrayList<>();
		region_builders.add(new DefaultRegionBuilder());
		region_builders.add(new DynamicSupplyRegionBuilder());
		_region_factory = new BuilderBasedFactory<Region>(region_builders);

	}

	private static JSONObject load_JSON_file(InputStream in) {
		return new JSONObject(new JSONTokener(in));
	}
	

	private static void start_batch_mode() throws Exception {
		InputStream is = new FileInputStream(new File(_in_file));
	
		JSONObject json = load_JSON_file(is);
		OutputStream os = new FileOutputStream(new File(_out_file));
		Simulator sim = new Simulator(json.getInt("cols"), json.getInt("rows"), json.getInt("width"),
				json.getInt("height"), _animal_factory, _region_factory);
		Controller controller = new Controller(sim);
		controller.load_data(json);
		controller.run(_time, _delta_time, _sv, os);
		os.close();

	}

	private static void start_GUI_mode() throws Exception {
		// TODO VOLVER A PONER TRY CATCH
		/*
		 * try {
		 * 
		 * } catch (Exception e) { throw new
		 * UnsupportedOperationException("GUI mode is not ready yet ..."); }
		 */
        if(_in_file==null) {
        	_in_file = "resources/examples/ex1.json";//si no hay file carga el ex1.json por defecto
        }
		InputStream is = new FileInputStream(new File(_in_file));
		JSONObject json = load_JSON_file(is);
		is.close();
		Simulator sim = new Simulator(json.getInt("cols"), json.getInt("rows"), json.getInt("width"),
				json.getInt("height"), _animal_factory, _region_factory);
		Controller controller = new Controller(sim);
		controller.load_data(json);
		SwingUtilities.invokeAndWait(() -> new MainWindow(controller));
	}

	private static void start(String[] args) throws Exception {
		init_factories();
		parse_args(args);
		switch (_mode) {
		case BATCH:
			start_batch_mode();
			break;
		case GUI:
			start_GUI_mode();
			break;
		}
	}

	public static void main(String[] args) {
		Utils._rand.setSeed(2147483647l);// Seteamos la misma semilla
		try {
			start(args);
		} catch (Exception e) {
			System.err.println("Something went wrong ...");
			System.err.println();
			e.printStackTrace();
		}
	}
}
