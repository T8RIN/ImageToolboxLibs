@file:Suppress("ConvertLongToDuration")

package com.t8rin.gmic

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.t8rin.gmic.filters.BandingDenoise
import com.t8rin.gmic.filters.BlackAndWhiteFilm
import com.t8rin.gmic.filters.BoostChromaticity
import com.t8rin.gmic.filters.BufferError
import com.t8rin.gmic.filters.ChannelProcessing
import com.t8rin.gmic.filters.CleanText
import com.t8rin.gmic.filters.ColorTemperature
import com.t8rin.gmic.filters.ConstrainedSharpen
import com.t8rin.gmic.filters.DcpDehaze
import com.t8rin.gmic.filters.DenoiseSmooth
import com.t8rin.gmic.filters.DesaturateNorm
import com.t8rin.gmic.filters.Descreen
import com.t8rin.gmic.filters.DodgeSketch
import com.t8rin.gmic.filters.DynamicContrast
import com.t8rin.gmic.filters.EqualizeLight
import com.t8rin.gmic.filters.EqualizeShadow
import com.t8rin.gmic.filters.FillHoles
import com.t8rin.gmic.filters.FishEye
import com.t8rin.gmic.filters.FreakyBlackAndWhite
import com.t8rin.gmic.filters.GradientSharpen
import com.t8rin.gmic.filters.GraduatedColorAccent
import com.t8rin.gmic.filters.Hedcut
import com.t8rin.gmic.filters.HessianSharpen
import com.t8rin.gmic.filters.HighlightSynthesis
import com.t8rin.gmic.filters.HoughSketch
import com.t8rin.gmic.filters.HslAdjustment
import com.t8rin.gmic.filters.HsvEqualizer
import com.t8rin.gmic.filters.LensDistortion
import com.t8rin.gmic.filters.LightGlow
import com.t8rin.gmic.filters.LocalContrastEnhancement
import com.t8rin.gmic.filters.LocalVarianceNormalization
import com.t8rin.gmic.filters.MetallicLook
import com.t8rin.gmic.filters.OldPhotograph
import com.t8rin.gmic.filters.PenDrawing
import com.t8rin.gmic.filters.PolarTransform
import com.t8rin.gmic.filters.PolaroidFrame
import com.t8rin.gmic.filters.PosterizedDithering
import com.t8rin.gmic.filters.PowerTwirl
import com.t8rin.gmic.filters.RandomDeformations
import com.t8rin.gmic.filters.RemoveHotPixels
import com.t8rin.gmic.filters.RemoveScratches
import com.t8rin.gmic.filters.RetroFade
import com.t8rin.gmic.filters.RowShifter
import com.t8rin.gmic.filters.SaturationEqualizer
import com.t8rin.gmic.filters.SelectiveDesaturation
import com.t8rin.gmic.filters.ShockWaves
import com.t8rin.gmic.filters.SixtiesCinema
import com.t8rin.gmic.filters.SpecificSaturation
import com.t8rin.gmic.filters.TargetColorSpot
import com.t8rin.gmic.filters.TemperatureBalance
import com.t8rin.gmic.filters.TextureSharpen
import com.t8rin.gmic.filters.ToneEnhance
import com.t8rin.gmic.filters.ToneSharpen
import com.t8rin.gmic.filters.Unpurple
import com.t8rin.gmic.filters.Unstrip
import com.t8rin.gmic.filters.Warhol
import com.t8rin.gmic.filters.WhitenSharpen
import com.t8rin.gmic.filters.ZoneSystem
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NewGmicFiltersInstrumentedTest {

    @Test
    fun defaultFiltersExecute() = runBlocking {
        val input = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888).apply {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    setPixel(x, y, Color.rgb(x * 10, y * 10, (x + y) * 5))
                }
            }
        }
        val filters: List<GmicFilter> = listOf(
            Unpurple(),
            Unstrip(),
            BandingDenoise(),
            DcpDehaze(),
            HessianSharpen(),
            WhitenSharpen(),
            HoughSketch(),
            Warhol(),
            Descreen(),
            DenoiseSmooth(),
            CleanText(),
            FillHoles(),
            RemoveScratches(),
            RemoveHotPixels(),
            TextureSharpen(),
            PolaroidFrame(),
            PenDrawing(),
            OldPhotograph(),
            RetroFade(),
            SixtiesCinema(),
            MetallicLook(),
            ZoneSystem(),
            BufferError(),
            FreakyBlackAndWhite(),
            DodgeSketch(),
            PosterizedDithering(),
            Hedcut(),
            LensDistortion(),
            PowerTwirl(),
            RandomDeformations(),
            ShockWaves(),
            RowShifter(),
            PolarTransform(),
            FishEye(),
            BoostChromaticity(),
            EqualizeLight(),
            EqualizeShadow(),
            LightGlow(),
            ColorTemperature(),
            TemperatureBalance(),
            SelectiveDesaturation(),
            GraduatedColorAccent(),
            HslAdjustment(),
            SpecificSaturation(),
            ChannelProcessing(),
            HsvEqualizer(),
            TargetColorSpot(),
            BlackAndWhiteFilm(),
            LocalContrastEnhancement(),
            LocalVarianceNormalization(),
            GradientSharpen(),
            ToneSharpen(),
            ConstrainedSharpen(),
            HighlightSynthesis(),
            DesaturateNorm(),
            SaturationEqualizer(),
            DynamicContrast(),
            ToneEnhance()
        )

        filters.forEach { filter ->
            val output = runCatching {
                withTimeout(60_000) { Gmic.runCancellable(input, filter) }
            }.getOrElse { error ->
                throw AssertionError("${filter.javaClass.simpleName} failed", error)
            }
            assertTrue(filter.javaClass.simpleName, output.width > 0 && output.height > 0)
            output.recycle()
        }
        input.recycle()
    }
}
