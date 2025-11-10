/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2021-2023 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.popup

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.view.KeyEvent // Added import
import android.view.ViewOutlineProvider
import org.fcitx.fcitx5.android.core.KeyState
import org.fcitx.fcitx5.android.core.KeyStates
import org.fcitx.fcitx5.android.data.theme.Theme
import org.fcitx.fcitx5.android.input.AutoScaleTextView
import org.fcitx.fcitx5.android.input.FcitxInputMethodService // Added import
import org.fcitx.fcitx5.android.input.keyboard.KeyAction
import splitties.dimensions.dp
import splitties.views.dsl.core.Ui
import splitties.views.dsl.core.add
import splitties.views.dsl.core.frameLayout
import splitties.views.dsl.core.horizontalLayout
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.verticalLayout
import splitties.views.dsl.core.view
import splitties.views.gravityCenter
import splitties.views.gravityEnd
import splitties.views.gravityStart
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * @param ctx [Context]
 * @param theme [Theme]
 * @param outerBounds bound [Rect] of [PopupComponent] root view.
 * @param triggerBounds bound [Rect] of popup trigger view. Used to calculate free space of both sides and
 * determine column order. See [focusColumn] and [columnOrder].
 * @param onDismissSelf callback when popup keyboard wants to close
 * @param radius popup keyboard and key radius
 * @param keyWidth key width in popup keyboard
 * @param keyHeight key height in popup keyboard
 * @param popupHeight popup preview view height. Used to transform gesture coordinate from
 * trigger view to popup keyboard view. See [offsetX] and [offsetY].
 * @param service The FcitxInputMethodService instance
 * @param keys character to commit when triggered
 * @param labels symbols to show on keys
 */
class PopupKeyboardUi(
    override val ctx: Context,
    theme: Theme,
    outerBounds: Rect,
    triggerBounds: Rect,
    onDismissSelf: PopupContainerUi.() -> Unit = {},
    private val radius: Float,
    private val keyWidth: Int,
    private val keyHeight: Int,
    private val popupHeight: Int,
    private val service: FcitxInputMethodService, // Added service parameter
    private val keys: Array<String>,
    private val labels: Array<String>
) : PopupContainerUi(ctx, theme, outerBounds, triggerBounds, onDismissSelf) {

    class PopupKeyUi(override val ctx: Context, val theme: Theme, val text: String) : Ui {

        val textView = view(::AutoScaleTextView) {
            text = this@PopupKeyUi.text
            scaleMode = AutoScaleTextView.Mode.Proportional
            textSize = 23f
            setTextColor(theme.keyTextColor)
            setFontTypeFace("popup_key_font")
        }

        override val root = frameLayout {
            add(textView, lParams {
                gravity = gravityCenter
            })
        }
    }

    private val inactiveBackground = GradientDrawable().apply {
        cornerRadius = radius
        setColor(theme.popupBackgroundColor)
    }

    private val focusBackground = GradientDrawable().apply {
        cornerRadius = radius
        setColor(theme.genericActiveBackgroundColor)
    }

    private val rowCount: Int
    private val columnCount: Int

    // those 2 variables meas initial focus row/column during initialization
    private val focusRow: Int
    private val focusColumn: Int

    init {
        val keyCount: Float = keys.size.toFloat()
        rowCount = ceil(keyCount / 5).toInt()
        columnCount = (keyCount / rowCount).roundToInt()

        focusRow = 0
        focusColumn = calcInitialFocusedColumn(columnCount, keyWidth, outerBounds, triggerBounds)
    }

    /**
     * Offset on X axis made up of 2 parts:
     *  1. from trigger view bounds left to popup entry view left
     *  2. from left-most column to initial focused column
     *
     * Offset on Y axis made up of 2 parts as well:
     *  1. from trigger view top to popup entry view top
     *  2. from top-most row to initial focused row (bottom row)
     *
     * ```
     *                    c───┬───┬───┐
     *                    │   │ 4 │ 5 │
     *                 ┌─ ├───p───┼───┤ ─┐
     *   popupKeyHeight│  │ 3 │ 1 │ 2 │  │
     *                 └─ └───┼───┼───┘  │
     *                        │   │      │popupHeight
     *                 ┌───── │o─┐│      │
     *  bounds.height()│      ││a││      │
     *                 └───── └┴─┴┘ ─────┘
     * ```
     * o: trigger view top-left origin
     *
     * p: popup preview ([PopupEntryUi]) top-left origin
     *
     * c: container view top-left origin
     *
     * Applying only `1.` parts of both X and Y offset, the origin should transform from `o` to `p`.
     * `2.` parts of both offset transform it from `p` to `c`.
     */
    override val offsetX = ((triggerBounds.width() - keyWidth) / 2) - (keyWidth * focusColumn)
    override val offsetY = (triggerBounds.height() - popupHeight) - (keyHeight * (rowCount - 1))

    private val columnOrder = createColumnOrder(columnCount, focusColumn)

    /**
     * row with smaller index displays at bottom.
     * for example, keyOrders array:
     * ```
     * [[2, 0, 1, 3], [6, 4, 5, 7]]
     * ```
     * displays as
     * ```
     * | 6 | 4 | 5 | 7 |
     * | 2 | 0 | 1 | 3 |
     * ```
     * in which `0` indicates default focus
     */
    private val keyOrders = Array(rowCount) { row ->
        IntArray(columnCount) { col -> row * columnCount + columnOrder[col] }
    }

    private var focusedIndex = keyOrders[focusRow][focusColumn]

    private val keyUis = labels.map {
        PopupKeyUi(ctx, theme, it)
    }

    init {
        markFocus(focusedIndex)
    }

    override val root = verticalLayout root@{
        background = inactiveBackground
        outlineProvider = ViewOutlineProvider.BACKGROUND
        elevation = dp(2f)
        // add rows in reverse order, because newly added view shows at bottom
        for (i in rowCount - 1 downTo 0) {
            val order = keyOrders[i]
            add(horizontalLayout row@{
                for (j in 0 until columnCount) {
                    val keyUi = keyUis.getOrNull(order[j])
                    if (keyUi == null) {
                        // align columns to right (end) when first column is empty, eg.
                        // |   | 6 | 5 | 4 |(no free space)
                        // | 3 | 2 | 1 | 0 |(no free space)
                        gravity = if (j == 0) gravityEnd else gravityStart
                    } else {
                        add(keyUi.root, lParams(keyWidth, keyHeight))
                    }
                }
            }, lParams(width = matchParent))
        }
    }

    private fun markFocus(index: Int) {
        keyUis.getOrNull(index)?.apply {
            root.background = focusBackground
            textView.setTextColor(theme.genericActiveForegroundColor)
        }
    }

    private fun markInactive(index: Int) {
        keyUis.getOrNull(index)?.apply {
            root.background = null
            textView.setTextColor(theme.popupTextColor)
        }
    }

    override fun onChangeFocus(x: Float, y: Float): Boolean {
        // move to next row when gesture moves above 30% from bottom of current row
        var newRow = rowCount - (y / keyHeight - 0.2).roundToInt()
        // move to next column when gesture moves out of current column
        var newColumn = floor(x / keyWidth).toInt()
        // retain focus when gesture moves between ±2 rows/columns of range
        if (newRow < -2 || newRow > rowCount + 1 || newColumn < -2 || newColumn > columnCount + 1) {
            onDismissSelf(this)
            return true
        }
        newRow = limitIndex(newRow, rowCount)
        newColumn = limitIndex(newColumn, columnCount)
        val newFocus = keyOrders[newRow][newColumn]
        if (newFocus < keyUis.size) {
            markInactive(focusedIndex)
            markFocus(newFocus)
            focusedIndex = newFocus
        }
        return false
    }

    override fun onTrigger(): KeyAction? {
        val rawKeyString = keys.getOrNull(focusedIndex) ?: return null

        if (rawKeyString.startsWith("Ctrl+") && rawKeyString.length > 5) {
            val actualCharStr = rawKeyString.substring(5)
            if (actualCharStr.isNotEmpty()) {
                val charToPress = actualCharStr[0]
                var keyCode = 0
                val upperChar = charToPress.uppercaseChar()

                // Simplified conversion: A-Z, 0-9
                if (upperChar in 'A'..'Z') {
                    keyCode = KeyEvent.KEYCODE_A + (upperChar.code - 'A'.code)
                } else if (charToPress in '0'..'9') {
                    keyCode = KeyEvent.KEYCODE_0 + (charToPress.code - '0'.code)
                } else {
                    // For other characters, this mapping is incomplete.
                    // You might want to add specific mappings for common symbols:
                    // when (charToPress) {
                    //     '.' -> keyCode = KeyEvent.KEYCODE_PERIOD
                    //     ',' -> keyCode = KeyEvent.KEYCODE_COMMA
                    //     // ... etc.
                    // }
                    // android.util.Log.w("PopupKeyboardUi", "Cannot map char '$charToPress' to KeyEvent.KEYCODE for Ctrl operation.")
                }

                if (keyCode != 0) {
                    service.sendCombinationKeyEvents(keyCode, ctrl = true)
                    return null // Action handled directly by service, no further KeyAction needed
                } else {
                    // Fallback: If keyCode couldn't be determined for sendCombinationKeyEvents,
                    // send the original FcitxKeyAction with Ctrl state.
                    // This ensures some Ctrl functionality remains if mapping fails.
                    return KeyAction.FcitxKeyAction(
                        act = actualCharStr,
                        states = KeyStates(KeyState.Ctrl, KeyState.Virtual)
                    )
                }
            } else {
                // "Ctrl+" but no character after it - unlikely, but handle defensively by sending as is.
                return KeyAction.FcitxKeyAction(rawKeyString)
            }
        } else {
            // Not a "Ctrl+" key, send as a normal character.
            return KeyAction.FcitxKeyAction(rawKeyString)
        }
    }
}
