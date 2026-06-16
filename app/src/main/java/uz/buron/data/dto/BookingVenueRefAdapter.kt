package uz.buron.data.dto

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

class BookingVenueRefAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): BookingVenueSummaryDto? {
        return when (reader.peek()) {
            JsonReader.Token.STRING -> {
                BookingVenueSummaryDto(id = reader.nextString(), name = "")
            }
            JsonReader.Token.BEGIN_OBJECT -> {
                var id: String? = null
                var name = ""
                var region: String? = null
                var district: String? = null
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "_id" -> id = reader.nextString()
                        "name" -> name = reader.nextString()
                        "region" -> region = reader.nextString()
                        "district" -> district = reader.nextString()
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
                BookingVenueSummaryDto(id = id, name = name, region = region, district = district)
            }
            JsonReader.Token.NULL -> {
                reader.nextNull<Any>()
                null
            }
            else -> {
                reader.skipValue()
                null
            }
        }
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: BookingVenueSummaryDto?) {
        if (value == null) {
            writer.nullValue()
        } else {
            writer.beginObject()
            value.id?.let {
                writer.name("_id")
                writer.value(it)
            }
            writer.name("name")
            writer.value(value.name)
            writer.endObject()
        }
    }
}
