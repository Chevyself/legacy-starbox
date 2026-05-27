package me.googas.starbox.commands.providers;

import java.util.ArrayList;
import java.util.List;

import com.github.chevyself.starbox.bukkit.context.CommandContext;
import com.github.chevyself.starbox.bukkit.providers.type.BukkitArgumentProvider;
import com.github.chevyself.starbox.exceptions.ArgumentProviderException;
import lombok.NonNull;
import me.googas.starbox.BukkitLine;
import net.md_5.bungee.api.chat.ComponentBuilder;

/**
 * Provides {@link net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention} to the {@link
 * com.github.chevyself.starbox.CommandManager}.
 */
public class FormatRetentionProvider
    implements BukkitArgumentProvider<ComponentBuilder.FormatRetention> {

  @NonNull private static final List<String> suggestions = new ArrayList<>();

  static {
    for (ComponentBuilder.FormatRetention value : ComponentBuilder.FormatRetention.values()) {
      FormatRetentionProvider.suggestions.add(value.name().toLowerCase());
    }
  }

  @Override
  public @NonNull Class<ComponentBuilder.FormatRetention> getClazz() {
    return ComponentBuilder.FormatRetention.class;
  }

  @Override
  public @NonNull ComponentBuilder.FormatRetention fromString(
      @NonNull String string, @NonNull CommandContext context) throws ArgumentProviderException {
    try {
      return ComponentBuilder.FormatRetention.valueOf(string.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw BukkitLine.localized(context.getSender(), "invalid.format-retention")
          .format(string)
          .formatSample()
          .asProviderException();
    }
  }

  @Override
  public @NonNull List<String> getSuggestions(@NonNull String s, @NonNull CommandContext commandContext) {
    return FormatRetentionProvider.suggestions;
  }
}
