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
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import com.king.camera.scan.CameraScan
import com.king.logx.LogX

/**
 * 相机配置：根据纵横比配置相机，使输出分析的图像尽可能的接近屏幕的比例
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 *
 * @deprecated 后续版本可能会删除此类；请使用 [AdaptiveCameraConfig]
 */
@Deprecated("后续版本可能会删除此类；请使用 AdaptiveCameraConfig")
open class AspectRatioCameraConfig(context: Context) : CameraConfig() {

    /**
     * 纵横比
     */
    private var mAspectRatio: Int = 0

    init {
        initTargetAspectRatio(context)
    }

    /**
     * 初始化 [mAspectRatio]
     *
     * @param context 上下文
     */
    private fun initTargetAspectRatio(context: Context) {
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        LogX.d("displayMetrics: %dx%d", width, height)

        val ratio = maxOf(width, height) / minOf(width, height).toFloat()
        mAspectRatio = if (Math.abs(ratio - CameraScan.ASPECT_RATIO_4_3) < Math.abs(ratio - CameraScan.ASPECT_RATIO_16_9)) {
            AspectRatio.RATIO_4_3
        } else {
            AspectRatio.RATIO_16_9
        }
        LogX.d("aspectRatio: %d", mAspectRatio)
    }

    override fun options(builder: CameraSelector.Builder): CameraSelector {
        return super.options(builder)
    }

    override fun options(builder: Preview.Builder): Preview {
        return super.options(builder)
    }

    override fun options(builder: ImageAnalysis.Builder): ImageAnalysis {
        builder.setTargetAspectRatio(mAspectRatio)
        return super.options(builder)
    }
}
