package ru.vitaliy.belyaev.wishapp.ui.screens.aboutapp

import android.content.Intent
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.wishapp.BuildConfig
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.entity.createAppFeedback
import ru.vitaliy.belyaev.wishapp.ui.core.topappbar.WishAppTopBar
import ru.vitaliy.belyaev.wishapp.ui.screens.settings.components.SettingBlock
import ru.vitaliy.belyaev.wishapp.ui.theme.material3.CommonColors
import ru.vitaliy.belyaev.wishapp.utils.annotatedStringResource
import ru.vitaliy.belyaev.wishapp.utils.createSendEmailIntent
import ru.vitaliy.belyaev.wishapp.utils.showDismissableSnackbar

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalMaterialApi
@Composable
fun AboutAppScreen(
    onBackPressed: () -> Unit,
    onPrivacyPolicyClicked: () -> Unit,
    viewModel: AboutAppViewModel = hiltViewModel()
) {

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollState: ScrollState = rememberScrollState()

    val onSendFeedbackClicked: () -> Unit = {
        viewModel.onSendFeedbackClicked()
        val feedback = createAppFeedback(context.resources)
        val intent = createSendEmailIntent(feedback.email, feedback.subject, feedback.message)
        try {
            context.startActivity(intent)
        } catch (t: Throwable) {
            scope.launch {
                snackbarHostState.showDismissableSnackbar(context.getString(R.string.no_email_app_error))
            }
        }
    }

    val systemUiController = rememberSystemUiController()
    val screenNavBarColor = CommonColors.navBarColor()
    LaunchedEffect(key1 = Unit) {
        systemUiController.setNavigationBarColor(color = screenNavBarColor)
    }

    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.Companion.safeDrawing,
        topBar = {
            WishAppTopBar(
                stringResource(R.string.about_app),
                withBackIcon = true,
                onBackPressed = onBackPressed,
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) {
        val licensesTitle = stringResource(R.string.licenses)
        val onLicensesClick: () -> Unit = {
            OssLicensesMenuActivity.setActivityTitle(licensesTitle)
            val intent = Intent(context, OssLicensesMenuActivity::class.java)
            ContextCompat.startActivity(context, intent, null)
        }
        val uriHandler = LocalUriHandler.current

        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(it)
        ) {
            Text(
                text = stringResource(
                    R.string.app_version_pattern,
                    "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                ),
                modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
            )
            val appDescription = annotatedStringResource(R.string.app_description)
            ClickableText(
                text = appDescription,
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                onClick = {
                    viewModel.onSourceCodeUrlClicked()
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
                onClick = { onSendFeedbackClicked() }
            )
            SettingBlock(
                title = licensesTitle,
                onClick = { onLicensesClick() }
            )
            SettingBlock(
                title = stringResource(R.string.privacy_policy),
                onClick = { onPrivacyPolicyClicked() }
            )
        }
    }
}