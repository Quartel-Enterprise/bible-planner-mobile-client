import com.bibleplanner.buildlogic.configureCoverage
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.bibleplanner.kotlin.composeMultiplatform)
}

// A headless driver for coding agents: it runs the app's real ViewModels, Koin graph and Room
// database on the JVM without a window, simulator or emulator. See docs/agent-cli.md.
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

configureCoverage()

dependencies {
    implementation(projects.shared)

    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.sqlite.bundled)
    // Compose Resources resolves strings through Skiko, whose native library only this artifact ships
    implementation(compose.desktop.currentOs)
    implementation(libs.compose.components.resources)
    implementation(libs.kermit)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.navigation3.runtime)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
}

/*
 * scripts/agent-cli starts the JVM from these two files instead of an installDist: :core:chapter_study
 * and :feature:chapter_study both build chapter_study-jvm.jar, which one lib/ directory can't hold.
 */
val agentCliLauncher = tasks.register("agentCliLauncher") {
    group = "application"
    description = "Writes the java binary and runtime classpath scripts/agent-cli launches the agent CLI with."
    val runtimeClasspath = sourceSets.main.map { sourceSet -> sourceSet.runtimeClasspath }
    val launcherDirectory = layout.buildDirectory.dir("agent-cli-launcher")
    inputs.files(runtimeClasspath)
    outputs.dir(launcherDirectory)
    doLast {
        val directory = launcherDirectory.get().asFile
        directory.mkdirs()
        directory.resolve("java").writeText(File(System.getProperty("java.home"), "bin/java").absolutePath)
        directory.resolve("classpath").writeText(runtimeClasspath.get().asPath)
    }
}

// CI runs every module's tests through `jvmTest`, the name Kotlin Multiplatform gives them
tasks.register("jvmTest") {
    group = "verification"
    description = "Runs the agent CLI tests under the name CI runs every JVM test with."
    dependsOn(tasks.test)
}
