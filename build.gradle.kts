// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    //noinspection GradleDependency
    id("com.google.devtools.ksp") version "2.0.10-1.0.24" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}
buildscript {
    dependencies {
        classpath(libs.androidx.androidx.navigation.safeargs.gradle.plugin)
        classpath(libs.androidx.room.gradle.plugin)
    }
}