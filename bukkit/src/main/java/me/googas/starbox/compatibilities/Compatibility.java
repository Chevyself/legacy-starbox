package me.googas.starbox.compatibilities;

import java.util.Collection;

import com.github.chevyself.starbox.bukkit.commands.BukkitCommand;
import com.github.chevyself.starbox.bukkit.context.CommandContext;
import com.github.chevyself.starbox.providers.StarboxContextualProvider;
import lombok.NonNull;
import me.googas.starbox.Starbox;
import me.googas.starbox.StarboxPlugin;
import me.googas.starbox.modules.Module;
import org.bukkit.plugin.Plugin;

/**
 * This object represents another plugin which can be used with Starbox or which ever plugin that
 * implements {@link CompatibilityManager}. The {@link CompatibilityManager} is used to check if the
 * compatibilities are loaded and use them.
 *
 * <p>Implement this class in the plugin that you need. It is very important that in the class that
 * implements it you don't import anything related to the compatibility because if it is not loaded
 * an exception will be thrown when the class loader checks the imports.
 */
public interface Compatibility {

  /**
   * Get the modules which can be engaged if the compatibility is loaded.
   *
   * @param plugin the plugin which is available to register the modules
   * @return the modules that can be engaged
   */
  @NonNull
  Collection<Module> getModules(@NonNull Plugin plugin);

  /**
   * If the compatibility is enabled this method will be called in {@link StarboxPlugin#onEnable()}.
   *
   * <p>This method should be overridden by each compatibility
   */
  default void onEnable() {
    Starbox.getPlugin().getLogger().info(this.getName() + " has been enabled");
  }

  /**
   * Set whether the compatibility is enabled. This method should only be called by a {@link
   * CompatibilityManager}
   *
   * @param bol the new value of enabled
   */
  void setEnabled(boolean bol);

  /**
   * Compatibilities can register commands.
   *
   * @see #getCommands()
   *     <p>Therefore, it is important that it can also register providers
   * @return the collection of providers to register
   */
  Collection<StarboxContextualProvider<?, CommandContext>> getProviders();

  /**
   * Get the commands to register with this compatibility.
   *
   * @return the collection of commands to register
   */
  @NonNull
  Collection<BukkitCommand> getCommands();

  /**
   * Get whether the compatibility is in the class path and could be enabled.
   *
   * @return true if the compatibility can be used
   */
  boolean isEnabled();

  /**
   * Get the name of the compatibility.
   *
   * <p>This must be the name of the Bukkit plugin as the {@link CompatibilityManager} checks if the
   * plugins are loaded with the name of it. This means the name set in the `plugin.yml` of the
   * plugin.
   *
   * @return the name of the compatibility
   */
  @NonNull
  String getName();
}
