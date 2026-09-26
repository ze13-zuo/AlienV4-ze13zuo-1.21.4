package dev.luminous.asm.mixins;

import dev.luminous.api.interfaces.IShaderEffectHook;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;

// 1.21.4: PostEffectProcessor 被重写，不再存在 targetsByName/close，自定义帧缓冲注入暂不可用，降级为空实现
@Mixin(PostEffectProcessor.class)
public class MixinShaderEffect implements IShaderEffectHook {
   @Unique
   private final Map<String, Framebuffer> alien$fakedBuffers = new HashMap<>();

   @Override
   public void alienClient$addHook(String name, Framebuffer buffer) {
      this.alien$fakedBuffers.put(name, buffer);
   }
}
