package com.king.camera.scan.config

import android.content.Context
import android.util.DisplayMetrics
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import com.king.camera.scan.CameraScan
import com.king.logx.LogX

/**
 * 自适应相机配置：主要是根据纵横比和设备屏幕的分辨率找到与相机之间合适的相机配置；
 * 在适配、性能与体验之间找到平衡点，最终创建一个比较适合当前设备的 CameraConfig。
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
open class AdaptiveCameraConfig(context: Context) : CameraConfig() {

    companion object {
        /** 1440P */
        private const val IMAGE_QUALITY_1440P = 1440
        /** 1080P */
        private const val IMAGE_QUALITY_1080P = 1080
        /** 720P */
        private const val IMAGE_QUALITY_720P = 720
        /** 480P */
        private const val IMAGE_QUALITY_480P = 480

        /** 允许的尺寸偏差比例 15% */
        private const val ALLOWED_DEVIATION_RATIO = 0.15f
    }

    private var mAspectRatioStrategy: AspectRatioStrategy
    private var mPreviewTargetSize: Size
    private var mAnalysisTargetSize: Size
    private var mPreviewQuality: Int = 0
    private var mAnalysisQuality: Int = 0

    /**
     * 构造
     *
     * @param context 上下文
     */
    init {
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        LogX.d("displayMetrics: %dx%d", width, height)
        val processors = Runtime.getRuntime().availableProcessors()
        LogX.d("processors: %d", processors)

        val shortSide = minOf(width, height)
        val longSide = maxOf(width, height)
        val ratio = longSide / shortSide.toFloat()

        mAspectRatioStrategy = if (Math.abs(ratio - CameraScan.ASPECT_RATIO_4_3) < Math.abs(ratio - CameraScan.ASPECT_RATIO_16_9)) {
            AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY
        } else {
            AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY
        }

        mPreviewQuality = if (shortSide >= IMAGE_QUALITY_1080P) {
            IMAGE_QUALITY_1080P
        } else {
            maxOf(shortSide, IMAGE_QUALITY_720P)
        }

        mPreviewTargetSize = Size(Math.round(mPreviewQuality * ratio), mPreviewQuality)

        mAnalysisQuality = when {
            shortSide >= IMAGE_QUALITY_1440P && processors >= 8 -> IMAGE_QUALITY_1080P
            shortSide > IMAGE_QUALITY_720P -> IMAGE_QUALITY_720P
            else -> IMAGE_QUALITY_480P
        }

        mAnalysisTargetSize = Size(Math.round(mAnalysisQuality * ratio), mAnalysisQuality)

        LogX.d("Preview target: %s, Analysis target: %s", mPreviewTargetSize, mAnalysisTargetSize)
    }

    override fun options(builder: Preview.Builder): Preview {
        builder.setResolutionSelector(createResolutionSelector("Preview", mPreviewTargetSize, mPreviewQuality))
        return super.options(builder)
    }

    override fun options(builder: ImageAnalysis.Builder): ImageAnalysis {
        builder.setResolutionSelector(createResolutionSelector("ImageAnalysis", mAnalysisTargetSize, mAnalysisQuality))
        return super.options(builder)
    }

    /**
     * 创建分辨率选择器；根据自适应策略，创建一个合适的 [ResolutionSelector]
     *
     * @return [ResolutionSelector]
     */
    private fun createResolutionSelector(tag: String, targetSize: Size, quality: Int): ResolutionSelector {
        return ResolutionSelector.Builder()
            .setAspectRatioStrategy(mAspectRatioStrategy)
            .setResolutionStrategy(ResolutionStrategy(targetSize, ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER))
            .setResolutionFilter { supportedSizes, _ ->
                LogX.d("%s supportedSizes: %s", tag, supportedSizes)
                filterResolutions(supportedSizes, quality)
            }
            .build()
    }

    /**
     * 过滤分辨率
     */
    private fun filterResolutions(supportedSizes: List<Size>, targetQuality: Int): List<Size> {
        val minAcceptable = Math.round(targetQuality * (1 - ALLOWED_DEVIATION_RATIO))
        val maxAcceptable = Math.round(targetQuality * (1 + ALLOWED_DEVIATION_RATIO))

        val list = supportedSizes.filter { supportedSize ->
            val size = minOf(supportedSize.width, supportedSize.height)
            size >= minAcceptable && size <= maxAcceptable
        }

        LogX.d("Filtered resolutions for target %d (%d~%d): %s",
            targetQuality, minAcceptable, maxAcceptable, list)

        if (list.isEmpty()) {
            LogX.w("No suitable resolution found, returning all supported sizes")
            return supportedSizes
        }

        return list
    }
}
