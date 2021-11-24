package ru.vitaliy.belyaev.wishapp.ui.screens.settings

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar

@Composable
fun SettingsScreen(onBackPressed: () -> Unit) {

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            WishAppTopBar(
                stringResource(R.string.settings),
                withBackIcon = true,
                onBackPressed = onBackPressed
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        val scrollState = rememberScrollState()
        val context = LocalContext.current

        val licensesTitle = stringResource(R.string.licenses)
        val onLicensesClick: () -> Unit = {
            OssLicensesMenuActivity.setActivityTitle(licensesTitle)
            val intent = Intent(context, OssLicensesMenuActivity::class.java)
            ContextCompat.startActivity(context, intent, null)
        }

        Column(
            modifier = Modifier.verticalScroll(scrollState)
        ) {
            SettingBlock(
                title = stringResource(R.string.theme),
                onClick = { }
            )
            SettingBlock(
                title = stringResource(R.string.feedback),
                onClick = { }
            )
            SettingBlock(
                title = stringResource(R.string.backup),
                onClick = { }
            )
            SettingBlock(
                title = stringResource(R.string.privacy_policy),
                onClick = { }
            )
            SettingBlock(
                title = licensesTitle,
                onClick = { onLicensesClick() }
            )
            SettingBlock(
                title = stringResource(R.string.about_app),
                onClick = { }
            )
        }
    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsScreen({})
}