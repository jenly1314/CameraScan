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
package com.king.camera.scan.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.king.logx.LogX;

import java.util.Arrays;

/**
 * 权限工具类
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
public class PermissionUtils {

    private PermissionUtils() {
        throw new AssertionError();
    }

    /**
     * 检测是否授权
     *
     * @param context    {@link Context}
     * @param permission 权限
     * @return 返回{@code true} 表示已授权，{@code false}表示未授权
     */
    public static boolean checkPermission(@NonNull Context context, @NonNull String permission) {
        return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 请求权限
     *
     * @param activity    {@link Activity}
     * @param permission  权限
     * @param requestCode 请求码
     */
    public static void requestPermission(@NonNull Activity activity, @NonNull String permission, @IntRange(from = 0) int requestCode) {
        requestPermissions(activity, new String[]{permission}, requestCode);
    }

    /**
     * 请求权限
     *
     * @param fragment    {@link Fragment}
     * @param permission  权限
     * @param requestCode 请求码
     */
    public static void requestPermission(@NonNull Fragment fragment, @NonNull String permission, @IntRange(from = 0) int requestCode) {
        requestPermissions(fragment, new String[]{permission}, requestCode);
    }

    /**
     * 请求权限
     *
     * @param activity    {@link Activity}
     * @param permissions 权限
     * @param requestCode 请求码
     */
    public static void requestPermissions(@NonNull Activity activity, @NonNull String[] permissions, @IntRange(from = 0) int requestCode) {
        LogX.d("requestPermissions: %s", Arrays.toString(permissions));
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    /**
     * 请求权限
     *
     * @param fragment    {@link Fragment}
     * @param permissions 权限
     * @param requestCode 请求码
     */
    public static void requestPermissions(@NonNull Fragment fragment, @NonNull String[] permissions, @IntRange(from = 0) int requestCode) {
        LogX.d("requestPermissions: %s", Arrays.toString(permissions));
        fragment.requestPermissions(permissions, requestCode);
    }

    /**
     * 请求权限结果
     *
     * @param requestPermission 需要校验的请求权限
     * @param permissions       请求的权限
     * @param grantResults      权限相应的授权结果
     * @return 返回{@code true} 表示已授权，{@code false}表示未授权
     */
    public static boolean requestPermissionsResult(@NonNull String requestPermission, @NonNull String[] permissions, @NonNull int[] grantResults) {
        int length = permissions.length;
        for (int i = 0; i < length; i++) {
            if (requestPermission.equals(permissions[i])) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 请求权限结果
     *
     * @param requestPermissions 需要校验的请求权限
     * @param permissions        请求的权限
     * @param grantResults       权限相应的授权结果
     * @return 返回{@code true} 表示全部已授权，{@code false}表示未全部授权
     */
    public static boolean requestPermissionsResult(@NonNull String[] requestPermissions, @NonNull String[] permissions, @NonNull int[] grantResults) {
        int length = permissions.length;
        for (int i = 0; i < length; i++) {
            for (String requestPermission : requestPermissions) {
                if (requestPermission.equals(permissions[i])) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

}
