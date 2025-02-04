plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("io.realm.kotlin") version "3.0.0"
    id ("dagger.hilt.android.plugin")
    kotlin("kapt") version "1.8.21"
}

android {
    namespace = "com.example.mealplannerapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mealplannerapp"
        minSdk = 24
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
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {


    implementation ("io.realm.kotlin:library-base:3.0.0")



    // Dagger Core
    implementation ("com.google.dagger:dagger:2.55")
    kapt ("com.google.dagger:dagger-compiler:2.55")

// Dagger Android
    api ("com.google.dagger:dagger-android:2.55")
    api("com.google.dagger:dagger-android-support:2.55")
    kapt ("com.google.dagger:dagger-android-processor:2.55")

// Dagger - Hilt
    implementation ("com.google.dagger:hilt-android:2.55")
    kapt ("com.google.dagger:hilt-android-compiler:2.55")
    implementation ("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}