package com.king.camera.scan.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

/**
 * CameraScan 演示示例
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    /**
     * 跳转到相机扫描页
     */
    private fun startCameraScanActivity(customLayout: Boolean) {
        val intent = Intent(this, CameraScanActivity::class.java)
        intent.putExtra(KEY_CUSTOM_LAYOUT, customLayout)
        startActivity(intent)
    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.btnCameraPreview -> {
                startCameraScanActivity(false)
            }

            R.id.btnCustomCameraPreview -> {
                startCameraScanActivity(true)
            }
        }
    }

    companion object {
        const val KEY_CUSTOM_LAYOUT = "custom_layout"
    }
}
