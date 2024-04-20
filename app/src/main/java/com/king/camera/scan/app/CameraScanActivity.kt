package com.king.camera.scan.app

import android.view.View
import androidx.camera.core.CameraSelector
import com.king.camera.scan.AnalyzeResult
import com.king.camera.scan.BaseCameraScanActivity
import com.king.camera.scan.CameraScan
import com.king.camera.scan.analyze.Analyzer
import com.king.camera.scan.config.CameraConfigFactory

/**
 * CameraScan作为一个相机扫描基础库，这里仅演示CameraScan 的基本配置和自定义布局
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
class CameraScanActivity : BaseCameraScanActivity<Unit>() {

    private var isFront = false

    /**
     * 初始化CameraScan
     */
    override fun initCameraScan(cameraScan: CameraScan<Unit>) {
        super.initCameraScan(cameraScan)
        // 根据需要修改CameraScan相关配置
        cameraScan.setPlayBeep(true)//设置是否播放音效，默认为false
            .setVibrate(true)//设置是否震动，默认为false
            .setCameraConfig(CameraConfigFactory.createDefaultCameraConfig(this, CameraSelector.LENS_FACING_BACK))//设置相机配置信息
            .setNeedTouchZoom(true)//支持多指触摸捏合缩放，默认为true
            .setDarkLightLux(45f)//设置光线足够暗的阈值（单位：lux），需要通过{@link #bindFlashlightView(View)}绑定手电筒才有效
            .setBrightLightLux(100f)//设置光线足够明亮的阈值（单位：lux），需要通过{@link #bindFlashlightView(View)}绑定手电筒才有效
            .bindFlashlightView(ivFlashlight)//绑定手电筒，绑定后可根据光线传感器，动态显示或隐藏手电筒按钮
            .setOnScanResultCallback(this)//设置扫描结果回调，需要自己处理或者需要连扫时，可设置回调，自己去处理相关逻辑
            .setAnalyzeImage(false)//设置是否分析图片，默认为true。如果设置为false，相当于关闭了扫描识别功能
    }

    /**
     * 布局ID；通过覆写此方法可以自定义布局
     *
     * @return 布局ID
     */
    override fun getLayoutId(): Int {
        val customLayout = intent.getBooleanExtra(MainActivity.KEY_CUSTOM_LAYOUT, false)
        return if (customLayout) R.layout.activity_camera_scan else super.getLayoutId()
    }

    override fun onScanResultCallback(result: AnalyzeResult<Unit>) {
        // TODO 扫描结果回调；分析后得到的结果
    }

    override fun createAnalyzer(): Analyzer<Unit>? {
        // TODO 创建分析器；由具体的分析器去实现分析检测功能
        return null
    }

    /**
     * 切换摄像头
     */
    private fun cameraSwitch() {
        isFront = !isFront
        val lensFacing = if (isFront) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        cameraScan.setCameraConfig(
            CameraConfigFactory.createDefaultCameraConfig(this, lensFacing)
        )
        // 修改CameraConfig相关配置后，需重新调用startCamera后配置才能生效
        startCamera()
    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.ivCameraSwitch -> {
                cameraSwitch()
            }
        }
    }
}