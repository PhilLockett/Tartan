/*  ColourSelect - a JavaFX based colour selector.
 *
 *  Copyright 2024 Philip Lockett.
 *
 *  This file is part of FXMLCustomControler3.
 *
 *  FXMLCustomControler3 is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  FXMLCustomControler3 is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FXMLCustomControler3.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * ColourSelect is a class that helps select a colour.
 */
package phillockett65.Tartan;


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;

public class ColourSelect extends GridPane {

    private static final double MIN = 0.0;
    private static final double MAX = 100.0;

    private static final double width = 400.0;
    private static final double height = 400.0;
    private static final double border = width * 0.05;
    private static final double xCentre = width / 2;
    private static final double yCentre = height / 2;
    private static final double radius2 = xCentre - border;
    private static final double radius = radius2 * 0.80;
    private static final double radius2S = radius2 * radius2;
    private static final double radiusS = radius * radius;
    private static final double swatchRadius = radius * 0.80;

    private static final double pi = Math.acos(-1);
    private static final double pi2 = pi / 2;
    private static final double pi3 = pi2 * 3;

    private Group group;
    private final Canvas canvas;
    private GraphicsContext gc;
    private Line guide;
    private Circle swatch;

    private Slider brightnessSlider;
    private Slider saturationSlider;
    private Slider opacitySlider;

    private Slider redSlider;
    private Slider greenSlider;
    private Slider blueSlider;

    private double theta = 0;
    private double hue = radiansToDegrees(theta);
    private double sat = 1.0;
    private double val = 1.0;

    private double opa = 1.0;

    private double red = 1.0;
    private double gre = 0.0;
    private double blu = 0.0;



    /************************************************************************
     * General support code.
     */

    private double radiansToDegrees(double radians) {
        return (180.0 / pi) * radians;
    }

    private double degreesToRadians(double degrees) {
        return (pi / 180.0) * degrees;
    }


    /**
     * Sync the Guide line to the hue value.
     */
    private void syncHueGuide() {
        double x = radius * Math.cos(theta);
        double y = radius * Math.sin(theta);
        guide.setEndX(xCentre + x);
        guide.setEndY(yCentre + y);
    }

    private void syncBrightnessSlider() { brightnessSlider.setValue(val * MAX); }
    private void syncSaturationSlider() { saturationSlider.setValue(sat * MAX); }
    private void syncOpacitySlider() { opacitySlider.setValue(opa * MAX); }

    private void syncRedSlider() { redSlider.setValue(red * MAX); }
    private void syncGreenSlider() { greenSlider.setValue(gre * MAX); }
    private void syncBlueSlider() { blueSlider.setValue(blu * MAX); }



    /************************************************************************
     * Initialization of the Canvas, including Mouse handler code.
     */

    private void setHue(double x, double y) {
        theta = Math.atan2(y, x);
        hue = radiansToDegrees(theta);
        syncHueGuide();
    }

    private boolean isSwatch(double x, double y) {
        x -= xCentre;
        y -= yCentre;
        final double x2 = x*x;
        final double y2 = y*y;
        final double s = x2 + y2;

        if (s < radiusS)
            return false;

        if (s > radius2S)
            return false;

        return true;
    }

    // Used by the mouse.
    private void colourSwatch(double x, double y) {
        if (isSwatch(x, y)) {
            setHue(x-xCentre, y-yCentre);
            updateRGB();
        }
    }

    /**
     * Draw a Chess board to illustrate Opacity.
     */
    private void drawChessBoard() {
        gc.setFill(Color.SILVER);
        final int squares = 8;
        final double origin = yCentre - swatchRadius;
        final double size = swatchRadius / squares;
        double yPos = origin;
        for (int y = 0; y < squares; ++y) {
            double xPos = origin;
            for (int x = 0; x < squares; ++x) {
                if ((x+y) % 2 == 0)
                    gc.fillRect(xPos, yPos, size, size);

                xPos += size;
            }
            yPos += size;
        }
    }

    /**
     * Initialize the Canvas.
     * @return the Canvas.
     */
    private Canvas buildCanvas() {
        Canvas canvas = new Canvas(getMyWidth(), getMyHeight());
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, getMyWidth(), getMyHeight());

        drawChessBoard();

        canvas.setOnMouseClicked(event -> {
            colourSwatch(event.getX(), event.getY());
        });

        canvas.setOnMouseDragged(event -> {
            colourSwatch(event.getX(), event.getY());
        });

        return canvas;
    }



    /************************************************************************
     * Support code for guide Line.
     */

    private static final String GUIDECOL = "select-guide";
    
    private final Color guideCol = Color.BLACK;

    /**
     * Initialize the guide Line.
     * @return the guide Line.
     */
    private Line buildGuide() {
        guide = new Line();
        guide.setStartX(xCentre);
        guide.setStartY(yCentre);
        guide.setEndX(xCentre + radius);    // theta == 0
        guide.setEndY(yCentre);
        guide.setStroke(guideCol);
        guide.getStyleClass().add(GUIDECOL);
        guide.setStrokeWidth(5);
        guide.setStrokeLineCap(StrokeLineCap.ROUND);
        guide.setVisible(true);

        return guide;
    }



    /************************************************************************
     * Support code for Swatch.
     */

    /**
     * Initialize the Swatch.
     * @return the Swatch.
     */
    private Circle buildSwatch() {
        Circle swatch = new Circle(xCentre, yCentre, swatchRadius);

        return swatch;
    }

    private Color lastColour = null;

    /**
     * Set the swatch colour.
     * @param colour to set the swatch to.
     */
    private void setSwatch(Color colour) {
        // System.out.println("setSwatch()" + colour.toString());
        if (colour.equals(lastColour))
            return;

        lastColour = colour;
        swatch.setFill(colour);

        swatch.fireEvent(new ColourEvent(ColourEvent.COLOUR_CHANGE, colour));
    }

    private void syncSwatch() {
        // System.out.println("syncSwatch()");
        setSwatch(getAlphaColour());
    }



    /************************************************************************
     * Initialization the colour ring on the Canvas.
     */

    private void point(Color colour, double x, double y) {
        gc.setStroke(colour);
        gc.strokeLine(x, y, x+1, y);
    }

    private void point(double theta, double x, double y) {
        double hue = (180.0 / pi) * theta;

        point(Color.hsb(hue, 1.0, 1.0), x, y);
    }

    /**
     * Draw the colour ring on the Canvas.
     */
    private void drawRing() {
        for (int y = (int)(radius * 0.7); y < radius2; ++y) {
            final int y2 = y*y;

            for (int x = 0; x <= y; ++x) {
                final int x2 = x*x;
                final double s = x2 + y2;

                // point(Color.BLACK, xCentre+x, yCentre+y);
                if (s > radius2S) {
                    break;
                }

                if (s > radiusS) {
                    final double theta = Math.atan2(y, x);

                    point(   theta, xCentre+x, yCentre+y);
                    point(pi-theta, xCentre-x, yCentre+y);
                    point(pi+theta, xCentre-x, yCentre-y);
                    point(  -theta, xCentre+x, yCentre-y);

                    point(pi2-theta, xCentre+y, yCentre+x);
                    point(pi2+theta, xCentre-y, yCentre+x);
                    point(pi3-theta, xCentre-y, yCentre-x);
                    point(pi3+theta, xCentre+y, yCentre-x);
                }
            }
        }

        // Clean up the edges of the ring.
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2.0);
        gc.strokeOval(xCentre-radius, yCentre-radius, radius*2, radius*2);
        gc.strokeOval(xCentre-radius2, yCentre-radius2, radius2*2, radius2*2);
    }



    /************************************************************************
     * Support code for the Initialization of the Grid Layout.
     */

    private void addColumnConstraint(double width) {
        ColumnConstraints col = new ColumnConstraints();
        col.setHalignment(HPos.RIGHT);
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
        addColumnConstraint(LeftWidth);
        addColumnConstraint(height - LeftWidth);

        
        final double rowHeight = 30.0;
        final double withPadding = rowHeight + 20.0;
        addRowConstraint(height);       // Selector.
        addRowConstraint(rowHeight);    // Brightness.
        addRowConstraint(rowHeight);    // Saturation.
        addRowConstraint(withPadding);  // Opacity.
        addRowConstraint(rowHeight);    // Red.
        addRowConstraint(rowHeight);    // Green.
        addRowConstraint(rowHeight);    // Blue.
    }


    private Slider buildSlider(double value) {
        Slider slider = new Slider(MIN, MAX, value);
        // slider.showTickMarksProperty();
        slider.setOrientation(Orientation.HORIZONTAL);

        return slider;
    }


    /**
     * Fill the Grid layout, including the sliders with Listener code.
     */
    private void fillGrid() {
        // Add the colour selector.
        int row = 0;
        this.add(group, 0, row, 2, 1);

        // Add the HSB sliders
        ++row;
        Label brightnessLabel = new Label("Brightness: ");
        this.add(brightnessLabel, 0, row);

        brightnessSlider = buildSlider(MAX);
        brightnessSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                // System.out.println("Brightness: " + new_val.doubleValue());
                val = new_val.doubleValue() / MAX;
                updateRGB();
            }
        });
        this.add(brightnessSlider, 1, row);

        ++row;
        Label saturationLabel = new Label("Saturation: ");
        this.add(saturationLabel, 0, row);

        saturationSlider = buildSlider(MAX);
        saturationSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                // System.out.println("Saturation: " + new_val.doubleValue());
                sat = new_val.doubleValue() / MAX;
                updateRGB();
            }
        });
        this.add(saturationSlider, 1, row);

        ++row;
        Label opacityLabel = new Label("Opacity: ");
        this.add(opacityLabel, 0, row);

        opacitySlider = buildSlider(MAX);
        opacitySlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                // System.out.println("Opacity: " + new_val.doubleValue());
                opa = new_val.doubleValue() / MAX;
                syncSwatch();
            }
        });
        this.add(opacitySlider, 1, row);

        // Add the RGB sliders
        ++row;
        Label redLabel = new Label("Red: ");
        this.add(redLabel, 0, row);

        redSlider = buildSlider(MAX);
        redSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                // System.out.println("Red: " + new_val.doubleValue());
                red = new_val.doubleValue() / MAX;
                updateHSB();
            }
        });
        this.add(redSlider, 1, row);

        ++row;
        Label greenLabel = new Label("Green: ");
        this.add(greenLabel, 0, row);

        greenSlider = buildSlider(MIN);
        greenSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                // System.out.println("Green: " + new_val.doubleValue());
                gre = new_val.doubleValue() / MAX;
                updateHSB();
            }
        });
        this.add(greenSlider, 1, row);

        ++row;
        Label blueLabel = new Label("Blue: ");
        this.add(blueLabel, 0, row);

        blueSlider = buildSlider(MIN);
        blueSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                // System.out.println("Blue: " + new_val.doubleValue());
                blu = new_val.doubleValue() / MAX;
                updateHSB();
            }
        });
        this.add(blueSlider, 1, row);

    }



    /************************************************************************
     * General run-time support code.
     */

    /**
     * RGB changed, so update values and UI for HSB.
     */
    private void updateHSB() {
        // System.out.println("updateHSB()");
        Color colour = Color.color(red, gre, blu, opa);

        hue = colour.getHue();
        theta = degreesToRadians(hue);
        val = colour.getBrightness();
        sat = colour.getSaturation();

        syncHueGuide();
        syncBrightnessSlider();
        syncSaturationSlider();

        setSwatch(colour);
    }

    /**
     * HSB changed, so update values and UI for RGB.
     */
    private void updateRGB() {
        // System.out.println("updateRGB()");
        Color colour = Color.hsb(hue, sat, val, opa);

        red = colour.getRed();
        gre = colour.getGreen();
        blu = colour.getBlue();

        syncRedSlider();
        syncGreenSlider();
        syncBlueSlider();

        setSwatch(colour);
    }



    /************************************************************************
     * Public interface.
     */

    public void setHue(double value) {
        hue = value;
        theta = degreesToRadians(hue);
        syncHueGuide();
        updateRGB();
    }

    public void setBrightness(double value) {
        val = value;
        syncBrightnessSlider();
    }

    public void setSaturation(double value) {
        sat = value;
        syncSaturationSlider();
    }

    public void setOpa(double value) {
        opa = value;
        syncOpacitySlider();
    }

    public void setRed(double value) {
        red = value;
        syncRedSlider();
    }

    public void setGreen(double value) {
        gre = value;
        syncGreenSlider();
    }

    public void setBlue(double value) {
        blu = value;
        syncBlueSlider();
    }

    public void setColour(Color colour) {
        // System.out.println("setColour() " + colour.toString());
        red = colour.getRed();
        gre = colour.getGreen();
        blu = colour.getBlue();

        syncRedSlider();
        syncGreenSlider();
        syncBlueSlider();

        updateHSB();
    }

    public void setAlphaColour(Color colour) {
        opa = colour.getOpacity();
        syncOpacitySlider();
        setColour(colour);
    }

    public double getMyWidth() { return width; }
    public double getMyHeight() { return height; }

    public Color getColour() { return Color.hsb(hue, sat, val); }
    public Color getAlphaColour() { return Color.hsb(hue, sat, val, opa); }


    /**
     * Constructor.
     */
    public ColourSelect() {
        super();
        // System.out.println("ColourSelect() constructed.");

        buildGrid();

        group = new Group();

        canvas = buildCanvas();
        guide = buildGuide();
        swatch = buildSwatch();

        group.getChildren().add(canvas);
        group.getChildren().add(guide);
        group.getChildren().add(swatch);

        drawRing();

        fillGrid();
        updateRGB();
     }
 
    public void init() {
        // System.out.println("ColourSelect() init()");

    }
 
 }
