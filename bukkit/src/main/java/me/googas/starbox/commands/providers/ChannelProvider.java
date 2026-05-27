package me.googas.starbox.commands.providers;

import com.github.chevyself.starbox.bukkit.context.CommandContext;
import com.github.chevyself.starbox.bukkit.providers.type.BukkitExtraArgumentProvider;
import lombok.NonNull;
import me.googas.starbox.modules.channels.Channel;

/** Provides {@link Channel} to the {@link com.github.chevyself.starbox.CommandManager}. */
public class ChannelProvider implements BukkitExtraArgumentProvider<Channel> {

  @Override
  public @NonNull Class<Channel> getClazz() {
    return Channel.class;
  }

  @Override
  public @NonNull Channel getObject(@NonNull CommandContext context) {
    return Channel.of(context.getSender());
  }
}
