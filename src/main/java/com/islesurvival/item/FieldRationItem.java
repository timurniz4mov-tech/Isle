package com.islesurvival.item;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

public class FieldRationItem extends Item {
    public FieldRationItem(Properties properties) {
        super(properties.food(new FoodProperties.Builder().nutrition(6).saturationMod(0.6F).alwaysEat().build()));
    }
}
