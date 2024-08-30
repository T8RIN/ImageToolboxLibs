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
import java.util.ArrayList;
import java.util.Iterator;

// TODO: Auto-generated Javadoc

/**
 * The Class PsdList.
 *
 * @author Dmitry Belsky
 */
public class PsdList extends PsdObject implements Iterable<PsdObject> {

    /**
     * The objects.
     */
    private final ArrayList<PsdObject> objects = new ArrayList<PsdObject>();

    /**
     * Instantiates a new psdreader list.
     *
     * @param stream the stream
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public PsdList(PsdInputStream stream) throws IOException {
        int itemsCount = stream.readInt();
        logger.finest("PsdList.itemsCount: " + itemsCount);
        for (int i = 0; i < itemsCount; i++) {
            objects.add(PsdObjectFactory.loadPsdObject(stream));
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<PsdObject> iterator() {
        return objects.iterator();
    }

    /**
     * Size.
     *
     * @return the int
     */
    public int size() {
        return objects.size();
    }

    /**
     * Gets the.
     *
     * @param i the i
     * @return the psdreader object
     */
    public PsdObject get(int i) {
        return objects.get(i);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "VlLs:" + objects;
    }

}
