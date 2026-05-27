package me.googas.starbox.commands.providers;

import com.github.chevyself.starbox.bukkit.context.CommandContext;
import com.github.chevyself.starbox.bukkit.providers.type.BukkitArgumentProvider;
import com.github.chevyself.starbox.exceptions.ArgumentProviderException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import me.googas.reflect.wrappers.inventory.WrappedEnchantment;
import me.googas.starbox.BukkitLine;
import org.bukkit.enchantments.Enchantment;

/** Provides {@link Enchantment} to the {@link com.github.chevyself.starbox.CommandManager}. */
public class EnchantmentProvider implements BukkitArgumentProvider<Enchantment> {

  @NonNull
  private static final List<String> suggestions =
      WrappedEnchantment.values().stream()
          .map(wrap -> wrap.getName().toLowerCase())
          .collect(Collectors.toList());

  @Override
  public @NonNull Class<Enchantment> getClazz() {
    return Enchantment.class;
  }

  @Override
  public @NonNull Enchantment fromString(@NonNull String string, @NonNull CommandContext context)
      throws ArgumentProviderException {
    try {
      return WrappedEnchantment.valueOf(string).getEnchantment();
    } catch (IllegalArgumentException e) {
      throw BukkitLine.localized(context.getSender(), "invalid.enchantment")
          .format(string)
          .formatSample()
          .asProviderException();
    }
  }

  @Override
  public @NonNull List<String> getSuggestions(@NonNull String s, CommandContext commandContext) {
    return EnchantmentProvider.suggestions;
  }
}
