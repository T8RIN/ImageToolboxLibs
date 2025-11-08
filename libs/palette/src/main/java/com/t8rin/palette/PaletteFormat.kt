package com.t8rin.palette

import com.t8rin.palette.coders.ACBPaletteCoder
import com.t8rin.palette.coders.ACOPaletteCoder
import com.t8rin.palette.coders.ACTPaletteCoder
import com.t8rin.palette.coders.AFPaletteCoder
import com.t8rin.palette.coders.ASEPaletteCoder
import com.t8rin.palette.coders.AndroidColorsXMLCoder
import com.t8rin.palette.coders.AutodeskColorBookCoder
import com.t8rin.palette.coders.BasicXMLCoder
import com.t8rin.palette.coders.CLFPaletteCoder
import com.t8rin.palette.coders.CPLPaletteCoder
import com.t8rin.palette.coders.CSVPaletteCoder
import com.t8rin.palette.coders.CorelDraw3PaletteCoder
import com.t8rin.palette.coders.CorelPainterCoder
import com.t8rin.palette.coders.CorelXMLPaletteCoder
import com.t8rin.palette.coders.DCPPaletteCoder
import com.t8rin.palette.coders.GIMPPaletteCoder
import com.t8rin.palette.coders.HEXPaletteCoder
import com.t8rin.palette.coders.HPLPaletteCoder
import com.t8rin.palette.coders.ImagePaletteCoder
import com.t8rin.palette.coders.JCWPaletteCoder
import com.t8rin.palette.coders.JSONPaletteCoder
import com.t8rin.palette.coders.KOfficePaletteCoder
import com.t8rin.palette.coders.KotlinPaletteCoder
import com.t8rin.palette.coders.OpenOfficePaletteCoder
import com.t8rin.palette.coders.PaintNETPaletteCoder
import com.t8rin.palette.coders.PaintShopProPaletteCoder
import com.t8rin.palette.coders.ProcreateSwatchesCoder
import com.t8rin.palette.coders.RGBAPaletteCoder
import com.t8rin.palette.coders.RGBPaletteCoder
import com.t8rin.palette.coders.RIFFPaletteCoder
import com.t8rin.palette.coders.SKPPaletteCoder
import com.t8rin.palette.coders.SVGPaletteCoder
import com.t8rin.palette.coders.ScribusXMLPaletteCoder
import com.t8rin.palette.coders.SimplePaletteCoder
import com.t8rin.palette.coders.SkencilPaletteCoder
import com.t8rin.palette.coders.SketchPaletteCoder
import com.t8rin.palette.coders.SwatchbookerCoder
import com.t8rin.palette.coders.SwiftPaletteCoder
import com.t8rin.palette.coders.VGA18BitPaletteCoder
import com.t8rin.palette.coders.VGA24BitPaletteCoder

/**
 * Supported palette formats
 */
enum class PaletteFormat(val fileExtension: List<String>) {
    ACB(listOf("acb")),                // Adobe Color Book
    ACO(listOf("aco")),                // Adobe Photoshop Swatch
    ACT(listOf("act")),                // Adobe Color Tables
    ANDROID_XML(listOf("xml")),        // Android XML Palette file
    ASE(listOf("ase")),                // Adobe Swatch Exchange
    BASIC_XML(listOf("xml")),          // Basic XML palette format
    COREL_PAINTER(listOf("txt")),      // Corel Painter Swatches
    COREL_DRAW(listOf("xml")),         // CorelDraw XML
    SCRIBUS_XML(listOf("xml")),        // Scribus XML swatches
    COREL_PALETTE(listOf("cpl")),      // Corel Palette
    CSV(listOf("csv")),                // CSV Palette
    DCP(listOf("dcp")),                // ColorPaletteCodable binary format
    GIMP(listOf("gpl")),               // GIMP gpl format
    HEX_RGBA(listOf("hex")),           // Hex RGBA coded files
    IMAGE(listOf("png", "jpg", "jpeg")), // image-based palette coder
    JSON(listOf("jsoncolorpalette", "json")), // ColorPaletteCodable JSON format
    OPEN_OFFICE(listOf("soc")),        // OpenOffice palette format (.soc)
    PAINT_NET(listOf("txt")),         // Paint.NET palette file (.txt)
    PAINT_SHOP_PRO(listOf("psppalette", "pal")), // Paint Shop Pro palette (.pal, .psppalette)
    RGBA(listOf("rgba", "txt")),       // RGBA encoded text files (.rgba, .txt)
    RGB(listOf("rgb", "txt")),         // RGB encoded text files (.rgb, .txt)
    RIFF(listOf("pal")),               // Microsoft RIFF palette (.pal)
    SKETCH(listOf("sketchpalette")),   // Sketch palette file (.sketchpalette)
    SKP(listOf("skp")),                // SKP Palette
    SVG(listOf("svg")),                // Scalable Vector Graphics palette (.svg)
    SWIFT(listOf("swift")),            // (export only) Swift source file (.swift)
    KOTLIN(listOf("kt")),              // (export only) Kotlin/Jetpack Compose source file (.kt)
    COREL_DRAW_V3(listOf("pal")),      // Corel Draw V3 file (.pal)
    CLF(listOf("clf")),                // LAB colors
    SWATCHES(listOf("swatches")),      // Procreate swatches
    AUTODESK_COLOR_BOOK(listOf("acb")), // Autodesk Color Book (unencrypted only) (.acb)
    SIMPLE_PALETTE(listOf("color-palette")), // Simple Palette format
    SWATCHBOOKER(listOf("sbz")),       // Swatchbooker .sbz file
    AFPALETTE(listOf("afpalette")),    // Affinity Designer .afpalette file
    XARA(listOf("jcw")),               // Xara palette file (.jcw)
    KOFFICE(listOf("colors")),         // KOffice palette file (.colors)
    HPL(listOf("hpl")),                // Homesite Palette file (.hpl)
    SKENCIL(listOf("spl")),            // Skencil Palette file (.spl)
    VGA_24BIT(listOf("vga24")),        // 24-bit RGB VGA (3 bytes RRGGBB)
    VGA_18BIT(listOf("vga18"));        // 18-bit RGB VGA (3 bytes RRGGBB)

    companion object {
        /**
         * Formats that support both decode and encode
         */
        val formatsWithDecodeAndEncode: List<PaletteFormat> = listOf(
            ACB,        // Adobe Color Book
            ACO,
            ACT,
            ANDROID_XML,
            ASE,
            BASIC_XML,
            CLF,        // LAB colors
            COREL_PAINTER,
            COREL_DRAW,
            SCRIBUS_XML,
            COREL_PALETTE, // Corel Palette
            CSV,
            DCP,
            GIMP,
            HEX_RGBA,
            IMAGE,
            JSON,
            OPEN_OFFICE,
            PAINT_NET,
            PAINT_SHOP_PRO,
            RGBA,
            RGB,
            RIFF,       // Microsoft RIFF palette
            SKETCH,
            SKP,
            COREL_DRAW_V3,
            SWATCHES,
            AUTODESK_COLOR_BOOK,
            SIMPLE_PALETTE,
            SWATCHBOOKER, // Swatchbooker .sbz file
            AFPALETTE,  // Affinity Designer .afpalette file
            XARA,
            KOFFICE,
            HPL,
            SKENCIL,
            VGA_24BIT,
            VGA_18BIT
        )

        /**
         * Formats that support only encode (write-only)
         */
        val formatsEncodeOnly: List<PaletteFormat> = listOf(
            SVG,        // Scalable Vector Graphics palette
            SWIFT,      // Swift source file
            KOTLIN      // Kotlin/Jetpack Compose source file
        )
    }
}

/**
 * Get coder for format
 */
fun PaletteFormat.getCoder(): PaletteCoder {
    return when (this) {
        PaletteFormat.ACB -> ACBPaletteCoder()
        PaletteFormat.ACO -> ACOPaletteCoder()
        PaletteFormat.ACT -> ACTPaletteCoder()
        PaletteFormat.ANDROID_XML -> AndroidColorsXMLCoder()
        PaletteFormat.ASE -> ASEPaletteCoder()
        PaletteFormat.BASIC_XML -> BasicXMLCoder()
        PaletteFormat.COREL_PAINTER -> CorelPainterCoder()
        PaletteFormat.COREL_DRAW -> CorelXMLPaletteCoder()
        PaletteFormat.SCRIBUS_XML -> ScribusXMLPaletteCoder()
        PaletteFormat.COREL_PALETTE -> CPLPaletteCoder()
        PaletteFormat.CSV -> CSVPaletteCoder()
        PaletteFormat.DCP -> DCPPaletteCoder()
        PaletteFormat.GIMP -> GIMPPaletteCoder()
        PaletteFormat.HEX_RGBA -> HEXPaletteCoder()
        PaletteFormat.IMAGE -> ImagePaletteCoder()
        PaletteFormat.JSON -> JSONPaletteCoder()
        PaletteFormat.OPEN_OFFICE -> OpenOfficePaletteCoder()
        PaletteFormat.PAINT_NET -> PaintNETPaletteCoder()
        PaletteFormat.PAINT_SHOP_PRO -> PaintShopProPaletteCoder()
        PaletteFormat.RGBA -> RGBAPaletteCoder()
        PaletteFormat.RGB -> RGBPaletteCoder()
        PaletteFormat.RIFF -> RIFFPaletteCoder()
        PaletteFormat.SKETCH -> SketchPaletteCoder()
        PaletteFormat.SVG -> SVGPaletteCoder()
        PaletteFormat.SKP -> SKPPaletteCoder()
        PaletteFormat.SWIFT -> SwiftPaletteCoder()
        PaletteFormat.KOTLIN -> KotlinPaletteCoder()
        PaletteFormat.COREL_DRAW_V3 -> CorelDraw3PaletteCoder()
        PaletteFormat.CLF -> CLFPaletteCoder()
        PaletteFormat.SWATCHES -> ProcreateSwatchesCoder()
        PaletteFormat.AUTODESK_COLOR_BOOK -> AutodeskColorBookCoder()
        PaletteFormat.SIMPLE_PALETTE -> SimplePaletteCoder()
        PaletteFormat.SWATCHBOOKER -> SwatchbookerCoder()
        PaletteFormat.AFPALETTE -> AFPaletteCoder()
        PaletteFormat.XARA -> JCWPaletteCoder()
        PaletteFormat.KOFFICE -> KOfficePaletteCoder()
        PaletteFormat.HPL -> HPLPaletteCoder()
        PaletteFormat.SKENCIL -> SkencilPaletteCoder()
        PaletteFormat.VGA_24BIT -> VGA24BitPaletteCoder()
        PaletteFormat.VGA_18BIT -> VGA18BitPaletteCoder()
    }
}


/**
 * Get format from file extension
 * Searches through all enum entries to find matching file extension
 */
fun String.toPaletteFormat(): PaletteFormat? {
    val ext = lowercase().removePrefix(".")

    // Search through all enum entries for matching extension
    return PaletteFormat.entries.firstOrNull { format ->
        format.fileExtension.contains(ext)
    }
}


