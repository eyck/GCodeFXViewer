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

import java.io.IOException;
import java.util.logging.Handler;
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
		// suppress the logging output to the console
//	    Logger rootLogger = Logger.getLogger("");
//	    Handler[] handlers = rootLogger.getHandlers();
//	    if (handlers[0] instanceof ConsoleHandler) {
//	      rootLogger.removeHandler(handlers[0]);
//	    }
	    // and add out handler
//	    rootLogger.addHandler(BufferedHandler.INSTANCE);
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
