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

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application.Parameters;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class MainViewController implements Initializable {

	private static final Logger logger = Logger.getLogger(MainViewController.class.getName());

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

	@FXML
	private TimelineDisplay timelineDisplay;
	
	Timeline  theTimeline = new Timeline();
	private SimpleIntegerProperty selectedLayer = new SimpleIntegerProperty(0);
	private SimpleIntegerProperty selectedLine = new SimpleIntegerProperty(0);
    int animationType=1;

    public Button startBtn;
    public Button rwBtn;
    public ToggleButton playBtn;
    public Button ffBtn;
    public Button endBtn;
    public ToggleButton loopBtn;

	private final CodeArea codeArea = new CodeArea();

	private SubScene subScene;

	final PerspectiveCamera camera = new PerspectiveCamera(true);
	final Xform cameraXform =new Xform(RotateOrder.XYZ);
	final double cameraDistance = 650;

	final Group printerSpace = new Group();
	{
		printerSpace.setId("printerSpace");
	}

	JavaFXMachineProcessor jfxProcessor = new JavaFXMachineProcessor();

	private String inputFileName;

	private TextAreaStream textAreaStream;
    
	private TimelineController timelineController;

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
			logger.log(Level.SEVERE, null, e);
		}
		// setup logging
		textAreaStream = new TextAreaStream(getLogView());
		Logger rootLogger = Logger.getLogger("");
		rootLogger.addHandler(textAreaStream.getLogHandler());
		// System.setErr(textAreaStream);    // redirect System.err
		// System.setOut(textAreaStream);

		final ContentModel contentModel = GCodeFXViewer.getContentModel();
        // create timelineController;
        timelineController = new TimelineController(startBtn,rwBtn,playBtn,ffBtn,endBtn,loopBtn);
        timelineController.timelineProperty().bind(contentModel.timelineProperty());
        timelineDisplay.timelineProperty().bind(contentModel.timelineProperty());
        selectedLayer.addListener((ov, o, n)->{
        	if(n!=null && n.intValue()>0){ 
        		int i=0;
        		for(Node node: printerSpace.getChildren()){
        			node.setVisible(n.intValue()>i++);
        		}
        	}
        });
        selectedLine.addListener((ov, o, n)->{
        	if(n!=null && n.intValue()>0){ 
        		codeArea.moveTo(codeArea.position(n.intValue(), 1).toOffset());
        	}
        });
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
		//codeArea.replaceText("G0 F6000 X119.175 Y115.993 Z25.000\nM104 S0\nM140 S0\nG91\nG1 E-1 F300\nG1 Z+0.5 E-5 X-20 Y-20 F6000\nG28 X0 Y0\nM84\nG90\n");
		codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
		codeArea.currentParagraphProperty().addListener(
				(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue)->{
					GCodeFXViewer.getContentModel().setHighlight(jfxProcessor.getNode4Line(newValue+1));
				});
		editorContainer.setContent(codeArea);
		// setup the 3D view area
		setSubScene();
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
						try{
						for(AbstractGCode gcode:gcodes){
							updateProgress(i++, size);
							gcode.process(jfxProcessor);
						}
						} catch(Throwable t){
							logger.log(Level.SEVERE, "Failure parsing gcode", t);
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
		GCodeFXViewer.getContentModel().setTimeline(null);
		jfxProcessor.initialize();
		GcodeParseService service = new GcodeParseService(jfxProcessor,code);
		service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@SuppressWarnings({ "unchecked" })
			@Override
			public void handle(WorkerStateEvent t) {
				logger.log(Level.INFO, "File "+inputFileName+" loaded");
				Object res = t.getSource().getValue();
				if(res!=null && res instanceof List<?>){
					printerSpace.getChildren().addAll((List<? extends Node>)res);
					Point3D dim = jfxProcessor.getPlateDimensions();
					ContentModel contentModel = GCodeFXViewer.getContentModel();
					contentModel.setContent(printerSpace);
					if(dim!=null) contentModel.setDimension(dim);
					contentModel.resetCamera(true);
					
					int count = printerSpace.getChildren().size()+1;
					settingsController.setFirstLayerValues(count, 1);
					settingsController.setLastLayerValues(count, count);
					logger.log(Level.INFO, "Added "+printerSpace.getChildren().size()+" layer(s)");
					theTimeline.setCycleCount(Timeline.INDEFINITE);
			        theTimeline.setAutoReverse(false);
			        KeyFrame keyFrame=null;
			        switch(animationType){
			        case 0:{
				        KeyValue keyValue = new KeyValue(contentModel.getCameraRotate().rz.angleProperty(), 360);
				        keyFrame = new KeyFrame(javafx.util.Duration.seconds(30), keyValue);
			        }
			        break;
			        case 1:{
				        KeyValue keyValue = new KeyValue(selectedLayer, printerSpace.getChildren().size());
				        keyFrame = new KeyFrame(javafx.util.Duration.seconds(printerSpace.getChildren().size()/2), keyValue);
			        }
			        break;
			        case 2:{
			        	// TODO: needs implementation
				        KeyValue keyValue = new KeyValue(selectedLine, printerSpace.getChildren().size());
				        keyFrame = new KeyFrame(javafx.util.Duration.seconds(printerSpace.getChildren().size()/2), keyValue);
			        }
			        break;
			        default:
				        break;	
					}
			        //add the keyframe to the timeline
			        if(keyFrame!=null) theTimeline.getKeyFrames().add(keyFrame);
			        contentModel.setTimeline(theTimeline);
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
