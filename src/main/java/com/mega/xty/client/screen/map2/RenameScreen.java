package com.mega.xty.client.screen.map2;

import com.mega.endinglib.util.mc.client.MegaGuiGraphics;
import com.mega.xty.client.overlay.map2.HealthOverlay;
import com.mega.xty.client.shader.ModShaders;
import com.mega.xty.common.network.NetworkHandler;
import com.mega.xty.common.network.c2s.map2.C2SSetNamePacket;
import com.mega.xty.common.network.c2s.map2.C2SStopJoiningGamePacket;
import com.mega.xty.proxy.CommonProxy;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.brigadier.StringReader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

public class RenameScreen extends Screen {
    Minecraft mc = Minecraft.getInstance();
    public int tickCount;
    public final AbstractClientPlayer targetPlayer;
    private EditBox input;
    private boolean isNameNotAllowed;
    private String editedName = "点击输入名称";
    public RenameScreen(AbstractClientPlayer targetPlayer) {
        super(Component.empty());
        this.targetPlayer = targetPlayer;
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (minecraft == null) return;
        MegaGuiGraphics guiGraphics = MegaGuiGraphics.of(graphics);
        float backgroundAlpha = (this.tickCount + partialTicks) / 30F;
        int guiWidth = guiGraphics.guiWidth();
        int guiHeight = guiGraphics.guiHeight();
        graphics.fill(-1, -1, guiWidth + 1, guiHeight + 1, 0xFF1b2238);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.setColor(1F, 1F, 1F, Math.min(1.0F, backgroundAlpha));
        RenderSystem.setShader(ModShaders::getMap2Start);
        ModShaders.getMap2Start().safeGetUniform("Mouse").set(new float[] {(float) mouseX / guiWidth, (float) mouseY / guiHeight});
        Matrix4f matrix4f = new Matrix4f().identity();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix4f, 0, 0, 0F).uv(0, 0).endVertex();
        bufferbuilder.vertex(matrix4f, 0, guiHeight, 0F).uv(0, 1).endVertex();
        bufferbuilder.vertex(matrix4f, guiWidth, guiHeight, 0F).uv(1, 1).endVertex();
        bufferbuilder.vertex(matrix4f, guiWidth, 0, 0F).uv(1, 0).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        super.render(graphics, mouseX, mouseY, partialTicks);
        //渲染大头
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(guiWidth / 2F, guiHeight * 0.425F - 48F, 0F);
        HealthOverlay.renderProfileIcon(targetPlayer, guiGraphics, poseStack, 32F);
        poseStack.popPose();
        //渲染提示
        if (isNameNotAllowed) {
            guiGraphics.drawCenteredString(font, "名字中存在不合法字符",
                    this.input.getX() + this.input.getWidth() / 2,
                    this.input.getY() + this.input.getHeight() + 4,
                    0xFFFF0000);
        } else {
            guiGraphics.drawCenteredString(font, editedName.isEmpty() ? "应用玩家初始名\ue010" : "点击确认名字\ue010",
                    this.input.getX() + this.input.getWidth() / 2,
                    this.input.getY() + this.input.getHeight() + 4,
                    0xFF00FF00);
        }
        guiGraphics.setColor(1F, 1F, 1F, 1F);
        RenderSystem.disableBlend();

        poseStack.pushPose();
        float scale = 0.75F;
        float lineHeight = font.lineHeight * scale;
        poseStack.translate(guiWidth / 2F, guiHeight - 3 - lineHeight, 0);
        poseStack.scale(scale, scale, scale);
        guiGraphics.drawCenteredString(font, "Shader created by JeremyBankes", 0, 0, 0xFFa1a1a1);
        poseStack.popPose();
        guiGraphics.setColor(1F, 1F, 1F, 1F);
    }

    @Override
    protected void init() {
        this.input = new EditBox(this.mc.fontFilterFishy, mc.getWindow().getGuiScaledWidth() / 2 - 32, (int) (mc.getWindow().getGuiScaledHeight() * 0.5F), 64, 12, Component.literal("输入玩家名称")) {
        };
        this.input.setMaxLength(256);
        this.input.setBordered(true);
        this.input.setValue(editedName);
        this.input.setResponder(this::onEdited);
        this.addRenderableWidget(input);
    }
    public boolean inBounds(double mouseX, double mouseY) {
        int x = this.input.getX();
        int y = this.input.getY() + this.input.getHeight();
        int endX = x + this.input.getWidth();
        int endY = y + this.input.getHeight();
        return mouseX >= x && mouseX < endX && mouseY >= y && mouseY < endY;

    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
            if (inBounds(x, y)) {
                sure();
            }
        }
        return super.mouseClicked(x, y, button);
    }

    private void sure() {
        NetworkHandler.sendToServer(new C2SStopJoiningGamePacket());
        NetworkHandler.sendToServer(new C2SSetNamePacket(editedName, targetPlayer.getUUID()));
        if (mc.player != null)
            CommonProxy.getMap2Cap(mc.player).ifPresent(cap -> {
                cap.setNeedStart(false);
            });
        this.mc.popGuiLayer();
    }
    private void onEdited(String text) {
        this.editedName = text;
        StringReader reader = new StringReader(text);
        isNameNotAllowed = false;
        while (reader.canRead()) {
            if (isNotAllowed(reader.peek()))  {
                isNameNotAllowed = true;
                break;
            } else reader.skip();
        }
        if (StringUtils.isBlank(text) && !text.isEmpty()) isNameNotAllowed = true;
    }

    @Override
    public void tick() {
        tickCount++;
        super.tick();
    }
    public static boolean isNotAllowed(char c) {
        return c == '%' || c == '*' || c == '/' || c == '\\';
    }
}
