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
- 以后若单次代码改动超过 300 行，需要进行编译检测。

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

## 14. 2026-04-30 长对话简化记录

本节用于压缩记录本轮较长对话中已经形成的工程状态和后续约束。以后新对话继续处理 `XtyMegaMod` 时，若涉及下列系统，应优先读取本节并核对实时源码。

### 14.1 通用协作约定

- 工作区仍为 `E:/CollapsingWorld/System-e/XtyMegaMod`。
- 涉及前置库或 EndingLibrary API 时，再读取 `E:/CollapsingWorld/System-e/EndingLibrary` 的真实源码，不凭印象写 API。
- 用户偏好中文回答，代码和语言文件里的中文默认使用直接中文，不使用 Unicode 转义。
- 若单次代码改动超过 300 行，应进行编译检测。
- 当前仓库存在较多连续任务形成的未提交改动，后续不要随意回滚无关文件。

### 14.2 C4 HUD 与 KillCountOverlay

- `C4Overlay#renderBombCountdown` 继续作为 FPS 通知提示框的视觉基准。
- KillCountOverlay 的 C4 图标效果已经调整过：
  - 原效果下方额外渲染一层 `C4_2` 纹理，颜色为 `0x88001b`。
  - 滴滴后，原效果颜色从纯红过渡到 `0x88001b` 时，透明度从 `1.0F` 渐变到 `0.0F`。
  - 透明度 easing 最终应使用 `IN_OUT_SINE`。
  - 曾经要求“10s 后快速滴滴期间禁用透明度变低”，后来已撤回，不应保留该限制。
- C4 物品按 Minecraft 使用逻辑应在使用后消耗。
- `SoundsInit` 中只有 C4 滴滴声的播放范围被要求限制为 32 格。

### 14.3 回合胜利/失败 Overlay

- 已新增或调整 `WinOverlay` / `LoseOverlay`，严格参考 C4Overlay 的通知式写法。
- 相关通知和时间字段放在 `ClientFpsData`。
- MVP 选择使用 `ClientFpsData#PLAYER_COMPARATOR`，由在线玩家 current KAD 排序获得。
- 胜利/失败提示框：
  - “回合胜利/回合失败”提示框显示时间增加 4s。
  - MVP 提示框推迟 1s 出现，并与主提示框同时结束。
  - MVP 提示框宽度加长过 25%。
  - MVP 头像放大 1.25x；若超过提示框宽度，应等比例扩大提示框。
  - 文本渲染包含 shadow。
  - 击杀数使用金色。
- `LoseOverlay#renderMvpBackground` 使用着色器 blit 时要注意 `graphics.flush()`，因为需要在切换 shader / buffer 状态前让前面的 GUI 批次真正提交。
- `WinOverlay` 的 MVP 背景写法应与 `LoseOverlay` 保持一致。
- 已添加用于触发 `ClientFpsData#requestRoundWinRender` 和 `requestRoundLoseRender` 的网络包，若后续要再加包，先检查是否已存在。

### 14.4 后处理着色器

- `game2_aqua` post effect：
  - 已有 `TotalTime` uniform，单位秒。
  - 时间小于 5 秒时使用 RGB `(0.8, 0.8, 1.1)` 滤镜。
  - 0 秒颜色遮盖透明度为 1.0，第 5 秒为 0.0。
  - 启用方法曾按要求临时返回 `false`。
- `DeadPostEffect` / `dead` shader 多轮调整后的设计方向：
  - 深红死亡滤镜。
  - 暗角作为基础。
  - 2s 后黑屏效果基于暗角向中间扩散，同时降低饱和度。
  - 黑屏内侧边缘要更平滑透明。
  - 饱和度最低降低到原先的 30%。
  - 饱和度降低逻辑参考 `grayscale_position_tex.fsh`。

### 14.5 game2 死亡相机与死亡流程

- 死亡效果相关类已移动到 `com.mega.xty.common.event.map2`：
  - `DeathCameraEffectHandler`
  - `C4SpectateCameraHandler`
- 玩家死亡时的相机动画不再直接使用 EndingLibrary 动画 API，而使用 EndingLibrary 的相机坐标事件一类的方式处理。
- `restoreCameraState` 等位置不应残留旧相机动画 API 调用。
- 死亡屏幕效果时长与相机占用时间一致，由服务端通知客户端应用；玩家播放期间重进游戏后不再播放。
- 玩家在死亡屏幕效果期间不渲染 `SelSelectPlayerOverlay`。
- 玩家死亡后相机动画数据需要保存死亡时坐标和视角旋转，一起同步、设置和清空。
- 死亡视角动画：
  - 坐标动画延迟到 3s。
  - 视角缓慢向下，时间 1s，`Easing.OUT_SINE`。
  - 向下旋转最终弱化为 60 度。
  - 曾添加 z 轴 roll 动画，最终在玩家面向 C4 时要求不再由原死亡动画设置 roll。
- C4 旁观视角：
  - 玩家无人可旁观时，可锁定到 C4 周围位置。
  - 面向 C4 的实现后来改成设置玩家实体朝向，不再使用 `ComputeCameraAngles` 事件。
  - 在面向 C4 时禁用死亡视角动画的相机视角设置。
  - 玩家死亡后锁定到 C4 周围时，不应设置实体坐标，只应移动相机坐标。
  - 锁定时禁用鼠标转动输入，不再锁定后解除。
- 玩家死亡状态下：
  - 无视重力和碰撞。
  - 使用 `Invulnerable` 标签保证无敌，写在 xaero dead 赋值逻辑里。
  - `Map2Capability#getSoulInvisible >= 15` 的玩家，对其他队伍非创造/旁观玩家不渲染。

### 14.6 Map2Capability 的 C4 旁观位置

- `Map2Capability` 中新增同步数据 `PLAYER_C4_POS`，类型为 `Optional<Vector3f>`，需要序列化存储。
- 用于存储玩家在无人可以旁观时，位于 C4 附近的旁观相机位置。
- 相关操作包含 getter、setter、序列化/反序列化、同步。
- 搜索逻辑：
  - 在 Point A、Point B 周围 64x64x64 查找 C4 实体。
  - 找到后，在 C4 周围 32x32x32 随机选取候选位置。
  - 位置与 C4 连线无方块碰撞时写入能力。
  - 候选尝试次数提升到 256 次。
- 客户端同步到位置数据后，如果玩家死亡且 map2 game2 正在游玩，则相机移动到该坐标并让玩家朝向 C4。
- 玩家未死亡或游戏未开始时清空该坐标。
- 不要“修复” `aliveSameTeamPlayersWithoutLocal` 排除 xaero dead 玩家这一点；若某段逻辑需要活着的队友，应在遍历使用时单独排除。

### 14.7 game2 回合状态与流程

- `Map2SavedData` 添加过：
  - `redHome`
  - `blueHome`
  - game2 游玩维度字段
  - `maxWins`
- `redHome` / `blueHome` 使用 `ObjectArrayList`。
- home 坐标集合最大长度为 10，不同步到客户端，需要 NBT 序列化、反序列化、getter、setter，setter 需要 `setDirty()`。
- `backToHome` / 内部 home 传送逻辑：
  - 红队玩家传送到 game2 维度的 `redHome` 随机位置。
  - 蓝队玩家传送到 game2 维度的 `blueHome` 随机位置。
  - 只对前 home 列表长度个人玩家操作。
- `startNewRound` 已重命名为 `startGame2NewRound`。
- `startGame2NewRound` 当前职责包括：
  - 若 `redWins` 或 `blueWins` 大于等于 `maxWins`，将双方胜局清 0，并结束 game2，然后返回。
  - 调用 `backToHome`，且该调用必须发生在复活死亡玩家之后。
  - 所有玩家恢复血量、清空 buff、清空着火时间，最大血量设为 100。
  - 对传送到 home 的玩家禁用前后左右、跳跃、鼠标左键等输入。
  - 10s 后解除输入禁用；若玩家期间退出，写入 Map2 capability 标记，让其下次同步/进服时清除禁用输入。
  - 对传送到 home 的玩家播放 `Game2StartPostEffect` 10s，玩家退出后效果时间归 0。
  - 红队胸甲设为 `OPTICAL_NANOSUIT`。
  - 蓝队胸甲设为最大耐久 10 的锁链甲，最大耐久通过 EndingLibrary 物品组件修改。
  - 回合开始后清除世界中所有 C4 实体。
  - 回合开始后清除世界上的掉落物。
  - 回合开始时调用 `Game2Functions#startNewRoundFunction`。
- `setStopped(false)` 表示 game2 开始但不包括回合开始；`setStopped(true)` 才表示 game2 结束。
- 因此“game2 结束时”的清理逻辑应放在 `Game2SavedData#setStopped(true)` 对应路径，不应误放进 `Map2SavedData#setStopped(false)`。
- game2 结束时：
  - 复活玩家。
  - 清空所有玩家的键盘输入禁用效果。
  - 最大血量恢复为 20。
  - 清空红蓝队所有非创造/旁观玩家护甲栏。
  - 将所有人传送至各自出生点。
  - 清除世界中的 C4 实体和 GrenadeEntity 系投掷物实体。
  - 服务端设置 C4 不存在并同步客户端。

### 14.8 game2 回合胜负与自动下一回合

- `Map2SavedData#finish(redWin)` 中向所有玩家客户端播放胜利音频：
  - `redWin == true` 播放 `twin`
  - 否则播放 `ctwin`
- game2 一回合结束 7s 后自动开始新一回合。
- `maxWins` 有命令，用户后来要求 maxWins 命令写在 `map2` 下。
- 当玩家死亡时：
  - 若 game2 正在游玩，并且死亡玩家队伍所有玩家都已 xaero dead，则调用 `Map2SavedData#finish` 结束回合。
  - 这段逻辑应写在当前死亡玩家被设置死亡之后。
  - 红队全死时不能直接判蓝队胜，要先检测是否存在 C4；若存在 C4 则不结束回合，等待 C4 爆炸或拆除。
- 摔落死亡也要进入完整 game2 死亡流程：
  - KAD 计算、`setXaeroDead` 等按原玩家击杀流程处理。
  - 摔死玩家清空自己在 `FpsCapability` 中的死亡信息。
  - 直接发送一条 `DeathData`，内容为：`<玩家名> 摔死了`。
- 死亡玩家若没有同队玩家可以附身：
  - C4 镜头优先。
  - 没有 C4 镜头时，再随机附身一名非本队玩家。

### 14.9 game2 回合物品与清理

- 红队新回合：
  - 清空所有玩家的 C4。
  - 随机挑选一个红队玩家给予一个 C4。
  - 在给予 C4 前，应用其当前选中武器仓库背包选项中的近战武器。
- 蓝队新回合：
  - 在给予 C4 钳前，应用其当前选中的武器仓库背包选项。
  - 若背包没有 C4 钳，给予一个 C4 钳。
  - 将背包内所有继承 `IGun` 的物品子弹填满。
  - 在原有给予物品基础上，额外给予 TACZ 的“全类型创造弹药盒”。
  - TACZ 弹药盒已验证使用 `ModItems.AMMO_BOX` 与 `IAmmoBox#setCreative(..., true)` 设置 `AllTypeCreative`。
- game2 结束或回合结束背包清理：
  - 不应清掉玩家仓库自选数据，仓库数据是能力/配置，不是实际背包物品。
  - 实际背包中需要清理武器相关物品。
  - 后来要求清理所有继承 `IGun` 的物品、剑类、斧类、投掷物、弹药盒子等 game2 物品。
  - 需要杀死场上的投掷物实体与投掷物生成实体，继承自 `me.xjqsh.lrtactical.entity.GrenadeEntity`。

### 14.10 RoundStartOverlay

- 新增 `RoundStartOverlay`，用于渲染回合启动广播倒计时提示框。
- 暂定不只用于 map2 game2，因此数据字段/方法写入 `com.mega.xty.common.data.fps.RoundStartData`。
- 提示框仿照 C4Overlay 写法。
- 文本：
  - 第一行：`回合即将开始`
  - 第二行：`倒计时<时间>秒`
- 展示 11s，渲染倒计时为 10 到 0 秒。
- 注意新回合倒计时时间可能不足 10s 的风险，后续涉及倒计时调度时需要严谨核对任务调度条件。

### 14.11 WeaponWarehouse 武器仓库系统

- 已编写玩家自主选择配置“武器仓库”的系统，包含双端数据、UI、命令等。
- 玩家可配置 5 个背包。
- 每个背包槽位：
  - 1 号主武器
  - 2 号副武器
  - 3 号近战武器
  - 4~6 号投掷物：m67 手雷、烟雾弹、闪光弹，使用 LesRaisins 物品
- 默认没有任何物品，除了近战武器默认为 LesRaisins 爪刀。
- 玩家实际数据建议存入新的实体能力；当前已有相关能力/包/UI实现，继续修改前查源码。
- 已补游戏内打开仓库界面的命令。
- UI 调整记录：
  - 界面曾过大，已做屏幕大小适配。
  - 调整背包栏与可选项框大小，避免 5/6 号栏和可选项框重叠。
  - 去掉了“主武器预览”展示框。
  - 按钮不再使用原版按钮，改为更现代的自绘按钮。
  - 搜索框使用服务于本 Screen 的 `WarehouseSearchBox` / EditBox 子类。
  - 搜索框文本 y 轴居中。
  - 无文本时渲染提示词 `输入要查找的武器名`。
  - 可选项提示框宽度后来缩小，避免和拖动条重叠。
  - 拖动条加粗，拖动时要有手感。
  - 3x2 网格物品名超过本网格边界时需要裁剪。
- 可选项逻辑：
  - 重复物品只算一个。
  - 曾修复主武器可选项过少的问题，TACZ 自带枪械数量应从创造标签/物品数据中尽量完整收集。
  - 可选项有可视滚动条，拖动滑条后可以选择其他武器。
  - 曾撤回“第一个选项永远为空”的修正，`ItemStack.EMPTY` 仍应作为可选空位存在。
  - 近战物品 `lrtactical:melee` 的 ID 相同，不能只靠 item id 区分，可能需要读 ItemStack NBT/组件区分具体近战。
- Game2 自定义仓库近战：
  - `Game2SavedData` 存储额外近战可选项，类型为自定义 `ItemStack` 列表。
  - 命令增加近战物品时读取执行玩家主手物品。
  - 需要序列化/反序列化、getter、setter，setter 要 `setDirty()`。
  - 由于这是服务端配置，注意同步到客户端仓库 UI 的问题。
  - `isValidWarehouseMelee` 不应排除手动添加的非 `IMeeleWeapon` 物品。
  - `Game2WarehouseMeleeCommand` 的聊天栏输出语言文件需要补齐。

### 14.12 命令与 Spyglass

- 多个命令曾被移动或新增：
  - `game2Dimension`
  - `startGame2NewRound`
  - `redHome`
  - `blueHome`
  - `maxWins`
  - `Game2Functions#startNewRoundFunction`
  - game2 仓库近战命令
  - 打开武器仓库界面的命令
- `game2Dimension`、`startGame2NewRound`、`redHome`、`blueHome` 后来要求移动到 `map2 game2` 子命令下。
- `maxWins` 后来要求写在 `map2` 下。
- `Game2Functions` 中新增 `startNewRoundFunction` 字段：
  - 写法模仿已有函数字段。
  - game2 新回合开始时调用。
  - 需要命令，位置和写法与其他函数字段一致。
- 对所有命令，`spyglass/extra_commands.json` 应按 `spyglass/commands.json` 格式补全；格式变化的命令要移除旧 JSON 再写入新定义。

### 14.13 音频资源

- `C:/Users/Administrator/Desktop/fps/to_ogg` 下音频/视频曾被处理：
  - 音频转换为 `.ogg` 并导入 `sounds.json`。
  - 注册到 `SoundsInit`。
  - mp4 视频提取人耳能听到的声音部分为 mp3，再调整音量并导出 ogg。
  - 阈值尝试过 35dB、-15dB、-55dB、-75dB 等；后续如继续处理音频，要先确认用户要保留起步音还是去噪。
- 回合胜利声音：
  - `twin`
  - `ctwin`
- C4 声音注册中仅 C4 滴滴声范围应为 32 格。

### 14.14 物品切换组件

- 已要求编写“物品切换”组件：
  - 当玩家主手或副手出现上一 tick 不在手上的、带有此组件的物品时，调用组件的函数。
- 当前工作区中可见相关新增文件：
  - `src/main/java/com/mega/xty/common/component/ItemSwitchComponent.java`
- 相关组件包曾从 `common/item/component` 移动到 `common/component`，后续引用要检查实时源码，避免使用旧包名。

### 14.15 最近一次小改动：新 game2 回合清理掉落物

- 用户要求：开始新 game2 回合后清除世界上的掉落物。
- 当前实现位置：
  - `Map2SavedData#startGame2NewRound`
  - 在 `runStartNewRoundFunction()` 后、玩家复活/传送/发物品前调用 `clearWorldDroppedItems()`。
- `clearWorldDroppedItems()` 遍历所有 `ServerLevel` 的实体，收集并 `discard()` 所有 `ItemEntity`。
- 这样不会删除后续同一回合刚给予玩家、或因背包满而可能掉出的新物品。
- 已运行 `git diff --check -- src/main/java/com/mega/xty/common/data/map2/Map2SavedData.java`，无空白错误，仅有 Git LF/CRLF 提示。
## WinOverlay Prompt Baseline

- `com.mega.xty.client.overlay.map2.WinOverlay#renderTitleBox` 鏄綋鍓嶆ā浠?C4Overlay 妯＄硦鎻愮ず妗嗙殑鏂板熀鍑嗐€?
- 鑳屾櫙缁撴瀯锛?`graphics.flush()` -> `BlurRectRenderer.render(...)` -> 宸﹀彸 2px 杈规锛岃儗鏅鑹蹭负涓€х伆 `0x00303030`锛屾ā绯婂崐寰勪负 `alpha * 8.0F`銆?
- 瀹藉害灞曞紑锛氭彁绀烘浣跨敤 `realWidth = alpha * width`锛屼粠灞忓箷涓績鍚戜袱渚у睍寮€銆?
- 鏂囨湰瑁佸壀锛氭枃鏈粯鍒跺墠瑕佷娇鐢?`enableScissor(...)`锛岃鍓寖鍥村拰 `realWidth` 淇濇寔鍚屾銆?
- 瀛椾綋鍒囨崲锛氬綋 `realWidth < width` 鏃讹紝鏂囨湰浣跨敤 `ErrorFont.INSTANCE`锛涘畬鍏ㄥ睍寮€鍚庡垏鍥炴櫘閫?`Font`銆?
- 缁撳熬鐧介棯锛氱粨鏉熷墠 10 tick 淇濈暀涓€灞?`((255 - alpha * 255) << 24) | 0x00FFFFFF` 鐨勭櫧鑹查棯灞傘€?
- 2026-05-01 锛?`RoundStartOverlay` 宸叉寜杩欏鍐欐硶瀵归綈锛?`LoseOverlay` 褰撳墠宸茬粡鍜?`WinOverlay` 淇濇寔涓€鑷淬€?

## 15. 2026-05-02 至 2026-05-03 简要补充

本节只记录上次记忆文件更新后的新增工程约定，不包含 2026-05-04 当天关于 TACZ 枪包伤害数据学习、批量调参、单枪回滚与数值修改的对话。

### 15.1 game2 枪械开火拦截

- game2 新回合开始阶段禁止枪械开火，确认使用的事件为 `com.tacz.guns.api.event.common.GunFireEvent`。
- 用户明确要求不要复用 `map1` 区域现成类来承接这段逻辑，因此应在 `map2` 下单独处理。
- 当前做法是新增 `com.mega.xty.common.event.map2.Game2TaczEvents`：
  - 服务端在 game2 正在进行且玩家仍处于回合开始锁定期时取消开火。
  - 服务端对 `xaero dead` 玩家同样取消开火。
  - 客户端只拦截本地玩家自己的开火事件，避免误伤其他实体的事件流。
- `XtyMegaMod` 入口在 TACZ 已加载时注册 `Game2TaczEvents`，而 `TaczCommonEvents` 仍保留给通用枪械组件逻辑使用。

### 15.2 回合开始输入恢复

- `Map2Capability#tryClearPendingRoundKeyboardUnlock` 曾漏掉鼠标输入恢复。
- 现在当回合开始 10 秒锁定期结束，或玩家登录同步到期后自动清理时，除了前后左右和跳跃，还要同时移除：
  - `InputOperations.MOUSE_ATTACK`
  - `InputOperations.MOUSE_USE`
- 这样可以避免玩家在退出重进后残留“不能开火/不能右键”的状态。

### 15.3 语言文件拆分

- `src/main/resources/assets/xtymegamod/lang/en_us.json` 不再保留中文文案，已整体翻译为英文。
- 从原 `en_us.json` 复制出新的 `zh_cn.json`，用来保留简体中文文案。
- 这次语言文件调整后的稳定约定：
  - `zh_cn.json` 作为中文主文案来源。
  - `en_us.json` 保持纯英文，不混入中文条目。
  - 两个文件都已经验证为有效 UTF-8 JSON。

### 15.4 BlurRectRenderer 说明结论

- `com.mega.xty.client.renderer.BlurRectRenderer` 的核心职责可以固定理解为：
  - 先复制主 `framebuffer` 到临时 `TextureTarget`
  - 再让 shader 采样这份屏幕副本
  - 最后把模糊矩形绘回主画面
- 这套实现的关键目的，是避免一边读取主 `framebuffer` 一边写回主 `framebuffer` 的未定义行为。
- 以后若继续扩展模糊提示框渲染，优先沿用这套“先复制屏幕再局部采样”的结构。
