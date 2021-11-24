package ru.vitaliy.belyaev.wishapp.ui.screens.settings.entity

import ru.vitaliy.belyaev.wishapp.R

sealed class SettingItem(val titleRes: Int)

object Theme : SettingItem(R.string.theme)
object Backup : SettingItem(R.string.backup)
object Feedback : SettingItem(R.string.feedback)
object AboutApp : SettingItem(R.string.about_app)
object Licenses : SettingItem(R.string.licenses)
object PrivacyPolicy : SettingItem(R.string.privacy_policy)
