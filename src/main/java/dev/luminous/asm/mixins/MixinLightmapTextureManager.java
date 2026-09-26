package dev.luminous.asm.mixins;

import dev.luminous.mod.modules.impl.render.Ambience;
import dev.luminous.mod.modules.impl.render.NoRender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightmapTextureManager.class)
public class MixinLightmapTextureManager {
   @Shadow
   private boolean dirty;
   @Shadow
   private float flickerIntensity;
   @Final
   @Shadow
   private GameRenderer renderer;
   @Final
   @Shadow
   private MinecraftClient client;

   @Shadow
   public static float getBrightness(DimensionType type, int lightLevel) {
      float f = lightLevel / 15.0F;
      float g = f / (4.0F - 3.0F * f);
      return MathHelper.lerp(type.ambientLight(), g, 1.0F);
   }

   @Redirect(
      method = "update",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z",
         ordinal = 0,
         remap = false
      ),
      require = 0
   )
   private boolean nightVisionHook(ClientPlayerEntity instance, RegistryEntry<StatusEffect> registryEntry) {
      return Ambience.INSTANCE.isOn() && Ambience.INSTANCE.fullBright.getValue() ? true : instance.hasStatusEffect(registryEntry);
   }

   // 1.21.4: updateHook removed due to NativeImage.setColor private and getProfiler removal

   @Inject(method = "getDarknessFactor(F)F", at = @At("HEAD"), cancellable = true)
   private void getDarknessFactor(float tickDelta, CallbackInfoReturnable<Float> info) {
      if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.darkness.getValue()) {
         info.setReturnValue(0.0F);
      }
   }

   @Shadow
   private float getDarknessFactor(float delta) {
      StatusEffectInstance statusEffectInstance = this.client.player.getStatusEffect(StatusEffects.DARKNESS);
      return statusEffectInstance != null ? statusEffectInstance.getFadeFactor(this.client.player, delta) : 0.0F;
   }

   @Shadow
   private float getDarkness(LivingEntity entity, float factor, float delta) {
      float f = 0.45F * factor;
      return Math.max(0.0F, MathHelper.cos((entity.age - delta) * (float) Math.PI * 0.025F) * f);
   }
}
