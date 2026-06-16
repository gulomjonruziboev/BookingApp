package uz.buron.util

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlin.coroutines.resume

object RegionDetector {
    private val regionAliases = mapOf(
        "tashkent" to "Toshkent",
        "toshkent" to "Toshkent",
        "samarkand" to "Samarqand",
        "samarqand" to "Samarqand",
        "bukhara" to "Buxoro",
        "buxoro" to "Buxoro",
        "fergana" to "Farg'ona",
        "farg'ona" to "Farg'ona",
        "andijan" to "Andijon",
        "andijon" to "Andijon",
        "namangan" to "Namangan",
        "kashkadarya" to "Qashqadaryo",
        "qashqadaryo" to "Qashqadaryo",
        "surkhandarya" to "Surxondaryo",
        "surxondaryo" to "Surxondaryo",
        "jizzakh" to "Jizzax",
        "jizzax" to "Jizzax",
        "sirdaryo" to "Sirdaryo",
        "navoiy" to "Navoiy",
        "khorezm" to "Xorazm",
        "xorazm" to "Xorazm",
        "karakalpakstan" to "Qoraqalpog'iston",
        "qoraqalpog'iston" to "Qoraqalpog'iston"
    )

    @SuppressLint("MissingPermission")
    suspend fun detectRegion(context: Context): String? {
        val location = getLastLocation(context) ?: return null
        return reverseGeocode(location.first, location.second)
    }

    @SuppressLint("MissingPermission")
    private suspend fun getLastLocation(context: Context): Pair<Double, Double>? =
        suspendCancellableCoroutine { cont ->
            val client = LocationServices.getFusedLocationProviderClient(context)
            val cancellation = CancellationTokenSource()
            client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellation.token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        cont.resume(location.latitude to location.longitude)
                    } else {
                        cont.resume(null)
                    }
                }
                .addOnFailureListener {
                    cont.resume(null)
                }
        }

    private suspend fun reverseGeocode(lat: Double, lon: Double): String? =
        withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val url =
                    "https://nominatim.openstreetmap.org/reverse?lat=$lat&lon=$lon&format=json"
                val request = Request.Builder()
                    .url(url)
                    .header("Accept-Language", "en")
                    .header("User-Agent", "BuronAndroid/1.0")
                    .build()
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val json = JSONObject(body)
                val address = json.optJSONObject("address") ?: return@withContext null
                val state = address.optString("state", "")
                val region = address.optString("region", "")
                val county = address.optString("county", "")
                mapToUzbekRegion("$state $region $county")
            } catch (_: Exception) {
                null
            }
        }

    private fun mapToUzbekRegion(text: String): String? {
        val lower = text.lowercase()
        regionAliases.forEach { (alias, region) ->
            if (lower.contains(alias)) return region
        }
        Constants.UZBEK_REGIONS.forEach { region ->
            if (lower.contains(region.lowercase())) return region
        }
        return null
    }
}
