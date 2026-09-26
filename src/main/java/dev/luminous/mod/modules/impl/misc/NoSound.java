package dev.luminous.mod.modules.impl.misc;

import dev.luminous.api.events.eventbus.EventListener;
import dev.luminous.api.events.impl.PlaySoundEvent;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.Module.Category;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import java.util.ArrayList;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class NoSound extends Module {
   static final ArrayList<SoundEvent> armor = new ArrayList();
   public static NoSound INSTANCE;
   private final BooleanSetting equip = this.add(new BooleanSetting("ArmorEquip", true));
   private final BooleanSetting explode = this.add(new BooleanSetting("Explode", true));
   private final BooleanSetting attack = this.add(new BooleanSetting("Attack", true));
   private final BooleanSetting teleport = this.add(new BooleanSetting("Teleport", true));
   private final BooleanSetting throwConfig = this.add(new BooleanSetting("Throw", true));
   private final BooleanSetting potion = this.add(new BooleanSetting("Potion", true));
   private final BooleanSetting elytra = this.add(new BooleanSetting("Elytra", true));

   public NoSound() {
      super("NoSound", Module.Category.Misc);
      this.setChinese("去除声音");
      INSTANCE = this;
   }

   @EventListener
   public void onPlaySound(PlaySoundEvent event) {
      if (this.equip.getValue()) {
         for (SoundEvent se : armor) {
            if (event.sound.getId() == se.id()) {
               event.cancel();
               return;
            }
         }
      }

      if (!this.explode.getValue()
         || event.sound.getId() != SoundEvents.ENTITY_GENERIC_EXPLODE.value().id()
            && event.sound.getId() != SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE.id()) {
         if (this.attack.getValue()
            && (
               event.sound.getId() == SoundEvents.ENTITY_PLAYER_ATTACK_WEAK.id()
                  || event.sound.getId() == SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK.id()
                  || event.sound.getId() == SoundEvents.ENTITY_PLAYER_ATTACK_STRONG.id()
            )) {
            event.cancel();
         }

         if (this.teleport.getValue() && event.sound.getId() == SoundEvents.ENTITY_PLAYER_TELEPORT.id()) {
            event.cancel();
         }

         if (this.potion.getValue() && event.sound.getId() == SoundEvents.ENTITY_SPLASH_POTION_BREAK.id()) {
            event.cancel();
         }

         if (this.elytra.getValue() && event.sound.getId() == SoundEvents.ITEM_ELYTRA_FLYING.id()) {
            event.cancel();
         }

         if (this.throwConfig.getValue()
            && (
               event.sound.getId() == SoundEvents.ENTITY_ENDER_PEARL_THROW.id()
                  || event.sound.getId() == SoundEvents.ENTITY_EGG_THROW.id()
                  || event.sound.getId() == SoundEvents.ENTITY_EXPERIENCE_BOTTLE_THROW.id()
                  || event.sound.getId() == SoundEvents.ENTITY_SNOWBALL_THROW.id()
                  || event.sound.getId() == SoundEvents.ENTITY_SPLASH_POTION_THROW.id()
            )) {
            event.cancel();
         }
      } else {
         event.cancel();
      }
   }

   static {
      armor.add((SoundEvent)SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE.value());
      armor.add((SoundEvent)SoundEvents.ITEM_ARMOR_EQUIP_TURTLE.value());
      armor.add((SoundEvent)SoundEvents.ITEM_ARMOR_EQUIP_CHAIN.value());
      armor.add((SoundEvent)SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA.value());
      armor.add((SoundEvent)SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND.value());
      armor.add((SoundEvent)SoundEvents.ITEM_ARMOR_EQUIP_GOLD.value());
      armor.add((SoundEvent)SoundEvents.ITEM_ARMOR_EQUIP_IRON.value());
      armor.add((SoundEvent)SoundEvents.ITEM_ARMOR_EQUIP_LEATHER.value());
      armor.add((SoundEvent)SoundEvents.ITEM_ARMOR_EQUIP_GENERIC.value());
   }
}
