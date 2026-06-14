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
import kotlin.jvm.JvmName

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
    @get:JvmSynthetic
    @get:JvmName("getBitmapKt")
    @Volatile
    var bitmap: Bitmap? = null
        private set
        get() = field ?: getOrCreateBitmap()

    /**
     * 图像的宽；基于帧元数据的旋转角度进行计算
     */
    @get:JvmSynthetic
    @get:JvmName("getImageWidthKt")
    val imageWidth: Int
        get() = calculateImageWidth()

    /**
     * 图像的高；基于帧元数据的旋转角度进行计算
     */
    @get:JvmSynthetic
    @get:JvmName("getImageHeightKt")
    val imageHeight: Int
        get() = calculateImageHeight()

    /**
     * 获取分析图像
     *
     * @return 分析的图像
     */
    fun getBitmap(): Bitmap? = bitmap

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
    fun getImageWidth(): Int = calculateImageWidth()

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
    fun getImageHeight(): Int = calculateImageHeight()

    private fun getOrCreateBitmap(): Bitmap? {
        if (imageFormat != ImageFormat.NV21) {
            throw IllegalArgumentException("only support ImageFormat.NV21 for now.")
        }
        return synchronized(this) {
            bitmap ?: BitmapUtils.getBitmap(imageData, frameMetadata).also { bitmap = it }
        }
    }

    private fun isImageRotated(): Boolean = frameMetadata.rotation % 180 != 0

    private fun calculateImageWidth(): Int {
        return if (isImageRotated()) frameMetadata.height else frameMetadata.width
    }

    private fun calculateImageHeight(): Int {
        return if (isImageRotated()) frameMetadata.width else frameMetadata.height
    }
}
