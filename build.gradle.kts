@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.androidApplication).apply(false)
    alias(libs.plugins.androidKotlinMultiplatformLibrary).apply(false)
    alias(libs.plugins.kotlinParcelize).apply(false)
    alias(libs.plugins.kotlinSerialization).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.kotlinComposeCompilerPlugin).apply(false)
    alias(libs.plugins.sqlDelight).apply(false)
    alias(libs.plugins.hilt).apply(false)
    alias(libs.plugins.googleServices).apply(false)
    alias(libs.plugins.firebase.crashlytics).apply(false)
    alias(libs.plugins.firebase.perf).apply(false)
    alias(libs.plugins.ksp).apply(false)
    alias(libs.plugins.kmpNativeCoroutines).apply(false)
    alias(libs.plugins.ktlint).apply(false)
    alias(libs.plugins.detekt).apply(false)
}

// Прежний вид Android (ui/old, navigation/old) не меняется, поэтому не проверяется.
// Правила стиля описаны в docs/CODE_STYLE.md.
val staticAnalysisExcludes = listOf("**/ui/old/**", "**/navigation/old/**")
val ktlintVersion = libs.versions.ktlint.get()
val composeRulesDetekt = libs.composeRules.detekt

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "dev.detekt")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set(ktlintVersion)
        filter {
            exclude { element ->
                val path = element.file.invariantSeparatorsPath
                path.contains("/build/") || path.contains("/ui/old/") || path.contains("/navigation/old/")
            }
        }
    }

    // ktlint-gradle при инкрементальном запуске помнит ошибки удалённых файлов.
    // Меняющееся свойство делает каждый запуск полным: файлов немного, это секунды.
    tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask>().configureEach {
        inputs.property("fullRun", System.nanoTime())
    }

    configure<dev.detekt.gradle.extensions.DetektExtension> {
        buildUponDefaultConfig.set(true)
        // Один таск detekt на модуль, без проверки типов: все исходники из src
        source.setFrom(project.file("src"))
        parallel.set(true)
        config.setFrom(rootProject.file("config/detekt/detekt.yml"))
        baseline.set(rootProject.file("config/detekt/baseline-${project.name}.xml"))
    }

    dependencies {
        add("detektPlugins", composeRulesDetekt)
    }

    // Задачи с проверкой типов берут и сгенерированный код (SQLDelight, BuildConfig) из build/
    tasks.withType<dev.detekt.gradle.Detekt>().configureEach {
        exclude(staticAnalysisExcludes)
        exclude { it.file.invariantSeparatorsPath.contains("/build/") }
    }
    tasks.withType<dev.detekt.gradle.DetektCreateBaselineTask>().configureEach {
        exclude(staticAnalysisExcludes)
        exclude { it.file.invariantSeparatorsPath.contains("/build/") }
    }
    // В «ktlintFormat detekt» detekt смотрит уже отформатированный код
    tasks.named("detekt") {
        mustRunAfter("ktlintFormat")
    }
}

// detekt с проверкой типов: дольше (нужна компиляция), ловит больше. Не в хуке агента,
// а перед коммитом (git pre-commit) и перед вливанием ветки, см. docs/CODE_STYLE.md.
// В app берём только debug: в нём все папки исходников, detektMain гонял бы каждый вариант.
val detektTypedTasks = listOf(
    ":app:detektDebug",
    ":app:detektDebugUnitTest",
    ":app:detektDebugAndroidTest",
    ":shared:detektMainAndroid",
)
tasks.register("detektTyped") {
    group = "verification"
    description = "Runs detekt with type resolution"
    dependsOn(detektTypedTasks)
}
tasks.register("detektTypedBaseline") {
    group = "verification"
    description = "Creates baselines for detekt with type resolution"
    dependsOn(detektTypedTasks.map { it.replace(":detekt", ":detektBaseline") })
}

buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://plugins.gradle.org/m2/")
        maven("https://jitpack.io")
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    }

    dependencies {
        classpath("com.google.android.gms:oss-licenses-plugin:0.13.0")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}