package me.googas.starbox.compatibilities.viaversion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;

import com.github.chevyself.starbox.bukkit.commands.BukkitCommand;
import com.github.chevyself.starbox.bukkit.context.CommandContext;
import com.github.chevyself.starbox.providers.StarboxContextualProvider;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.googas.starbox.Starbox;
import me.googas.starbox.compatibilities.Compatibility;
import me.googas.starbox.compatibilities.viaversion.channels.ProtocolChannelsModule;
import me.googas.starbox.modules.Module;
import org.bukkit.plugin.Plugin;

/**
 * Represents the compatibility with the plugin 'ViaVersion'. It is used to have channels that
 * support different protocol versions
 */
public class ViaVersionCompatibility implements Compatibility {

  @Getter @Setter private boolean enabled;

  @Override
  public @NonNull Collection<Module> getModules(@NonNull Plugin plugin) {
    return Collections.singletonList(new ProtocolChannelsModule());
  }

  @Override
  public void onEnable() {
    try {
      Class.forName("us.myles.ViaVersion.api.Via");
      Compatibility.super.onEnable();
    } catch (ClassNotFoundException e) {
      Starbox.getLogger().log(Level.WARNING, "Could not find ViaVersion VIA class");
      this.setEnabled(false);
    }
  }

  @Override
  public Collection<StarboxContextualProvider<?, CommandContext>> getProviders() {
    return new ArrayList<>();
  }

  @Override
  public @NonNull Collection<BukkitCommand> getCommands() {
    return new ArrayList<>();
  }

  @Override
  public @NonNull String getName() {
    return "ViaVersion";
  }
}
