package com.king.camera.scan.config;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;

/**
 * 相机配置工厂类：主要用于创建CameraConfig，提供相对简单的创建方式
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
public final class CameraConfigFactory {

    private CameraConfigFactory() {
        throw new AssertionError();
    }

    /**
     * 根据设备配置创建一个相匹配的CameraConfig；
     * <p>
     * 自适应相机配置：主要是根据纵横比和设备屏幕的分辨率找到与相机之间合适的相机配置；
     * 在适配、性能与体验之间找到平衡点，最终创建一个比较适合当前设备的 CameraConfig。
     *
     * @param context    {@link Context}
     * @param lensFacing {@link CameraSelector#LENS_FACING_BACK} or {@link CameraSelector#LENS_FACING_FRONT}
     * @return 返回一个比较适合当前设备的 {@link CameraConfig}
     */
    public static CameraConfig createDefaultCameraConfig(Context context, @CameraSelector.LensFacing int lensFacing) {
        return new AdaptiveCameraConfig(context) {
            @NonNull
            @Override
            public CameraSelector options(@NonNull CameraSelector.Builder builder) {
                if (lensFacing != CameraSelector.LENS_FACING_UNKNOWN) {
                    builder.requireLensFacing(lensFacing);
                }
                return super.options(builder);
            }
        };
    }
}
