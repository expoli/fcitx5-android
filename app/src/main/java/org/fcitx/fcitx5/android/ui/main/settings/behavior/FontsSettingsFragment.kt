/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.ui.main.settings.behavior

import android.os.Bundle
import androidx.preference.PreferenceScreen
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.data.fonts.FontManager
import org.fcitx.fcitx5.android.data.prefs.AppPrefs
import org.fcitx.fcitx5.android.data.prefs.ManagedPreferenceFragment
import org.fcitx.fcitx5.android.utils.addPreference

class FontsSettingsFragment : ManagedPreferenceFragment(AppPrefs.getInstance().fonts) {

    override fun onPreferenceUiCreated(screen: PreferenceScreen) {
        super.onPreferenceUiCreated(screen)
        val fontPrefs = AppPrefs.getInstance().fonts

        screen.addPreference(
            R.string.font_candidate,
            summary = getFontDisplayName(fontPrefs.candFont.getValue()),
            onClick = { showFontPicker(R.string.font_candidate, fontPrefs.candFont) }
        )

        screen.addPreference(
            R.string.font_preedit,
            summary = getFontDisplayName(fontPrefs.preeditFont.getValue()),
            onClick = { showFontPicker(R.string.font_preedit, fontPrefs.preeditFont) }
        )

        screen.addPreference(
            R.string.font_popup_key,
            summary = getFontDisplayName(fontPrefs.popupKeyFont.getValue()),
            onClick = { showFontPicker(R.string.font_popup_key, fontPrefs.popupKeyFont) }
        )

        screen.addPreference(
            R.string.font_key_main,
            summary = getFontDisplayName(fontPrefs.keyMainFont.getValue()),
            onClick = { showFontPicker(R.string.font_key_main, fontPrefs.keyMainFont) }
        )

        screen.addPreference(
            R.string.font_key_alt,
            summary = getFontDisplayName(fontPrefs.keyAltFont.getValue()),
            onClick = { showFontPicker(R.string.font_key_alt, fontPrefs.keyAltFont) }
        )

        screen.addPreference(
            R.string.font_default,
            summary = getFontDisplayName(fontPrefs.defaultFont.getValue()),
            onClick = { showFontPicker(R.string.font_default, fontPrefs.defaultFont) }
        )
    }

    private fun getFontDisplayName(fontFileName: String): String {
        return if (fontFileName.isEmpty()) {
            getString(R.string.system_default)
        } else {
            fontFileName.substringBeforeLast('.')
        }
    }

    private fun showFontPicker(
        titleRes: Int,
        preference: org.fcitx.fcitx5.android.data.prefs.ManagedPreference.PString
    ) {
        FontManager.showFontPickerDialog(requireContext(), titleRes) { selectedFont ->
            preference.setValue(selectedFont)
            // 刷新设置界面以显示新的摘要
            preferenceScreen.removeAll()
            onCreatePreferences(null, null)
        }
    }
}
