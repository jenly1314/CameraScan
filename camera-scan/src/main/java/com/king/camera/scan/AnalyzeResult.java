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

import android.graphics.Bitmap;
import android.graphics.ImageFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.king.camera.scan.util.BitmapUtils;

/**
 * 分析结果
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
@SuppressWarnings("unused")
public class AnalyzeResult<T> {

    /**
     * 图像数据
     */
    private final byte[] imageData;
    /**
     * 图像格式：{@link ImageFormat}
     */
    private final int imageFormat;
    /**
     * 帧元数据
     */
    private final FrameMetadata frameMetadata;
    /**
     * 分析的图像
     */
    private Bitmap bitmap;

    /**
     * 分析结果
     */
    private final T result;

    public AnalyzeResult(@NonNull byte[] imageData, int imageFormat, @NonNull FrameMetadata frameMetadata, @NonNull T result) {
        this.imageData = imageData;
        this.imageFormat = imageFormat;
        this.frameMetadata = frameMetadata;
        this.result = result;
    }

    /**
     * 获取图像帧数据：YUV数据
     *
     * @return
     */
    @NonNull
    public byte[] getImageData() {
        return imageData;
    }

    /**
     * 获取图像格式：{@link ImageFormat}
     *
     * @return
     */
    public int getImageFormat() {
        return imageFormat;
    }

    /**
     * 获取帧元数据：{@link FrameMetadata}
     *
     * @return
     */
    @NonNull
    public FrameMetadata getFrameMetadata() {
        return frameMetadata;
    }

    /**
     * 获取分析图像
     *
     * @return
     */
    @Nullable
    public Bitmap getBitmap() {
        if (imageFormat != ImageFormat.NV21) {
            throw new IllegalArgumentException("only support ImageFormat.NV21 for now.");
        }
        if (bitmap == null) {
            bitmap = BitmapUtils.getBitmap(imageData, frameMetadata);
        }
        return bitmap;
    }

    /**
     * 获取图像的宽
     *
     * @return
     * @deprecated 替换为 {@link #getImageWidth()}
     */
    @Deprecated
    public int getBitmapWidth() {
        return getImageWidth();
    }

    /**
     * 获取图像的宽
     *
     * @return
     */
    public int getImageWidth() {
        if (frameMetadata.getRotation() % 180 == 0) {
            return frameMetadata.getWidth();
        }
        return frameMetadata.getHeight();
    }

    /**
     * 获取图像的高
     *
     * @return
     * @deprecated 替换为 {@link #getImageHeight()}
     */
    @Deprecated
    public int getBitmapHeight() {
        return getImageHeight();
    }

    /**
     * 获取图像的高
     *
     * @return
     */
    public int getImageHeight() {
        if (frameMetadata.getRotation() % 180 == 0) {
            return frameMetadata.getHeight();
        }
        return frameMetadata.getWidth();
    }

    /**
     * 获取分析结果
     *
     * @return
     */
    @NonNull
    public T getResult() {
        return result;
    }
}
