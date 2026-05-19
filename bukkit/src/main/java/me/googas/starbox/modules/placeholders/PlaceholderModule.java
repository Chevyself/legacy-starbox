package me.googas.starbox.modules.placeholders;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.NonNull;
import me.googas.starbox.modules.Module;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

/** Module to build placeholders from {@link String}. */
public class PlaceholderModule implements Module {

  public static final Pattern PATTERN = Pattern.compile("%.*?%");
  @NonNull private final Map<Plugin, Set<Placeholder>> map = new HashMap<>();

  /**
   * Register a {@link Placeholder}.
   *
   * @param plugin the plugin that is registering the placeholder
   * @param placeholder the placeholder that is being registered
   * @return this same instance
   */
  @NonNull
  public PlaceholderModule register(@NonNull Plugin plugin, @NonNull Placeholder placeholder) {
    map.computeIfAbsent(plugin, key -> new HashSet<>()).add(placeholder);
    return this;
  }

  /**
   * Register many {@link Placeholder}.
   *
   * @param plugin the plugin that is registering the placeholders
   * @param placeholders the placeholders that are being registered
   * @return this same instance
   */
  @NonNull
  public PlaceholderModule registerAll(
      @NonNull Plugin plugin, @NonNull Collection<? extends Placeholder> placeholders) {
    this.map.computeIfAbsent(plugin, pluginKey -> new HashSet<>()).addAll(placeholders);
    return this;
  }

  /**
   * Register many {@link Placeholder}.
   *
   * @param plugin the plugin that is registering the placeholders
   * @param placeholders the placeholders that are being registered
   * @return this same instance
   */
  @NonNull
  public PlaceholderModule registerAll(@NonNull Plugin plugin, Placeholder... placeholders) {
    return this.registerAll(plugin, Arrays.asList(placeholders));
  }

  /**
   * Unregisters all placeholders from a {@link Plugin}.
   *
   * @param plugin the plugin to unregister the placeholders
   * @return this same instance
   */
  @NonNull
  public PlaceholderModule unregister(@NonNull Plugin plugin) {
    this.map.remove(plugin);
    return this;
  }

  @Override
  public @NonNull String getName() {
    return "placeholders";
  }

  /**
   * Get a placeholder by its name.
   *
   * @param name the name of the placeholder to get
   * @return the placeholder matching the name, might be null
   */
  public Placeholder getPlaceholder(String name) {
    for (Set<Placeholder> placeholders : map.values()) {
      for (Placeholder placeholder : placeholders) {
        if (placeholder.getName().equalsIgnoreCase(name)) {
          return placeholder;
        }
      }
    }
    return null;
  }

  /**
   * Build a {@link String} for a player.
   *
   * @param player the player to build the string for
   * @param raw the raw string to build
   * @return the built string
   */
  @NonNull
  public String build(@NonNull OfflinePlayer player, @NonNull String raw) {
    Matcher matcher = PlaceholderModule.PATTERN.matcher(raw);
    while (matcher.find()) {
      String name = matcher.group().replace("%", "");
      Placeholder placeholder = this.getPlaceholder(name);
      if (placeholder != null) raw = raw.replace("%" + name + "%", placeholder.build(player));
    }
    return raw;
  }
}
