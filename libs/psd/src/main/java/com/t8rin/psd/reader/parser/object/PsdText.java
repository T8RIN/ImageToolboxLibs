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
 * The Class PsdText.
 *
 * @author Dmitry Belsky
 */
public class PsdText extends PsdObject {

    /**
     * The value.
     */
    private final String value;

    /**
     * Instantiates a new psdreader text.
     *
     * @param stream the stream
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public PsdText(PsdInputStream stream) throws IOException {
        int textSize = stream.readInt();
        StringBuffer buffer = new StringBuffer(textSize);
        boolean stopReading = false;
        for (int ti = 0; ti < textSize; ti++) {
            char b = (char) stream.readShort();
            if (b == 0) {
                stopReading = true;
            }
            if (!stopReading) {
                if (b == 13 || b == 10) {
                    buffer.append('\n');
                } else {
                    buffer.append(b);
                }
            }
        }
        value = buffer.toString();

        logger.finest("PsdText.value: " + value);
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return value;
    }

}
