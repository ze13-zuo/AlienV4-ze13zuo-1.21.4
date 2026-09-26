package dev.luminous.asm.mixins;

import dev.luminous.mod.modules.impl.render.Xray;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockModelRenderer.class)
public abstract class MixinBlockModelRenderer {
   @Inject(method = {"renderSmooth", "renderFlat"}, at = @At("HEAD"), cancellable = true)
   private void onRenderSmooth(
      BlockRenderView world,
      BakedModel model,
      BlockState state,
      BlockPos pos,
      MatrixStack matrices,
      VertexConsumer vertexConsumer,
      boolean cull,
      Random random,
      long seed,
      int overlay,
      CallbackInfo info
   ) {
      if (Xray.shouldBlock(state)) {
         info.cancel();
      }
   }

   @Redirect(
      method = "renderSmooth",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/block/Block;shouldDrawSide(Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;)Z"
      )
   )
   private boolean alien$xrayShouldDrawSideSmooth(
      BlockState state,
      BlockState adjState,
      Direction side,
      BlockRenderView world,
      BakedModel model,
      BlockState selfState,
      BlockPos pos,
      MatrixStack matrices,
      VertexConsumer vertexConsumer,
      boolean cull,
      Random random,
      long seed,
      int overlay
   ) {
      boolean original = Block.shouldDrawSide(state, adjState, side);
      return Xray.INSTANCE.isOn() ? Xray.INSTANCE.modifyDrawSide(state, world, pos, side, original) : original;
   }

   @Redirect(
      method = "renderFlat",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/block/Block;shouldDrawSide(Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;)Z"
      )
   )
   private boolean alien$xrayShouldDrawSideFlat(
      BlockState state,
      BlockState adjState,
      Direction side,
      BlockRenderView world,
      BakedModel model,
      BlockState selfState,
      BlockPos pos,
      MatrixStack matrices,
      VertexConsumer vertexConsumer,
      boolean cull,
      Random random,
      long seed,
      int overlay
   ) {
      boolean original = Block.shouldDrawSide(state, adjState, side);
      return Xray.INSTANCE.isOn() ? Xray.INSTANCE.modifyDrawSide(state, world, pos, side, original) : original;
   }
}
