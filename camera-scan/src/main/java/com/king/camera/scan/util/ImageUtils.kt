package com.king.camera.scan.util

import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer

/**
 * 图片工具类
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
@Suppress("unused")
object ImageUtils {

    /**
     * YUV420_888转NV21
     *
     * @param image [ImageProxy]
     * @param nv21  NV21 format bytes
     */
    @JvmStatic
    fun yuv_420_888toNv21(image: ImageProxy, nv21: ByteArray) {
        val yPlane: ImageProxy.PlaneProxy = image.planes[0]
        val uPlane: ImageProxy.PlaneProxy = image.planes[1]
        val vPlane: ImageProxy.PlaneProxy = image.planes[2]

        val yBuffer: ByteBuffer = yPlane.buffer
        val uBuffer: ByteBuffer = uPlane.buffer
        val vBuffer: ByteBuffer = vPlane.buffer
        yBuffer.rewind()
        uBuffer.rewind()
        vBuffer.rewind()

        val ySize = yBuffer.remaining()

        var position = 0

        // Add the full y buffer to the array. If rowStride > 1, some padding may be skipped.
        for (row in 0 until image.height) {
            yBuffer.get(nv21, position, image.width)
            position += image.width
            yBuffer.position(minOf(ySize, yBuffer.position() - image.width + yPlane.rowStride))
        }

        val chromaHeight = image.height / 2
        val chromaWidth = image.width / 2
        val vRowStride = vPlane.rowStride
        val uRowStride = uPlane.rowStride
        val vPixelStride = vPlane.pixelStride
        val uPixelStride = uPlane.pixelStride

        // Interleave the u and v frames, filling up the rest of the buffer. Use two line buffers to
        // perform faster bulk gets from the byte buffers.
        val vLineBuffer = ByteArray(vRowStride)
        val uLineBuffer = ByteArray(uRowStride)
        for (row in 0 until chromaHeight) {
            vBuffer.get(vLineBuffer, 0, minOf(vRowStride, vBuffer.remaining()))
            uBuffer.get(uLineBuffer, 0, minOf(uRowStride, uBuffer.remaining()))
            var vLineBufferPosition = 0
            var uLineBufferPosition = 0
            for (col in 0 until chromaWidth) {
                nv21[position++] = vLineBuffer[vLineBufferPosition]
                nv21[position++] = uLineBuffer[uLineBufferPosition]
                vLineBufferPosition += vPixelStride
                uLineBufferPosition += uPixelStride
            }
        }
    }
}
