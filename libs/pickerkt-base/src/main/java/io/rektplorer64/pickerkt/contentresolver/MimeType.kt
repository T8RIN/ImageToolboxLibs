package io.rektplorer64.pickerkt.contentresolver

import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * A predefined list of commonly used MIME Types.
 * Reference 1: [W3Cub](https://docs.w3cub.com/http/basics_of_http/mime_types/complete_list_of_mime_types)
 * Reference 2: [filext](https://filext.com/faq/office_mime_types.html)
 */
@Serializable
@JsonClass(generateAdapter = false)
enum class MimeType(
    val id: String,
    val group: Group,
    val extensions: Set<String>,
    val aliases: List<String>
) {
    Aac(
        id = "audio/aac",
        group = Group.Audio,
        extensions = setOf("aac"),
        aliases = listOf("AAC")
    ),
    Midi(
        id = "audio/midi",
        group = Group.Audio,
        extensions = setOf("mid", "midi"),
        aliases = listOf("MIDI")
    ),
    Mp3(
        id = "audio/mpeg",
        group = Group.Audio,
        extensions = setOf("mp3"),
        aliases = listOf("MP3")
    ),
    OggAudio(
        id = "audio/ogg",
        group = Group.Audio,
        extensions = setOf("oga", "ogg"),
        aliases = listOf("OGG")
    ),
    Wav(
        id = "audio/wav",
        group = Group.Audio,
        extensions = setOf("wav"),
        aliases = listOf("WAV")
    ),
    WebmAudio(
        id = "audio/webm",
        group = Group.Audio,
        extensions = setOf("weba"),
        aliases = listOf("WEBM Audio")
    ),
    ThreeGPAudio(
        id = "audio/3gpp",
        group = Group.Audio,
        extensions = setOf("3gp"),
        aliases = listOf("3GP")
    ),

    Bmp(
        id = "image/bmp",
        group = Group.Image,
        extensions = setOf("bmp"),
        aliases = listOf("BMP")
    ),
    Gif(
        id = "image/gif",
        group = Group.Image,
        extensions = setOf("gif"),
        aliases = listOf("GIF")
    ),
    Jpeg(
        id = "image/jpeg",
        group = Group.Image,
        extensions = setOf("jpeg", "jpg"),
        aliases = listOf("JPEG", "JPG")
    ),
    Png(
        id = "image/png",
        group = Group.Image,
        extensions = setOf("png"),
        aliases = listOf("PNG")
    ),
    Svg(
        id = "image/svg+xml",
        group = Group.Image,
        extensions = setOf("svg"),
        aliases = listOf("SVG")
    ),
    Webp(
        id = "image/webp",
        group = Group.Image,
        extensions = setOf("webp"),
        aliases = listOf("WEBP Image")
    ),

    Avi(
        id = "video/x-msvideo",
        group = Group.Video,
        extensions = setOf("avi"),
        aliases = listOf("AVI")
    ),
    Mpeg(
        id = "video/mpeg",
        group = Group.Video,
        extensions = setOf("mpeg"),
        aliases = listOf("MPEG")
    ),
    Mpeg4(
        id = "video/mp4",
        group = Group.Video,
        extensions = setOf("mp4"),
        aliases = listOf("MP4")
    ),
    OggVideo(
        id = "video/ogg",
        group = Group.Video,
        extensions = setOf("ogv"),
        aliases = listOf("OGG")
    ),
    WebmVideo(
        id = "video/webm",
        group = Group.Video,
        extensions = setOf("webm"),
        aliases = listOf("WEBM Video")
    ),

    MsWordDoc(
        id = "application/msword",
        group = Group.Document,
        extensions = setOf("doc", "dot"),
        aliases = listOf("MS Word Document")
    ),
    MsWordDoc2007(
        id = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        group = Group.Document,
        extensions = setOf("docx", "dotx"),
        aliases = listOf("MS Word Document (New)")
    ),
    MsExcelSheet(
        id = "application/vnd.ms-excel",
        group = Group.Document,
        extensions = setOf("xls", "xlt", "xla"),
        aliases = listOf("MS Excel Sheet")
    ),
    MsExcelSheet2007(
        id = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        group = Group.Document,
        extensions = setOf("xlsx"),
        aliases = listOf("MS Excel Sheet (New)")
    ),
    MsPowerpointPresentation(
        id = "application/vnd.ms-powerpoint",
        group = Group.Document,
        extensions = setOf("ppt", "pot", "pps", "ppa"),
        aliases = listOf("MS Powerpoint Presentation")
    ),
    MsPowerpointPresentation2007(
        id = "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        group = Group.Document,
        extensions = setOf("pptx", "potx", "ppsx"),
        aliases = listOf("MS Powerpoint Presentation (New)")
    ),

    Unknown(
        id = "",
        group = Group.Unknown,
        extensions = emptySet(),
        aliases = emptyList()
    );

    val displayName: String
        get() = this.aliases.firstOrNull() ?: Unknown.name

    companion object {

        val knownValues = entries.filter { it != Unknown }

        fun of(mimeType: String): MimeType {
            return entries.firstOrNull { it.id.lowercase() == mimeType.lowercase() } ?: Unknown
        }

        fun knownValueOf(mimeType: String): MimeType {
            return entries.firstOrNull { it.name.lowercase() == mimeType.lowercase() } ?: Unknown
        }

        fun ofExtension(extension: String): MimeType {
            return entries.firstOrNull { it.extensions.contains(extension) } ?: Unknown
        }

        fun knownValuesOfGroup(group: Group) = knownValues.filter { it.group == group }
    }

    @JsonClass(generateAdapter = false)
    enum class Group {
        Image,
        Video,
        Audio,
        Text,
        Document,
        Unknown;

        fun toMediaStoreExternalUri(): Uri {
            return when (this) {
                Image -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                Video -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                Audio -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                else -> MediaStore.Files.getContentUri("external")
            }
        }
    }
}