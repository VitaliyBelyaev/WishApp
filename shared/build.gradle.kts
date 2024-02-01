plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.sqlDelight)
    alias(libs.plugins.kmpNativeCoroutines)
    alias(libs.plugins.ksp)
}

kotlin.sourceSets.all {
    // For KMP-NativeCoroutines https://github.com/rickclephas/KMP-NativeCoroutines#kotlin
    languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
}

android {
    namespace = "ru.vitaliy.belyaev.wishapp.shared"
    compileSdk = 33
    defaultConfig {
        minSdk = 23
        targetSdk = 33
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    androidTarget()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
        }
    }
    cocoapods {

        version = "1.0"
        summary = "WishAppSdk"
        homepage = "https://github.com/VitaliyBelyaev/WishApp"
        ios.deploymentTarget = "16.0"

        podfile = project.file("../iosApp/Podfile")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Coroutines
                implementation(libs.kotlin.coroutines.core)

                // Date time
                implementation(libs.kotlin.dateTime)

                // SQLDelight
                implementation(libs.sqlDelight.runtime)
                implementation(libs.sqlDelight.extensions.coroutines)

                // UUID
                implementation(libs.kmmUuid)

                // DI
                implementation(libs.koin.core)

                // Logging
                implementation(libs.napier)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        val androidMain by getting {
            dependencies {
                // SQLDelight
                implementation(libs.sqlDelight.driver.android)
            }
        }
        val androidUnitTest by getting
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)

            dependencies {
                // SQLDelight
                implementation(libs.sqlDelight.driver.native)
            }
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
}

sqldelight {
    databases {
        create("WishAppDb") {
            packageName.set("ru.vitaliy.belyaev.wishapp.shared.data.database")
            dialect("app.cash.sqldelight:sqlite-3-24-dialect:${libs.versions.sqlDelight.get()}")
            schemaOutputDirectory = file("src/commonMain/sqldelight/databases")
            verifyMigrations.set(true)
        }
    }
}