/*
 * IntelliJ Platform Plugin build configuration.
 * See https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
 */

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.intellijPlatform)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    alias(libs.plugins.grammarKit)
}

group = "dev.mouli.rune"
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
        pluginVerifier()
    }
    implementation(libs.jflex.lib)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(21)
}

intellijPlatform {
    pluginConfiguration {
        id = "dev.mouli.rune-intellij"
        name = "Rune DSL"
        version = project.version.toString()

        ideaVersion {
            sinceBuild = "242"
            untilBuild = "243.*"
        }
    }

    pluginVerification {
        ides {
            ide("IC-2024.2.4")
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
    verbose.set(false)
    android.set(false)
    outputToConsole.set(false)

    // Disable ktlint temporarily due to parsing issues
    // Re-enable after fixing compatibility
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("$rootDir/detekt.yml"))
}

// Grammar-Kit configuration for lexer and parser generation
grammarKit {
    jflexRelease.set("1.9.2")
}

tasks {
    generateLexer {
        sourceFile.set(file("src/main/kotlin/dev/mouli/rune/lexer/RosettaLexer.flex"))
        targetOutputDir.set(file("build/generated/sources/lexer/dev/mouli/rune/lexer"))
        purgeOldFiles.set(true)
    }

    generateParser {
        sourceFile.set(file("src/main/kotlin/dev/mouli/rune/parser/Rosetta.bnf"))
        targetRootOutputDir.set(file("build/generated/sources/parser"))
        pathToParser.set("dev/mouli/rune/parser/RosettaParser.java")
        pathToPsiRoot.set("dev/mouli/rune/psi")
        purgeOldFiles.set(true)
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        dependsOn(generateLexer, generateParser)
    }

    named("prepareJarSearchableOptions") {
        enabled = false
    }
}

sourceSets {
    main {
        java {
            srcDir("build/generated/sources/lexer")
            srcDir("build/generated/sources/parser")
        }
    }
}
