package me.bruno.removerodblocking.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class RemoveBobber {

    @Inject(at = @At("HEAD"), method = "renderEntity", cancellable = true)
    private static void removeBobber(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (entity instanceof FishingBobberEntity) {
            Entity hooked = ((FishingBobberEntity) entity).getHookedEntity();
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
