package me.bruno.removerodblocking.mixin;

import me.bruno.removerodblocking.FishingBobberEntityStateWithPlayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.FishingBobberEntityRenderer;
import net.minecraft.client.render.entity.state.FishingBobberEntityState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.util.Colors;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingBobberEntityRenderer.class)
public abstract class FishingBobberEntityRendererMixin extends EntityRenderer<FishingBobberEntity, FishingBobberEntityState> {


    @Shadow protected abstract Vec3d getHandPos(PlayerEntity player, float f, float tickDelta);

    @Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/client/render/entity/state/FishingBobberEntityState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V", cancellable = true)
    public void renderExceptInHead(FishingBobberEntityState fishingBobberEntityState, MatrixStack matrixStack, OrderedRenderCommandQueue orderedRenderCommandQueue, CameraRenderState cameraRenderState, CallbackInfo ci){
        if (fishingBobberEntityState instanceof FishingBobberEntityStateWithPlayer withPlayer) {
            Entity hooked = withPlayer.hooked;

            if (hooked == null) {
                return;
            }
            if (!(hooked instanceof ClientPlayerEntity)) {
                return;
            }
            if (MinecraftClient.getInstance().player == null) {
                return;
            }
            if (!MinecraftClient.getInstance().player.getUuid().equals(hooked.getUuid())) {
                return;
            }

            //render fishing line
            matrixStack.push();
            float f = (float)fishingBobberEntityState.pos.x;
            float g = (float)fishingBobberEntityState.pos.y;
            float h = (float)fishingBobberEntityState.pos.z;
            orderedRenderCommandQueue.submitCustom(matrixStack, RenderLayer.getLines(), (entry, vertexConsumer) -> {
                for(int j = 0; j < 16; ++j) {
                    float k = percentage(j, 16);
                    float l = percentage(j + 1, 16);
                    renderFishingLine(f, g, h, vertexConsumer, entry, k, l);
                    renderFishingLine(f, g, h, vertexConsumer, entry, l, k);
                }
            });
            matrixStack.pop();
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "updateRenderState(Lnet/minecraft/entity/projectile/FishingBobberEntity;Lnet/minecraft/client/render/entity/state/FishingBobberEntityState;F)V")
    public void updateRenderStateWithPlayer(FishingBobberEntity fishingBobberEntity, FishingBobberEntityState fishingBobberEntityState, float f, CallbackInfo ci) {
        super.updateRenderState(fishingBobberEntity, fishingBobberEntityState, f);
        PlayerEntity playerEntity = fishingBobberEntity.getPlayerOwner();
        if (playerEntity == null) {
            fishingBobberEntityState.pos = Vec3d.ZERO;
        } else {
            float g = playerEntity.getHandSwingProgress(f);
            float h = MathHelper.sin(MathHelper.sqrt(g) * (float) Math.PI);
            Vec3d vec3d = getHandPos(playerEntity, h, f);
            Vec3d vec3d2 = fishingBobberEntity.getLerpedPos(f).add(0.0, 0.25, 0.0);
            fishingBobberEntityState.pos = vec3d.subtract(vec3d2);
        }
        if (fishingBobberEntityState instanceof FishingBobberEntityStateWithPlayer withPlayer) {
            if (fishingBobberEntity.getHookedEntity() instanceof ClientPlayerEntity player) {
                withPlayer.hooked = player;
            } else {
                withPlayer.hooked = null;
            }
        }
    }
    protected FishingBobberEntityRendererMixin(EntityRendererFactory.Context context) {
        super(context);
    }
    @Inject(at = @At("HEAD"), method = "createRenderState()Lnet/minecraft/client/render/entity/state/FishingBobberEntityState;", cancellable = true)
    public void createNewRenderState(CallbackInfoReturnable<FishingBobberEntityState> cir) {
        cir.setReturnValue(new FishingBobberEntityStateWithPlayer());
    }

    private static float percentage(int value, int max) {
        return (float)value / max;
    }

    private static void renderFishingLine(float x, float y, float z, VertexConsumer buffer, MatrixStack.Entry matrices, float segmentStart, float segmentEnd) {
        float f = x * segmentStart;
        float g = y * (segmentStart * segmentStart + segmentStart) * 0.5F + 0.25F;
        float h = z * segmentStart;
        float i = x * segmentEnd - f;
        float j = y * (segmentEnd * segmentEnd + segmentEnd) * 0.5F + 0.25F - g;
        float k = z * segmentEnd - h;
        float l = MathHelper.sqrt(i * i + j * j + k * k);
        i /= l;
        j /= l;
        k /= l;
        buffer.vertex(matrices, f, g, h).color(Colors.BLACK).normal(matrices, i, j, k);
    }
}
