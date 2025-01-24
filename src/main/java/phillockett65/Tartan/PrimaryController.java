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
 * PrimaryController is the class that is responsible for centralizing control.
 * It is instantiated by the FXML loader creates the Model.
 */
package phillockett65.Tartan;

import java.util.ArrayList;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import phillockett65.ColourSelect.ColourExtend;
import phillockett65.ColourSelect.ColourSelect;
import phillockett65.Debug.Debug;
import phillockett65.ColourSelect.ColourEvent;


public class PrimaryController {

    private Model model;
    private ColourSelect colourSelect;
    private ColourExtend colourExtend;

    @FXML
    private VBox root;

    @FXML
    private VBox colourSelectSetUpVBox;


    /************************************************************************
     * General run-time support code.
     */

    /**
     * Synchronise all controls with the model. This should be the last step 
     * in the initialisation.
     */
    private void syncUI() {

        for (int i = 0; i < Default.SWATCH_COUNT.getInt(); ++i) {
            setSwatch(i, model.getSwatchColour(i), model.getSwatchName(i));
        }

        final int selected = model.getSelectedColourIndex();
        colourSwatches.get(selected).setSelected(true);
        setSelectedColourRadioButton(selected);
        syncSelectedColour();

        rowCountSpinner.setDisable(model.isDuplicate());
        duplicateCheckbox.setSelected(model.isDuplicate());
        showGuideCheckbox.setSelected(model.isShowGuide());

        guideLineColourPicker.setValue(model.getGuideLineColour());
    }

    /**
     * Adjusts the UI based on "duplicate the column threads for the rows" 
     * selection.
     */
    private void fixUISettings() {
        rowCountSpinner.setDisable(model.isDuplicate());
    }



    /************************************************************************
     * Support code for the Initialization of the Controller.
     */

    /**
     * Responsible for constructing the Model and any local objects. Called by 
     * the FXMLLoader().
     */
    public PrimaryController() {
        Debug.trace("PrimaryController constructed.");
        model = Model.getInstance();
    }

    /**
     * Called by the FXML mechanism to initialize the controller. Called after 
     * the constructor to initialise all the controls.
     */
    @FXML public void initialize() {
        Debug.trace("PrimaryController initialized.");
        model.initialize(this);

        initializeTopBar();
        initializeColourPalette();
        initializeLayout();
        initializeStatusLine();

        colourSelect = new ColourSelect(false);
        colourExtend = new ColourExtend();

        colourSelectSetUpVBox.getChildren().addAll(colourSelect, colourExtend);
        
        colourSelect.addEventFilter(ColourEvent.ANY, this::handleColourSelectEvent);
        colourExtend.addEventFilter(ColourEvent.ANY, this::handleColourExtendEvent);
    }

    /**
     * Called by Application after the stage has been set. Completes any 
     * initialization dependent on other components being initialized.
     */
    public void init(Stage stage) {
        Debug.trace("PrimaryController init.");
        
        model.init(stage);

        syncUI();
        setStatusMessage("Ready.");
    }



    /************************************************************************
     * Public interface.
     */

    /**
     * Set the styles based on the focus state. Only called by the 
     * focusedProperty listener defined in App.
     * @param state is true if we have focus, false otherwise.
     */
    public void setFocus(boolean state) {
        Model.styleFocus(root, "unfocussed-root", state);
        Model.styleFocus(topBar, "unfocussed-bar", state);
    }



    /************************************************************************
     * Support code for "Top Bar" panel.
     */

    private double x = 0.0;
    private double y = 0.0;

    @FXML
    private HBox topBar;

    @FXML
    void topBarOnMousePressed(MouseEvent event) {
        x = event.getSceneX();
        y = event.getSceneY();
    }

    @FXML
    void topBarOnMouseDragged(MouseEvent event) {
        model.getStage().setX(event.getScreenX() - x);
        model.getStage().setY(event.getScreenY() - y);
    }
 

    /**
     * Initialize "Top Bar" panel.
     */
    private void initializeTopBar() {
        Pane cancel = Model.buildCancelButton();
        cancel.setOnMouseClicked(event -> model.close());

        topBar.getChildren().add(cancel);
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
        model.close();
    }

    @FXML
    private void editClearOnAction() {
        model.defaultSettings();
        syncUI();
        setStatusMessage("Settings cleared");
    }


    @FXML
    private void helpAboutOnAction() {
        launchHelpWindow();
    }



    /************************************************************************
     * Support code for pop-up windows. 
     */

    private boolean launchLoadWindow() {
        if (LoadControl.showControl("Load Tartan")) {
            setStatusMessage("Loaded " + model.loadTartan());
            syncUI();

            return true;
        }

        return false;
    }

    private boolean launchSaveAsWindow() {
        if (SaveAsControl.showControl("Save Tartan")) {
            setStatusMessage("Saved to " + model.saveTartan());

            return true;
        }

        return false;
    }

     private boolean launchHelpWindow() {
        return HelpControl.showControl(model.getTitle());
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

    private void syncSelectedColour() {
        final Color selectedColour = model.getSelectedColour();
        colourSelect.setColour(selectedColour);
        colourExtend.setColour(selectedColour);
    }

    private void selectedColourActionPerformed(int index) {
        Debug.trace("selectedColourActionPerformed(" + index + ")");

        final int previous = model.setSelectedColourIndex(index);
        colourSwatches.get(previous).setSelected(false);
        colourSwatches.get(index).setSelected(true);

        syncSelectedColour();
    }

    @FXML
    void selectedColourRadioButtonActionPerformed(ActionEvent event) {
        RadioButton field = (RadioButton)event.getSource();
        Debug.trace("selectedColourRadioButtonActionPerformed(" + field.getId() + ", " + field.getText() + ")");

        final int index = idToInt(field.getId());
        selectedColourActionPerformed(index);
    }

    @FXML
    void swatchNameKeyPressed(KeyEvent event) {
        TextField field = (TextField)event.getSource();
        Debug.trace("reflectorKeyTyped(" + field.getId() + ", " + field.getText() + ")");

        model.setSwatchName(idToInt(field.getId()), field.getText());
    }

    private Color handleColourEvent(ColourEvent event) {
        final int index = model.getSelectedColourIndex();
        final Color colour = event.getColour();
        model.setSwatchColour(index, colour);
        colourSwatches.get(index).setColour(colour);

        return colour;
    }

    private void handleColourSelectEvent(ColourEvent event) {
        final Color colour = handleColourEvent(event);
        colourExtend.handleColourEvent(colour);
    }

    private void handleColourExtendEvent(ColourEvent event) {
        final Color colour = handleColourEvent(event);
        colourSelect.setColour(colour);
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
        private Rectangle colour;
        private TextField label;
        private boolean selected;

        private final String activeStyle = "colour-selected";
        private final String inactiveStyle = "colour-unselected";

        /**
         * 
         * @param color of the Rectangle to present the colour of the swatch.
         * @param name of the swatch.
         */
        public ColourSwatch(Rectangle color, TextField name) {
            colour = color;
            label = name;

            colour.setStrokeWidth(2.0);
            colour.getStyleClass().add(inactiveStyle);
            colour.setOnMousePressed(mouseEvent -> {
                final int index = idToInt(color.getId());
                selectedColourActionPerformed(index);
                setSelectedColourRadioButton(index);
            });
    
        }

        private void showActive() {
            if (selected) {
                colour.setStroke(Color.WHITE);
                colour.getStyleClass().remove(inactiveStyle);
                if (!colour.getStyleClass().contains(activeStyle)) {
                    colour.getStyleClass().add(activeStyle);
                }
            } else {
                colour.setStroke(Color.BLACK);
                colour.getStyleClass().remove(activeStyle);
                if (!colour.getStyleClass().contains(inactiveStyle)) {
                    colour.getStyleClass().add(inactiveStyle);
                }
            }
        }

        public void setColour(Color color) { colour.setFill(color); }
        public void setName(String name) { label.setText(name); }
        public void setSelected(boolean state) { 
            selected = state;
            showActive();
        }

        /**
         * Set the id of the Rectangle and TextField associated with the swatch. 
         * @param index value to be assigned.
         */
        public void setId(int index) {
            String id = String.valueOf(index);
            colour.setId(id);
            label.setId(id);
        }

    }

    /**
     * Set the colour and name of the indexed swatch. Only called by syncUI().
     * @param index of the swatch to set.
     * @param colour of the swatch.
     * @param name of the swatch.
     */
    private void setSwatch(int index, Color colour, String name) {
        ColourSwatch swatch = colourSwatches.get(index);
        
        swatch.setColour(colour);
        swatch.setName(name);
        swatch.setSelected(false);
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
    }

    @FXML
    void guideLineColourPickerActionPerformed(ActionEvent event) {
        model.setGuideLineColour(guideLineColourPicker.getValue());
    }

    /**
     * Initialize "Layout" panel.
     */
    private void initializeLayout() {
        columnCountSpinner.setValueFactory(model.getColumnCountSVF());
        rowCountSpinner.setValueFactory(model.getRowCountSVF());
        threadCountSpinner.setValueFactory(model.getThreadCountSVF());
        threadSizeSpinner.setValueFactory(model.getThreadSizeSVF());
        borderThicknessSpinner.setValueFactory(model.getBorderThicknessSVF());

        columnCountSpinner.setTooltip(new Tooltip("Set the column repeat count"));
        rowCountSpinner.setTooltip(new Tooltip("Set the row repeat count"));
        duplicateCheckbox.setTooltip(new Tooltip("Duplicate the column threads for the rows (or vice versa)"));
        showGuideCheckbox.setTooltip(new Tooltip("Display guide lines"));
        guideLineColourPicker.setTooltip(new Tooltip("Select the colour for guide lines and thread highlight"));
        threadCountSpinner.setTooltip(new Tooltip("Set the number of adjacent threads to the selected colour"));
        threadSizeSpinner.setTooltip(new Tooltip("Set the thread size in pixels"));
        borderThicknessSpinner.setTooltip(new Tooltip("Set the thread border thickness in 0.1 pixels"));

        columnCountSpinner.valueProperty().addListener( (v, oldValue, newValue) -> {
            Debug.trace("columnCountSpinner.Listener(" + newValue + "))");
            model.setColumnCount(newValue.intValue());
        });

        rowCountSpinner.valueProperty().addListener( (v, oldValue, newValue) -> {
            Debug.trace("rowCountSpinner.Listener(" + newValue + "))");
            model.setRowCount(newValue.intValue());
        });

        threadSizeSpinner.valueProperty().addListener( (v, oldValue, newValue) -> {
            Debug.trace("threadSizeSpinner.Listener(" + newValue + "))");
            model.syncThreadSize();
        });

        borderThicknessSpinner.valueProperty().addListener( (v, oldValue, newValue) -> {
            Debug.trace("borderThicknessSpinner.Listener(" + newValue + "))");
            model.syncThreadSize();
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