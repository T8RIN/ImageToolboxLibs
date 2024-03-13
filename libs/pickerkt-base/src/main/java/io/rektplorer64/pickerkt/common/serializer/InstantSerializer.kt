package io.rektplorer64.pickerkt.common.serializer

import androidx.annotation.Keep
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.threeten.bp.Instant


/**
 * A Serializer for [kotlinx.serialization] that serializes and deserializes [Instant].
 */
@Keep
object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.LONG)

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.ofEpochMilli(decoder.decodeLong())
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeLong(value.toEpochMilli())
    }
}


@Retention(AnnotationRetention.RUNTIME)
annotation class UnixMilliTimeInstant

object UnixMilliTimeInstantJsonTypeAdapter {
    @ToJson
    fun toJson(@UnixMilliTimeInstant instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }

    @FromJson
    @UnixMilliTimeInstant
    fun fromJson(instant: Long?): Instant? {
        return instant?.let {
            Instant.ofEpochMilli(it)
        }
    }
}