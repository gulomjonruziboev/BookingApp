package uz.buron.util

object Constants {
    val UZBEK_REGIONS = listOf(
        "Toshkent",
        "Samarqand",
        "Buxoro",
        "Farg'ona",
        "Andijon",
        "Namangan",
        "Qashqadaryo",
        "Surxondaryo",
        "Jizzax",
        "Sirdaryo",
        "Navoiy",
        "Xorazm",
        "Qoraqalpog'iston"
    )

    val SESSION_LABELS = mapOf(
        "morning" to "Nahorgi (09:00–14:00)",
        "afternoon" to "Abetgi (14:00–18:00)",
        "evening" to "Kechgi (18:00–23:00)"
    )

    val SESSION_ORDER = listOf("morning", "afternoon", "evening")

    val STATUS_LABELS = mapOf(
        "pending" to "Kutilmoqda",
        "confirmed" to "Tasdiqlangan",
        "cancelled" to "Bekor qilingan"
    )

    const val CONTACT_ADDRESS = "Toshkent sh., Yunusobod tumani"
    const val CONTACT_PHONE = "+998900000000"
    const val CONTACT_PHONE_DISPLAY = "+998 (90) 000-00-00"
    const val CONTACT_TELEGRAM = "@buron_uz"
    const val CONTACT_EMAIL = "info@buron.uz"
    const val CONTACT_HOURS = "09:00 – 22:00 (har kuni)"
}
