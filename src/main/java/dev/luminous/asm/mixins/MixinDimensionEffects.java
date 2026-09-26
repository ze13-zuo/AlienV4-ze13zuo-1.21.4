package dev.luminous.asm.mixins;

import dev.luminous.mod.modules.impl.render.Ambience;
import java.awt.Color;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// 1.21.4: getFogColorOverride 被移除；adjustFogColor 在基类为抽象方法，实际实现位于 Overworld 子类
@Mixin(DimensionEffects.Overworld.class)
public class MixinDimensionEffects {
   @Inject(method = "adjustFogColor", at = @At("HEAD"), cancellable = true)
   private void hookGetFogColorOverride(Vec3d color, float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
      if (Ambience.INSTANCE.isOn() && Ambience.INSTANCE.dimensionColor.booleanValue) {
         Color c = Ambience.INSTANCE.dimensionColor.getValue();
         cir.setReturnValue(new Vec3d(c.getRed() / 255.0, c.getGreen() / 255.0, c.getBlue() / 255.0));
      }
   }
}
