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
package com.king.camera.scan.manager

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

/**
 * 环境光照度管理器：主要通过传感器来监听光照强度变化
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
@Suppress("unused")
class AmbientLightManager(context: Context) : SensorEventListener {

    companion object {
        private const val INTERVAL_DURATION = 150

        protected const val DARK_LUX: Float = 45.0F
        protected const val BRIGHT_LUX: Float = 100.0F
    }

    /**
     * 光照度太暗时，默认：光照度 45 lux
     */
    private var darkLightLux: Float = DARK_LUX

    /**
     * 光照度足够亮时，默认：光照度 100 lux
     */
    private var brightLightLux: Float = BRIGHT_LUX

    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    private var lastTime: Long = 0

    var isLightSensorEnabled: Boolean = true

    private var mOnLightSensorEventListener: OnLightSensorEventListener? = null

    /**
     * 注册
     */
    fun register() {
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    /**
     * 注销
     */
    fun unregister() {
        if (lightSensor != null) {
            sensorManager.unregisterListener(this)
        }
    }

    override fun onSensorChanged(sensorEvent: SensorEvent) {
        if (isLightSensorEnabled) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastTime < INTERVAL_DURATION) {
                // 降低频率
                return
            }
            lastTime = currentTime

            mOnLightSensorEventListener?.let { listener ->
                val lightLux = sensorEvent.values[0]
                listener.onSensorChanged(lightLux)
                when {
                    lightLux <= darkLightLux -> listener.onSensorChanged(true, lightLux)
                    lightLux >= brightLightLux -> listener.onSensorChanged(false, lightLux)
                }
            }
        }
    }

    /**
     * 设置光照强度足够暗的阈值（单位：lux）
     *
     * @param lightLux 光照度
     */
    fun setDarkLightLux(lightLux: Float) {
        this.darkLightLux = lightLux
    }

    /**
     * 设置光照强度足够明亮的阈值（单位：lux）
     *
     * @param lightLux 光照度
     */
    fun setBrightLightLux(lightLux: Float) {
        this.brightLightLux = lightLux
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // do nothing
    }

    /**
     * 设置光照传感器监听器，只有在 [isLightSensorEnabled] 为`true` 才有效
     *
     * @param listener 光照传感器监听器
     */
    fun setOnLightSensorEventListener(listener: OnLightSensorEventListener?) {
        mOnLightSensorEventListener = listener
    }

    /**
     * 光线传感器事件监听器
     */
    interface OnLightSensorEventListener {
        /**
         * @param lightLux 当前检测到的光照强度值
         */
        fun onSensorChanged(lightLux: Float) {}

        /**
         * 传感器改变事件
         *
         * @param dark     是否太暗了，当检测到的光照强度值小于[darkLightLux]时，为`true`
         * @param lightLux 当前检测到的光照强度值
         */
        fun onSensorChanged(dark: Boolean, lightLux: Float)
    }
}
