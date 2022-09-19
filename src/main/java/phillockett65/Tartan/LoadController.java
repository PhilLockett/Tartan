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
 * LoadController is a class that is responsible for the controls of the 
 * load tartan window.
 */
package phillockett65.Tartan;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;

public class LoadController {



    /************************************************************************
     * Support code for "Load" pop-up. 
     */

    private Model model;


    /**
     * Called by the FXML mechanism to initialize the controller.
     */
    @FXML public void initialize() {
//		System.out.println("Controller initialized.");
    }

    /**
     * Called by PrimaryController after the stage has been set. Provided with
     * model reference and completes the initialization.
     */
    public void init(Model model) {
        this.model = model;
        // model.setLoadWindowLaunched(true);

        initializeLoadPanel();
    }

    /**
     * Synchronise all controls with the model.
     */
    public void syncUI() {
        tartanListView.setItems(model.getTartanList());
        loadButton.setDisable(true);
    }


    /************************************************************************
     * Support code for "Load" panel. 
     */

    @FXML
    private Button loadButton;

    @FXML
    private Button cancelButton;

    @FXML
    private ListView<String> tartanListView;

    @FXML
    void cancelButtonActionPerformed(ActionEvent event) {
        model.getController().closeLoadWindow(null);
    }

    @FXML
    void loadButtonActionPerformed(ActionEvent event) {
        final String prompt = model.loadTartan();

        model.getController().closeLoadWindow(prompt);
    }

    /**
     * Initialize "Load" panel.
     */
    private void initializeLoadPanel() {
        tartanListView.setItems(model.getTartanList());
        tartanListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                model.setName(newValue);
                loadButton.setDisable(false);
            }
        } );

        tartanListView.setTooltip(new Tooltip("Select a tartan"));
        loadButton.setTooltip(new Tooltip("Load selected tartan"));
        cancelButton.setTooltip(new Tooltip("Cancel Load"));
    }



}
