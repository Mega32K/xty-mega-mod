package com.mega.xty.common.data.map2;

import com.mega.endinglib.api.data.CompoundTagUtils;
import com.mega.endinglib.common.data.EndingLibrarySavedData;
import com.mega.endinglib.common.data.InputOperations;
import com.mega.endinglib.util.mixin.level.ServerEC;
import com.mega.xty.common.data.fps.FpsSavedData;
import com.mega.xty.common.data.fps.kad.KAD;
import com.mega.xty.common.network.NetworkHandler;
import com.mega.xty.common.network.s2c.map2.game2.S2CGame2StatsPacket;
import com.mega.xty.proxy.CommonProxy;
import com.mega.xty.util.data_expand.SavedDataGetter;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.scores.Team;
import org.jetbrains.annotations.NotNull;

public class Game2SavedData extends SavedData {
    public MinecraftServer server;
    private boolean isStopped = true;
    private Game2Functions game2Functions = new Game2Functions(this);
    public static Game2SavedData readOrCreate(MinecraftServer server) {
        Game2SavedData data = server.overworld().getDataStorage().computeIfAbsent(tag-> load(tag,server), Game2SavedData::new, "xty_map2_game_2");
        data.server = server;
        return data;
    }
    public static Game2SavedData getInstance(MinecraftServer server) {
        return ((SavedDataGetter) ((ServerEC) server).endinglib$serverECData()).getMap2game2SavedData();
    }
    public static Game2SavedData load(CompoundTag tag, MinecraftServer server) {
        Game2SavedData data = new Game2SavedData();
        if (CompoundTagUtils.containsBoolean(tag, "Stopped"))
            data.isStopped = tag.getBoolean("Stopped");
        if (CompoundTagUtils.containsCompound(tag, "Game2Functions"))
            data.game2Functions = Game2Functions.load(tag.getCompound("Game2Functions"), server, data);
        return data;
    }
    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        tag.putBoolean("Stopped", this.isStopped);
        CompoundTag functions = new CompoundTag();
        if (this.game2Functions != null) this.game2Functions.save(functions);
        tag.put("Game2Functions", functions);
        return tag;
    }

    public void setStopped(boolean stopped) {
        if (this.isStopped != stopped) {
            this.setDirty();
            for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers())
                NetworkHandler.sendToPlayer(new S2CGame2StatsPacket(stopped), serverPlayer);
        }
        if (stopped) {
            FpsSavedData fpsSavedData = FpsSavedData.getInstance(server);
            EndingLibrarySavedData elData = EndingLibrarySavedData.getInstance(this.server);
            for (ServerPlayer serverPlayer : this.server.getPlayerList().getPlayers()) {
                CommonProxy.getMap2Cap(serverPlayer).ifPresent(cap -> cap.setXaeroDead(false));
                clearRoundControlInputs(elData, serverPlayer);
                resetPlayerToDefaultMaxHealth(serverPlayer);
                clearRoundArmor(serverPlayer);
                fpsSavedData.getOrPutKAD(serverPlayer).setKAD(KAD.KAD_CURRENT, KAD.deserialize(0));
                fpsSavedData.getOrPutKAD(serverPlayer).setKAD(KAD.KAD_GENERAL, KAD.deserialize(0));
            }
        }
        isStopped = stopped;
    }
    public boolean isStopped() {
        return isStopped;
    }
    public Game2Functions getGame2Functions() {
        return game2Functions;
    }

    private void resetPlayerToDefaultMaxHealth(ServerPlayer player) {
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0D);
        if (player.getHealth() > 20.0F) {
            player.setHealth(20.0F);
        }
        CommonProxy.getXtyCap(player).ifPresent(cap -> {
            cap.setGame2MaxHealth(20.0F);
            cap.setGame2Health(player.getHealth());
        });
    }

    private void clearRoundControlInputs(EndingLibrarySavedData data, ServerPlayer player) {
        data.removeDisabledPermission(player, InputOperations.MOVE_FORWARD);
        data.removeDisabledPermission(player, InputOperations.MOVE_BACKWARD);
        data.removeDisabledPermission(player, InputOperations.MOVE_LEFT);
        data.removeDisabledPermission(player, InputOperations.MOVE_RIGHT);
        data.removeDisabledPermission(player, InputOperations.JUMP);
        data.removeDisabledPermission(player, InputOperations.MOUSE_ATTACK);
    }

    private void clearRoundArmor(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        Team team = player.getTeam();
        if (team == null) {
            return;
        }
        if (team.getColor() == ChatFormatting.RED || team.getColor() == ChatFormatting.BLUE) {
            player.getInventory().armor.replaceAll(itemStack -> ItemStack.EMPTY);
        }
    }
}
