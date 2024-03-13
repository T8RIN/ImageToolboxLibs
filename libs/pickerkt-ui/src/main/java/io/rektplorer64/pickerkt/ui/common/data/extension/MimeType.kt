package io.rektplorer64.pickerkt.ui.common.data.extension

import androidx.annotation.DrawableRes
import io.rektplorer64.pickerkt.contentresolver.MimeType
import io.rektplorer64.pickerkt.ui.R

val MimeType.Group.icon: Int
    @DrawableRes get() = when(this) {
        MimeType.Group.Image -> R.drawable.ic_image_outline
        MimeType.Group.Video -> R.drawable.ic_video_outline
        MimeType.Group.Audio -> R.drawable.ic_music_note_outline
        MimeType.Group.Text -> R.drawable.ic_text_box_outline
        MimeType.Group.Document -> R.drawable.ic_file_outline
        MimeType.Group.Unknown -> R.drawable.ic_help_rhombus_outline
    }

val MimeType.icon: Int
    @DrawableRes get() = when(this) {
        MimeType.Aac -> null
        MimeType.Midi -> null
        MimeType.Mp3 -> null
        MimeType.OggAudio -> null
        MimeType.Wav -> null
        MimeType.WebmAudio -> null
        MimeType.ThreeGPAudio -> null

        MimeType.Bmp -> null
        MimeType.Gif -> R.drawable.ic_file_gif_box
        MimeType.Jpeg -> R.drawable.ic_file_jpg_box
        MimeType.Png -> R.drawable.ic_file_png_box
        MimeType.Svg -> R.drawable.ic_svg

        MimeType.Webp -> null
        MimeType.Avi -> null
        MimeType.Mpeg -> null
        MimeType.Mpeg4 -> null
        MimeType.OggVideo -> null
        MimeType.WebmVideo -> null

        MimeType.MsWordDoc -> R.drawable.ic_file_word_box
        MimeType.MsWordDoc2007 -> R.drawable.ic_file_word_box
        MimeType.MsExcelSheet -> R.drawable.ic_file_excel_box
        MimeType.MsExcelSheet2007 -> R.drawable.ic_file_excel_box
        MimeType.MsPowerpointPresentation -> R.drawable.ic_file_powerpoint_box
        MimeType.MsPowerpointPresentation2007 -> R.drawable.ic_file_powerpoint_box

        MimeType.Unknown -> R.drawable.ic_help_rhombus_outline
    } ?: group.icon