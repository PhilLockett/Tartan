/*  ColourSelect - a JavaFX based colour selector.
 *
 *  Copyright 2025 Philip Lockett.
 *
 *  This file is part of ColourSelect.
 *
 *  ColourSelect is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  ColourSelect is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with ColourSelect.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * ColourExtend is a class that helps select a colour by specifying numeric 
 * values.
 */
package phillockett65.ColourSelect;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import phillockett65.Debug.Debug;

public class ColourExtend extends GridPane {

    private static final String ERROR = "error-text-field";

    private CheckBox hsbCheckbox = new CheckBox("HSB");
    private CheckBox hexCheckbox = new CheckBox("Hex");
    private Button setButton = new Button(" Apply ");

    private boolean isHSB() { return hsbCheckbox.isSelected(); }
    private boolean isHex() { return hexCheckbox.isSelected(); }

    private int[] scales = { 100, 255, 360, 1000 };
    private ObservableList<String> scaleList = FXCollections.observableArrayList("100", "255", "360", "1000");

    private Color selectedColour = Color.WHITE;


    /************************************************************************
     * General support code.
     */

    private String intToString(int value) {
        return String.format(isHex() ? "%X" : "%d", value);
    }

    private String doubleToString(double value) {
        return intToString((int)Math.round(value));
    }

    private int stringToInt(String value) {
        try {
            return Integer.parseInt(value, isHex() ? 16 : 10);
        } catch (NumberFormatException e) {

        }

        return -1;
    }


     /************************************************************************
     * Supporting sub-class code.
     */

    private class ColourComp {
        final String label;
        final ColourFilter filter;
        int scale;

        public ColourComp(String label, ColourFilter filter, int scale) {
            this.label = label;
            this.filter = filter;
            this.scale = scale;
        }
    }

    private class ColourCompIO {
        ColourComp colourComp;
        Label label;
        TextField value;
        boolean valid = true;
        ChoiceBox<String> scale;
        ColourFilter filter;

        private int getScale() {
            return stringToInt(scale.getValue());
        }

        public void setScale(int value) {
            scale.setValue(intToString(value));
        }

        private void checkValidity() {
            valid = true;

            // Check validity of the content of value.
            final int integer = stringToInt(value.getText());
            if ((integer < 0) || (integer > getScale())) {
                valid = false;
            }

            // Style value based on the validity of it's content.
            if (valid) {
                value.getStyleClass().remove(ERROR);
            } else {
                if (!value.getStyleClass().contains(ERROR)) {
                    value.getStyleClass().add(ERROR);
                }
            }
        }

        private double getValue() {
            final Integer integer = Integer.parseInt(value.getText(), isHex() ? 16 : 10);

            return integer.doubleValue() / getScale();
        }

        public void set(ColourComp colourCompare) {
            colourComp = colourCompare;
            label.setText(colourComp.label + ": ");
            filter = colourComp.filter;
            setScale(colourComp.scale);
            updateValue();
        }

        private void setTooltip() {
            String tooltip = "Set the " + (isHex() ? "hexa" : "") + "decimal value for '" + 
                colourComp.label + "' in the range 0 to " + intToString(colourComp.scale);
            value.setTooltip(new Tooltip(tooltip));
        }

        public void updateValue() {
            double current = filter.filter(getSelectedColour());
            current *= colourComp.scale;

            setTooltip();
            value.setText(doubleToString(current));
        }

        public ColourCompIO() {
            label = new Label();

            value = new TextField();
            value.setOnKeyTyped(event -> {
                checkValidity();
                syncUI();
            });
    
            scale = new ChoiceBox<String>(scaleList);
            scale.getSelectionModel().selectedItemProperty().addListener( (v, oldValue, newValue) -> {
                if (newValue != null) {
                    colourComp.scale = stringToInt(newValue);
                    updateValue();
                }
            });
            scale.setPrefWidth(80);
            scale.setTooltip(new Tooltip("Select the required scale factor"));
        }
    }


    private ColourComp[] colourCompHSB = {
        new ColourComp("Hue", (c) -> { return c.getHue()/360; }, 360),
        new ColourComp("Saturation", (c) -> c.getSaturation(), 100),
        new ColourComp("Brightness", (c) -> c.getBrightness(), 100)
    };

    private ColourComp[] colourCompRGB = {
        new ColourComp("Red", (c) -> c.getRed(), 255),
        new ColourComp("Green", (c) -> c.getGreen(), 255),
        new ColourComp("Blue", (c) -> c.getBlue(), 255)
    };

    private ColourCompIO[] colourCompList = {
        new ColourCompIO(),
        new ColourCompIO(),
        new ColourCompIO()
    };



    /************************************************************************
     * Support code for the Initialization of the Grid Layout.
     */

    private void addHandlers() {
        hsbCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                syncGrid();
            }
        });

        hexCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                syncBase();
            }
        });

        setButton.setOnAction(event -> setButton.fireEvent(new ColourEvent(ColourEvent.COLOUR_CHANGE, getColour())) );
    }

    private void addColumnConstraint(double width, boolean right) {
        ColumnConstraints col = new ColumnConstraints();
        col.setHalignment(right ? HPos.RIGHT : HPos.LEFT);
        col.setHgrow(Priority.SOMETIMES);
        col.setMinWidth(width);
        col.setPrefWidth(width);

        getColumnConstraints().add(col);
    }

    private void addRowConstraint(double height) {
        RowConstraints row = new RowConstraints();
        row.setValignment(VPos.CENTER);
        row.setVgrow(Priority.SOMETIMES);
        row.setMinHeight(height);
        row.setPrefHeight(height);

        getRowConstraints().add(row);
    }

    /**
     * Build the Grid layout, but don't fill it.
     */
    private void buildGrid() {
        final double LeftWidth = 100.0;
        addColumnConstraint(60.0, false);
        addColumnConstraint(LeftWidth, true);
        addColumnConstraint(LeftWidth, true);
        addColumnConstraint(LeftWidth, true);

        
        final double rowHeight = 30.0;
        addRowConstraint(rowHeight);    // Red.
        addRowConstraint(rowHeight);    // Green.
        addRowConstraint(rowHeight);    // Blue.
    }


    /**
     * Fill the Grid layout from colourCompList[].
     */
    private void fillGrid() {
        hsbCheckbox.setTooltip(new Tooltip("Select to display/modify as Hue/Saturation/Brightness"));
        hexCheckbox.setTooltip(new Tooltip("Select to display/modify values in hexadecimal"));
        setButton.setTooltip(new Tooltip("Click to update selected colour using the values specified"));

        this.add(hsbCheckbox, 0, 0);
        this.add(hexCheckbox, 0, 1);
        this.add(setButton, 0, 2);

        final int MAX = colourCompList.length;
        for (int row = 0; row < MAX; ++row) {
            final ColourCompIO source = colourCompList[row];
            this.add(source.label, 1, row);
            this.add(source.value, 2, row);
            this.add(source.scale, 3, row);
        }

        addHandlers();
    }

    private void syncGrid() {
        final int MAX = colourCompList.length;

        if (isHSB()) {
            for (int i = 0; i < MAX; ++i) {
                final ColourComp source = colourCompHSB[i];
                ColourCompIO target = colourCompList[i];
                target.set(source);
            }
        } else {
            for (int i = 0; i < MAX; ++i) {
                final ColourComp source = colourCompRGB[i];
                ColourCompIO target = colourCompList[i];
                target.set(source);
            }
        }
    }

    private void syncBase() {
        // Update the ObservableList 'scaleList' to match the current base.
        final int SCALES = scales.length;
        for (int i = 0; i < SCALES; ++i) {
            scaleList.set(i, intToString(scales[i]));
        }

        // Update the TextField 'value' with current base and update the 
        // ChoiceBox 'scale' with the base-corrected selection.
        final int MAX = colourCompList.length;
        for (int i = 0; i < MAX; ++i) {
            ColourCompIO target = colourCompList[i];
            target.updateValue();
            target.scale.setValue(intToString(target.colourComp.scale));
        }
    }


    private Color getColour() {
        if (isHSB()) {
            final double h = colourCompList[0].getValue() * 360;
            final double s = colourCompList[1].getValue();
            final double b = colourCompList[2].getValue();

            return Color.hsb(h, s, b);
        }

        final int r = (int)Math.round(colourCompList[0].getValue() * 255);
        final int g = (int)Math.round(colourCompList[1].getValue() * 255);
        final int b = (int)Math.round(colourCompList[2].getValue() * 255);

        return Color.rgb(r, g, b);
    }


    private boolean isColourValid() {
        final int MAX = colourCompList.length;

        for (int i = 0; i < MAX; ++i) {
            ColourCompIO target = colourCompList[i];
            if (target.valid == false) {
                return false;
            }
        }

        return true;
    }

    private void syncUI() {
        setButton.setDisable(!isColourValid());
    }

    private Color getSelectedColour() { return selectedColour; }

    private void init() {
        Debug.info("ColourSelect() init()");

        buildGrid();
        fillGrid();
        syncGrid();
        syncUI();
    }



    /************************************************************************
     * Public interface.
     */

    /**
     * Called by PrimaryController whenever the selected colour is changed.
     * @param colour currently in use.
     */
    public void setColour(Color colour) {
        selectedColour = colour;
    }

    /**
     * Called by PrimaryController whenever the colourSelect fires an event to 
     * indicate the current colour has changed.
     * @param colour currently in use.
     */
    public void handleColourEvent(Color colour) {
        setColour(colour);
        syncBase();
    }

    /**
     * Constructor.
     */
    public ColourExtend() {
        super();
        Debug.trace("ColourSelect() constructed.");
        init();
     }


}
