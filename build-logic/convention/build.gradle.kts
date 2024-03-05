plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.android.tools.common)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplicationConventionPlugin") {
            id = "gradlePlugins.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("AndroidLibraryConventionPlugin") {
            id = "gradlePlugins.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidApplicationComposeConventionPlugin") {
            id = "gradlePlugins.android.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("androidLibraryComposeConventionPlugin") {
            id = "gradlePlugins.android.library.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("androidRoomConventionPlugin") {
            id = "gradlePlugins.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("androidHiltConventionPlugin") {
            id = "gradlePlugins.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }
    }
}
