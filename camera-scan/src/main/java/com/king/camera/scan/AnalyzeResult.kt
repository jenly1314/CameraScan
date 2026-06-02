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

import android.graphics.Bitmap
import android.graphics.ImageFormat
import com.king.camera.scan.util.BitmapUtils

/**
 * 分析结果
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
@Suppress("unused")
class AnalyzeResult<T: Any>(
    /**
     * 图像数据
     */
    val imageData: ByteArray,
    /**
     * 图像格式：[ImageFormat]
     */
    val imageFormat: Int,
    /**
     * 帧元数据
     */
    val frameMetadata: FrameMetadata,
    /**
     * 分析结果
     */
    val result: T
) {
    /**
     * 分析的图像
     */
    private var bitmap: Bitmap? = null

    /**
     * 获取分析图像
     *
     * @return 分析的图像
     */
    fun getBitmap(): Bitmap? {
        if (imageFormat != ImageFormat.NV21) {
            throw IllegalArgumentException("only support ImageFormat.NV21 for now.")
        }
        if (bitmap == null) {
            bitmap = BitmapUtils.getBitmap(imageData, frameMetadata)
        }
        return bitmap
    }

    /**
     * 获取图像的宽
     *
     * @return 图像的宽
     * @deprecated 替换为 [getImageWidth]
     */
    @Deprecated("替换为 getImageWidth()", ReplaceWith("getImageWidth()"))
    fun getBitmapWidth(): Int = getImageWidth()

    /**
     * 获取图像的宽
     *
     * @return 图像的宽
     */
    fun getImageWidth(): Int {
        return if (frameMetadata.rotation % 180 == 0) {
            frameMetadata.width
        } else {
            frameMetadata.height
        }
    }

    /**
     * 获取图像的高
     *
     * @return 图像的高
     * @deprecated 替换为 [getImageHeight]
     */
    @Deprecated("替换为 getImageHeight()", ReplaceWith("getImageHeight()"))
    fun getBitmapHeight(): Int = getImageHeight()

    /**
     * 获取图像的高
     *
     * @return 图像的高
     */
    fun getImageHeight(): Int {
        return if (frameMetadata.rotation % 180 == 0) {
            frameMetadata.height
        } else {
            frameMetadata.width
        }
    }
}
