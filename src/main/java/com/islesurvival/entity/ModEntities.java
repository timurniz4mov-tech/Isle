package com.islesurvival.entity;

import com.islesurvival.IsleSurvivalMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, IsleSurvivalMod.MOD_ID);

    public static final RegistryObject<EntityType<IsleStalkerEntity>> STALKER = ENTITY_TYPES.register("stalker",
            () -> EntityType.Builder.<IsleStalkerEntity>of(IsleStalkerEntity::new, MobCategory.MONSTER)
                    .sized(0.85F, 2.35F)
                    .clientTrackingRange(10)
                    .build("islesurvival:stalker"));

    private ModEntities() { }

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        AttributeSupplier.Builder attributes = IsleStalkerEntity.createAttributes();
        event.put(STALKER.get(), attributes.build());
    }
}
