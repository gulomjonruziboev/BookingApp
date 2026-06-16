package uz.buron.ui.screens.contact

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import uz.buron.R
import uz.buron.ui.components.FormPhoneField
import uz.buron.ui.components.FormTextField
import uz.buron.ui.components.PrimaryButton
import uz.buron.util.Constants
import uz.buron.util.PhoneUtils
import uz.buron.util.Validation

@Composable
fun ContactScreen(snackbarHostState: SnackbarHostState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var phoneDigits by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var messageError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.contact_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ContactInfoRow(
                    label = stringResource(R.string.contact_address),
                    value = Constants.CONTACT_ADDRESS
                )
                ContactInfoRow(
                    label = stringResource(R.string.contact_phone),
                    value = Constants.CONTACT_PHONE_DISPLAY,
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Constants.CONTACT_PHONE}"))
                        context.startActivity(intent)
                    }
                )
                ContactInfoRow(
                    label = stringResource(R.string.contact_telegram),
                    value = Constants.CONTACT_TELEGRAM,
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://t.me/${Constants.CONTACT_TELEGRAM.removePrefix("@")}")
                        )
                        context.startActivity(intent)
                    }
                )
                ContactInfoRow(
                    label = stringResource(R.string.contact_email),
                    value = Constants.CONTACT_EMAIL,
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${Constants.CONTACT_EMAIL}"))
                        context.startActivity(intent)
                    }
                )
                ContactInfoRow(
                    label = stringResource(R.string.contact_hours),
                    value = Constants.CONTACT_HOURS
                )
            }
        }

        Text(
            text = stringResource(R.string.contact_form_title),
            style = MaterialTheme.typography.titleLarge
        )

        FormTextField(
            value = name,
            onValueChange = { name = it; nameError = null },
            label = stringResource(R.string.booking_name),
            error = nameError
        )
        FormPhoneField(
            digits = phoneDigits,
            onDigitsChange = { phoneDigits = it; phoneError = null },
            label = stringResource(R.string.booking_phone),
            error = phoneError
        )
        FormTextField(
            value = message,
            onValueChange = { message = it; messageError = null },
            label = stringResource(R.string.contact_message),
            error = messageError,
            singleLine = false,
            minLines = 4
        )

        PrimaryButton(
            text = stringResource(R.string.contact_send),
            onClick = {
                nameError = Validation.validateName(name, "Ism")
                val phone = PhoneUtils.normalizePhoneUz("+998$phoneDigits")
                phoneError = PhoneUtils.validatePhoneUz(phone)
                messageError = Validation.validateContactMessage(message)
                if (nameError == null && phoneError == null && messageError == null) {
                    name = ""
                    phoneDigits = ""
                    message = ""
                    scope.launch {
                        snackbarHostState.showSnackbar(context.getString(R.string.contact_success))
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ContactInfoRow(
    label: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = if (onClick != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
