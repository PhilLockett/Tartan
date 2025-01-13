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
 * ColourFilter is an functional interface that enables lambda expressions to 
 * get the component parts of a Color.
 */
package phillockett65.ColourSelect;

import javafx.scene.paint.Color;

@FunctionalInterface
public interface ColourFilter {
    public double filter(Color colour);
}

