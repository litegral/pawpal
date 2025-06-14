// di package com.litegral.pawpal.akbar
package com.litegral.pawpal.akbar.model

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@IgnoreExtraProperties
@Parcelize
data class CatModel(
    var id: String = "",
    var uploaderUid: String = "",
    var uploaderName: String = "",
    var name: String = "",
    var age: String = "", // Tetap simpan string untuk ditampilkan "2 Tahun"
    var isFemale: Boolean = false,
    var breed: String = "",
    var description: String = "",
    var petPosition: String = "",
    var imageUrls: List<String> = listOf(),
    @ServerTimestamp
    var postedDate: Date? = null,

    // --- FIELD BARU UNTUK SORTING ---
    var ageInMonths: Int = 0
) : Parcelable {
    // Fungsi ini bisa Anda hapus jika tidak diperlukan
    @Exclude
    fun getAgeInMonthsForSorting(): Int {
        val ageParts = age.split(" ")
        if (ageParts.size == 2) {
            val value = ageParts[0].toIntOrNull() ?: 0
            val unit = ageParts[1].lowercase()
            return when {
                unit.contains("tahun") || unit.contains("year") -> value * 12
                unit.contains("bulan") || unit.contains("month") -> value
                else -> 0
            }
        }
        return 0
    }
}