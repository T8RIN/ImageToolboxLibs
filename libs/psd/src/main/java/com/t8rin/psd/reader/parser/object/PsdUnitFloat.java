/*
 * This file is part of java-psd-library.
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.t8rin.psd.reader.parser.object;

import com.t8rin.psd.reader.parser.PsdInputStream;

import java.io.IOException;

// TODO: Auto-generated Javadoc

/**
 * The Class PsdUnitFloat.
 *
 * @author Dmitry Belsky
 */
public class PsdUnitFloat extends PsdObject {

    /**
     * The unit.
     */
    private final String unit;

    /**
     * The value.
     */
    private final double value;

    /**
     * Instantiates a new psdreader unit float.
     *
     * @param stream the stream
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public PsdUnitFloat(PsdInputStream stream) throws IOException {
        unit = stream.readString(4);
        value = stream.readDouble();
        logger.finest("PsdUnitFloat.unit: " + unit + " PsdUnitFloat.value: " + value);
    }

    /**
     * Gets the unit.
     *
     * @return the unit
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public double getValue() {
        return value;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "UntF:<" + unit + ":" + value + ">";
    }

}
