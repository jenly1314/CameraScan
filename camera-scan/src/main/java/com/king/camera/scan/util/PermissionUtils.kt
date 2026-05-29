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
package com.king.camera.scan.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.IntRange
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.king.logx.LogX

/**
 * 权限工具类
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
object PermissionUtils {

    /**
     * 检测是否授权
     *
     * @param context    [Context]
     * @param permission 权限
     * @return 返回`true` 表示已授权，`false`表示未授权
     */
    @JvmStatic
    fun checkPermission(context: Context, permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 请求权限
     *
     * @param activity    [Activity]
     * @param permission  权限
     * @param requestCode 请求码
     */
    @JvmStatic
    fun requestPermission(activity: Activity, permission: String, @IntRange(from = 0) requestCode: Int) {
        requestPermissions(activity, arrayOf(permission), requestCode)
    }

    /**
     * 请求权限
     *
     * @param fragment    [Fragment]
     * @param permission  权限
     * @param requestCode 请求码
     */
    @JvmStatic
    fun requestPermission(fragment: Fragment, permission: String, @IntRange(from = 0) requestCode: Int) {
        requestPermissions(fragment, arrayOf(permission), requestCode)
    }

    /**
     * 请求权限
     *
     * @param activity    [Activity]
     * @param permissions 权限
     * @param requestCode 请求码
     */
    @JvmStatic
    fun requestPermissions(activity: Activity, permissions: Array<String>, @IntRange(from = 0) requestCode: Int) {
        LogX.d("requestPermissions: %s", permissions.contentToString())
        ActivityCompat.requestPermissions(activity, permissions, requestCode)
    }

    /**
     * 请求权限
     *
     * @param fragment    [Fragment]
     * @param permissions 权限
     * @param requestCode 请求码
     */
    @JvmStatic
    fun requestPermissions(fragment: Fragment, permissions: Array<String>, @IntRange(from = 0) requestCode: Int) {
        LogX.d("requestPermissions: %s", permissions.contentToString())
        @Suppress("DEPRECATION")
        fragment.requestPermissions(permissions, requestCode)
    }

    /**
     * 请求权限结果
     *
     * @param requestPermission 需要校验的请求权限
     * @param permissions       请求的权限
     * @param grantResults      权限相应的授权结果
     * @return 返回`true` 表示已授权，`false`表示未授权
     */
    @JvmStatic
    fun requestPermissionsResult(requestPermission: String, permissions: Array<String>, grantResults: IntArray): Boolean {
        for (i in permissions.indices) {
            if (requestPermission == permissions[i]) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 请求权限结果
     *
     * @param requestPermissions 需要校验的请求权限
     * @param permissions        请求的权限
     * @param grantResults       权限相应的授权结果
     * @return 返回`true` 表示全部已授权，`false`表示未全部授权
     */
    @JvmStatic
    fun requestPermissionsResult(requestPermissions: Array<String>, permissions: Array<String>, grantResults: IntArray): Boolean {
        for (i in permissions.indices) {
            for (requestPermission in requestPermissions) {
                if (requestPermission == permissions[i]) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        return false
                    }
                }
            }
        }
        return true
    }
}
