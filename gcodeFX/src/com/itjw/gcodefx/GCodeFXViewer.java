package com.itjw.gcodefx;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.itjw.gcodefx.model.ContentModel;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GCodeFXViewer extends Application {
    
    private static MainViewController controller;

	private static ContentModel contentModel =  new ContentModel();

    public static MainViewController getController() {
		return controller;
	}

	public static ContentModel getContentModel() {
		return contentModel;
	}

	/**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent main = loadFromFXML();
        controller.setArguments(this.getParameters());
        
        Scene scene = new Scene(main, 1280, 768,true);
        primaryStage.setTitle("GCodeFXViewer");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1280);
        primaryStage.setMinHeight(768);
        primaryStage.show();
    }

    public static Parent loadFromFXML() {
        if (controller!=null) throw new IllegalStateException("UI already loaded");
        FXMLLoader fxmlLoader = new FXMLLoader(GCodeFXViewer.class.getResource("main.fxml"));
        try {
            fxmlLoader.load();
        } catch (IOException ex) {
            Logger.getLogger(GCodeFXViewer.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
        Parent root = fxmlLoader.getRoot();
        root.getStylesheets().add(GCodeFXViewer.class.getResource("GCodeFXViewer.css").toExternalForm());
        controller = fxmlLoader.getController();
        return root;
    }
}
