package ru.vitaliy.belyaev.wishapp.domain.model.analytics.action_events

import ru.vitaliy.belyaev.wishapp.domain.model.analytics.AnalyticsEvent

object BackupGiveDrivePermissionClickedEvent : AnalyticsEvent {

    override val name: String = "Backup Screen - Give Drive Permission Clicked"

    override val params: Map<String, Any?> = emptyMap()
}

object BackupInfoIconClickedEvent : AnalyticsEvent {

    override val name: String = "Backup Screen - Info Icon Clicked"

    override val params: Map<String, Any?> = emptyMap()
}

object BackupRefreshInfoClickedEvent : AnalyticsEvent {

    override val name: String = "Backup Screen - Refresh Info Clicked"

    override val params: Map<String, Any?> = emptyMap()
}

object BackupCreateBackupClickedEvent : AnalyticsEvent {

    override val name: String = "Backup Screen - Create Backup Clicked"

    override val params: Map<String, Any?> = emptyMap()
}

object BackupRestoreBackupClickedEvent : AnalyticsEvent {

    override val name: String = "Backup Screen - Restore Backup Clicked"

    override val params: Map<String, Any?> = emptyMap()
}

object BackupForceUpdateAppDataClickedEvent : AnalyticsEvent {

    override val name: String = "Backup Screen - Force Update App Data Clicked"

    override val params: Map<String, Any?> = emptyMap()
}

object BackupCreateBackupSucceedEvent : AnalyticsEvent {

    override val name: String = "Backup Screen - Create Backup Succeed"

    override val params: Map<String, Any?> = emptyMap()
}

object BackupRestoreBackupSucceedEvent : AnalyticsEvent {

    override val name: String = "Backup Screen - Restore Backup Succeed"

    override val params: Map<String, Any?> = emptyMap()
}

object BackupForceUpdateAppDataSucceedEvent : AnalyticsEvent {

    override val name: String = "Backup Screen - Force Update App Data Succeed"

    override val params: Map<String, Any?> = emptyMap()
}