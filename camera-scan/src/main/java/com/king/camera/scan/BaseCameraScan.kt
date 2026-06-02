/*
 * Copyright (C) Jenly, CameraScan Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.king.camera.scan

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.view.MotionEvent
import android.view.View
import androidx.annotation.FloatRange
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.MeteringPoint
import androidx.camera.core.Preview
import androidx.camera.core.ResolutionInfo
import androidx.camera.core.TorchState
import androidx.camera.core.ZoomState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.google.common.util.concurrent.ListenableFuture
import com.king.camera.scan.analyze.Analyzer
import com.king.camera.scan.config.CameraConfig
import com.king.camera.scan.config.CameraConfigFactory
import com.king.camera.scan.internal.ZoomGestureDetector
import com.king.camera.scan.manager.AmbientLightManager
import com.king.camera.scan.manager.BeepManager
import com.king.logx.LogX
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

/**
 * 相机扫描基类；[BaseCameraScan] 为 [CameraScan] 的默认实现
 * <p>
 * 快速实现扫描识别主要有以下几种方式：
 * <p>
 * 1、通过继承 [BaseCameraScanActivity]或者[BaseCameraScanFragment]或其子类，可快速实现扫描识别。
 * （适用于大多数场景，自定义布局时需覆写getLayoutId方法）
 * <p>
 * 2、在你项目的Activity或者Fragment中实例化一个[BaseCameraScan]。（适用于想在扫描界面写交互逻辑，又因为项目
 * 架构或其它原因，无法直接或间接继承[BaseCameraScanActivity]或[BaseCameraScanFragment]时使用）
 * <p>
 * 3、继承[CameraScan]自己实现一个，可参照默认实现类[BaseCameraScan]，其他步骤同方式2。（高级用法，谨慎使用）
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
class BaseCameraScan<T> : CameraScan<T> {

    companion object {
        /**
         * Defines the maximum duration in milliseconds between a touch pad
         * touch and release for a given touch to be considered a tap (click) as
         * opposed to a hover movement gesture.
         */
        private const val HOVER_TAP_TIMEOUT = 150

        /**
         * Defines the maximum distance in pixels that a touch pad touch can move
         * before being released for it to be considered a tap (click) as opposed
         * to a hover movement gesture.
         */
        private const val HOVER_TAP_SLOP = 20

        /**
         * 每次缩放改变的步长
         */
        private const val ZOOM_STEP_SIZE = 0.1F
    }

    private val mContext: Context
    private val mLifecycleOwner: LifecycleOwner

    /**
     * 预览视图
     */
    private val mPreviewView: PreviewView

    private var mExecutorService: ExecutorService? = null

    private var mZoomGestureDetector: ZoomGestureDetector? = null

    private var mCameraProvider: ProcessCameraProvider? = null

    /**
     * 相机会话token；用于丢弃过期的异步启动回调
     */
    private val mCameraSessionToken = AtomicInteger(0)

    /**
     * 是否已释放
     */
    @Volatile
    private var isReleased: Boolean = false

    /**
     * 相机
     */
    private var mCamera: Camera? = null

    /**
     * 相机配置
     */
    private var mCameraConfig: CameraConfig? = null

    /**
     * 分析器
     */
    private var mAnalyzer: Analyzer<T>? = null

    /**
     * 是否分析
     */
    @Volatile
    private var isAnalyze: Boolean = true

    /**
     * 是否自动停止分析
     */
    @Volatile
    private var isAutoStopAnalyze: Boolean = true

    /**
     * 是否已经分析出结果
     */
    @Volatile
    private var isAnalyzeResult: Boolean = false

    /**
     * 闪光灯（手电筒）视图
     */
    private var flashlightView: View? = null

    /**
     * 分析结果
     */
    private var mResultLiveData: MutableLiveData<AnalyzeResult<T>?>? = null

    /**
     * 扫描结果回调
     */
    private var mOnScanResultCallback: OnScanResultCallback<T>? = null

    /**
     * 分析监听器
     */
    private var mOnAnalyzeListener: Analyzer.OnAnalyzeListener<T>? = null

    /**
     * 音效管理器：主要用于播放蜂鸣提示音和振动效果
     */
    private var mBeepManager: BeepManager? = null

    /**
     * 环境光照度管理器：主要通过传感器来监听光照强度变化
     */
    private var mAmbientLightManager: AmbientLightManager? = null

    /**
     * 最后点击时间，根据两次点击时间间隔用于区分单机和触摸缩放事件
     */
    private var mLastHoveTapTime: Long = 0

    /**
     * 是否是点击事件
     */
    private var isClickTap: Boolean = false

    /**
     * 按下时X坐标
     */
    private var mDownX: Float = 0f

    /**
     * 按下时Y坐标
     */
    private var mDownY: Float = 0f

    constructor(activity: ComponentActivity, previewView: PreviewView) : this(activity, activity, previewView)

    constructor(fragment: Fragment, previewView: PreviewView) : this(fragment.requireContext(), fragment.viewLifecycleOwner, previewView)

    constructor(context: Context, lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        this.mContext = context
        this.mLifecycleOwner = lifecycleOwner
        this.mPreviewView = previewView
        initData()
    }

    /**
     * 初始化
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun initData() {
        mExecutorService = Executors.newSingleThreadExecutor()
        mResultLiveData = MutableLiveData()
        mResultLiveData!!.observe(mLifecycleOwner) { result ->
            if (result != null) {
                handleAnalyzeResult(result)
            } else if (mOnScanResultCallback != null) {
                mOnScanResultCallback!!.onScanResultFailure()
            }
        }

        mOnAnalyzeListener = object : Analyzer.OnAnalyzeListener<T> {
            override fun onSuccess(result: AnalyzeResult<T>) {
                mResultLiveData?.postValue(result)
            }

            override fun onFailure(e: Exception?) {
                mResultLiveData?.postValue(null)
            }
        }

        mZoomGestureDetector = ZoomGestureDetector(mContext) { zoomEvent ->
            if (zoomEvent is ZoomGestureDetector.ZoomEvent.Move) {
                val pinchToZoomScale = zoomEvent.incrementalScaleFactor
                val zoomState = getZoomState()
                if (zoomState != null) {
                    val clampedRatio = zoomState.zoomRatio * pinchToZoomScale
                    zoomTo(clampedRatio)
                }
            }
            true
        }

        mPreviewView.setOnTouchListener { _, event ->
            handlePreviewViewClickTap(event)
            if (isNeedTouchZoom()) {
                mZoomGestureDetector!!.onTouchEvent(event)
            } else {
                false
            }
        }

        mBeepManager = BeepManager(mContext.applicationContext)
        mAmbientLightManager = AmbientLightManager(mContext.applicationContext)
        mAmbientLightManager!!.register()
        mAmbientLightManager!!.setOnLightSensorEventListener(object : AmbientLightManager.OnLightSensorEventListener {
            override fun onSensorChanged(dark: Boolean, lightLux: Float) {
                flashlightView?.let { view ->
                    if (dark) {
                        if (view.visibility != View.VISIBLE) {
                            view.visibility = View.VISIBLE
                            view.isSelected = isTorchEnabled()
                        }
                    } else if (view.visibility == View.VISIBLE && !isTorchEnabled()) {
                        view.visibility = View.INVISIBLE
                        view.isSelected = false
                    }
                }
            }
        })
    }

    /**
     * 处理预览视图点击事件；如果触发的点击事件被判定对焦操作，则开始自动对焦
     *
     * @param event 事件
     */
    private fun handlePreviewViewClickTap(event: MotionEvent) {
        if (event.pointerCount == 1) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isClickTap = true
                    mDownX = event.x
                    mDownY = event.y
                    mLastHoveTapTime = System.currentTimeMillis()
                }
                MotionEvent.ACTION_MOVE -> {
                    // Once moved out of tap slop, this gesture should not become a tap again.
                    if (isClickTap && distance(mDownX, mDownY, event.x, event.y) >= HOVER_TAP_SLOP) {
                        isClickTap = false
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (isClickTap && mLastHoveTapTime + HOVER_TAP_TIMEOUT > System.currentTimeMillis()) {
                        // 开始对焦和测光
                        startFocusAndMetering(event.x, event.y)
                    }
                }
                MotionEvent.ACTION_CANCEL -> isClickTap = false
            }
        }
    }

    /**
     * 计算两点的距离
     */
    private fun distance(aX: Float, aY: Float, bX: Float, bY: Float): Float {
        val xDiff = aX - bX
        val yDiff = aY - bY
        return Math.sqrt((xDiff * xDiff + yDiff * yDiff).toDouble()).toFloat()
    }

    /**
     * 开始对焦和测光
     *
     * @param x X轴坐标
     * @param y Y轴坐标
     */
    private fun startFocusAndMetering(x: Float, y: Float) {
        mCamera?.let { camera ->
            val point: MeteringPoint = mPreviewView.meteringPointFactory.createPoint(x, y)
            val focusMeteringAction = FocusMeteringAction.Builder(point).build()
            if (camera.cameraInfo.isFocusMeteringSupported(focusMeteringAction)) {
                camera.cameraControl.startFocusAndMetering(focusMeteringAction)
                LogX.d("startFocusAndMetering: %f, %f", x, y)
            }
        }
    }

    override fun setCameraConfig(cameraConfig: CameraConfig): CameraScan<T> {
        this.mCameraConfig = cameraConfig
        return this
    }

    override fun startCamera() {
        if (isReleased) {
            LogX.w("startCamera ignored: BaseCameraScan has been released")
            return
        }
        if (mCameraConfig == null) {
            mCameraConfig = CameraConfigFactory.createDefaultCameraConfig(mContext, CameraSelector.LENS_FACING_UNKNOWN)
        }
        stopCamera()
        val sessionToken = mCameraSessionToken.incrementAndGet()
        val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> = ProcessCameraProvider.getInstance(mContext)
        cameraProviderFuture.addListener({
            try {
                if (isStartCameraCanceled(sessionToken, "before config")) {
                    return@addListener
                }
                // 相机选择器
                val cameraSelector: CameraSelector = mCameraConfig!!.options(CameraSelector.Builder())
                // 预览
                val preview: Preview = mCameraConfig!!.options(Preview.Builder())
                // 设置SurfaceProvider
                preview.setSurfaceProvider(mPreviewView.surfaceProvider)
                // 图像分析
                val imageAnalysis: ImageAnalysis = mCameraConfig!!.options(
                    ImageAnalysis.Builder()
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                )
                val executorService = mExecutorService
                if (executorService == null || executorService.isShutdown) {
                    LogX.w("startCamera canceled: analyzer executor is shutdown")
                    return@addListener
                }
                imageAnalysis.setAnalyzer(executorService) { image ->
                    if (isAnalyze && !isAnalyzeResult && mAnalyzer != null) {
                        mAnalyzer!!.analyze(image, mOnAnalyzeListener!!)
                    }
                    image.close()
                }

                if (mAnalyzer != null) {
                    LogX.d("Analyzer: %s", mAnalyzer!!.javaClass.name)
                } else {
                    LogX.w("Analyzer is null")
                }

                mCameraProvider = cameraProviderFuture.get()
                if (isStartCameraCanceled(sessionToken, "before bind")) {
                    return@addListener
                }
                // 绑定到生命周期
                mCamera = mCameraProvider!!.bindToLifecycle(mLifecycleOwner, cameraSelector, preview, imageAnalysis)
                val previewResolutionInfo: ResolutionInfo? = preview.resolutionInfo
                if (previewResolutionInfo != null) {
                    LogX.d("Preview resolution: " + previewResolutionInfo.resolution)
                }
                val imageResolutionInfo: ResolutionInfo? = imageAnalysis.resolutionInfo
                if (imageResolutionInfo != null) {
                    LogX.d("ImageAnalysis resolution: " + imageResolutionInfo.resolution)
                }
            } catch (e: Exception) {
                LogX.e(e)
            }
        }, ContextCompat.getMainExecutor(mContext))
    }

    private fun isStartCameraCanceled(sessionToken: Int, stage: String): Boolean {
        if (isReleased) {
            LogX.d("startCamera canceled(%s): released", stage)
            return true
        }
        val currentToken = mCameraSessionToken.get()
        if (sessionToken != currentToken) {
            LogX.d("startCamera canceled(%s): stale session token=%d, current=%d",
                stage, sessionToken, currentToken)
            return true
        }
        return false
    }

    /**
     * 处理分析结果
     *
     * @param result 分析结果
     */
    @Synchronized
    private fun handleAnalyzeResult(result: AnalyzeResult<T>) {
        if (isAnalyzeResult || !isAnalyze) {
            return
        }
        isAnalyzeResult = true
        if (isAutoStopAnalyze) {
            isAnalyze = false
        }
        mBeepManager?.playBeepSoundAndVibrate()
        mOnScanResultCallback?.onScanResultCallback(result)
        isAnalyzeResult = false
    }

    override fun stopCamera() {
        mCameraSessionToken.incrementAndGet()
        mCameraProvider?.let { provider ->
            try {
                provider.unbindAll()
            } catch (e: Exception) {
                LogX.e(e)
            } finally {
                mCameraProvider = null
                mCamera = null
            }
        }
    }

    override fun setAnalyzeImage(analyze: Boolean): CameraScan<T> {
        isAnalyze = analyze
        return this
    }

    override fun setAutoStopAnalyze(autoStopAnalyze: Boolean): CameraScan<T> {
        isAutoStopAnalyze = autoStopAnalyze
        return this
    }

    override fun setAnalyzer(analyzer: Analyzer<T>?): CameraScan<T> {
        mAnalyzer = analyzer
        return this
    }

    override fun zoomIn() {
        val zoomState = getZoomState()
        if (zoomState != null) {
            val ratio = zoomState.zoomRatio + ZOOM_STEP_SIZE
            val maxRatio = zoomState.maxZoomRatio
            if (ratio <= maxRatio) {
                mCamera!!.cameraControl.setZoomRatio(ratio)
            }
        }
    }

    override fun zoomOut() {
        val zoomState = getZoomState()
        if (zoomState != null) {
            val ratio = zoomState.zoomRatio - ZOOM_STEP_SIZE
            val minRatio = zoomState.minZoomRatio
            if (ratio >= minRatio) {
                mCamera!!.cameraControl.setZoomRatio(ratio)
            }
        }
    }

    override fun zoomTo(ratio: Float) {
        val zoomState = getZoomState()
        if (zoomState != null) {
            val maxRatio = zoomState.maxZoomRatio
            val minRatio = zoomState.minZoomRatio
            val zoom = maxOf(minOf(ratio, maxRatio), minRatio)
            mCamera!!.cameraControl.setZoomRatio(zoom)
        }
    }

    override fun lineZoomIn() {
        val zoomState = getZoomState()
        if (zoomState != null) {
            val zoom = zoomState.linearZoom + ZOOM_STEP_SIZE
            if (zoom <= 1f) {
                mCamera!!.cameraControl.setLinearZoom(zoom)
            }
        }
    }

    override fun lineZoomOut() {
        val zoomState = getZoomState()
        if (zoomState != null) {
            val zoom = zoomState.linearZoom - ZOOM_STEP_SIZE
            if (zoom >= 0f) {
                mCamera!!.cameraControl.setLinearZoom(zoom)
            }
        }
    }

    override fun lineZoomTo(@FloatRange(from = 0.0, to = 1.0) linearZoom: Float) {
        mCamera?.cameraControl?.setLinearZoom(linearZoom)
    }

    override fun enableTorch(torch: Boolean) {
        if (mCamera != null && hasFlashUnit()) {
            mCamera!!.cameraControl.enableTorch(torch)
        }
    }

    override fun isTorchEnabled(): Boolean {
        if (mCamera != null) {
            val torchState: Int? = mCamera!!.cameraInfo.torchState.value
            return torchState != null && torchState == TorchState.ON
        }
        return false
    }

    override fun hasFlashUnit(): Boolean {
        if (mCamera != null) {
            return mCamera!!.cameraInfo.hasFlashUnit()
        }
        return mContext.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    override fun setVibrate(vibrate: Boolean): CameraScan<T> {
        mBeepManager?.setVibrate(vibrate)
        return this
    }

    override fun setPlayBeep(playBeep: Boolean): CameraScan<T> {
        mBeepManager?.setPlayBeep(playBeep)
        return this
    }

    override fun setOnScanResultCallback(callback: OnScanResultCallback<T>): CameraScan<T> {
        this.mOnScanResultCallback = callback
        return this
    }

    override fun getCamera(): Camera? = mCamera

    /**
     * 获取ZoomState
     *
     * @return [ZoomState]
     */
    private fun getZoomState(): ZoomState? {
        return mCamera?.cameraInfo?.zoomState?.value
    }

    override fun release() {
        isReleased = true
        isAnalyze = false
        flashlightView = null
        mAmbientLightManager?.unregister()
        mBeepManager?.close()
        mExecutorService?.shutdown()
        stopCamera()
    }

    override fun bindFlashlightView(flashlightView: View?): CameraScan<T> {
        this.flashlightView = flashlightView
        mAmbientLightManager?.isLightSensorEnabled = flashlightView != null
        return this
    }

    override fun setDarkLightLux(lightLux: Float): CameraScan<T> {
        mAmbientLightManager?.setDarkLightLux(lightLux)
        return this
    }

    override fun setBrightLightLux(lightLux: Float): CameraScan<T> {
        mAmbientLightManager?.setBrightLightLux(lightLux)
        return this
    }
}
