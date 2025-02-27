package me.bruno.removerodblocking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.FishingBobberEntityState;
import net.minecraft.entity.player.PlayerEntity;

@Environment(EnvType.CLIENT)
public class FishingBobberEntityStateWithPlayer extends FishingBobberEntityState {
    public PlayerEntity hooked;

    public FishingBobberEntityStateWithPlayer() {
        this.hooked = null;
    }
}
