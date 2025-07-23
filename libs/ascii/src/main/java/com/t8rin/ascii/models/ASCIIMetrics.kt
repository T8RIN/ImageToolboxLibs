package com.t8rin.ascii.models

/**
 * Created by Muhammad Umar on 17/06/2018.
 * @author Muhammad Umar (ee_umar@yahoo.com)
 */
class ASCIIMetrics(characters: String?, luminance: Float) {
    var ascii: String? = null
    var luminance = 0f

    init {
        ascii = characters
        this.luminance = luminance
    }
}
