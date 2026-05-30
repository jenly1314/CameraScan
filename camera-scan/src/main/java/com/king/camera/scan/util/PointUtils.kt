package com.king.camera.scan.util

import android.graphics.Point
import com.king.logx.LogX

/**
 * 坐标点工具类：主要是将宽高原始坐标点到宽高变化之后目标坐标点之间进行转换
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
@Suppress("unused")
object PointUtils {

    /**
     * 转换坐标：将原始 point 的坐标点从原始：srcWidth，srcHeight 进行换算后，转换成目标：destWidth，destHeight 后的坐标点
     *
     * @param point      原始坐标点
     * @param srcWidth   原始宽度
     * @param srcHeight  原始高度
     * @param destWidth  目标宽度
     * @param destHeight 目标高度
     * @param isFit      是否自适应，如果为 true 表示：宽或高自适应铺满，如果为 false 表示：填充铺满（可能会出现裁剪）
     * @return 转换之后的坐标点
     */
    @JvmStatic
    @JvmOverloads
    fun transform(point: Point, srcWidth: Int, srcHeight: Int, destWidth: Int, destHeight: Int, isFit: Boolean = false): Point {
        return transform(point.x, point.y, srcWidth, srcHeight, destWidth, destHeight, isFit)
    }

    /**
     * 转换坐标：将原始 x，y 的坐标点从原始：srcWidth，srcHeight 进行换算后，转换成目标：destWidth，destHeight 后的坐标点
     *
     * @param x          原始X坐标
     * @param y          原值Y坐标
     * @param srcWidth   原始宽度
     * @param srcHeight  原始高度
     * @param destWidth  目标宽度
     * @param destHeight 目标高度
     * @param isFit      是否自适应，如果为 true 表示：宽或高自适应铺满，如果为 false 表示：填充铺满（可能会出现裁剪）
     * @return 转换之后的坐标点
     */
    @JvmStatic
    @JvmOverloads
    fun transform(x: Int, y: Int, srcWidth: Int, srcHeight: Int, destWidth: Int, destHeight: Int, isFit: Boolean = false): Point {
        LogX.d("transform: %d,%d | %d,%d", srcWidth, srcHeight, destWidth, destHeight)
        val widthRatio = destWidth * 1.0f / srcWidth
        val heightRatio = destHeight * 1.0f / srcHeight
        val point = Point()
        if (isFit) {
            // 宽或高自适应铺满
            val ratio = minOf(widthRatio, heightRatio)
            val left = Math.abs(srcWidth * ratio - destWidth) / 2
            val top = Math.abs(srcHeight * ratio - destHeight) / 2
            point.x = (x * ratio + left).toInt()
            point.y = (y * ratio + top).toInt()
        } else {
            // 填充铺满（可能会出现裁剪）
            val ratio = maxOf(widthRatio, heightRatio)
            val left = Math.abs(srcWidth * ratio - destWidth) / 2
            val top = Math.abs(srcHeight * ratio - destHeight) / 2
            point.x = (x * ratio - left).toInt()
            point.y = (y * ratio - top).toInt()
        }
        return point
    }
}
