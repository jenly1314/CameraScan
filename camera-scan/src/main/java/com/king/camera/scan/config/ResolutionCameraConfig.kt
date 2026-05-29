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
package com.king.camera.scan.config

import android.content.Context
import android.util.DisplayMetrics
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import com.king.camera.scan.CameraScan
import com.king.logx.LogX

/**
 * 相机配置：根据尺寸配置相机的目标图像大小，使输出分析的图像的分辨率尽可能的接近屏幕尺寸
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 *
 * @deprecated 后续版本可能会删除此类；请使用 [AdaptiveCameraConfig]
 */
@Deprecated("后续版本可能会删除此类；请使用 AdaptiveCameraConfig")
open class ResolutionCameraConfig @JvmOverloads constructor(
    context: Context,
    imageQuality: Int = IMAGE_QUALITY_1080P
) : CameraConfig() {

    companion object {
        /** 1080P */
        const val IMAGE_QUALITY_1080P: Int = 1080
        /** 720P */
        const val IMAGE_QUALITY_720P: Int = 720
    }

    /**
     * 目标尺寸
     */
    private var mTargetSize: Size

    init {
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        LogX.d("displayMetrics: %dx%d", width, height)

        // 因为为了保持流畅性和性能，尽可能的限制在imageQuality（默认：1080p），在此前提下尽可能的找到屏幕接近的分辨率
        mTargetSize = if (width < height) {
            val ratio = height / width.toFloat()
            val size = minOf(width, imageQuality)
            if (Math.abs(ratio - CameraScan.ASPECT_RATIO_4_3) < Math.abs(ratio - CameraScan.ASPECT_RATIO_16_9)) {
                Size(size, Math.round(size * CameraScan.ASPECT_RATIO_4_3))
            } else {
                Size(size, Math.round(size * CameraScan.ASPECT_RATIO_16_9))
            }
        } else {
            val size = minOf(height, imageQuality)
            val ratio = width / height.toFloat()
            if (Math.abs(ratio - CameraScan.ASPECT_RATIO_4_3) < Math.abs(ratio - CameraScan.ASPECT_RATIO_16_9)) {
                Size(Math.round(size * CameraScan.ASPECT_RATIO_4_3), size)
            } else {
                Size(Math.round(size * CameraScan.ASPECT_RATIO_16_9), size)
            }
        }
        LogX.d("targetSize: $mTargetSize")
    }

    override fun options(builder: CameraSelector.Builder): CameraSelector {
        return super.options(builder)
    }

    override fun options(builder: Preview.Builder): Preview {
        return super.options(builder)
    }

    @Suppress("DEPRECATION")
    override fun options(builder: ImageAnalysis.Builder): ImageAnalysis {
        builder.setTargetResolution(mTargetSize)
        return super.options(builder)
    }
}
