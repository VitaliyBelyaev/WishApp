# Wishapp / Хотелки

https://wishapp.info/

<a href='https://play.google.com/store/apps/details?id=ru.vitaliy.belyaev.wishapp'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' width='225'/></a>
<a href='https://apps.apple.com/app/id6450624836'><img alt='Download on the App Store' src='https://github-production-user-asset-6210df.s3.amazonaws.com/21678329/261083041-baac00dd-7f84-49dd-a358-17ea4dc089ad.png' width='205'/></a>

Multiplatform app for tracking wishes with ability to share wish list with other person in text format.<br/>
You can also group your wishes with labels and share wishes by the label.

Written with Kotlin Multiplatform for Android and iOS platforms.<br/>

Wishes and labels stored in SQLite database through SQLDelight, that placed in shared module and used by both Android and iOS apps.<br/>

For access Flows from shared module in iOS app was used `KMP-NativeCoroutines` library with Swift Combine.<br/>

## Kotlin Multiplatform stack and libs:
- [SQLDelight](https://github.com/cashapp/sqldelight)
- [KMP-NativeCoroutines](https://github.com/rickclephas/KMP-NativeCoroutines)
- [kotlinx-datetime](https://github.com/Kotlin/kotlinx-datetime)
- [benasher44/uuid](https://github.com/benasher44/uuid)
- [Koin](https://github.com/InsertKoinIO/koin)
- [Napier](https://github.com/AAkira/Napier)


## Android app

Fully written with Jetpack Compose with Material You(Material 3) theming and dynamic colors support for Android 12 and higher.

### Tech stack:
- Jetpack Compose
- Material You
- MVVM (Android ViewModel)
- Jetpack Navigation for Compose
- Hilt
- Kotlin Coroutines
- Google Drive API for backup and restore user data

## iOS app

Written with SwiftUI with using new version of NavigationStack with NavPath values and persisting NavPath after app relaunch.

### Tech stack:
- SwiftUI
- Combine
- MVVM


