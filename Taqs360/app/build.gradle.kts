import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.taqs360"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.taqs360"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "WEATHER_API_KEY", "\"${getApiKey()}\"")
        buildConfigField("String", "BUILD_TIME", "\"${System.currentTimeMillis()}\"")
        externalNativeBuild {
            cmake {
                cppFlags += ""
            }
        }
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

fun getApiKey(): String {
    return try {
        val properties = Properties()
        val localProperties = File(rootProject.projectDir, "local.properties")
        if (localProperties.exists()) {
            FileInputStream(localProperties).use {
                properties.load(it)
            }
        }
        properties.getProperty("WEATHER_API_KEY") ?: throw GradleException("API key not found in local.properties")
    } catch (e: Exception) {
        throw GradleException("Failed to load API key: ${e.message}")
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

    // UI Components
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.airbnb.android:lottie:6.6.6")

    // Google Services
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    // Room Database
    kapt("androidx.room:room-compiler:2.6.1")
    implementation ("androidx.room:room-ktx:2.6.1")
    implementation ("androidx.room:room-runtime:2.6.1")

    // osmdroid for OpenStreetMap
    implementation("org.osmdroid:osmdroid-android:6.1.10")
    implementation("org.slf4j:slf4j-api:2.0.9")

    // Fragment for MapFragment
    implementation("androidx.fragment:fragment-ktx:1.6.2")

        implementation ("com.luckycatlabs:SunriseSunsetCalculator:1.2")
    // WorkManager
    implementation ("androidx.work:work-runtime-ktx:2.9.0")
        implementation ("com.google.android.material:material:1.12.0")

    implementation("com.github.prolificinteractive:material-calendarview:2.0.0")
    implementation("com.jakewharton.threetenabp:threetenabp:1.4.4")

    implementation ("com.wdullaer:materialdatetimepicker:4.2.3")
    implementation ("androidx.navigation:navigation-fragment-ktx:2.8.0")
    implementation ("androidx.navigation:navigation-ui-ktx:2.8.0")

    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

}