package net.dumbcode.projectnublar.ik.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.dumbcode.projectnublar.entity.ik.parts.ik_chains.IKChain;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;

public class TestCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> {
            dispatcher.register(
                    Commands.literal("step")
                            .then(Commands.literal("change")
                                    .then(Commands.argument("amount", IntegerArgumentType.integer())
                                            .executes(context -> {
                                                int amount = IntegerArgumentType.getInteger(context, "amount");

                                                IKChain.MAXLECK += amount;

                                                return 1;
                                            })
                                    )));

            dispatcher.register(
                    Commands.literal("step")
                            .then(Commands.literal("set")
                                    .then(Commands.argument("amount", IntegerArgumentType.integer())
                                            .executes(context -> {
                                                int amount = IntegerArgumentType.getInteger(context, "amount");

                                                IKChain.MAXLECK = amount;

                                                return 1;
                                            })
                                    )));
        });
    }
}
