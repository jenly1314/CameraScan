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

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview

/**
 * 相机配置：主要用于提供相机预览时可自定义一些配置，便于拓展；
 * <p>
 * 库中内置实现[CameraConfig]的有[AdaptiveCameraConfig]；
 * <p>
 * 这里简单说下各自的特点：
 * <p>
 * [CameraConfig] - CameraX默认的相机配置
 * <p>
 * [AdaptiveCameraConfig] - 自适应相机配置：主要是根据纵横比和设备屏幕的分辨率找到与相机之间合适的相机配置
 * <p>
 * 当使用默认的 [CameraConfig]在某些机型上体验欠佳时，你可以尝试使用[AdaptiveCameraConfig]会有意想不到奇效。
 * <p>
 * 你也可以自定义或覆写 [CameraConfig] 中的 [options] 方法，根据需要定制配置。
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
open class CameraConfig {

    /**
     * 配置 [CameraSelector.Builder]
     * <p>
     * 如配置前置摄像头：`builder.requireLensFacing(CameraSelector.LENS_FACING_FRONT)`
     * <p>
     * 切记，外部请勿直接调用 [options]
     *
     * @param builder [CameraSelector.Builder]
     * @return [CameraSelector]
     */
    open fun options(builder: CameraSelector.Builder): CameraSelector {
        return builder.build()
    }

    /**
     * 配置 [Preview.Builder]
     * <p>
     * 如配置目标旋转角度为90度：`builder.setTargetRotation(Surface.ROTATION_90)`
     * <p>
     * 切记，外部请勿直接调用 [options]
     *
     * @param builder [Preview.Builder]
     * @return [Preview]
     */
    open fun options(builder: Preview.Builder): Preview {
        return builder.build()
    }

    /**
     * 配置 [ImageAnalysis.Builder]
     * <p>
     * 如配置目标旋转角度为90度：`builder.setTargetRotation(Surface.ROTATION_90)`
     * <p>
     * 切记，外部请勿直接调用 [options]
     *
     * @param builder [ImageAnalysis.Builder]
     * @return [ImageAnalysis]
     */
    open fun options(builder: ImageAnalysis.Builder): ImageAnalysis {
        return builder.build()
    }
}
