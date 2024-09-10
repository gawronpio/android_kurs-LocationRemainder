plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.locationremainder"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.locationremainder"
        minSdk = 28
        targetSdk = 34
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        dataBinding = true
    }
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")

    }

    dependencies {
        ksp(libs.androidx.room.compiler)

        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.appcompat)
        implementation(libs.material)
        implementation(libs.androidx.activity)
        implementation(libs.androidx.constraintlayout)
        implementation(libs.androidx.databinding.runtime)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.lifecycle.viewmodel.ktx)
        implementation(libs.androidx.navigation.fragment.ktx)
        implementation(libs.androidx.navigation.ui.ktx)
        implementation(libs.androidx.navigation.runtime.ktx)
        implementation(libs.androidx.room.runtime)
        implementation(libs.androidx.room.ktx)
        implementation(libs.androidx.room.gradle.plugin)
        implementation(libs.androidx.work.runtime.ktx)
        implementation(libs.firebase.auth.ktx)
        implementation(platform(libs.firebase.bom))
        implementation(libs.firebase.ui.auth)
        implementation(libs.google.services)

        testImplementation(libs.junit)

        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
    }
}