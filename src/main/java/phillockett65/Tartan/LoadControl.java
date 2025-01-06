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
 * LoadControl is a class that is responsible for the controls of the 
 * load tartan window.
 */
package phillockett65.Tartan;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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


public class LoadControl extends Stage {



    /************************************************************************
     * Support code for "Load" pop-up. 
     */

    private Model model;

    private Scene scene;

    private VBox root;

    private Button load;
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
        load = new Button("Load");

        cancel.setMnemonicParsing(false);
        load.setMnemonicParsing(false);
    
        cancel.setOnAction(event -> {
            result = false;
            this.close();
        });

        load.setOnAction(event -> {
            result = true;
            this.close();
        });

        cancel.setTooltip(new Tooltip("Cancel Load"));
        load.setTooltip(new Tooltip("Load selected tartan"));

        Region region = new Region();

        options.getChildren().add(cancel);
        options.getChildren().add(region);
        options.getChildren().add(load);

        HBox.setHgrow(region, Priority.ALWAYS);

        return options;
    }

    /**
     * Builds the selected pairs display as a HBox.
     * @return the HBox that represents the selected pairs display.
     */
    private ListView<String> buildListView() {
        ListView<String> selection = new ListView<String>();

        selection.setPrefSize(350, 240);
        selection.setItems(model.getTartanList());
        selection.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                model.setName(newValue);
                load.setDisable(false);
            }
        } );

        selection.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                result = model.isNamed();
                this.close();
            }
        });

        selection.setTooltip(new Tooltip("Select a tartan from the list"));

        return selection;
    }

    /**
     * Builds the User controls as a VBox.
     * @return the VBox that captures the User controls.
     */
    private VBox buildControlPanel() {
        VBox panel = new VBox();

        panel.setSpacing(10);
        panel.setPadding(new Insets(10.0));

        Label prompt = new Label("Select the tartan to load:");

        panel.getChildren().add(prompt);
        panel.getChildren().add(buildListView());
        panel.getChildren().add(buildOptions());

        return panel;
    }

    /**
     * Initialize the control.
     */
    private void init(String title) {
        model = Model.getInstance();

        this.setTitle(title);
        this.resizableProperty().setValue(false);
        this.initStyle(StageStyle.UNDECORATED);
        this.initModality(Modality.APPLICATION_MODAL);

        root = new VBox();

        root.getChildren().add(buildTopBar());
        root.getChildren().add(buildControlPanel());
        root.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

        scene = new Scene(root);

        this.setScene(scene);
    }



    /************************************************************************
     * Support code for the Operation of the Controller.
     */

    /**
     * Synchronise all controls with the model.
     */
    private void syncUI() {
        load.setDisable(!model.isNamed());
    }


    /**
     * Constructor.
     */
    private LoadControl() {
        super();
    }


    /**
     * Construct and launch the Load Tartan Control and wait for user input.
     * @return true if the load was requested, false if cancelled.
     */
    public static boolean showControl(String title) {
        LoadControl control = new LoadControl();

        control.init(title);
        control.syncUI();
        control.showAndWait();

        return control.result;
    }

}
