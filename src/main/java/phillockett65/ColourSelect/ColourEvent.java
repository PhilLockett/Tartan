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
 * ColourSelect is a class that helps select a colour.
 */
package phillockett65.ColourSelect;


import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.paint.Color;



    /************************************************************************
     * Support code for the ColourSelect Event class.
     */

public class ColourEvent extends Event {

    private static final long serialVersionUID = 202411141956L;

    /**
     * The only valid EventTypes for the SelectEvent.
     */
    public static final EventType<ColourEvent> COLOUR =
        new EventType<>(Event.ANY, "COLOUR");
    public static final EventType<ColourEvent> ANY = COLOUR;
    public static final EventType<ColourEvent> COLOUR_CHANGE =
        new EventType<>(ColourEvent.ANY, "COLOUR_CHANGE");

    private final Color colour;
    private boolean colourSelect = false;
    private boolean colourExtend = false;

    public Color getAlphaColour() { return colour; }
    public double getRed() { return colour.getRed(); }
    public double getGreen() { return colour.getGreen(); }
    public double getBlue() { return colour.getBlue(); }
    public double getOpacity() { return colour.getOpacity(); }
    public Color getColour() {
        return Color.color(getRed(), getGreen(), getBlue());
    }

    public boolean isColourSelect() { return colourSelect; }
    public boolean isColourExtend() { return colourExtend; }

    public boolean enableColourSelect() {
        if (colourExtend == true) {
            return false;
        }

        colourSelect = true;

        return true;
    }

    public boolean enableColourExtend() {
        if (colourSelect == true) {
            return false;
        }

        colourExtend = true;

        return true;
    }



    /**
     * Creates a new {@code ColourEvent} with an event type of {@code ANY}.
     * The source and target of the event is set to {@code NULL_SOURCE_TARGET}.
     */
    public ColourEvent() { super(ANY); colour = Color.BLACK; }


    /**
     * Construct a new {@code ColourEvent} with the specified event type and 
     * selected colour.
     * The source and target of the event are set to {@code NULL_SOURCE_TARGET}.
     *
     * @param eventType this event represents.
     * @param colour    selected.
     */
    public ColourEvent(EventType<? extends Event> eventType, Color colour) {
        super(eventType);
        this.colour = colour;
    }

    @Override
    public ColourEvent copyFor(Object newSource, EventTarget newTarget) {
        return (ColourEvent) super.copyFor(newSource, newTarget);
    }

    @SuppressWarnings("unchecked")
    @Override
    public EventType<? extends ColourEvent> getEventType() {
        return (EventType<? extends ColourEvent>) super.getEventType();
    }

}

