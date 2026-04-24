# XtyMegaMod 会话记忆

## 1. 工作区与上下文

当前主工作区：

- `E:/CollapsingWorld/System-e/XtyMegaMod`

当前默认使用的主 skill：

- `xty-mega-mod-forge-1201`

当前相关的前置库工作区：

- `E:/CollapsingWorld/System-e/EndingLibrary`

前置库相关 skill：

- `ending-library-forge-1201`

这份文件用于记录本次长期有效的工程约束、界面风格、音频接入规则、炸弹系统逻辑与命令约定，供后续继续开发时直接参考。

## 2. 长期协作约束

### 2.1 编译与验证

- 用户已经明确同意以后可以直接运行 Gradle 编译测试。
- `XtyMegaMod` 的 `compileJava`、`processResources` 已多次验证可正常执行。
- 普通权限下偶尔会遇到 Gradle wrapper 的 `.zip.lck` 访问问题；必要时可以提升权限执行。

### 2.2 API 真实性要求

- 所有 Forge 1.20.1 / Minecraft / EndingLibrary / XtyMegaMod 相关代码必须以本地真实源码和缓存 jar 为准。
- 不允许臆造类、字段、方法、事件名、资源路径。
- 需要优先遵守当前工作区已有结构和实际 API。

### 2.3 中文文本与编码

- 写中文代码、注释、语言文件时，若无特殊要求，不使用 Unicode 转义。
- 已确认仓库里关键 Java / JSON 文件本体大多是 UTF-8。
- 之前出现的中文乱码主要来自终端输出链路编码不一致，不完全是文件本身损坏。
- `PointsOverlay` 中被写坏的 Javadoc 已被修复为正常中文。

## 3. FPS Overlay 视觉基准

今后编写与 FPS 相关的 overlay，默认以：

- `com.mega.xty.client.overlay.map2.C4Overlay#renderBombCountdown`

作为视觉基准。

### 3.1 这套基准的关键特点

- HUD 中下区域显示。
- 整体以屏幕中心为锚点布局。
- 使用模糊背景条，而不是纯色实心底板。
- 左右边框单独绘制，边框颜色受队伍色影响。
- 容器宽度不是固定值，而是根据文本宽度动态计算。
- 实际显示宽度由 `alpha` 驱动，从中心向两边展开。
- 文字显示必须和展开动画同步，使用 `enableScissor(...)` 做裁剪。
- 文本默认是偏灰白色，不是纯白。
- 可加入一层淡白色覆盖，模拟 CS2 提示条的白色淡入淡出感。

### 3.2 以后写 FPS overlay 时默认遵守

- 先决定内容文本，再决定目标宽度。
- 再用 easing 驱动实际展开宽度。
- 再在展开宽度基础上绘制模糊条、边框、scissor 裁剪和文本。
- 若没有特殊要求，不随意改成其他风格。

## 4. 已完成的渲染工具与 Shader

### 4.1 模糊矩形渲染

已新增：

- `com.mega.xty.client.renderer.BlurRectRenderer`

核心思路：

- 先把主 framebuffer 复制到临时 `TextureTarget`
- 再用 shader 采样复制后的纹理
- 避免一边读主 framebuffer 一边写主 framebuffer 的未定义行为

### 4.2 logo glitch shader

已新增：

- `logo_glitch.json`
- `logo_glitch.fsh`

用途：

- 用于 logo / icon 类小图标的故障效果
- 不是全屏 post effect，而是 core shader
- 可配合 `MegaGuiGraphics.blit(..., ModShaders::getLogoGlitch)` 使用

### 4.3 PointsOverlay 投影逻辑

`PointsOverlay#projectToScreen` 已改为：

- 使用 `w=1` 作为点
- 使用 `w` 做透视除法
- 屏幕外点压到边框
- 后方点也压到边框
- 不再对后方点翻转 `ndcX/ndcY`

另外：

- `EndingLibrary` 中 `LEVEL_MODEL_VIEW_MAT` 的采集方式曾被修正为包含 `-cameraPos`
- 当前 `PointsOverlay` 的投影逻辑已按现有工作区状态调整

## 5. 音频资源整理结果

### 5.1 源音频处理

原始目录：

- `D:/qq/c4`

已完成处理：

- 将其中的 `.wav` / `.mp3` 全部转成 `.ogg`
- 后续又将这些 `.ogg` 移入模组资源目录

目标资源目录：

- `src/main/resources/assets/xtymegamod/sounds/fps/c4/`

### 5.2 sounds.json 与注册

已完成：

- `sounds.json` 中写入所有 `fps.c4.*` 项
- `SoundsInit` 中注册了所有对应声音

当前声音注册统一使用：

- 固定听距 `32` 格

### 5.3 这批声音的语义大致约定

- 下包开始：`c4_click`
- 下包输入：`key_press1` 到 `key_press7`
- 安放完成：`c4_plant`
- 启动：`c4_initiate`
- 拆包开始：`c4_disarmstart`
- 拆包完成：`c4_disarmfinish`
- 爆炸主体：`c4_explode1`
- 爆炸残响：`c4_exp_deb1` / `c4_exp_deb2`
- 滴滴声：目前统一只使用 `c4_beep2`

## 6. C4 / BDK 当前物品逻辑

### 6.1 `C4BombItem`

当前已经接入的音效行为：

- 成功开始下包时播放 `C4_CLICK`
- 下包过程中按索引位置播放 `KEY_PRESS1..7`
- 下包完成时播放 `C4_PLANT`
- 下包完成时播放 `C4_INITIATE`

额外说明：

- 下包过程中按键音已经改为按“密码位置索引”播放，不再按密码数字本身播放
- 中途停止下包时，后续按键音不会继续触发

### 6.2 `BDKItem`

当前已经接入的音效行为：

- 成功开始拆包时播放 `C4_DISARMSTART`
- 拆包完成时播放 `C4_DISARMFINISH`
- 中途停止拆包时，`onStopUsing` 会发送 `ClientboundStopSoundPacket` 停止开始拆包声

当前逻辑还包括：

- 拆包成功后直接结束炸弹状态
- 用户后续要求中，拆包成功也应当直接结束游戏

## 7. 炸弹服务端 / 客户端倒计时系统

### 7.1 服务端数据

类：

- `com.mega.xty.common.data.fps.FpsSavedData`

当前核心字段：

- `bombExist`
- `bombPosition`
- `bombCountdownTicks`

当前约束：

- 服务端每 tick 让 `bombCountdownTicks` 自减
- `bombCountdownTicks` 参与 NBT 存档
- `onBombCountdownFinished()` 保留为空

### 7.2 客户端数据

类：

- `com.mega.xty.common.data.fps.ClientFpsData`

当前核心字段：

- `bombExist`
- `bombPosition`
- `bombCountdownTicks`
- `bombCountdownRenderTicks`
- `bombCountdownRenderTimer`
- `bombPlantedTickCount`

当前约束：

- 客户端本地每 tick 递减 `bombCountdownTicks`
- 客户端 `onBombCountdownFinished()` 保留为空
- 客户端根据本地剩余时间播放滴滴声
- 客户端每 15 秒请求一次倒计时提示框
- 安放瞬间也会立刻触发一次倒计时提示框

### 7.3 同步包

类：

- `S2CBombDataPacket`

当前同步内容：

- `bombExist`
- `bombPosition`
- `bombCountdownTicks`

## 8. 炸弹倒计时与 map2 / game2 的关系

这一块经历过多轮调整，当前要点如下。

### 8.1 map2 原倒计时

原始 map2 倒计时主驱动仍在：

- `com.mega.xty.common.event.map2.GameCommonEvents#onServerTick`

它本来负责：

- `Map2SavedData.countdown--`
- countdown 归零时调用 `Map2Functions#getCountdownStopFunction()`

### 8.2 炸弹安放后的处理

用户当前要求是：

- 炸弹安放时，若 `map2 game2` 正在游玩，则立即把原 map2 倒计时归零
- 但此时不能调用 `countdownStopFunction`

当前实现状态：

- `GameCommonEvents#onServerTick` 中，如果 `game2` 正在游玩且炸弹存在，会把 `Map2SavedData.countdown` 强制压成 `0`
- 只有在没有炸弹时，才继续走原始 `countdown-- -> countdownStopFunction` 逻辑
- `C4BombItem#finishUsingItem(...)` 在安包成功时也会补一次把 countdown 设成 `0` 并同步，避免安包瞬间 HUD 不一致

### 8.3 炸弹爆炸后的处理

用户当前要求是：

- 爆炸只做粒子效果
- 爆炸音效只使用自定义 C4 ogg
- 若 `map2 game2` 正在游玩，则在爆炸时调用游戏结束函数

当前实现状态：

- `FpsSavedData#explodeBombEffects()` 中会：
- 找到 C4 实体
- 发粒子
- 播放 `C4_EXPLODE1`
- 再播一层 `C4_EXP_DEB1` 或 `C4_EXP_DEB2`
- 移除 `C4Entity`
- 若 `game2` 正在游玩，则调用：
  - `Map2SavedData.getMap2Functions().getCountdownStopFunction()`

### 8.4 当前的职责分布

- `GameCommonEvents` 负责“原 map2 倒计时流程”
- `FpsSavedData` 负责“炸弹自己的倒计时和爆炸效果”
- `C4BombItem` 负责“安包时清零原 map2 countdown 的补充同步”

## 9. 客户端 C4 倒计时提示框

类：

- `com.mega.xty.client.overlay.map2.C4Overlay`

当前 `renderBombCountdown(...)` 的职责：

- 在 HUD 中下区域绘制一个模糊提示框
- 两行文本：
  - “炸弹已被安放”
  - “离被引爆还剩 X 秒”
- 宽度由最长文本决定
- 用 `alpha` 驱动提示框从中间向外展开
- 用 `scissor` 裁剪文本
- 右侧与左侧都有边框
- 有一层 CS2 风格的白色覆盖淡入淡出效果

这套方法已被明确指定为以后 FPS overlay 的默认视觉基准。

## 10. KillCountOverlay 中的 C4 图标颜色逻辑

类：

- `com.mega.xty.client.overlay.map2.KillCountOverlay`

当前约定：

- C4 图标会根据当前炸弹滴滴节奏变色
- 刚 beep 的瞬间是纯红
- beep 之后逐渐回到淡红
- 紧促程度跟炸弹 beep 的频率分段一致
- 已经从原来塞在一行里的超长表达式拆成了辅助方法

## 11. FpsCommand 与 Spyglass

### 11.1 已新增命令

根命令：

- `fps`

已新增炸弹子命令：

- `fps bomb set exist <bool>`
- `fps bomb set position <0..2>`
- `fps bomb set countdown <seconds>`
- `fps bomb get exist`
- `fps bomb get position`
- `fps bomb get countdown`

### 11.2 命令行为约定

- `set countdown` 参数现在使用“秒”，内部再乘 `20` 变成 tick
- `set exist` / `set position` 修改后会再触发一次同步
- `get ...` 输出已经改成语言文件版本

### 11.3 Spyglass

文件：

- `spyglass/extra_commands.json`

当前已经同步更新，Spyglass 可以识别这些新命令。

## 12. 语言文件现状

文件：

- `src/main/resources/assets/xtymegamod/lang/en_us.json`

当前状态：

- 文件本体是有效 UTF-8 JSON
- 新增的 `fps bomb get ...` 语言项已经改成直接中文，不再用 Unicode 转义

当前新增键：

- `commands.xtymegamod.message.fps.bomb.exist.get`
- `commands.xtymegamod.message.fps.bomb.position.get`
- `commands.xtymegamod.message.fps.bomb.countdown.get`

## 13. 以后继续开发时默认遵守

- 若任务涉及 FPS HUD / 提示框 / 安包拆包 HUD，优先延续 `C4Overlay#renderBombCountdown` 的视觉语言。
- 若任务涉及 C4 / BDK / 炸弹倒计时，优先以当前这条链路为准：
  - `C4BombItem`
  - `BDKItem`
  - `FpsSavedData`
  - `ClientFpsData`
  - `GameCommonEvents`
  - `C4Overlay`
- 若任务涉及中文文本写入：
  - 优先直接中文
  - 除非有特殊原因，不写 Unicode 转义
