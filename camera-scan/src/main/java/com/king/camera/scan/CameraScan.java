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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;

import com.king.camera.scan.analyze.Analyzer;
import com.king.camera.scan.config.CameraConfig;

/**
 * 相机扫描基类定义；内置的默认实现见：{@link BaseCameraScan}
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
@SuppressWarnings("unused")
public abstract class CameraScan<T> implements ICamera, ICameraControl {

    /**
     * 扫描返回结果的key；解析方式可参见：{@link #parseScanResult(Intent)}
     */
    public static String SCAN_RESULT = "SCAN_RESULT";

    /**
     * A camera on the device facing the same direction as the device's screen.
     */
    public static int LENS_FACING_FRONT = CameraSelector.LENS_FACING_FRONT;
    /**
     * A camera on the device facing the opposite direction as the device's screen.
     */
    public static int LENS_FACING_BACK = CameraSelector.LENS_FACING_BACK;

    /**
     * 纵横比：4:3
     */
    public static final float ASPECT_RATIO_4_3 = 4.0F / 3.0F;
    /**
     * 纵横比：16:9
     */
    public static final float ASPECT_RATIO_16_9 = 16.0F / 9.0F;

    /**
     * 是否需要支持触摸缩放
     */
    private boolean isNeedTouchZoom = true;
    /**
     * 扩展参数
     */
    protected Bundle mExtras;

    /**
     * 是否需要支持触摸缩放
     *
     * @return 返回是否需要支持触摸缩放
     */
    protected boolean isNeedTouchZoom() {
        return isNeedTouchZoom;
    }

    /**
     * 设置是否需要支持触摸缩放
     *
     * @param needTouchZoom 是否需要支持触摸缩放
     * @return {@link CameraScan}
     */
    public CameraScan<T> setNeedTouchZoom(boolean needTouchZoom) {
        isNeedTouchZoom = needTouchZoom;
        return this;
    }

    /**
     * 获取扩展参数：当{@link CameraScan}的默认实现不满足你的需求时，你可以通过自定义实现一个{@link CameraScan}；
     * 然后通过此方法获取扩展参数，进行扩展参数的传递；需使用时直接在实现类中获取 {@link #mExtras}即可。
     *
     * @return {@link Bundle}
     */
    @NonNull
    public Bundle getExtras() {
        if (mExtras == null) {
            mExtras = new Bundle();
        }
        return mExtras;
    }

    /**
     * 设置相机配置，请在{@link #startCamera()}之前调用
     *
     * @param cameraConfig 相机配置
     * @return {@link CameraScan}
     */
    public abstract CameraScan<T> setCameraConfig(CameraConfig cameraConfig);

    /**
     * 设置是否分析图像，默认为：true；通过此方法可以动态控制是否分析图像；在连续扫描识别时，可能会用到。
     * <p>
     * 如：当分析图像成功一次之后，如需继续连扫，可以在结果回调函数中等处理了自己的业务后，继续调用此方法并设置为true，就可以继续扫描分析图像了。
     *
     * @param analyze 是否分析图像
     * @return {@link CameraScan}
     */
    public abstract CameraScan<T> setAnalyzeImage(boolean analyze);

    /**
     * 设置是否自动停止分析图像；默认为：true；
     * <p>
     * 大多数情况下，单次扫描的场景应用较多；很容易忘记主动调用 {@link CameraScan#setAnalyzeImage(boolean)} 来停止分析。
     * <p>
     * 如果设置为：true；即：启用了自动停止分析图像：当分析图像成功一次之后；那么设置的分析图像会自动停止；如果此时
     * 需要继续分析图像，可以在结果回调里面调用 {@link CameraScan#setAnalyzeImage(boolean)} 来控制是否继续分析图像。
     * <p>
     * 如果设置为：false；即：禁用了自动停止分析图像：当分析图像成功一次之后；不会有任何变化；会继续分析图像。
     *
     * @param autoStopAnalyze
     * @return
     */
    public abstract CameraScan<T> setAutoStopAnalyze(boolean autoStopAnalyze);

    /**
     * 设置分析器，如果内置的一些分析器不满足您的需求，你也可以自定义{@link Analyzer}，
     * 自定义时，切记需在{@link #startCamera()}之前调用才有效。
     *
     * @param analyzer 分析器
     * @return {@link CameraScan}
     */
    public abstract CameraScan<T> setAnalyzer(Analyzer<T> analyzer);

    /**
     * 设置是否振动
     *
     * @param vibrate 是否振动
     * @return {@link CameraScan}
     */
    public abstract CameraScan<T> setVibrate(boolean vibrate);

    /**
     * 设置是否播放提示音
     *
     * @param playBeep 是否播放蜂鸣提示音
     * @return {@link CameraScan}
     */
    public abstract CameraScan<T> setPlayBeep(boolean playBeep);

    /**
     * 设置扫描结果回调
     *
     * @param callback 扫描结果回调
     * @return {@link CameraScan}
     */
    public abstract CameraScan<T> setOnScanResultCallback(OnScanResultCallback<T> callback);

    /**
     * 绑定手电筒，绑定后可根据光照传感器，动态显示或隐藏手电筒；并自动处理点击手电筒时的开关切换。
     *
     * @param v 手电筒视图
     * @return {@link CameraScan}
     */
    public abstract CameraScan<T> bindFlashlightView(@Nullable View v);

    /**
     * 设置光照强度足够暗的阈值（单位：lux），需要通过{@link #bindFlashlightView(View)}绑定手电筒才有效
     *
     * @param lightLux 光照度阈值
     * @return {@link CameraScan}
     */
    public abstract CameraScan<T> setDarkLightLux(float lightLux);

    /**
     * 设置光照强度足够明亮的阈值（单位：lux），需要通过{@link #bindFlashlightView(View)}绑定手电筒才有效
     *
     * @param lightLux 光照度阈值
     * @return {@link CameraScan}
     */
    public abstract CameraScan<T> setBrightLightLux(float lightLux);

    /**
     * 扫描结果回调
     *
     * @param <T> 扫描结果数据类型
     */
    public interface OnScanResultCallback<T> {
        /**
         * 扫描结果回调
         *
         * @param result 扫描结果
         */
        void onScanResultCallback(@NonNull AnalyzeResult<T> result);

        /**
         * 扫描结果识别失败时触发此回调方法
         */
        default void onScanResultFailure() {

        }
    }

    /**
     * 解析扫描结果
     *
     * @param data 需解析的意图数据
     * @return 返回解析结果
     */
    @Nullable
    public static String parseScanResult(Intent data) {
        if (data != null) {
            return data.getStringExtra(SCAN_RESULT);
        }
        return null;
    }

}
