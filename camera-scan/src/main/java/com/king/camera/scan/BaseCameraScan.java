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
package com.king.camera.scan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.Preview;
import androidx.camera.core.ResolutionInfo;
import androidx.camera.core.TorchState;
import androidx.camera.core.ZoomState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.activity.ComponentActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.google.common.util.concurrent.ListenableFuture;
import com.king.camera.scan.analyze.Analyzer;
import com.king.camera.scan.config.CameraConfig;
import com.king.camera.scan.config.CameraConfigFactory;
import com.king.camera.scan.internal.ZoomGestureDetector;
import com.king.camera.scan.manager.AmbientLightManager;
import com.king.camera.scan.manager.BeepManager;
import com.king.logx.LogX;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 相机扫描基类；{@link BaseCameraScan} 为 {@link CameraScan} 的默认实现
 * <p>
 * 快速实现扫描识别主要有以下几种方式：
 * <p>
 * 1、通过继承 {@link BaseCameraScanActivity}或者{@link BaseCameraScanFragment}或其子类，可快速实现扫描识别。
 * （适用于大多数场景，自定义布局时需覆写getLayoutId方法）
 * <p>
 * 2、在你项目的Activity或者Fragment中实例化一个{@link BaseCameraScan}。（适用于想在扫描界面写交互逻辑，又因为项目
 * 架构或其它原因，无法直接或间接继承{@link BaseCameraScanActivity}或{@link BaseCameraScanFragment}时使用）
 * <p>
 * 3、继承{@link CameraScan}自己实现一个，可参照默认实现类{@link BaseCameraScan}，其他步骤同方式2。（高级用法，谨慎使用）
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
public class BaseCameraScan<T> extends CameraScan<T> {

    /**
     * Defines the maximum duration in milliseconds between a touch pad
     * touch and release for a given touch to be considered a tap (click) as
     * opposed to a hover movement gesture.
     */
    private static final int HOVER_TAP_TIMEOUT = 150;
    /**
     * Defines the maximum distance in pixels that a touch pad touch can move
     * before being released for it to be considered a tap (click) as opposed
     * to a hover movement gesture.
     */
    private static final int HOVER_TAP_SLOP = 20;
    /**
     * 每次缩放改变的步长
     */
    private static final float ZOOM_STEP_SIZE = 0.1F;

    private final Context mContext;
    private final LifecycleOwner mLifecycleOwner;
    /**
     * 预览视图
     */
    private final PreviewView mPreviewView;

    private ExecutorService mExecutorService;

    private ZoomGestureDetector mZoomGestureDetector;

    private ListenableFuture<ProcessCameraProvider> mCameraProviderFuture;
    /**
     * 相机
     */
    private Camera mCamera;
    /**
     * 相机配置
     */
    private CameraConfig mCameraConfig;
    /**
     * 分析器
     */
    private Analyzer<T> mAnalyzer;
    /**
     * 是否分析
     */
    private volatile boolean isAnalyze = true;
    /**
     * 是否自动停止分析
     */
    private volatile boolean isAutoStopAnalyze = true;
    /**
     * 是否已经分析出结果
     */
    private volatile boolean isAnalyzeResult;
    /**
     * 闪光灯（手电筒）视图
     */
    private View flashlightView;
    /**
     * 分析结果
     */
    private MutableLiveData<AnalyzeResult<T>> mResultLiveData;
    /**
     * 扫描结果回调
     */
    private OnScanResultCallback<T> mOnScanResultCallback;
    /**
     * 分析监听器
     */
    private Analyzer.OnAnalyzeListener<T> mOnAnalyzeListener;
    /**
     * 音效管理器：主要用于播放蜂鸣提示音和振动效果
     */
    private BeepManager mBeepManager;
    /**
     * 环境光照度管理器：主要通过传感器来监听光照强度变化
     */
    private AmbientLightManager mAmbientLightManager;
    /**
     * 最后点击时间，根据两次点击时间间隔用于区分单机和触摸缩放事件
     */
    private long mLastHoveTapTime;
    /**
     * 是否是点击事件
     */
    private boolean isClickTap;
    /**
     * 按下时X坐标
     */
    private float mDownX;
    /**
     * 按下时Y坐标
     */
    private float mDownY;

    public BaseCameraScan(@NonNull ComponentActivity activity, @NonNull PreviewView previewView) {
        this(activity, activity, previewView);
    }

    public BaseCameraScan(@NonNull Fragment fragment, @NonNull PreviewView previewView) {
        this(fragment.requireContext(), fragment.getViewLifecycleOwner(), previewView);
    }

    public BaseCameraScan(@NonNull Context context, @NonNull LifecycleOwner lifecycleOwner, @NonNull PreviewView previewView) {
        this.mContext = context;
        this.mLifecycleOwner = lifecycleOwner;
        this.mPreviewView = previewView;
        initData();
    }

    /**
     * 初始化
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initData() {
        mExecutorService = Executors.newSingleThreadExecutor();
        mResultLiveData = new MutableLiveData<>();
        mResultLiveData.observe(mLifecycleOwner, result -> {
            if (result != null) {
                handleAnalyzeResult(result);
            } else if (mOnScanResultCallback != null) {
                mOnScanResultCallback.onScanResultFailure();
            }
        });

        mOnAnalyzeListener = new Analyzer.OnAnalyzeListener<T>() {
            @Override
            public void onSuccess(@NonNull AnalyzeResult<T> result) {
                mResultLiveData.postValue(result);
            }

            @Override
            public void onFailure(@Nullable Exception e) {
                mResultLiveData.postValue(null);
            }

        };
        mZoomGestureDetector = new ZoomGestureDetector(mContext,
            zoomEvent -> {
                if (zoomEvent instanceof ZoomGestureDetector.ZoomEvent.Move) {
                    float pinchToZoomScale = ((ZoomGestureDetector.ZoomEvent.Move) zoomEvent)
                            .getIncrementalScaleFactor();

                    ZoomState zoomState = getZoomState();
                    if (zoomState != null) {
                        float clampedRatio = zoomState.getZoomRatio() * pinchToZoomScale;
                        zoomTo(clampedRatio);
                    }
                }
                return true;
            });

        mPreviewView.setOnTouchListener((v, event) -> {
            handlePreviewViewClickTap(event);
            if (isNeedTouchZoom()) {
                return mZoomGestureDetector.onTouchEvent(event);
            }
            return false;
        });

        mBeepManager = new BeepManager(mContext.getApplicationContext());
        mAmbientLightManager = new AmbientLightManager(mContext.getApplicationContext());
        mAmbientLightManager.register();
        mAmbientLightManager.setOnLightSensorEventListener((dark, lightLux) -> {
            if (flashlightView != null) {
                if (dark) {
                    if (flashlightView.getVisibility() != View.VISIBLE) {
                        flashlightView.setVisibility(View.VISIBLE);
                        flashlightView.setSelected(isTorchEnabled());
                    }
                } else if (flashlightView.getVisibility() == View.VISIBLE && !isTorchEnabled()) {
                    flashlightView.setVisibility(View.INVISIBLE);
                    flashlightView.setSelected(false);
                }
            }
        });
    }

    /**
     * 处理预览视图点击事件；如果触发的点击事件被判定对焦操作，则开始自动对焦
     *
     * @param event 事件
     */
    private void handlePreviewViewClickTap(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isClickTap = true;
                    mDownX = event.getX();
                    mDownY = event.getY();
                    mLastHoveTapTime = System.currentTimeMillis();
                    break;
                case MotionEvent.ACTION_MOVE:
                    isClickTap = distance(mDownX, mDownY, event.getX(), event.getY()) < HOVER_TAP_SLOP;
                    break;
                case MotionEvent.ACTION_UP:
                    if (isClickTap && mLastHoveTapTime + HOVER_TAP_TIMEOUT > System.currentTimeMillis()) {
                        // 开始对焦和测光
                        startFocusAndMetering(event.getX(), event.getY());
                    }
                    break;
            }
        }
    }

    /**
     * 计算两点的距离
     *
     * @param aX a点X坐标
     * @param aY a点Y坐标
     * @param bX b点X坐标
     * @param bY b点Y坐标
     * @return 两点距离
     */
    private float distance(float aX, float aY, float bX, float bY) {
        float xDiff = aX - bX;
        float yDiff = aY - bY;
        return (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    /**
     * 开始对焦和测光
     *
     * @param x X轴坐标
     * @param y Y轴坐标
     */
    private void startFocusAndMetering(float x, float y) {
        if (mCamera != null) {
            MeteringPoint point = mPreviewView.getMeteringPointFactory().createPoint(x, y);
            FocusMeteringAction focusMeteringAction = new FocusMeteringAction.Builder(point).build();
            if (mCamera.getCameraInfo().isFocusMeteringSupported(focusMeteringAction)) {
                mCamera.getCameraControl().startFocusAndMetering(focusMeteringAction);
                LogX.d("startFocusAndMetering: %f, %f", x, y);
            }
        }
    }

    @Override
    public CameraScan<T> setCameraConfig(CameraConfig cameraConfig) {
        if (cameraConfig != null) {
            this.mCameraConfig = cameraConfig;
        }
        return this;
    }

    @Override
    public void startCamera() {
        if (mCameraConfig == null) {
            mCameraConfig = CameraConfigFactory.createDefaultCameraConfig(mContext, CameraSelector.LENS_FACING_UNKNOWN);
        }
        mCameraProviderFuture = ProcessCameraProvider.getInstance(mContext);
        mCameraProviderFuture.addListener(() -> {
            try {
                // 相机选择器
                CameraSelector cameraSelector = mCameraConfig.options(new CameraSelector.Builder());
                // 预览
                Preview preview = mCameraConfig.options(new Preview.Builder());
                // 设置SurfaceProvider
                preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());
                // 图像分析
                ImageAnalysis imageAnalysis = mCameraConfig.options(new ImageAnalysis.Builder()
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST));
                imageAnalysis.setAnalyzer(mExecutorService, image -> {
                    if (isAnalyze && !isAnalyzeResult && mAnalyzer != null) {
                        mAnalyzer.analyze(image, mOnAnalyzeListener);
                    }
                    image.close();
                });

                if(mAnalyzer != null) {
                    LogX.d("Analyzer: %s", mAnalyzer.getClass().getName());
                } else {
                    LogX.w("Analyzer is null");
                }

                if (mCamera != null) {
                    mCameraProviderFuture.get().unbindAll();
                }
                // 绑定到生命周期
                mCamera = mCameraProviderFuture.get().bindToLifecycle(mLifecycleOwner, cameraSelector, preview, imageAnalysis);
                ResolutionInfo previewResolutionInfo = preview.getResolutionInfo();
                if(previewResolutionInfo != null) {
                    LogX.d("Preview resolution: " +previewResolutionInfo.getResolution());
                }
                ResolutionInfo imageResolutionInfo = imageAnalysis.getResolutionInfo();
                if(imageResolutionInfo != null) {
                    LogX.d("ImageAnalysis resolution: " + imageResolutionInfo.getResolution());
                }
            } catch (Exception e) {
                LogX.e(e);
            }

        }, ContextCompat.getMainExecutor(mContext));
    }

    /**
     * 处理分析结果
     *
     * @param result 分析结果
     */
    private synchronized void handleAnalyzeResult(AnalyzeResult<T> result) {
        if (isAnalyzeResult || !isAnalyze) {
            return;
        }
        isAnalyzeResult = true;
        if (isAutoStopAnalyze) {
            isAnalyze = false;
        }
        if (mBeepManager != null) {
            mBeepManager.playBeepSoundAndVibrate();
        }
        if (mOnScanResultCallback != null) {
            mOnScanResultCallback.onScanResultCallback(result);
        }
        isAnalyzeResult = false;
    }

    @Override
    public void stopCamera() {
        if (mCameraProviderFuture != null) {
            try {
                mCameraProviderFuture.get().unbindAll();
            } catch (Exception e) {
                LogX.e(e);
            }
        }
    }

    @Override
    public CameraScan<T> setAnalyzeImage(boolean analyze) {
        isAnalyze = analyze;
        return this;
    }

    @Override
    public CameraScan<T> setAutoStopAnalyze(boolean autoStopAnalyze) {
        isAutoStopAnalyze = autoStopAnalyze;
        return this;
    }

    @Override
    public CameraScan<T> setAnalyzer(Analyzer<T> analyzer) {
        mAnalyzer = analyzer;
        return this;
    }

    @Override
    public void zoomIn() {
        ZoomState zoomState = getZoomState();
        if (zoomState != null) {
            float ratio = zoomState.getZoomRatio() + ZOOM_STEP_SIZE;
            float maxRatio = zoomState.getMaxZoomRatio();
            if (ratio <= maxRatio) {
                mCamera.getCameraControl().setZoomRatio(ratio);
            }
        }
    }

    @Override
    public void zoomOut() {
        ZoomState zoomState = getZoomState();
        if (zoomState != null) {
            float ratio = zoomState.getZoomRatio() - ZOOM_STEP_SIZE;
            float minRatio = zoomState.getMinZoomRatio();
            if (ratio >= minRatio) {
                mCamera.getCameraControl().setZoomRatio(ratio);
            }
        }
    }

    @Override
    public void zoomTo(float ratio) {
        ZoomState zoomState = getZoomState();
        if (zoomState != null) {
            float maxRatio = zoomState.getMaxZoomRatio();
            float minRatio = zoomState.getMinZoomRatio();
            float zoom = Math.max(Math.min(ratio, maxRatio), minRatio);
            mCamera.getCameraControl().setZoomRatio(zoom);
        }
    }

    @Override
    public void lineZoomIn() {
        ZoomState zoomState = getZoomState();
        if (zoomState != null) {
            float zoom = zoomState.getLinearZoom() + ZOOM_STEP_SIZE;
            if (zoom <= 1f) {
                mCamera.getCameraControl().setLinearZoom(zoom);
            }
        }
    }

    @Override
    public void lineZoomOut() {
        ZoomState zoomState = getZoomState();
        if (zoomState != null) {
            float zoom = zoomState.getLinearZoom() - ZOOM_STEP_SIZE;
            if (zoom >= 0f) {
                mCamera.getCameraControl().setLinearZoom(zoom);
            }
        }
    }

    @Override
    public void lineZoomTo(@FloatRange(from = 0.0, to = 1.0) float linearZoom) {
        if (mCamera != null) {
            mCamera.getCameraControl().setLinearZoom(linearZoom);
        }
    }

    @Override
    public void enableTorch(boolean torch) {
        if (mCamera != null && hasFlashUnit()) {
            mCamera.getCameraControl().enableTorch(torch);
        }
    }

    @Override
    public boolean isTorchEnabled() {
        if (mCamera != null) {
            Integer torchState = mCamera.getCameraInfo().getTorchState().getValue();
            return torchState != null && torchState == TorchState.ON;
        }
        return false;
    }

    @Override
    public boolean hasFlashUnit() {
        if (mCamera != null) {
            return mCamera.getCameraInfo().hasFlashUnit();
        }
        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    @Override
    public CameraScan<T> setVibrate(boolean vibrate) {
        if (mBeepManager != null) {
            mBeepManager.setVibrate(vibrate);
        }
        return this;
    }

    @Override
    public CameraScan<T> setPlayBeep(boolean playBeep) {
        if (mBeepManager != null) {
            mBeepManager.setPlayBeep(playBeep);
        }
        return this;
    }

    @Override
    public CameraScan<T> setOnScanResultCallback(OnScanResultCallback<T> callback) {
        this.mOnScanResultCallback = callback;
        return this;
    }

    @Nullable
    @Override
    public Camera getCamera() {
        return mCamera;
    }

    /**
     * 获取ZoomState
     *
     * @return {@link ZoomState}
     */
    @Nullable
    private ZoomState getZoomState() {
        if (mCamera != null) {
            return mCamera.getCameraInfo().getZoomState().getValue();
        }
        return null;
    }

    @Override
    public void release() {
        isAnalyze = false;
        flashlightView = null;
        if (mAmbientLightManager != null) {
            mAmbientLightManager.unregister();
        }
        if (mBeepManager != null) {
            mBeepManager.close();
        }
        if(mExecutorService != null) {
            mExecutorService.shutdown();
        }
        stopCamera();
    }

    @Override
    public CameraScan<T> bindFlashlightView(@Nullable View flashlightView) {
        this.flashlightView = flashlightView;
        if (mAmbientLightManager != null) {
            mAmbientLightManager.setLightSensorEnabled(flashlightView != null);
        }
        return this;
    }

    @Override
    public CameraScan<T> setDarkLightLux(float lightLux) {
        if (mAmbientLightManager != null) {
            mAmbientLightManager.setDarkLightLux(lightLux);
        }
        return this;
    }

    @Override
    public CameraScan<T> setBrightLightLux(float lightLux) {
        if (mAmbientLightManager != null) {
            mAmbientLightManager.setBrightLightLux(lightLux);
        }
        return this;
    }

}
