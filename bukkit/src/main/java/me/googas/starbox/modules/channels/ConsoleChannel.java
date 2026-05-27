package me.googas.starbox.modules.channels;

import com.github.chevyself.starbox.bukkit.utils.BukkitUtils;
import java.util.Locale;
import java.util.Optional;
import lombok.NonNull;
import me.googas.reflect.wrappers.chat.WrappedSoundCategory;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;

/** A channel that is used to send data to the console. */
public class ConsoleChannel implements Channel {

  @NonNull private static final ConsoleChannel instance = new ConsoleChannel();

  private ConsoleChannel() {}

  /**
   * Get the unique instance of the channel.
   *
   * @return the instance
   */
  @NonNull
  public static ConsoleChannel getInstance() {
    return ConsoleChannel.instance;
  }

  @Override
  public @NonNull ConsoleChannel send(@NonNull BaseComponent... components) {
    BukkitUtils.send(Bukkit.getConsoleSender(), components);
    return this;
  }

  @Override
  public @NonNull ConsoleChannel send(@NonNull String text) {
    Bukkit.getConsoleSender().sendMessage(text);
    return this;
  }

  @Override
  public @NonNull ConsoleChannel sendTitle(
      String title, String subtitle, int fadeIn, int stay, int fadeOut) {
    if (title != null) this.send(title);
    if (subtitle != null) this.send(subtitle);
    return this;
  }

  @Override
  public @NonNull ConsoleChannel setTabList(String header, String bottom) {
    return this;
  }

  @Override
  public @NonNull Channel playSound(
      @NonNull Location location,
      @NonNull Sound sound,
      @NonNull WrappedSoundCategory category,
      float volume,
      float pitch) {
    return this;
  }

  @Override
  public @NonNull Channel playSound(
      @NonNull Location location, @NonNull Sound sound, float volume, float pitch) {
    return this;
  }

  @Override
  public Optional<Locale> getLocale() {
    return Optional.of(Locale.ENGLISH);
  }
}
