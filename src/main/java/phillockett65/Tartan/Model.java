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

    /**
     * Add or remove the unfocussed style from the given pane object.
     * @param pane to add/remove unfocussed style.
     * @param style named in .css to define unfocussed style.
     * @param state is true if we have focus, false otherwise.
     */
    public static void styleFocus(Pane pane, String style, boolean state) {
        if (state) {
            pane.getStyleClass().remove(style);
        } else {
            if (!pane.getStyleClass().contains(style)) {
                pane.getStyleClass().add(style);
            }
        }
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
        sample = new Sample();

        initializeColourPalette();
        initializeLayout();
        initializeSample();
        initializeStatusLine();

    }

    /**
     * Called by the controller after the stage has been set. Completes any 
     * initialization dependent on other components being initialized.
     */
    public void init(Stage primaryStage) {
        // System.out.println("Model init.");

        stage = primaryStage;
        sample.init();

        defaultSettings();
    }

    /**
     * Set all attributes to the default values.
     */
    public void defaultSettings() {
        clearSwatches();
        setSelectedColourIndex(1);
        setSwatch(0, Color.WHITE, "Blank");
        setSwatch(1, Color.LIGHTCYAN, "Highlight");
        setSwatch(2, Color.BLACK, "Lowlight");
        setSwatch(3, Color.RED, "Major 1");
        setSwatch(4, Color.GREEN, "Major 2");
        setSwatch(5, Color.BLUE, "Major 3");

        setColumnCountSVF(Default.INIT_THREAD_COUNT.getInt());
        setRowCountSVF(Default.INIT_THREAD_COUNT.getInt());
        setDuplicate(true);
        setShowGuide(true);
        setGuideLineColour(Color.RED);
        initThreadCount(1);
        initThreadSize(Default.INIT_THREAD_SIZE.getFloat());
        initBorderThickness(Default.INIT_BORDER_THICKNESS.getFloat());

        sample.clear();
        sample.syncThreadSize();
    }

    public PrimaryController getController() { return controller; }
    public Stage getStage() { return stage; }
    public Sample getSample() { return sample; }
    public String getTitle() { return stage.getTitle(); }

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

    public void setRowList(ArrayList<Integer> list) {
        setRowCountSVF(list.size());
        sample.setRowList(list);
    }
    public void setColumnList(ArrayList<Integer> list) {
        setColumnCountSVF(list.size());
        sample.setColumnList(list);
    }

    public ArrayList<Integer> getRowList() { return sample.getRowList(); }
    public ArrayList<Integer> getColumnList() { return sample.getColumnList(); }

    public int getColumnCount() { return sample.getColumnCount(); }
    public int getRowCount() { return sample.getRowCount(); }

    private int getRowColourIndex(int i) { return sample.getRowColourIndex(i % getRowCount()); }
    private int getColColourIndex(int i) { return sample.getColColourIndex(i % getColumnCount()); }


    /**
     * Initialize "Sample" panel.
     */
    private void initializeSample() {
    }



    /************************************************************************
     * Support code for "Load" panel.
     */

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


 
     /************************************************************************
     * Support code for "Colour Palette" panel.
     */

    private class ColourSwatch  {
        public ColourSwatch() { clear(); }

        public void clear() { colour = Color.WHITE; name = ""; }

        public Color colour;
        public String name;
    }


    private int colourSelected = 0;
    private ArrayList<ColourSwatch> colourSwatches = new ArrayList<ColourSwatch>(Default.SWATCH_COUNT.getInt());

    public int setSelectedColourIndex(int index) { 
        if (index < colourSwatches.size()) {
            final int previous = colourSelected;
            colourSelected = index;

            return previous;
        }

        return index;
    }
    public int getSelectedColourIndex() { return colourSelected; }

    public Color getSelectedColour() {
        return colourSwatches.get(colourSelected).colour;
    }

    public Color getSwatchColour(int index) {
        if (index < colourSwatches.size()) {
            return colourSwatches.get(index).colour;
        }

        return Color.WHITE;
    }

    public String getSwatchName(int index) {
        if (index < colourSwatches.size()) {
            return colourSwatches.get(index).name;
        }

        return "";
    }

    public boolean setSwatch(int index, Color colour, String name) {
        if (index < colourSwatches.size()) {
            ColourSwatch swatch = colourSwatches.get(index);

            swatch.colour = colour;
            swatch.name = name;
            
            return true;
        }

        return false;
    }

    public boolean setSwatchColour(int index, Color colour) {
        if (index < colourSwatches.size()) {
            colourSwatches.get(index).colour = colour;
            sample.syncColour();

            return true;
        }

        return false;
    }

    public boolean setSwatchName(int index, String name) {
        if (index < colourSwatches.size()) {
            colourSwatches.get(index).name = name;

            return true;
        }

        return false;
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
        for (int i = 0; i < Default.SWATCH_COUNT.getInt(); ++i) {
            colourSwatches.add(new ColourSwatch());
        }
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

    private void setColumnCountSVF(int value) { columnCountSVF.setValue(value); }
    private void setRowCountSVF(int value) { rowCountSVF.setValue(value); }

    public void syncColumnCountSVF() { setColumnCountSVF(getColumnCount()); }
    public void syncRowCountSVF() { setRowCountSVF(getRowCount()); }

    public void syncDuplicateSVF() {
        if (duplicate) {
            setRowCountSVF(getColumnCount());
        }
    }

    public void setDuplicate(boolean state) {
        duplicate = state;

        if (duplicate) {
            setRowCountSVF(getColumnCount());
            sample.syncDuplicateThreads();
        }
    }

    public void setShowGuide(boolean state) {
        showGuide = state;
        sample.syncGuideVisible();
    }
    public void initThreadCount(int value) { threadCountSVF.setValue(value); }
    public void initThreadSize(double value) { threadSizeSVF.setValue(value); }
    public void setGuideLineColour(Color colour) {
        guideLineColour = colour;
        sample.syncGuideLineColour();
    }
    public void initBorderThickness(double value) { borderThicknessSVF.setValue(value); }


    /**
     * Initialize "Layout" panel.
     */
    private void initializeLayout() {
        columnCountSVF = new SpinnerValueFactory.IntegerSpinnerValueFactory(Default.MIN_THREAD_COUNT.getInt(), Default.WIDTH.getInt(), Default.INIT_THREAD_COUNT.getInt());
        rowCountSVF = new SpinnerValueFactory.IntegerSpinnerValueFactory(Default.MIN_THREAD_COUNT.getInt(), Default.HEIGHT.getInt(), Default.INIT_THREAD_COUNT.getInt());
        threadCountSVF = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 8, 1);
        threadSizeSVF = new SpinnerValueFactory.DoubleSpinnerValueFactory(2.0, 40.0, 30.0, 1.0);
        borderThicknessSVF = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 10.0, 1.0, 0.1);
    }



    /************************************************************************
     * Support code for "Status Line" panel.
     */

    private String baseDirectory = ".\\" + SWATCHES;
    private String name;

    public String getBaseDirectory() { return baseDirectory; }

    public boolean isNamed() { return !((name == null) || (name.isBlank())); }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    private String getOutputPath() {
        final String base = getBaseDirectory();
        if (!isNamed())
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

    private Color getRowColour(int i) { return getSwatchColour(getRowColourIndex(i)); }
    private Color getColColour(int i) { return getSwatchColour(getColColourIndex(i)); }

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

        final int cCount = getColumnCount();
        final int rCount = getRowCount();
        final double xMax = cCount * size2;
        final double yMax = rCount * size2;
        canvas = new Canvas(xMax, yMax);

        gc = canvas.getGraphicsContext2D();

        gc.setStroke(getBorderColour());
        gc.setLineWidth(getBorderThickness());

        double xPos = 0;
        double yPos = 0;
        int count = cCount / 2;
        for (int i = 0; i < (rCount * 2); ++i) {
            final Color colour = getRowColour(i);
            
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

        count = rCount / 2;
        xPos = 0;
        yPos = 0;
        for (int i = 0; i < (cCount * 2); ++i) {
            final Color colour = getColColour(i);
            
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
