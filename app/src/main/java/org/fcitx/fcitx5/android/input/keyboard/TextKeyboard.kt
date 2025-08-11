/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2021-2023 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.annotation.Keep
import androidx.core.view.allViews
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.core.InputMethodEntry
import org.fcitx.fcitx5.android.core.KeyState
import org.fcitx.fcitx5.android.core.KeyStates
import org.fcitx.fcitx5.android.data.prefs.AppPrefs
import org.fcitx.fcitx5.android.data.prefs.ManagedPreference
import org.fcitx.fcitx5.android.data.theme.Theme
import org.fcitx.fcitx5.android.input.popup.PopupAction
import splitties.views.imageResource
import java.io.File
import kotlinx.serialization.json.*
import org.fcitx.fcitx5.android.utils.appContext
import kotlinx.serialization.Serializable
import android.util.Log

object DisplayTextResolver {
    fun resolve(
        displayText: JsonElement?,
        subModeLabel: String,
        default: String
    ): String {
        return when {
            displayText == null -> default
            displayText is JsonPrimitive -> displayText.content
            displayText is JsonObject -> resolveMap(displayText, subModeLabel) ?: default
            else -> default
        }
    }

    private fun resolveMap(
        map: JsonObject,
        subModeLabel: String
    ): String? {
        // 直接匹配子模式标签
        return map[subModeLabel]?.jsonPrimitive?.content
            ?: map[""]?.jsonPrimitive?.content
    }
}

@SuppressLint("ViewConstructor")
class TextKeyboard(
    context: Context,
    theme: Theme
) : BaseKeyboard(context, theme, TextKeyboard::Layout) { // Changed to reference companion Layout

    enum class CapsState { None, Once, Lock }

    companion object {
        const val Name = "Text"
        private val KEYBOARD_LAYOUT_DIR_NAME = "config"
        private val KEYBOARD_LAYOUT_FILE_NAME = "TextKeyboardLayout.json"
        private val TAG = "TextKeyboard"

        // Coroutine scope for background tasks
        private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        private var lastModifiedTimestamp = 0L // Renamed for clarity

        private val _textLayoutJsonMapFlow = MutableStateFlow<Map<String, List<List<KeyJson>>>?>(null)
        val textLayoutJsonMapFlow = _textLayoutJsonMapFlow.asStateFlow()
        
        var ime: InputMethodEntry? = null // Retained from original, usage needs review for async model

        @Serializable
        data class KeyJson(
            val type: String,
            val main: String? = null,
            val alt: String? = null,
            val displayText: JsonElement? = null,
            val label: String? = null,
            val subLabel: String? = null,
            val weight: Float? = null
        )

        fun loadLayoutsIfNeededAsync() {
            scope.launch {
                val externalFilesDir = appContext.getExternalFilesDir(null)
                if (externalFilesDir == null) {
                    Log.e(TAG, "External files directory is null. Cannot load layouts.")
                    _textLayoutJsonMapFlow.value = emptyMap() // Indicate an issue or clear existing
                    return@launch
                }
                val layoutDir = File(externalFilesDir, KEYBOARD_LAYOUT_DIR_NAME)
                val jsonFile = File(layoutDir, KEYBOARD_LAYOUT_FILE_NAME)

                if (!jsonFile.exists()) {
                    Log.w(TAG, "$KEYBOARD_LAYOUT_FILE_NAME not found in $layoutDir.")
                    if (_textLayoutJsonMapFlow.value != null || lastModifiedTimestamp != 0L) {
                        _textLayoutJsonMapFlow.value = emptyMap() // Clear if it was previously loaded
                        lastModifiedTimestamp = 0L
                    }
                    return@launch
                }

                val currentJsonTimestamp = jsonFile.lastModified()
                if (_textLayoutJsonMapFlow.value == null || lastModifiedTimestamp != currentJsonTimestamp) {
                    val newMap = runCatching {
                        val jsonText = jsonFile.readText()
                        // Ensure Json instance is configured for your needs, e.g., ignoreUnknownKeys
                        val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }
                        jsonParser.decodeFromString<Map<String, List<List<KeyJson>>>>(jsonText)
                    }.getOrElse { e ->
                        Log.e(TAG, "Error loading or parsing $KEYBOARD_LAYOUT_FILE_NAME", e)
                        emptyMap<String, List<List<KeyJson>>>() // Fallback to empty map on error
                    }
                    _textLayoutJsonMapFlow.value = newMap
                    lastModifiedTimestamp = currentJsonTimestamp
                }
            }
        }
        
        // Provides synchronous access to the current map, primarily for getTextLayoutJsonForIme.
        // UI elements should prefer observing the flow.
        private val currentTextLayoutJsonMap: Map<String, List<List<KeyJson>>>?
            get() = _textLayoutJsonMapFlow.value
        
        // This function might be called before the flow has emitted if used very early.
        // Consider making it suspend or handle nullability more explicitly if called from UI layer directly.
        fun getTextLayoutJsonForIme(displayName: String): List<List<KeyJson>>? {
            val map = currentTextLayoutJsonMap ?: return null // Return null if map not loaded yet
            return map[displayName]
        }

        // Default static layout (remains unchanged)
        val Layout: List<List<KeyDef>> = listOf(
            listOf(
                AlphabetKey("Q", "1"),
                AlphabetKey("W", "2"),
                AlphabetKey("E", "3"),
                AlphabetKey("R", "4"),
                AlphabetKey("T", "5"),
                AlphabetKey("Y", "6"),
                AlphabetKey("U", "7"),
                AlphabetKey("I", "8"),
                AlphabetKey("O", "9"),
                AlphabetKey("P", "0")
            ),
            listOf(
                AlphabetKey("A", "@"),
                AlphabetKey("S", "*"),
                AlphabetKey("D", "+"),
                AlphabetKey("F", "-"),
                AlphabetKey("G", "="),
                AlphabetKey("H", "/"),
                AlphabetKey("J", "#"),
                AlphabetKey("K", "("),
                AlphabetKey("L", ")")
            ),
            listOf(
                CapsKey(),
                AlphabetKey("Z", "'"),
                AlphabetKey("X", ":"),
                AlphabetKey("C", "\""),
                AlphabetKey("V", "?"),
                AlphabetKey("B", "!"),
                AlphabetKey("N", "~"),
                AlphabetKey("M", "\\"),
                BackspaceKey()
            ),
            listOf(
                LayoutSwitchKey("?123", ""),
                CommaKey(0.1f, KeyDef.Appearance.Variant.Alternative),
                LanguageKey(),
                SpaceKey(),
                SymbolKey(".", 0.1f, KeyDef.Appearance.Variant.Alternative),
                ReturnKey()
            )
        )
    }

    val caps: ImageKeyView by lazy { findViewById(R.id.button_caps) }
    val backspace: ImageKeyView by lazy { findViewById(R.id.button_backspace) }
    val quickphrase: ImageKeyView by lazy { findViewById(R.id.button_quickphrase) }
    val lang: ImageKeyView by lazy { findViewById(R.id.button_lang) }
    val space: TextKeyView by lazy { findViewById(R.id.button_space) }
    val `return`: ImageKeyView by lazy { findViewById(R.id.button_return) }

    private val showLangSwitchKey = AppPrefs.getInstance().keyboard.showLangSwitchKey

    @Keep
    private val showLangSwitchKeyListener = ManagedPreference.OnChangeListener<Boolean> { _, v ->
        updateLangSwitchKey(v)
    }

    private val keepLettersUppercase by AppPrefs.getInstance().keyboard.keepLettersUppercase

    private val textKeys: List<TextKeyView> by lazy {
        allViews.filterIsInstance(TextKeyView::class.java).toList()
    }

    private var capsState: CapsState = CapsState.None
    private var punctuationMapping: Map<String, String> = mapOf()
    
    // Coroutine Job for this instance to cancel collection on detach
    private var viewCollectJob: Job? = null
    private val keyboardScope = CoroutineScope(Dispatchers.Main)


    init {
        // Trigger initial load
        loadLayoutsIfNeededAsync()
        
        // Observe layout changes - This might be better in onAttach if ime is not ready here
        // However, onInputMethodUpdate will also trigger updateAlphabetKeys
    }

    private fun transformAlphabet(c: String): String {
        return when (capsState) {
            CapsState.None -> c.lowercase()
            else -> c.uppercase()
        }
    }

    private fun transformPunctuation(p: String) = punctuationMapping.getOrDefault(p, p)

    override fun onAction(action: KeyAction, source: KeyActionListener.Source) {
        var transformed = action
        when (action) {
            is KeyAction.FcitxKeyAction -> when (source) {
                KeyActionListener.Source.Keyboard -> {
                    when (capsState) {
                        CapsState.None -> {
                            transformed = action.copy(act = action.act.lowercase())
                        }
                        CapsState.Once -> {
                            transformed = action.copy(
                                act = action.act.uppercase(),
                                states = KeyStates(KeyState.Virtual, KeyState.Shift)
                            )
                            switchCapsState()
                        }
                        CapsState.Lock -> {
                            transformed = action.copy(
                                act = action.act.uppercase(),
                                states = KeyStates(KeyState.Virtual, KeyState.CapsLock)
                            )
                        }
                    }
                }
                KeyActionListener.Source.Popup -> {
                    if (capsState == CapsState.Once) {
                        switchCapsState()
                    }
                }
            }
            is KeyAction.CapsAction -> switchCapsState(action.lock)
            else -> {}
        }
        super.onAction(transformed, source)
    }

    override fun onAttach() {
        super.onAttach() // Call super if BaseKeyboard has onAttach
        capsState = CapsState.None
        updateCapsButtonIcon()
        // Initial update using currently available data (might be null/empty if not loaded)
        updateAlphabetKeys()
        
        // Start collecting layout updates
        viewCollectJob?.cancel() // Cancel previous job if any
        viewCollectJob = keyboardScope.launch {
            textLayoutJsonMapFlow.collectLatest { layoutMap ->
                // This collection will trigger whenever the global layoutMapFlow updates
                // We then call updateAlphabetKeys which uses the current IME's specific layout
                Log.d(TAG, "Layout data updated via flow. Refreshing alphabet keys.")
                updateAlphabetKeys()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateLangSwitchKey(showLangSwitchKey.getValue())
        showLangSwitchKey.registerOnChangeListener(showLangSwitchKeyListener)
        // Ensure layouts are loaded if this keyboard becomes visible again
        loadLayoutsIfNeededAsync()
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        showLangSwitchKey.unregisterOnChangeListener(showLangSwitchKeyListener)
        viewCollectJob?.cancel() // Stop collecting when view is detached
    }

    override fun onReturnDrawableUpdate(returnDrawable: Int) {
        `return`.img.imageResource = returnDrawable
    }

    override fun onPunctuationUpdate(mapping: Map<String, String>) {
        punctuationMapping = mapping
        updatePunctuationKeys()
    }

    override fun onInputMethodUpdate(imeUpdate: InputMethodEntry) {
        // Update companion object's ime. This static reference might need careful handling
        // if multiple TextKeyboard instances could exist with different IMEs.
        TextKeyboard.ime = imeUpdate 
        Log.d(TAG, "Input method updated to: ${imeUpdate.uniqueName}. Refreshing alphabet keys.")
        updateAlphabetKeys() // This will now use the async-loaded layout
        space.mainText.text = buildString {
            append(imeUpdate.displayName)
            imeUpdate.subMode.run { label.ifEmpty { name.ifEmpty { null } } }?.let { append(" ($it)") }
        }
        if (capsState != CapsState.None) {
            switchCapsState() // Reset caps state on IME change
        }
    }

    private fun transformPopupPreview(c: String): String {
        if (c.length != 1) return c
        if (c[0].isLetter()) return transformAlphabet(c)
        return transformPunctuation(c)
    }

    override fun onPopupAction(action: PopupAction) {
        val newAction = when (action) {
            is PopupAction.PreviewAction -> action.copy(content = transformPopupPreview(action.content))
            is PopupAction.PreviewUpdateAction -> action.copy(content = transformPopupPreview(action.content))
            is PopupAction.ShowKeyboardAction -> {
                val label = action.keyboard.label
                if (label.length == 1 && label[0].isLetter())
                    action.copy(keyboard = KeyDef.Popup.Keyboard(transformAlphabet(label)))
                else action
            }
            else -> action
        }
        super.onPopupAction(newAction)
    }

    private fun switchCapsState(lock: Boolean = false) {
        capsState =
            if (lock) {
                when (capsState) {
                    CapsState.Lock -> CapsState.None
                    else -> CapsState.Lock
                }
            } else {
                when (capsState) {
                    CapsState.None -> CapsState.Once
                    else -> CapsState.None
                }
            }
        updateCapsButtonIcon()
        updateAlphabetKeys()
    }

    private fun updateCapsButtonIcon() {
        caps.img.apply {
            imageResource = when (capsState) {
                CapsState.None -> R.drawable.ic_capslock_none
                CapsState.Once -> R.drawable.ic_capslock_once
                CapsState.Lock -> R.drawable.ic_capslock_lock
            }
        }
    }

    private fun updateLangSwitchKey(visible: Boolean) {
        lang.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun updateAlphabetKeys() {
        // Use TextKeyboard.ime as it's updated by onInputMethodUpdate
        val currentIme = TextKeyboard.ime 
        if (currentIme == null) {
            Log.w(TAG, "Cannot update alphabet keys, IME is null.")
            // Optionally, clear keys or set to a default state
            // textKeys.forEach { it.mainText.text = "" }
            return
        }
        
        // getTextLayoutJsonForIme now gets data from the flow's current value (potentially null)
        val layoutJson = getTextLayoutJsonForIme(currentIme.uniqueName)

        if (layoutJson != null) {
            Log.d(TAG, "Applying custom layout for ${currentIme.uniqueName}")
            textKeys.forEach { keyView ->
                if (keyView.def !is KeyDef.Appearance.AltText) return@forEach // Assuming def is set
                val keyJson = layoutJson.flatten().find { key -> key.main == keyView.def.character }
                val displayText = if (keyJson != null ) {
                  DisplayTextResolver.resolve(
                    keyJson.displayText,
                    currentIme.subMode?.label ?: "",
                    keyJson.main ?: "" // Fallback to main if displayText logic returns nothing useful
                  )
                } else {
                  keyView.def.character // Fallback to original def character
                }

                keyView.mainText.text = displayText.let { str ->
                    if (str.isEmpty()) keyView.def.character // further fallback if display text ends up empty
                    else if (keepLettersUppercase) {
                      keyJson?.main?.uppercase() ?: str.uppercase()
                    } else {
                      when(capsState) {
                        CapsState.None -> str.lowercase()
                        else -> str.uppercase()
                      }
                    }
                }
            }
        } else {
            // Fallback to default behavior if no custom layout is found or loaded
            Log.d(TAG, "No custom layout for ${currentIme.uniqueName}, using default key definitions.")
            textKeys.forEach { keyView ->
                if (keyView.def !is KeyDef.Appearance.AltText) return@forEach
                 // Ensure def.displayText is not null, provide fallback
                val originalDisplayText = keyView.def.displayText ?: keyView.def.character ?: ""
                if (originalDisplayText.length == 1 && originalDisplayText[0].isLetter()) {
                     keyView.mainText.text = if (keepLettersUppercase) originalDisplayText.uppercase() else transformAlphabet(originalDisplayText)
                } else {
                    keyView.mainText.text = originalDisplayText // Use as is if not a single letter
                }
            }
        }
    }

    private fun updatePunctuationKeys() {
        textKeys.forEach {
            if (it is AltTextKeyView) {
                it.def as KeyDef.Appearance.AltText
                it.altText.text = transformPunctuation(it.def.altText)
            } else {
                // Ensure def and displayText are appropriately handled if they could be null
                (it.def as? KeyDef.Appearance.Text)?.displayText?.let { str ->
                    if (str.isNotEmpty() && str[0].run { isLetter() || isWhitespace() }) return@forEach
                    it.mainText.text = transformPunctuation(str)
                }
            }
        }
    }
}
