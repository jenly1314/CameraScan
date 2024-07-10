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
package com.king.camera.scan.config;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;

import com.king.camera.scan.CameraScan;
import com.king.logx.LogX;

/**
 * 相机配置：根据尺寸配置相机的目标图像大小，使输出分析的图像的分辨率尽可能的接近屏幕尺寸
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 *
 * @deprecated 后续版本可能会删除此类；请使用 {@link AdaptiveCameraConfig}
 */
@Deprecated
public class ResolutionCameraConfig extends CameraConfig {

    /**
     * 1080P
     */
    public static final int IMAGE_QUALITY_1080P = 1080;
    /**
     * 720P
     */
    public static final int IMAGE_QUALITY_720P = 720;

    /**
     * 目标尺寸
     */
    private Size mTargetSize;

    /**
     * 构造
     *
     * @param context 上下文
     */
    public ResolutionCameraConfig(Context context) {
        this(context, IMAGE_QUALITY_1080P);
    }

    /**
     * 构造
     *
     * @param context      上下文
     * @param imageQuality 图像质量；此参数只是期望的图像质量，最终以实际计算结果为准
     */
    public ResolutionCameraConfig(Context context, int imageQuality) {
        super();
        initTargetResolutionSize(context, imageQuality);
    }

    /**
     * 初始化 {@link #mTargetSize}
     *
     * @param context      上下文
     * @param imageQuality 图像质量；此参数只是期望的图像质量，最终以实际计算结果为准
     */
    private void initTargetResolutionSize(Context context, int imageQuality) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        LogX.d("displayMetrics: %dx%d", width, height);

        // 因为为了保持流畅性和性能，尽可能的限制在imageQuality（默认：1080p），在此前提下尽可能的找到屏幕接近的分辨率
        if (width < height) {
            float ratio = height / (float) width;
            int size = Math.min(width, imageQuality);
            if (Math.abs(ratio - CameraScan.ASPECT_RATIO_4_3) < Math.abs(ratio - CameraScan.ASPECT_RATIO_16_9)) {
                mTargetSize = new Size(size, Math.round(size * CameraScan.ASPECT_RATIO_4_3));
            } else {
                mTargetSize = new Size(size, Math.round(size * CameraScan.ASPECT_RATIO_16_9));
            }
        } else {
            int size = Math.min(height, imageQuality);
            float ratio = width / (float) height;
            if (Math.abs(ratio - CameraScan.ASPECT_RATIO_4_3) < Math.abs(ratio - CameraScan.ASPECT_RATIO_16_9)) {
                mTargetSize = new Size(Math.round(size * CameraScan.ASPECT_RATIO_4_3), size);
            } else {
                mTargetSize = new Size(Math.round(size * CameraScan.ASPECT_RATIO_16_9), size);
            }
        }
        LogX.d("targetSize: " + mTargetSize);
    }

    @NonNull
    @Override
    public CameraSelector options(@NonNull CameraSelector.Builder builder) {
        return super.options(builder);
    }

    @NonNull
    @Override
    public Preview options(@NonNull Preview.Builder builder) {
        return super.options(builder);
    }

    @NonNull
    @Override
    public ImageAnalysis options(@NonNull ImageAnalysis.Builder builder) {
        builder.setTargetResolution(mTargetSize);
        return super.options(builder);
    }
}
