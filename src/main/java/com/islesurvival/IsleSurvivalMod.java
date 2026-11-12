package com.islesurvival;

import com.islesurvival.entity.ModEntities;
import com.islesurvival.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod(IsleSurvivalMod.MOD_ID)
public class IsleSurvivalMod {
    public static final String MOD_ID = "islesurvival";

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN_TAB = CREATIVE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.islesurvival"))
                    .icon(() -> new ItemStack(ModItems.HUNTING_RIFLE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.HUNTING_RIFLE.get());
                        output.accept(ModItems.SCOPED_RIFLE.get());
                        output.accept(ModItems.SEMI_AUTO_RIFLE.get());
                        output.accept(ModItems.SUBMACHINE_GUN.get());
                        output.accept(ModItems.PUMP_SHOTGUN.get());
                        output.accept(ModItems.RIFLE_AMMO.get());
                        output.accept(ModItems.SHOTGUN_SHELLS.get());
                        output.accept(ModItems.MEDKIT.get());
                        output.accept(ModItems.FIELD_RATION.get());
                        output.accept(ModItems.KEYCARD.get());
                        output.accept(ModItems.RADIO_PART.get());
                    }).build());

    public IsleSurvivalMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        CREATIVE_TABS.register(modBus);
        modBus.addListener(ModEntities::registerAttributes);
    }
}
