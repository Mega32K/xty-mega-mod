package com.mega.xty.common.network;

import com.mega.xty.common.network.c2s.debug.C2SDebugUnRedoPacket;
import com.mega.xty.common.network.c2s.debug.fill_fucntion.C2SClearRecordsPacket;
import com.mega.xty.common.network.c2s.debug.fill_fucntion.C2SFillMakeLinePacket;
import com.mega.xty.common.network.c2s.map1.game2.C2SPlayerJumpPacket;
import com.mega.xty.common.network.c2s.map1.game2.C2SPlayerLaydownPacket;
import com.mega.xty.common.network.c2s.map1.game2.C2SPlayerSwingHandNoticePacket;
import com.mega.xty.common.network.c2s.map2.C2SSetNamePacket;
import com.mega.xty.common.network.c2s.map2.C2SStopJoiningGamePacket;
import com.mega.xty.common.network.s2c.S2CDisableBIPacket;
import com.mega.xty.common.network.s2c.S2CPartialTeleportPacket;
import com.mega.xty.common.network.s2c.fps.S2CBombDataPacket;
import com.mega.xty.common.network.s2c.fps.S2CPlayerKADPacket;
import com.mega.xty.common.network.s2c.fps.S2CPlayerNamePacket;
import com.mega.xty.common.network.s2c.fps.S2CRoundLoseRenderPacket;
import com.mega.xty.common.network.s2c.fps.S2CRoundStartRenderPacket;
import com.mega.xty.common.network.s2c.fps.S2CRoundWinRenderPacket;
import com.mega.xty.common.network.s2c.fps.S2CUsingKADPacket;
import com.mega.xty.common.network.s2c.map1.game2.S2CGame2HitEffectPacket;
import com.mega.xty.common.network.s2c.map1.game2.S2CGame2StatsPacket;
import com.mega.xty.common.network.s2c.map1.game2.S2CScreenShakePacket;
import com.mega.xty.common.network.s2c.map1.game2.S2CSimpleScreenShakePacket;
import com.mega.xty.common.network.s2c.map2.*;
import com.mega.xty.common.network.s2c.map2.game2.S2CGame2DeathEffectPacket;
import com.mega.xty.common.network.s2c.map2.game2.S2CGame2StartEffectPacket;
import com.mega.xty.common.network.s2c.map2.game1.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.concurrent.atomic.AtomicInteger;

public class NetworkHandler {
    public static SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(ResourceLocation.fromNamespaceAndPath("xtymegamod", "xtymegamod_packet"), () -> "1", (s) -> true, (s) -> true);
    private static final AtomicInteger id = new AtomicInteger(0);
    private static int id() {
        return id.getAndIncrement();
    }
    public static void registerPackets() {
        INSTANCE.registerMessage(id(), C2SDebugUnRedoPacket.class, C2SDebugUnRedoPacket::encode, C2SDebugUnRedoPacket::decode, C2SDebugUnRedoPacket::handle);
        INSTANCE.registerMessage(id(), C2SFillMakeLinePacket.class, C2SFillMakeLinePacket::encode, C2SFillMakeLinePacket::decode, C2SFillMakeLinePacket::handle);
        INSTANCE.registerMessage(id(), C2SClearRecordsPacket.class, C2SClearRecordsPacket::encode, C2SClearRecordsPacket::decode, C2SClearRecordsPacket::handle);
        INSTANCE.registerMessage(id(), S2CPartialTeleportPacket.class, S2CPartialTeleportPacket::encode, S2CPartialTeleportPacket::decode, S2CPartialTeleportPacket::handle);
        INSTANCE.registerMessage(id(), S2CScreenShakePacket.class, S2CScreenShakePacket::encode, S2CScreenShakePacket::decode, S2CScreenShakePacket::handle);
        INSTANCE.registerMessage(id(), S2CSimpleScreenShakePacket.class, S2CSimpleScreenShakePacket::encode, S2CSimpleScreenShakePacket::decode, S2CSimpleScreenShakePacket::handle);
        INSTANCE.registerMessage(id(), S2CGame2StatsPacket.class, S2CGame2StatsPacket::encode, S2CGame2StatsPacket::decode, S2CGame2StatsPacket::handle);
        INSTANCE.registerMessage(id(), C2SPlayerJumpPacket.class, C2SPlayerJumpPacket::encode, C2SPlayerJumpPacket::decode, C2SPlayerJumpPacket::handle);
        INSTANCE.registerMessage(id(), C2SPlayerSwingHandNoticePacket.class, C2SPlayerSwingHandNoticePacket::encode, C2SPlayerSwingHandNoticePacket::decode, C2SPlayerSwingHandNoticePacket::handle);
        INSTANCE.registerMessage(id(), C2SPlayerLaydownPacket.class, C2SPlayerLaydownPacket::encode, C2SPlayerLaydownPacket::decode, C2SPlayerLaydownPacket::handle);
        INSTANCE.registerMessage(id(), S2CGame2HitEffectPacket.class, S2CGame2HitEffectPacket::encode, S2CGame2HitEffectPacket::decode, S2CGame2HitEffectPacket::handle);
        INSTANCE.registerMessage(id(), S2CDisableBIPacket.class, S2CDisableBIPacket::encode, S2CDisableBIPacket::decode, S2CDisableBIPacket::handle);
        INSTANCE.registerMessage(id(), S2CGame1StatsPacket.class, S2CGame1StatsPacket::encode, S2CGame1StatsPacket::decode, S2CGame1StatsPacket::handle);
        INSTANCE.registerMessage(id(), S2CGame1EvolutionWeaponPacket.class, S2CGame1EvolutionWeaponPacket::encode, S2CGame1EvolutionWeaponPacket::decode, S2CGame1EvolutionWeaponPacket::handle);
        INSTANCE.registerMessage(id(), S2CGame1EvolutionSelectorPacket.class, S2CGame1EvolutionSelectorPacket::encode, S2CGame1EvolutionSelectorPacket::decode, S2CGame1EvolutionSelectorPacket::handle);
        INSTANCE.registerMessage(id(), S2CMap2ModePacket.class, S2CMap2ModePacket::encode, S2CMap2ModePacket::decode, S2CMap2ModePacket::handle);
        INSTANCE.registerMessage(id(), S2CMap2CountdownPacket.class, S2CMap2CountdownPacket::encode, S2CMap2CountdownPacket::decode, S2CMap2CountdownPacket::handle);
        INSTANCE.registerMessage(id(), S2CAddDeathDataPacket.class, S2CAddDeathDataPacket::encode, S2CAddDeathDataPacket::decode, S2CAddDeathDataPacket::handle);
        INSTANCE.registerMessage(id(), S2CSyncTeamScorePacket.class, S2CSyncTeamScorePacket::encode, S2CSyncTeamScorePacket::decode, S2CSyncTeamScorePacket::handle);
        INSTANCE.registerMessage(id(), S2CMap2TeamScoreVisiblePacket.class, S2CMap2TeamScoreVisiblePacket::encode, S2CMap2TeamScoreVisiblePacket::decode, S2CMap2TeamScoreVisiblePacket::handle);
        INSTANCE.registerMessage(id(), S2CPlayerCountNeedPacket.class, S2CPlayerCountNeedPacket::encode, S2CPlayerCountNeedPacket::decode, S2CPlayerCountNeedPacket::handle);
        INSTANCE.registerMessage(id(), S2CPlayerRenamePacket.class, S2CPlayerRenamePacket::encode, S2CPlayerRenamePacket::decode, S2CPlayerRenamePacket::handle);
        INSTANCE.registerMessage(id(), C2SStopJoiningGamePacket.class, C2SStopJoiningGamePacket::encode, C2SStopJoiningGamePacket::decode, C2SStopJoiningGamePacket::handle);
        INSTANCE.registerMessage(id(), C2SSetNamePacket.class, C2SSetNamePacket::encode, C2SSetNamePacket::decode, C2SSetNamePacket::handle);
        INSTANCE.registerMessage(id(), S2CMap2StatsPacket.class, S2CMap2StatsPacket::encode, S2CMap2StatsPacket::decode, S2CMap2StatsPacket::handle);
        INSTANCE.registerMessage(id(), S2CMap2ScoreOverlayVisiblePacket.class, S2CMap2ScoreOverlayVisiblePacket::encode, S2CMap2ScoreOverlayVisiblePacket::decode, S2CMap2ScoreOverlayVisiblePacket::handle);
        INSTANCE.registerMessage(id(), com.mega.xty.common.network.s2c.map2.game2.S2CGame2StatsPacket.class, com.mega.xty.common.network.s2c.map2.game2.S2CGame2StatsPacket::encode, com.mega.xty.common.network.s2c.map2.game2.S2CGame2StatsPacket::decode, com.mega.xty.common.network.s2c.map2.game2.S2CGame2StatsPacket::handle);
        INSTANCE.registerMessage(id(), S2CSyncPointsPacket.class, S2CSyncPointsPacket::encode, S2CSyncPointsPacket::decode, S2CSyncPointsPacket::handle);
        INSTANCE.registerMessage(id(), S2CPlayerKADPacket.class, S2CPlayerKADPacket::encode, S2CPlayerKADPacket::decode, S2CPlayerKADPacket::handle);
        INSTANCE.registerMessage(id(), S2CUsingKADPacket.class, S2CUsingKADPacket::encode, S2CUsingKADPacket::decode, S2CUsingKADPacket::handle);
        INSTANCE.registerMessage(id(), S2CPlayerNamePacket.class, S2CPlayerNamePacket::encode, S2CPlayerNamePacket::decode, S2CPlayerNamePacket::handle);
        INSTANCE.registerMessage(id(), S2CMap2TextTipPacket.class, S2CMap2TextTipPacket::encode, S2CMap2TextTipPacket::decode, S2CMap2TextTipPacket::handle);
        INSTANCE.registerMessage(id(), S2CSyncTeamWinsPacket.class, S2CSyncTeamWinsPacket::encode, S2CSyncTeamWinsPacket::decode, S2CSyncTeamWinsPacket::handle);
        INSTANCE.registerMessage(id(), S2CBombDataPacket.class, S2CBombDataPacket::encode, S2CBombDataPacket::decode, S2CBombDataPacket::handle);
        INSTANCE.registerMessage(id(), S2CRoundLoseRenderPacket.class, S2CRoundLoseRenderPacket::encode, S2CRoundLoseRenderPacket::decode, S2CRoundLoseRenderPacket::handle);
        INSTANCE.registerMessage(id(), S2CRoundWinRenderPacket.class, S2CRoundWinRenderPacket::encode, S2CRoundWinRenderPacket::decode, S2CRoundWinRenderPacket::handle);
        INSTANCE.registerMessage(id(), S2CRoundStartRenderPacket.class, S2CRoundStartRenderPacket::encode, S2CRoundStartRenderPacket::decode, S2CRoundStartRenderPacket::handle);
        INSTANCE.registerMessage(id(), S2CGame2DeathEffectPacket.class, S2CGame2DeathEffectPacket::encode, S2CGame2DeathEffectPacket::decode, S2CGame2DeathEffectPacket::handle);
        INSTANCE.registerMessage(id(), S2CGame2StartEffectPacket.class, S2CGame2StartEffectPacket::encode, S2CGame2StartEffectPacket::decode, S2CGame2StartEffectPacket::handle);
    }
    public static <MSG> void sendToAll(MSG msg) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), msg);
    }

    public static <MSG> void sendToServer(MSG msg) {
        INSTANCE.sendToServer(msg);
    }

    public static <MSG> void sendToPlayer(MSG msg, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    public static <MSG> void sendToEntity(MSG message, LivingEntity entity) {
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), message);
    }
}
