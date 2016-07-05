package com.itjw.gcodefx;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.controlsfx.dialog.ProgressDialog;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.NavigationActions.SelectionPolicy;
import org.fxmisc.richtext.StyleSpansBuilder;
import org.reactfx.Change;
import org.reactfx.EventStream;
import org.reactfx.EventStreams;

import com.itjw.gcode.AbstractGCode;
import com.itjw.gcode.GCodeReader;
import com.itjw.gcodefx.Xform.RotateOrder;

import javafx.application.Application.Parameters;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SubScene;
import javafx.scene.control.Accordion;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

/**
 * FXML Controller class
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class MainViewController implements Initializable {

	private static final String COMMAND_PATTERN = "\\b([GM][0-9]+)\\b";
	private static final String PARAMETER_PATTERN = "\\b([XYZIJEF][0-9.]+)\\b";
	private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
	private static final String COMMENT_PATTERN = ";[^\n]*";

	private static final Pattern PATTERN = Pattern.compile(
			"(?<COMMAND>"      + COMMAND_PATTERN + ")"
					+ "|(?<PARAMETER>" + PARAMETER_PATTERN + ")"
					+ "|(?<STRING>"    + STRING_PATTERN + ")"
					+ "|(?<COMMENT>"   + COMMENT_PATTERN + ")"
			);

	@FXML
	private VBox vboxContainer;

	@FXML
	private TextArea logView;

	@FXML
	private ScrollPane editorContainer;

	@FXML
	private Pane viewContainer;

	@FXML
	private AnchorPane settingsContainer;

	private Accordion settingsPanel;

	private final CodeArea codeArea = new CodeArea();

	private SubScene subScene;

	final PerspectiveCamera camera = new PerspectiveCamera(true);
	final Xform cameraXform =new Xform(RotateOrder.XYZ);
	final double cameraDistance = 650;

	final AmbientLight ambientLight = new AmbientLight(Color.DARKGREY);
	final PointLight light1 = new PointLight(Color.WHITE);
	final PointLight light2 = new PointLight(Color.ANTIQUEWHITE);
	final PointLight light3 = new PointLight(Color.ALICEBLUE);

	final Group printerSpace = new Group();
	{
		printerSpace.setId("printerSpace");
	}

	JavaFXMachineProcessor jfxProcessor = new JavaFXMachineProcessor();
	
	private String inputFileName;

	/**
	 * Initializes the controller class.
	 *
	 * @param url
	 * @param rb
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {

		try {
			// CREATE SETTINGS PANEL
			settingsPanel = FXMLLoader.load(MainViewController.class.getResource("settings.fxml"));
			// SETUP SPLIT PANE
			settingsContainer.getChildren().add(settingsPanel);
			AnchorPane.setLeftAnchor(settingsPanel, 0d);
			AnchorPane.setTopAnchor(settingsPanel, 0d);
			AnchorPane.setRightAnchor(settingsPanel, 0d);
			AnchorPane.setBottomAnchor(settingsPanel, 0d);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// setup the code area 
		codeArea.textProperty().addListener(
				(ov, oldText, newText) -> {
					int lastKwEnd = 0;
					StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
					Matcher matcher = PATTERN.matcher(newText);
					while (matcher.find()) {
						String styleClass =
								matcher.group("COMMAND") != null ? "command" :
									matcher.group("PARAMETER") != null ? "parameter" :
										matcher.group("STRING") != null ? "string" :
											matcher.group("COMMENT") != null ? "comment" :
												null; /* never happens */
						assert styleClass != null;
						spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
						spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
						lastKwEnd = matcher.end();
					}
					spansBuilder.add(Collections.emptyList(), newText.length() - lastKwEnd);
					codeArea.setStyleSpans(0, spansBuilder.create());
				});

		EventStream<Change<String>> textEvents = EventStreams.changesOf(codeArea.textProperty());
		textEvents.reduceSuccessions((a, b) -> b, Duration.ofMillis(500)).subscribe(code -> {
			compile(code.getNewValue());
		});

		codeArea.replaceText("G1 X230 Y25 Z0.35 F5000\nG1 X20 E25 F1000\n");
		codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
		codeArea.currentParagraphProperty().addListener(
				(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue)->{
					GCodeFXViewer.getContentModel().setHighlight(jfxProcessor.getXform4Line(newValue+1));
					// this is the line number
					// System.out.println("Paragraph "+newValue);
				});
		editorContainer.setContent(codeArea);
		// setup the 3D view area
		setSubScene();
		GCodeFXViewer.getContentModel().subSceneProperty().addListener((ov, oldVal, newVal)->{
			viewContainer.getChildren().clear();	
			setSubScene();			
		});
	}

	private void setSubScene() {
		subScene = GCodeFXViewer.getContentModel().getSubScene();
		subScene.widthProperty().bind(viewContainer.widthProperty());
		subScene.heightProperty().bind(viewContainer.heightProperty());
		viewContainer.getChildren().add(subScene);
	}

	private static class GcodeParseService extends Service<List<Layer>> {
		private final String code;
		private final JavaFXMachineProcessor jfxProcessor;

		public GcodeParseService(JavaFXMachineProcessor jfxProcessor, String code) {
			super();
			this.jfxProcessor=jfxProcessor;
			this.code = code;
		}

		protected Task<List<Layer>> createTask() {
			return new Task<List<Layer>>() {
				protected List<Layer> call() throws IOException {
					List<AbstractGCode> gcodes = GCodeReader.parseStrings(code);
					if(gcodes != null){
	                    updateMessage("Generating 3D objects");
	                    int size = gcodes.size();
	                    int i=1;
						for(AbstractGCode gcode:gcodes){
							updateProgress(i++, size);
							gcode.process(jfxProcessor);
						}
						return (jfxProcessor.getGcodeGroup());
					}
					return null;
				}
			};
		}
	}

	private void compile(final String code) {
		logView.setText("");
		printerSpace.getChildren().clear();
		GcodeParseService service = new GcodeParseService(jfxProcessor,code);
		service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@SuppressWarnings("unchecked")
			@Override
			public void handle(WorkerStateEvent t) {
				Object res = t.getSource().getValue();
				if(res!=null && res instanceof List<?>){
					printerSpace.getChildren().addAll((List<? extends Node>)res);
					GCodeFXViewer.getContentModel().setContent(printerSpace);
				}
			}
		});
		ProgressDialog pd=new ProgressDialog(service);
		pd.setTitle("Loading model");
		pd.setHeaderText("Progress parsing and generating model");
		service.start();
	}

	/**
	 * Returns the location of the Jar archive or .class file the specified
	 * class has been loaded from. <b>Note:</b> this only works if the class is
	 * loaded from a jar archive or a .class file on the locale file system.
	 *
	 * @param cls class to locate
	 * @return the location of the Jar archive the specified class comes from
	 */
	public static File getClassLocation(Class<?> cls) {
		String className = cls.getName();
		ClassLoader cl = cls.getClassLoader();
		URL url = cl.getResource(className.replace(".", "/") + ".class");
		String urlString = url.toString().replace("jar:", "");
		if (!urlString.startsWith("file:")) {
			throw new IllegalArgumentException("The specified class\"" + cls.getName() + "\" has not been loaded from a location on the local filesystem.");
		}
		urlString = urlString.replace("file:", "");
		urlString = urlString.replace("%20", " ");
		int location = urlString.indexOf(".jar!");
		if (location > 0) {
			urlString = urlString.substring(0, location) + ".jar";
		}
		return new File(urlString);
	}

	@FXML
	private void onLoadFile(ActionEvent e) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open GCode File");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("GCode files (*.gcode, *.gc)", "*.gcode", "*.gc"));
		File f = fileChooser.showOpenDialog(null);
		if (f == null) {
			return;
		}
		loadFile( f.getAbsolutePath());
		inputFileName=f.getAbsolutePath();
	}

	@FXML
	private void onSaveFile(ActionEvent e) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save GCode File");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("GCode files (*.gcode, *.gc)", "*.gcode", "*.gc"));
		if(inputFileName!=null) fileChooser.setInitialFileName(inputFileName);
		File f = fileChooser.showSaveDialog(null);
		if (f == null) return;
		String fName = f.getAbsolutePath();
		try {
			Files.write(Paths.get(fName), codeArea.getText().getBytes("UTF-8"));
		} catch (IOException ex) {
			Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@FXML
	private void onClose(ActionEvent e) {
		System.exit(0);
	}

	@FXML
	private void onResetPerspective(ActionEvent e) {
		GCodeFXViewer.getContentModel().resetCamera(true);
	}

	public TextArea getLogView() {
		return logView;
	}

	public CodeArea getCodeArea(){
		return codeArea;
	}

	private void loadFile(String fName) {
		try {
			codeArea.replaceText(new String(Files.readAllBytes(Paths.get(fName)), "UTF-8"));
			codeArea.start(SelectionPolicy.CLEAR);
		} catch (IOException ex) {
			Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void setArguments(Parameters parameters) {
		if(!parameters.getUnnamed().isEmpty()){
			inputFileName=parameters.getUnnamed().get(0);
			loadFile(inputFileName);
		}		
	}

}
