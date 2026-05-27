package me.googas.starbox.commands.providers;

import com.github.chevyself.starbox.bukkit.context.CommandContext;
import com.github.chevyself.starbox.bukkit.providers.type.BukkitArgumentProvider;
import com.github.chevyself.starbox.exceptions.ArgumentProviderException;
import java.util.Arrays;
import java.util.List;
import lombok.NonNull;
import me.googas.reflect.APIVersion;
import me.googas.reflect.wrappers.WrappedClass;
import me.googas.reflect.wrappers.WrappedMethod;
import me.googas.starbox.BukkitLine;
import me.googas.starbox.utility.Versions;
import net.md_5.bungee.api.ChatColor;

/** Provides {@link ChatColor} to the {@link com.github.chevyself.starbox.CommandManager}. */
public class BungeeChatColorProvider implements BukkitArgumentProvider<ChatColor> {

  @NonNull
  private static final WrappedClass<ChatColor> CHAT_COLOR = WrappedClass.of(ChatColor.class);

  @NonNull
  @APIVersion(since = 8, max = 15)
  private static final WrappedMethod<ChatColor> VALUE_OF =
      BungeeChatColorProvider.CHAT_COLOR.getMethod(ChatColor.class, "valueOf", String.class);

  @NonNull
  private static final List<String> suggestions =
      Arrays.asList(
          "dark_blue",
          "dark_green",
          "dark_aqua",
          "dark_red",
          "dark_purple",
          "gold",
          "gray",
          "dark_gray",
          "blue",
          "green",
          "aqua",
          "red",
          "light_purple",
          "yellow",
          "white");

  @Override
  public @NonNull Class<ChatColor> getClazz() {
    return ChatColor.class;
  }

  @Override
  public @NonNull ChatColor fromString(@NonNull String string, @NonNull CommandContext context)
      throws ArgumentProviderException {
    try {
      if (Versions.BUKKIT < 16) {
        return BungeeChatColorProvider.VALUE_OF
            .prepare(null, string.toUpperCase())
            .provide()
            .orElseThrow(() -> new IllegalArgumentException("Could not match color for " + string));
      } else {
        return ChatColor.of(string);
      }
    } catch (IllegalArgumentException e) {
      throw BukkitLine.localized(context.getSender(), "invalid.color")
          .format(string)
          .formatSample()
          .asProviderException();
    }
  }

  @Override
  public @NonNull List<String> getSuggestions(@NonNull String string, CommandContext context) {
    return BungeeChatColorProvider.suggestions;
  }
}
