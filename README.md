# Mapbox Vision AR SDK for Android

The Vision SDK uses highly efficient neural networks to process imagery directly on user’s
mobile or embedded devices, turning any connected camera into a second set of eyes
for your car. In doing so, the Vision SDK enables the following user-facing features:

- Augmented reality navigation with turn-by-turn directions
- Classification and display of regulatory and warning signs
- Object detection for vehicles, pedestrians, road signs, and traffic lights
- Semantic segmentation of the roadway into 12 different classes (roadway, painted roadway (e.g. crosswalk), lane boundaries, traffic lights, traffic signs, vehicles, cyclists, pedestrians, buildings, vegetation, sky, other)
- Distance detection that indicates spacing to lead vehicle

#### Components of the Vision SDK
There are three components to the Vision SDK: VisionCore, VisionSDK, and VisionAR.

VisionCore is the core logic of the system, including all machine learning models; it exists as compiled library for each platform with a user-facing API.

[VisionSDK](https://github.com/mapbox/mapbox-vision-android) is a framework written in native language (Kotlin for Android, Swift for iOS) that encapsulates core utilization and platform-dependent tasks. It calls VisionCore.

[VisionAR](https://github.com/mapbox/mapbox-vision-ar-android) is a native framework with dependency on the Mapbox Navigation SDK. It takes information from the specified navigation route, transfers it to VisionCore via VisionSDK, receives instructions on displaying the route, and then finally renders it on top of camera frame using native instruments.

#### Hardware requirements

VisionSDK requires Android 6 (API 23) and higher, with QC Snapdragon 650 // 710 // 8xx with Open CL support

Some of devices that will work with VisionSDK:
- Samsung Galaxy S8, S8+ // S9, S9+ // Note 8
- Xiaomi Mi 6 // 8
- HTC U11, U11+ // U12, U12+
- OnePlus 5 // 6

You can also check more details at [Vison SDK FAQ](https://vision.mapbox.com/faq).

## Setup

You can look for an example of complete integration in the [Mapbox Vision SDK Teaser repo](https://github.com/mapbox/mapbox-vision-android-examples).

1. Add the dependencies:

Add the following dependency to your project's `build.gradle`:

```
    implementation 'com.mapbox.vision:mapbox-android-vision:0.1.0-SNAPSHOT'
    implementation 'com.mapbox.vision:mapbox-android-vision-ar:0.1.0-SNAPSHOT'
```

and to your top-level `build.gradle`:

```
repositories {
     maven { url 'https://mapbox.bintray.com/mapbox' }
}
```

2. Set your [Mapbox access token](https://www.mapbox.com/help/how-access-tokens-work/):

```
class DemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        VisionManager.init(this, "<access token>")
    }
}
```

3. Setup permissions:

Mapbox Vision SDK will require the following list of permissions to work:

```
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

You should grant them all before calling the SDK.

4. Add VisionView to the activity layout:

GlArView will render the navigation route on top of camera image.

You can add it with the following snippet:

```
    <com.mapbox.vision.ar.view.gl.GlArView
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
```

5. Lifecycle methods:

You will need to call the following lifecycle methods of `VisionManager` from the activity or fragment containing VisionView:

```
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ...
    VisionManager.create()
}

override fun onResume() {
    super.onResume()
    ...
    VisionManager.start()
}

override fun onPause() {
    super.onPause()
    ...
    VisionManager.stop()
}

override fun onDestroy() {
    super.onDestroy()
    ...
    VisionManager.destroy()
}
```

6. Tie Mapbox Navigation SDK to Mapbox Vision AR:

To display AR details you will need to create MapboxNavigation, [the detailed instructions about
setup](https://www.mapbox.com/android-docs/navigation/overview/).

Then you'll need to launch created navigation and set GlArView as listener:

```
    mapboxNavigation.startNavigation(directionsRoute)
    mapboxNavigation.addProgressChangeListener(findViewById(R.id.gl_ar_view))
```

To stop navigation and remove listener afterwards:

```
    mapboxNavigation.removeProgressChangeListener(mapbox_ar_view)
    mapboxNavigation.stopNavigation()
```

Also, this flow is covered in
[ArMapActivity](https://github.com/mapbox/mapbox-vision-android-examples/blob/master/app/src/main/java/com/mapbox/vision/examples/screens/ar/ArMapActivity.kt) and
[ArNavigationActivity](https://github.com/mapbox/mapbox-vision-android-examples/blob/master/app/src/main/java/com/mapbox/vision/examples/screens/ar/ArNavigationActivity.kt)
 of our sample Teaser app.
