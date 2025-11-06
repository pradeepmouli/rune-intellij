/*
 * IntelliJ Platform Plugin build configuration.
 * See https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
 */

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.intellijPlatform)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

group = "com.github.pmouli.rune"
version = "0.1.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2024.2.4")
        bundledPlugins("com.intellij.java")
        instrumentationTools()
        pluginVerifier()
    }
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(21)
}

intellijPlatform {
    pluginConfiguration {
        id = "com.github.pmouli.rune-intellij"
        name = "Rune IntelliJ Plugin"
        version = project.version.toString()

        ideaVersion {
            sinceBuild = "242"
            untilBuild = "243.*"
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
    }

    runIde {
        jvmArgs("-Xmx2G")
    }

    buildSearchableOptions {
        enabled = false // Disable for initial development
    }
}

ktlint {
    version.set("1.0.1")
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("$rootDir/detekt.yml"))
}
