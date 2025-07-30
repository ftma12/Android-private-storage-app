plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.privatefilestorageapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.privatefilestorageapp"
        minSdk = 24
        targetSdk = 35
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
}
var cameraxVersion = "1.1.0-alpha05"


dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // Biometric API dependency
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("com.google.android.material:material:1.10.0")
    //for the dashboard grid view
    implementation("com.google.android.material:material:1.11.0")
    implementation ("androidx.gridlayout:gridlayout:1.0.0")
//for the camera
    implementation("androidx.camera:camera-core:${cameraxVersion}")
    implementation("androidx.camera:camera-camera2:${cameraxVersion}")
    implementation("androidx.camera:camera-lifecycle:${cameraxVersion}")
    implementation("androidx.camera:camera-view:1.0.0-alpha25")
    //for the searchbr
    implementation ("com.google.android.material:material:1.8.0") // or newer version


}


