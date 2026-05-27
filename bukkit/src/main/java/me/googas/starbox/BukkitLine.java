package me.googas.starbox;

import com.github.chevyself.starbox.bukkit.utils.BukkitUtils;
import com.github.chevyself.starbox.common.Components;
import com.github.chevyself.starbox.exceptions.ArgumentProviderException;
import com.github.chevyself.starbox.result.Result;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import me.googas.starbox.builders.Line;
import me.googas.starbox.modules.channels.Channel;
import me.googas.starbox.modules.channels.ForwardingChannel;
import me.googas.starbox.modules.channels.PlayerChannel;
import me.googas.starbox.modules.language.LanguageModule;
import me.googas.starbox.modules.placeholders.PlaceholderModule;
import me.googas.starbox.utility.BaseComponentResult;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Implementation of {@link Line} to be used in 'Bukkit'. */
public interface BukkitLine extends Line {

  /**
   * Start a localized line.
   *
   * @param locale the locale to get the language
   * @param key the key to get the json/text message
   * @return a new {@link Localized} instance
   */
  @NonNull
  static Localized localized(@NonNull Locale locale, @NonNull String key) {
    return new Localized(
        locale, Starbox.getModules().require(LanguageModule.class).getRaw(locale, key).trim());
  }

  /**
   * Start a localized line.
   *
   * @param sender the sender to get the language
   * @param key the key to get the json/text message
   * @return a new {@link Localized} instance
   */
  @NonNull
  static Localized localized(@NonNull CommandSender sender, @NonNull String key) {
    return BukkitLine.localized(BukkitLanguage.getLocale(sender), key);
  }

  /**
   * Start a localized line.
   *
   * @param channel the channel to get the language
   * @param key the key to get the json/text message
   * @return a new {@link Localized} instance
   */
  static Localized localized(@NonNull Channel channel, String key) {
    return BukkitLine.localized(channel.getLocale().orElse(Locale.ENGLISH), key);
  }

  /**
   * Get the localized lines for a forwarding channel.
   *
   * @param forwardingChannel the forwarding channel to get the lines
   * @param key the key to get the json/text message
   * @return a {@link List} containing the lines
   */
  static List<Localized> localized(
      @NonNull ForwardingChannel.Multiple forwardingChannel, @NonNull String key) {
    return forwardingChannel.getChannels().stream()
        .map(channel -> BukkitLine.localized(channel, key))
        .collect(Collectors.toList());
  }

  /**
   * Get a localized reference from a key.
   *
   * @param key the key of the localized message
   * @return ta new {@link LocalizedReference} instance
   */
  static LocalizedReference localized(@NonNull String key) {
    return new LocalizedReference(key);
  }

  /**
   * Start a plain line.
   *
   * @param text the text of the line
   * @return a plain line
   */
  static Plain of(@NonNull String text) {
    return new BukkitLine.Plain(text);
  }

  /**
   * Parse a line from a string. If the string starts with 'localized:' a {@link LocalizedReference}
   * will be returned else a {@link Plain} will be provided
   *
   * @param string the string to parse
   * @return the parsed line
   */
  @NonNull
  static BukkitLine parse(@NonNull String string) {
    if (!string.contains(" ") && (string.startsWith("localized:") || string.startsWith("$"))) {
      if (string.startsWith("localized:")) {
        string = string.substring(10);
      } else if (string.startsWith("$")) {
        string = string.substring(1);
      }
      return BukkitLine.localized(string);
    }
    return BukkitLine.of(string);
  }

  /**
   * Parse a line from a string. If the string starts with 'localized:' a {@link Localized} will be
   * returned else a {@link Plain} will be provided
   *
   * @param locale the locale parsing the line
   * @param string the string to parse
   * @return the parsed line
   */
  @Deprecated
  static BukkitLine parse(Locale locale, @NonNull String string) {
    if (!string.contains(" ")
        && (string.startsWith("localized:") || string.startsWith("$") && locale != null)) {
      if (string.startsWith("localized:")) {
        string = string.substring(10);
      } else if (string.startsWith("$")) {
        string = string.substring(1);
      }
      return BukkitLine.localized(locale, string);
    } else {
      return BukkitLine.of(string);
    }
  }

  /**
   * Copy this line.
   *
   * @return a new copied instance of this line
   */
  @NonNull
  BukkitLine copy();

  /**
   * Build the line as a {@link Result} for commands.
   *
   * @return the line built as a result
   */
  @NonNull
  Result asResult();

  @Override
  BaseComponent @NonNull [] build();

  /**
   * Build this line with placeholders. The placeholders will be built using {@link
   * PlaceholderModule}
   *
   * @param player the player to build the placeholders
   * @return the built {@link BaseComponent}
   */
  @NonNull
  default BaseComponent[] buildWithPlaceholders(@NonNull OfflinePlayer player) {
    BukkitLine copy = this.copy();
    Starbox.getModules()
        .get(PlaceholderModule.class)
        .ifPresent(module -> copy.setRaw(module.build(player, copy.getRaw())));
    return copy.build();
  }

  /**
   * Build this line with placeholders as {@link String}. The placeholders will be built using
   * {@link PlaceholderModule}
   *
   * @param player the player to build the placeholders
   * @return the built {@link String}
   */
  @NonNull
  default Optional<String> asTextWithPlaceholders(@NonNull OfflinePlayer player) {
    BukkitLine copy = this.copy();
    Starbox.getModules()
        .get(PlaceholderModule.class)
        .ifPresent(module -> copy.setRaw(module.build(player, copy.getRaw())));
    return copy.asText();
  }

  /**
   * Send this line to a {@link Channel}.
   *
   * @see #sendWithPlaceholders(Channel)
   * @param channel the channel to send this line to
   * @param placeholders whether to build this line with placeholders
   */
  default void send(@NonNull Channel channel, boolean placeholders) {
    if (channel instanceof PlayerChannel && placeholders) {
      channel.send(this.buildWithPlaceholders(((PlayerChannel) channel).getOffline()));
    } else {
      this.send(channel);
    }
  }

  /**
   * Send this line to a {@link Channel}.
   *
   * @param channel the channel to send this line to
   */
  default void send(@NonNull Channel channel) {
    channel.send(this.build());
  }

  /**
   * Send this line with placeholders.
   *
   * @param channel the channel to send this line to
   */
  default void sendWithPlaceholders(@NonNull Channel channel) {
    if (channel instanceof PlayerChannel) {
      this.send(channel, true);
    } else {
      this.send(channel);
    }
  }

  /**
   * Set the raw text of the line.
   *
   * @see #getRaw()
   * @param raw the new raw text
   * @return this same instance
   */
  @NonNull
  Line setRaw(@NonNull String raw);

  /**
   * Get the raw text of the line. This is the line without being formatted.
   *
   * <p>Ex: {@link Localized} the raw text is its json
   *
   * @return the raw text
   */
  @NonNull
  String getRaw();

  /**
   * This must be used if the line is a sample line to format it. This line will be formatted using
   * {@link me.googas.starbox.modules.language.SampleFormatter}
   *
   * @return this same instance
   */
  @NonNull
  default BukkitLine formatSample() {
    Starbox.getModules()
        .get(LanguageModule.class)
        .ifPresent(module -> this.format(module.getSampleFormatter()));
    return this;
  }

  /**
   * Get this line as a {@link ArgumentProviderException}.
   *
   * @return the new {@link ArgumentProviderException}
   */
  @NonNull
  default ArgumentProviderException asProviderException() {
    if (this.asText().isPresent()) {
      return new ArgumentProviderException(this.asText().get());
    }
    return new ArgumentProviderException();
  }

  /**
   * Format this sample using a locale.
   *
   * @param locale the locale to format this sample with
   * @return this line formatted
   */
  @NonNull
  default BukkitLine formatSample(@NonNull Locale locale) {
    Starbox.getModules()
        .get(LanguageModule.class)
        .ifPresent(module -> module.getSampleFormatter().format(locale, this));
    return this;
  }

  /** This is a {@link BukkitLine} which uses a message obtained from {@link LanguageModule}. */
  class Localized implements BukkitLine {

    @NonNull @Getter private final Locale locale;
    @NonNull private String json;

    private Localized(@NonNull Locale locale, @NonNull String json) {
      this.locale = locale;
      this.json = json;
    }

    @Override
    public @NonNull String getRaw() {
      return json;
    }

    @NonNull
    public Localized setRaw(@NonNull String json) {
      this.json = json;
      return this;
    }

    @Override
    public @NonNull Localized copy() {
      return new Localized(locale, json);
    }

    @Override
    public @NonNull Result asResult() {
      return new BaseComponentResult(this.build());
    }

    @Override
    public BaseComponent @NonNull [] build() {
      return Components.getComponent(json);
    }

    @Override
    public @NonNull Optional<String> asText() {
      return Optional.of(new TextComponent(this.build()).toLegacyText());
    }

    @Override
    public @NonNull Localized format(@NonNull Object... objects) {
      json = Strings.format(json, objects);
      return this;
    }

    @Override
    public @NonNull Localized format(@NonNull Map<String, String> map) {
      json = Strings.format(json, map);
      return this;
    }

    @Override
    public @NonNull Localized format(@NonNull Formatter formatter) {
      return (Localized) formatter.format(this);
    }
  }

  /** Represents a plain text line. */
  class Plain implements BukkitLine {

    @NonNull private String text;

    private Plain(@NonNull String text) {
      this.text = text;
    }

    @Override
    public @NonNull Plain copy() {
      return new Plain(text);
    }

    @Override
    public @NonNull Result asResult() {
      return new BaseComponentResult(this.build());
    }

    @Override
    public BaseComponent @NonNull [] build() {
      return Components.deserializePlain('&', text);
    }

    @Override
    public @NonNull Optional<String> asText() {
      return Optional.of(BukkitUtils.format(text));
    }

    @Override
    public @NonNull String getRaw() {
      return this.text;
    }

    @Override
    public @NonNull Plain setRaw(@NonNull String raw) {
      this.text = raw;
      return this;
    }

    @Override
    public @NonNull Plain format(@NonNull Object... objects) {
      this.text = String.format(text, objects);
      return this;
    }

    @Override
    public @NonNull Plain format(@NonNull Map<String, String> map) {
      this.text = String.format(text, map);
      return this;
    }

    @Override
    public @NonNull Plain format(@NonNull Formatter formatter) {
      formatter.format(this);
      return this;
    }
  }

  /** Represents a formatter which can format {@link BukkitLine} using {@link Locale}. */
  interface LocalizedFormatter {
    /**
     * Format the line.
     *
     * @param locale the locale to format the line with
     * @param line the line to format
     * @return the formatted line
     */
    @NonNull
    BukkitLine format(@NonNull Locale locale, @NonNull BukkitLine line);
  }

  /** A {@link BukkitLine} that references a language key to be built into {@link Localized}. */
  class LocalizedReference implements BukkitLine {

    /** Objects formatters. */
    @NonNull private final List<Object> objects;
    /** Placeholders formatters. */
    @NonNull private final Map<String, String> placeholders;
    /** Formatters. */
    @NonNull private final List<Formatter> formatters;

    @NonNull private String key;

    private LocalizedReference(@NonNull String key) {
      this(new ArrayList<>(), new HashMap<>(), new ArrayList<>(), key);
    }

    private LocalizedReference(
        @NonNull List<Object> objects,
        @NonNull Map<String, String> placeholders,
        @NonNull List<Formatter> formatters,
        @NonNull String key) {
      this.objects = objects;
      this.placeholders = placeholders;
      this.formatters = formatters;
      this.key = key;
    }

    /**
     * Get the {@link Localized} that this references to.
     *
     * @param locale the locale to get the raw message
     * @return the {@link Localized}
     */
    public @NonNull Localized asLocalized(@NonNull Locale locale) {
      Localized localized = BukkitLine.localized(locale, this.key);
      if (!objects.isEmpty()) localized.format(objects.toArray());
      if (!placeholders.isEmpty()) localized.format(placeholders);
      if (!formatters.isEmpty()) localized.format(formatters);
      return localized;
    }

    /**
     * Get the {@link Localized} that this references to.
     *
     * @param sender the sender to get the locale from
     * @return the {@link Localized}
     */
    public @NonNull Localized asLocalized(@NonNull CommandSender sender) {
      return this.asLocalized(BukkitLanguage.getLocale(sender));
    }

    /**
     * Get the {@link Localized} that this references to.
     *
     * @param channel the channel to get the locale from
     * @return the {@link Localized}
     */
    public @NonNull Localized asLocalized(@NonNull Channel channel) {
      return this.asLocalized(channel.getLocale().orElse(Locale.ENGLISH));
    }

    /**
     * Raw use of {@link Localized}. This will warn the {@link java.util.logging.Logger} when used
     *
     * @return the {@link Localized}
     */
    public @NonNull Localized asLocalized() {
      return this.asLocalized(Locale.ENGLISH);
    }

    @Override
    public @NonNull LocalizedReference copy() {
      return new LocalizedReference(
          new ArrayList<>(this.objects),
          new HashMap<>(this.placeholders),
          new ArrayList<>(this.formatters),
          this.key);
    }

    @Override
    public @NonNull Result asResult() {
      Starbox.getLogger().warning("Raw use of LocalizedReference#asResult");
      return this.asLocalized().asResult();
    }

    @Override
    public BaseComponent @NonNull [] build() {
      Starbox.getLogger().warning("Raw use of LocalizedReference#build");
      return this.asLocalized().build();
    }

    @Override
    public @NonNull Optional<String> asText() {
      Starbox.getLogger().warning("Raw use of LocalizedReference#asText");
      return this.asLocalized().asText();
    }

    @Override
    public @NonNull LocalizedReference format(@NonNull Object... objects) {
      this.objects.addAll(Arrays.asList(objects));
      return this;
    }

    @Override
    public @NonNull LocalizedReference format(@NonNull Map<String, String> map) {
      this.placeholders.putAll(map);
      return this;
    }

    @Override
    public @NonNull LocalizedReference format(@NonNull Formatter formatter) {
      this.formatters.add(formatter);
      return this;
    }

    @Override
    public @NonNull String getRaw() {
      Starbox.getLogger().warning("Raw use of LocalizedReference#getRaw");
      return this.asLocalized().getRaw();
    }

    @Override
    public @NonNull LocalizedReference setRaw(@NonNull String raw) {
      this.key = raw;
      return this;
    }

    @Override
    public BaseComponent[] buildWithPlaceholders(@NonNull OfflinePlayer player) {
      Player onlinePlayer = player.getPlayer();
      return this.asLocalized(
              onlinePlayer == null ? Locale.ENGLISH : BukkitLanguage.getLocale(onlinePlayer))
          .buildWithPlaceholders(player);
    }

    @Override
    public @NonNull Optional<String> asTextWithPlaceholders(@NonNull OfflinePlayer player) {
      Player onlinePlayer = player.getPlayer();
      return this.asLocalized(
              onlinePlayer == null ? Locale.ENGLISH : BukkitLanguage.getLocale(onlinePlayer))
          .asTextWithPlaceholders(player);
    }
  }
}
