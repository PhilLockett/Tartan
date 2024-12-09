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


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;

public class SaveAsController {


    /************************************************************************
     * Support code for "Save As" pop-up. 
     */

    private Model model;


    /**
     * Called by the FXML mechanism to initialize the controller.
     */
    @FXML public void initialize() {
//		System.out.println("Controller initialized.");
    }

    /**
     * Called by PrimaryController after the stage has been set. 
     */
    public void init() {
        model = Model.getInstance();
        // model.setSaveAsWindowLaunched(true);

        initializeSaveAsPanel();
    }

    /**
     * Synchronise all controls with the model.
     */
    public void syncUI() {
        saveAsTextField.setText(model.getName());
        saveButton.setDisable(!model.isNamed());
    }


    /************************************************************************
     * Support code for "Save As" panel. 
     */

    @FXML
    private TextField saveAsTextField;

    @FXML
    private Button cancelButton;

    @FXML
    private Button saveButton;

    @FXML
    void saveAsTextFieldKeyPressed(KeyEvent event) {
        model.setName(saveAsTextField.getText());
        saveButton.setDisable(!model.isNamed());
    }

    @FXML
    void cancelButtonActionPerformed(ActionEvent event) {
        model.getController().closeSaveAsWindow(null);
    }

    @FXML
    void saveButtonActionPerformed(ActionEvent event) {
        final String prompt = model.saveTartan();
        model.getController().closeSaveAsWindow(prompt);
    }

    /**
     * Initialize "Save As" panel.
     */
    private void initializeSaveAsPanel() {
        saveAsTextField.setTooltip(new Tooltip("Enter a name for the tartan"));
        saveButton.setTooltip(new Tooltip("Save tartan using the specified name"));
        cancelButton.setTooltip(new Tooltip("Cancel save"));
    }


}
