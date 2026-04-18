package com.mega.xty.common.init;

import com.mega.xty.XtyMegaMod;
import com.mega.xty.common.block.BountyBoardBlock;
import com.mega.xty.common.block.GoldSpawnpointBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockInit {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, XtyMegaMod.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, XtyMegaMod.MODID);
    public static final RegistryObject<Block> GOLD_SPAWNPOINT = BLOCKS.register(
            "gold_spawnpoint_block",
            ()-> new GoldSpawnpointBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GOLD)
                    .instrument(NoteBlockInstrument.BELL)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.METAL)
            )
    );
    public static final RegistryObject<Block> BOUNTY_BOARD = BLOCKS.register(
            "bounty_board",
            ()-> new BountyBoardBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PODZOL)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2)
                    .sound(SoundType.WOOD)
                    .ignitedByLava()
            )
    );
    /*public static RegistryObject<Block> CHUNK_NO_CULLING_BLOCK;
    BLOCKS.register(
            "chunk_no_culling_block",
            ()-> new ChunkNoCullingBlock(BlockBehaviour.Properties.of()
                    .strength(-1.0F, 3600000.8F)
                    .noLootTable()
                    .noOcclusion()
                    .isValidSpawn((a,b,c,d)-> false)
                    .noParticlesOnBreak()
                    .pushReaction(PushReaction.BLOCK))
    );

    public static RegistryObject<BlockEntityType<ChunkNoCullingBlockEntity>> CHUNK_NO_CULLING_BE;
    = BLOCK_ENTITIES.register("rune_reactor", () ->
            BlockEntityType.Builder.of(
                    ChunkNoCullingBlockEntity::new,
                    CHUNK_NO_CULLING_BLOCK.get()
            ).build(null)
    );
    */
}
