package ru.vitaliy.belyaev.wishapp.ui.screens.backup

import android.icu.text.DateFormat as IcuDateFormat
import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.domain.model.BackupInfo
import ru.vitaliy.belyaev.wishapp.ui.components.header.HeaderButtonSize
import ru.vitaliy.belyaev.wishapp.ui.components.header.PageHeader
import ru.vitaliy.belyaev.wishapp.ui.components.header.PageHeaderCollapse
import ru.vitaliy.belyaev.wishapp.ui.components.header.headerHeight
import ru.vitaliy.belyaev.wishapp.ui.core.snackbar.AppSnackMessage
import ru.vitaliy.belyaev.wishapp.ui.screens.wishlist.WishSheet
import ru.vitaliy.belyaev.wishapp.ui.screens.wishlist.sheetBottomPadding
import ru.vitaliy.belyaev.wishapp.ui.theme.AppThemeExtras
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing
import ru.vitaliy.belyaev.wishapp.ui.theme.bodyS
import ru.vitaliy.belyaev.wishapp.ui.theme.titleM
import ru.vitaliy.belyaev.wishapp.utils.BytesSizeFormatter
import ru.vitaliy.belyaev.wishapp.utils.openGooglePlay
import ru.vitaliy.belyaev.wishapp.utils.restartAppWithDelayMillis
import ru.vitaliy.belyaev.wishapp.utils.trackScreenShow

class BackupActions(
    val onBack: () -> Unit,
    val onShowMessage: (AppSnackMessage) -> Unit,
)

/** Какое окно подтверждения открыто. */
internal enum class BackupDialog { CREATE_OVER, RESTORE, REPLACE }

/**
 * Резервная копия нового вида: сверху статус данных, главное действие зависит от ситуации,
 * второстепенное тише ниже. Окна подтверждения только там, где данные заменяются безвозвратно.
 */
@Composable
fun BackupScreen(actions: BackupActions) {
    BackupScreen(actions = actions, viewModel = hiltViewModel())
}

@Composable
internal fun BackupScreen(actions: BackupActions, viewModel: BackupViewModel) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    trackScreenShow { viewModel.trackScreenShow() }

    val signInLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        viewModel.onSignInResult(it)
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            val message = when (event) {
                is BackupEvent.Message -> AppSnackMessage(
                    text = resources.getString(event.text),
                    actionLabel = event.retry?.let { resources.getString(R.string.retry) },
                    onAction = event.retry,
                )
                // Висит, пока не закроют: без обновления копию не вернуть
                BackupEvent.NewerVersion -> AppSnackMessage(
                    text = resources.getString(R.string.backup_restore_newer_version_text),
                    actionLabel = resources.getString(R.string.backup_update_app_action),
                    onAction = { context.openGooglePlay() },
                    duration = SnackbarDuration.Indefinite,
                )
                BackupEvent.Restored -> {
                    context.restartAppWithDelayMillis(RESTART_DELAY_MILLIS)
                    AppSnackMessage(text = resources.getString(R.string.snack_backup_restored))
                }
            }
            actions.onShowMessage(message)
        }
    }

    BackupContent(
        state = state,
        callbacks = object : BackupCallbacks {
            override fun onBack() = actions.onBack()
            override fun onInfo() = viewModel.onInfoClicked()
            override fun onAllowAccess() {
                viewModel.onAllowAccessClicked()
                signInLauncher.launch(viewModel.signInIntent)
            }
            override fun onRefresh() = viewModel.onRefreshClicked()
            override fun onCreate() = viewModel.onCreateClicked()
            override fun onRestore() = viewModel.onRestoreConfirmed()
            override fun onSignOut() = viewModel.onSignOutClicked()
            override fun onDisconnect() = viewModel.onDisconnectClicked()
        },
    )
}

internal interface BackupCallbacks {
    fun onBack()
    fun onInfo()
    fun onAllowAccess()
    fun onRefresh()
    fun onCreate()

    /** Восстановление уже подтверждено в окне. */
    fun onRestore()
    fun onSignOut()
    fun onDisconnect()
}

@Composable
internal fun BackupContent(state: BackupState, callbacks: BackupCallbacks) {
    val work = state.work
    if (work is BackupWork.Restoring) {
        // Данные заменяются: уйти с экрана нельзя, после него приложение перезапустится
        BackHandler {}
        RestoringScreen(progressText = progressText(work.progress), fraction = work.progress?.fraction)
        return
    }

    var dialog by rememberSaveable { mutableStateOf<BackupDialog?>(null) }
    var showInfo by rememberSaveable { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val collapsePx = with(LocalDensity.current) { PageHeaderCollapse.toPx() }
    val progress: () -> Float = { (scrollState.value / collapsePx).coerceIn(0f, 1f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .padding(start = WishAppSpacing.s4, end = WishAppSpacing.s4, top = headerHeight() + WishAppSpacing.s1)
                .testTag(BackupTestTags.CONTENT),
        ) {
            BackupBody(state = state, callbacks = callbacks, onDialog = { dialog = it })
            Spacer(modifier = Modifier.height(40.dp))
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
        PageHeader(
            title = stringResource(R.string.backup_title),
            progress = progress,
            onBack = callbacks::onBack,
            action = {
                IconButton(
                    onClick = {
                        callbacks.onInfo()
                        showInfo = true
                    },
                    modifier = Modifier.size(HeaderButtonSize),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_ms_info_filled),
                        contentDescription = stringResource(R.string.cd_backup_info),
                    )
                }
            },
        )
    }

    dialog?.let { shown ->
        BackupDialogFor(
            dialog = shown,
            onConfirm = {
                dialog = null
                if (shown == BackupDialog.CREATE_OVER) callbacks.onCreate() else callbacks.onRestore()
            },
            onDismiss = { dialog = null },
        )
    }
    if (showInfo) {
        BackupInfoSheet(onDismiss = { showInfo = false })
    }
}

/**
 * Карточка статуса и всё, что под ней. Смена состояния не скачет: карточка растворяется в новую,
 * а высота меняется плавно. Прогресс внутри того же состояния обновляется без анимации.
 */
@Composable
private fun BackupBody(state: BackupState, callbacks: BackupCallbacks, onDialog: (BackupDialog) -> Unit) {
    AnimatedContent(
        targetState = state,
        contentKey = { it.cardKey() },
        transitionSpec = { backupFade() },
        label = "backupCard",
    ) { shown ->
        Column { StatusCardFor(shown, callbacks, onDialog) }
    }
    AnimatedContent(
        targetState = state,
        contentKey = { it.belowKey() },
        transitionSpec = { backupFade() },
        label = "backupBelowCard",
    ) { shown ->
        Column { BelowCard(shown, callbacks, onDialog) }
    }
}

private fun AnimatedContentTransitionScope<BackupState>.backupFade(): ContentTransform =
    fadeIn(tween(FADE_IN_MILLIS, delayMillis = FADE_OUT_MILLIS)) togetherWith
        fadeOut(tween(FADE_OUT_MILLIS)) using
        SizeTransform(clip = false) { _, _ -> tween(SIZE_MILLIS) }

/** Какая карточка сверху: пока ключ тот же, карточка не перерисовывается анимацией. */
private fun BackupState.cardKey(): String = when (val status = status) {
    BackupStatus.Loading -> "loading"
    BackupStatus.SignedOut -> "signedOut"
    is BackupStatus.Checking -> "checking"
    is BackupStatus.CheckFailed -> "checkFailed"
    is BackupStatus.NoBackup -> if (work is BackupWork.Saving) "saving" else "none"
    is BackupStatus.HasBackup -> when {
        work is BackupWork.Saving -> "saving"
        status.isRestoreSuggested -> "restore"
        else -> "safe"
    }
}

/** Что под карточкой: только аккаунт или ещё «Другие действия». */
private fun BackupState.belowKey(): String = when (val status = status) {
    BackupStatus.Loading, BackupStatus.SignedOut -> "empty"
    is BackupStatus.HasBackup -> "actions|${status.isRestoreSuggested && work == null}|${work == null}"
    else -> "account|${work == null}"
}

@Composable
private fun StatusCardFor(state: BackupState, callbacks: BackupCallbacks, onDialog: (BackupDialog) -> Unit) {
    val work = state.work
    when (val status = state.status) {
        BackupStatus.Loading -> Unit
        BackupStatus.SignedOut -> StatusCard(
            tone = CardTone.PLAIN,
            icon = R.drawable.ic_ms_cloud_done_filled,
            title = stringResource(R.string.backup_in_drive),
            text = stringResource(R.string.backup_in_drive_text),
        ) {
            CardButton(
                text = stringResource(R.string.backup_allow_access_to_drive_text),
                onClick = callbacks::onAllowAccess,
            )
        }
        is BackupStatus.Checking -> StatusCard(
            tone = CardTone.PLAIN,
            icon = R.drawable.ic_ms_cloud_done_filled,
            title = stringResource(R.string.backup_checking),
        ) {
            BackupProgressBar(
                fraction = null,
                color = MaterialTheme.colorScheme.tertiary,
                track = AppThemeExtras.colors.surfaceCard,
            )
        }
        is BackupStatus.CheckFailed -> StatusCard(
            tone = CardTone.ERROR,
            icon = R.drawable.ic_ms_cloud_off_filled,
            title = stringResource(R.string.backup_check_failed),
            text = stringResource(R.string.backup_data_ok_retry),
        ) {
            CardButton(text = stringResource(R.string.retry), onClick = callbacks::onRefresh)
        }
        is BackupStatus.NoBackup -> if (work is BackupWork.Saving) {
            SavingCard(work)
        } else {
            StatusCard(
                tone = CardTone.WARN,
                icon = R.drawable.ic_ms_cloud_off_filled,
                title = stringResource(R.string.backup_none_title),
                text = stringResource(R.string.backup_none_text),
                onRefresh = callbacks::onRefresh,
            ) {
                CardButton(text = stringResource(R.string.backup_create), onClick = callbacks::onCreate)
            }
        }
        is BackupStatus.HasBackup -> HasBackupCard(status, work, callbacks, onDialog)
    }
}

@Composable
private fun HasBackupCard(
    status: BackupStatus.HasBackup,
    work: BackupWork?,
    callbacks: BackupCallbacks,
    onDialog: (BackupDialog) -> Unit,
) {
    val backup = status.backup
    when {
        work is BackupWork.Saving -> SavingCard(work)
        status.isRestoreSuggested -> {
            StatusCard(
                tone = CardTone.PLAIN,
                icon = R.drawable.ic_ms_history,
                title = stringResource(R.string.backup_exists_title),
                text = listOf(
                    backupDetails(backup),
                    stringResource(R.string.backup_not_restored),
                ).joinToString(" "),
                onRefresh = callbacks::onRefresh,
            ) {
                CardButton(text = stringResource(R.string.backup_restore), onClick = { onDialog(BackupDialog.RESTORE) })
            }
            Text(
                text = stringResource(R.string.backup_restore_warning),
                style = MaterialTheme.typography.bodyS,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = WishAppSpacing.s1,
                    end = WishAppSpacing.s1,
                    top = WishAppSpacing.s2,
                ),
            )
        }
        else -> StatusCard(
            tone = CardTone.DONE,
            icon = R.drawable.ic_ms_cloud_done_filled,
            title = stringResource(R.string.backup_all_safe),
            text = formatBackupDate(backup.modifiedDateTime),
            onRefresh = callbacks::onRefresh,
        ) {
            Text(
                text = backupMeta(backup),
                style = MaterialTheme.typography.cardText,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            CardButton(text = stringResource(R.string.backup_save_now), onClick = callbacks::onCreate, isDone = true)
        }
    }
}

@Composable
private fun BelowCard(state: BackupState, callbacks: BackupCallbacks, onDialog: (BackupDialog) -> Unit) {
    val work = state.work
    val isIdle = work == null
    val status = state.status
    if (status is BackupStatus.HasBackup) {
        BackupSectionLabel(stringResource(R.string.backup_other))
        BackupList(enabled = isIdle) {
            if (status.isRestoreSuggested && isIdle) {
                BackupRow(
                    icon = R.drawable.ic_ms_cloud_done_filled,
                    title = stringResource(R.string.backup_create_new),
                    subtitle = stringResource(R.string.backup_replaces_drive),
                    onClick = { onDialog(BackupDialog.CREATE_OVER) },
                )
            } else {
                BackupRow(
                    icon = R.drawable.ic_ms_history,
                    title = stringResource(R.string.backup_replace),
                    subtitle = stringResource(R.string.backup_replace_hint).takeIf { isIdle },
                    onClick = { onDialog(BackupDialog.REPLACE) }.takeIf { isIdle },
                )
            }
        }
    }
    // Проверка и ошибка проверки не блокируют выход из аккаунта
    val accountEnabled = isIdle || status is BackupStatus.Checking || status is BackupStatus.CheckFailed
    AccountSection(
        account = status.account,
        name = state.accountName,
        photoUrl = state.accountPhotoUrl,
        enabled = accountEnabled,
        callbacks = callbacks,
    )
}

/** «Сохраняем…» вместо карточки статуса: мегабайты, когда размер известен. */
@Composable
private fun SavingCard(work: BackupWork.Saving) {
    StatusCard(
        tone = CardTone.DONE,
        icon = R.drawable.ic_ms_cloud_done_filled,
        title = stringResource(R.string.backup_saving),
        text = progressText(work.progress),
    ) {
        BackupProgressBar(
            fraction = work.progress?.fraction,
            color = MaterialTheme.colorScheme.tertiary,
            track = AppThemeExtras.colors.surfaceCard,
        )
    }
}

/** Кто вошёл — шапкой с аватаром, не строкой: её не путают с действиями ниже. */
@Composable
private fun AccountSection(
    account: String?,
    name: String?,
    photoUrl: String?,
    enabled: Boolean,
    callbacks: BackupCallbacks,
) {
    if (account == null) return
    BackupSectionLabel(stringResource(R.string.backup_account))
    BackupList(enabled = enabled) {
        AccountHeader(email = account, name = name, photoUrl = photoUrl)
        BackupRow(
            icon = R.drawable.ic_ms_logout,
            title = stringResource(R.string.backup_account_sing_out_button_text),
            onClick = callbacks::onSignOut.takeIf { enabled },
        )
        BackupRow(
            icon = R.drawable.ic_ms_close,
            title = stringResource(R.string.backup_account_disconnect_button_text),
            subtitle = stringResource(R.string.backup_disconnect_hint),
            onClick = callbacks::onDisconnect.takeIf { enabled },
        )
    }
}

private const val FADE_IN_MILLIS = 220
private const val FADE_OUT_MILLIS = 90
private const val SIZE_MILLIS = 300

@Composable
private fun BackupDialogFor(dialog: BackupDialog, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val (title, text, confirm) = when (dialog) {
        BackupDialog.CREATE_OVER -> Triple(
            R.string.backup_new_confirm_title,
            R.string.backup_new_confirm_text,
            R.string.backup_create_backup_confirm_dialog_positive_text,
        )
        BackupDialog.RESTORE -> Triple(
            R.string.backup_restore_confirm_title,
            R.string.backup_restore_confirm_text,
            R.string.backup_restore_backup_confirm_dialog_positive_text,
        )
        BackupDialog.REPLACE -> Triple(
            R.string.backup_replace_confirm_title,
            R.string.backup_restore_confirm_text,
            R.string.backup_replace_action,
        )
    }
    BackupConfirmDialog(
        title = stringResource(title),
        text = stringResource(text),
        confirm = stringResource(confirm),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

/** Шторка «Как работает копия» по «i» в шапке. */
@Composable
private fun BackupInfoSheet(onDismiss: () -> Unit) {
    WishSheet(onDismiss = onDismiss) {
        Text(
            text = stringResource(R.string.backup_help_title),
            style = MaterialTheme.typography.titleM,
            modifier = Modifier.padding(bottom = WishAppSpacing.s2),
        )
        listOf(R.string.backup_help_1, R.string.backup_help_2).forEach { paragraph ->
            Text(
                text = stringResource(paragraph),
                style = MaterialTheme.typography.cardText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 10.dp),
            )
        }
        Spacer(modifier = Modifier.height(sheetBottomPadding()))
    }
}

@Composable
private fun progressText(progress: BackupProgress?): String = if (progress == null) {
    stringResource(R.string.backup_preparing)
} else {
    stringResource(
        R.string.backup_progress,
        BytesSizeFormatter.format(progress.loadedBytes),
        BytesSizeFormatter.format(progress.totalBytes),
    )
}

/** «15 октября, 18:30, 2,4 МБ, Samsung S24.» для карточки «Есть копия». */
@Composable
private fun backupDetails(backup: BackupInfo.Value): String =
    listOf(formatBackupDate(backup.modifiedDateTime), backupMeta(backup)).joinToString(", ") + "."

/** Размер и телефон, с которого сделана копия. */
private fun backupMeta(backup: BackupInfo.Value): String =
    listOfNotNull(BytesSizeFormatter.format(backup.sizeInBytes), backup.device).joinToString(", ")

/**
 * Дата по правилам языка: «15 октября в 18:30», год — только если не текущий. ICU по шаблону-скелету
 * сам ставит месяц в нужный падеж, `java.time` на Android даёт «15 октябрь».
 */
@Composable
@ReadOnlyComposable
private fun formatBackupDate(dateTime: LocalDateTime): String {
    val locale = LocalLocale.current.platformLocale
    val hours = if (DateFormat.is24HourFormat(LocalContext.current)) "Hmm" else "hmm"
    val skeleton = (if (dateTime.year == LocalDateTime.now().year) "dMMMM" else "dMMMMyyyy") + hours
    val date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant())
    return IcuDateFormat.getInstanceForSkeleton(skeleton, locale).format(date)
}

object BackupTestTags {
    const val CONTENT = "backup_content"
    const val STATUS = "backup_status"
    const val CONFIRM = "backup_confirm"
    const val RESTORING = "backup_restoring"
}

private const val RESTART_DELAY_MILLIS = 3000L
