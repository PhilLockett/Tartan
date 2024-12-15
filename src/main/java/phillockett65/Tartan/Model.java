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
 * Model is the class that captures the dynamic shared data plus some 
 * supporting constants and provides access via getters and setters.
 * It is implemented as a basic (non thread safe) Singleton.
 */
package phillockett65.Tartan;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Stage;

public class Model {

    private final static String DATAFILE = "Settings.dat";
    private final static String SWATCHES = "swatches";
    private final static String IMAGEFILE = "tartan.png";

    private static Model model = new Model();


    /************************************************************************
     * General support code.
     */
    

    private static final String TOPBARICON = "top-bar-icon";
 
    /**
     * Builds the cancel button as a Pane.
     * Does not include the mouse click handler.
     * @return the Pane that represents the cancel button.
     */
    public static Pane buildCancelButton() {
        final double iconSize = 32.0;
        final double cancelPadding = 0.3;

        Pane cancel = new Pane();
        cancel.setPrefWidth(iconSize);
        cancel.setPrefHeight(iconSize);
        cancel.getStyleClass().add(TOPBARICON);

        double a = iconSize * cancelPadding;
        double b = iconSize - a;
        Line line1 = new Line(a, a, b, b);
        line1.setStroke(Color.WHITE);
        line1.setStrokeWidth(4.0);
        line1.setStrokeLineCap(StrokeLineCap.ROUND);

        Line line2 = new Line(a, b, b, a);
        line2.setStroke(Color.WHITE);
        line2.setStrokeWidth(4.0);
        line2.setStrokeLineCap(StrokeLineCap.ROUND);

        cancel.getChildren().addAll(line1, line2);

        return cancel;
    }



    /************************************************************************
     * Support code for the Initialization of the Model.
     */

    private PrimaryController controller;
    private Stage stage;
    private Sample sample;

    /**
     * Private default constructor - part of the Singleton Design Pattern.
     * Called at initialization only, constructs the single private instance.
     */
    private Model() {
    }

    /**
     * Singleton implementation.
     * @return the only instance of the model.
     */
    public static Model getInstance() { return model; }

    /**
     * Called by the controller after the constructor to initialise any 
     * objects after the controls have been initialised.
     */
    public void initialize(PrimaryController mainController) {
        // System.out.println("Model initialized.");

        controller = mainController;

        initializeColourPalette();
        initializeLayout();
        initializeSample();
        initializeLoadPanel();
        initializeSavePanel();
        initializeStatusLine();

        defaultSettings();
    }

    /**
     * Called by the controller after the stage has been set. Completes any 
     * initialization dependent on other components being initialized.
     */
    public void init(Stage primaryStage, Sample primarySample) {
        // System.out.println("Model init.");

        stage = primaryStage;
        sample = primarySample;
    }

    /**
     * Set all attributes to the default values.
     */
    public void defaultSettings() {
        clearSwatches();
        setSelectedColour(1);
        setSwatch(0, Color.WHITE, "Blank");
        setSwatch(1, Color.LIGHTCYAN, "Highlight");
        setSwatch(2, Color.BLACK, "Lowlight");
        setSwatch(3, Color.RED, "Major 1");
        setSwatch(4, Color.GREEN, "Major 2");
        setSwatch(5, Color.BLUE, "Major 3");

        initColumnCount(Default.INIT_THREAD_COUNT.getInt());
        initRowCount(Default.INIT_THREAD_COUNT.getInt());
        initThreadSize(Default.INIT_THREAD_SIZE.getFloat());
        setGuideLineColour(Color.RED);
        initBorderThickness(Default.INIT_BORDER_THICKNESS.getFloat());
    }

    public PrimaryController getController() { return controller; }
    public Stage getStage() { return stage; }
    public Sample getSample() { return sample; }

    public void close() {
        sample.close();
        stage.close();
    }


    /************************************************************************
     * Support code for state persistence.
     */

    /**
     * Call the static DataStore1 method, to save the data to disc.
     * @return true if data successfully written to disc, false otherwise.
     */
    private boolean writeData() {
        return DataStore1.writeData();
    }


    /**
     * Call the static DataStore1 method, to read the data from disc.
     * @return true if data successfully read from disc, false otherwise.
     */
    private boolean readData() {
        if (DataStore1.readData() == true) {
            return true;
        }

        return false;
    }



    /************************************************************************
     * Support code for "Sample" panel.
     */

    private ObservableList<Integer> rowList = FXCollections.observableArrayList();
    private ObservableList<Integer> colList = FXCollections.observableArrayList();


    public int getRowCount() { return rowList.size(); }
    public int getColumnCount() { return colList.size(); }

    public void initRowList(int size) {
        rowList.clear();
        for (int i = 0; i < size; ++i)
            rowList.add(0);
    }
    public void initColumnList(int size) {
        colList.clear();
        for (int i = 0; i < size; ++i)
            colList.add(0);
    }

    public void setRowCount(int size) {
        final int count = getRowCount();
        if (size < count) {
            rowList.remove(size, count);
        } else {
            for (int i = count; i < size; ++i)
                rowList.add(getSelectedColour());
        }
    }

    public void setColumnCount(int size) {
        final int count = getColumnCount();
        if (size < count) {
            colList.remove(size, count);
        } else {
            for (int i = count; i < size; ++i)
                colList.add(getSelectedColour());
        }
    }

    public void setRowColourIndex(int i, int c) { if (i < getRowCount()) rowList.set(i, c); }
    public void setRowColour(int i) { setRowColourIndex(i, colourSelected); }
    public int getRowColourIndex(int i) { return rowList.get(i % getRowCount()); }
    public Color getRowColour(int i) { return getSwatchColour(getRowColourIndex(i)); }
    
    public void setColColourIndex(int i, int c) { if (i < getColumnCount()) colList.set(i, c); }
    public void setColColour(int i) { setColColourIndex(i, colourSelected); }
    public int getColColourIndex(int i) { return colList.get(i % getColumnCount()); }
    public Color getColColour(int i) { return getSwatchColour(getColColourIndex(i)); }

    /**
     * Initialize "Sample" panel.
     */
    private void initializeSample() {
        initRowList(Default.INIT_THREAD_COUNT.getInt());
        initColumnList(Default.INIT_THREAD_COUNT.getInt());
    }



    /************************************************************************
     * Support code for "Load" panel.
     */

    private LoadControl loadControl;
    private ObservableList<String> tartanList = FXCollections.observableArrayList();

    /**
     * Builds a list of tartans using the names of directories in the base 
     * directory.
     * @return true if the list contains entries, alse otherwise.
     */
    private boolean fillDirectoryList() {

        final String directoryName = getBaseDirectory();
        final File tartanPath= new File(directoryName);

        tartanList.clear();
        for (final File tartan : tartanPath.listFiles()) {
            if (tartan.isDirectory()) {
                // System.out.println(directoryName + "\\" + tartan.getName());
                tartanList.add(tartan.getName());
            }
        }

        return !tartanList.isEmpty();
    }

    /**
     * Provide access to the latest list of tartans.
     * @return the latest list of tartans.
     */
    public ObservableList<String> getTartanList() {
        fillDirectoryList();

        return tartanList;
    }

    public boolean launchFileLoader() {
        if (loadControl.showControl()) {
            return true;
        }

        return false;
    }

    /**
     * Called by the controller to initialize the load controller.
     */
    public void initializeLoadPanel() {
        // System.out.println("Load Controller initialized.");
        loadControl = new LoadControl("Load Tartan");
    }


    /************************************************************************
     * Support code for "Save" panel.
     */

    private SaveAsControl saveAsControl;

    public boolean launchSaveAs() {
        if (saveAsControl.showControl()) {
            return true;
        }

        return false;
    }

    /**
     * Called by the controller to initialize the Save controller.
     */
    public void initializeSavePanel() {
        // System.out.println("Save Controller initialized.");
        saveAsControl = new SaveAsControl("Save Tartan");
    }


    /************************************************************************
     * Support code for "Colour Palette" panel.
     */

    private int colourSelected;

    public void setSelectedColour(int index) { colourSelected = index; }
    public int getSelectedColour() { return colourSelected; }

    private class ColourSwatch  {
        public ColourSwatch() { clear(); }

        public void clear() { colour = Color.WHITE; name = null; }

        public Color colour;
        public String name;
    }

    private ArrayList<ColourSwatch> colourSwatches = new ArrayList<ColourSwatch>();

    public Color getCurrentColour() {
        return colourSwatches.get(colourSelected).colour;
    }

    public Color getSwatchColour(int index) {
        return colourSwatches.get(index).colour;
    }

    public String getSwatchName(int index) {
        return colourSwatches.get(index).name;
    }

    public boolean setSwatch(int index, Color colour, String name) {
        if (index >= Default.SWATCH_COUNT.getInt())
            return false;

        ColourSwatch swatch = colourSwatches.get(index);
        
        swatch.colour = colour;
        swatch.name = name;

        return true;
    }

    public boolean setSwatchColour(int index, Color colour) {
        if (index >= Default.SWATCH_COUNT.getInt())
            return false;

        ColourSwatch swatch = colourSwatches.get(index);
        
        swatch.colour = colour;

        return true;
    }

    public boolean setSwatchName(int index, String name) {
        if (index >= Default.SWATCH_COUNT.getInt())
            return false;

        ColourSwatch swatch = colourSwatches.get(index);
        
        swatch.name = name;

        return true;
    }


    public void clearSwatches() {
        for (ColourSwatch swatch : colourSwatches) {
            swatch.clear();
        }
    }

    /**
     * Initialize "Colour Palette" panel.
     */
    private void initializeColourPalette() {
        for (int i = 0; i < Default.SWATCH_COUNT.getInt(); ++i)
            colourSwatches.add(new ColourSwatch());

    }



    /************************************************************************
     * Support code for "Layout" panel.
     */

    private SpinnerValueFactory<Integer> columnCountSVF;

    private SpinnerValueFactory<Integer> rowCountSVF;

    private boolean duplicate = true;
    private boolean showGuide = true;

    private SpinnerValueFactory<Integer> threadCountSVF;

    private SpinnerValueFactory<Double> threadSizeSVF;

    private Color guideLineColour;

    private SpinnerValueFactory<Double> borderThicknessSVF;

    public SpinnerValueFactory<Integer> getColumnCountSVF() { return columnCountSVF; }
    public SpinnerValueFactory<Integer> getRowCountSVF() { return rowCountSVF; }
    public SpinnerValueFactory<Integer> getThreadCountSVF() { return threadCountSVF; }
    public SpinnerValueFactory<Double> getThreadSizeSVF() { return threadSizeSVF; }
    public SpinnerValueFactory<Double> getBorderThicknessSVF() { return borderThicknessSVF; }

    public boolean isDuplicate() { return duplicate; }
    public boolean isShowGuide() { return showGuide; }
    public int getThreadCount() { return threadCountSVF.getValue(); }
    public double getThreadSize() { return threadSizeSVF.getValue(); }
    public Color getBorderColour() { return Color.BLACK; }
    public Color getGuideLineColour() { return guideLineColour; }
    public double getBorderThickness() { return borderThicknessSVF.getValue(); }

    private void initColumnCount(int value) { columnCountSVF.setValue(value); }
    private void initRowCount(int value) { rowCountSVF.setValue(value); }
    public void setDuplicate(boolean state) { duplicate = state; }
    public void setShowGuide(boolean state) { showGuide = state; }
    public void initThreadCount(int value) { threadCountSVF.setValue(value); }
    public void initThreadSize(double value) { threadSizeSVF.setValue(value); }
    public void setGuideLineColour(Color colour) { guideLineColour = colour; }
    public void initBorderThickness(double value) { borderThicknessSVF.setValue(value); }

    /**
     * Get the width of the swatch in pixels.
     * @return the width of the swatch in pixels.
     */
    public double getSwatchWidth() { return getThreadSize() * getColumnCount(); }

    /**
     * Get the height of the swatch in pixels.
     * @return the height of the swatch in pixels.
     */
    public double getSwatchHeight() { return getThreadSize() * getRowCount(); }


    /**
     * Initialize "Layout" panel.
     */
    private void initializeLayout() {
        columnCountSVF = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Default.WIDTH.getInt(), Default.INIT_THREAD_COUNT.getInt());
        rowCountSVF = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Default.HEIGHT.getInt(), Default.INIT_THREAD_COUNT.getInt());
        threadCountSVF = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 8, 1);
        threadSizeSVF = new SpinnerValueFactory.DoubleSpinnerValueFactory(2.0, 40.0, 30.0, 1.0);
        borderThicknessSVF = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 10.0, 1.0, 0.1);
    }



    /************************************************************************
     * Support code for "Status Line" panel.
     */

    private String baseDirectory = ".\\" + SWATCHES;
    private String name = "";

    public String getBaseDirectory() { return baseDirectory; }

    public boolean isNamed() { return !name.isBlank(); }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    private String getOutputPath() {
        final String base = getBaseDirectory();
        if (name.isBlank())
            return base;
        
        return base + "\\" + name;
    }

    /**
     * Make the output directory if it doesn't already exist.
     * @return true if the directory exists, false otherwise.
     */
    private boolean makeTartanDirectory() {
        final String base = getOutputPath();
        File dir = new File(base);
        if (dir.exists())
            return true;

        return dir.mkdir();
    }

    private String getOutputImageFile() {
        return getOutputPath() + "\\" + IMAGEFILE;
    }

    /**
     * @return the file path of the settings data file.
     */
    public String getSettingsFile() {
        return getOutputPath() + "\\" + DATAFILE;
    }

    /**
     * Save the tartan design as an image.
     * @return true if the image was generated, false otherwise.
     */
    private boolean saveImage() {
        boolean success = false;
        final Canvas canvas;
        final GraphicsContext gc;
        final double size = getThreadSize();
        final double size2 = size * 2;
        final double size4 = size * 4;
        
        final double xMax = getColumnCount() * size2;
        final double yMax = getRowCount() * size2;
        canvas = new Canvas(xMax, yMax);

        gc = canvas.getGraphicsContext2D();

        gc.setStroke(getBorderColour());
        gc.setLineWidth(getBorderThickness());

        double xPos = 0;
        double yPos = 0;
        final int rCount = rowList.size();
        int count = getColumnCount() / 2;
        for (int i = 0; i < (rCount * 2); ++i) {
            final Color colour = getRowColour(i % rCount);
            
            int c = i % 4;
            c = (4 - c) % 4;
            xPos = size * c;
            for (int j = 0; j < count; ++j) {
                gc.setFill(colour);
                gc.fillRect(xPos, yPos, size2, size);
                gc.strokeRect(xPos, yPos, size2, size);

                xPos += size4;
            }
            yPos += size;
        }

        final int cCount = colList.size();
        count = getRowCount() / 2;
        xPos = 0;
        yPos = 0;
        for (int i = 0; i < (cCount * 2); ++i) {
            final Color colour = getColColour(i % cCount);
            
            int r = i % 4;
            r = (6 - r) % 4;
            yPos = size * r;
            for (int j = 0; j < count; ++j) {
                gc.setFill(colour);
                gc.fillRect(xPos, yPos, size, size2);
                gc.strokeRect(xPos, yPos, size, size2);

                yPos += size4;
            }
            xPos += size;
        }


        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);

        WritableImage snapshot = new WritableImage((int)xMax, (int)yMax);
        try {
            canvas.snapshot(parameters, snapshot);
        } catch (IllegalStateException e) {
            System.out.println("saveImage() - Failed to take snapshot: " + e);
        }

        try {
            final BufferedImage image = SwingFXUtils.fromFXImage(snapshot, null);
            File output = new File(getOutputImageFile());

            ImageIO.write(image, "png", output);
            success = true;
        } catch (Exception e) {
            System.out.println("saveImage() - Failed saving image: " + e);
        }

        return success;
    }

    /**
     * Save the tartan design data and as an image.
     * @return the file path of the saved data.
     */
    public String saveTartan() {
        makeTartanDirectory();
        writeData();
        saveImage();

        return getOutputPath();
    }

    /**
     * Load the tartan design data.
     * @return the name of the loaded data.
     */
    public String loadTartan() {
        readData();

        return getName();
    }

    /**
     * Initialize "Status Line" panel.
     */
    private void initializeStatusLine() {
        makeTartanDirectory();
    }


}
