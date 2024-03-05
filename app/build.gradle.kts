plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.common.android.application)
    alias(libs.plugins.common.android.application.compose)
    alias(libs.plugins.common.android.hilt)
}

android {
    namespace = "com.jbm.video"

    defaultConfig {
        applicationId = "com.jbm.video"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":core"))

    // Androidx
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtimeKtx)
    implementation(libs.androidx.work.runtimeKtx)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
}
