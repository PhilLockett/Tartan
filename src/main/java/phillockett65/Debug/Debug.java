/*  Debug - a JavaFX package for logging diagnostics.
 *
 *  Copyright 2025 Philip Lockett.
 *
 *  This file is part of Debug.
 *
 *  Debug is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Debug is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Debug.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * Debug is the static class that manages debug output.
 */
package phillockett65.Debug;


public class Debug {

    private static final int NONE = 0;
    private static final int CRITICAL = 1;
    private static final int MAJOR = 2;
    private static final int MINOR = 3;
    private static final int TRACE = 4;
    private static final int INFO = 5;
    private static final int ALL = 6;

    // Logging level for entire application.
    private static final int LEVEL = MINOR;



    /************************************************************************
     * General support code.
     */

    private static String pre(int level) {
        switch (level) {
            case CRITICAL:  return "Critical error";
            case MAJOR:     return "Major error";
            case MINOR:     return "Minor error";
            case TRACE:     return "Trace";
            case INFO:      return "Info";
        }

        return "";
    }

    private static String formMessage(int level, String line) {
        if (line == null) {
            return "";
        }

        if (line.isEmpty()) {
            return "";
        }

        return pre(level) + ": " + line;
    }

    private static void display(int level, int delta, String line) {
        if (level-delta <= LEVEL) {
            final String message = formMessage(level, line);
            if (level <= MAJOR)
                System.err.println(message);
            else
                System.out.println(message);
        }
    }



    /************************************************************************
     * Support code for the Initialization of the Model.
     */

    /**
     * Private default constructor - part of the Singleton Design Pattern.
     * Called at initialization only, constructs the single private instance.
     */
    private Debug() {
    }



    /************************************************************************
     * Support code for static public interface.
     */

    /**
     * Log Critical error messsage.
     * 
     * @param delta adjustment, +ve values increase the amount of debug 
     *              generated, whereas -ve values decrease it.
     * @param line to log
     */
    public static void critical(int delta, String line) {
        display(CRITICAL, delta, line);
    }

    /**
     * Log Major error messsage.
     * @param delta adjustment, +ve values increase the amount of debug 
     *              generated, whereas -ve values decrease it.
     * @param line to log
     */
    public static void major(int delta, String line) {
        display(MAJOR, delta, line);
    }

    /**
     * Log Minor error messsage.
     * @param delta adjustment, +ve values increase the amount of debug 
     *              generated, whereas -ve values decrease it.
     * @param line to log
     */
    public static void minor(int delta, String line) {
        display(MINOR, delta, line);
    }

    /**
     * Log Trace messsage.
     * @param delta adjustment, +ve values increase the amount of debug 
     *              generated, whereas -ve values decrease it.
     * @param line to log
     */
    public static void trace(int delta, String line) {
        display(TRACE, delta, line);
    }

    /**
     * Log Informational messsage.
     * @param delta adjustment, +ve values increase the amount of debug 
     *              generated, whereas -ve values decrease it.
     * @param line to log
     */
    public static void info(int delta, String line) {
        display(INFO, delta, line);
    }

}
