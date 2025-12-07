plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.7.1"
}

group = "dev.kalachik"
version = "1.0.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Configure IntelliJ Platform Gradle Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    intellijPlatform {
        create("IC", "2025.1.4.1")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)

         bundledPlugin("org.intellij.plugins.markdown")
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "251"
        }

        description = """
            <p><strong>Mermaid Hex Helper</strong> helps you visualize colors in Mermaid diagrams.</p>
            <p>Features:</p>
            <ul>
                <li>Shows color previews for hex codes in Mermaid blocks.</li>
                <li>Supports Markdown files.</li>
            </ul>
        """.trimIndent()

        changeNotes = """
            <strong>v1.0.0</strong> - initial release
        """.trimIndent()
    }
    buildSearchableOptions = false
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}
