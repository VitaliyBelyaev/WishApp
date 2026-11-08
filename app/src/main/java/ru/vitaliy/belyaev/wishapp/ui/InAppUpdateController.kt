package ru.vitaliy.belyaev.wishapp.ui

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.requestAppUpdateInfo
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.InAppUpdateAnsweredEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.InAppUpdateDownloadedEvent
import ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events.InAppUpdateShownEvent
import ru.vitaliy.belyaev.wishapp.domain.repository.AnalyticsRepository
import timber.log.Timber

/**
 * Мягкое обновление через Google Play (in-app update, гибкий режим): Play качает новую версию
 * в фоне, человек продолжает пользоваться приложением, а когда всё скачано, на главном экране
 * снэкбар «Перезапустить».
 *
 * Предлагаем, только если устройство знает о новой версии не меньше [MinStalenessDays] дней: пока
 * версия на поэтапной раскатке, рано. После отказа повторяем не чаще раза в [PromptIntervalDays] дней.
 * Работает только у сборок, установленных из Google Play: в остальных Play отвечает ошибкой,
 * и предложения просто нет.
 *
 * Создавать в `onCreate` активити: окно Play запускается через Activity Result API.
 */
internal class InAppUpdateController(
    private val activity: ComponentActivity,
    private val dataStoreRepository: DataStoreRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val onUpdateDownloaded: () -> Unit,
) {

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)

    private val updateFlowLauncher: ActivityResultLauncher<IntentSenderRequest> =
        activity.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            analyticsRepository.trackEvent(InAppUpdateAnsweredEvent(accepted = result.resultCode == Activity.RESULT_OK))
        }

    // Снэкбар о скачанном обновлении один раз за запуск: иначе он всплывал бы при каждом возврате в приложение
    private var downloadedReported = false

    private val installStateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) reportDownloaded()
    }

    init {
        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                appUpdateManager.registerListener(installStateListener)
            }

            override fun onResume(owner: LifecycleOwner) {
                // Обновление могло докачаться, пока приложение было свёрнуто или закрыто
                appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
                    if (info.installStatus() == InstallStatus.DOWNLOADED) reportDownloaded()
                }
            }

            override fun onDestroy(owner: LifecycleOwner) {
                appUpdateManager.unregisterListener(installStateListener)
            }
        })
    }

    /** Проверить, есть ли обновление, и предложить его. Вызывать при первом запуске активити. */
    fun checkForUpdate() {
        activity.lifecycleScope.launch {
            val info = try {
                appUpdateManager.requestAppUpdateInfo()
            } catch (e: Exception) {
                // Не из Google Play, нет Play или сети: ожидаемо, в Crashlytics не шлём
                Timber.w(e, "In-app update info is not available")
                return@launch
            }
            if (shouldPrompt(info)) prompt(info)
        }
    }

    /** Поставить скачанное обновление: Play перезапустит приложение. */
    fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }

    private suspend fun shouldPrompt(info: AppUpdateInfo): Boolean {
        if (info.updateAvailability() != UpdateAvailability.UPDATE_AVAILABLE) return false
        if (!info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) return false
        if ((info.clientVersionStalenessDays() ?: 0) < MinStalenessDays) return false
        val shownAt = dataStoreRepository.getUpdatePromptShownAtMillis() ?: return true
        return System.currentTimeMillis() - shownAt >= TimeUnit.DAYS.toMillis(PromptIntervalDays)
    }

    private suspend fun prompt(info: AppUpdateInfo) {
        val started = appUpdateManager.startUpdateFlowForResult(
            info,
            updateFlowLauncher,
            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
        )
        if (!started) return
        dataStoreRepository.updateUpdatePromptShownAtMillis(System.currentTimeMillis())
        analyticsRepository.trackEvent(
            InAppUpdateShownEvent(
                stalenessDays = info.clientVersionStalenessDays() ?: 0,
                availableVersionCode = info.availableVersionCode(),
            )
        )
    }

    private fun reportDownloaded() {
        if (downloadedReported) return
        downloadedReported = true
        analyticsRepository.trackEvent(InAppUpdateDownloadedEvent)
        onUpdateDownloaded()
    }

    private companion object {
        const val MinStalenessDays = 3
        const val PromptIntervalDays = 7L
    }
}
