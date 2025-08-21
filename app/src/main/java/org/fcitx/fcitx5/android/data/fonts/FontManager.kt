/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.data.fonts

import android.app.AlertDialog
import android.content.Context
import androidx.annotation.StringRes
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.data.prefs.AppPrefs
import org.fcitx.fcitx5.android.input.AutoScaleTextView
import org.fcitx.fcitx5.android.utils.appContext
import org.json.JSONObject
import java.io.File

object FontManager {

    private val fontsDir = File(appContext.getExternalFilesDir(null), "fonts")
    private val fontConfigFile = File(fontsDir, "fontset.json")

    init {
        if (!fontsDir.exists()) {
            fontsDir.mkdirs()
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
            val fontPrefs = AppPrefs.getInstance().fonts
            val config = JSONObject().apply {
                val candFont = fontPrefs.candFont.getValue()
                val preeditFont = fontPrefs.preeditFont.getValue()
                val popupKeyFont = fontPrefs.popupKeyFont.getValue()
                val keyMainFont = fontPrefs.keyMainFont.getValue()
                val keyAltFont = fontPrefs.keyAltFont.getValue()
                val defaultFont = fontPrefs.defaultFont.getValue()
                
                put("cand_font", candFont)
                put("font", defaultFont)
                put("preedit_font", preeditFont)
                put("popup_key_font", popupKeyFont)
                put("key_main_font", keyMainFont)
                put("key_alt_font", keyAltFont)
            }
            
            if (!fontsDir.exists()) {
                fontsDir.mkdirs()
            }
            
            fontConfigFile.writeText(config.toString(2))
            
            // 清除字体缓存，触发重新加载
            AutoScaleTextView.clearFontCache()
        } catch (e: Exception) {
            // 记录错误但不崩溃
            e.printStackTrace()
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
