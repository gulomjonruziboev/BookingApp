package uz.buron.util

object Validation {
    private val NAME_REGEX = Regex("""^[\u0400-\u04FFa-zA-Z\s'-]{2,50}$""")
    private val PASSWORD_REGEX = Regex("""^.{6,64}$""")
    private val SEARCH_QUERY_REGEX = Regex("""^[\u0400-\u04FFa-zA-Z0-9\s'-]{0,80}$""")

    fun validateName(value: String, label: String): String? {
        return if (NAME_REGEX.matches(value.trim())) {
            null
        } else {
            "$label: faqat harflar, 2–50 belgi"
        }
    }

    fun validatePassword(password: String): String? {
        return if (PASSWORD_REGEX.matches(password)) {
            null
        } else {
            "Parol kamida 6 belgidan iborat bo'lishi kerak"
        }
    }

    fun validateSearchQuery(query: String): String? {
        return if (SEARCH_QUERY_REGEX.matches(query)) {
            null
        } else {
            "Qidiruv so'rovi noto'g'ri"
        }
    }

    fun validateReviewComment(comment: String): String? {
        return if (comment.trim().length >= 5) {
            null
        } else {
            "Sharh kamida 5 belgi"
        }
    }

    fun validateContactMessage(message: String): String? {
        return if (message.trim().length >= 5) {
            null
        } else {
            "Xabar kamida 5 belgi"
        }
    }
}
