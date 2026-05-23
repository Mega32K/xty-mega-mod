package com.mega.map.game2;

import com.mega.endinglib.api.client.Easing;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

public class ShakeInstance {
    public float power;
    public int duration;
    public int time;
    public int circles;
    public boolean removed;
    private boolean reverse = Math.random() > 0.5D;
    private final Vec2 arrow;

    public ShakeInstance(float power, int duration, int time, Vec2 arrow, int circles) {
        this.power = power;
        this.duration = duration;
        this.time = time;
        this.arrow = arrow.normalized();
        this.circles = circles;
    }

    public void tick() {
        if (removed) return;
        time++;
        if (time > duration) {
            removed = true;
        }
    }
    public float value(float partialTicks, float offset) {
        if (removed) return 0;
        float progress = Mth.clamp((time + partialTicks) / (float) duration, 0.0F ,1.0F);
        float f = progress * Mth.PI * circles;
        float t = reverse ? Mth.cos(f) + offset : Mth.sin(f) + offset;
        t = (t + 1.0F) / -2F;
        return (1.0F-Easing.IN_OUT_SINE.calculate(progress)) * t;
    }
    public float getX(float partialTicks) {
        float value = (value(partialTicks, arrow.x)) * power;
        return arrow.x * value;
    }
    public float getY(float partialTicks) {
        float value = (value(partialTicks, arrow.y)) * power;
        return arrow.y * value;
    }
}
