package me.googas.starbox;

import com.github.chevyself.starbox.CommandManager;
import com.github.chevyself.starbox.CommandManagerBuilder;
import com.github.chevyself.starbox.bukkit.BukkitAdapter;
import com.github.chevyself.starbox.bukkit.commands.BukkitCommand;
import com.github.chevyself.starbox.bukkit.context.CommandContext;
import com.github.chevyself.starbox.bukkit.messages.BukkitMessagesProvider;
import com.github.chevyself.starbox.bukkit.messages.GenericBukkitMessagesProvider;
import com.github.chevyself.starbox.registry.ProvidersRegistry;
import lombok.Getter;
import lombok.NonNull;
import me.googas.starbox.commands.ComponentBuilderCommands;
import me.googas.starbox.commands.ItemBuilderCommands;
import me.googas.starbox.commands.ScoreboardCommands;
import me.googas.starbox.commands.providers.BukkitLineProvider;
import me.googas.starbox.commands.providers.BungeeChatColorProvider;
import me.googas.starbox.commands.providers.ChannelProvider;
import me.googas.starbox.commands.providers.ClickEventActionProvider;
import me.googas.starbox.commands.providers.EnchantmentProvider;
import me.googas.starbox.commands.providers.FormatRetentionProvider;
import me.googas.starbox.commands.providers.HoverEventActionProvider;
import me.googas.starbox.compatibilities.Compatibility;
import me.googas.starbox.compatibilities.CompatibilityManager;
import me.googas.starbox.compatibilities.viaversion.ViaVersionCompatibility;
import me.googas.starbox.modules.ModuleRegistry;
import me.googas.starbox.modules.language.LanguageModule;
import me.googas.starbox.modules.placeholders.Placeholder;
import me.googas.starbox.modules.placeholders.PlaceholderModule;
import me.googas.starbox.modules.ui.UIModule;
import org.bukkit.plugin.java.JavaPlugin;

/** Main class of the Starbox Bukkit plugin. */
public class StarboxPlugin extends JavaPlugin {

  @NonNull @Getter private final ModuleRegistry modules = new ModuleRegistry(this);

  @NonNull @Getter
  private final CompatibilityManager compatibilities =
      new CompatibilityManager().addAll(new ViaVersionCompatibility());

  @NonNull private final BukkitMessagesProvider messages = new GenericBukkitMessagesProvider();

  @NonNull
  private final ProvidersRegistry<CommandContext> providers =
      new ProvidersRegistry<>(messages)
          .addProviders(
              new BukkitLineProvider(),
              new BungeeChatColorProvider(),
              new ChannelProvider(),
              new ClickEventActionProvider(),
              new EnchantmentProvider(),
              new FormatRetentionProvider(),
              new HoverEventActionProvider());

  @NonNull
  private final CommandManager<CommandContext, BukkitCommand> manager = new CommandManagerBuilder<>(new BukkitAdapter(this, true))
          .setProvidersRegistry(providers)
          .setMessagesProvider(messages)
          .build();

  @Override
  public void onEnable() {
    Starbox.setInstance(this);
    // Engage modules
    modules.engage(
        new UIModule(),
        new LanguageModule()
            .registerAll(this, BukkitYamlLanguage.of(this, "lang/sample", "lang/en")),
        new PlaceholderModule().registerAll(this, new Placeholder.Name(), new Placeholder.Ping()));
    // Command registration
    manager.parseAndRegisterAll(
            new ComponentBuilderCommands(),
            new ItemBuilderCommands(),
            new ScoreboardCommands().start(this)
    );
    // Check compatibilities
    compatibilities.check().getCompatibilities().stream()
        .filter(Compatibility::isEnabled)
        .forEach(
            compatibility -> {
              this.modules.engage(compatibility.getModules(this));
              this.manager.registerAll(compatibility.getCommands());
              this.manager.getProvidersRegistry().addProviders(compatibility.getProviders());
            });
    super.onEnable();
  }

  @Override
  public void onDisable() {
    modules.disengageAll();
    manager.close();
    super.onDisable();
  }
}
