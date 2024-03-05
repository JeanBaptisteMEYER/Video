import com.jbm.gradleplugins.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("dagger.hilt.android.plugin")
                apply("com.google.devtools.ksp")
            }

            dependencies {

                //implementation(libs.androidx.hilt.navigationCompose)
                "implementation"(libs.findLibrary("dagger.hilt.android").get())
                "implementation"(libs.findLibrary("androidx.hilt.navigationCompose").get())
                "ksp"(libs.findLibrary("dagger.hilt.compiler").get())
            }
        }
    }
}
