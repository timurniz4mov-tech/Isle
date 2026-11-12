package com.islesurvival.item;

import com.islesurvival.IsleSurvivalMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, IsleSurvivalMod.MOD_ID);

    private ModItems() { }

    public static final RegistryObject<Item> RIFLE_AMMO = ITEMS.register("rifle_ammo",
            () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> SHOTGUN_SHELLS = ITEMS.register("shotgun_shells",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> HUNTING_RIFLE = ITEMS.register("hunting_rifle",
            () -> new FirearmItem(16.0F, 62.0D, 28, 1, 1, 0.0F, () -> RIFLE_AMMO.get()));
    public static final RegistryObject<Item> SCOPED_RIFLE = ITEMS.register("scoped_rifle",
            () -> new FirearmItem(24.0F, 88.0D, 44, 1, 1, 0.0F, () -> RIFLE_AMMO.get()));
    public static final RegistryObject<Item> SEMI_AUTO_RIFLE = ITEMS.register("semi_auto_rifle",
            () -> new FirearmItem(11.0F, 52.0D, 10, 1, 1, 0.008F, () -> RIFLE_AMMO.get()));
    public static final RegistryObject<Item> SUBMACHINE_GUN = ITEMS.register("submachine_gun",
            () -> new FirearmItem(5.0F, 34.0D, 4, 1, 1, 0.028F, () -> RIFLE_AMMO.get()));
    public static final RegistryObject<Item> PUMP_SHOTGUN = ITEMS.register("pump_shotgun",
            () -> new FirearmItem(3.7F, 21.0D, 19, 8, 1, 0.095F, () -> SHOTGUN_SHELLS.get()));

    public static final RegistryObject<Item> MEDKIT = ITEMS.register("medkit", MedkitItem::new);
    public static final RegistryObject<Item> FIELD_RATION = ITEMS.register("field_ration",
            () -> new FieldRationItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> KEYCARD = ITEMS.register("keycard",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> RADIO_PART = ITEMS.register("radio_part",
            () -> new Item(new Item.Properties().stacksTo(16)));
}
