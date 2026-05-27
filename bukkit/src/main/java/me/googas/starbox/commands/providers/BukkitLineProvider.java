package me.googas.starbox.commands.providers;

import com.github.chevyself.starbox.bukkit.context.CommandContext;
import com.github.chevyself.starbox.bukkit.providers.type.BukkitArgumentProvider;
import com.github.chevyself.starbox.exceptions.ArgumentProviderException;
import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import me.googas.starbox.BukkitLine;

/** Provides {@link BukkitLine} to the {@link com.github.chevyself.starbox.CommandManager}. */
public class BukkitLineProvider implements BukkitArgumentProvider<BukkitLine> {
  @Override
  public @NonNull List<String> getSuggestions(
      @NonNull String string, @NonNull CommandContext commandContext) {
    return Collections.singletonList("$");
  }

  @Override
  public @NonNull BukkitLine fromString(@NonNull String string, @NonNull CommandContext context)
      throws ArgumentProviderException {
    return BukkitLine.parse(string.trim());
  }

  @Override
  public @NonNull Class<BukkitLine> getClazz() {
    return BukkitLine.class;
  }
}
