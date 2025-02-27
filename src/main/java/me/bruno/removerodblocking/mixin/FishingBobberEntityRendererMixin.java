package me.bruno.removerodblocking.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.FishingBobberEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingBobberEntityRenderer.class)
public abstract class FishingBobberEntityRendererMixin extends EntityRenderer<FishingBobberEntity> {

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
        buffer.vertex(matrices, f, g, h).color(-16777216).normal(matrices, i, j, k);
    }
    private static float percentage(int value, int max) {
        return (float)value / (float)max;
    }

    protected FishingBobberEntityRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }
    private Vec3d getHandPos(PlayerEntity player, float f, float tickDelta) {
        int i = player.getMainArm() == Arm.RIGHT ? 1 : -1;
        ItemStack itemStack = player.getMainHandStack();
        if (!itemStack.isOf(Items.FISHING_ROD)) {
            i = -i;
        }

        if (this.dispatcher.gameOptions.getPerspective().isFirstPerson() && player == MinecraftClient.getInstance().player) {
            double m = 960.0 / (double) this.dispatcher.gameOptions.getFov().getValue();
            Vec3d vec3d = this.dispatcher.camera.getProjection().getPosition((float)i * 0.525F, -0.1F).multiply(m).rotateY(f * 0.5F).rotateX(-f * 0.7F);
            return player.getCameraPosVec(tickDelta).add(vec3d);
        } else {
            float g = MathHelper.lerp(tickDelta, player.prevBodyYaw, player.bodyYaw) * 0.017453292F;
            double d = MathHelper.sin(g);
            double e = MathHelper.cos(g);
            float h = player.getScale();
            double j = (double)i * 0.35 * (double)h;
            double k = 0.8 * (double)h;
            float l = player.isInSneakingPose() ? -0.1875F : 0.0F;
            return player.getCameraPosVec(tickDelta).add(-e * j - d * k, (double)l - 0.45 * (double)h, -d * j + e * k);
        }
    }

    @Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/entity/projectile/FishingBobberEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", cancellable = true)
    public void renderExceptInHead(FishingBobberEntity fishingBobberEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        PlayerEntity playerEntity = fishingBobberEntity.getPlayerOwner();
        if (playerEntity == null) {
            return;
        }
        Entity hooked = fishingBobberEntity.getHookedEntity();

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

        // render fishing line
        float h = playerEntity.getHandSwingProgress(g);
        float j = MathHelper.sin(MathHelper.sqrt(h) * 3.1415927F);
        Vec3d vec3d = this.getHandPos(playerEntity, j, g);
        Vec3d vec3d2 = fishingBobberEntity.getLerpedPos(g).add(0.0, 0.25, 0.0);
        float k = (float)(vec3d.x - vec3d2.x);
        float l = (float)(vec3d.y - vec3d2.y);
        float m = (float)(vec3d.z - vec3d2.z);
        VertexConsumer vertexConsumer2 = vertexConsumerProvider.getBuffer(RenderLayer.getLineStrip());
        MatrixStack.Entry entry2 = matrixStack.peek();

        for(int o = 0; o <= 16; ++o) {
            renderFishingLine(k, l, m, vertexConsumer2, entry2, percentage(o, 16), percentage(o + 1, 16));
        }
        ci.cancel();
    }
}
