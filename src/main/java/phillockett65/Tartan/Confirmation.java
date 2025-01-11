/*  Tartan - a JavaFX based Tartan image generator.
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
 * Confirmation is a class that is responsible for the controls of the 
 * confirm action window.
 */
package phillockett65.Tartan;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Confirmation extends Stage {


    /************************************************************************
     * Support code for "Confirmation" pop-up. 
     */

    private Scene scene;

    private VBox root;

    private Button confirm;
    private Button cancel;

    private double x = 0.0;
    private double y = 0.0;

    private boolean result = false;



    /************************************************************************
      * Support code for the Initialization of the Controller.
      */

    /**
     * Builds the top-bar as a HBox and includes the cancel button the mouse 
     * press and drag handlers.
     * @return the HBox that represents the top-bar.
     */
    private HBox buildTopBar() {
        HBox topBar = new HBox();
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER);
        topBar.setPrefHeight(Default.TOP_BAR_HEIGHT.getFloat());

        // Make window dragable.
        topBar.setOnMousePressed(mouseEvent -> {
            x = mouseEvent.getSceneX();
            y = mouseEvent.getSceneY();
        });

        topBar.setOnMouseDragged(mouseEvent -> {
            this.setX(mouseEvent.getScreenX() - x);
            this.setY(mouseEvent.getScreenY() - y);
        });

        Image image = new Image(getClass().getResourceAsStream("icon32.png"));
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(28);
        imageView.setFitWidth(28);
        imageView.setPickOnBounds(true);
        imageView.setPreserveRatio(true);

        Label heading = new Label(" " + this.getTitle());
        Region region = new Region();

        Pane cancel = Model.buildCancelButton();
        cancel.setOnMouseClicked(event -> {
            result = false;
            this.close();
        });

        topBar.getChildren().add(imageView);
        topBar.getChildren().add(heading);
        topBar.getChildren().add(region);
        topBar.getChildren().add(cancel);
        
        HBox.setHgrow(region, Priority.ALWAYS);

        return topBar;
    }

    /**
     * Builds the options buttons as a HBox and includes the action event 
     * handlers for both the done and clear buttons.
     * @return the HBox that represents the options buttons.
     */
    private HBox buildOptions() {
        HBox options = new HBox();

        cancel = new Button("Cancel");
        confirm = new Button("Confirm");

        cancel.setMnemonicParsing(false);
        confirm.setMnemonicParsing(false);
    
        cancel.setOnAction(event -> {
            result = false;
            this.close();
        });

        confirm.setOnAction(event -> {
            result = true;
            this.close();
        });

        cancel.setTooltip(new Tooltip("Cancel action"));
        confirm.setTooltip(new Tooltip("Continue with action"));

        Region region = new Region();

        options.getChildren().add(cancel);
        options.getChildren().add(region);
        options.getChildren().add(confirm);

        HBox.setHgrow(region, Priority.ALWAYS);

        return options;
    }

    /**
     * Builds the User controls as a VBox.
     * @return the VBox that captures the User controls.
     */
    private VBox buildControlPanel(String query) {
        VBox panel = new VBox();

        panel.setSpacing(10);
        panel.setPadding(new Insets(10.0));

        Label prompt = new Label(query);

        panel.getChildren().add(prompt);
        panel.getChildren().add(buildOptions());

        return panel;
    }

    /**
     * Initialize the control.
     */
    private void init(String title, String query) {
        this.setTitle(title);
        this.resizableProperty().setValue(false);
        this.initStyle(StageStyle.UNDECORATED);
        this.initModality(Modality.APPLICATION_MODAL);

        root = new VBox();

        root.getChildren().add(buildTopBar());
        root.getChildren().add(buildControlPanel(query));
        root.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

        scene = new Scene(root);

        this.setScene(scene);
    }



    /************************************************************************
     * Support code for the Operation of the Controller.
     */

    /**
     * Constructor.
     */
    private Confirmation() {
        super();
    }


    /**
     * Construct and launch the Confirm Action Control and wait for user input.
     * @return true if the action was confirmed, false if cancelled.
     */
    public static boolean showControl(String title, String query) {
        Confirmation control = new Confirmation();

        control.init(title, query);
        control.showAndWait();

        return control.result;
    }

}
