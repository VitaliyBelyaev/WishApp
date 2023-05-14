@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("com.android.application").version(libs.versions.androidGradle.get()).apply(false)
    id("com.android.library").version(libs.versions.androidGradle.get()).apply(false)
    kotlin("android").version(libs.versions.kotlin.get()).apply(false)
    kotlin("kapt").version(libs.versions.kotlin.get()).apply(false)
    id("com.squareup.sqldelight").version(libs.versions.sqlDelight.get()).apply(false)
    id("com.google.dagger.hilt.android").version(libs.versions.hilt.get()).apply(false)
    id("com.google.gms.google-services").version("4.3.14").apply(false)
    id("com.google.firebase.crashlytics").version("2.9.2").apply(false)
    id("com.google.firebase.firebase-perf").version("1.4.2").apply(false)
    id("com.google.devtools.ksp").version("1.8.21-1.0.11").apply(false)
    id("com.rickclephas.kmp.nativecoroutines").version(libs.versions.nativeCoroutines.get()).apply(false)
}

buildscript {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://www.jetbrains.com/intellij-repository/releases")
        maven(url = "https://jetbrains.bintray.com/intellij-third-party-dependencies")
    }

    dependencies {
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.6")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}