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
 * DataStore1 is a class that serializes the settings data for saving and 
 * restoring to and from disc.
 */
package phillockett65.Tartan;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.util.ArrayList;

import javafx.scene.paint.Color;

public class DataStore1 extends DataStoreBase {
    private static final long serialVersionUID = 1L;

    private ArrayList<Integer> rowList;
    private ArrayList<Integer> colList;

    private Integer selectedSwatch = 0;
    private class ColourSwatch implements Serializable {
        public ColourSwatch(Color colour, String label) {
            red = colour.getRed();
            green = colour.getGreen();
            blue = colour.getBlue();
            name = label;
        }

        public Color getColour() { return Color.color(red, green, blue); }
        public String getName() { return name; }

        private Double red;
        private Double green;
        private Double blue;
        private String name;
    }
    private ArrayList<ColourSwatch> colourSwatches;

    private Boolean duplicate = true;
    private Boolean showGuide = true;
    private Double red = 1.0;
    private Double green = 1.0;
    private Double blue = 1.0;

    private Integer threadCount = 1;
    private Double threadSize = 1.0;
    private Double borderThickness = 1.0;



    /************************************************************************
     * Support code for the Initialization, getters and setters of DataStore1.
     */

    public DataStore1() {
        super();
        rowList = new ArrayList<Integer>();
        colList = new ArrayList<Integer>();
        colourSwatches = new ArrayList<ColourSwatch>();
    }

    public Color getGuideLineColour() { return Color.color(red, green, blue); }
    public void setGuideLineColour(Color colour) {
        red = colour.getRed();
        green = colour.getGreen();
        blue = colour.getBlue();
    }


    /**
     * Data exchange from the model to this DataStore.
     * @param model contains the data.
     * @return true if data successfully pulled from the model, false otherwise.
     */
    public boolean pull(Model model) {
        boolean success = true;

        final int rowCount = model.getRowCount();
        for (int i = 0; i < rowCount; ++i) {
            rowList.add(model.getRowColourIndex(i));
        }

        final int columnCount = model.getColumnCount();
        for (int i = 0; i < columnCount; ++i) {
            colList.add(model.getColColourIndex(i));
        }

        selectedSwatch = model.getSelectedColour();
        for (int i = 0; i < Default.SWATCH_COUNT.getInt(); ++i) {
            colourSwatches.add(new ColourSwatch(model.getSwatchColour(i), model.getSwatchName(i)));
        }

        duplicate = model.isDuplicate();
        showGuide = model.isShowGuide();
        setGuideLineColour(model.getGuideLineColour());

        threadCount = model.getThreadCount();
        threadSize = model.getThreadSize();
        borderThickness = model.getBorderThickness();

        return success;
    }

    /**
     * Data exchange from this DataStore to the model.
     * @param model contains the data.
     * @return true if data successfully pushed to the model, false otherwise.
     */
    public boolean push(Model model) {
        boolean success = true;

        model.initRowList(rowList.size());
        for (int i = 0; i < rowList.size(); ++i) {
            model.setRowColourIndex(i, rowList.get(i));
        }

        model.initColumnList(colList.size());
        for (int i = 0; i < colList.size(); ++i) {
            model.setColColourIndex(i, colList.get(i));
        }

        model.setSelectedColour(selectedSwatch);
        int i = 0;
        for (ColourSwatch swatch : colourSwatches) {
            model.setSwatch(i++, swatch.getColour(), swatch.getName());
        }

        model.setDuplicate(duplicate);
        model.setShowGuide(showGuide);
        model.setGuideLineColour(getGuideLineColour());

        model.initThreadCount(threadCount);
        model.initThreadSize(threadSize);
        model.initBorderThickness(borderThickness);

        return success;
    }


    /************************************************************************
     * Support code for static public interface.
     */

    /**
     * Static method that instantiates a DataStore, populates it from the 
     * model and writes it to disc.
     * @param model contains the data.
     * @return true if data successfully written to disc, false otherwise.
     */
    public static boolean writeData(Model model) {
        boolean success = false;

        DataStore1 store = new DataStore1();
        store.pull(model);
        store.dump();

        ObjectOutputStream objectOutputStream;
        try {
            objectOutputStream = new ObjectOutputStream(new FileOutputStream(model.getSettingsFile()));

            objectOutputStream.writeObject(store);
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

        return success;
    }

    /**
     * Static method that instantiates a DataStore, populates it from disc 
     * and writes it to the model.
     * @param model contains the data.
     * @return true if data successfully read from disc, false otherwise.
     */
    public static boolean readData(Model model) {
        boolean success = false;

        ObjectInputStream objectInputStream;
        try {
            objectInputStream = new ObjectInputStream(new FileInputStream(model.getSettingsFile()));

            DataStoreBase base = (DataStoreBase)objectInputStream.readObject();
            long SVUID = ObjectStreamClass.lookup(base.getClass()).getSerialVersionUID();
 
            DataStore1 store = null;
            if (SVUID == 1) {
                store = (DataStore1)base;
                success = store.push(model);
                store.dump();
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

        return success;
    }



    /************************************************************************
     * Support code for debug.
     */

    /**
     * Print data store on the command line.
     */
    public void dump() {
        System.out.print("Row Colours[" + rowList.size() + "] = { ");
        for (Integer i : rowList)
            System.out.print(i + " ");
        System.out.println("}");

        System.out.print("Col Colours[" + colList.size() + "] = { ");
        for (Integer i : colList)
            System.out.print(i + " ");
        System.out.println("}");

        System.out.println("Selected Swatch: " + selectedSwatch);
        System.out.println("Swatches[" + colourSwatches.size() + "] = { ");
        int i = 0;
        for (ColourSwatch s : colourSwatches)
            System.out.println("  " + i++ + " RGB(" + s.red + ", " + s.green + ", " + s.blue + ") - " + s.name);
        System.out.println("}");

        System.out.println("Duplicate Flag: " + duplicate);
        System.out.println("Show Guide Lines: " + showGuide);
        System.out.println("Guide Line Colour: RGB(" + red + ", " + green + ", " + blue + ")");

        System.out.println("Thread Repeat Count: " + threadCount);
        System.out.println("Thread Size: " + threadSize);
        System.out.println("Border Thickness: " + borderThickness);
    }

}

