package com.dirror.music.util

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build.VERSION
import com.dirror.music.MyApplication
import org.jetbrains.annotations.TestOnly

/**
 * Dirror 程序安全防护系统
 *
 * @版本 1.0.1
 * @更新时间 2020/10/6
 * @since 2020/10/4
 */
object Secure {
    private const val SIGNATURE = -1550371158 // 应该的签名值
    private const val PACKAGE_NAME = "com.dirror.music" // 包名

    private const val SKIP_DEBUG_MODE = true // Debug 模式下是否跳过安全检查

    val context = MyApplication.context

    /**
     * 主要方法
     * 是否安全
     */
    fun isSecure(): Boolean {
        if (isDebug()) {
            toast("DEBUG 模式")
            if (SKIP_DEBUG_MODE) {
                toast("跳过安全检查")
                return true
            }
        }
        // 签名错误
        if (!isSignatureCorrect()) {
            return false
        }
        // 检查抓包
        if (!checkProxy()) {
            return false
        }

        return true
    }

    /**
     * 签名检查
     */
    private fun isSignatureCorrect(): Boolean {
        // 获取当前签名 Hash 值
        val signature = getSignature()
        return signature == SIGNATURE
    }

    /**
     * 获取当前签名 Hash 值
     */
    @SuppressLint("PackageManagerGetSignatures")
    private fun getSignature(): Int {
        val packageManager = MyApplication.context.packageManager
        var signature = 0
        try {
            signature = if (VERSION.SDK_INT >= 28) {
                val packageInfo: PackageInfo = packageManager.getPackageInfo(PACKAGE_NAME, PackageManager.GET_SIGNING_CERTIFICATES)
                val arrayOfSignature = packageInfo.signingInfo.apkContentsSigners
                arrayOfSignature[0].hashCode()
            } else {
                val packageInfo: PackageInfo = packageManager.getPackageInfo(PACKAGE_NAME, PackageManager.GET_SIGNATURES)
                val arrayOfSignature = packageInfo.signatures
                arrayOfSignature[0].hashCode()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return signature
    }

    /**
     * 判断当前是否是 Debug 状态
     */
    private fun isDebug(): Boolean {
        return context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    }

    /**
     * 杀死自己
     */
    fun killMyself() {
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    /**
     * 检查 App
     */
    @TestOnly
    private fun isApplicationCorrect(): Boolean {
        val nowApplication = Application()
        val trueApplicationName = "MyApplication"
        val nowApplicationName = nowApplication.javaClass.simpleName
        return trueApplicationName == nowApplicationName
    }

    /**
     * 防抓包，检查是否有代理
     */
    private fun checkProxy(): Boolean {
        return System.getProperty("http.proxyHost") == null && System.getProperty("http.proxyPort")  == null
    }

}
