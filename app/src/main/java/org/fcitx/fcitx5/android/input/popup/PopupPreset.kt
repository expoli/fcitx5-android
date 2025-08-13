/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2021-2023 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.popup

/**
 * based on
 * [fcitx5/src/im/keyboard/longpress.cpp](https://github.com/fcitx/fcitx5/blob/5.0.18/src/im/keyboard/longpress.cpp#L15)
 */
val PopupPreset: Map<String, Array<String>> = hashMapOf(
    //
    // Latin
    //
    "q" to arrayOf("1", "Q", "Ctrl+q"),
    "w" to arrayOf("2", "W", "Ctrl+w"),
    "e" to arrayOf("3", "E", "ê", "ë", "ē", "é", "ě", "è", "ė", "ę", "ȩ", "ḝ", "ə", "Ctrl+e"),
    "r" to arrayOf("4", "R", "Ctrl+r"),
    "t" to arrayOf("5", "T", "Ctrl+t"),
    "y" to arrayOf("6", "Y", "ÿ", "ұ", "ү", "ӯ", "ў", "Ctrl+y"),
    "u" to arrayOf("7", "U", "û", "ü", "ū", "ú", "ǔ", "ù", "Ctrl+u"),
    "i" to arrayOf("8", "I", "î", "ï", "ī", "í", "ǐ", "ì", "į", "ı", "Ctrl+i"),
    "o" to arrayOf("9", "O", "ô", "ö", "ō", "ó", "ǒ", "ò", "œ", "ø", "õ", "Ctrl+o"),
    "p" to arrayOf("0", "P", "Ctrl+p"),
    "a" to arrayOf("@", "A", "â", "ä", "ā", "á", "ǎ", "à", "æ", "ã", "å", "Ctrl+a"),
    "s" to arrayOf("*", "S", "ß", "ś", "š", "ş", "Ctrl+s"),
    "d" to arrayOf("+", "D", "ð", "Ctrl+d"),
    "f" to arrayOf("-", "F", "Ctrl+f"),
    "g" to arrayOf("=", "G", "ğ", "Ctrl+g"),
    "h" to arrayOf("/", "H", "Ctrl+h"),
    "j" to arrayOf("#", "J", "Ctrl+j"),
    "k" to arrayOf("(", "[", "{", "K", "Ctrl+k"),
    "l" to arrayOf(")", "]", "}", "L", "ł", "Ctrl+l"),
    "z" to arrayOf("'", "Z", "`", "ž", "ź", "ż", "Ctrl+z"),
    "x" to arrayOf(":", "X", "×", "Ctrl+x"),
    "c" to arrayOf("\"", "C", "ç", "ć", "č", "Ctrl+c"),
    "v" to arrayOf("?", "V", "¿", "ü", "ǖ", "ǘ", "ǚ", "ǜ", "Ctrl+v"),
    "b" to arrayOf("!", "B", "¡", "Ctrl+b"),
    "n" to arrayOf("~", "N", "ñ", "ń", "Ctrl+n"),
    "m" to arrayOf("\\", "M", "Ctrl+m"),
    //
    // Upper case Latin
    //
    "Q" to arrayOf("1", "q", "Ctrl+Q"),
    "W" to arrayOf("2", "w", "Ctrl+W"),
    "E" to arrayOf("3", "e", "Ê", "Ë", "Ē", "É", "È", "Ė", "Ę", "Ȩ", "Ḝ", "Ə", "Ctrl+E"),
    "R" to arrayOf("4", "r", "Ctrl+R"),
    "T" to arrayOf("5", "t", "Ctrl+T"),
    "Y" to arrayOf("6", "y", "Ÿ", "Ұ", "Ү", "Ӯ", "Ў", "Ctrl+Y"),
    "U" to arrayOf("7", "u", "Û", "Ü", "Ù", "Ú", "Ū", "Ctrl+U"),
    "I" to arrayOf("8", "i", "Î", "Ï", "Í", "Ī", "Į", "Ì", "Ctrl+I"),
    "O" to arrayOf("9", "o", "Ô", "Ö", "Ò", "Ó", "Œ", "Ø", "Ō", "Õ", "Ctrl+O"),
    "P" to arrayOf("0", "p", "Ctrl+P"),
    "A" to arrayOf("@", "a", "Â", "Ä", "Ā", "Á", "À", "Æ", "Ã", "Å", "Ctrl+A"),
    "S" to arrayOf("*", "s", "ẞ", "Ś", "Š", "Ş", "Ctrl+S"),
    "D" to arrayOf("+", "d", "Ð", "Ctrl+D"),
    "F" to arrayOf("-", "f", "Ctrl+F"),
    "G" to arrayOf("=", "g", "Ğ", "Ctrl+G"),
    "H" to arrayOf("/", "h", "Ctrl+H"),
    "J" to arrayOf("#", "j", "Ctrl+J"),
    "K" to arrayOf("(", "k", "Ctrl+K"),
    "L" to arrayOf(")", "l", "Ł", "Ctrl+L"),
    "Z" to arrayOf("'", "z", "`", "Ž", "Ź", "Ż", "Ctrl+Z"),
    "X" to arrayOf(":", "x", "Ctrl+X"),
    "C" to arrayOf("\"", "c", "Ç", "Ć", "Č", "Ctrl+C"),
    "V" to arrayOf("?", "v", "Ctrl+V"),
    "B" to arrayOf("!", "b", "¡", "Ctrl+B"),
    "N" to arrayOf("~", "n", "Ñ", "Ń", "Ctrl+N"),
    "M" to arrayOf("\\", "m", "Ctrl+M"),
    //
    // Upper case Cyrillic
    //
    "г" to arrayOf("ғ"),
    "е" to arrayOf("ё"),      // this in fact NOT the same E as before
    "и" to arrayOf("ӣ", "і"), // і is not i
    "й" to arrayOf("ј"),      // ј is not j
    "к" to arrayOf("қ", "ҝ"),
    "н" to arrayOf("ң", "һ"), // һ is not h
    "о" to arrayOf("ә", "ө"),
    "ч" to arrayOf("ҷ", "ҹ"),
    "ь" to arrayOf("ъ"),
    //
    // Cyrillic
    //
    "Г" to arrayOf("Ғ"),
    "Е" to arrayOf("Ё"),      // This In Fact Not The Same E As Before
    "И" to arrayOf("Ӣ", "І"), // І is sot I
    "Й" to arrayOf("Ј"),      // Ј is sot J
    "К" to arrayOf("Қ", "Ҝ"),
    "Н" to arrayOf("Ң", "Һ"), // Һ is not H
    "О" to arrayOf("Ә", "Ө"),
    "Ч" to arrayOf("Ҷ", "Ҹ"),
    "Ь" to arrayOf("Ъ"),
    //
    // Arabic
    //
    // This renders weirdly in text editors, but is valid code.
    "ا" to arrayOf("أ", "إ", "آ", "ء"),
    "ب" to arrayOf("پ"),
    "ج" to arrayOf("چ"),
    "ز" to arrayOf("ژ"),
    "ف" to arrayOf("ڤ"),
    "ك" to arrayOf("گ"),
    "ل" to arrayOf("لا"),
    "ه" to arrayOf("ه"),
    "و" to arrayOf("ؤ"),
    //
    // Hebrew
    //
    // Likewise, this will render oddly, but is still valid code.
    "ג" to arrayOf("ג׳"),
    "ז" to arrayOf("ז׳"),
    "ח" to arrayOf("ח׳"),
    "צ׳" to arrayOf("צ׳"),
    "ת" to arrayOf("ת׳"),
    "י" to arrayOf("ײַ"),
    "י" to arrayOf("ײ"),
    "ח" to arrayOf("ױ"),
    "ו" to arrayOf("װ"),
    //
    // Numbers
    //
    "0" to arrayOf("∅", "ⁿ", "⁰"),
    "1" to arrayOf("¹", "½", "⅓", "¼", "⅕", "⅙", "⅐", "⅛", "⅑", "⅒"),
    "2" to arrayOf("²", "⅖", "⅔"),
    "3" to arrayOf("³", "⅗", "¾", "⅜"),
    "4" to arrayOf("⁴", "⅘", "⅝", "⅚"),
    "5" to arrayOf("⁵", "⅝", "⅚"),
    "6" to arrayOf("⁶"),
    "7" to arrayOf("⁷", "⅞"),
    "8" to arrayOf("⁸"),
    "9" to arrayOf("⁹"),
    //
    // Punctuation
    //
    "." to arrayOf(",", "?", "!", ":", ";", "_", "%", "$", "^", "&"),
    "-" to arrayOf("—", "–", "·"),
    "?" to arrayOf("¿", "‽"),
    "'" to arrayOf("‘", "’", "‚", "›", "‹"),
    "!" to arrayOf("¡"),
    "\"" to arrayOf("“", "”", "„", "»", "«"),
    "/" to arrayOf("÷"),
    "#" to arrayOf("№"),
    "%" to arrayOf("‰", "℅"),
    "^" to arrayOf("↑", "↓", "←", "→"),
    "+" to arrayOf("±"),
    "<" to arrayOf("≤", "«", "‹", "⟨"),
    "=" to arrayOf("∞", "≠", "≈"),
    ">" to arrayOf("≥", "»", "›", "⟩"),
    "°" to arrayOf("′", "″", "‴"),
    //
    // Currency
    //
    "$" to arrayOf("¢", "€", "£", "¥", "₹", "₽", "₺", "₩", "₱", "₿"),
)
