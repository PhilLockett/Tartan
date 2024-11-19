/*  CustomRotorController - a JavaFX based Custom Controller representing a Rotor.
 *
 *  Copyright 2024 Philip Lockett.
 *
 *  This file is part of CustomRotorController.
 *
 *  CustomRotorController is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  CustomRotorController is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CustomRotorController.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 */
package phillockett65.Tartan;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.paint.Color;

/**
 *
 * @author Phil
 */
class ColourEvent extends Event {

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

    public Color getColour() { return colour; }

    /**
     * Creates a new {@code SelectEvent} with an event type of {@code ANY}.
     * The source and target of the event is set to {@code NULL_SOURCE_TARGET}.
     */
    public ColourEvent() { super(ANY); colour = Color.BLACK; }

    public ColourEvent(EventType<? extends Event> eventType, Color c) {
        super(eventType);
        colour = c;
    }

    /**
     * Construct a new {@code SelectEvent} with the specified event source and target.
     *
     * @param source    the event source which sent the event
     * @param target    the event target to associate with the event
     */
    public ColourEvent(Object source, EventTarget target) {
        super(source, target, COLOUR_CHANGE);
        colour = Color.BLACK;
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
