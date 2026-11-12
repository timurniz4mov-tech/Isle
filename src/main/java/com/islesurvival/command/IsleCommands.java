package com.islesurvival.command;

import com.islesurvival.IsleSurvivalMod;
import com.islesurvival.item.ModItems;
import com.islesurvival.event.IsleWorldEvents;
import com.islesurvival.world.IsleIslandBuilder;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = IsleSurvivalMod.MOD_ID)
public final class IsleCommands {
    private IsleCommands() { }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("isle")
                .then(Commands.literal("start").executes(context -> start(context.getSource())))
                .then(Commands.literal("arsenal").executes(context -> arsenal(context.getSource())))
                .then(Commands.literal("escape").executes(context -> escape(context.getSource())))
                .then(Commands.literal("help").executes(context -> help(context.getSource()))));
    }

    private static int start(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();
        if (!level.dimension().equals(Level.OVERWORLD)) {
            source.sendFailure(Component.literal("Команду /isle start нужно запускать в обычном мире."));
            return 0;
        }
        BlockPos spawn = IsleIslandBuilder.build(level, player.blockPosition(), player);
        player.teleportTo(level, spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D,
                java.util.Set.of(), 0.0F, 0.0F);
        player.setRespawnPosition(level.dimension(), spawn, 0.0F, true, false);
        source.sendSuccess(() -> Component.literal("Остров Isle создан! Найди кораблекрушение, исследовательскую станцию и радиовышку."), false);
        player.displayClientMessage(Component.literal("Подсказка: /isle arsenal выдаёт тестовое оружие и патроны."), false);
        return 1;
    }

    private static int arsenal(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        give(player, ModItems.HUNTING_RIFLE.get(), 1);
        give(player, ModItems.SCOPED_RIFLE.get(), 1);
        give(player, ModItems.SEMI_AUTO_RIFLE.get(), 1);
        give(player, ModItems.SUBMACHINE_GUN.get(), 1);
        give(player, ModItems.PUMP_SHOTGUN.get(), 1);
        give(player, ModItems.RIFLE_AMMO.get(), 96);
        give(player, ModItems.SHOTGUN_SHELLS.get(), 32);
        give(player, ModItems.MEDKIT.get(), 4);
        source.sendSuccess(() -> Component.literal("Тестовый арсенал выдан."), false);
        return 1;
    }

    private static int escape(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        int keycards = count(player, ModItems.KEYCARD.get());
        int parts = count(player, ModItems.RADIO_PART.get());
        if (keycards < 1 || parts < 2) {
            source.sendFailure(Component.literal("Для радиопобега нужны 1 ключ-карта и 2 детали радиомаяка. Ищи их в тайниках лаборатории."));
            return 0;
        }
        consume(player, ModItems.KEYCARD.get(), 1);
        consume(player, ModItems.RADIO_PART.get(), 2);
        IsleWorldEvents.beginExtraction(player);
        source.sendSuccess(() -> Component.literal("Сигнал отправлен! Продержись 30 секунд — эвакуация уже в пути."), false);
        return 1;
    }

    private static int count(ServerPlayer player, Item item) {
        int total = 0;
        for (ItemStack stack : player.getInventory().items) if (stack.is(item)) total += stack.getCount();
        return total;
    }

    private static void consume(ServerPlayer player, Item item, int amount) {
        for (ItemStack stack : player.getInventory().items) {
            if (amount <= 0) break;
            if (stack.is(item)) {
                int removed = Math.min(amount, stack.getCount());
                stack.shrink(removed);
                amount -= removed;
            }
        }
    }

    private static int help(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("/isle start — построить остров вокруг себя (используй в новом мире один раз)."), false);
        source.sendSuccess(() -> Component.literal("/isle arsenal — получить всё оружие, патроны и аптечки для теста."), false);
        source.sendSuccess(() -> Component.literal("/isle escape — начать радиопобег, если найдены ключ-карта и 2 детали радиомаяка."), false);
        source.sendSuccess(() -> Component.literal("На острове ищи корабль, лабораторию, подземную комнату, радиовышку и эвакуационный причал."), false);
        return 1;
    }

    private static void give(ServerPlayer player, Item item, int count) {
        ItemStack stack = new ItemStack(item, count);
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }
}
