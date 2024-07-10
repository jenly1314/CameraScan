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

import androidx.annotation.NonNull;

/**
 * 帧元数据
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
public class FrameMetadata {

    private final int width;
    private final int height;
    private final int rotation;

    /**
     * 帧元数据的宽
     *
     * @return 帧元数据的宽
     */
    public int getWidth() {
        return width;
    }

    /**
     * 帧元数据的高
     *
     * @return 帧元数据的高
     */
    public int getHeight() {
        return height;
    }

    /**
     * 获取旋转角度
     *
     * @return 旋转角度
     */
    public int getRotation() {
        return rotation;
    }

    public FrameMetadata(int width, int height, int rotation) {
        this.width = width;
        this.height = height;
        this.rotation = rotation;
    }

    @NonNull
    @Override
    public String toString() {
        return "FrameMetadata{" +
                "width=" + width +
                ", height=" + height +
                ", rotation=" + rotation +
                '}';
    }
}
