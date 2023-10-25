package me.bruno.removerodblocking.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.Arrays;

@Mixin(WorldRenderer.class)
public class RemoveBobber {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Inject(at = @At("HEAD"), method = "renderEntity", cancellable = true)
    private void removeBobber(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (entity instanceof FishingBobberEntity bobber) {
            // allow versions 1.15.x to work also
            Entity hooked;
            if (Arrays.stream(FishingBobberEntity.class.getMethods()).anyMatch(method -> method.getName().equals("getHookedEntity"))) {
                hooked = bobber.getHookedEntity();
            } else {
                try {
                    Field f = bobber.getClass().getField("hookedEntity");
                    f.setAccessible(true);
                    hooked = (Entity) f.get(bobber);
                } catch (Exception e) {
                    LOGGER.error("Something happened!", e);
                    return;
                }
            }
            if (hooked instanceof ClientPlayerEntity player) {
                if (MinecraftClient.getInstance().player == null) {
                    return;
                }
                if (MinecraftClient.getInstance().player.getUuid().equals(player.getUuid())) {
                    ci.cancel();
                }
            }
        }

    }
}
