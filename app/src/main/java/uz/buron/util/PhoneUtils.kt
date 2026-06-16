package uz.buron.util

object PhoneUtils {
    private val PHONE_REGEX = Regex(
        """^\+998(33|50|55|77|88|90|91|93|94|95|97|98|99)\d{7}$"""
    )

    fun normalizePhoneUz(input: String): String {
        val digits = input.filter { it.isDigit() }
        return when {
            digits.startsWith("998") && digits.length >= 12 ->
                "+${digits.take(12)}"
            digits.length == 9 ->
                "+998$digits"
            input.startsWith("+998") ->
                "+998${digits.removePrefix("998").take(9)}"
            else -> input.trim()
        }
    }

    fun validatePhoneUz(phone: String): String? {
        val normalized = normalizePhoneUz(phone)
        return if (PHONE_REGEX.matches(normalized)) {
            null
        } else {
            "Telefon: +998 va 9 ta raqam (masalan +998901234567)"
        }
    }

    fun formatPhoneInput(digits: String): String {
        val clean = digits.filter { it.isDigit() }.take(9)
        return if (clean.isEmpty()) "" else clean
    }
}
