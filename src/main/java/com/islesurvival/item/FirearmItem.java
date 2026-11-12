package com.islesurvival.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class FirearmItem extends Item {
    private final float damage;
    private final double range;
    private final int cooldown;
    private final int pellets;
    private final float spread;
    private final Supplier<Item> ammoSupplier;

    public FirearmItem(float damage, double range, int cooldown, int pellets, int ignoredAmmoPerShot,
                       float spread, Supplier<Item> ammoSupplier) {
        super(new Item.Properties().stacksTo(1));
        this.damage = damage;
        this.range = range;
        this.cooldown = cooldown;
        this.pellets = pellets;
        this.spread = spread;
        this.ammoSupplier = ammoSupplier;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack gun = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(gun, true);
        }

        if (!consumeAmmo(player)) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Нет подходящих патронов!"), true);
            return InteractionResultHolder.fail(gun);
        }

        if (level instanceof ServerLevel serverLevel) {
            fire(serverLevel, player);
        }
        player.getCooldowns().addCooldown(this, cooldown);
        level.playSound(null, player.blockPosition(), SoundEvents.CROSSBOW_SHOOT,
                SoundSource.PLAYERS, 0.85F, 0.72F + player.getRandom().nextFloat() * 0.16F);
        return InteractionResultHolder.sidedSuccess(gun, false);
    }

    private boolean consumeAmmo(Player player) {
        Item ammo = ammoSupplier.get();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(ammo)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private void fire(ServerLevel level, Player player) {
        Vec3 origin = player.getEyePosition();
        for (int pellet = 0; pellet < pellets; pellet++) {
            Vec3 direction = player.getLookAngle();
            if (spread > 0.0F) {
                direction = direction.add(
                        (player.getRandom().nextDouble() - 0.5D) * spread,
                        (player.getRandom().nextDouble() - 0.5D) * spread,
                        (player.getRandom().nextDouble() - 0.5D) * spread
                ).normalize();
            }
            Vec3 requestedEnd = origin.add(direction.scale(range));
            BlockHitResult blockHit = level.clip(new ClipContext(origin, requestedEnd,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            Vec3 end = blockHit.getType() == BlockHitResult.Type.MISS ? requestedEnd : blockHit.getLocation();

            AABB searchBox = player.getBoundingBox().expandTowards(direction.scale(range)).inflate(2.0D);
            List<Entity> candidates = level.getEntities(player, searchBox,
                    entity -> entity instanceof LivingEntity && entity.isPickable());
            Entity nearest = null;
            Vec3 nearestPoint = null;
            double nearestDistance = origin.distanceToSqr(end);

            for (Entity candidate : candidates) {
                Optional<Vec3> intersection = candidate.getBoundingBox().inflate(0.25D).clip(origin, end);
                if (intersection.isPresent()) {
                    double distance = origin.distanceToSqr(intersection.get());
                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearest = candidate;
                        nearestPoint = intersection.get();
                    }
                }
            }

            if (nearest instanceof LivingEntity target && target.isAlive()) {
                target.hurt(level.damageSources().playerAttack(player), damage);
                if (nearestPoint != null) {
                    level.sendParticles(ParticleTypes.CRIT, nearestPoint.x, nearestPoint.y, nearestPoint.z,
                            5, 0.08D, 0.08D, 0.08D, 0.03D);
                }
            }
        }
    }
}
