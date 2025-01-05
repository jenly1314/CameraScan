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
package com.king.camera.scan.manager;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * 环境光照度管理器：主要通过传感器来监听光照强度变化
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
@SuppressWarnings("unused")
public class AmbientLightManager implements SensorEventListener {

    private static final int INTERVAL_DURATION = 150;

    protected static final float DARK_LUX = 45.0F;
    protected static final float BRIGHT_LUX = 100.0F;

    /**
     * 光照度太暗时，默认：光照度 45 lux
     */
    private float darkLightLux = DARK_LUX;
    /**
     * 光照度足够亮时，默认：光照度 100 lux
     */
    private float brightLightLux = BRIGHT_LUX;

    private final SensorManager sensorManager;
    private final Sensor lightSensor;

    private long lastTime;

    private boolean isLightSensorEnabled;

    private OnLightSensorEventListener mOnLightSensorEventListener;

    public AmbientLightManager(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        isLightSensorEnabled = true;
    }

    /**
     * 注册
     */
    public void register() {
        if (sensorManager != null && lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    /**
     * 注销
     */
    public void unregister() {
        if (sensorManager != null && lightSensor != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (isLightSensorEnabled) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastTime < INTERVAL_DURATION) {
                // 降低频率
                return;
            }
            lastTime = currentTime;

            if (mOnLightSensorEventListener != null) {
                float lightLux = sensorEvent.values[0];
                mOnLightSensorEventListener.onSensorChanged(lightLux);
                if (lightLux <= darkLightLux) {
                    mOnLightSensorEventListener.onSensorChanged(true, lightLux);
                } else if (lightLux >= brightLightLux) {
                    mOnLightSensorEventListener.onSensorChanged(false, lightLux);
                }
            }
        }
    }

    /**
     * 设置光照强度足够暗的阈值（单位：lux）
     *
     * @param lightLux 光照度
     */
    public void setDarkLightLux(float lightLux) {
        this.darkLightLux = lightLux;
    }

    /**
     * 设置光照强度足够明亮的阈值（单位：lux）
     *
     * @param lightLux 光照度
     */
    public void setBrightLightLux(float lightLux) {
        this.brightLightLux = lightLux;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

    public boolean isLightSensorEnabled() {
        return isLightSensorEnabled;
    }

    /**
     * 设置是否启用光照传感器
     *
     * @param lightSensorEnabled 是否启用光照传感器
     */
    public void setLightSensorEnabled(boolean lightSensorEnabled) {
        isLightSensorEnabled = lightSensorEnabled;
    }

    /**
     * 设置光照传感器监听器，只有在 {@link #isLightSensorEnabled} 为{@code true} 才有效
     *
     * @param listener 光照传感器监听器
     */
    public void setOnLightSensorEventListener(OnLightSensorEventListener listener) {
        mOnLightSensorEventListener = listener;
    }

    /**
     * 光线传感器事件监听器
     */
    public interface OnLightSensorEventListener {
        /**
         * @param lightLux 当前检测到的光照强度值
         */
        default void onSensorChanged(float lightLux) {

        }

        /**
         * 传感器改变事件
         *
         * @param dark     是否太暗了，当检测到的光照强度值小于{@link #darkLightLux}时，为{@code true}
         * @param lightLux 当前检测到的光照强度值
         */
        void onSensorChanged(boolean dark, float lightLux);
    }
}
