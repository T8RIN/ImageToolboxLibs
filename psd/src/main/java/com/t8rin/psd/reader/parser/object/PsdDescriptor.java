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
import java.util.HashMap;
import java.util.Map;

/**
 * The Class PsdDescriptor holds meta data of a layer.
 *
 * @author Dmitry Belsky
 */
public class PsdDescriptor extends PsdObject {

    /**
     * The class id or layer type.
     */
    private final String classId;

    /**
     * a map containing all values of the descriptor
     */
    private final HashMap<String, PsdObject> objects = new HashMap<String, PsdObject>();

    /**
     * Instantiates a new psdreader descriptor.
     *
     * @param stream the stream
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public PsdDescriptor(PsdInputStream stream) throws IOException {
        // Unicode string: name from classID
        int nameLen = stream.readInt() * 2;
        stream.skipBytes(nameLen);

        classId = stream.readPsdString();
        int itemsCount = stream.readInt();
        logger.finest("PsdDescriptor.itemsCount: " + itemsCount);
        for (int i = 0; i < itemsCount; i++) {
            String key = stream.readPsdString().trim();
            logger.finest("PsdDescriptor.key: " + key);
            objects.put(key, PsdObjectFactory.loadPsdObject(stream));
        }
    }

    /**
     * Gets the class id.
     *
     * @return the class id
     */
    public String getClassId() {
        return classId;
    }

    /**
     * Gets the objects.
     *
     * @return the objects
     */
    public Map<String, PsdObject> getObjects() {
        return objects;
    }

    /**
     * Gets the.
     *
     * @param key the key
     * @return the psdreader object
     */
    public PsdObject get(String key) {
        return objects.get(key);
    }

    /**
     * Contains key.
     *
     * @param key the key
     * @return true, if successful
     */
    public boolean containsKey(String key) {
        return objects.containsKey(key);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Objc:" + objects;
    }

}