package com.islesurvival.client;

import com.islesurvival.IsleSurvivalMod;
import com.islesurvival.entity.IsleStalkerEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;

public class IsleStalkerRenderer extends MobRenderer<IsleStalkerEntity, HumanoidModel<IsleStalkerEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(IsleSurvivalMod.MOD_ID,
            "textures/entity/stalker.png");

    public IsleStalkerRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.55F);
    }

    @Override
    public ResourceLocation getTextureLocation(IsleStalkerEntity entity) {
        return TEXTURE;
    }
}
