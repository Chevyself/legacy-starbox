package me.googas.starbox;

import com.github.chevyself.starbox.common.Components;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/** Represents a {@link BukkitLanguage} at a '.yml' file. */
public class BukkitYamlLanguage implements BukkitLanguage {

  @NonNull @Getter private final Locale locale;
  @NonNull private final ConfigurationSection section;
  private final boolean sample;

  private BukkitYamlLanguage(
      @NonNull Locale locale, @NonNull ConfigurationSection section, boolean sample) {
    this.locale = locale;
    this.section = section;
    this.sample = sample;
  }

  /**
   * Get a language from a reader.
   *
   * @param reader the reader to read the language
   * @return the read language
   */
  @NonNull
  public static BukkitYamlLanguage of(@NonNull Reader reader) {
    YamlConfiguration configuration = YamlConfiguration.loadConfiguration(reader);
    String language =
        Objects.requireNonNull(configuration.getString("language"), "There's no 'language' field");
    boolean sample = language.equalsIgnoreCase("sample");
    return new BukkitYamlLanguage(new Locale(sample ? "en" : language), configuration, sample);
  }

  /**
   * Get a language from a {@link InputStream}. This will create a reader and use it in {@link
   * #of(Reader)}
   *
   * @param resource the stream to start the reader
   * @return the read yaml language
   */
  @NonNull
  public static BukkitYamlLanguage of(@NonNull InputStream resource) {
    InputStreamReader reader = new InputStreamReader(resource);
    BukkitYamlLanguage language = BukkitYamlLanguage.of(reader);
    try {
      reader.close();
    } catch (IOException e) {
      Starbox.warning(e, () -> "Reader was not closed successfully");
    }
    return language;
  }

  /**
   * Get a language from a plugin. This will use the parameter language to search for the resource
   * as: '(language).yml'
   *
   * @param plugin the plugin to get the resource from
   * @param language the name of the language resource
   * @return the read yaml language
   */
  @NonNull
  public static BukkitYamlLanguage of(@NonNull Plugin plugin, @NonNull String language) {
    InputStream resource = plugin.getResource(language + ".yml");
    if (resource != null) {
      return BukkitYamlLanguage.of(resource);
    } else {
      plugin.getLogger().severe("Could not resolve resource " + language + ".yml");
      return new BukkitYamlLanguage(new Locale(language), new YamlConfiguration(), false);
    }
  }

  /**
   * Get many languages from a plugin. This will use the parameter languages to search for the
   * resources as: 'lang/(language).yml'
   *
   * @param plugin the plugin to get the resources from
   * @param languages the name of the language resources
   * @return the read yaml languages
   */
  @NonNull
  public static List<BukkitYamlLanguage> of(@NonNull Plugin plugin, @NonNull String... languages) {
    List<BukkitYamlLanguage> list = new ArrayList<>();
    for (String language : languages) {
      list.add(BukkitYamlLanguage.of(plugin, language));
    }
    return list;
  }

  @NonNull
  public Optional<String> getRaw(@NonNull String key) {
    return Optional.ofNullable(section.getString(key));
  }

  @NonNull
  public BaseComponent[] get(@NonNull String key) {
    return Components.getComponent(Strings.format(this.getRaw(key).orElse(key).trim()));
  }

  @NonNull
  public BaseComponent[] get(@NonNull String key, @NonNull Map<String, String> map) {
    return Components.getComponent(Strings.format(this.getRaw(key).orElse(key), map).trim());
  }

  @NonNull
  public BaseComponent[] get(@NonNull String key, Object... objects) {
    return Components.getComponent(Strings.format(this.getRaw(key).orElse(key), objects).trim());
  }

  @Override
  public boolean isSample() {
    return this.sample;
  }
}
