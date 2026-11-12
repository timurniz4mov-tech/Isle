package com.islesurvival.event;

import com.islesurvival.IsleSurvivalMod;
import com.islesurvival.entity.IsleStalkerEntity;
import com.islesurvival.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = IsleSurvivalMod.MOD_ID)
public final class IsleWorldEvents {
    private static final Map<UUID, Long> EXTRACTION_TIMERS = new ConcurrentHashMap<>();

    private IsleWorldEvents() { }

    public static void beginExtraction(ServerPlayer player) {
        EXTRACTION_TIMERS.put(player.getUUID(), player.serverLevel().getGameTime() + 600L);
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        if (!level.dimension().equals(Level.OVERWORLD)) return;
        if (level.getGameTime() % 20L == 0L) updateExtractions(level);
        if (level.getDifficulty() == Difficulty.PEACEFUL || level.getGameTime() % 200L != 0L) return;

        long dayTime = level.getDayTime() % 24000L;
        long day = level.getDayTime() / 24000L;
        boolean night = dayTime >= 13000L && dayTime <= 23000L;
        if (!night) return;

        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || player.isCreative()) continue;
            if (level.random.nextInt(3) == 0 && day >= 2) spawnStalker(level, player);
            if (day >= 2 && level.random.nextInt(8) == 0) spawnMercenaryPatrol(level, player);
        }
    }

    private static void updateExtractions(ServerLevel level) {
        for (Map.Entry<UUID, Long> entry : EXTRACTION_TIMERS.entrySet()) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null || !player.isAlive()) {
                EXTRACTION_TIMERS.remove(entry.getKey());
                continue;
            }
            if (player.serverLevel() != level) continue;
            long ticksLeft = entry.getValue() - level.getGameTime();
            if (ticksLeft <= 0L) {
                player.displayClientMessage(Component.literal("Эвакуация завершена! Ты покинул остров. Побег удался!"), false);
                player.giveExperiencePoints(150);
                EXTRACTION_TIMERS.remove(entry.getKey());
            } else {
                player.displayClientMessage(Component.literal("Эвакуация прибудет через " + ((ticksLeft + 19L) / 20L) + " сек."), true);
            }
        }
    }

    private static void spawnStalker(ServerLevel level, ServerPlayer player) {
        if (!level.getEntitiesOfClass(IsleStalkerEntity.class, player.getBoundingBox().inflate(72.0D)).isEmpty()) return;
        BlockPos spawnPos = randomGround(level, player.blockPosition(), 22, 38);
        if (spawnPos == null || !level.getBlockState(spawnPos.below()).blocksMotion()) return;
        IsleStalkerEntity stalker = ModEntities.STALKER.get().create(level);
        if (stalker == null) return;
        stalker.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                level.random.nextFloat() * 360.0F, 0.0F);
        stalker.setCustomName(Component.literal("Охотник"));
        stalker.setPersistenceRequired();
        level.addFreshEntity(stalker);
        player.displayClientMessage(Component.literal("В темноте слышится тяжёлое дыхание..."), true);
    }

    private static void spawnMercenaryPatrol(ServerLevel level, ServerPlayer player) {
        if (!level.getEntitiesOfClass(Pillager.class, player.getBoundingBox().inflate(90.0D),
                mob -> mob.getCustomName() != null && mob.getCustomName().getString().equals("Наёмник Isle")).isEmpty()) return;
        BlockPos base = randomGround(level, player.blockPosition(), 28, 44);
        if (base == null) return;
        for (int i = 0; i < 3; i++) {
            BlockPos pos = base.offset(level.random.nextInt(7) - 3, 0, level.random.nextInt(7) - 3);
            Pillager mercenary = EntityType.PILLAGER.create(level);
            if (mercenary == null) continue;
            mercenary.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
                    level.random.nextFloat() * 360.0F, 0.0F);
            mercenary.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
            mercenary.setCustomName(Component.literal("Наёмник Isle"));
            mercenary.setPersistenceRequired();
            level.addFreshEntity(mercenary);
        }
    }

    private static BlockPos randomGround(ServerLevel level, BlockPos center, int minRadius, int maxRadius) {
        double angle = level.random.nextDouble() * Math.PI * 2.0D;
        int radius = minRadius + level.random.nextInt(Math.max(1, maxRadius - minRadius + 1));
        int x = center.getX() + (int) Math.round(Math.cos(angle) * radius);
        int z = center.getZ() + (int) Math.round(Math.sin(angle) * radius);
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, center.getY(), z));
        if (!level.isEmptyBlock(surface) || !level.getBlockState(surface.below()).blocksMotion()) return null;
        return surface;
    }
}
