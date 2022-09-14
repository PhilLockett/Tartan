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

import java.util.ArrayList;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;


public class PrimaryController {

    private Model model;
    private Sample sample;


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
    public void saveState() {
        model.writeData();
    }

    /**
     * Synchronise all controls with the model. This should be the last step 
     * in the initialisation.
     */
    public void syncUI() {

        for (int i = 0; i < Default.SWATCH_COUNT.getInt(); ++i)
            setSwatch(i, model.getSwatchColour(i), model.getSwatchName(i));

        setSelectedColourRadioButton(model.getSelectedColour());
        selectedColourPicker.setValue(model.getCurrentColour());

        borderColourPicker.setValue(model.getBorderColour());

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
    private ColorPicker selectedColourPicker;

    @FXML
    void selectedColourRadioButtonActionPerformed(ActionEvent event) {
        RadioButton field = (RadioButton)event.getSource();
        // System.out.println("selectedColourRadioButtonActionPerformed(" + field.getId() + ", " + field.getText() + ")");

        int index = idToInt(field.getId());
        model.setSelectedColour(index);
        selectedColourPicker.setValue(model.getSwatchColour(index));
    }

    @FXML
    void selectedColourPickerActionPerformed(ActionEvent event) {
        final int index = model.getSelectedColour();
        final Color colour = selectedColourPicker.getValue();
        model.setSwatchColour(index, colour);
        colourSwatches.get(index).setColor(colour);
        sample.syncColour();
    }

    @FXML
    void swatchNameKeyPressed(KeyEvent event) {
        TextField field = (TextField)event.getSource();
        // System.out.println("reflectorKeyTyped(" + field.getId() + ", " + field.getText() + ")");

        model.setSwatchName(idToInt(field.getId()), field.getText());
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

        setSelectedColourRadioButtonToolTips();

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
    }



    /************************************************************************
     * Support code for "Layout" panel.
     */

    @FXML
    private Spinner<Integer> horizontalCountSpinner;

    @FXML
    private Spinner<Integer> verticalCountSpinner;

    @FXML
    private Spinner<Double> threadSizeSpinner;

    @FXML
    private ColorPicker borderColourPicker;

    @FXML
    private Spinner<Double> borderThicknessSpinner;

    @FXML
    void borderColourPickerActionPerformed(ActionEvent event) {
        model.setBorderColour(borderColourPicker.getValue());
        sample.syncBorderColour();
    }

    /**
     * Initialize "Layout" panel.
     */
    private void initializeLayout() {
        horizontalCountSpinner.setValueFactory(model.getHorizontalCountSVF());
        verticalCountSpinner.setValueFactory(model.getVerticalCountSVF());
        threadSizeSpinner.setValueFactory(model.getThreadSizeSVF());
        borderThicknessSpinner.setValueFactory(model.getBorderThicknessSVF());

        horizontalCountSpinner.setTooltip(new Tooltip("Select the horizontal ring count"));
        verticalCountSpinner.setTooltip(new Tooltip("Select the vertical ring count"));
        threadSizeSpinner.setTooltip(new Tooltip("Select the thread size"));
        borderColourPicker.setTooltip(new Tooltip("Select a thread border colour"));
        borderThicknessSpinner.setTooltip(new Tooltip("Select the border thickness"));

        horizontalCountSpinner.valueProperty().addListener( (v, oldValue, newValue) -> {
            // System.out.println("horizontalCountSpinner.Listener(" + newValue + "))");
            model.setHorizontalCount(newValue);
            sample.syncHorizontalCount();
        });

        verticalCountSpinner.valueProperty().addListener( (v, oldValue, newValue) -> {
            // System.out.println("verticalCountSpinner.Listener(" + newValue + "))");
            model.setVerticalCount(newValue);
            sample.syncVerticalCount();
        });

        threadSizeSpinner.valueProperty().addListener( (v, oldValue, newValue) -> {
            // System.out.println("ringRadiusSpinner.Listener(" + newValue + "))");
            model.setThreadSize(newValue);
            sample.syncThreadSize();
        });

        borderThicknessSpinner.valueProperty().addListener( (v, oldValue, newValue) -> {
            // System.out.println("ringThicknessSpinner.Listener(" + newValue + "))");
            model.setBorderThickness(newValue);
            sample.syncThreadSize();
        });

    }



    /************************************************************************
     * Support code for "Status Line" panel.
     */

    @FXML
    private Label statusLabel;

    @FXML
    private Button generateButton;

    @FXML
    void generateButtonActionPerformed(ActionEvent event) {
        final String path = model.generate();
        setStatusMessage("Generated in: " + path);
    }

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