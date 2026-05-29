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

import androidx.camera.core.Camera

/**
 * 相机定义
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
interface ICamera {

    /**
     * 启动相机预览
     */
    fun startCamera()

    /**
     * 停止相机预览
     */
    fun stopCamera()

    /**
     * 获取 [Camera]
     *
     * @return [Camera]
     */
    fun getCamera(): Camera?

    /**
     * 释放
     */
    fun release()
}
