import com.bibleplanner.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project

class ComposeMultiplatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply(
                libs
                    .findPlugin("compose-multiplatform")
                    .get()
                    .get()
                    .pluginId,
            )
            apply(
                libs
                    .findPlugin("compose-compiler")
                    .get()
                    .get()
                    .pluginId,
            )
        }
    }
}
