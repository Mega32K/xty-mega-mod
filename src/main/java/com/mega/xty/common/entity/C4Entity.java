package com.mega.xty.common.entity;

import com.mega.xty.common.init.EntityInit;
import com.mega.xty.common.init.ItemInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class C4Entity extends Entity {
    @Nullable
    public Vec3 lockedPos = null;
    public final LazyOptional<ItemStack> C4 = LazyOptional.of(ItemInit.C4_BOMB.get()::getDefaultInstance);
    public C4Entity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }
    public C4Entity(Player player) {
        this(EntityInit.C4.get(), player.level());
        Vec3 pos = player.position();
        this.setPos(pos);
        this.setYRot(player.getYRot());
        this.lockedPos = pos;
    }
    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {

    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {

    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
    public ItemStack getC4() {
        return C4.orElse(ItemInit.C4_BOMB.get().getDefaultInstance());
    }

    @Override
    public void tick() {
        if (this.lockedPos != null) {
            this.setPos(this.lockedPos.x, this.getY(), this.lockedPos.z);
        }
        this.move(MoverType.SELF, new Vec3(0D, -8D, 0D));
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isNoGravity() {
        return false;
    }
}
