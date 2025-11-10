# Fcitx5 Android 字体配置功能实现

## 概述

本功能为 Fcitx5-android 项目添加了完整的字体配置管理功能，允许用户在设置界面中直接选择和配置不同组件的字体。

## 功能特性

### 支持的字体配置项
- **候选字体** (`cand_font`): 候选词显示字体
- **编码字体** (`preedit_font`): 输入编码显示字体
- **弹出按键字体** (`popup_key_font`): 弹出键盘按键字体
- **按键主字体** (`key_main_font`): 主要按键文字字体
- **按键次字体** (`key_alt_font`): 按键辅助文字字体
- **默认字体** (`font`): AutoScaleTextView 默认字体

### 核心功能
1. **字体文件管理**: 自动扫描 `fonts/` 目录下的 `.ttf` 和 `.otf` 字体文件
2. **实时配置**: 配置更改后立即生效，无需重启应用
3. **配置持久化**: 自动保存配置到 `fonts/fontset.json` 文件
4. **系统默认支持**: 可选择使用系统默认字体

## 实现细节

### 文件结构

```
app/src/main/java/org/fcitx/fcitx5/android/
├── data/
│   ├── fonts/
│   │   ├── FontManager.kt           # 字体管理核心类
│   │   └── FontConfigTest.kt        # 测试工具类
│   └── prefs/
│       └── AppPrefs.kt              # 添加了 Fonts 配置类
└── ui/main/settings/behavior/
    └── FontsSettingsFragment.kt     # 字体设置界面
```

### 核心类说明

#### FontManager
- 管理字体文件扫描和字体选择对话框
- 处理字体配置的读写和更新
- 提供字体缓存清理功能

#### AppPrefs.Fonts
- 管理字体配置的 SharedPreferences 存储
- 监听配置更改并触发自动更新

#### FontsSettingsFragment
- 提供用户友好的字体选择界面
- 显示当前选择的字体名称
- 集成字体选择对话框

### 配置文件格式

字体配置保存在 `fonts/fontset.json` 文件中：

```json
{
  "cand_font": "LXGWWenKai-Regular.ttf",
  "font": "",
  "preedit_font": "segoepr.ttf",
  "popup_key_font": "segoepr.ttf",
  "key_main_font": "segoepr.ttf",
  "key_alt_font": "segoepr.ttf"
}
```

空字符串表示使用系统默认字体。

## 使用方法

### 用户操作
1. 将字体文件 (`.ttf` 或 `.otf`) 放入应用的 `fonts/` 目录
2. 打开 Fcitx5 设置 → 字体
3. 点击要配置的字体项目
4. 从弹出的列表中选择字体
5. 配置立即生效

### 开发者集成
```kotlin
// 手动更新字体配置
FontManager.updateFontConfiguration()

// 获取可用字体列表
val fonts = FontManager.getAvailableFonts()

// 显示字体选择对话框
FontManager.showFontPickerDialog(context, R.string.font_title) { selectedFont ->
    // 处理选择的字体
}
```

## 技术实现亮点

1. **模块化设计**: 字体管理功能独立模块，易于维护和扩展
2. **异常安全**: 完善的错误处理，避免因字体配置问题导致应用崩溃
3. **性能优化**: 字体缓存机制，避免重复加载
4. **用户体验**: 实时预览字体名称，直观的选择界面
5. **向后兼容**: 兼容现有的字体配置文件格式

## 国际化支持

已添加中英文字符串资源：
- 英文: `values/strings.xml`
- 简体中文: `values-zh-rCN/strings.xml`

## 测试

使用 `FontConfigTest` 类进行功能验证：
```kotlin
// 创建测试配置
FontConfigTest.createTestFontConfig()

// 验证配置正确性
val isValid = FontConfigTest.validateFontConfig()
```

## 注意事项

1. 字体文件需要放在应用的外部存储 `fonts/` 目录下
2. 支持的字体格式：`.ttf`, `.otf`
3. 配置更改后会自动清理字体缓存以确保更改生效
4. 空字体文件名表示使用系统默认字体

## 未来扩展

- 字体预览功能
- 字体大小配置
- 字体样式配置（粗体、斜体等）
- 字体文件在线下载功能
