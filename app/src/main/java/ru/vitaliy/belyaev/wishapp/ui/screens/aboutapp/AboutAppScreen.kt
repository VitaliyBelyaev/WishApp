package ru.vitaliy.belyaev.wishapp.ui.screens.aboutapp

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import ru.vitaliy.belyaev.wishapp.BuildConfig
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.components.SettingBlock
import ru.vitaliy.belyaev.wishapp.utils.annotatedStringResource

@Composable
fun AboutAppScreen(onBackPressed: () -> Unit) {

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            WishAppTopBar(
                stringResource(R.string.about_app),
                withBackIcon = true,
                onBackPressed = onBackPressed
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        val context = LocalContext.current
        val licensesTitle = stringResource(R.string.licenses)
        val onLicensesClick: () -> Unit = {
            OssLicensesMenuActivity.setActivityTitle(licensesTitle)
            val intent = Intent(context, OssLicensesMenuActivity::class.java)
            ContextCompat.startActivity(context, intent, null)
        }
        val scrollState = rememberScrollState()
        val uriHandler = LocalUriHandler.current
        Column(
            modifier = Modifier.verticalScroll(scrollState)
        ) {
            Column {

                Text(
                    text = stringResource(
                        R.string.app_version_pattern,
                        "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                    ),
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
                )
                val appDescription = annotatedStringResource(R.string.app_description)
                ClickableText(
                    text = appDescription,
                    style = TextStyle.Default.copy(fontSize = 16.sp, color = Color.Gray),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                    onClick = {
                        appDescription
                            .getStringAnnotations("URL", it, it)
                            .firstOrNull()?.let { stringAnnotation ->
                                uriHandler.openUri(stringAnnotation.item)
                            }
                    }
                )
                Divider(modifier = Modifier.padding(start = 16.dp, end = 16.dp))
                SettingBlock(
                    title = stringResource(R.string.feedback),
                    onClick = { }
                )
                SettingBlock(
                    title = licensesTitle,
                    onClick = { onLicensesClick() }
                )
                SettingBlock(
                    title = stringResource(R.string.privacy_policy),
                    onClick = { }
                )
            }
        }
    }
}