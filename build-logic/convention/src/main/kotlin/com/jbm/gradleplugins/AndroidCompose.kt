package com.jbm.gradleplugins

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension<*, *, *, *, *>,
) {
    commonExtension.apply {
        buildFeatures {
            compose = true
        }

        composeOptions {
            kotlinCompilerExtensionVersion =
                libs.findVersion("kotlinCompilerExtension").get().toString()
        }
    }

    dependencies {
        val composeBom = libs.findLibrary("androidx.compose.bom").get()
        add("implementation", platform(composeBom))
        add("implementation", libs.findLibrary("androidx.activity.compose").get())
        add("implementation", libs.findLibrary("androidx.compose.ui").get())
        add("implementation", libs.findLibrary("androidx.compose.uiGraphic").get())
        add("implementation", libs.findLibrary("androidx.compose.uiToolingPreview").get())
        add("implementation", libs.findLibrary("androidx.compose.material3").get())
        add("implementation", libs.findLibrary("androidx.navigation.compose").get())
        add("implementation", libs.findLibrary("androidx.compose.constrainLayout").get())
        add("implementation", libs.findLibrary("androidx.lifecycle.runtimeCompose").get())

        add("debugImplementation", libs.findLibrary("androidx.compose.ui.tooling").get())
        add("debugImplementation", libs.findLibrary("androidx.compose.ui.testManifest").get())
    }
}
