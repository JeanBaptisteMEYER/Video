import androidx.room.gradle.RoomExtension
import com.jbm.gradleplugins.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            with(pluginManager) {
                apply("androidx.room")
                apply("com.google.devtools.ksp")
            }

            extensions.configure<RoomExtension> {
                // The schemas directory contains a schema file for each version of the Room database.
                // This is required to enable Room auto migrations.
                // See https://developer.android.com/reference/kotlin/androidx/room/AutoMigration.
                schemaDirectory("$projectDir/schemas")
            }

            dependencies {
                add("implementation", libs.findLibrary("androidx.room.runtime").get())
                add("annotationProcessor", libs.findLibrary("androidx.room.compiler").get())
                add("ksp", libs.findLibrary("androidx.room.compiler").get())
            }
        }
    }
}
