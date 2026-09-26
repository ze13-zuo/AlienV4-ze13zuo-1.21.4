package dev.luminous.asm.mixins;

import dev.luminous.api.utils.render.SimpleItemModel;
import dev.luminous.mod.modules.impl.render.NoRender;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer {
   @Unique
   private static final SimpleItemModel alien$flattenedModel = new SimpleItemModel();
   @Unique
   private static ModelTransformationMode alien$renderMode;

   @Inject(
      method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;IILnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;I)V",
      at = @At("HEAD")
   )
   private void alien$getRenderType(
      ItemStack itemStack,
      ModelTransformationMode transformationMode,
      int light,
      int overlay,
      MatrixStack matrices,
      VertexConsumerProvider vertexConsumers,
      World world,
      int seed,
      CallbackInfo ci
   ) {
      alien$renderMode = transformationMode;
   }

   @Inject(
      method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;III)V",
      at = @At("HEAD")
   )
   private void alien$getRenderTypeEntity(
      LivingEntity entity,
      ItemStack itemStack,
      ModelTransformationMode transformationMode,
      boolean leftHand,
      MatrixStack matrices,
      VertexConsumerProvider vertexConsumers,
      World world,
      int light,
      int overlay,
      int seed,
      CallbackInfo ci
   ) {
      alien$renderMode = transformationMode;
   }

   @ModifyVariable(method = "renderBakedItemModel", at = @At("HEAD"), index = 0, argsOnly = true, require = 0)
   private static BakedModel alien$replaceItemModelClass(
      BakedModel value, BakedModel model, int[] colors, int light, int overlay, MatrixStack matrices, VertexConsumer vertices
   ) {
      if (NoRender.INSTANCE.isOn()
         && NoRender.INSTANCE.fastItem.getValue()
         && !NoRender.INSTANCE.renderSidesOfItems.getValue()
         && !model.hasDepth()
         && alien$renderMode == ModelTransformationMode.GROUND) {
         alien$flattenedModel.setItem(model);
         return alien$flattenedModel;
      } else {
         return value;
      }
   }
}
