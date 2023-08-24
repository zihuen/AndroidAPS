package info.nightscout.rx.weardata

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import info.nightscout.shared.R
import kotlinx.serialization.Serializable
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

val CUSTOM_VERSION = "0.10"

enum class CwfDrawableFileMap(val key: String, @DrawableRes val icon: Int?, val fileName: String) {
    UNKNOWN("unknown", null, "Unknown"),
    CUSTOM_WATCHFACE("customWatchface", R.drawable.watchface_custom, "CustomWatchface"),
    BACKGROUND(ViewKeys.BACKGROUND.key, R.drawable.background, "Background"),
    BACKGROUND_HIGH(ViewKeys.BACKGROUND.key, R.drawable.background, "BackgroundHigh"),
    BACKGROUND_LOW(ViewKeys.BACKGROUND.key, R.drawable.background, "BackgroundLow"),
    COVER_CHART(ViewKeys.COVER_CHART.key, null, "CoverChart"),
    COVER_PLATE(ViewKeys.COVER_PLATE.key, R.drawable.simplified_dial, "CoverPlate"),
    HOUR_HAND(ViewKeys.HOUR_HAND.key, R.drawable.hour_hand, "HourHand"),
    MINUTE_HAND(ViewKeys.MINUTE_HAND.key, R.drawable.minute_hand, "MinuteHand"),
    SECOND_HAND(ViewKeys.SECOND_HAND.key, R.drawable.second_hand, "SecondHand");

    companion object {

        fun fromKey(key: String): CwfDrawableFileMap =
            values().firstOrNull { it.key == key } ?: UNKNOWN

        fun fromFileName(file: String): CwfDrawableFileMap = values().firstOrNull { it.fileName == file.substringBeforeLast(".") } ?: UNKNOWN
    }
}

enum class DrawableFormat(val extension: String) {
    UNKNOWN(""),

    //XML("xml"),
    //SVG("svg"),
    JPG("jpg"),
    PNG("png");

    companion object {

        fun fromFileName(fileName: String): DrawableFormat =
            values().firstOrNull { it.extension == fileName.substringAfterLast(".") } ?: UNKNOWN

    }
}

@Serializable
data class DrawableData(val value: ByteArray, val format: DrawableFormat) {

    fun toDrawable(resources: Resources): Drawable? {
        try {
            return when (format) {
                DrawableFormat.PNG, DrawableFormat.JPG -> {
                    val bitmap = BitmapFactory.decodeByteArray(value, 0, value.size)
                    BitmapDrawable(resources, bitmap)
                }
                /*
                                DrawableFormat.SVG -> {
                                    //TODO: include svg to Drawable convertor here
                                    null
                                }
                                DrawableFormat.XML -> {
                                    // Always return a null Drawable, even if xml file is a valid xml vector file
                                    val xmlInputStream = ByteArrayInputStream(value)
                                    val xmlPullParser = Xml.newPullParser()
                                    xmlPullParser.setInput(xmlInputStream, null)
                                    Drawable.createFromXml(resources, xmlPullParser)
                                }
                */
                else                                   -> null
            }
        } catch (e: Exception) {
            return null
        }
    }
}

typealias CwfDrawableDataMap = MutableMap<CwfDrawableFileMap, DrawableData>
typealias CwfMetadataMap = MutableMap<CwfMetadataKey, String>

@Serializable
data class CwfData(val json: String, var metadata: CwfMetadataMap, val drawableDatas: CwfDrawableDataMap)

enum class CwfMetadataKey(val key: String, @StringRes val label: Int, val isPref: Boolean) {

    CWF_NAME("name", R.string.metadata_label_watchface_name, false),
    CWF_FILENAME("filename", R.string.metadata_wear_import_filename, false),
    CWF_AUTHOR("author", R.string.metadata_label_watchface_author, false),
    CWF_CREATED_AT("created_at", R.string.metadata_label_watchface_created_at, false),
    CWF_VERSION("cwf_version", R.string.metadata_label_plugin_version, false),
    CWF_AUTHOR_VERSION("author_version", R.string.metadata_label_watchface_name_version, false),
    CWF_COMMENT("comment", R.string.metadata_label_watchface_comment, false), // label not planed to be used for CWF_COMMENT
    CWF_AUTHORIZATION("cwf_authorization", R.string.metadata_label_watchface_authorization, false),
    CWF_PREF_WATCH_SHOW_DETAILED_IOB("key_show_detailed_iob", R.string.pref_show_detailed_iob, true),
    CWF_PREF_WATCH_SHOW_DETAILED_DELTA("key_show_detailed_delta", R.string.pref_show_detailed_delta, true),
    CWF_PREF_WATCH_SHOW_BGI("key_show_bgi", R.string.pref_show_bgi, true),
    CWF_PREF_WATCH_SHOW_IOB("key_show_iob", R.string.pref_show_iob, true),
    CWF_PREF_WATCH_SHOW_COB("key_show_cob", R.string.pref_show_cob, true),
    CWF_PREF_WATCH_SHOW_DELTA("key_show_delta", R.string.pref_show_delta, true),
    CWF_PREF_WATCH_SHOW_AVG_DELTA("key_show_avg_delta", R.string.pref_show_avgdelta, true),
    CWF_PREF_WATCH_SHOW_UPLOADER_BATTERY("key_show_uploader_battery", R.string.pref_show_phone_battery, true),
    CWF_PREF_WATCH_SHOW_RIG_BATTERY("key_show_rig_battery", R.string.pref_show_rig_battery, true),
    CWF_PREF_WATCH_SHOW_TEMP_BASAL("key_show_temp_basal", R.string.pref_show_basal_rate, true),
    CWF_PREF_WATCH_SHOW_DIRECTION("key_show_direction", R.string.pref_show_direction_arrow, true),
    CWF_PREF_WATCH_SHOW_AGO("key_show_ago", R.string.pref_show_ago, true),
    CWF_PREF_WATCH_SHOW_BG("key_show_bg", R.string.pref_show_bg, true),
    CWF_PREF_WATCH_SHOW_LOOP_STATUS("key_show_loop_status", R.string.pref_show_loop_status, true),
    CWF_PREF_WATCH_SHOW_DATE("key_show_date", R.string.pref_show_date, true);

    companion object {

        fun fromKey(key: String): CwfMetadataKey? =
            values().firstOrNull { it.key == key }
    }
}

enum class ViewKeys(val key: String, @StringRes val comment: Int?) {

    BACKGROUND("background", null),
    CHART("chart", null),
    COVER_CHART("cover_chart", null),
    FREETEXT1("freetext1", null),
    FREETEXT2("freetext2", null),
    FREETEXT3("freetext3", null),
    FREETEXT4("freetext4", null),
    IOB1("iob1", null),
    IOB2("iob2", null),
    COB1("cob1", null),
    COB2("cob2", null),
    DELTA("delta", null),
    AVG_DELTA("avg_delta", null),
    UPLOADER_BATTERY("uploader_battery", null),
    RIG_BATTERY("rig_battery", null),
    BASALRATE("basalRate", null),
    BGI("bgi", null),
    TIME("time", null),
    HOUR("hour", null),
    MINUTE("minute", null),
    SECOND("second", null),
    TIMEPERIOD("timePeriod", null),
    DAY_NAME("day_name", null),
    DAY("day", null),
    MONTH("month", null),
    LOOP("loop", null),
    DIRECTION("direction", null),
    TIMESTAMP("timestamp", null),
    SGV("sgv", null),
    COVER_PLATE("cover_plate", null),
    HOUR_HAND("hour_hand", null),
    MINUTE_HAND("minute_hand", null),
    SECOND_HAND("second_hand", null)
}

enum class JsonKeys(val key: String, val viewType: ViewType, @StringRes val comment: Int?) {
    METADATA("metadata", ViewType.NONE, null),
    ENABLESECOND("enableSecond", ViewType.NONE, null),
    HIGHCOLOR("highColor", ViewType.NONE, null),
    MIDCOLOR("midColor", ViewType.NONE, null),
    LOWCOLOR("lowColor", ViewType.NONE, null),
    LOWBATCOLOR("lowBatColor", ViewType.NONE, null),
    CARBCOLOR("carbColor", ViewType.NONE, null),
    BASALBACKGROUNDCOLOR("basalBackgroundColor", ViewType.NONE, null),
    BASALCENTERCOLOR("basalCenterColor", ViewType.NONE, null),
    GRIDCOLOR("gridColor", ViewType.NONE, null),
    POINTSIZE("pointSize", ViewType.NONE, null),
    WIDTH("width", ViewType.ALLVIEWS, null),
    HEIGHT("height", ViewType.ALLVIEWS, null),
    TOPMARGIN("topmargin", ViewType.ALLVIEWS, null),
    LEFTMARGIN("leftmargin", ViewType.ALLVIEWS, null),
    ROTATION("rotation", ViewType.TEXTVIEW, null),
    VISIBILITY("visibility", ViewType.ALLVIEWS, null),
    TEXTSIZE("textsize", ViewType.TEXTVIEW, null),
    TEXTVALUE("textvalue", ViewType.TEXTVIEW, null),
    GRAVITY("gravity", ViewType.TEXTVIEW, null),
    FONT("font", ViewType.TEXTVIEW, null),
    FONTSTYLE("fontStyle", ViewType.TEXTVIEW, null),
    FONTCOLOR("fontColor", ViewType.TEXTVIEW, null),
    COLOR("color", ViewType.IMAGEVIEW, null)
}

enum class JsonKeyValues(val key: String, val jsonKey: JsonKeys) {
    GONE("gone", JsonKeys.VISIBILITY),
    VISIBLE("visible", JsonKeys.VISIBILITY),
    INVISIBLE("invisible", JsonKeys.VISIBILITY),
    CENTER("center", JsonKeys.GRAVITY),
    LEFT("left", JsonKeys.GRAVITY),
    RIGHT("right", JsonKeys.GRAVITY),
    SANS_SERIF("sans_serif", JsonKeys.FONT),
    DEFAULT("default", JsonKeys.FONT),
    DEFAULT_BOLD("default_bold", JsonKeys.FONT),
    MONOSPACE("monospace", JsonKeys.FONT),
    SERIF("serif", JsonKeys.FONT),
    ROBOTO_CONDENSED_BOLD("roboto_condensed_bold", JsonKeys.FONT),
    ROBOTO_CONDENSED_LIGHT("roboto_condensed_light", JsonKeys.FONT),
    ROBOTO_CONDENSED_REGULAR("roboto_condensed_regular", JsonKeys.FONT),
    ROBOTO_SLAB_LIGHT("roboto_slab_light", JsonKeys.FONT),
    NORMAL("normal", JsonKeys.FONTSTYLE),
    BOLD("bold", JsonKeys.FONTSTYLE),
    BOLD_ITALIC("bold_italic", JsonKeys.FONTSTYLE),
    ITALIC("italic", JsonKeys.FONTSTYLE),
    BGCOLOR("bgColor", JsonKeys.COLOR),
    BGCOLOR1("bgColor", JsonKeys.FONTCOLOR)
}

enum class ViewType(@StringRes val comment: Int?) {
    NONE(null),
    TEXTVIEW(null),
    IMAGEVIEW(null),
    ALLVIEWS(null)
}

class ZipWatchfaceFormat {
    companion object {

        const val CWF_EXTENTION = ".zip"
        const val CWF_JSON_FILE = "CustomWatchface.json"

        fun loadCustomWatchface(cwfFile: File, authorization: Boolean): CwfData? {
            var json = JSONObject()
            var metadata: CwfMetadataMap = mutableMapOf()
            val drawableDatas: CwfDrawableDataMap = mutableMapOf()

            try {
                val zipInputStream = ZipInputStream(cwfFile.inputStream())
                var zipEntry: ZipEntry? = zipInputStream.nextEntry
                while (zipEntry != null) {
                    val entryName = zipEntry.name

                    val buffer = ByteArray(2048)
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    var count = zipInputStream.read(buffer)
                    while (count != -1) {
                        byteArrayOutputStream.write(buffer, 0, count)
                        count = zipInputStream.read(buffer)
                    }
                    zipInputStream.closeEntry()

                    if (entryName == CWF_JSON_FILE) {
                        val jsonString = byteArrayOutputStream.toByteArray().toString(Charsets.UTF_8)
                        json = JSONObject(jsonString)
                        metadata = loadMetadata(json)
                        metadata[CwfMetadataKey.CWF_FILENAME] = cwfFile.name
                        metadata[CwfMetadataKey.CWF_AUTHORIZATION] = authorization.toString()
                    } else {
                        val cwfDrawableFileMap = CwfDrawableFileMap.fromFileName(entryName)
                        val drawableFormat = DrawableFormat.fromFileName(entryName)
                        if (cwfDrawableFileMap != CwfDrawableFileMap.UNKNOWN && drawableFormat != DrawableFormat.UNKNOWN) {
                            drawableDatas[cwfDrawableFileMap] = DrawableData(byteArrayOutputStream.toByteArray(), drawableFormat)
                        }
                    }
                    zipEntry = zipInputStream.nextEntry
                }

                // Valid CWF file must contains a valid json file with a name within metadata and a custom watchface image
                if (metadata.containsKey(CwfMetadataKey.CWF_NAME) && drawableDatas.containsKey(CwfDrawableFileMap.CUSTOM_WATCHFACE))
                    return CwfData(json.toString(4), metadata, drawableDatas)
                else
                    return null

            } catch (e: Exception) {
                return null
            }
        }

        fun saveCustomWatchface(file: File, customWatchface: CwfData) {

            try {
                val outputStream = FileOutputStream(file)
                val zipOutputStream = ZipOutputStream(BufferedOutputStream(outputStream))

                // Ajouter le fichier JSON au ZIP
                val jsonEntry = ZipEntry(CWF_JSON_FILE)
                zipOutputStream.putNextEntry(jsonEntry)
                zipOutputStream.write(customWatchface.json.toByteArray())
                zipOutputStream.closeEntry()

                // Ajouter les fichiers divers au ZIP
                for (drawableData in customWatchface.drawableDatas) {
                    val fileEntry = ZipEntry("${drawableData.key.fileName}.${drawableData.value.format.extension}")
                    zipOutputStream.putNextEntry(fileEntry)
                    zipOutputStream.write(drawableData.value.value)
                    zipOutputStream.closeEntry()
                }
                zipOutputStream.close()
                outputStream.close()
            } catch (_: Exception) {
            }

        }

        fun loadMetadata(contents: JSONObject): CwfMetadataMap {
            val metadata: CwfMetadataMap = mutableMapOf()

            if (contents.has(JsonKeys.METADATA.key)) {
                val meta = contents.getJSONObject(JsonKeys.METADATA.key)
                for (key in meta.keys()) {
                    val metaKey = CwfMetadataKey.fromKey(key)
                    if (metaKey != null) {
                        metadata[metaKey] = meta.getString(key)
                    }
                }
            }
            return metadata
        }
    }

}