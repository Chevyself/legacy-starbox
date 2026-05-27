package me.googas.starbox.commands;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.chevyself.starbox.annotations.Command;
import com.github.chevyself.starbox.annotations.Required;
import com.github.chevyself.starbox.arguments.ArgumentBehaviour;
import com.github.chevyself.starbox.common.CommandPermission;
import com.github.chevyself.starbox.result.Result;
import lombok.NonNull;
import com.github.chevyself.starbox.bukkit.utils.BukkitUtils;
import me.googas.io.StarboxFile;
import me.googas.reflect.wrappers.inventory.WrappedEnchantment;
import me.googas.starbox.BukkitLine;
import me.googas.starbox.Starbox;
import me.googas.starbox.StarboxBukkitFiles;
import me.googas.starbox.utility.items.ItemBuilder;
import me.googas.starbox.utility.items.meta.ItemMetaBuilder;
import me.googas.starbox.utility.items.meta.SkullMetaBuilder;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

/**
 * Commands to create items using {@link ItemBuilder}. Javadoc's warnings are suppressed as commands
 * already have a description and usage.
 */
@SuppressWarnings("JavaDoc")
public class ItemBuilderCommands {

  @NonNull private final Map<UUID, ItemBuilder> builders = new HashMap<>();

  @CommandPermission("starbox.item-builder")
  @Command(aliases = "build", description = "Build the item")
  public Result build(Player player) {
    player.getInventory().addItem(this.getBuilder(player).build());
    return BukkitLine.localized(player, "item-builder.build").asResult();
  }

  @CommandPermission("starbox.item-builder")
  @Command(
      aliases = "material",
      description = "Change the material of the item")
  public Result material(
      Player player,
      @Required(name = "material", description = "The new material of the item")
          Material material) {
    this.getBuilder(player).setMaterial(material);
    return BukkitLine.localized(player, "item-builder.material")
        .format(material.toString().toLowerCase())
        .asResult();
  }

  @CommandPermission("starbox.item-builder")
  @Command(
      aliases = "amount",
      description = "Change the amount of the item")
  public Result amount(
      Player player,
      @Required(name = "amount", description = "The new amount of the item") int amount) {
    this.getBuilder(player).setAmount(amount);
    return BukkitLine.localized(player, "item-builder.amount").format(amount).asResult();
  }

  @CommandPermission("starbox.item-builder")
  @Command(
      aliases = "name",
      description = "Set the name of the item")
  public Result name(
      Player player,
      @Required(name = "name", description = "The new name of the item", behaviour = ArgumentBehaviour.CONTINUOUS) String name) {
    this.getBuilder(player).withMeta(meta -> meta.setName(name));
    return BukkitLine.localized(player, "item-builder.name")
        .format(BukkitUtils.format(name))
        .asResult();
  }

    @CommandPermission("starbox.item-builder")
  @Command(
      aliases = "lore",
      description = "Set the lore of the item")
  public Result lore(
      Player player,
      @Required(name = "lore", description = "The new lore of the item", behaviour = ArgumentBehaviour.CONTINUOUS) String lore) {
    this.getBuilder(player).withMeta(meta -> meta.setLore(lore));
    return BukkitLine.localized(player, "item-builder.lore")
        .format(BukkitUtils.format(lore))
        .asResult();
  }

    @CommandPermission("starbox.item-builder")
  @Command(
      aliases = "unbreakable",
      description = "Set whether the item is unbreakable")
  public Result unbreakable(
      Player player,
      @Required(name = "unbreakable", description = "Whether the item has to be unbreakable")
          boolean unbreakable) {
    this.getBuilder(player).withMeta(meta -> meta.setUnbreakable(unbreakable));
    return BukkitLine.localized(player, "item-builder.unbreakable").format(unbreakable).asResult();
  }

    @CommandPermission("starbox.item-builder")
  @Command(
      aliases = "owner",
      description = "Set the owner of the skull")
  public Result owner(
      Player player,
      @Required(name = "owner", description = "The owner of the skull") OfflinePlayer owner) {
    ItemBuilder builder = this.getBuilder(player);
    ItemMetaBuilder metaBuilder = builder.getMetaBuilder();
    if (metaBuilder instanceof SkullMetaBuilder) {
      ((SkullMetaBuilder) metaBuilder).setOwner(owner);
      String name = owner.getName() == null ? owner.getUniqueId().toString() : owner.getName();
      return BukkitLine.localized(player, "item-builder.owner").format(name).asResult();
    }
    return BukkitLine.localized(player, "item-builder.not-skull").asResult();
  }

    @CommandPermission("starbox.item-builder")
  @Command(
      aliases = "skin",
      description = "Set the skin of the skull")
  public Result skin(
      Player player,
      @Required(name = "skin", description = "The skin in its Base64") String base64) {
    ItemBuilder builder = this.getBuilder(player);
    ItemMetaBuilder metaBuilder = builder.getMetaBuilder();
    if (metaBuilder instanceof SkullMetaBuilder) {
      ((SkullMetaBuilder) metaBuilder).setSkin(base64);
      return BukkitLine.localized(player, "item-builder.skin").format(base64).asResult();
    }
    return BukkitLine.localized(player, "item-builder.not-skull").asResult();
  }

    @CommandPermission("starbox.item-builder")
  @Command(
      aliases = "reset",
      description = "Reset your item builder"
      )
  public Result reset(Player player) {
    this.builders.remove(player.getUniqueId());
    return BukkitLine.localized(player, "item-builder.reset").asResult();
  }

    @CommandPermission("starbox.item-builder")
  @Command(
      aliases = "export",
      description = "Export your current builder")
  public Result export(
      Player player,
      @Required(name = "name", description = "The name of the exported file") String name) {
    StarboxFile file =
        new StarboxFile(StarboxBukkitFiles.EXPORTS, name.endsWith(".json") ? name : name + ".json");
    boolean exported =
        file.write(StarboxBukkitFiles.Contexts.JSON, this.getBuilder(player))
            .provide()
            .orElse(false);
    if (exported) {
      return BukkitLine.localized(player, "item-builder.export.success").format(file).asResult();
    } else {
      return BukkitLine.localized(player, "item-builder.export.not").asResult();
    }
  }

    @CommandPermission("starbox.item-builder")
  @Command(
      aliases = "enchant",
      description = "Enchant the item")
  public Result enchant(
      Player player,
      @Required(name = "enchantment", description = "The enchantment") Enchantment enchantment,
      @Required(name = "value", description = "The value of the enchantment") int value) {
    if (value > 0) {
      this.getBuilder(player).withMeta(meta -> meta.getEnchantments().put(enchantment, value));
      return BukkitLine.localized(player, "item-builder.enchant.done")
          .format(WrappedEnchantment.of(enchantment).getName().toLowerCase(), value)
          .asResult();
    } else {
      this.getBuilder(player).withMeta(meta -> meta.getEnchantments().remove(enchantment));
      return BukkitLine.localized(player, "item-builder.enchant.removed")
          .format(enchantment)
          .asResult();
    }
  }

    @CommandPermission("starbox.item-builder")
  @Command(
      aliases = "import",
      description = "Import a builder")
  public Result importBuilder(
      Player player,
      @Required(name = "name", description = "The name of the file to import") String name) {
    StarboxFile file =
        new StarboxFile(StarboxBukkitFiles.EXPORTS, name.endsWith(".json") ? name : name + ".json");
    if (file.exists()) {
      AtomicBoolean successful = new AtomicBoolean(true);
      ItemBuilder builder =
          file.read(StarboxBukkitFiles.Contexts.JSON, ItemBuilder.class)
              .handle(Starbox::severe)
              .provide()
              .orElseGet(
                  () -> {
                    successful.set(false);
                    return new ItemBuilder();
                  });
      this.builders.put(player.getUniqueId(), builder);
      if (successful.get()) {
        return BukkitLine.localized(player, "item-builder.import.success").format(file).asResult();
      } else {
        return BukkitLine.localized(player, "item-builder.import.not").format(file).asResult();
      }
    }
    return BukkitLine.localized(player, "item-builder.import.no-file").format(file).asResult();
  }

  @NonNull
  private ItemBuilder getBuilder(@NonNull Player player) {
    return this.builders.computeIfAbsent(player.getUniqueId(), uuid -> new ItemBuilder());
  }
}
