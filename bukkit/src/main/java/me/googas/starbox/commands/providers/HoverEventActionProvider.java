package me.googas.starbox.commands.providers;

import java.util.ArrayList;
import java.util.List;

import com.github.chevyself.starbox.bukkit.context.CommandContext;
import com.github.chevyself.starbox.bukkit.providers.type.BukkitArgumentProvider;
import com.github.chevyself.starbox.exceptions.ArgumentProviderException;
import lombok.NonNull;
import me.googas.starbox.BukkitLine;
import net.md_5.bungee.api.chat.HoverEvent;

/** Provides {@link HoverEvent.Action} to the {@link com.github.chevyself.starbox.CommandManager}. */
public class HoverEventActionProvider implements BukkitArgumentProvider<HoverEvent.Action> {

  @NonNull private static final List<String> suggestions = new ArrayList<>();

  static {
    for (HoverEvent.Action value : HoverEvent.Action.values()) {
      HoverEventActionProvider.suggestions.add(value.name().toLowerCase());
    }
  }

  @Override
  public @NonNull Class<HoverEvent.Action> getClazz() {
    return HoverEvent.Action.class;
  }

  @Override
  public @NonNull HoverEvent.Action fromString(
      @NonNull String string, @NonNull CommandContext context) throws ArgumentProviderException {
    try {
      return HoverEvent.Action.valueOf(string.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw BukkitLine.localized(context.getSender(), "invalid.hover-action")
          .format(string)
          .formatSample()
          .asProviderException();
    }
  }

  @Override
  public @NonNull List<String> getSuggestions(@NonNull String string, @NonNull CommandContext context) {
    return HoverEventActionProvider.suggestions;
  }
}
