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

import android.util.Log;

import java.util.Locale;

/**
 * 日志工具类
 *
 * <pre>
 *  ┌──────────────────────────
 *    Method stack info
 *  ├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄
 *    Log message
 *  └──────────────────────────
 * </pre>
 *
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 * <p>
 * <a href="https://github.com/jenly1314">Follow me</a>
 */
@SuppressWarnings("unused")
public class LogUtils {

    public static final String TAG = "CameraScan";

    /**
     * 是否显示日志
     */
    private static boolean isShowLog = true;

    /**
     * 日志优先级别
     */
    private static int priority = 1;

    /**
     * Priority constant for the println method;use System.out.println
     */
    public static final int PRINTLN = 1;

    /**
     * Priority constant for the println method; use Log.v.
     */
    public static final int VERBOSE = 2;

    /**
     * Priority constant for the println method; use Log.d.
     */
    public static final int DEBUG = 3;

    /**
     * Priority constant for the println method; use Log.i.
     */
    public static final int INFO = 4;

    /**
     * Priority constant for the println method; use Log.w.
     */
    public static final int WARN = 5;

    /**
     * Priority constant for the println method; use Log.e.
     */
    public static final int ERROR = 6;

    /**
     * Priority constant for the println method.use Log.wtf.
     */
    public static final int ASSERT = 7;

    public static final String STACK_TRACE_FORMAT = "%s.%s(%s:%d)";

    private static final int MIN_STACK_OFFSET = 5;
    private static final int LOG_STACK_OFFSET = 6;

    /**
     * Drawing toolbox
     */
    private static final char TOP_LEFT_CORNER = '┌';
    private static final char BOTTOM_LEFT_CORNER = '└';
    private static final char MIDDLE_CORNER = '├';
    private static final char HORIZONTAL_LINE = '│';
    private static final String DOUBLE_DIVIDER = "────────────────────────────────────────────────────────";
    private static final String SINGLE_DIVIDER = "┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄";
    private static final String TOP_BORDER = TOP_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
    private static final String BOTTOM_BORDER = BOTTOM_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
    private static final String MIDDLE_BORDER = MIDDLE_CORNER + SINGLE_DIVIDER + SINGLE_DIVIDER;
    private static final String LINE_FEED = "\n";
    
    private LogUtils() {
        throw new AssertionError();
    }

    /**
     * 设置是否显示日志；此设置可全局控制是否打印日志
     * <p>
     * 如果你只想根据日志级别来控制日志的打印；可以使用{@link LogUtils#setPriority(int)}；
     *
     * @param isShowLog {@link LogUtils#isShowLog}
     */
    public static void setShowLog(boolean isShowLog) {
        LogUtils.isShowLog = isShowLog;
    }

    /**
     * 是否显示日志
     *
     * @return {@link LogUtils#isShowLog}
     */
    public static boolean isShowLog() {
        return isShowLog;
    }

    /**
     * 获取日志优先级别
     *
     * @return {@link LogUtils#priority}
     */
    public static int getPriority() {
        return priority;
    }

    /**
     * 设置日志优先级别；设置优先级之后，低于此优先级别的日志将不会打印；
     * <p>
     * 你也可以通过{@link LogUtils#setShowLog(boolean)}来全局控制是否打印日志
     *
     * @param priority {@link LogUtils#priority}
     */
    public static void setPriority(int priority) {
        LogUtils.priority = priority;
    }

    /**
     * 获取堆栈信息 className.methodName(fileName:lineNumber)
     *
     * <pre>
     *  ┌──────────────────────────
     *    Method stack info
     *  ├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄
     *    Log message
     *  └──────────────────────────
     * </pre>
     *
     * @return
     */
    private static String getStackTraceMessage(Object msg, int stackOffset) {
        StackTraceElement caller = getStackTraceElement(stackOffset);
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        String methodStack = String.format(Locale.getDefault(), STACK_TRACE_FORMAT, callerClazzName, caller.getMethodName(), caller.getFileName(), caller.getLineNumber());
        return new StringBuilder().append(TOP_BORDER)
                .append(LINE_FEED)
                .append(methodStack)
                .append(LINE_FEED)
                .append(MIDDLE_BORDER)
                .append(LINE_FEED)
                .append(msg)
                .append(LINE_FEED)
                .append(BOTTOM_BORDER)
                .toString();
    }

    /**
     * 获取堆栈
     *
     * @param n n=0		VMStack
     *          n=1		Thread
     *          n=3		CurrentStack
     *          n=4		CallerStack
     *          ...
     * @return
     */
    private static StackTraceElement getStackTraceElement(int n) {
        return Thread.currentThread().getStackTrace()[n];
    }

    /**
     * 根据异常获取堆栈信息
     *
     * @param t 异常
     * @return
     */
    private static String getStackTraceString(Throwable t) {
        return Log.getStackTraceString(t);
    }

    // -----------------------------------Log.v

    /**
     * 打印日志；日志级别：{@link LogUtils#VERBOSE}
     *
     * @param msg 日志信息
     */
    public static void v(String msg) {
        if (isShowLog && priority <= VERBOSE)
            log(Log.VERBOSE, msg);

    }

    /**
     * 打印日志；日志级别：{@link LogUtils#VERBOSE}
     *
     * @param t 异常
     */
    public static void v(Throwable t) {
        if (isShowLog && priority <= VERBOSE)
            log(Log.VERBOSE, t);
    }

    /**
     * 打印日志；日志级别：{@link LogUtils#VERBOSE}
     *
     * @param msg 日志信息
     * @param t   异常
     */
    public static void v(String msg, Throwable t) {
        if (isShowLog && priority <= VERBOSE)
            log(Log.VERBOSE, msg, t);
    }

    // -----------------------------------Log.d

    /**
     * 打印日志；日志级别：{@link LogUtils#DEBUG}
     *
     * @param msg 日志信息
     */
    public static void d(String msg) {
        if (isShowLog && priority <= DEBUG)
            log(Log.DEBUG, msg);
    }

    /**
     * 打印日志；日志级别：{@link LogUtils#DEBUG}
     *
     * @param t 异常
     */
    public static void d(Throwable t) {
        if (isShowLog && priority <= DEBUG)
            log(Log.DEBUG, t);
    }

    /**
     * 打印日志；日志级别：{@link LogUtils#DEBUG}
     *
     * @param msg 日志信息
     * @param t   异常
     */
    public static void d(String msg, Throwable t) {
        if (isShowLog && priority <= DEBUG)
            log(Log.DEBUG, msg, t);
    }

    // -----------------------------------Log.i

    /**
     * 打印日志；日志级别：{@link LogUtils#INFO}
     *
     * @param msg 日志信息
     */
    public static void i(String msg) {
        if (isShowLog && priority <= INFO)
            log(Log.INFO, msg);
    }

    /**
     * 打印日志；日志级别：{@link LogUtils#INFO}
     *
     * @param t 异常
     */
    public static void i(Throwable t) {
        if (isShowLog && priority <= INFO)
            log(Log.INFO, t);
    }

    /**
     * 打印日志；日志级别：{@link LogUtils#INFO}
     *
     * @param msg 日志信息
     * @param t   异常
     */
    public static void i(String msg, Throwable t) {
        if (isShowLog && priority <= INFO)
            log(Log.INFO, msg, t);
    }

    // -----------------------------------Log.w

    /**
     * 打印日志；日志级别：{@link LogUtils#WARN}
     *
     * @param msg 日志信息
     */
    public static void w(String msg) {
        if (isShowLog && priority <= WARN)
            log(Log.WARN, msg);
    }

    /**
     * 打印日志；日志级别：{@link LogUtils#WARN}
     *
     * @param t 异常
     */
    public static void w(Throwable t) {
        if (isShowLog && priority <= WARN)
            log(Log.WARN, t);
    }

    /**
     * 打印日志；日志级别：{@link LogUtils#WARN}
     *
     * @param msg 日志信息
     * @param t   异常
     */
    public static void w(String msg, Throwable t) {
        if (isShowLog && priority <= WARN)
            log(Log.WARN, msg, t);
    }

    // -----------------------------------Log.e

    /**
     * 打印日志；日志级别：{@link LogUtils#ERROR}
     *
     * @param msg 日志信息
     */
    public static void e(String msg) {
        if (isShowLog && priority <= ERROR)
            log(Log.ERROR, msg);
    }

    /**
     * 打印日志；日志级别：{@link LogUtils#ERROR}
     *
     * @param t 异常
     */
    public static void e(Throwable t) {
        if (isShowLog && priority <= ERROR)
            log(Log.ERROR, t);
    }

    /**
     * 打印日志；日志级别：{@link LogUtils#ERROR}
     *
     * @param msg 日志信息
     * @param t   异常
     */
    public static void e(String msg, Throwable t) {
        if (isShowLog && priority <= ERROR)
            log(Log.ERROR, msg, t);
    }

    // -----------------------------------Log.wtf

    /**
     * 打印日志；日志级别：{@link LogUtils#ASSERT}
     *
     * @param msg 日志信息
     */
    public static void wtf(String msg) {
        if (isShowLog && priority <= ASSERT)
            log(Log.ASSERT, msg);
    }

    /**
     * 打印日志；日志级别：{@link LogUtils#ASSERT}
     *
     * @param t 异常
     */
    public static void wtf(Throwable t) {
        if (isShowLog && priority <= ASSERT)
            log(Log.ASSERT, t);
    }

    /**
     * 打印日志；日志级别：{@link LogUtils#ASSERT}
     *
     * @param msg 日志信息
     * @param t   异常
     */
    public static void wtf(String msg, Throwable t) {
        if (isShowLog && priority <= ASSERT)
            log(Log.ASSERT, msg, t);
    }

    /**
     * 打印日志；日志级别：ASSERT
     *
     * @param priority 日志优先级别
     * @param msg      日志信息
     */
    private static void log(int priority, String msg) {
        Log.println(priority, TAG, getStackTraceMessage(msg, LOG_STACK_OFFSET));
    }

    /**
     * 打印日志
     *
     * @param priority 日志优先级别
     * @param t        异常
     */
    private static void log(int priority, Throwable t) {
        Log.println(priority, TAG, getStackTraceMessage(getStackTraceString(t), LOG_STACK_OFFSET));
    }

    /**
     * 打印日志
     *
     * @param priority 日志优先级别
     * @param msg      日志信息
     * @param t        异常
     */
    private static void log(int priority, String msg, Throwable t) {
        Log.println(priority, TAG, getStackTraceMessage(msg + '\n' + getStackTraceString(t), LOG_STACK_OFFSET));
    }

    // -----------------------------------System.out.println

    /**
     * System.out.println
     *
     * @param msg
     */
    public static void println(String msg) {
        if (isShowLog && priority <= PRINTLN)
            System.out.println(getStackTraceMessage(msg, MIN_STACK_OFFSET));
    }

    public static void println(Object obj) {
        if (isShowLog && priority <= PRINTLN)
            System.out.println(getStackTraceMessage(obj, MIN_STACK_OFFSET));
    }

}
