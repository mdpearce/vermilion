package com.neaniesoft.vermilion.ui.videos.direct

import android.net.Uri
import android.os.Parcelable
import androidx.core.net.toUri
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Parcelize
@Serializable
data class VideoDescriptor(
    val width: @RawValue VideoWidth,
    val height: @RawValue VideoHeight,
    @Serializable(with = UriAsStringSerializer::class) val dash: Uri,
    @Serializable(with = UriAsStringSerializer::class) val hls: Uri,
    @Serializable(with = UriAsStringSerializer::class) val fallback: Uri
) : Parcelable

object UriAsStringSerializer : KSerializer<Uri> {
    override fun deserialize(decoder: Decoder): Uri {
        return decoder.decodeString().toUri()
    }

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Uri", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Uri) {
        encoder.encodeString(value.toString())
    }
}
