plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Use emulator host mapping (10.0.2.2) so Android emulator can reach the backend on the development machine.
        // If you're testing on a real device, replace with your host machine IP e.g. "http://192.168.x.y:8000/api/techbyte/"
        buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8000/api/techbyte/\"")
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
        buildConfig = true
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    packaging {
        resources {
            // Khi gặp file trùng thì chọn cái đầu tiên nó tìm thấy
            pickFirsts.add("META-INF/androidx.cardview_cardview.version")
        }
    }
    sourceSets {
        getByName("main") {
            res.srcDirs(
                "src/main/res/layout_auth",
                "src/main/res/layout_home",
                "src/main/res/layout_profile",
                "src/main/res/layout_article",
                "src/main/res"
            )
        }
    }

}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.recyclerview:recyclerview-selection:1.2.0")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation(libs.firebase.auth)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation(libs.play.services.ads)
    implementation(libs.cardview)
    implementation(libs.cardview.v7)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.glide)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}