/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.data.fonts

import org.fcitx.fcitx5.android.utils.appContext
import org.json.JSONObject
import java.io.File

/**
 * 字体配置测试工具类
 */
object FontConfigTest {
    
    /**
     * 创建一个测试字体配置
     */
    fun createTestFontConfig() {
        val fontsDir = File(appContext.getExternalFilesDir(null), "fonts")
        if (!fontsDir.exists()) {
            fontsDir.mkdirs()
        }
        
        val configFile = File(fontsDir, "fontset.json")
        val testConfig = JSONObject().apply {
            put("cand_font", "")  // 使用系统默认
            put("font", "")       // 使用系统默认
            put("preedit_font", "")
            put("popup_key_font", "")
            put("key_main_font", "")
            put("key_alt_font", "")
        }
        
        configFile.writeText(testConfig.toString(2))
        println("创建测试字体配置: ${configFile.absolutePath}")
    }
    
    /**
     * 验证字体配置是否正确加载
     */
    fun validateFontConfig(): Boolean {
        val fontsDir = File(appContext.getExternalFilesDir(null), "fonts")
        val configFile = File(fontsDir, "fontset.json")
        
        return try {
            if (!configFile.exists()) {
                println("字体配置文件不存在")
                return false
            }
            
            val config = JSONObject(configFile.readText())
            val requiredKeys = listOf("cand_font", "font", "preedit_font", "popup_key_font", "key_main_font", "key_alt_font")
            
            requiredKeys.all { key ->
                config.has(key).also { hasKey ->
                    if (!hasKey) println("缺少必需的键: $key")
                }
            }
        } catch (e: Exception) {
            println("验证字体配置时出错: ${e.message}")
            false
        }
    }
}
