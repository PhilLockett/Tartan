/*  Tartan - a JavaFX based playing card image generator.
 *
 *  Copyright 2022 Philip Lockett.
 *
 *  This file is part of Tartan.
 *
 *  Tartan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Tartan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Tartan.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * Boilerplate code responsible for launching the JavaFX application. 
 */
package phillockett65.Tartan;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    PrimaryController controller;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("primary.fxml"));

        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        controller = fxmlLoader.getController();

        ObservableList<Image> icons = stage.getIcons();
        icons.add(new Image(getClass().getResourceAsStream("icon32.png")));

        stage.setTitle("Tartan Designer 1.0.1");
        stage.setOnCloseRequest(e -> Platform.exit());
        stage.resizableProperty().setValue(false);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(scene);

        stage.focusedProperty().addListener((obs, oldVal, newVal) -> 
            controller.setFocus(newVal));

        stage.show();

        controller.init(stage);
    }

    @Override
    public void stop() throws Exception {
        // controller.saveState();
    }

    public static void main(String[] args) {
        launch();
    }

}