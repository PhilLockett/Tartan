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
 */
package phillockett65.Tartan;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Model {

    private final static String DATAFILE = "Settings.dat";
    private final static String SWATCHES = "swatches";
    private final static String IMAGEFILE = "tartan.png";



    /************************************************************************
     * General support code.
     */
    


    /************************************************************************
     * Support code for the Initialization of the Model.
     */

    private PrimaryController controller;
    private Stage stage;
    private Sample sample;

    /**
     * Responsible for constructing the Model and any local objects. Called by 
     * the controller.
     */
    public Model(PrimaryController mainController) {
        controller = mainController;
        group = new Group();
    }

    /**
     * Called by the controller after the constructor to initialise any 
     * objects after the controls have been initialised.
     */
    public void initialize() {
        // System.out.println("Model initialized.");
 
        initializeColourPalette();
        initializeLayout();
        initializeSample();
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
        
        initColumnCount(114);
        initRowCount(114);
        initThreadSize(6.0);
        setBorderColour(Color.BLACK);
        initBorderThickness(1.0);
    }

    public PrimaryController getController() { return controller; }
    public Stage getStage() { return stage; }
    public Sample getSample() { return sample; }



    /************************************************************************
     * Support code for state persistence.
     */

    /**
     * Instantiate a DataStore, populate it with data and save it to disc.
     * @return true if data successfully written to disc, false otherwise.
     */
    private boolean writeData() {
        final String file = getSettingsFile();
        DataStore data = new DataStore();

        data.setRowCount(rowList.size());
        for (int i = 0; i < rowList.size(); ++i)
            data.setRowColour(i, getRowColourIndex(i));

        data.setColCount(colList.size());
        for (int i = 0; i < colList.size(); ++i)
            data.setColColour(i, getColColourIndex(i));

        data.setSelectedSwatch(getSelectedColour());
        data.setSwatchCount(Default.SWATCH_COUNT.getInt());
        for (int i = 0; i < Default.SWATCH_COUNT.getInt(); ++i)
            data.setSwatch(i, getSwatchColour(i), getSwatchName(i));

        data.setHorizontalCount(getColumnCount());
        data.setVerticalCount(getRowCount());
        data.setThreadSize(getThreadSize());
        data.setBorderColour(getBorderColour());
        data.setBorderThickness(getBorderThickness());

        if (!DataStore.writeData(data, file)) {
            data.dump();

            return false;
        }

        return true;
    }

    /**
     * Get a DataStore populated with data previously stored to disc and update
     * the model with the data.
     * @return true if the model is successfully updated, false otherwise.
     */
    private boolean readData() {
        final String file = getSettingsFile();
        DataStore data = DataStore.readData(file);
        if (data == null)
            return false;

        initRowList(data.getRowCount());
        for (int i = 0; i < rowList.size(); ++i)
            setRowColourIndex(i, data.getRowColour(i));

        initColumnList(data.getColCount());
        for (int i = 0; i < colList.size(); ++i)
            setColColourIndex(i, data.getColColour(i));

        setSelectedColour(data.getSelectedSwatch());
        for (int i = 0; i < Default.SWATCH_COUNT.getInt(); ++i)
            setSwatch(i, data.getSwatchColour(i), data.getSwatchName(i));

        initColumnCount(data.getHorizontalCount());
        initRowCount(data.getVerticalCount());
        initThreadSize(data.getThreadSize());
        setBorderColour(data.getBorderColour());
        initBorderThickness(data.getBorderThickness());

        return true;
    }



    /************************************************************************
     * Support code for "Sample" panel.
     */

    private Group group;
    private ObservableList<Integer> rowList = FXCollections.observableArrayList();
    private ObservableList<Integer> colList = FXCollections.observableArrayList();


    /**
     * @return the Group used by the "Sample" panel.
     */
    public Group getGroup() { return group; }

    public int getRowCount() { return rowList.size(); }
    public int getColumnCount() { return colList.size(); }

    private void initRowList(int size) {
        rowList.clear();
        for (int i = 0; i < size; ++i)
            rowList.add(0);
    }
    private void initColumnList(int size) {
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

    public void setRowColour(int i) { rowList.set(i, colourSelected); }
    public void setRowColourIndex(int i, int c) { rowList.set(i, c); }
    public void setColColour(int i) { colList.set(i, colourSelected); }
    public void setColColourIndex(int i, int c) { colList.set(i, c); }

    public int getRowColourIndex(int i) { return rowList.get(i); }
    public int getColColourIndex(int i) { return colList.get(i); }

    public Color getRowColour(int i) { return getSwatchColour(rowList.get(i)); }
    public Color getColColour(int i) { return getSwatchColour(colList.get(i)); }

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

    private ObservableList<String> tartanList = FXCollections.observableArrayList();

    private boolean loadWindowLaunched = false;

    public boolean isLoadWindowLaunched() { return loadWindowLaunched; }
    public void setLoadWindowLaunched(boolean state) { loadWindowLaunched = state; }

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

    /**
     * Called by the controller to initialize the load controller.
     */
    public void initializeLoadPanel() {
        // System.out.println("Load Controller initialized.");
    }


    /************************************************************************
     * Support code for "Save" panel.
     */

    private boolean saveWindowLaunched = false;

    public boolean isSaveAsWindowLaunched() { return saveWindowLaunched; }
    public void setSaveAsWindowLaunched(boolean state) { saveWindowLaunched = state; }

    /**
     * Called by the controller to initialize the Save controller.
     */
    public void initializeSavePanel() {
        // System.out.println("Save Controller initialized.");

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

    private Color borderColour;

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
    public Color getBorderColour() { return borderColour; }
    public double getBorderThickness() { return borderThicknessSVF.getValue(); }

    private void initColumnCount(int value) { columnCountSVF.setValue(value); }
    private void initRowCount(int value) { rowCountSVF.setValue(value); }
    public void setDuplicate(boolean state) { duplicate = state; }
    public void setShowGuide(boolean state) { showGuide = state; }
    public void initThreadSize(double value) { threadSizeSVF.setValue(value); }
    public void setBorderColour(Color colour) { borderColour = colour; }
    public void initBorderThickness(double value) { borderThicknessSVF.setValue(value); }


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
