package me.googas.starbox.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.github.chevyself.starbox.annotations.Command;
import com.github.chevyself.starbox.annotations.Free;
import com.github.chevyself.starbox.annotations.Required;
import com.github.chevyself.starbox.arguments.ArgumentBehaviour;
import com.github.chevyself.starbox.common.CommandPermission;
import com.github.chevyself.starbox.common.ComponentResult;
import com.github.chevyself.starbox.result.Result;
import lombok.NonNull;
import com.github.chevyself.starbox.bukkit.utils.BukkitUtils;
import me.googas.io.StarboxFile;
import me.googas.reflect.wrappers.WrappedClass;
import me.googas.reflect.wrappers.WrappedConstructor;
import me.googas.reflect.wrappers.chat.Component;
import me.googas.reflect.wrappers.chat.WrappedHoverEvent;
import me.googas.reflect.wrappers.chat.WrappedText;
import me.googas.starbox.BukkitLine;
import me.googas.starbox.Starbox;
import me.googas.starbox.StarboxBukkitFiles;
import me.googas.starbox.utility.Versions;
import me.googas.starbox.utility.items.ItemBuilder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Commands to create {@link BaseComponent} using {@link ComponentBuilder}. Javadoc's warnings are
 * suppressed as commands already have a description and usage.
 */
@CommandPermission("starbox.component-builder")
@Command(
    aliases = "componentBuilder",
    description = "Helps with the construction of chat components",
    usage = "componentBuilder <subcommand>"
)
public class ComponentBuilderCommands {

  @NonNull
  private static final WrappedClass<HoverEvent> HOVER_EVENT = WrappedClass.of(HoverEvent.class);

  @NonNull
  private static final WrappedConstructor<HoverEvent> HOVER_EVENT_CONSTRUCTOR =
      ComponentBuilderCommands.HOVER_EVENT.getConstructor(
          HoverEvent.Action.class, BaseComponent[].class);

  @NonNull
  private static final ItemStack colorBook =
      StarboxBukkitFiles.Contexts.JSON
          .read(StarboxBukkitFiles.Resources.COLORS, ItemBuilder.class)
          .handle(Starbox::severe)
          .provide()
          .orElseGet(ItemBuilder::new)
          .build();

  @NonNull private final Map<UUID, Component> builders = new HashMap<>();

  @CommandPermission("starbox.component-builder")
  @Command(
      aliases = {"reset", "clear"},
      description = "Reset the builder")
  public Result reset(Player player) {
    builders.remove(player.getUniqueId());
    return BukkitLine.localized(player, "component-builder.reset").asResult();
  }

  @CommandPermission("starbox.component-builder")
  @Command(
      aliases = "build",
      description = "Build an see your current component")
  public Result build(Player player) {
    return new ComponentResult(this.getBuilder(player).build());
  }

  @CommandPermission("starbox.component-builder")
  @Command(
      aliases = "space",
      description = "Appends spaces in the builder")
  public Result space(
      Player player,
      @Free(
              name = "number",
              description = "The number of spaces to add",
              suggestions = {"1", "2", "3"})
          int spaces) {
    if (spaces < 1) {
      return BukkitLine.localized(player, "component-builder.spaces.less-than-1").asResult();
    } else {
      Component builder = this.getBuilder(player);
      for (int i = 0; i < spaces; i++) {
        builder.append(" ");
      }
      return BukkitLine.localized(player, "component-builder.spaces.success").asResult();
    }
  }

  @CommandPermission("starbox.component-builder")
  @Command(
      aliases = "see",
      description = "See how a text component would look like using color codes")
  public Result see(
      Player player,
      @Required(name = "text", description = "The text to test", behaviour = ArgumentBehaviour.CONTINUOUS) String text) {
    return Result.of(BukkitUtils.format(text));
  }

  @CommandPermission("starbox.component-builder")
  @Command(
      aliases = "text",
      description = "Append some text to the component and decide the format retention")
  public Result text(
      Player player,
      @Required(name = "retention", description = "How should the text retain previous formats")
          ComponentBuilder.FormatRetention retention,
      @Required(name = "text", description = "The text to append", behaviour = ArgumentBehaviour.CONTINUOUS) String text) {
    this.getBuilder(player).append(text, retention);
    return BukkitLine.localized(player, "component-builder.append").format(text).asResult();
  }

  @CommandPermission("starbox.component-builder")
  @Command(
      aliases = "append",
      description = "Append some text to the component")
  public Result append(
      Player player,
      @Required(name = "text", description = "The text to append", behaviour = ArgumentBehaviour.CONTINUOUS) String text) {
    this.getBuilder(player).append(text);
    return BukkitLine.localized(player, "component-builder.append").format(text).asResult();
  }

    @CommandPermission("starbox.component-builder")
  @Command(
      aliases = "color",
      description = "Set the color for a current part")
  public Result color(
      Player player, @Required(name = "color", description = "The color to set") ChatColor color) {
    this.getBuilder(player).color(color);
    return BukkitLine.localized(player, "component-builder.color").asResult();
  }

  @CommandPermission("starbox.component-builder.colors")
  @Command(
      aliases = "colors",
      description = "Get the color book")
  public Result colors(Player player) {
    player.getInventory().addItem(ComponentBuilderCommands.colorBook);
    return BukkitLine.localized(player, "component-builder.colors").asResult();
  }

  @CommandPermission("starbox.component-builder")
  @Command(
      aliases = "click",
      description = "Adds a click event to the current part")
  public Result click(
      Player player,
      @Required(name = "action", description = "The action of the event") ClickEvent.Action action,
      @Required(name = "value", description = "The value of the action for the event", behaviour = ArgumentBehaviour.CONTINUOUS)
          String value) {
    this.getBuilder(player).event(new ClickEvent(action, value));
    return BukkitLine.localized(player, "component-builder.event").asResult();
  }

    @CommandPermission("starbox.component-builder")
  @Command(
      aliases = "hover",
      description = "Adds a hover event to the current part")
  public Result hover(
      Player player,
      @Required(name = "name", description = "The name of the component to import to set as value")
          String name) {
    HoverEvent.Action action = HoverEvent.Action.SHOW_TEXT;
    StarboxFile file =
        new StarboxFile(StarboxBukkitFiles.EXPORTS, name.endsWith(".json") ? name : name + ".json");
    BaseComponent[] components = this.importComponents(file).orElseGet(() -> new BaseComponent[0]);
    if (file.exists()) {
      if (Versions.BUKKIT < 16) {
        this.getBuilder(player).event(WrappedHoverEvent.construct(action, components));
      } else {
        this.getBuilder(player)
            .event(WrappedHoverEvent.construct(action, new WrappedText(components)));
      }
      return BukkitLine.localized(player, "component-builder.event").asResult();
    }
    return BukkitLine.localized(player, "component-builder.import.no-file").format(file).asResult();
  }

    @CommandPermission("starbox.component-builder")
  @Command(
      aliases = "obfuscate",
      description = "Obfuscates the current part")
  public Result obfuscate(Player player) {
    this.getBuilder(player).obfuscated(true);
    return BukkitLine.localized(player, "component-builder.modify").asResult();
  }

  @CommandPermission("starbox.component-builder")
  @Command(
      aliases = "strikethrough",
      description = "Strikethrough the current part")
  public Result strikethrough(Player player) {
    this.getBuilder(player).strikethrough(true);
    return BukkitLine.localized(player, "component-builder.modify").asResult();
  }

    @CommandPermission("starbox.component-builder")
  @Command(
      aliases = "italic",
      description = "Italic the current part")
  public Result italic(Player player) {
    this.getBuilder(player).italic(true);
    return BukkitLine.localized(player, "component-builder.modify").asResult();
  }

    @CommandPermission("starbox.component-builder")
  @Command(
      aliases = "bold",
      description = "Bold the current part")
  public Result bold(Player player) {
    this.getBuilder(player).bold(true);
    return BukkitLine.localized(player, "component-builder.modify").asResult();
  }

    @CommandPermission("starbox.component-builder")
  @Command(
      aliases = "underline",
      description = "Underline the current part")
  public Result underline(Player player) {
    this.getBuilder(player).underline(true);
    return BukkitLine.localized(player, "component-builder.modify").asResult();
  }

    @CommandPermission("starbox.component-builder")
  @Command(
      aliases = "export",
      description = "Export your current builder")
  public Result export(
      Player player,
      @Required(name = "name", description = "The name of the exported file") String name) {
    StarboxFile file =
        new StarboxFile(StarboxBukkitFiles.EXPORTS, name.endsWith(".json") ? name : name + ".json");
    boolean exported =
        file.write(StarboxBukkitFiles.Contexts.JSON, this.getBuilder(player).build())
            .handle(Starbox::severe)
            .provide()
            .orElse(false);
    if (exported) {
      return BukkitLine.localized(player, "component-builder.export.success")
          .format(file)
          .asResult();
    } else {
      return BukkitLine.localized(player, "component-builder.export.not").asResult();
    }
  }

    @CommandPermission("starbox.component-builder")
  @Command(
      aliases = "import",
      description = "Import a builder")
  public Result importBuilder(
      Player player,
      @Required(name = "name", description = "The name of the file to import") String name) {
    StarboxFile file =
        new StarboxFile(StarboxBukkitFiles.EXPORTS, name.endsWith(".json") ? name : name + ".json");
    if (file.exists()) {
      this.builders.put(player.getUniqueId(), this.importBuilder(file));
      return BukkitLine.localized(player, "component-builder.import.success")
          .format(file)
          .asResult();
    } else {
      return BukkitLine.localized(player, "component-builder.import.no-file")
          .format(file)
          .asResult();
    }
  }

  @NonNull
  private Component importBuilder(@NonNull StarboxFile file) {
    return this.importComponents(file).map(Component::new).orElseGet(Component::new);
  }

  private @NonNull Optional<BaseComponent[]> importComponents(@NonNull StarboxFile file) {
    return file.read(StarboxBukkitFiles.Contexts.JSON, BaseComponent[].class)
        .handle(Starbox::severe)
        .provide();
  }

  @NonNull
  private Component getBuilder(@NonNull Player player) {
    return this.builders.computeIfAbsent(player.getUniqueId(), uuid -> new Component());
  }
}
