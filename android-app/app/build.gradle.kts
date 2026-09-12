import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.ibrahimtemur.macremote"
    compileSdk = 36

    val rootVersionFile = rootDir.resolve("../VERSION")
    val appVersionName = if (rootVersionFile.exists()) rootVersionFile.readText().trim() else "1.0.0"

    defaultConfig {
        applicationId = "com.ibrahimtemur.macremote"
        minSdk = 26
        targetSdk = 36
        versionCode = 6
        versionName = appVersionName

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    val localProperties = Properties().apply {
        val localPropsFile = rootDir.resolve("local.properties")
        if (localPropsFile.exists()) {
            localPropsFile.inputStream().use { inputStream ->
                load(inputStream)
            }
        }
    }

    signingConfigs {
        create("release") {
            val envKeystore = System.getenv("ANDROID_KEYSTORE_FILE")
            val propKeystore = localProperties.getProperty("RELEASE_STORE_FILE")
            val defaultRootKeystore = rootDir.resolve("../upload-keystore.jks")

            val resolvedKeystore = when {
                !envKeystore.isNullOrEmpty() && file(envKeystore).exists() -> file(envKeystore)
                !propKeystore.isNullOrEmpty() && file(propKeystore).exists() -> file(propKeystore)
                defaultRootKeystore.exists() -> defaultRootKeystore
                else -> null
            }

            val storePass = System.getenv("ANDROID_KEYSTORE_PASSWORD") ?: localProperties.getProperty("RELEASE_STORE_PASSWORD")
            val keyUsr = System.getenv("ANDROID_KEY_ALIAS") ?: localProperties.getProperty("RELEASE_KEY_ALIAS") ?: "upload"
            val keyPass = System.getenv("ANDROID_KEY_PASSWORD") ?: localProperties.getProperty("RELEASE_KEY_PASSWORD") ?: storePass

            if (resolvedKeystore != null && !storePass.isNullOrEmpty()) {
                storeFile = resolvedKeystore
                storePassword = storePass
                keyAlias = keyUsr
                keyPassword = keyPass
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile != null && releaseSigning.storeFile!!.exists()) {
                signingConfig = releaseSigning
            }
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    
    // OkHttp for WebSockets
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // Gson for JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")
}
