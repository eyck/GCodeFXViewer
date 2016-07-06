/*******************************************************************************
 * Copyright (c) 2016, Eyck Jentzsch and others
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of GCodeFXViewer nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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
import com.itjw.gcodefx.model.ContentModel;

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

	private SettingsController settingsController;
	
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
			FXMLLoader fxmlLoader = new FXMLLoader(GCodeFXViewer.class.getResource("settings.fxml"));
			Accordion settingsPanel = fxmlLoader.load();
			settingsController = fxmlLoader.getController();
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
		final ContentModel contentModel = GCodeFXViewer.getContentModel();
		contentModel.subSceneProperty().addListener((ov, oldVal, newVal)->{
			viewContainer.getChildren().clear();	
			setSubScene();			
		});
		contentModel.selectedGcodeProperty().addListener((ov, oldVal, newVal)->{
			if(newVal!=null)
				codeArea.moveTo(codeArea.position(newVal.getLineNo()-1, 0).toOffset());
		});
		settingsController.firstLayerProperty().addListener((ov, oldVal, newVal)->{
			updateLayerVisibility();
		});
		settingsController.lastLayerProperty().addListener((ov, oldVal, newVal)->{
			updateLayerVisibility();
		});
	}

	private void setSubScene() {
		subScene = GCodeFXViewer.getContentModel().getSubScene();
		subScene.widthProperty().bind(viewContainer.widthProperty());
		subScene.heightProperty().bind(viewContainer.heightProperty());
		viewContainer.getChildren().add(subScene);
	}

	private void updateLayerVisibility(){
		int i=1;
		double first = settingsController.getFirstLayer();
		double last = settingsController.getLastLayer();
		for(Node n: printerSpace.getChildren()){
			n.setVisible(i>=first && i<=last);
			i++;
		}
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
		jfxProcessor.initialize();
		GcodeParseService service = new GcodeParseService(jfxProcessor,code);
		service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@SuppressWarnings("unchecked")
			@Override
			public void handle(WorkerStateEvent t) {
				Object res = t.getSource().getValue();
				if(res!=null && res instanceof List<?>){
					printerSpace.getChildren().addAll((List<? extends Node>)res);
					Double dim[] = jfxProcessor.plateDimensions;
					ContentModel contentModel = GCodeFXViewer.getContentModel();
					contentModel.setContent(printerSpace);
					contentModel.setViewPoint(-dim[0]/2, -dim[1]/2, 0d);
					contentModel.getCameraPosition().setZ(-dim[2]*2.5);
					contentModel.getCameraRotate().rx.setAngle(30);
					int count = printerSpace.getChildren().size()+1;
					settingsController.setFirstLayerValues(count, 1);
					settingsController.setLastLayerValues(count, count);
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
