package com.mega.xty.common.command.argument;

import com.mega.endinglib.api.client.Easing;
import com.mega.endinglib.common.command.CommandsEvent;
import com.mega.xty.common.capability.Limbs;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class LimbArgumentType implements ArgumentType<String> {
    public static final Collection<String> EXAMPLES = Arrays.stream(Limbs.values()).map(Limbs::getName).toList();
    public static LimbArgumentType limb() {
        return new LimbArgumentType();
    }
    @Nullable
    public static Limbs getLimb(CommandContext<?> context, String name) {
        return Limbs.fromName(context.getArgument(name, String.class));
    }
    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readUnquotedString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        CommandsEvent.suggestFromExamples(EXAMPLES, builder);
        return builder.buildFuture();
    }
}
