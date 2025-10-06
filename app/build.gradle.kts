plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.physioarv5"
    compileSdk = 34 // 34 ก็พอ ถ้าอยาก 36 ได้เช่นกัน

    defaultConfig {
        applicationId = "com.example.physioarv5"
        minSdk = 24      // SceneView + Filament ต้อง >= 24
        targetSdk = 34   // หรือ 36 ได้
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

    // SceneView/Filament ต้องใช้ Java 17
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures { compose = true }

    composeOptions {
        // ถ้าไม่มี libs.versions ระบุไว้ ก็ไม่ต้องตั้งเวอร์ชันนี้
        // kotlinCompilerExtensionVersion = "..."
    }
}

dependencies {
    // ---------- Compose (ใช้ BOM เดียวให้หมด) ----------
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // พื้นฐาน AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // CameraX (เดิมของโปรเจ็กต์)
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")

    // MediaPipe (เดิมของโปรเจ็กต์)
    implementation("com.google.mediapipe:tasks-vision:0.10.14")

    // ---------- SceneView (AR + Compose Wrapper) ----------
    implementation("io.github.sceneview:arsceneview:2.4.2")
    implementation("io.github.sceneview:sceneview-compose:2.4.2")
    // (ไม่ต้องใส่ 2 เวอร์ชันซ้ำ/ชนกัน และไม่ต้องใส่ sceneview ตัวอื่นเพิ่ม)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
