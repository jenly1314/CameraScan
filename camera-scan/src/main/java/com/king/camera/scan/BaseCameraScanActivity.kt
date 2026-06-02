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

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import com.king.camera.scan.analyze.Analyzer
import com.king.camera.scan.util.PermissionUtils
import com.king.logx.LogX

/**
 * 相机扫描基类；[BaseCameraScanActivity] 内部持有[CameraScan]，便于快速实现扫描识别。
 * <p>
 * 快速实现扫描识别主要有以下几种方式：
 * <p>
 * 1、通过继承 [BaseCameraScanActivity]或者[BaseCameraScanFragment]或其子类，可快速实现扫描识别。
 * （适用于大多数场景，自定义布局时需覆写getLayoutId方法）
 * <p>
 * 2、在你项目的Activity或者Fragment中实例化一个[BaseCameraScan]。（适用于想在扫描界面写交互逻辑，又因为项目
 * 架构或其它原因，无法直接或间接继承[BaseCameraScanActivity]或[BaseCameraScanFragment]时使用）
 * <p>
 * 3、继承[CameraScan]自己实现一个，可参照默认实现类[BaseCameraScan]，其他步骤同方式2。（高级用法，谨慎使用）
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
abstract class BaseCameraScanActivity<T: Any> : AppCompatActivity(), CameraScan.OnScanResultCallback<T> {

    /**
     * 预览视图
     */
    protected var previewView: PreviewView? = null

    /**
     * 手电筒视图
     */
    protected var ivFlashlight: View? = null

    /**
     * CameraScan
     */
    private var mCameraScan: CameraScan<T>? = null

    private lateinit var mRequestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mRequestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
            ::requestCameraPermissionResult
        )
        if (isContentView()) {
            setContentView(getLayoutId())
        }
        initUI()
    }

    /**
     * 初始化
     */
    open fun initUI() {
        previewView = findViewById(getPreviewViewId())
        val ivFlashlightId = getFlashlightId()
        if (ivFlashlightId != View.NO_ID && ivFlashlightId != 0) {
            ivFlashlight = findViewById(ivFlashlightId)
            ivFlashlight?.setOnClickListener { onClickFlashlight() }
        }
        mCameraScan = createCameraScan(previewView!!)
        initCameraScan(mCameraScan!!)
        startCamera()
    }

    /**
     * 初始化CameraScan
     */
    open fun initCameraScan(cameraScan: CameraScan<T>) {
        cameraScan.setAnalyzer(createAnalyzer())
            .bindFlashlightView(ivFlashlight)
            .setOnScanResultCallback(this)
    }

    /**
     * 点击手电筒
     */
    protected open fun onClickFlashlight() {
        toggleTorchState()
    }

    /**
     * 切换闪光灯状态（开启/关闭）
     */
    protected open fun toggleTorchState() {
        getCameraScan()?.let { scanner ->
            val isTorch = scanner.isTorchEnabled()
            scanner.enableTorch(!isTorch)
            ivFlashlight?.isSelected = !isTorch
        }
    }

    /**
     * 启动相机预览
     */
    open fun startCamera() {
        val cameraScan = mCameraScan
        if (cameraScan != null) {
            if (PermissionUtils.checkPermission(this, Manifest.permission.CAMERA)) {
                cameraScan.startCamera()
            } else {
                LogX.d("Camera permission not granted, requesting permission.")
                mRequestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        } else {
            LogX.w("startCamera failed: mCameraScan is null")
        }
    }

    /**
     * 释放相机
     */
    private fun releaseCamera() {
        mCameraScan?.release()
    }

    /**
     * 请求Camera权限回调结果
     */
    protected open fun requestCameraPermissionResult(granted: Boolean) {
        if (granted) {
            LogX.d("Camera permission granted, starting camera")
            mCameraScan!!.startCamera()
        } else {
            LogX.w("Camera permission denied, finishing activity")
            finish()
        }
    }

    override fun onDestroy() {
        releaseCamera()
        super.onDestroy()
    }

    /**
     * 返回true时会自动初始化[setContentView]，返回为false是需自己去初始化[setContentView]
     *
     * @return 默认返回true
     */
    open fun isContentView(): Boolean = true

    /**
     * 布局ID；通过覆写此方法可以自定义布局
     *
     * @return 布局ID
     */
    open fun getLayoutId(): Int = R.layout.camera_scan

    /**
     * 预览视图[previewView]的ID
     *
     * @return 预览视图ID
     */
    open fun getPreviewViewId(): Int = R.id.previewView

    /**
     * 获取 [ivFlashlight] 的ID
     *
     * @return 默认返回`R.id.ivFlashlight`, 如果不需要手电筒按钮可以返回[View.NO_ID]
     */
    open fun getFlashlightId(): Int = R.id.ivFlashlight

    /**
     * 获取[CameraScan]
     *
     * @return [mCameraScan]
     */
    fun getCameraScan(): CameraScan<T>? = mCameraScan

    /**
     * 创建[CameraScan]
     *
     * @param previewView [PreviewView]
     * @return [CameraScan]
     */
    open fun createCameraScan(previewView: PreviewView): CameraScan<T> {
        return BaseCameraScan(this, previewView)
    }

    /**
     * 创建分析器
     *
     * @return [Analyzer]
     */
    abstract fun createAnalyzer(): Analyzer<T>?
}
