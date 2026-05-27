package me.googas.starbox.commands.providers;

import com.github.chevyself.starbox.bukkit.context.CommandContext;
import com.github.chevyself.starbox.bukkit.providers.type.BukkitArgumentProvider;
import com.github.chevyself.starbox.exceptions.ArgumentProviderException;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import me.googas.starbox.BukkitLine;
import net.md_5.bungee.api.chat.ClickEvent;

/**
 * Provides {@link ClickEvent.Action} to the {@link com.github.chevyself.starbox.CommandManager}.
 */
public class ClickEventActionProvider implements BukkitArgumentProvider<ClickEvent.Action> {

  @NonNull private static final List<String> suggestions = new ArrayList<>();

  static {
    for (ClickEvent.Action value : ClickEvent.Action.values()) {
      ClickEventActionProvider.suggestions.add(value.name().toLowerCase());
    }
  }

  @Override
  public @NonNull Class<ClickEvent.Action> getClazz() {
    return ClickEvent.Action.class;
  }

  @Override
  public @NonNull ClickEvent.Action fromString(
      @NonNull String string, @NonNull CommandContext context) throws ArgumentProviderException {
    try {
      return ClickEvent.Action.valueOf(string.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw BukkitLine.localized(context.getSender(), "invalid.click-event")
          .format(string)
          .formatSample()
          .asProviderException();
    }
  }

  @Override
  public @NonNull List<String> getSuggestions(
      @NonNull String string, @NonNull CommandContext context) {
    return ClickEventActionProvider.suggestions;
  }
}
