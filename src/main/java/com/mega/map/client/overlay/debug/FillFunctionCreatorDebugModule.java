package com.mega.map.client.overlay.debug;

import com.mega.endinglib.api.client.Easing;
import com.mega.endinglib.api.client.cmc.ClientLoreHelper;
import com.mega.endinglib.api.item.component.ItemComponentManager;
import com.mega.endinglib.client.ClientWrapped;
import com.mega.endinglib.util.mc.client.ClientUtils;
import com.mega.endinglib.util.mc.client.MegaGuiGraphics;
import com.mega.map.MegaMod;
import com.mega.map.client.overlay.DebugModule;
import com.mega.map.client.overlay.DebugOverlays;
import com.mega.map.common.component.ComponentInit;
import com.mega.map.common.init.ItemInit;
import com.mega.map.common.item.FillFunctionCreatorItem;
import com.mega.map.common.component.FillCreatorComponent;
import com.mega.map.common.network.NetworkHandler;
import com.mega.map.common.network.c2s.debug.C2SDebugUnRedoPacket;
import com.mega.map.common.network.c2s.debug.fill_fucntion.C2SClearRecordsPacket;
import com.mega.map.common.network.c2s.debug.fill_fucntion.C2SFillMakeLinePacket;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.fml.loading.FMLLoader;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FillFunctionCreatorDebugModule implements DebugModule {
    private final ClickOverlayButton undo =
            new ClickOverlayButton(Component.translatable("ui.megamod.debug.undo"))
                    .setCanUse((stack, player) -> {
                        FillCreatorComponent component = ItemComponentManager.get(stack, ComponentInit.FILL_CREATOR);
                        if(component == null) return false;
                        return !component.history().isEmpty();
                    })
                    .setConsumer(player -> NetworkHandler.sendToServer(new C2SDebugUnRedoPacket(ItemInit.FILL_FUNCTION_CREATOR.get(), true)));
    private final ClickOverlayButton redo =
            new ClickOverlayButton(Component.translatable("ui.megamod.debug.redo"))
                    .setCanUse((stack, player) -> {
                        FillCreatorComponent component = ItemComponentManager.get(stack, ComponentInit.FILL_CREATOR);
                        if(component == null) return false;
                        return component.history().size() > component.records().size();
                    })
                    .setConsumer(player -> NetworkHandler.sendToServer(new C2SDebugUnRedoPacket(ItemInit.FILL_FUNCTION_CREATOR.get(), false)));
    private final ClickOverlayButton leftClick = new ClickOverlayButton(Component.translatable("ui.megamod.debug.selected_first"));
    private final ClickOverlayButton rightClick = new ClickOverlayButton(Component.translatable("ui.megamod.debug.selected_second"));
    private final ClickOverlayButton line =
            new ClickOverlayButton(Component.translatable("ui.megamod.debug.line"))
                    .setCanUse((stack, player) -> {
                        CompoundTag nbt = stack.getTag();
                        FillCreatorComponent component = ItemComponentManager.get(stack, ComponentInit.FILL_CREATOR);
                        if(component == null) return false;
                        if (nbt != null) {
                            CompoundTag interaction = nbt.getCompound(FillFunctionCreatorItem.INTERACTION);
                            if (!interaction.isEmpty()) {
                                BlockPos start = FillFunctionCreatorItem.fromArray(interaction.getIntArray("0"));
                                BlockPos end = FillFunctionCreatorItem.fromArray(interaction.getIntArray("1"));
                                return start != null && end != null && component.use().isPresent();
                            }

                        }
                        return false;
                    })
                    .setConsumer(player -> NetworkHandler.sendToServer(new C2SFillMakeLinePacket()));
    private final ClickOverlayButton save =
            new ClickOverlayButton(Component.translatable("ui.megamod.debug.save"))
                    .setCanUse((stack, player) -> {
                        FillCreatorComponent component = ItemComponentManager.get(stack, ComponentInit.FILL_CREATOR);
                        if(component == null) return false;
                        return !component.history().isEmpty();
                    })
                    .setConsumer(player -> {
                        NetworkHandler.sendToServer(new C2SClearRecordsPacket());
                        CompletableFuture.runAsync(() -> {
                            FillCreatorComponent component = ItemComponentManager.get(player.getItemInHand(InteractionHand.MAIN_HAND), ComponentInit.FILL_CREATOR);
                            if (component != null && component.use().isPresent()) {
                                String block = BuiltInRegistries.BLOCK.getKey(component.use().get()).toString();
                                Set<String> lines = component.records().stream()
                                        .map(single -> makeFunctionLine(single.start(), single.end(), block))
                                        .collect(Collectors.toSet());
                                try {
                                    Path path = FMLLoader.getGamePath().resolve(MegaMod.MODID).resolve("fill_functions");
                                    Files.createDirectories(path);
                                    StringBuilder result = new StringBuilder();
                                    for (String s : lines) {
                                        result.append(s).append("\n");
                                    }
                                    String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm_ss")) + ".mcfunction";
                                    Files.writeString(
                                            path.resolve(fileName),
                                            result.toString()
                                    );


                                    player.sendSystemMessage(Component.translatable("ui.megamod.debug.save.success",
                                            Component.literal(", "+ path.toAbsolutePath() + fileName)
                                                    .withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE)
                                                    .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, path.toAbsolutePath().toString())))
                                    ));
                                } catch (Throwable throwable) {
                                    throwable.printStackTrace();
                                }
                            }
                        }, ClientUtils.CLIENT_TEST_POOL);
                    });
    private static String makeFunctionLine(BlockPos start, BlockPos end, String block) {
        return "fill %d %d %d %d %d %d %s".formatted(
                start.getX(),
                start.getY(),
                start.getZ(),
                end.getX(),
                end.getY(),
                end.getZ(),
                block
        );
    }
    public FillFunctionCreatorDebugModule() {
        buttons.add(this.undo);
        buttons.add(this.redo);
        buttons.add(this.leftClick);
        buttons.add(this.rightClick);
        buttons.add(this.line);
        buttons.add(this.save);
    }

    private final List<ClickOverlayButton> buttons = new ObjectArrayList<>(4);
    @Override
    public void tick() {
        this.buttons.forEach(ClickOverlayButton::tick);
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        float ticks = DebugOverlays.tickCount + partialTick;
        GuiGraphics graphics = MegaGuiGraphics.of(guiGraphics);
        PoseStack stack = graphics.pose();
        stack.pushPose();
        stack.translate(4F, 32F, 0F);
        if (ticks < 100.0F) {
            RenderSystem.setShaderColor(1F, 1F,1F, ticks * 0.5F);
        }
        for (int index = 0;index < this.buttons.size();index++) {
            ClickOverlayButton b = this.buttons.get(index);
            stack.translate(0F, 8F + gui.getFont().lineHeight, 0F);
            float f = ticks - index;
            if (ticks < 100.0F) {
                float textW = gui.getFont().width(b.text);
                float xDelta = -index * 1.5F - textW;
                if (f > 0) {
                    xDelta += Mth.clamp(Easing.IN_OUT_CUBIC.interpolate(f, 0, index * 1.5F + textW), 0, index * 1.5F + textW);
                }
                stack.translate(xDelta, 0, 0);
                b.render(gui, graphics, partialTick, screenWidth, screenHeight);
                stack.translate(-xDelta, 0, 0);
            } else b.render(gui, graphics, partialTick, screenWidth, screenHeight);
        }
        if (ticks < 100.0F) {
            RenderSystem.setShaderColor(1F, 1F,1F, 1F);
        }
        stack.popPose();
    }

    @Override
    public void onKeyboard(InputEvent.Key event) {
        if (ClientLoreHelper.hasControlDown() && event.getAction() != InputConstants.RELEASE) {
            Player player = ClientWrapped.clientPlayer();
            if (event.getKey() == GLFW.GLFW_KEY_Z) {
                this.undo.click(player);
            } else if (event.getKey() == GLFW.GLFW_KEY_Y) {
                this.redo.click(player);
            } else if (event.getKey() == GLFW.GLFW_KEY_S) {
                this.save.click(player);
            }
        }
    }

    @Override
    public void onMouseButton(InputEvent.MouseButton.Post event) {
        if (event.getAction() == InputConstants.PRESS) {
            Player player = ClientWrapped.clientPlayer();
            if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT)
                this.leftClick.click(player);
            else if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                this.rightClick.click(player);
            } else if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                if (ClientLoreHelper.hasControlDown()) {
                    this.line.click(player);
                }
            }
        }
    }

    @Override
    public void undo(ServerPlayer player, ItemStack stack) {
        ItemComponentManager manager = ItemComponentManager.get(stack);
        FillCreatorComponent component = manager.get(ComponentInit.FILL_CREATOR);
        component.undo(stack, player);
    }

    @Override
    public void redo(ServerPlayer player, ItemStack stack) {
        ItemComponentManager manager = ItemComponentManager.get(stack);
        FillCreatorComponent component = manager.get(ComponentInit.FILL_CREATOR);
        component.redo(stack, player);
    }
}
