# CameraScan

[![Download](https://img.shields.io/badge/download-App-blue.svg)](https://raw.githubusercontent.com/jenly1314/CameraScan/master/app/release/app-release.apk)
[![MavenCentral](https://img.shields.io/maven-central/v/com.github.jenly1314/camera-scan)](https://repo1.maven.org/maven2/com/github/jenly1314/camera-scan)
[![JitPack](https://jitpack.io/v/jenly1314/CameraScan.svg)](https://jitpack.io/#jenly1314/CameraScan)
[![CircleCI](https://circleci.com/gh/jenly1314/CameraScan.svg?style=svg)](https://circleci.com/gh/jenly1314/CameraScan)
[![API](https://img.shields.io/badge/API-21%2B-blue.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![License](https://img.shields.io/badge/license-Apche%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

CameraScan for Android 是一个简化扫描识别流程的通用基础库。

**CameraScan** 本身并不提供具体的分析识别功能，只是将相机预览、扫描分析等过程进行抽象分离，从而简化扫描识别功能的实现，你只需将 **CameraScan** 作为基础库，然后实现具体的识别业务，便可快速实现各种扫描识别相关功能。

> 如果你之前有使用过 [ZXingLite](https://github.com/jenly1314/ZXingLite) 、[MLKit](https://github.com/jenly1314/MLKit) 或 [WeChatQRCode](https://github.com/jenly1314/WeChatQRCode)，可能会对 **CameraScan** 比较眼熟。现已将 **CameraScan** 独立出来，便于后续统一维护。

> 以后 [ZXingLite](https://github.com/jenly1314/ZXingLite)、[MLKit](https://github.com/jenly1314/MLKit)、[WeChatQRCode](https://github.com/jenly1314/WeChatQRCode) 发布的新版本都将使用 **CameraScan** 作为相机扫描基础库。

> [**CameraScan**](https://github.com/jenly1314/CameraScan) + [**ViewfinderView**](https://github.com/jenly1314/ViewfinderView) +（[ZXingLite](https://github.com/jenly1314/ZXingLite)、[MLKit](https://github.com/jenly1314/MLKit)、[WeChatQRCode](https://github.com/jenly1314/WeChatQRCode) 其中之一） = 完美搭配。

## 引入

### Gradle:

1. 在Project的 **build.gradle** 或 **setting.gradle** 中添加远程仓库

    ```gradle
    repositories {
        //...
        mavenCentral()
    }
    ```

2. 在Module的 **build.gradle** 里面添加引入依赖项

    ```gradle
    implementation 'com.github.jenly1314:camera-scan:1.2.0'
    ```

### 温馨提示

#### 关于CameraScan版本与编译的SDK版本要求

> 使用 **v1.1.x** 以上版本时，要求 **compileSdkVersion >= 34**

> 使用 **v1.0.x** 以上版本时，要求 **compileSdkVersion >= 33**

## 使用

### 快速实现扫描识别主要有以下几种方式：

> 1、通过继承 **BaseCameraScanActivity** 或者 **BaseCameraScanFragment** 或其子类，可快速实现扫描识别。
> （适用于大多场景，自定义布局时需覆写 **getLayoutId** 方法）

> 2、在你项目的Activity或者Fragment中实例化一个 **BaseCameraScan**。（适用于想在扫描界面写交互逻辑，又因为项目
> 架构或其它原因，无法直接或间接继承 **BaseCameraScanActivity** 或 **BaseCameraScanFragment** 时使用）

> 3、继承 **CameraScan** 自己实现一个，可参照默认实现类 **BaseCameraScan**，其他步骤同方式2。（高级用法，谨慎使用）

### 关于 CameraScan

**CameraScan** 作为相机扫描的（核心）基类；所有与相机扫描相关的都是基于此类来直接或间接进行控制的。

### 关于 CameraConfig

主要是相机相关的配置；如：摄像头的前置后置、相机预览相关、图像分析相关等配置。

> CameraConfig中提供的可配置项：`CameraSelector.Builder`、`Preview.Builder`、`ImageAnalysis.Builder`；

> 你可以直接库中内置实现的相机配置： **CameraConfig** 、**AspectRatioCameraConfig** 和 **ResolutionCameraConfig**。

#### 这里简单说下各自的特点：

* **CameraConfig**：CameraX默认的相机配置。
* **AdaptiveCameraConfig**：自适应相机配置：主要是根据纵横比和设备屏幕的分辨率找到与相机之间合适的相机配置（v1.1.0新增）
* **AspectRatioCameraConfig**：根据纵横比配置相机，使输出分析的图像尽可能的接近屏幕的比例
* **ResolutionCameraConfig**：根据尺寸配置相机的目标图像大小，使输出分析的图像的分辨率尽可能的接近屏幕尺寸

> 你也可以自定义或覆写 **CameraConfig** 中的 **options** 方法，根据需要定制配置。

这里特别温馨提示：默认配置在未配置相机的目标分析图像大小时，会优先使用：横屏：640 * 480 竖屏：480 * 640；

根据这个图像质量顺便说下默认配置的优缺点：

* 优点：因为图像质量不高，所以在低配置的设备上使用也能hold住，这样就能尽可能的适应各种设备；
* 缺点：正是由于图像质量不高，从而可能会对检测识别率略有影响，比如在某些机型上体验欠佳。
* 结论：在适配、性能与体验之间得有所取舍，找到平衡点。

基于上面的配置优缺点特性，CameraScan内部默认会通过 **CameraConfigFactory** 来获取适合当前设备的的相机配置：CameraConfig；如果内部默认配置不满足你的需求，你也可以根据具体需求去设置CameraScan的CameraConfig；

如：通过CameraConfig设置前置摄像头

**方式一：** 通过工厂类创建CameraConfig（**推荐**）
```java
// 通过工厂类创建适合当前设备的CameraConfig
CameraConfig cameraConfig = CameraConfigFactory.createDefaultCameraConfig(this, CameraSelector.LENS_FACING_FRONT);
getCameraScan().setCameraConfig(cameraConfig);
```

**方式二：** 通过对象创建CameraConfig
```java
// 此处的CameraConfig可以是CameraConfig的任意子类（其它CameraConfig相关配置修改方式与设置前置摄像头类似）
CameraConfig cameraConfig = new CameraConfig() {
    @NonNull
    @Override
    public CameraSelector options(@NonNull CameraSelector.Builder builder) {
        builder.requireLensFacing(CameraSelector.LENS_FACING_FRONT);
        return super.options(builder);
    }
};

getCameraScan().setCameraConfig(cameraConfig);
```

> 需要注意的是：`cameraScan.setCameraConfig(cameraConfig)` 一般是在相机启动预览之前调用的，如果是在相机启动预览之后修改了CameraConfig的配置，则需重新调用`cameraScan.startCamera()`后，CameraConfig相关配置才会生效。

### 关于 **Analyzer**

**Analyzer** 为定义的分析器接口；主要用于分析相机预览的帧数据，从而得到具体的结果。一般实现某种扫描识别功能时，你只需自定义并实现一个 **Analyzer** 对应的功能即可。

### 关于 **BaseCameraScanActivity** 和 **BaseCameraScanFragment**

**BaseCameraScanActivity** 和 **BaseCameraScanFragment** 作为扫描预览界面的基类，主要目的是便于快速实现扫描识别。

> 扫描预览界面内部持有 **CameraScan**，并处理了 **CameraScan** 的初始化（如：相机权限、相机预览、生命周期等细节）

### CameraScan配置示例

**CameraScan** 里面包含部分支持链式调用的方法，即调用返回是 **CameraScan** 本身的一些配置建议在调用 **startCamera()** 方法之前调用。

> 如果是通过继承 **BaseCameraScanActivity** 或者 **BaseCameraScanFragment** 或其子类实现的相机扫描，可以在
**initCameraScan()** 方法中根据需要修改CameraScan相关配置。

示例：

```java
// 根据需要修改CameraScan相关配置
cameraScan.setPlayBeep(true)//设置是否播放音效，默认为false
    .setVibrate(true)//设置是否震动，默认为false
    .setCameraConfig(CameraConfigFactory.createDefaultCameraConfig(this, CameraSelector.LENS_FACING_BACK))//设置相机配置信息
    .setNeedTouchZoom(true)//支持多指触摸捏合缩放，默认为true
    .setDarkLightLux(45f)//设置光线足够暗的阈值（单位：lux），需要通过{@link #bindFlashlightView(View)}绑定手电筒才有效
    .setBrightLightLux(100f)//设置光线足够明亮的阈值（单位：lux），需要通过{@link #bindFlashlightView(View)}绑定手电筒才有效
    .bindFlashlightView(ivFlashlight)//绑定手电筒，绑定后可根据光线传感器，动态显示或隐藏手电筒按钮
    .setOnScanResultCallback(this)//设置扫描结果回调，需要自己处理或者需要连扫时，可设置回调，自己去处理相关逻辑
    .setAnalyzeImage(true);//设置是否分析图片，默认为true。如果设置为false，相当于关闭了扫描识别功能

```

启动相机预览
```java
// 启动预览（如果是通过直接或间接继承BaseCameraScanActivity或BaseCameraScanFragment实现的则无需调用startCamera）
getCameraScan().startCamera();
```

控制闪光灯
```java
// 设置闪光灯（手电筒）是否开启,需在startCamera之后调用才有效
getCameraScan().enableTorch(torch);
```

设置分析器
```java
// 设置分析器
cameraScan.setAnalyzer(analyzer);
```
> 如果是通过直接或间接继承 **BaseCameraScanActivity** 来实现的，可直接覆写 `createAnalyzer` 方法返回具体的分析器即可。

* 看到这里，你对 **CameraScan** 相关配置应该有了基本的了解，**CameraScan** 作为一个简化扫描识别流程的通用基础库，将通用的预览分析流程，进行尽可能的抽象分离，然后实现公共部分；将具体分析功能进行抽象；基于**CameraScan** 你只需实现一个**Analyzer**，并根据需要进行**CameraScan**相关配置，就可以快速实现具体的扫描检测功能。

> 介绍完 **CameraScan** 相关配置后，下面就来说下界面和布局部分；

### 界面示例

通过继承**BaseCameraScanActivity**可快速实现具体的扫描功能；（**BaseCameraScanFragment**与之类似）

示例：
```kotlin
// 此处的泛型*表示分析得到的具体结果
class CameraScanActivity : BaseCameraScanActivity<*>() {
    /**
     * 初始化CameraScan
     */
    override fun initCameraScan(cameraScan: CameraScan<*>) {
        super.initCameraScan(cameraScan)
        // TODO 根据需要初始化CameraScan相关配置
        cameraScan.setPlayBeep(true)
    }

    /**
     * 布局ID；通过覆写此方法可以自定义布局
     *
     * @return 布局ID
     */
    override fun getLayoutId(): Int {
        return super.getLayoutId()
    }

    override fun onScanResultCallback(result: AnalyzeResult<*>) {
        // TODO 扫描结果回调；分析后得到的结果
    }

    override fun createAnalyzer(): Analyzer<*>? {
        // TODO 创建分析器；由具体的分析器去实现分析检测功能
        return null
    }
}

```

> 一般在 [ZXingLite](https://github.com/jenly1314/ZXingLite) 、[MLKit](https://github.com/jenly1314/MLKit) 或 [WeChatQRCode](https://github.com/jenly1314/WeChatQRCode) 对应的库中都有提供 **BaseCameraScanActivity** 的子类，可快速实现具体的分析识别功能。（比如：[MLKit](https://github.com/jenly1314/MLKit)中的条码扫描有：[BarcodeCameraScanActivity](https://github.com/jenly1314/MLKit/blob/master/mlkit-barcode-scanning/src/main/java/com/king/mlkit/vision/barcode/BarcodeCameraScanActivity.java)）

### 布局示例

**PreviewView** 用来预览，布局内至少要保证有 **PreviewView** （必须的）；如果是继承 **BaseCameraScanActivity** 或
**BaseCameraScanFragment** 或其子类实现的相机扫描；快速实现扫描功能；
需自定义布局时，通过覆写`getLayoutId`方法即可；预览控件ID可覆写`getPreviewViewId`方法自定义；

**ivFlashlight** 是布局内置的手电筒（非必须的），如果是继承 **BaseCameraScanActivity** 或 **BaseCameraScanFragment**，其内部已默认处理了手电筒点击开关事件；控件id可覆写`getFlashlightId`方法自定义，默认为 **ivFlashlight**。返回0表示无需内置手电筒。您也可以自己去定义

更多CameraScan相关用法可查看**BaseCameraScanActivity**源码或参见下面的使用示例。


BaseCameraScanActivity和BaseCameraScanFragment使用的默认布局：
```Xml

<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <androidx.camera.view.PreviewView
        android:id="@+id/previewView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
    <!-- 只需保证有布局内有PreviewView即可，然后你可根据需要添加的控件 -->

    <ImageView
        android:id="@+id/ivFlashlight"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:layout_marginTop="@dimen/camera_scan_flashlight_margin_top"
        android:contentDescription="@null"
        android:src="@drawable/camera_scan_flashlight_selector" />
</FrameLayout>

```

自定义布局示例：（通过覆写`getLayoutId`方法）
```Xml

<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <androidx.camera.view.PreviewView
        android:id="@+id/previewView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
    <!-- 只需保证有布局内有PreviewView即可，然后你可根据需要添加的控件 -->
    <com.king.view.viewfinderview.ViewfinderView
        android:id="@+id/viewfinderView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
    <ImageView
        android:id="@+id/ivFlashlight"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:layout_marginTop="@dimen/camera_scan_flashlight_margin_top"
        android:contentDescription="@null"
        android:src="@drawable/camera_scan_flashlight_selector" />
</FrameLayout>

```

> 如上面的自定布局，使用了开源库：[**ViewfinderView**](https://github.com/jenly1314/ViewfinderView)；一般在实现扫码相关需求时，会经常用到。

> 页面完整示例：[CameraScanActivity](app/src/main/java/com/king/camera/scan/app/CameraScanActivity.kt)