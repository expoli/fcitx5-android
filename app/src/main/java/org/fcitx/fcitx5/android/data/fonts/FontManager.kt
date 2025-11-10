/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.data.fonts

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import androidx.annotation.StringRes
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.data.prefs.AppPrefs
import org.fcitx.fcitx5.android.input.AutoScaleTextView
import org.fcitx.fcitx5.android.utils.appContext
import org.json.JSONObject
import java.io.File

object FontManager {

    private const val TAG = "FontManager"
    
    private val fontsDir = File(appContext.getExternalFilesDir(null), "fonts")
    private val fontConfigFile = File(fontsDir, "fontset.json")
    
    // 字体更改监听器
    interface OnFontChangeListener {
        fun onFontConfigurationChanged()
    }
    
    private val fontChangeListeners = mutableListOf<OnFontChangeListener>()

    init {
        if (!fontsDir.exists()) {
            fontsDir.mkdirs()
        }
    }

    /**
     * 添加字体更改监听器
     */
    fun addOnFontChangeListener(listener: OnFontChangeListener) {
        fontChangeListeners.add(listener)
    }
    
    /**
     * 移除字体更改监听器
     */
    fun removeOnFontChangeListener(listener: OnFontChangeListener) {
        fontChangeListeners.remove(listener)
    }
    
    /**
     * 通知所有监听器字体配置已更改
     */
    private fun notifyFontConfigurationChanged() {
        Log.d(TAG, "通知字体配置更改，监听器数量: ${fontChangeListeners.size}")
        fontChangeListeners.forEach { listener ->
            try {
                listener.onFontConfigurationChanged()
            } catch (e: Exception) {
                Log.e(TAG, "字体更改监听器执行失败", e)
            }
        }
    }

    /**
     * 获取可用字体列表
     */
    fun getAvailableFonts(): List<FontInfo> {
        val fonts = mutableListOf<FontInfo>()
        
        // 添加系统默认字体
        fonts.add(FontInfo("", "System Default"))
        
        // 扫描 fonts 目录
        if (fontsDir.exists()) {
            fontsDir.listFiles { file ->
                file.isFile && (file.extension.lowercase() in listOf("ttf", "otf"))
            }?.forEach { file ->
                fonts.add(FontInfo(file.name, file.nameWithoutExtension))
            }
        }
        
        return fonts.sortedBy { it.displayName }
    }

    /**
     * 显示字体选择对话框
     */
    fun showFontPickerDialog(
        context: Context,
        @StringRes titleRes: Int,
        onFontSelected: (String) -> Unit
    ) {
        val fonts = getAvailableFonts()
        val fontNames = fonts.map { it.displayName }.toTypedArray()
        
        AlertDialog.Builder(context)
            .setTitle(titleRes)
            .setItems(fontNames) { _, which ->
                val selectedFont = fonts[which].fileName
                onFontSelected(selectedFont)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /**
     * 更新字体配置文件
     */
    fun updateFontConfiguration() {
        try {
            Log.d(TAG, "开始更新字体配置")
            
            val fontPrefs = AppPrefs.getInstance().fonts
            val config = JSONObject().apply {
                val candFont = fontPrefs.candFont.getValue()
                val preeditFont = fontPrefs.preeditFont.getValue()
                val popupKeyFont = fontPrefs.popupKeyFont.getValue()
                val keyMainFont = fontPrefs.keyMainFont.getValue()
                val keyAltFont = fontPrefs.keyAltFont.getValue()
                val defaultFont = fontPrefs.defaultFont.getValue()
                
                Log.d(TAG, "读取到的字体配置:")
                Log.d(TAG, "  - candFont: '$candFont'")
                Log.d(TAG, "  - preeditFont: '$preeditFont'")
                Log.d(TAG, "  - popupKeyFont: '$popupKeyFont'")
                Log.d(TAG, "  - keyMainFont: '$keyMainFont'")
                Log.d(TAG, "  - keyAltFont: '$keyAltFont'")
                Log.d(TAG, "  - defaultFont: '$defaultFont'")
                
                put("cand_font", candFont)
                put("font", defaultFont)
                put("preedit_font", preeditFont)
                put("popup_key_font", popupKeyFont)
                put("key_main_font", keyMainFont)
                put("key_alt_font", keyAltFont)
            }
            
            Log.d(TAG, "目标目录: ${fontsDir.absolutePath}")
            Log.d(TAG, "目标文件: ${fontConfigFile.absolutePath}")
            
            if (!fontsDir.exists()) {
                Log.d(TAG, "创建字体目录")
                val created = fontsDir.mkdirs()
                Log.d(TAG, "目录创建结果: $created")
            }
            
            Log.d(TAG, "目录是否存在: ${fontsDir.exists()}")
            Log.d(TAG, "目录是否可写: ${fontsDir.canWrite()}")
            
            fontConfigFile.writeText(config.toString(2))
            
            Log.d(TAG, "文件是否存在: ${fontConfigFile.exists()}")
            Log.d(TAG, "文件大小: ${if (fontConfigFile.exists()) fontConfigFile.length() else "N/A"}")
            
            // 添加日志信息
            Log.i(TAG, "字体配置已更新到文件: ${fontConfigFile.absolutePath}")
            Log.d(TAG, "配置内容: ${config.toString(2)}")
            
            // 清除字体缓存，触发重新加载
            AutoScaleTextView.clearFontCache()
            
            // 通知所有监听器字体配置已更改
            notifyFontConfigurationChanged()
        } catch (e: Exception) {
            // 记录错误但不崩溃
            Log.e(TAG, "更新字体配置时出错: ${e.message}", e)
        }
    }

    /**
     * 字体信息
     */
    data class FontInfo(
        val fileName: String,
        val displayName: String
    )
}
