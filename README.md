# Wishapp / Хотелки

<p align="center">
 <img src="https://github.com/VitaliyBelyaev/WishApp/assets/21678329/92a968ac-8746-4c2a-b6b4-36f229397bc6" width="220"/>
 <img src="https://github.com/VitaliyBelyaev/WishApp/assets/21678329/3e621942-5d5e-4cba-b86f-f8a28b49307e" width="220"/>
 <img width="220" alt="2" src="https://github.com/VitaliyBelyaev/WishApp/assets/21678329/5b548b65-6dee-45ec-8bfb-da0c64853bb7">
 <img width="220" alt="7" src="https://github.com/VitaliyBelyaev/WishApp/assets/21678329/33c5a7f6-7e6e-4398-a26f-8da453e77f8b">
</p>

https://wishapp.info/<br/>

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

<img src="https://github.com/VitaliyBelyaev/WishApp/assets/21678329/7098f2d4-705e-4847-929b-0ab3764c14c9" width="220"/>
<img src="https://github.com/VitaliyBelyaev/WishApp/assets/21678329/9a755b13-0a82-4dff-b446-ef3f231e19e0" width="220"/>
<img src="https://github.com/VitaliyBelyaev/WishApp/assets/21678329/31f3dfe5-5641-417a-9c5e-e2a9b6ed7d13" width="220"/>
<img src="https://github.com/VitaliyBelyaev/WishApp/assets/21678329/1d0f1911-7c37-4af5-8232-bcffd79028d2" width="220"/>

<br/>
<br/>
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


<img width="220" alt="5" src="https://github.com/VitaliyBelyaev/WishApp/assets/21678329/1dcc54c0-c46e-45a5-aafc-1b80d70b637a">
<img width="220" alt="7" src="https://github.com/VitaliyBelyaev/WishApp/assets/21678329/33c5a7f6-7e6e-4398-a26f-8da453e77f8b">
<img width="220" alt="2" src="https://github.com/VitaliyBelyaev/WishApp/assets/21678329/5b548b65-6dee-45ec-8bfb-da0c64853bb7">
<img width="220" alt="3" src="https://github.com/VitaliyBelyaev/WishApp/assets/21678329/e898d46d-cac0-4395-a1f0-f57cc4ebfc02">


<br/>
<br/>
Written with SwiftUI with using new version of NavigationStack with NavPath values and persisting NavPath after app relaunch.

### Tech stack:
- SwiftUI
- Combine
- MVVM


