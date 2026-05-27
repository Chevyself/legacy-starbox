package me.googas.starbox.utility.items.meta;

import java.util.HashMap;
import lombok.Getter;
import lombok.NonNull;
import com.github.chevyself.starbox.bukkit.utils.BukkitUtils;
import me.googas.reflect.APIVersion;
import me.googas.reflect.wrappers.WrappedClass;
import me.googas.reflect.wrappers.WrappedMethod;
import me.googas.reflect.wrappers.attributes.WrappedAttributes;
import me.googas.starbox.Strings;
import me.googas.starbox.builders.MapBuilder;
import me.googas.starbox.builders.SuppliedBuilder;
import me.googas.starbox.utility.Materials;
import me.googas.starbox.utility.Versions;
import me.googas.starbox.utility.items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** Builds {@link ItemMeta}. */
public class ItemMetaBuilder implements SuppliedBuilder<ItemStack, ItemMeta> {

  @NonNull private static final WrappedClass<ItemMeta> ITEM_META = WrappedClass.of(ItemMeta.class);

  @NonNull
  @APIVersion(since = 8, max = 15)
  private static final WrappedClass<?> ITEM_META_SPIGOT =
      WrappedClass.forName("org.bukkit.inventory.meta.ItemMeta.Spigot");

  @NonNull
  @APIVersion(since = 8, max = 15)
  private static final WrappedMethod<?> ITEM_META_SPIGOT_METHOD =
      ItemMetaBuilder.ITEM_META.getMethod("spigot");

  @NonNull
  @APIVersion(since = 8, max = 10)
  private static final WrappedMethod<?> SPIGOT_SET_UNBREAKABLE =
      ItemMetaBuilder.ITEM_META_SPIGOT.getMethod("setUnbreakable", boolean.class);

  @Getter private EnchantmentsBuilder enchantments;
  @Getter private String name;
  @Getter private String lore;
  @Getter private WrappedAttributes attributes;
  @Getter private boolean unbreakable;

  /**
   * Create the builder.
   *
   * @param enchantments the enchantments to add in the item
   * @param name the initial name of the item
   * @param lore the lore of the item
   * @param attributes attributes of the item
   * @param unbreakable whether the item must be unbreakable
   */
  protected ItemMetaBuilder(
      EnchantmentsBuilder enchantments,
      String name,
      String lore,
      WrappedAttributes attributes,
      boolean unbreakable) {
    this.enchantments = enchantments;
    this.name = name;
    this.lore = lore;
    this.attributes = attributes;
    this.unbreakable = unbreakable;
  }

  /**
   * Create the builder.
   *
   * @param other other builder instance to copy its values
   */
  public ItemMetaBuilder(@NonNull ItemMetaBuilder other) {
    this(
        other.getEnchantments(),
        other.getName(),
        other.getLore(),
        other.getAttributes(),
        other.isUnbreakable());
  }

  /**
   * Create the builder.
   *
   * @param builder the item builder to which this meta will be built
   */
  public ItemMetaBuilder(@NonNull ItemBuilder builder) {
    this(builder.getMetaBuilder());
  }

  /** Create the builder. */
  public ItemMetaBuilder() {
    this(null, null, null, null, false);
  }

  /**
   * Set the name of the item.
   *
   * @param name the new name
   * @return this same instance
   */
  @NonNull
  public ItemMetaBuilder setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * Set the lore of the item.
   *
   * @param lore the new lore
   * @return this same instance
   */
  @NonNull
  public ItemMetaBuilder setLore(String lore) {
    this.lore = lore;
    return this;
  }

  /**
   * Get an instance given a material.
   *
   * @param material the material to get the meta builder from
   * @return the meta builder
   */
  @NonNull
  public static ItemMetaBuilder getMeta(@NonNull Material material) {
    return ItemMetaBuilder.getMeta(material, null);
  }

  /**
   * Set whether this item is unbreakable.
   *
   * @param unbreakable the new value
   * @return this same instance
   */
  @NonNull
  public ItemMetaBuilder setUnbreakable(boolean unbreakable) {
    this.unbreakable = unbreakable;
    return this;
  }

  /**
   * Get an instance given a material and copy the values of another builder.
   *
   * @param material the material to get the meta builder from
   * @param other another builder to copy its values
   * @return the meta builder
   */
  @NonNull
  public static ItemMetaBuilder getMeta(@NonNull Material material, ItemMetaBuilder other) {
    if (material == Materials.getWritableBook() || material == Material.WRITTEN_BOOK) {
      return other == null ? new BookMetaBuilder() : new BookMetaBuilder(other);
    } else if (Materials.isBanner(material)) {
      return other == null ? new BannerMetaBuilder() : new BannerMetaBuilder(other);
    } else if (Materials.isSkull(material)) {
      return other == null ? new SkullMetaBuilder() : new SkullMetaBuilder(other);
    } else if (material == Material.ENCHANTED_BOOK) {
      return other == null
          ? new EnchantmentStorageMetaBuilder()
          : new EnchantmentStorageMetaBuilder(other);
    } else if (material == Materials.getFireworkStar()) {
      return other == null ? new FireworkEffectMetaBuilder() : new FireworkEffectMetaBuilder(other);
    } else if (material == Materials.getFireworkRocket()) {
      return other == null ? new FireworkMetaBuilder() : new FireworkEffectMetaBuilder(other);
    }
    return other == null ? new ItemMetaBuilder() : new ItemMetaBuilder(other);
  }

  /**
   * Set the attributes of this item.
   *
   * @param attributes the new attributes of this item
   * @return this same instance
   */
  @NonNull
  @APIVersion(since = 12)
  public ItemMetaBuilder setAttributes(WrappedAttributes attributes) {
    this.attributes = attributes;
    return this;
  }

  /**
   * Add an enchantment to the item.
   *
   * @param enchantment the enchantment to add
   * @param level the level of the enchantment
   * @return this same instance
   */
  @NonNull
  public ItemMetaBuilder addEnchantment(@NonNull Enchantment enchantment, int level) {
    if (this.enchantments == null) {
      this.enchantments = new EnchantmentsBuilder();
    }
    this.enchantments.put(enchantment, level);
    return this;
  }

  @Override
  public ItemMeta build(@NonNull ItemStack stack) {
    ItemMeta meta = stack.getItemMeta();
    if (meta != null) {
      if (this.name != null) meta.setDisplayName(BukkitUtils.format(this.name));
      if (this.lore != null) meta.setLore(Strings.divide(BukkitUtils.format(this.lore), 64));
      if (this.attributes != null && Versions.BUKKIT >= 12) {
        meta.setAttributeModifiers(this.attributes.build());
      }
      if (Versions.BUKKIT <= 10) {
        ItemMetaBuilder.SPIGOT_SET_UNBREAKABLE
            .prepare(
                ItemMetaBuilder.ITEM_META_SPIGOT_METHOD.prepare(meta).provide().orElse(null),
                this.unbreakable)
            .run();
      } else {
        meta.setUnbreakable(this.unbreakable);
      }
      if (this.enchantments != null) {
        this.enchantments
            .build()
            .forEach(((enchantment, integer) -> meta.addEnchant(enchantment, integer, true)));
      }
    }
    return meta;
  }

  /**
   * Builds a {@link java.util.Map} containing {@link Enchantment} and its level to then save it
   * into the {@link ItemMeta}.
   */
  public static class EnchantmentsBuilder extends MapBuilder<Enchantment, Integer> {

    /** Start the builder. */
    public EnchantmentsBuilder() {
      super(new HashMap<>());
    }
  }
}
