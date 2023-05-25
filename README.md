# SPICE-Android

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Medtronic-LABS_spice-android&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Medtronic-LABS_spice-android)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Medtronic-LABS_spice-android&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=Medtronic-LABS_spice-android)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Medtronic-LABS_spice-android&metric=bugs)](https://sonarcloud.io/summary/new_code?id=Medtronic-LABS_spice-android)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Medtronic-LABS_spice-android&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=Medtronic-LABS_spice-android)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Medtronic-LABS_spice-android&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=Medtronic-LABS_spice-android)

## Prerequisite
- To bring up the Spice backend server, there are a few prerequisites that need to be completed. Please follow the instructions provided in this [link](https://github.com/Medtronic-LABS/spice-server.git). Once you have completed the steps, you will get a ***SERVER URL*** to use in our Application.

## Tools used

```sh
Android Studio IDE
```
## Tech stack used

```sh
Kotlin-1.9.0
Java-18
Android MVVM Design Pattern
Gradle-7.3.1
```

## Installation Steps

#### Download and Install Android Studio
- You can download Android Studio from [here](https://developer.android.com/studio/install)

- Next, check your Java version by running the command ***java -version*** in your terminal.
  ```
  $ java –version
  ```
  If Java 18 is not installed, you can follow these steps:
    * Visit the JDK downloads page using this [link](https://www.oracle.com/java/technologies/javase/jdk18-archive-downloads.html).
    * Install Java 18 according to the provided instructions.
      </br>

- To change the Java version in Android Studio,
    * Open your Android Studio project.
    * Click on "File" in the top menu.
    * Select "Project Structure" from the dropdown menu.
    * In the left panel, select "SDK Location".
    * Under the "JDK Location" section, click on the dropdown menu next to "JDK location" and select the path to the desired Java version.
    * Click "Apply" and then "OK" to save the changes.
    * You may need to re-build your project by selecting "Build" -> "Rebuild Project".</br>

#### Download and Install Git.
To check the Git version, you can run the following command in your terminal:
```sh
git --version
```

If Git is not installed, you can follow the instructions below based on your operating system:
##### Ubuntu:
To install Git on Ubuntu, run the following command in your terminal or click on [Git Official site](https://git-scm.com/download/linux).

```sh
$ sudo apt install git
```
##### Windows:
For Windows, you can visit the [Git Official site](https://git-scm.com/download/win) and download the Git installer.

## Download Source code
Once you have Git installed, you can clone the Spice open source repository by running the following command in your terminal:

```sh
git clone https://github.com/Medtronic-LABS/spice-android.git
```
</br>
After cloning the repository, you can open the folder in Android Studio.

## Configuration
To execute the application, you must access the ***build.gradle*** file located in the app-level directory of your project.
```
build.gradle[App-level]
```

This file allows you to define your desired values for specific properties.

### Configure Server URL
Use the server URL obtained from the prerequisites mentioned above. Or, you may also follow the steps as mentioned in the [link](https://github.com/Medtronic-LABS/spice-server.git).
```server
    server = [debug: "http://localhost:8762/", release: "http://localhost:8762/"]
````
Substitute ***http://localhost:8762/*** with the server URL obtained.

### Configure Salt key
To enhance security, you may need to provide the salt key used in the backend for user authentication. The salt key is a randomly generated string that adds an extra layer of security when hashing passwords.
```Salt key
    salt = [debug: "spice_opensource", release: "spice_opensource"]
````

By default, the Salt key is set as spice_opensource, but you can modify it if necessary. Please note that the Salt key must match the key used in the backend.
### Signing APK/Bundle
`SigningConfigs`  configuration in the app's build.gradle file is used to specify the signing information for your Android application. It allows you to configure the necessary details for signing your app with a digital certificate, such as the keystore file, key alias, and key passwords.
```
    signingConfigs {
        development {
            keyAlias '<Specify the alias used during the generation of the signed APK/Bundle>'
            keyPassword '<Specify the key password used during the generation of the signed APK/Bundle>'
            storeFile file('<Specify the key store path used during the generation of the signed APK/Bundle'>)
            storePassword '<Specify the store password used during the generation of the signed APK/Bundle>'
        }
    }
```
### Complete build.gradle look like this:
```properties
plugins {
    id 'com.android.application'
    id 'kotlin-android'
    id 'kotlin-kapt'
    id 'dagger.hilt.android.plugin'
    id 'kotlin-parcelize'
    id "org.sonarqube" version "3.4.0.2513"
    id 'org.jetbrains.kotlin.android'
}

android {
    compileSdk 33

    defaultConfig {
        applicationId "com.medtroniclabs.opensource"
        minSdk 23
        targetSdk 33
        versionCode 1
        versionName "1.0.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        development {
            keyAlias '<Specify the alias used during the generation of the signed APK/Bundle>'
            keyPassword '<Specify the key password used during the generation of the signed APK/Bundle>'
            storeFile file('<Specify the key store path used during the generation of the signed APK/Bundle'>)
            storePassword '<Specify the store password used during the generation of the signed APK/Bundle>'
        }
        release {
            keyAlias '<Specify the alias used during the generation of the signed APK/Bundle>'
            keyPassword '<Specify the key password used during the generation of the signed APK/Bundle>'
            storeFile file('<Specify the key store path used during the generation of the signed APK/Bundle'>)
            storePassword '<Specify the store password used during the generation of the signed APK/Bundle>'
        }
    }

    buildTypes {
        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = '1.8'
    }

    buildFeatures {
        viewBinding true
    }

    lintOptions {
        checkReleaseBuilds false
    }

    packagingOptions {
        exclude 'META-INF/DEPENDENCIES'
    }

    flavorDimensions "version"

    productFlavors {

        development {
            dimension "version"
            applicationIdSuffix ".dev"
            ext {
                server = [debug: "http://localhost:8762/", release: "http://localhost:8762/"]
                admin = [debug: "{ADMIN.URL}",release: "{ADMIN.URL}"]
                salt = [debug: "salt_opensource", release: "salt_opensource"]
            }
            signingConfig signingConfigs.development
        }
        production {
            dimension "version"
            ext {
                server = [debug: "http://localhost:8762/", release: "http://localhost:8762/"]
                admin = [debug: "{ADMIN.URL}",release: "{ADMIN.URL}"]
                salt = [debug: "salt_opensource", release: "salt_opensource"]
            }
            signingConfig signingConfigs.release
        }
    }

    applicationVariants.all { variant ->
        def flavor = variant.productFlavors[0]
        println "Setting up server URL ${flavor.ext.server[variant.buildType.name]} for variant [${variant.name}]"
        variant.buildConfigField "String", "SERVER_URL", "\"${flavor.ext.server[variant.buildType.name]}\""
        variant.buildConfigField "String", "ADMIN_URL", "\"${flavor.ext.admin[variant.buildType.name]}\""
        variant.buildConfigField "String", "SALT", "\"${flavor.ext.salt[variant.buildType.name]}\""
        variant.outputs.each {
            output ->
                def formattedDate = new Date().format('yyyy-MM-dd-HH-mm')
                def name = "SPICE_${flavor.name}_${variant.buildType.name}_${formattedDate}.apk"
                output.outputFileName = name
        }
    }
    bundle{
        language{
            enableSplit = false
        }
    }
}

dependencies {

    implementation 'androidx.core:core-ktx:1.9.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.8.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'

    // Retrofit
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.10.0'

    implementation 'com.jakewharton.timber:timber:4.7.1'

    implementation "com.google.dagger:hilt-android:$hilt_version"
    implementation 'androidx.legacy:legacy-support-v4:1.0.0'
    implementation 'androidx.preference:preference-ktx:1.2.0'
    implementation 'androidx.core:core-ktx:1.9.0'
    kapt "com.google.dagger:hilt-compiler:$hilt_version"

    def room_version = '2.5.0'

    implementation "androidx.room:room-runtime:$room_version"
    annotationProcessor "androidx.room:room-compiler:$room_version"
    // To use Kotlin annotation processing tool (kapt)
    kapt "androidx.room:room-compiler:$room_version"
    implementation "androidx.room:room-ktx:$room_version"

    // Kotlin coroutine dependencies
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.6.0'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.0'

    implementation 'androidx.activity:activity-ktx:1.6.1'
    implementation 'androidx.fragment:fragment-ktx:1.5.5'

    //Glide
    implementation 'com.github.bumptech.glide:glide:4.12.0'

    implementation 'androidx.security:security-crypto:1.1.0-alpha05'

    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'

    // loading progress
    implementation 'com.github.ybq:Android-SpinKit:1.4.0'

    implementation 'com.robertlevonyan.view:MaterialChipView:2.2.6'

    implementation 'com.google.android.gms:play-services-location:21.0.1'

    implementation 'androidx.security:security-crypto:1.0.0'

    def lottieVersion = '4.2.2'
    implementation "com.airbnb.android:lottie:$lottieVersion"


    implementation 'net.zetetic:android-database-sqlcipher:4.5.2'
    implementation 'androidx.sqlite:sqlite-ktx:2.3.0'
    implementation 'com.github.jeffreyliu8:FlexBoxRadioGroup:0.0.8'
    implementation 'androidx.paging:paging-runtime-ktx:3.1.1'
    implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
    implementation 'com.google.android.play:integrity:1.1.0'
    implementation 'com.google.apis:google-api-services-playintegrity:v1-rev20220928-2.0.0'
    implementation 'org.bitbucket.b_c:jose4j:0.9.1'

    //in-app update
    implementation 'com.google.android.play:app-update:2.0.1'
    implementation 'com.google.android.play:app-update-ktx:2.0.1'
}
```
<font color = "#BA1016">`Synchronize the Gradle files by selecting File > Sync project. After that, you can click on the "Run" button.`</font>


### Localization

Currently, our application supports English and Bangla languages for most user roles. However, the following user roles are not included in this language support.

- PROVIDER
- PHYSICIAN_PRESCRIBER
- LAB_TECHNICIAN
- PHARMACIST
- NUTRITIONIST

To modify the language settings, you can easily do so by accessing the side menu on the home page and selecting the option labeled **Language Preference**. This action will trigger a dialog box to appear, allowing you to select either English or Bangla as your preferred language. Once you have made your selection, simply log in again to observe the changes. Please note that the default language is English.

## Optional
#### [Emulator Set up](https://developer.android.com/studio/run/managing-avds)
To install an app on an Android emulator in Android Studio you can follow these steps
- Open the Android Studio and launch the emulator that you want to use
- Click on the "Run" button or press the "Shift" + "F10" keys to open the Run Configuration dialog
- Select the app module that you want to run from the "Module" drop-down list
- Select the emulator that you want to use from the "Device" drop-down list
- Click on the "OK" button to run the app on the selected emulator
- Wait for the app to be installed and launched on the emulator. </br>

**Note:** Before running the app on the emulator, make sure that the emulator is properly configured and running. if not, follow the below instructions:

You can install an Android emulator in Android Studio by following these steps:
- Open Android Studio and click on the "AVD Manager" button in the toolbar or navigate to "Tools" -> "AVD Manager".
- Click on the "Create Virtual Device" button.
- Select a device definition from the list, or click on "New Hardware Profile" to create a custom device definition.
- Choose a system image to run on the emulator, and click "Download" if the image has not been previously downloaded.
- Click "Next", then customize any additional settings you wish to modify, such as the device name or the amount of RAM allocated to the emulator.
- Click "Finish" to create the virtual device.
- Once the virtual device is created, you can start it by selecting it from the AVD Manager and clicking the "Play" button.

After starting the emulator, you can install your app on it by running your project in Android Studio and selecting the emulator as the deployment target.
If you want to run apps on a hardware device,[follow the instructions.](https://developer.android.com/studio/run/device)

### Future Enhancement
- Provide Bangla Language support for all user roles.

This is the client-side application for SPICE, designed to help track hypertensive and diabetic patients across a population.
This repository contains the full android setup for the application. Please refer the SPICE webpage using the following URL:

[SPICE DOCUMENTATION](https://app.gitbook.com/o/RnePNEThd1XTpW5Hf3HB/s/7inBQ0zjo0nwpqK5625P/~/changes/16/deploy/deployment-guide/android)