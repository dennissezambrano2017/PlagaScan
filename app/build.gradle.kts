plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.plagascan"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.plagascan"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        mlModelBinding = true
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("com.google.mediapipe:tasks-vision:latest.release")

    implementation("me.relex:circleindicator:2.1.6")
    implementation("org.imaginativeworld.whynotimagecarousel:whynotimagecarousel:2.1.0")
    implementation("com.codesgood:justifiedtextview:1.1.0")
    implementation ("androidx.camera:camera-view:1.3.0-alpha06")
    implementation ("com.github.jose-jhr:Library-CameraX:1.0.8")

    // CameraX core library
    implementation ("androidx.camera:camera-core:1.1.0")

    // CameraX Camera2 extensions
    implementation ("androidx.camera:camera-camera2:1.1.0")

    // CameraX Lifecycle library
    implementation ("androidx.camera:camera-lifecycle:1.1.0")

    // CameraX View class
    implementation ("androidx.camera:camera-view:1.1.0")

    implementation ("androidx.cardview:cardview:latest.release")

}