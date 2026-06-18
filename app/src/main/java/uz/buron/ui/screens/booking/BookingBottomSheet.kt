package uz.buron.ui.screens.booking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import uz.buron.R
import uz.buron.ui.components.FormPhoneField
import uz.buron.ui.components.FormTextField
import uz.buron.ui.components.PrimaryButton
import uz.buron.util.Constants
import uz.buron.util.DateUtils
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingBottomSheet(
    visible: Boolean,
    selectedDate: LocalDate?,
    selectedSessions: Set<String>,
    isLoggedIn: Boolean,
    guestName: String,
    guestPhoneDigits: String,
    guestNameError: String?,
    guestPhoneError: String?,
    isSubmitting: Boolean,
    bookingSuccess: Boolean,
    bookingError: String?,
    onDismiss: () -> Unit,
    onGuestNameChange: (String) -> Unit,
    onGuestPhoneChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.booking_title),
                    style = MaterialTheme.typography.headlineMedium
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Yopish")
                }
            }

            if (bookingSuccess) {
                Text(
                    text = stringResource(R.string.booking_success),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                PrimaryButton(text = stringResource(R.string.close), onClick = onDismiss)
            } else {
                selectedDate?.let { date ->
                    Text(
                        text = DateUtils.formatDisplayDate(DateUtils.toIsoDateString(date)),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    text = selectedSessions.joinToString(", ") { session ->
                        Constants.SESSION_LABELS[session] ?: session
                    },
                    style = MaterialTheme.typography.bodyMedium
                )

                if (!isLoggedIn) {
                    FormTextField(
                        value = guestName,
                        onValueChange = onGuestNameChange,
                        label = stringResource(R.string.booking_name),
                        error = guestNameError
                    )
                    FormPhoneField(
                        digits = guestPhoneDigits,
                        onDigitsChange = onGuestPhoneChange,
                        label = stringResource(R.string.booking_phone),
                        error = guestPhoneError
                    )
                }

                bookingError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                PrimaryButton(
                    text = if (isSubmitting) {
                        stringResource(R.string.booking_submitting)
                    } else {
                        stringResource(R.string.booking_submit)
                    },
                    onClick = onSubmit,
                    enabled = !isSubmitting
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
