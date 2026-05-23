package com.mega.map.common.init;

import com.mega.map.MegaMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundsInit {
    private static final float C4_BEEP_SOUND_RANGE = 32.0F;
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MegaMod.MODID);

    public static final RegistryObject<SoundEvent> C4_BEEP2 = registerFixedRange("fps.c4.c4_beep2", C4_BEEP_SOUND_RANGE);
    public static final RegistryObject<SoundEvent> C4_BEEP2_10SEC = registerFixedRange("fps.c4.c4_beep2_10sec", C4_BEEP_SOUND_RANGE);
    public static final RegistryObject<SoundEvent> C4_BEEP3 = registerFixedRange("fps.c4.c4_beep3", C4_BEEP_SOUND_RANGE);
    public static final RegistryObject<SoundEvent> C4_BEEP3_10SEC = registerFixedRange("fps.c4.c4_beep3_10sec", C4_BEEP_SOUND_RANGE);
    public static final RegistryObject<SoundEvent> C4_CLICK = register("fps.c4.c4_click");
    public static final RegistryObject<SoundEvent> C4_DISARMFINISH = register("fps.c4.c4_disarmfinish");
    public static final RegistryObject<SoundEvent> C4_DISARMSTART = register("fps.c4.c4_disarmstart");
    public static final RegistryObject<SoundEvent> C4_DRAW = register("fps.c4.c4_draw");
    public static final RegistryObject<SoundEvent> C4_DRAW_01 = register("fps.c4.c4_draw_01");
    public static final RegistryObject<SoundEvent> C4_DRAW_02 = register("fps.c4.c4_draw_02");
    public static final RegistryObject<SoundEvent> C4_DRAW_03 = register("fps.c4.c4_draw_03");
    public static final RegistryObject<SoundEvent> C4_DRAW_04 = register("fps.c4.c4_draw_04");
    public static final RegistryObject<SoundEvent> C4_DRAW_05 = register("fps.c4.c4_draw_05");
    public static final RegistryObject<SoundEvent> C4_DRAW_06 = register("fps.c4.c4_draw_06");
    public static final RegistryObject<SoundEvent> C4_DRAW_07 = register("fps.c4.c4_draw_07");
    public static final RegistryObject<SoundEvent> C4_EXP_DEB1 = register("fps.c4.c4_exp_deb1");
    public static final RegistryObject<SoundEvent> C4_EXP_DEB2 = register("fps.c4.c4_exp_deb2");
    public static final RegistryObject<SoundEvent> C4_EXPLODE1 = register("fps.c4.c4_explode1");
    public static final RegistryObject<SoundEvent> C4_INITIATE = register("fps.c4.c4_initiate");
    public static final RegistryObject<SoundEvent> C4_PLANT = register("fps.c4.c4_plant");
    public static final RegistryObject<SoundEvent> C4_PLANT_QUIET = register("fps.c4.c4_plant_quiet");
    public static final RegistryObject<SoundEvent> KEY_PRESS1 = register("fps.c4.key_press1");
    public static final RegistryObject<SoundEvent> KEY_PRESS2 = register("fps.c4.key_press2");
    public static final RegistryObject<SoundEvent> KEY_PRESS3 = register("fps.c4.key_press3");
    public static final RegistryObject<SoundEvent> KEY_PRESS4 = register("fps.c4.key_press4");
    public static final RegistryObject<SoundEvent> KEY_PRESS5 = register("fps.c4.key_press5");
    public static final RegistryObject<SoundEvent> KEY_PRESS6 = register("fps.c4.key_press6");
    public static final RegistryObject<SoundEvent> KEY_PRESS7 = register("fps.c4.key_press7");
    public static final RegistryObject<SoundEvent> CTWIN = register("fps.ctwin");
    public static final RegistryObject<SoundEvent> TERWIN = register("fps.terwin");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MegaMod.MODID, name)));
    }

    private static RegistryObject<SoundEvent> registerFixedRange(String name, float range) {
        return SOUNDS.register(name, () -> SoundEvent.createFixedRangeEvent(ResourceLocation.fromNamespaceAndPath(MegaMod.MODID, name), range));
    }
}
