package dev.luminous.mod.modules.impl.player;

import dev.luminous.api.events.eventbus.EventListener;
import dev.luminous.api.events.impl.PacketEvent;
import dev.luminous.api.events.impl.UpdateEvent;
import dev.luminous.asm.accessors.IPlayerMoveC2SPacket;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.Module.Category;
import dev.luminous.mod.modules.impl.exploit.BowBomb;
import dev.luminous.mod.modules.settings.impl.EnumSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;

public class NoFall extends Module {
   private final EnumSetting<NoFall.NoFallMode> mode = this.add(new EnumSetting("Mode", NoFall.NoFallMode.Packet));
   private final SliderSetting distance = this.add(new SliderSetting("Distance", 3.0, 0.0, 8.0, 0.1));
   private boolean lastPosValid;
   private double lastY;
   private float fallBlocks;

   public NoFall() {
      super("NoFall", "Prevents fall damage.", Module.Category.Player);
      this.setChinese("没有摔落伤害");
   }

   @Override
   public String getInfo() {
      return ((NoFall.NoFallMode)this.mode.getValue()).name();
   }

   @EventListener
   public void onUpdate(UpdateEvent event) {
      if (!nullCheck()) {
         if (this.mode.is(NoFall.NoFallMode.Grim) && this.checkFalling()) {
            mc.getNetworkHandler()
               .sendPacket(
                  new Full(
                     mc.player.getX(),
                     mc.player.getY() + 1.0E-9,
                     mc.player.getZ(),
                     mc.player.getYaw(),
                     mc.player.getPitch(),
                     false,
                     false
                  )
               );
            mc.player.onLanding();
         }
      }
   }

   private boolean checkFalling() {
      return mc.player.fallDistance > mc.player.getSafeFallDistance() && !mc.player.isOnGround() && !mc.player.isGliding();
   }

   @Override
   public void onDisable() {
      this.lastPosValid = false;
      this.fallBlocks = 0.0F;
   }

   @EventListener
   public void onPacketSend(PacketEvent.Send event) {
      if (nullCheck()) {
         return;
      }

      // 仅在真正使用鞘翅滑翔时跳过；仅仅装备鞘翅（未滑翔）不应禁用 NoFall。
      // 客户端 fallDistance 在部分环境下自由落体时会恒为 0，不能作为判据，
      // 因此按 Y 坐标自行累计真实下落格数；超过阈值后把移动包标记为着地，服务端即清除坠落距离、不再造成摔落伤害。
      if (this.mode.is(NoFall.NoFallMode.Packet)
         && !mc.player.isGliding()
         && !BowBomb.send
         && event.getPacket() instanceof PlayerMoveC2SPacket packet) {
         double y = mc.player.getY();
         if (!this.lastPosValid) {
            this.lastPosValid = true;
            this.lastY = y;
         }

         double delta = y - this.lastY;
         this.lastY = y;
         if (mc.player.isOnGround()) {
            this.fallBlocks = 0.0F;
         } else if (delta < 0.0) {
            this.fallBlocks = Math.min(100.0F, this.fallBlocks + (float)(-delta));
         }

         if (this.fallBlocks >= (float)this.distance.getValue()) {
            ((IPlayerMoveC2SPacket)packet).setOnGround(true);
         }
      }
   }

   public static enum NoFallMode {
      Packet,
      Grim;
   }
}
