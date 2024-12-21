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
 * Default is an enumeration that captures the static values used by the  
 * application and provides access via integer and real getters.
 */
package phillockett65.Tartan;

/**
 *
 * @author Phil
 */
public enum Default {
    WIDTH (200F),
    HEIGHT (200F),

    MIN_WIDTH (20F),
    MAX_WIDTH (2000F),
    MIN_HEIGHT (20F),
    MAX_HEIGHT (2000F),

    TOP_BAR_HEIGHT (32F),

    MPC_WIDTH (1050F),
    MPC_HEIGHT (1050F),
    SWATCH_COUNT (8F),
    GUIDE_COUNT (7F),
    TOTAL_GUIDE_COUNT (14F),
    BORDER_WIDTH (30F),
    INIT_THREAD_SIZE (6F),
    INIT_THREAD_COUNT (114F),
    MIN_THREAD_COUNT (20F),
    INIT_BORDER_THICKNESS (1.0F);

    private final int	iValue;
    private final float	rValue;

    Default(float val) {
        rValue = val;
        iValue = Math.round(val);
    }
    
    public int getInt() { return iValue; }
    public float getFloat() { return rValue; }

}
