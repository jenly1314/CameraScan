package com.king.camera.scan.config;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.core.resolutionselector.AspectRatioStrategy;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.core.resolutionselector.ResolutionStrategy;

import com.king.camera.scan.CameraScan;
import com.king.logx.LogX;

import java.util.ArrayList;
import java.util.List;

/**
 * 自适应相机配置：主要是根据纵横比和设备屏幕的分辨率找到与相机之间合适的相机配置；
 * 在适配、性能与体验之间找到平衡点，最终创建一个比较适合当前设备的 CameraConfig。
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
public class AdaptiveCameraConfig extends CameraConfig {

    /**
     * 1440P
     */
    private static final int IMAGE_QUALITY_1440P = 1440;
    /**
     * 1080P
     */
    private static final int IMAGE_QUALITY_1080P = 1080;
    /**
     * 720P
     */
    private static final int IMAGE_QUALITY_720P = 720;
    /**
     * 480P
     */
    private static final int IMAGE_QUALITY_480P = 480;

    /**
     * 允许的尺寸偏差比例 15%
     */
    private static final float ALLOWED_DEVIATION_RATIO = 0.15f;

    private AspectRatioStrategy mAspectRatioStrategy;

    private Size mPreviewTargetSize;
    private Size mAnalysisTargetSize;
    private int mPreviewQuality;
    private int mAnalysisQuality;

    /**
     * 构造
     *
     * @param context 上下文
     */
    public AdaptiveCameraConfig(Context context) {
        initAdaptiveCameraConfig(context);
    }

    /**
     * 初始化配置；根据 {@link DisplayMetrics} 获取屏幕尺寸来动态计算，从而找到合适的预览尺寸和分析尺寸
     *
     * @param context 上下文
     */
    private void initAdaptiveCameraConfig(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        LogX.d("displayMetrics: %dx%d", width, height);
        int processors = Runtime.getRuntime().availableProcessors();
        LogX.d("processors: %d", processors);

        int shortSide = Math.min(width, height);
        int longSide = Math.max(width, height);
        float ratio = longSide / (float) shortSide;

        if (Math.abs(ratio - CameraScan.ASPECT_RATIO_4_3) < Math.abs(ratio - CameraScan.ASPECT_RATIO_16_9)) {
            mAspectRatioStrategy = AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY;
        } else {
            mAspectRatioStrategy = AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY;
        }

        if (shortSide >= IMAGE_QUALITY_1080P) {
            mPreviewQuality = IMAGE_QUALITY_1080P;
        } else {
            mPreviewQuality = Math.max(shortSide, IMAGE_QUALITY_720P);
        }

        mPreviewTargetSize = new Size(Math.round(mPreviewQuality * ratio), mPreviewQuality);

        if (shortSide >= IMAGE_QUALITY_1440P && processors >= 8) {
            mAnalysisQuality = IMAGE_QUALITY_1080P;
        } else if (shortSide > IMAGE_QUALITY_720P) {
            mAnalysisQuality = IMAGE_QUALITY_720P;
        } else {
            mAnalysisQuality = IMAGE_QUALITY_480P;
        }

        mAnalysisTargetSize = new Size(Math.round(mAnalysisQuality * ratio), mAnalysisQuality);

        LogX.d("Preview target: %s, Analysis target: %s", mPreviewTargetSize, mAnalysisTargetSize);
    }

    @NonNull
    @Override
    public Preview options(@NonNull Preview.Builder builder) {
        builder.setResolutionSelector(createResolutionSelector("Preview", mPreviewTargetSize, mPreviewQuality));
        return super.options(builder);
    }

    @NonNull
    @Override
    public ImageAnalysis options(@NonNull ImageAnalysis.Builder builder) {
        builder.setResolutionSelector(createResolutionSelector("ImageAnalysis", mAnalysisTargetSize, mAnalysisQuality));
        return super.options(builder);
    }

    /**
     * 创建分辨率选择器；根据自适应策略，创建一个合适的 {@link ResolutionSelector}
     *
     * @return {@link ResolutionSelector}
     */
    private ResolutionSelector createResolutionSelector(String tag, Size targetSize, int quality) {
        return new ResolutionSelector.Builder()
            .setAspectRatioStrategy(mAspectRatioStrategy)
            .setResolutionStrategy(new ResolutionStrategy(targetSize, ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER))
            .setResolutionFilter((supportedSizes, rotationDegrees) -> {
                LogX.d("%s supportedSizes: %s", tag, supportedSizes);
                return filterResolutions(supportedSizes, quality);
            })
            .build();
    }

    /**
     * 过滤分辨率
     */
    private List<Size> filterResolutions(List<Size> supportedSizes, int targetQuality) {
        List<Size> list = new ArrayList<>();
        int minAcceptable = Math.round(targetQuality * (1 - ALLOWED_DEVIATION_RATIO));
        int maxAcceptable = Math.round(targetQuality * (1 + ALLOWED_DEVIATION_RATIO));

        for (Size supportedSize : supportedSizes) {
            int size = Math.min(supportedSize.getWidth(), supportedSize.getHeight());
            if (size >= minAcceptable && size <= maxAcceptable) {
                list.add(supportedSize);
            }
        }

        LogX.d("Filtered resolutions for target %d (%d~%d): %s",
            targetQuality, minAcceptable, maxAcceptable, list);

        if (list.isEmpty()) {
            LogX.w("No suitable resolution found, returning all supported sizes");
            return supportedSizes;
        }

        return list;
    }
}
