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
 * PrimaryController is the class that is responsible for centralizing control.
 * It is instantiated by the FXML loader creates the Model.
 */
package phillockett65.Tartan;

import java.io.IOException;
import java.util.ArrayList;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import phillockett65.Tartan.ColourSelect.ColourEvent;


public class PrimaryController {

    private Model model;
    private Sample sample;
    private ColourSelect colourSelect;

    @FXML
    private VBox colourSelectSetUpHBox;


    /************************************************************************
     * Support code for the Initialization of the Controller.
     */

    /**
     * Responsible for constructing the Model and any local objects. Called by 
     * the FXMLLoader().
     */
    public PrimaryController() {
        // System.out.println("PrimaryController constructed.");
        model = new Model(this);
    }

    /**
     * Called by the FXML mechanism to initialize the controller. Called after 
     * the constructor to initialise all the controls.
     */
    @FXML public void initialize() {
        // System.out.println("PrimaryController initialized.");
        model.initialize();

        initializeColourPalette();
        initializeLayout();
        initializeStatusLine();

        colourSelect = new ColourSelect();
        colourSelectSetUpHBox.getChildren().addAll(colourSelect);
        colourSelectSetUpHBox.addEventFilter(ColourEvent.ANY, this::handleColourEvent);
    }

    /**
     * Called by Application after the stage has been set. Completes any 
     * initialization dependent on other components being initialized.
     */
    public void init(Stage stage) {
        // System.out.println("PrimaryController init.");
        sample = new Sample(model, "Sample");

        model.init(stage, sample);
        sample.init();
        syncUI();
        setStatusMessage("Ready.");
    }

    /**
     * Save the current state to disc, called by the application on shut down.
     */
    // public void saveState() {
    //     model.writeData();
    // }

    /**
     * Synchronise all controls with the model. This should be the last step 
     * in the initialisation.
     */
    private void syncUI() {

        for (int i = 0; i < Default.SWATCH_COUNT.getInt(); ++i)
            setSwatch(i, model.getSwatchColour(i), model.getSwatchName(i));

        setSelectedColourRadioButton(model.getSelectedColour());
        colourSelect.setColour(model.getCurrentColour());

        duplicateCheckbox.setSelected(model.isDuplicate());
        showGuideCheckbox.setSelected(model.isShowGuide());
        sample.syncGuideVisible();
        sample.syncGuideLineColour();

        guideLineColourPicker.setValue(model.getGuideLineColour());

        fixUISettings();
    }

    /**
     * Adjusts the UI based on "duplicate the column threads for the rows" 
     * selection.
     */
    private void fixUISettings() {
        if (model.isDuplicate()) {
            rowCountSpinner.getValueFactory().setValue(model.getColumnCount());
            rowCountSpinner.setDisable(true);
            sample.syncDuplicateThreads();
        } else {
            rowCountSpinner.setDisable(false);
        }
    }



    /************************************************************************
     * Support code for "Tartan Designer" Menu structure.
     */

    @FXML
    private void fileLoadOnAction() {
        launchLoadWindow();
    }

    @FXML
    private void fileSaveOnAction() {
        if (model.isNamed()) {
            final String prompt = model.saveTartan();

            if (prompt != null)
                setStatusMessage("Saved to: " + prompt);
        }
        else
            launchSaveAsWindow();
    }

    @FXML
    private void fileSaveAsOnAction() {
        launchSaveAsWindow();
    }

    @FXML
    private void fileCloseOnAction() {
        sample.close();
        model.getStage().close();
    }

    @FXML
    private void editClearOnAction() {
        model.defaultSettings();
        syncUI();
        sample.syncCount();
        setStatusMessage("Settings cleared");
}


    @FXML
    private void helpAboutOnAction() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About Tartan Designer");
        alert.setHeaderText("Tartan Designer 1.0");
        alert.setContentText("Tartan Designer is an application for generating Tartan designs.");

        alert.showAndWait();
    }



    /************************************************************************
     * Support code for "Load" panel. 
     */

    private LoadController loadController;
    private Stage loadStage;

    private boolean launchLoadWindow() {
        // System.out.println("launchLoadWindow() " + model.isLoadWindowLaunched());
        if (model.isLoadWindowLaunched())
            return false;

        model.setLoadWindowLaunched(true);

        if (loadController != null) {
            loadController.syncUI();
            loadStage.show();

            return true;
        }


        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("load.fxml"));
            Parent root = fxmlLoader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(App.class.getResource("application.css").toExternalForm());

            loadStage = new Stage();
            loadStage.setTitle("Load Tartan");
            loadStage.resizableProperty().setValue(false);
            loadStage.setScene(scene);
            loadStage.setOnCloseRequest(e -> Platform.exit());

            loadController = fxmlLoader.getController();
            loadController.init(model);
            loadController.syncUI();

            loadStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean closeLoadWindow(String prompt) {
        // System.out.println("closeLoadWindow() " + model.isLoadWindowLaunched());
        if (!model.isLoadWindowLaunched())
            return false;

        model.setLoadWindowLaunched(false);
        loadStage.hide();
        if (prompt != null) {
            syncUI();
            sample.syncCount();
            setStatusMessage("Loaded " + prompt);
        }

        return true;
    }



    /************************************************************************
     * Support code for "Save As" panel. 
     */

    private SaveAsController saveAsController;
    private Stage saveAsStage;

    private boolean launchSaveAsWindow() {
        // System.out.println("launchSaveAsWindow() " + model.isSaveAsWindowLaunched());
        if (model.isSaveAsWindowLaunched())
            return false;

        model.setSaveAsWindowLaunched(true);

        if (saveAsController != null) {
            saveAsStage.show();

            return true;
        }


        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("saveAs.fxml"));
            Parent root = fxmlLoader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(App.class.getResource("application.css").toExternalForm());

            saveAsStage = new Stage();
            saveAsStage.setTitle("Save Tartan");
            saveAsStage.resizableProperty().setValue(false);
            saveAsStage.setScene(scene);
            saveAsStage.setOnCloseRequest(e -> Platform.exit());

            saveAsController = fxmlLoader.getController();
            saveAsController.init(model);
            saveAsController.syncUI();

            saveAsStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean closeSaveAsWindow(String prompt) {
        // System.out.println("closeSaveAsWindow() " + model.isSaveAsWindowLaunched());
        if (!model.isSaveAsWindowLaunched())
            return false;

        model.setSaveAsWindowLaunched(false);
        saveAsStage.hide();
        if (prompt != null)
            setStatusMessage("Saved to: " + prompt);

        return true;
    }



    /************************************************************************
     * Support code for "Colour Palette" panel.
     */

    @FXML
    private ToggleGroup myToggleGroup;

    private ArrayList<RadioButton> selectedColour = new ArrayList<RadioButton>();
    private ArrayList<ColourSwatch> colourSwatches = new ArrayList<ColourSwatch>();

    @FXML
    private RadioButton colour0RadioButton;

    @FXML
    private RadioButton colour1RadioButton;

    @FXML
    private RadioButton colour2RadioButton;

    @FXML
    private RadioButton colour3RadioButton;

    @FXML
    private RadioButton colour4RadioButton;

    @FXML
    private RadioButton colour5RadioButton;

    @FXML
    private RadioButton colour6RadioButton;

    @FXML
    private RadioButton colour7RadioButton;

    @FXML
    Rectangle colour0Rectangle;

    @FXML
    Rectangle colour1Rectangle;

    @FXML
    Rectangle colour2Rectangle;

    @FXML
    Rectangle colour3Rectangle;

    @FXML
    Rectangle colour4Rectangle;

    @FXML
    Rectangle colour5Rectangle;

    @FXML
    Rectangle colour6Rectangle;

    @FXML
    Rectangle colour7Rectangle;

    @FXML
    TextField colour0TextField;

    @FXML
    TextField colour1TextField;

    @FXML
    TextField colour2TextField;

    @FXML
    TextField colour3TextField;

    @FXML
    TextField colour4TextField;

    @FXML
    TextField colour5TextField;

    @FXML
    TextField colour6TextField;

    @FXML
    TextField colour7TextField;

    @FXML
    void selectedColourRadioButtonActionPerformed(ActionEvent event) {
        RadioButton field = (RadioButton)event.getSource();
        // System.out.println("selectedColourRadioButtonActionPerformed(" + field.getId() + ", " + field.getText() + ")");

        int index = idToInt(field.getId());
        model.setSelectedColour(index);
        colourSelect.setColour(model.getSwatchColour(index));
    }

    @FXML
    void swatchNameKeyPressed(KeyEvent event) {
        TextField field = (TextField)event.getSource();
        // System.out.println("reflectorKeyTyped(" + field.getId() + ", " + field.getText() + ")");

        model.setSwatchName(idToInt(field.getId()), field.getText());
    }

    public void handleColourEvent(ColourEvent event) {
        final int index = model.getSelectedColour();
        final Color colour = event.getColour();
        model.setSwatchColour(index, colour);
        colourSwatches.get(index).setColor(colour);
        sample.syncColour();
    }

    /**
     * Convert a String representation of an integer to an int.
     * @param id String representation of an integer.
     * @return the int value represented in the String.
     */
    private int idToInt(String id) { return Integer.valueOf(id); }

    /**
     * Class that represents a colour swatch.
     */
    private class ColourSwatch {
        private Rectangle color;
        private TextField name;

        /**
         * 
         * @param color of the Rectangle to present the colour of the swatch.
         * @param name of the swatch.
         */
        public ColourSwatch(Rectangle color, TextField name) {
            this.color = color;
            this.name = name;
        }

        public void setColor(Color color) { this.color.setFill(color); }
        public void setName(String name) { this.name.setText(name); }

        /**
         * Set the id of the Rectangle and TextField associated with the swatch. 
         * @param index value to be assigned.
         */
        public void setId(int index) {
            String id = String.valueOf(index);
            color.setId(id);
            name.setId(id);
        }

    }

    /**
     * Set the colour and name of the indexed swatch.
     * @param index of the swatch to set.
     * @param colour of the swatch.
     * @param name of the swatch.
     * @return true if the swatch was set, false otherwise.
     */
    private boolean setSwatch(int index, Color colour, String name) {
        if (index >= Default.SWATCH_COUNT.getInt())
            return false;

        ColourSwatch swatch = colourSwatches.get(index);
        
        swatch.setColor(colour);
        swatch.setName(name);

        return true;
    }

    /**
     * Set the selected radio button.
     * @param index of the selected radio button.
     */
    private void setSelectedColourRadioButton(int index) {
        selectedColour.get(index).setSelected(true);
    }

    /**
     * Set the tool tips of all the radio buttons.
     */
    private void setSelectedColourRadioButtonToolTips() {
        for (RadioButton item : selectedColour) {
            item.setTooltip(new Tooltip("Select as active colour"));
        }
    }

    /**
     * Initialize "Colour Palette" panel.
     */
    private void initializeColourPalette() {

        selectedColour.add(colour0RadioButton);
        selectedColour.add(colour1RadioButton);
        selectedColour.add(colour2RadioButton);
        selectedColour.add(colour3RadioButton);
        selectedColour.add(colour4RadioButton);
        selectedColour.add(colour5RadioButton);
        selectedColour.add(colour6RadioButton);
        selectedColour.add(colour7RadioButton);

        colourSwatches.add(new ColourSwatch(colour0Rectangle, colour0TextField));
        colourSwatches.add(new ColourSwatch(colour1Rectangle, colour1TextField));
        colourSwatches.add(new ColourSwatch(colour2Rectangle, colour2TextField));
        colourSwatches.add(new ColourSwatch(colour3Rectangle, colour3TextField));
        colourSwatches.add(new ColourSwatch(colour4Rectangle, colour4TextField));
        colourSwatches.add(new ColourSwatch(colour5Rectangle, colour5TextField));
        colourSwatches.add(new ColourSwatch(colour6Rectangle, colour6TextField));
        colourSwatches.add(new ColourSwatch(colour7Rectangle, colour7TextField));

        for (int i = 0; i < colourSwatches.size(); ++i) {
            colourSwatches.get(i).setId(i);
        }

        for (int i = 0; i < selectedColour.size(); ++i) {
            String id = String.valueOf(i);
            selectedColour.get(i).setId(id);
        }

        setSelectedColourRadioButtonToolTips();
    }



    /************************************************************************
     * Support code for "Layout" panel.
     */

    @FXML
    private Spinner<Integer> columnCountSpinner;

    @FXML
    private Spinner<Integer> rowCountSpinner;

    @FXML
    private CheckBox duplicateCheckbox;

    @FXML
    private CheckBox showGuideCheckbox;

    @FXML
    private ColorPicker guideLineColourPicker;

    @FXML
    private Spinner<Integer> threadCountSpinner;

    @FXML
    private Spinner<Double> threadSizeSpinner;

    @FXML
    private Spinner<Double> borderThicknessSpinner;

    @FXML
    void duplicateCheckboxOnAction(ActionEvent event) {
        model.setDuplicate(duplicateCheckbox.isSelected());
        fixUISettings();
    }

    @FXML
    void showGuideCheckboxOnAction(ActionEvent event) {
        model.setShowGuide(showGuideCheckbox.isSelected());
        sample.syncGuideVisible();
    }

    @FXML
    void guideLineColourPickerActionPerformed(ActionEvent event) {
        model.setGuideLineColour(guideLineColourPicker.getValue());
        sample.syncGuideLineColour();
    }

    /**
     * Initialize "Layout" panel.
     */
    private void initializeLayout() {
        columnCountSpinner.setValueFactory(model.getColumnCountSVF());
        rowCountSpinner.setValueFactory(model.getRowCountSVF());
        duplicateCheckbox.setSelected(model.isDuplicate());
        threadCountSpinner.setValueFactory(model.getThreadCountSVF());
        threadSizeSpinner.setValueFactory(model.getThreadSizeSVF());
        borderThicknessSpinner.setValueFactory(model.getBorderThicknessSVF());

        columnCountSpinner.setTooltip(new Tooltip("Set the column repeat count"));
        rowCountSpinner.setTooltip(new Tooltip("Set the row repeat count"));
        duplicateCheckbox.setTooltip(new Tooltip("Duplicate the column threads for the rows"));
        threadCountSpinner.setTooltip(new Tooltip("Set the number of adjacent threads to colour"));
        threadSizeSpinner.setTooltip(new Tooltip("Set the thread size in pixels"));
        borderThicknessSpinner.setTooltip(new Tooltip("Set the thread border thickness in pixels"));
        guideLineColourPicker.setTooltip(new Tooltip("Select the colour for guide lines"));

        columnCountSpinner.valueProperty().addListener( (v, oldValue, newValue) -> {
            // System.out.println("columnCountSpinner.Listener(" + newValue + "))");
            model.setColumnCount(newValue.intValue());
            sample.syncColumnCount();
            if (model.isDuplicate()) {
                rowCountSpinner.getValueFactory().setValue(newValue.intValue());
            }
        });

        rowCountSpinner.valueProperty().addListener( (v, oldValue, newValue) -> {
            // System.out.println("rowCountSpinner.Listener(" + newValue + "))");
            model.setRowCount(newValue.intValue());
            sample.syncRowCount();
        });

        threadSizeSpinner.valueProperty().addListener( (v, oldValue, newValue) -> {
            // System.out.println("threadSizeSpinner.Listener(" + newValue + "))");
            sample.syncThreadSize();
        });

        borderThicknessSpinner.valueProperty().addListener( (v, oldValue, newValue) -> {
            // System.out.println("borderThicknessSpinner.Listener(" + newValue + "))");
            sample.syncThreadSize();
        });

    }



    /************************************************************************
     * Support code for "Status Line" panel.
     */

    @FXML
    private Label statusLabel;

    /**
     * Set the status line message.
     * @param message to display on the status line.
     */
    private void setStatusMessage(String message) {
        statusLabel.setText(message);
    }

    /**
     * Initialize "Status Line" panel.
     */
    private void initializeStatusLine() {
    }

}