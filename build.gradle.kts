// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}

// Redirect build directory outside of OneDrive to avoid file locking issues
allprojects {
    layout.buildDirectory.set(file("C:/temp/gradle-builds/PAN_CASA/${project.name}"))
}
