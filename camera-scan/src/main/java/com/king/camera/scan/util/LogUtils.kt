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

import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter
import java.util.Locale

/**
 * 日志工具类
 *
 * <pre>
 *  ┌──────────────────────────
 *    Thread: name ➔ Method stack info
 *  ├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄
 *    Log message
 *  └──────────────────────────
 * </pre>
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 *
 * @deprecated 此类已标记为废弃，后续可能会删除；后续内部的日志都将使用 <a href="https://github.com/jenly1314/LogX">LogX</a> 进行来管理。
 */
@Deprecated("此类已标记为废弃，后续可能会删除；后续内部的日志都将使用 LogX 进行来管理。")
object LogUtils {

    const val TAG: String = "CameraScan"

    /**
     * 是否显示日志
     */
    @JvmField
    var isShowLog: Boolean = true

    /**
     * 日志优先级别
     */
    @JvmField
    var priority: Int = 1

    /** Priority constant for the println method;use System.out.println */
    const val PRINTLN: Int = 1

    /** Priority constant for the println method; use Log.v. */
    const val VERBOSE: Int = 2

    /** Priority constant for the println method; use Log.d. */
    const val DEBUG: Int = 3

    /** Priority constant for the println method; use Log.i. */
    const val INFO: Int = 4

    /** Priority constant for the println method; use Log.w. */
    const val WARN: Int = 5

    /** Priority constant for the println method; use Log.e. */
    const val ERROR: Int = 6

    /** Priority constant for the println method.use Log.wtf. */
    const val ASSERT: Int = 7

    const val STACK_TRACE_FORMAT: String = "%s.%s(%s:%d)"

    private const val MIN_STACK_OFFSET = 5
    private const val LOG_STACK_OFFSET = 6

    // Drawing toolbox
    private const val TOP_LEFT_CORNER = '┌'
    private const val BOTTOM_LEFT_CORNER = '└'
    private const val MIDDLE_CORNER = '├'
    private const val HORIZONTAL_LINE = '│'
    private const val DOUBLE_DIVIDER = "───────────────────────────────────────"
    private const val SINGLE_DIVIDER = "┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄"
    private val TOP_BORDER = "$TOP_LEFT_CORNER$DOUBLE_DIVIDER$DOUBLE_DIVIDER"
    private val BOTTOM_BORDER = "$BOTTOM_LEFT_CORNER$DOUBLE_DIVIDER$DOUBLE_DIVIDER"
    private val MIDDLE_BORDER = "$MIDDLE_CORNER$SINGLE_DIVIDER$SINGLE_DIVIDER"
    private const val LINE_FEED = "\n"

    private const val SPACE = " "
    private const val ARROW = " ➔ "

    /**
     * 设置是否显示日志；此设置可全局控制是否打印日志
     *
     * @param isShowLog [isShowLog]
     */
    @JvmStatic
    fun setShowLog(isShowLog: Boolean) {
        LogUtils.isShowLog = isShowLog
    }

    /**
     * 是否显示日志
     *
     * @return [isShowLog]
     */
    @JvmStatic
    fun isShowLog(): Boolean = isShowLog

    /**
     * 获取日志优先级别
     *
     * @return [priority]
     */
    @JvmStatic
    fun getPriority(): Int = priority

    /**
     * 设置日志优先级别；设置优先级之后，低于此优先级别的日志将不会打印；
     *
     * @param priority [priority]
     */
    @JvmStatic
    fun setPriority(priority: Int) {
        LogUtils.priority = priority
    }

    private fun getStackTraceMessage(msg: Any?, stackOffset: Int): String {
        val caller = getStackTraceElement(stackOffset)
        var callerClazzName = caller.className
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf('.') + 1)
        val methodStack = String.format(Locale.getDefault(), STACK_TRACE_FORMAT, callerClazzName, caller.methodName, caller.fileName, caller.lineNumber)
        return StringBuilder().append(TOP_BORDER)
            .append(LINE_FEED)
            .append(SPACE)
            .append("Thread: ")
            .append(Thread.currentThread().name)
            .append(ARROW)
            .append(methodStack)
            .append(LINE_FEED)
            .append(MIDDLE_BORDER)
            .append(LINE_FEED)
            .append(SPACE)
            .append(msg)
            .append(LINE_FEED)
            .append(BOTTOM_BORDER)
            .toString()
    }

    private fun getStackTraceElement(n: Int): StackTraceElement {
        return Thread.currentThread().stackTrace[n]
    }

    private fun getStackTraceString(t: Throwable?): String {
        if (t != null) {
            val sw = StringWriter(256)
            val pw = PrintWriter(sw, false)
            t.printStackTrace(pw)
            pw.flush()
            return sw.toString()
        }
        return ""
    }

    // -----------------------------------Log.v

    @JvmStatic
    fun v(msg: String) {
        if (isShowLog && priority <= VERBOSE) log(Log.VERBOSE, msg)
    }

    @JvmStatic
    fun v(t: Throwable) {
        if (isShowLog && priority <= VERBOSE) log(Log.VERBOSE, t)
    }

    @JvmStatic
    fun v(msg: String, t: Throwable) {
        if (isShowLog && priority <= VERBOSE) log(Log.VERBOSE, msg, t)
    }

    // -----------------------------------Log.d

    @JvmStatic
    fun d(msg: String) {
        if (isShowLog && priority <= DEBUG) log(Log.DEBUG, msg)
    }

    @JvmStatic
    fun d(t: Throwable) {
        if (isShowLog && priority <= DEBUG) log(Log.DEBUG, t)
    }

    @JvmStatic
    fun d(msg: String, t: Throwable) {
        if (isShowLog && priority <= DEBUG) log(Log.DEBUG, msg, t)
    }

    // -----------------------------------Log.i

    @JvmStatic
    fun i(msg: String) {
        if (isShowLog && priority <= INFO) log(Log.INFO, msg)
    }

    @JvmStatic
    fun i(t: Throwable) {
        if (isShowLog && priority <= INFO) log(Log.INFO, t)
    }

    @JvmStatic
    fun i(msg: String, t: Throwable) {
        if (isShowLog && priority <= INFO) log(Log.INFO, msg, t)
    }

    // -----------------------------------Log.w

    @JvmStatic
    fun w(msg: String) {
        if (isShowLog && priority <= WARN) log(Log.WARN, msg)
    }

    @JvmStatic
    fun w(t: Throwable) {
        if (isShowLog && priority <= WARN) log(Log.WARN, t)
    }

    @JvmStatic
    fun w(msg: String, t: Throwable) {
        if (isShowLog && priority <= WARN) log(Log.WARN, msg, t)
    }

    // -----------------------------------Log.e

    @JvmStatic
    fun e(msg: String) {
        if (isShowLog && priority <= ERROR) log(Log.ERROR, msg)
    }

    @JvmStatic
    fun e(t: Throwable) {
        if (isShowLog && priority <= ERROR) log(Log.ERROR, t)
    }

    @JvmStatic
    fun e(msg: String, t: Throwable) {
        if (isShowLog && priority <= ERROR) log(Log.ERROR, msg, t)
    }

    // -----------------------------------Log.wtf

    @JvmStatic
    fun wtf(msg: String) {
        if (isShowLog && priority <= ASSERT) log(Log.ASSERT, msg)
    }

    @JvmStatic
    fun wtf(t: Throwable) {
        if (isShowLog && priority <= ASSERT) log(Log.ASSERT, t)
    }

    @JvmStatic
    fun wtf(msg: String, t: Throwable) {
        if (isShowLog && priority <= ASSERT) log(Log.ASSERT, msg, t)
    }

    private fun log(priority: Int, msg: String) {
        Log.println(priority, TAG, getStackTraceMessage(msg, LOG_STACK_OFFSET))
    }

    private fun log(priority: Int, t: Throwable) {
        Log.println(priority, TAG, getStackTraceMessage(getStackTraceString(t), LOG_STACK_OFFSET))
    }

    private fun log(priority: Int, msg: String, t: Throwable) {
        Log.println(priority, TAG, getStackTraceMessage(msg + '\n' + getStackTraceString(t), LOG_STACK_OFFSET))
    }

    // -----------------------------------System.out.println

    @JvmStatic
    fun println(msg: String) {
        if (isShowLog && priority <= PRINTLN)
            System.out.println(getStackTraceMessage(msg, MIN_STACK_OFFSET))
    }

    @JvmStatic
    fun println(obj: Any?) {
        if (isShowLog && priority <= PRINTLN)
            System.out.println(getStackTraceMessage(obj, MIN_STACK_OFFSET))
    }
}
