/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.jhlabs;

/**
 * A filter which removes noise from an image using a "pepper and salt" algorithm.
 */
public class DespeckleFilter extends WholeImageFilter {

    public DespeckleFilter() {
    }

    private short pepperAndSalt(short c, short v1, short v2) {
        if (c < v1)
            c++;
        if (c < v2)
            c++;
        if (c > v1)
            c--;
        if (c > v2)
            c--;
        return c;
    }

    public String toString() {
        return "Blur/Despeckle...";
    }

}
