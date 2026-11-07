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

    tasks.withType<dev.detekt.gradle.Detekt>().configureEach {
        exclude(staticAnalysisExcludes)
    }
    tasks.withType<dev.detekt.gradle.DetektCreateBaselineTask>().configureEach {
        exclude(staticAnalysisExcludes)
    }
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