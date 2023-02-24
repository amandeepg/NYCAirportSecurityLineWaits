package ca.amandeep.nycairportsecuritylinewaits.data

import android.annotation.SuppressLint
import ca.amandeep.nycairportsecuritylinewaits.data.model.Queue
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

interface AirportApiService {

    @Headers("Referer: https://www.jfkairport.com/")
    @GET("SecurityWaitTimesPoints/{airport}")
    suspend fun getWaitTimes(@Path("airport") airport: String): List<Queue>

    companion object {
        private const val API_PATH = "https://avi-prod-mpp-webapp-api.azurewebsites.net/api/v1/"

        val INSTANCE: AirportApiService by lazy {
            Retrofit.Builder()
                .baseUrl(API_PATH)
                .addConverterFactory(
                    MoshiConverterFactory.create(
                        Moshi.Builder()
                            .add(Date::class.java, DateAdapter())
                            .build()
                    )
                )
                .build()
                .create(AirportApiService::class.java)
        }
    }
}

private class DateAdapter : JsonAdapter<Date>() {
    companion object {
        @SuppressLint("SimpleDateFormat")
        private val FORMATTER = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
    }

    @Throws(IOException::class)
    override fun fromJson(reader: JsonReader): Date? =
        FORMATTER.parse(reader.nextString().split(".")[0] + "Z")

    override fun toJson(writer: JsonWriter, value: Date?) {
    }
}

enum class AirportCode(
    val shortCode: String,
    val shortName: String,
    val fullName: String
) {
    EWR("EWR", "Newark", "Newark Liberty International Airport"),
    JFK("JFK", "Kennedy", "John F. Kennedy International Airport"),
    LGA("LGA", "LaGuardia", "LaGuardia Airport"),
    SWF("SWF", "Stewart", "New York Stewart International Airport")
}
