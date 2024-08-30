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

package com.t8rin.psd.reader.parser;

public enum ColorMode {
    BITMAP, // 0
    GRAYSCALE, // 1
    INDEXED, // 2
    RGB, // 3
    CMYK, // 4
    UNKNOWN_5, // 5,
    UNKNOWN_6, // 6
    MULTICHANNEL, // 7
    DUOTONE, // 8
    LAB, // 9
}
