plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.LoquoxLocation"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.LoquoxLocation"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation ("androidx.compose.material3:material3:1.2.0") // Última versión disponible
    implementation ("androidx.compose.material:material:1.6.0")   // Última versión si usas Material 2

    implementation ("androidx.compose.material:material-icons-extended:1.6.0")


    implementation("androidx.room:room-runtime:2.5.0")
    implementation(libs.androidx.benchmark.macro)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.play.services.maps)
    implementation(libs.androidx.preference.ktx)

    annotationProcessor("androidx.room:room-compiler:2.5.0")

    // Opcional: para usar coroutines con Room
    implementation("androidx.room:room-ktx:2.5.0")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.play.services.location)


    // Para osmdroid
    implementation ("org.osmdroid:osmdroid-android:6.1.15")


    implementation("com.google.accompanist:accompanist-permissions:0.28.0")
    implementation("androidx.navigation:navigation-compose:2.6.0")

    implementation("com.google.maps.android:maps-compose:2.12.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    implementation("io.coil-kt:coil-compose:2.5.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}