/*  Tartan - a JavaFX based Tartan image generator.
 *
 *  Copyright 2024 Philip Lockett.
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
 * Warp is a class that captures a collection of threads.
 */
package phillockett65.Tartan;

import java.util.ArrayList;

import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import phillockett65.Debug.Debug;


public class Warp {

    private Model model;

    private Group group;
    private GraphicsContext gc;

    private final boolean ROW;
    private final int MIN;
    private final int MAX;
    private int active;

    private ArrayList<Thread> list;

    private final Color defaultColour;



    /************************************************************************
     * General support code.
     */

    private static double OFFSET = Default.BORDER_WIDTH.getFloat();



    /************************************************************************
     * Support code for the Thread sub-class.
     */

    /**
     * Class to represent a single thread as a sequence of rectangles.
     */
    private class Thread {
        private boolean highlight;
        private final int index;
        private int colourIndex;
        private Rectangle stitch;

        /**
         * The Constructor is singularly responsible for adding the Rectangles
         * to the Group.
         * @param index of the row or column.
         * @param row if true, column otherwise.
         */
        public Thread(int index) {
            highlight = false;

            this.index = index;
            this.colourIndex = 0;
            ObservableList<Node> items = group.getChildren();

            final Color fillColour = model.getSwatchColour(colourIndex);
            stitch = new Rectangle();
            stitch.setFill(fillColour);
            stitch.setStroke(defaultColour);

            items.add(stitch);
        }

        public int getColourIndex() { return colourIndex; }
        public boolean isVisible() { return stitch.isVisible(); }

        /**
         * Set the visibilty of the lead thread.
         * @param visible 
         */
        public void setVisible(boolean visible) {
            stitch.setVisible(visible);
        }

        public void clear() {
            highlight = false;

            colourIndex = 0;

            final Color fillColour = model.getSwatchColour(0);
            stitch.setFill(fillColour);
        }

        /**
         * Draw the thread using the current gc colours.
         */
        private void _draw() {
            if (isRow())
                drawRow();
            else
                drawCol();
        }

        /**
         * Set the gc colours for the thread and then draw them.
         * @param border colour to use.
         */
        private void draw(Color border) {
            gc.setFill(model.getSwatchColour(colourIndex));
            gc.setStroke(border);
            _draw();
        }

        /**
         * Set the gc colours for the thread and then draw them.
         */
        private void draw() {
            final Color border = highlight ? model.getGuideLineColour() : defaultColour;
            draw(border);
        }

        /**
         * Set the colour of the thread to the given swatch colour.
         * @param index of the selected swatch.
         */
        public void setColourIndex(int index) {
            colourIndex = index;
            stitch.setFill(model.getSwatchColour(colourIndex));

            draw();
        }

        public void setHighlight(boolean state, Color border) {
            if (state == highlight) {
                return;
            }

            highlight = state;
            stitch.setStroke(border);

            draw(border);
        }

        /**
         * Synchronise the colour of the thread to the updated swatch colour.
         * @return true if this thread needed to be updated, false otherwise.
         */
        public boolean syncCurrentColour() {
            if (colourIndex != model.getSelectedColourIndex())
                return false;

            final Color color = model.getSelectedColour();

            stitch.setFill(color);

            draw();

            return true;
        }


        private void drawRow() {
            final double size = model.getThreadSize();
            final double size2 = size * 2;
            final double size4 = size * 4;
            final double yPos = index * size;
            final int count = (int)(Default.WIDTH.getFloat() / 4);

            int c = index % 4;
            c = (4 - c) % 4;
            double xPos = c * size;
            for (int j = 0; j < count; ++j) {
                gc.fillRect(xPos, yPos, size2, size);
                gc.strokeRect(xPos, yPos, size2, size);

                xPos += size4;
            }
        }

        private void drawCol() {
            final double size = model.getThreadSize();
            final double size2 = size * 2;
            final double size4 = size * 4;
            final double xPos = index * size;
            final int count = (int)(Default.HEIGHT.getFloat() / 4);

            int r = index % 4;
            r = (6 - r) % 4;
            double yPos = r * size;
            for (int j = 0; j < count; ++j) {
                gc.fillRect(xPos, yPos, size, size2);
                gc.strokeRect(xPos, yPos, size, size2);

                yPos += size4;
            }
        }

        /**
         * Synchronise the thread size for a row. Sets the size and position of
         * the Rectangles.
         */
        private void syncRowSize() {
            final double size = model.getThreadSize();
            final double yPos = (index * size);
            final double thickness = model.getBorderThickness();

            int c = index % 4;
            stitch.setWidth(OFFSET + ((c == 1) ? size : 0));
            stitch.setHeight(size);
            stitch.setX(0D);
            stitch.setY(OFFSET + yPos);
            stitch.setVisible(index < model.getRowCount());
            stitch.setStrokeWidth(thickness);

            final Color fillColour = model.getSwatchColour(colourIndex);
            gc.setFill(fillColour);

            drawRow();
        }

        /**
         * Synchronise the thread size for a column. Sets the size and position
         * of the Rectangles.
         */
        private void syncColSize() {
            final double size = model.getThreadSize();
            final double xPos = (index * size);
            final double thickness = model.getBorderThickness();

            int r = index % 4;
            stitch.setWidth(size);
            stitch.setHeight(OFFSET + ((r == 3) ? size : 0));
            stitch.setX(OFFSET + xPos);
            stitch.setY(0D);
            stitch.setVisible(index < model.getColumnCount());
            stitch.setStrokeWidth(thickness);

            final Color fillColour = model.getSwatchColour(colourIndex);
            gc.setFill(fillColour);

            drawCol();
        }

    }





    /************************************************************************
     * Support code for the Warp class.
     */


    /**
     * Constructor.
     */
    public Warp(boolean row, int min, int max) {
		Debug.trace("Warp constructed");

        model = Model.getInstance();
        defaultColour = model.getBorderColour();

        ROW = row;
        MIN = min;
        MAX = max;
        active = Default.INIT_THREAD_COUNT.getInt();
        
        list = new ArrayList<Thread>(MAX);
    }


    /**
     * Initialization after the model has been initialised.
     */
    public void init(Group g, GraphicsContext c) {
        group = g;
        gc = c;

        for (int index = 0; index < MAX; ++index) {
            Thread thread = new Thread(index);
            list.add(thread);
        }

    }


    public void clear() {
        for (Thread thread : list) {
            thread.clear();
        }
    }


    /************************************************************************
     * Public interface.
     */

    boolean isRow() { return ROW; }
    int getActive() { return active; }
    int getMax() { return MAX; }

    Thread get(int index) { return list.get(index); }

    public boolean setColourIndex(int index, int colourIndex) {
        if (index < MAX) {
            list.get(index).setColourIndex(colourIndex);
            return true;
        }

        return false;
    }

    public int getColourIndex(int index) {
        if (index < MAX) {
            return list.get(index).getColourIndex();
        }

        return 0;
    }

    public void syncVisibleThreads() {
        final int ACTIVE = getActive();

        for (int index = 0; index < MAX; ++index) {
            list.get(index).setVisible(index < ACTIVE);
        }
    }

    public void syncRepeatThreads() {
        final int ACTIVE = getActive();

        for (int index = ACTIVE; index < MAX; ++index) {
            final int colourIndex = list.get(index % ACTIVE).getColourIndex();

            list.get(index).setColourIndex(colourIndex);
        }
    }

    private void syncThreads() {
        syncVisibleThreads();
        syncRepeatThreads();
    }

    public void rotateIncrease() {
        final int ACTIVE = getActive();

        final int safeIndex = list.get(0).getColourIndex();
        for (int index = 1; index < ACTIVE; ++index) {
            final int colourIndex = list.get(index).getColourIndex();

            list.get(index-1).setColourIndex(colourIndex);
        }
        list.get(ACTIVE-1).setColourIndex(safeIndex);

        syncRepeatThreads();
    }

    public void rotateDecrease() {
        final int ACTIVE = getActive();

        final int safeIndex = list.get(ACTIVE-1).getColourIndex();
        for (int index = ACTIVE-1; index > 0 ; --index) {
            final int colourIndex = list.get(index-1).getColourIndex();

            list.get(index).setColourIndex(colourIndex);
        }
        list.get(0).setColourIndex(safeIndex);

        syncRepeatThreads();
    }




    private void syncToColourIndex(int pos, int length) {
        final int colourIndex = model.getSelectedColourIndex();
        for (int index = pos; index < (pos+length); ++index) {
            list.get(index).setColourIndex(colourIndex);
        }
    }

    public void setActive(int size) {
        if (size > MAX) {
            size = MAX;
        }

        if (size > active) {
            syncToColourIndex(active, size-active);
        }

        active = size;
        syncThreads();
    }
    
    public void incActive(int count) {
        setActive(active + count);
    }

    public void deleteThreads(int pos) {
        final int COUNT = model.getThreadCount();
        final int ACTIVE = getActive();
        final int LIM1 = ((ACTIVE - COUNT) < MIN ? (ACTIVE-MIN) : COUNT);
        final int LIM2 = ((pos + COUNT) > ACTIVE ? (ACTIVE-pos) : COUNT);
        final int SIZE = (LIM1 < LIM2 ? LIM1 : LIM2);

        int source = pos + SIZE;
        for (int index = pos; source < ACTIVE; ++index) {
            final int colourIndex = list.get(source++).getColourIndex();
            list.get(index).setColourIndex(colourIndex);
        }

        active -= SIZE;
        syncThreads();
    }


    public void insertThreads(int pos) {
        final int COUNT = model.getThreadCount();
        final int ACTIVE = getActive();
        final int SIZE = ((pos + COUNT) >= MAX ? (MAX-pos-1) : COUNT);

        int source = ACTIVE-1;
        for (int index = ACTIVE+SIZE-1; source >= pos ; --index) {
            final int colourIndex = list.get(source--).getColourIndex();
            list.get(index).setColourIndex(colourIndex);
        }

        active += SIZE;
        syncToColourIndex(pos, SIZE);
        syncThreads();
    }


    /************************************************************************
     * Support code for the mouse click handler.
     */

    /**
     * Set the threads (and repeat threads) to the selected swatch colour.
     * @param pos to set the colour of.
     * @param colourIndex to set the threads to.
     * @param count of threads.
     * @param repeat start point.
     */
    public void colourThreads(int pos, int colourIndex, int count, int repeat) {
        for (int thread = 0; thread < count; ++thread) {
            list.get(pos).setColourIndex(colourIndex);

            for (int i = 0; i < MAX; i += repeat) {
                final int index = i + pos;
                if (index >= MAX)
                    break;

                list.get(index).setColourIndex(colourIndex);
            }

            if (++pos >= repeat)
                break;
        }
    }


    /************************************************************************
     * Support code for the mouse move handler.
     */

    public void clearThreads() {
        for (Thread thread : list) {
            thread.setHighlight(false, defaultColour);
        }
    }

    public void highlightThread(int pos, boolean highlight, Color colour) {
        list.get(pos).setHighlight(highlight, colour);
    }



    /************************************************************************
     * Public interface.
     */

    public void setList(ArrayList<Integer> values) {
        final int ACTIVE = values.size();

        for (int index = 0; index < MAX; ++index) {
            final int colourIndex = values.get(index % ACTIVE);

            Thread stitch = list.get(index);
            stitch.setColourIndex(colourIndex);
            stitch.setVisible(index < ACTIVE);
        }
    }

    public ArrayList<Integer> getList() {
        ArrayList<Integer> result = new ArrayList<Integer>(active);

        for (Thread thread : list) {
            if (!thread.isVisible())
                break;

            result.add(thread.getColourIndex());
        }

        return result;
    }

    /************************************************************************
     * Synchronize interface.
     */

    /**
     * Synchronise to the current swatch colour.
     */
    public void syncColour() {
        for (Thread stitch : list) {
            stitch.syncCurrentColour();
        }
    }

    /**
     * Synchronise the thread size. Sets the size and position of the 
     * stitch and the thread Rectangles.
     */
    public void syncThreadSize() {

        if (isRow()) {
            for (Thread stitch : list) {
                stitch.syncRowSize();
            }
        } else {
            for (Thread stitch : list) {
                stitch.syncColSize();
            }
        }
    }

}
