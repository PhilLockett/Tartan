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
 * CardSample is a class that is responsible for creating the Stage, drawing 
 * and refreshing the card.
 */
package phillockett65.Tartan;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;


public class Sample extends Stage {
    
    private Model model;

    private Scene scene;
    private Group group;

    private double dx;	// Difference between the size of the stage and the size of the scene.
    private double dy;

    private ObservableList<Thread> colList = FXCollections.observableArrayList();
    private ObservableList<Thread> rowList = FXCollections.observableArrayList();
    private ObservableList<Line> guides = FXCollections.observableArrayList();

    private final Color defaultColour;


    /************************************************************************
     * General support code.
     */

    private static int OFFSET = 30;

    private double colToXPos(int col) {
        final double size = model.getThreadSize();
        return OFFSET + (col * size);
    }

    private double rowToYPos(int row) {
        final double size = model.getThreadSize();
        return OFFSET + (row * size);
    }

    private int xPosToCol(double x) {
        final double size = model.getThreadSize();
        return (int)((x - OFFSET) / size);
    }

    private int yPosToRow(double y) {
        final double size = model.getThreadSize();
        return (int)((y - OFFSET) / size);
    }


    /**
     * Class to represent a single thread as a sequence of rectangles.
     */
    private class Thread {
        private int index;
        private int colour;
        private Rectangle stitch;
        private ObservableList<Rectangle> stitchList = FXCollections.observableArrayList();

        /**
         * The Constructor is singularly responsible for adding the Rectangles
         * to the Group.
         * @param index of the row or column.
         * @param colour index for the Thread.
         */
        public Thread(int index, int colour) {
            this.index = index;
            this.colour = colour;
            ObservableList<Node> items = group.getChildren();

            final Color color = model.getSwatchColour(colour);
            stitch = new Rectangle();
            stitch.setFill(color);
            stitch.setStroke(defaultColour);

            items.add(stitch);

            final int count = Default.WIDTH.getInt() / 4;
            for (int i = 0; i < count; ++i) {
                Rectangle stitch = new Rectangle();
                stitch.setFill(color);
                stitch.setStroke(defaultColour);

                items.add(stitch);
                stitchList.add(stitch);
            }
        }

        /**
         * Set the colour of the thread to the given swatch colour.
         * @param colourIndex of the selected swatch.
         */
        public void setColour(int colourIndex) {
            final Color color = model.getSwatchColour(colourIndex);
            colour = colourIndex;
            stitch.setFill(color);
            for (Rectangle stitch : stitchList)
                stitch.setFill(color);
        }

        /**
         * Synchronise the colour of the thread to the updated swatch colour.
         * @return true if this thread needed to be updated, false otherwise.
         */
        public boolean syncCurrentColour() {
            if (colour != model.getSelectedColour())
                return false;

            final Color color = model.getCurrentColour();

            stitch.setFill(color);
            for (Rectangle stitch : stitchList)
                stitch.setFill(color);

            return true;
        }

        /**
         * Synchronise the thickness of the thread.
         */
        public void syncBorderThickness() {
            final double thickness = model.getBorderThickness();

            stitch.setStrokeWidth(thickness);
            for (Rectangle stitch : stitchList)
                stitch.setStrokeWidth(thickness);

        }

        /**
         * Set the visibilty of the lead thread.
         * @param visible 
         */
        public void setVisible(boolean visible) {
            stitch.setVisible(visible);
        }

        /**
         * Synchronise the thread size for a row. Sets the size and position of
         * the Rectangles.
         */
        public void syncRowSize() {
            final double size = model.getThreadSize();
            final double size2 = size * 2;
            final double yPos = rowToYPos(index);

            stitch.setWidth(Default.BORDER_WIDTH.getFloat() + size);
            stitch.setHeight(size);
            stitch.setX(0D);
            stitch.setY(yPos);
            stitch.setVisible(index < model.getRowCount());

            int c = index % 4;
            c = (4 - c) % 4;
            for (Rectangle stitch : stitchList) {
                stitch.setWidth(size2);
                stitch.setHeight(size);
                stitch.setX(colToXPos(c));
                stitch.setY(yPos);

                c += 4;
            }
        }

        /**
         * Synchronise the thread size for a column. Sets the size and position
         * of the Rectangles.
         */
        public void syncColSize() {
            final double size = model.getThreadSize();
            final double size2 = size * 2;
            final double xPos = colToXPos(index);

            stitch.setWidth(size);
            stitch.setHeight(Default.BORDER_WIDTH.getFloat() + size);
            stitch.setX(xPos);
            stitch.setY(0D);
            stitch.setVisible(index < model.getColumnCount());

            int r = index % 4;
            r = (6 - r) % 4;
            for (Rectangle stitch : stitchList) {
                stitch.setWidth(size);
                stitch.setHeight(size2);
                stitch.setX(xPos);
                stitch.setY(rowToYPos(r));

                r += 4;
            }
        }

    }



    /************************************************************************
     * Support code for the Initialization of the Sample.
     */

    /**
     * Constructor.
     * 
     * @param mainModel         - used to call the centralized data model.
     * @param title             - string displayed as the heading of the Stage.
     */
    public Sample(Model mainModel, String title) {
		// System.out.println("CardSample constructed: " + title);

        // resizableProperty().setValue(false);
        setOnCloseRequest(e -> Platform.exit());
        // initStyle(StageStyle.TRANSPARENT);

        model = mainModel;
        defaultColour = model.getBorderColour();
        group = new Group();

        this.setTitle(title);
        initializeCardSample();

        this.show();
    }

    private int getZone(double x, double y) {
        x -= OFFSET;
        y -= OFFSET;

        if ((x < 0) && (y < 0)) {
            return 0;
        }

        if (x >= model.getSwatchWidth()) {
            return 0;
        }

        if (y >= model.getSwatchHeight()) {
            return 0;
        }

        if (x < 0) {
            return 1;
        }

        if (y < 0) {
            return 2;
        }

        return 3;
    }

    private boolean isRow(double x, double y) {
        final int sum = (int)(x + y) - (2 * OFFSET);
        final double size = model.getThreadSize();
        final int div = (int)(size * 4);
        final int comp = (int)(size * 2);

        if (sum % div < comp) {
            return true;
        }

        return false;
    }

    /**
     * Initializes the stage and adds handlers to the scene.
     */
    private void initializeCardSample() {

        final double WIDTH = Default.MPC_WIDTH.getFloat();
        final double HEIGHT = Default.MPC_HEIGHT.getFloat();
        // System.out.println("initializeCardSample(" + WIDTH + ", " + HEIGHT + ")");

        scene = new Scene(group, WIDTH, HEIGHT, Color.SILVER);
        drawBlankLoom();
        syncGuidePosition();

        this.setScene(scene);
        this.setX(0);
        this.setY(0);

        dx = this.getWidth() - WIDTH;
        dy = this.getHeight() - HEIGHT;

        this.setMinWidth(Default.MIN_WIDTH.getFloat() + dx);
        this.setMinHeight(Default.MIN_HEIGHT.getFloat() + dy);
        this.setMaxWidth(Default.MAX_WIDTH.getFloat() + dx);
        this.setMaxHeight(Default.MAX_HEIGHT.getFloat() + dy);

        scene.setOnMouseMoved(event -> {
            final double x = event.getSceneX();
            final double y = event.getSceneY();
            final int zone = getZone(x, y);

            if (zone == 0) {
                scene.setCursor(Cursor.DEFAULT);
            } else {
                scene.setCursor(Cursor.CROSSHAIR);
            }
        });

        scene.setOnMouseExited(event -> {
            scene.setCursor(Cursor.DEFAULT);
        });

        scene.setOnMouseClicked(event -> {
            final double x = event.getSceneX();
            final double y = event.getSceneY();
            final int zone = getZone(x, y);
            final int colour = model.getSelectedColour();

            if (zone == 0) {
            } else if (zone == 1) {
                setRowColour(yPosToRow(y), colour);
            } else if (zone == 2) {
                setColColour(xPosToCol(x), colour);
            } else {
                if (isRow(x, y)) {
                    setRowColour(yPosToRow(y), colour);
                } else {
                    setColColour(xPosToCol(x), colour);
                }
            }

        });

    }

    /**
     * Called on initialization to set up the blank loom.
     */
    private void drawBlankLoom() {
        final int RCOUNT = model.getRowCount();
        final int ROWS = Default.HEIGHT.getInt();
        for (int row = 0; row < ROWS; ++row) {
            Thread thread = new Thread(row, model.getRowColourIndex(row % RCOUNT));
            rowList.add(thread);
        }

        final int CCOUNT = model.getColumnCount();
        final int COLS = Default.WIDTH.getInt();
        for (int col = 0; col < COLS; ++col) {
            Thread thread = new Thread(col, model.getColColourIndex(col % CCOUNT));
            colList.add(thread);
        }

        ObservableList<Node> items =  group.getChildren();
        final int GUIDES = Default.TOTAL_GUIDE_COUNT.getInt();
        for (int index = 0; index < GUIDES; ++index) {
            Line guide = new Line(0.0, 0.0, 0.0, 0.0);
            guide.setStroke(Color.BLUE);
            guides.add(guide);
            items.add(guide);
        }

        syncGuideLineColour();
        syncThreadSize();
    }

    /**
     * Initialization after the model has been initialised.
     */
    public void init() {
        for (int i = 0; i < model.getRowCount(); ++i)
            rowList.get(i).setColour(model.getRowColourIndex(i));

        for (int i = 0; i < model.getColumnCount(); ++i)
            colList.get(i).setColour(model.getColColourIndex(i));
    }



    /************************************************************************
     * Support code for the mouse click handler.
     */

    private void colourRows(int row, int colour, int count, int repeat) {
        for (int c = 0; c < count; ++c) {
            model.setRowColourIndex(row, colour);

            for (int i = 0; i < Default.HEIGHT.getInt(); i += repeat) {
                final int index = i + row;
                if (index >= Default.HEIGHT.getInt())
                    break;
                
                rowList.get(index).setColour(colour);
            }

            if (++row >= repeat)
                break;
        }
    }

    private void colourColumns(int col, int colour, int count, int repeat) {
        for (int c = 0; c < count; ++c) {
            model.setColColourIndex(col, colour);

            for (int i = 0; i < Default.WIDTH.getInt(); i += repeat) {
                final int index = i + col;
                if (index >= Default.WIDTH.getInt())
                    break;
                
                colList.get(index).setColour(colour);
            }

            if (++col >= repeat)
                break;
        }
    }

    /**
     * Set the row (and repeat rows) to the selected swatch colour.
     * @param row to set the colour of.
     * @param colour index to set the row to.
     */
    private void setRowColour(int row, int colour) {
        final int count = model.getThreadCount();
        final int repeat = model.getRowCount();

        colourRows(row, colour, count, repeat);

        if (model.isDuplicate()) {
            colourColumns(row, colour, count, repeat);
        }
    }

    /**
     * Set the column (and repeat columns) to the selected swatch colour.
     * @param col to set the colour of.
     * @param colour index to set the column to.
     */
    private void setColColour(int col, int colour) {
        final int count = model.getThreadCount();
        final int repeat = model.getColumnCount();

        colourColumns(col, colour, count, repeat);

        if (model.isDuplicate()) {
            colourRows(col, colour, count, repeat);
        }
    }



    /************************************************************************
     * Synchronize interface.
     */

    /**
     * Synchronise to the current swatch colour.
     */
    public void syncColour() {
        for (Thread stitch : rowList) {
            stitch.syncCurrentColour();
        }
        for (Thread stitch : colList) {
            stitch.syncCurrentColour();
        }
    }

    /**
     * Synchronise to the thread size.
     */
    public void syncThreadSize() {
        for (Thread stitch : rowList) {
            stitch.syncRowSize();
            stitch.syncBorderThickness();
        }

        for (Thread stitch : colList) {
            stitch.syncColSize();
            stitch.syncBorderThickness();
        }

        syncGuidePosition();
    }

    /**
     * Synchronise to the row repeat count.
     */
    public void syncRowCount() {
        final int COUNT = model.getRowCount();
        final int ROWS = Default.HEIGHT.getInt();
        int index = 0;
        boolean visible = true;

        for (int row = 0; row < ROWS; ++row) {
            Thread stitch = rowList.get(row);
            stitch.setColour(model.getRowColourIndex(index));
            stitch.setVisible(visible);
            if (++index >= COUNT) {
                visible = false;
                index = 0;
            }
        }

        syncGuidePosition();
    }

    /**
     * Synchronise to the column repeat count.
     */
    public void syncColumnCount() {
        final int COUNT = model.getColumnCount();
        final int COLS = Default.WIDTH.getInt();
        int index = 0;
        boolean visible = true;

        for (int col = 0; col < COLS; ++col) {
            Thread stitch = colList.get(col);
            stitch.setColour(model.getColColourIndex(index));
            stitch.setVisible(visible);
            if (++index >= COUNT) {
                visible = false;
                index = 0;
            }
        }

        syncGuidePosition();
    }

    /**
     * Convienience method to synchronise to both the row and column repeat 
     * counts.
     */
    public void syncCount() {
        syncRowCount();
        syncColumnCount();
    }

    /**
     * Synchronize to duplicate the column threads for the rows.
     * Assumes that the row count has been set to the column count.
     */
    public void syncDuplicateThreads() {
        final int COUNT = model.getColumnCount();

        for (int column = 0; column < COUNT; ++column) {
            Thread target = rowList.get(column);
            final int colourIndex = model.getColColourIndex(column);
            target.setColour(colourIndex);
            target.setVisible(true);
            model.setRowColourIndex(column, colourIndex);
        }
    }

    /**
     * Synchronise the positions of the guide lines to the thread size and 
     * thread counts.
     */
    private void syncGuidePosition() {
        final double SIZE = model.getThreadSize();

        final double CSIZE = SIZE * model.getColumnCount();
        final double RSIZE = SIZE * model.getRowCount();

        final double CSTEP = CSIZE / (Default.GUIDE_COUNT.getFloat() + 1);
        final double RSTEP = RSIZE / (Default.GUIDE_COUNT.getFloat() + 1);

        final double CLENGTH = OFFSET + RSIZE;
        final double RLENGTH = OFFSET + CSIZE;

        double xPos = OFFSET + CSTEP;
        double yPos = OFFSET + RSTEP;
        for (int i = 0; i < Default.GUIDE_COUNT.getInt(); ++i) {
            Line guide = guides.get(i);

            guide.setStartX(xPos);
            guide.setStartY(OFFSET);
            guide.setEndX(xPos);
            guide.setEndY(CLENGTH);

            guide = guides.get(i + Default.GUIDE_COUNT.getInt());

            guide.setStartX(OFFSET);
            guide.setStartY(yPos);
            guide.setEndX(RLENGTH);
            guide.setEndY(yPos);

            xPos += CSTEP;
            yPos += RSTEP;
        }
    }

    /**
     * Synchronize the displaying of the guide lines with the model.
     */
    public void syncGuideVisible() {
        final boolean show = model.isShowGuide();
        for (Line guide : guides) {
            guide.setVisible(show);
        }
    }

    /**
     * Synchronise to the guide line colour.
     */
    public void syncGuideLineColour() {
        final Color colour = model.getGuideLineColour();
        for (Line guide : guides) {
            guide.setStroke(colour);
        }
    }

}
