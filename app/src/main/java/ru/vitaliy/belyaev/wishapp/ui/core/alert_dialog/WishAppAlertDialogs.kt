package ru.vitaliy.belyaev.wishapp.ui.core.alert_dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogProperties
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppButtonColors

@Composable
fun DestructiveConfirmationAlertDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    confirmButtonText: @Composable () -> Unit = { Text(stringResource(R.string.delete)) },
    confirmClick: () -> Unit,
    dismissButtonText: @Composable () -> Unit = { Text(stringResource(R.string.cancel)) },
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = AlertDialogDefaults.containerColor,
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    properties: DialogProperties = DialogProperties()
) = AlertDialog(
    onDismissRequest = onDismissRequest,
    confirmButton = {
        TextButton(
            colors = WishAppButtonColors.destructiveTextButtonColors(),
            onClick = { confirmClick() }
        ) { confirmButtonText() }
    },
    modifier = modifier,
    dismissButton = {
        TextButton(
            colors = ButtonDefaults.textButtonColors(),
            onClick = { onDismissRequest() }
        ) { dismissButtonText() }
    },
    icon = icon,
    title = title,
    text = text,
    shape = shape,
    containerColor = containerColor,
    iconContentColor = iconContentColor,
    titleContentColor = titleContentColor,
    textContentColor = textContentColor,
    tonalElevation = tonalElevation,
    properties = properties
)