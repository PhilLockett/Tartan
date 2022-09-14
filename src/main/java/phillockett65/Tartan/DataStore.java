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
 * DataStore is a class that serializes the settings data for saving and 
 * restoring to and from disc.
 */
package phillockett65.Tartan;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import javafx.scene.paint.Color;

public class DataStore implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<Integer> rowColours = new ArrayList<Integer>();
    private ArrayList<Integer> colColours = new ArrayList<Integer>();

    private Integer selectedSwatch;
    private class ColourSwatch implements Serializable {
        public ColourSwatch() { red = 0D; green = 0D; blue = 0D; name = null; }

        public Double red;
        public Double green;
        public Double blue;
        public String name;
    }
    private ArrayList<ColourSwatch> colourSwatches = new ArrayList<ColourSwatch>();

    
    private int horizontalCount;
    private int verticalCount;
    private Double threadSize;
    private Double red, green, blue;
    private Double borderThickness;



    /************************************************************************
     * Support code for the Initialization, getters and setters of DataStore.
     */

    public DataStore() {
    }

    public int getRowCount() { return rowColours.size(); }
    public void setRowCount(int count) {
        for (int i = 0; i < count; ++i)
            rowColours.add(0);
    }
    public int getRowColour(int index) {
        return rowColours.get(index);
    }
    public void setRowColour(int index, int value) {
        rowColours.set(index, value);
    }

    public int getColCount() { return colColours.size(); }
    public void setColCount(int count) {
        for (int i = 0; i < count; ++i)
            colColours.add(0);
    }
    public int getColColour(int index) {
        return colColours.get(index);
    }
    public void setColColour(int index, int value) {
        colColours.set(index, value);
    }
 
    public int getSelectedSwatch() { return selectedSwatch; }
    public void setSelectedSwatch(int selectedColour) { this.selectedSwatch = selectedColour; }

    public int getSwatchCount() { return colourSwatches.size(); }
    public void setSwatchCount(int count) {
        for (int i = 0; i < count; ++i)
            colourSwatches.add(new ColourSwatch());
    }

    public Color getSwatchColour(int index) {
        ColourSwatch swatch = colourSwatches.get(index);

        return Color.color(swatch.red, swatch.green, swatch.blue);
    }

    public String getSwatchName(int index) {
        return colourSwatches.get(index).name;
    }

    public boolean setSwatch(int index, Color colour, String name) {
        if (index >= getSwatchCount())
            return false;

        ColourSwatch swatch = colourSwatches.get(index);
        
        swatch.red = colour.getRed();
        swatch.green = colour.getGreen();
        swatch.blue = colour.getBlue();
        swatch.name = name;

        return true;
    }

    public int getHorizontalCount() { return horizontalCount; }
    public void setHorizontalCount(int horizontalCount) { this.horizontalCount = horizontalCount; }
    public int getVerticalCount() { return verticalCount; }
    public void setVerticalCount(int verticalCount) { this.verticalCount = verticalCount; }
    public Double getThreadSize() { return threadSize; }
    public void setThreadSize(Double ringRadius) { this.threadSize = ringRadius; }

    public Color getBorderColour() { return Color.color(red, green, blue); }
    public void setBorderColour(Color colour) {
        red = colour.getRed();
        green = colour.getGreen();
        blue = colour.getBlue();
    }
    public Double getBorderThickness() { return borderThickness; }
    public void setBorderThickness(Double borderThickness) { this.borderThickness = borderThickness; }



    /************************************************************************
     * Support code for debug.
     */

     /**
      * Print data store on the command line.
      */
      public void dump() {
        System.out.print("Row Colours[" + getRowCount() + "] = { ");
        for (Integer i : rowColours)
            System.out.print(i + " ");
        System.out.println("}");

        System.out.print("Col Colours[" + getColCount() + "] = { ");
        for (Integer i : colColours)
            System.out.print(i + " ");
        System.out.println("}");

        System.out.println("Selected Swatch: " + selectedSwatch);
        System.out.println("Swatches[" + getSwatchCount() + "] = { ");
        int i = 0;
        for (ColourSwatch s : colourSwatches)
            System.out.println("  " + i++ + " RGB(" + s.red + ", " + s.green + ", " + s.blue + ") - " + s.name);
        System.out.println("}");

        System.out.println("Horizontal Repeat Count: " + horizontalCount);
        System.out.println("Vertical Repeat Count: " + verticalCount);
        System.out.println("Thread Size: " + threadSize);
        System.out.println("Border Colour: RGB(" + red + ", " + green + ", " + blue + ")");
        System.out.println("Border Thickness: " + borderThickness);
    }



    /************************************************************************
     * Support code for static public interface.
     */

    /**
     * Static method that receives a populated DataStore and writes it to disc.
     * @param dataStore contains the data.
     * @param settingsFile path of the settings data file.
     * @return true if data successfully written to disc, false otherwise.
     */
    public static boolean writeData(DataStore dataStore, String settingsFile) {
        boolean success = false;

        ObjectOutputStream objectOutputStream;
        try {
            objectOutputStream = new ObjectOutputStream(new FileOutputStream(settingsFile));

            objectOutputStream.writeObject(dataStore);
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

        return success;
    }

    /**
     * Static method that instantiates a DataStore, populates it from disc 
     * and returns it.
     * @param settingsFile path of the settings data file.
     * @return a populated DataStore if data successfully read from disc, null otherwise.
     */
    public static DataStore readData(String settingsFile) {
        DataStore dataStore = null;

        ObjectInputStream objectInputStream;
        try {
            objectInputStream = new ObjectInputStream(new FileInputStream(settingsFile));

            dataStore = (DataStore)objectInputStream.readObject();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

        return dataStore;
    }

}

