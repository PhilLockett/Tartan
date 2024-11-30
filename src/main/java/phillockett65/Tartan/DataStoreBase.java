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
 * DataStoreBase is a class that serializes the settings data for saving and 
 * restoring to and from disc.
 */
package phillockett65.Tartan;

import java.io.Serializable;

public class DataStoreBase implements Serializable {
    private static final long serialVersionUID = 1L;

    public DataStoreBase() { }

    public void dump() { }



    /************************************************************************
     * Support code for static public interface.
     */

    /**
     * Static method that receives a populated DataStoreBase and writes it to disc.
     * @param store contains the data.
     * @param settingsFile path String of the settings data file.
     * @return false to indicate unsuccessful.
     */
    public static boolean writeData(DataStoreBase store, String settingsFile) {
        return false;
    }

    /**
     * Static method that instantiates a DataStoreBase, populates it from disc 
     * and returns it.
     * @param settingsFile path of the settings data file.
     * @return null to indicate unsuccessful.
     */
    public static DataStoreBase readData(String settingsFile) {
        return null;
    }

}

