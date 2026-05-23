package com.mega.map.client.shader.post;

import com.mega.endinglib.api.client.shader.post.CustomScreenEffect;
import com.mega.endinglib.mixin.accessor.AccessorPostChain;

public abstract class TimePostEffect implements CustomScreenEffect {
    protected float lastStamp;
    protected float time;
    protected boolean canUse;
    @Override
    public void onRenderTick(float partialTicks) {
        if (partialTicks < this.lastStamp) {
            this.time += 1.0F - this.lastStamp;
            this.time += partialTicks;
        } else {
            this.time += partialTicks - this.lastStamp;
        }
        lastStamp = partialTicks;
        ((AccessorPostChain) this.current()).getPasses().forEach(postPass -> postPass.getEffect().safeGetUniform("TotalTime").set(time * 0.05F));
    }

    @Override
    public boolean canUse() {
        if (!this.canUse) {
            time = lastStamp = 0F;
        }
        return this.canUse;
    }
}
