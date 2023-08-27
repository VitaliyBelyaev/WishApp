package ru.vitaliy.belyaev.wishapp.ui

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.core.os.postDelayed
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.data.repository.analytics.AnalyticsRepository
import ru.vitaliy.belyaev.wishapp.domain.BackupInfo
import ru.vitaliy.belyaev.wishapp.entity.Theme
import ru.vitaliy.belyaev.wishapp.entity.analytics.action_events.InAppReviewRequestedEvent
import ru.vitaliy.belyaev.wishapp.entity.analytics.action_events.InAppReviewShowEvent
import ru.vitaliy.belyaev.wishapp.navigation.Navigation
import ru.vitaliy.belyaev.wishapp.navigation.WishDetailedRoute
import ru.vitaliy.belyaev.wishapp.shared.data.WishAppSdk
import ru.vitaliy.belyaev.wishapp.shared.domain.ShareWishListTextGenerator
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppTheme
import ru.vitaliy.belyaev.wishapp.utils.createSharePlainTextIntent
import ru.vitaliy.belyaev.wishapp.utils.restartApp
import timber.log.Timber

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@AndroidEntryPoint
class AppActivity : AppCompatActivity() {

    private val viewModel: AppActivityViewModel by viewModels()

    @Inject
    lateinit var analyticsRepository: AnalyticsRepository

    @Inject
    lateinit var wishAppSdk: WishAppSdk

    private var sharedLinkFromAnotherApp: String? = null

    private val shareLinkFlow = MutableSharedFlow<String>()

    private var doAfterSignIn: (GoogleSignInAccount, Context) -> Unit = { _, _ -> }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                if (result.data != null) {
                    lifecycleScope.launch {
                        kotlin.runCatching {
                            val account: GoogleSignInAccount = GoogleSignIn.getSignedInAccountFromIntent(intent).await()

                            Timber.tag("RTRT")
                                .d("account email: ${account?.email}, id: ${account?.id}, name: ${account?.displayName}")
                            account
                        }.onSuccess { account ->
                            doAfterSignIn(account, this@AppActivity)
                        }.onFailure {
                            Timber.e(it, "Google Login Error!")
                        }
                    }
                } else {
                    Toast.makeText(this, "Google Login Error!", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Google Login Error! Result no ok", Toast.LENGTH_LONG).show()
            }
        }

    @ExperimentalAnimationApi
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        installSplashScreen()
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            sharedLinkFromAnotherApp = extractSharedLinkAndShowErrorIfInvalid(intent)
        }

        viewModel.wishListToShareLiveData.observe(this) {
            val wishListAsFormattedText = ShareWishListTextGenerator.generateFormattedWishListText(
                title = getString(R.string.wish_list_title),
                wishes = it
            )
            startActivity(createSharePlainTextIntent(wishListAsFormattedText))
        }

        viewModel.requestReviewLiveData.observe(this) {
            analyticsRepository.trackEvent(InAppReviewRequestedEvent)
            val reviewManager = ReviewManagerFactory.create(this)
            reviewManager
                .requestReviewFlow()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val reviewInfo = task.result
                        analyticsRepository.trackEvent(InAppReviewShowEvent)
                        reviewManager.launchReviewFlow(this, reviewInfo)
                    } else {
                        task.exception?.let { FirebaseCrashlytics.getInstance().recordException(it) }
                    }
                }
        }
        setContent {
            val selectedTheme: Theme by viewModel.selectedTheme.collectAsState()
            val navController = rememberAnimatedNavController()
            WishAppTheme(selectedTheme = selectedTheme) {
                Navigation(
                    navController = navController,
                    onShareClick = { viewModel.onShareWishListClicked(it) },
                    onBackupClicked = {
                        onBackupClicked()
                    },
                    onCheckBackupClicked = {
                        onCheckBackupClicked()
                    },
                    onRestoreClicked = {
                        onRestoreClicked()
                    },
                )
            }

            sharedLinkFromAnotherApp?.let {
                navController.navigate(WishDetailedRoute.buildRoute(wishLink = it))
                sharedLinkFromAnotherApp = null
            }

            LaunchedEffect(key1 = Unit) {
                shareLinkFlow.collect {
                    if (navController.currentDestination?.id != navController.graph.startDestinationId) {
                        navController.popBackStack(navController.graph.startDestinationId, false)
                    }
                    navController.navigate(WishDetailedRoute.buildRoute(wishLink = it))
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        extractSharedLinkAndShowErrorIfInvalid(intent)?.let {
            lifecycleScope.launch {
                shareLinkFlow.emit(it)
            }
        }
    }

    private fun onBackupClicked() {
        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)

        doAfterSignIn = { acc, context ->
            uploadDbToDrive(acc, context)
        }
        if (account != null) {
            Timber.tag("RTRT").d("account email: ${account.email}, id: ${account.id}, name: ${account.displayName}")
            doAfterSignIn(account, this)
        } else {
            startForResult.launch(getGoogleSignInClient(this).signInIntent)
        }
    }

    private fun onCheckBackupClicked() {
        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)
        doAfterSignIn = { acc, context ->
            checkDbBackupInDrive(acc, context)
        }
        if (account != null) {
            Timber.tag("RTRT").d("account email: ${account.email}, id: ${account.id}, name: ${account.displayName}")
            doAfterSignIn(account, this)
        } else {
            startForResult.launch(getGoogleSignInClient(this).signInIntent)
        }
    }

    private fun onRestoreClicked() {
        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)
        doAfterSignIn = { acc, context ->
            if (viewModel.currentBackupInfo.value is BackupInfo.Value) {
                restoreDbFileFromDrive(
                    (viewModel.currentBackupInfo.value as BackupInfo.Value).driveFileId,
                    acc,
                    context
                )
            }
        }
        if (account != null) {
            Timber.tag("RTRT").d("account email: ${account.email}, id: ${account.id}, name: ${account.displayName}")
            doAfterSignIn(account, this)
        } else {
            startForResult.launch(getGoogleSignInClient(this).signInIntent)
        }
    }

    private fun restoreDbFileFromDrive(
        driveFileId: String,
        account: GoogleSignInAccount,
        context: Context
    ) {
        lifecycleScope.launch {
            runCatching {
                val credential = GoogleAccountCredential.usingOAuth2(
                    context,
                    listOf(DriveScopes.DRIVE_APPDATA)
                )
                credential.selectedAccount = account.account

                // get Drive Instance
                val drive = Drive
                    .Builder(
                        NetHttpTransport(),
                        JacksonFactory.getDefaultInstance(),
                        credential
                    )
                    .setApplicationName(getString(R.string.app_name))
                    .build()

                withContext(Dispatchers.IO) {

                    val outputStream = ByteArrayOutputStream()
                    drive.Files().get(driveFileId).executeMediaAndDownloadTo(outputStream)
                    val dbFile = getDatabasePath(wishAppSdk.databaseName)
                    dbFile.delete()
                    dbFile.writeBytes(outputStream.toByteArray())
                }
            }.onSuccess {
                Toast.makeText(this@AppActivity, "DB file restored", Toast.LENGTH_SHORT).show()
                restartApp()
            }.onFailure {
                Timber.e(it)

                if (it is UserRecoverableAuthIOException) {
                    Toast.makeText(this@AppActivity, "Auth error", Toast.LENGTH_SHORT).show()
                    startForResult.launch(it.intent)
                    return@onFailure
                }
                Toast.makeText(this@AppActivity, "Error while restore DB file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkDbBackupInDrive(account: GoogleSignInAccount, context: Context) {
        lifecycleScope.launch {
            runCatching {
                val credential = GoogleAccountCredential.usingOAuth2(
                    context,
                    listOf(DriveScopes.DRIVE_APPDATA)
                )
                credential.selectedAccount = account.account

                // get Drive Instance
                val drive = Drive
                    .Builder(
                        NetHttpTransport(),
                        JacksonFactory.getDefaultInstance(),
                        credential
                    )
                    .setApplicationName(getString(R.string.app_name))
                    .build()

                withContext(Dispatchers.IO) {
                    val resultFiles: FileList = drive.Files().list()
                        .setSpaces(FOLDER_NAME_FOR_PRIVATE_APP_STORAGE)
                        .setQ("name = '$BACKUP_FILE_NAME' and mimeType = '$BACKUP_FILE_MIME_TYPE'")
                        .setFields("nextPageToken, files(id, name, createdTime, size)")
                        .setPageSize(10)
                        .execute()


                    resultFiles.files.firstOrNull()?.let {
                        Timber.tag("RTRT").d("Found file: ${it.name} with id: ${it.id}")

                        val createdLocalDateTime = Instant.ofEpochMilli(it.createdTime.value)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()

                        val backupInfo = BackupInfo.Value(
                            driveFileId = it.id,
                            createdDateTime = createdLocalDateTime,
                            sizeInBytes = it.getSize()
                        )
                        viewModel.onNewBackupInfo(backupInfo)
                    }

                    for (file in resultFiles.files) {
                        Timber.tag("RTRT").d("Found file: ${file.name} with id: ${file.id}")
                    }
                }
            }.onSuccess {
                Toast.makeText(this@AppActivity, "Check existing backup succeed", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Timber.e(it)

                if (it is UserRecoverableAuthIOException) {
                    Toast.makeText(this@AppActivity, "Auth error", Toast.LENGTH_SHORT).show()
                    startForResult.launch(it.intent)
                    return@onFailure
                }
                Toast.makeText(this@AppActivity, "Error while check existing backup", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadDbToDrive(account: GoogleSignInAccount, context: Context) {
        lifecycleScope.launch {
            runCatching {
                val credential = GoogleAccountCredential.usingOAuth2(
                    context,
                    listOf(DriveScopes.DRIVE_APPDATA)
                )
                credential.selectedAccount = account.account

                // get Drive Instance
                val drive = Drive
                    .Builder(
                        NetHttpTransport(),
                        JacksonFactory.getDefaultInstance(),
                        credential
                    )
                    .setApplicationName(getString(R.string.app_name))
                    .build()

                withContext(Dispatchers.IO) {

                    val fileMetadata = File()
                    fileMetadata.name = BACKUP_FILE_NAME

                    val fileToStore = getDatabasePath(wishAppSdk.databaseName)
                    val mediaContent = FileContent(BACKUP_FILE_MIME_TYPE, fileToStore)

                    val backupInfo = viewModel.currentBackupInfo.value
                    if (backupInfo is BackupInfo.Value) {
                        drive.Files().update(backupInfo.driveFileId, fileMetadata, mediaContent)
                            .setFields("id, name, createdTime, size")
                            .apply {
                                // Progress listener
                                mediaHttpUploader.apply {
                                    setProgressListener {
                                        Timber.tag("RTRT").d("Upload progress: ${it.progress}")
                                    }
                                }
                            }.execute()
                    } else {
                        fileMetadata.parents = listOf(FOLDER_NAME_FOR_PRIVATE_APP_STORAGE)
                        drive.Files().create(fileMetadata, mediaContent)
                            .setFields("id, name, createdTime, size")
                            .apply {
                                // Progress listener
                                mediaHttpUploader.apply {
                                    setProgressListener {
                                        Timber.tag("RTRT").d("Upload progress: ${it.progress}")
                                    }
                                }
                            }.execute()
                    }
                }
            }.onSuccess {
                Timber.tag("RTRT").d("File created, id: ${it.id}")
                Toast.makeText(this@AppActivity, "File created, id: ${it.id}", Toast.LENGTH_SHORT).show()

                viewModel.onNewBackupInfo(
                    BackupInfo.Value(
                        driveFileId = it.id,
                        createdDateTime = Instant.ofEpochMilli(it.createdTime.value)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime(),
                        sizeInBytes = it.getSize()
                    )
                )
            }.onFailure {
                Timber.e(it)

                if (it is UserRecoverableAuthIOException) {
                    Toast.makeText(this@AppActivity, "Auth error", Toast.LENGTH_SHORT).show()
                    startForResult.launch(it.intent)
                    return@onFailure
                }
                Toast.makeText(this@AppActivity, "Error while uploading DB file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
//            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA), Scope(DriveScopes.DRIVE_FILE))
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()

        return GoogleSignIn.getClient(context, signInOptions)
    }

    private fun extractSharedLinkAndShowErrorIfInvalid(intent: Intent?): String? {
        if (intent == null) return null

        val link = if (intent.action == Intent.ACTION_SEND &&
            intent.type == "text/plain" &&
            intent.hasExtra(Intent.EXTRA_TEXT)
        ) {
            intent.getStringExtra(Intent.EXTRA_TEXT)
        } else {
            null
        }

        if (link == null) return null

        return if (validateLinkAndShowErrorIfInvalid(link)) link else null
    }

    private fun validateLinkAndShowErrorIfInvalid(link: String): Boolean {
        if (!isLinkValid(link)) {
            lifecycleScope.launch { viewModel.showSnackMessageOnMain(getString(R.string.invalid_link_error_message)) }
            return false
        }
        return true
    }

    private fun isLinkValid(link: String): Boolean {
        return Patterns.WEB_URL.matcher(link).matches()
    }

    companion object {

        private const val BACKUP_FILE_NAME = "wishapp.backup"
        private const val BACKUP_FILE_MIME_TYPE = "application/octet-stream"

        // This is special folder name in Google Drive that is used for storing app data, invisible to user
        private const val FOLDER_NAME_FOR_PRIVATE_APP_STORAGE = "appDataFolder"
    }
}