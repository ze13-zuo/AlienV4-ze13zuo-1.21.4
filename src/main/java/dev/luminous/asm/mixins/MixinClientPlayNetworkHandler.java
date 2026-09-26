package dev.luminous.asm.mixins;

import dev.luminous.Alien;
import dev.luminous.api.events.impl.EntityVelocityUpdateEvent;
import dev.luminous.api.events.impl.GameLeftEvent;
import dev.luminous.api.events.impl.InventoryS2CPacketEvent;
import dev.luminous.api.events.impl.S2CCloseScreenEvent;
import dev.luminous.api.events.impl.SendMessageEvent;
import dev.luminous.api.events.impl.ServerChangePositionEvent;
import dev.luminous.mod.modules.impl.exploit.AntiPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.EnterReconfigurationS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler extends ClientCommonNetworkHandler {
   @Shadow
   private ClientWorld world;
   @Unique
   private boolean alien$worldNotNull;
   @Unique
   private boolean ignore;

   protected MixinClientPlayNetworkHandler(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
      super(client, connection, connectionState);
   }

   @Inject(
      method = "onEnterReconfiguration",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V",
         shift = Shift.AFTER
      )
   )
   private void onEnterReconfiguration(EnterReconfigurationS2CPacket packet, CallbackInfo info) {
      Alien.EVENT_BUS.post(GameLeftEvent.INSTANCE);
   }

   @Inject(method = "onGameJoin", at = @At("HEAD"))
   private void onGameJoinHead(GameJoinS2CPacket packet, CallbackInfo info) {
      this.alien$worldNotNull = this.world != null;
   }

   @Inject(method = "onGameJoin", at = @At("TAIL"))
   private void onGameJoinTail(GameJoinS2CPacket packet, CallbackInfo info) {
      if (this.alien$worldNotNull) {
         Alien.EVENT_BUS.post(GameLeftEvent.INSTANCE);
      }
   }

   @Shadow
   public abstract void sendChatMessage(String var1);

   @Inject(
      method = "onInventory",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V",
         shift = Shift.AFTER
      ),
      cancellable = true
   )
   public void onInventoryS2CPacket(InventoryS2CPacket packet, CallbackInfo ci) {
      InventoryS2CPacketEvent event = InventoryS2CPacketEvent.get(packet);
      Alien.EVENT_BUS.post(event);
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
   private void onSendChatMessage(String message, CallbackInfo ci) {
      if (!this.ignore) {
         if (message.startsWith(Alien.getPrefix())) {
            Alien.COMMAND.command(message.split(" "));
            ci.cancel();
         } else {
            SendMessageEvent event = SendMessageEvent.get(message);
            Alien.EVENT_BUS.post(event);
            if (event.isCancelled()) {
               ci.cancel();
            } else if (!event.message.equals(event.defaultMessage)) {
               this.ignore = true;
               this.sendChatMessage(event.message);
               this.ignore = false;
               ci.cancel();
            }
         }
      }
   }

   @Inject(
      method = "onCloseScreen",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V",
         shift = Shift.AFTER
      ),
      cancellable = true
   )
   public void onCloseScreen(CloseScreenS2CPacket packet, CallbackInfo ci) {
      S2CCloseScreenEvent event = S2CCloseScreenEvent.get();
      Alien.EVENT_BUS.post(event);
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Redirect(method = "onEntityVelocityUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setVelocityClient(DDD)V"), require = 0)
   private void velocityHook(Entity instance, double x, double y, double z) {
      EntityVelocityUpdateEvent event = EntityVelocityUpdateEvent.get(instance, x, y, z, false);
      Alien.EVENT_BUS.post(event);
      if (!event.isCancelled()) {
         instance.setVelocityClient(event.getX(), event.getY(), event.getZ());
      }
   }

   @Redirect(
      method = "onExplosion",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;add(DDD)Lnet/minecraft/util/math/Vec3d;"),
      require = 0
   )
   private Vec3d velocityHook2(Vec3d instance, double x, double y, double z) {
      EntityVelocityUpdateEvent event = EntityVelocityUpdateEvent.get(this.client.player, x, y, z, true);
      Alien.EVENT_BUS.post(event);
      return !event.isCancelled() ? instance.add(event.getX(), event.getY(), event.getZ()) : instance;
   }

}
