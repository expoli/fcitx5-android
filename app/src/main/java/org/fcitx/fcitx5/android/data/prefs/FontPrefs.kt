/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.data.prefs

import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.preference.PreferenceScreen
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.data.fonts.FontManager
import org.fcitx.fcitx5.android.utils.addPreference

class FontPrefs(sharedPreferences: SharedPreferences) : ManagedPreferenceInternal(sharedPreferences) {

    // 字体配置项
    val candFont = string("cand_font", "")
    val preeditFont = string("preedit_font", "")
    val popupKeyFont = string("popup_key_font", "")
    val keyMainFont = string("key_main_font", "")
    val keyAltFont = string("key_alt_font", "")
    val defaultFont = string("font", "")

    override fun createUi(screen: PreferenceScreen) {
        val ctx = screen.context
        
        screen.addPreference(
            R.string.font_candidate,
            summary = getFontDisplayName(candFont.getValue()),
            onClick = { showFontPicker(ctx, R.string.font_candidate, candFont) }
        )
        
        screen.addPreference(
            R.string.font_preedit,
            summary = getFontDisplayName(preeditFont.getValue()),
            onClick = { showFontPicker(ctx, R.string.font_preedit, preeditFont) }
        )
        
        screen.addPreference(
            R.string.font_popup_key,
            summary = getFontDisplayName(popupKeyFont.getValue()),
            onClick = { showFontPicker(ctx, R.string.font_popup_key, popupKeyFont) }
        )
        
        screen.addPreference(
            R.string.font_key_main,
            summary = getFontDisplayName(keyMainFont.getValue()),
            onClick = { showFontPicker(ctx, R.string.font_key_main, keyMainFont) }
        )
        
        screen.addPreference(
            R.string.font_key_alt,
            summary = getFontDisplayName(keyAltFont.getValue()),
            onClick = { showFontPicker(ctx, R.string.font_key_alt, keyAltFont) }
        )
        
        screen.addPreference(
            R.string.font_default,
            summary = getFontDisplayName(defaultFont.getValue()),
            onClick = { showFontPicker(ctx, R.string.font_default, defaultFont) }
        )
    }

    private fun getFontDisplayName(fontFileName: String): String {
        return if (fontFileName.isEmpty()) {
            "System Default"
        } else {
            fontFileName.substringBeforeLast('.')
        }
    }

    private fun showFontPicker(
        context: android.content.Context,
        @StringRes titleRes: Int,
        preference: ManagedPreference.PString
    ) {
        FontManager.showFontPickerDialog(context, titleRes) { selectedFont ->
            preference.setValue(selectedFont)
            FontManager.updateFontConfiguration()
        }
    }
}
