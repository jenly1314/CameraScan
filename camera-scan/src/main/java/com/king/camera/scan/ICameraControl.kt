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

import androidx.annotation.FloatRange

/**
 * 相机控制：主要包括调节焦距和闪光灯控制
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
@Suppress("unused")
interface ICameraControl {

    /**
     * 放大
     */
    fun zoomIn()

    /**
     * 缩小
     */
    fun zoomOut()

    /**
     * 缩放到指定比例
     *
     * @param ratio 缩放比例
     */
    fun zoomTo(ratio: Float)

    /**
     * 线性放大
     */
    fun lineZoomIn()

    /**
     * 线性缩小
     */
    fun lineZoomOut()

    /**
     * 线性缩放到指定比例
     *
     * @param linearZoom 线性缩放比例；范围在：0.0 ~ 1.0之间
     */
    fun lineZoomTo(@FloatRange(from = 0.0, to = 1.0) linearZoom: Float)

    /**
     * 设置闪光灯（手电筒）是否开启
     *
     * @param torch 是否开启闪光灯（手电筒）
     */
    fun enableTorch(torch: Boolean)

    /**
     * 闪光灯（手电筒）是否开启
     *
     * @return 闪光灯（手电筒）是否开启
     */
    fun isTorchEnabled(): Boolean

    /**
     * 是否支持闪光灯
     *
     * @return 是否支持闪光灯
     */
    fun hasFlashUnit(): Boolean
}
