package me.googas.starbox.utility.items.meta;

import com.github.chevyself.starbox.common.Components;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import me.googas.reflect.APIVersion;
import me.googas.reflect.wrappers.WrappedClass;
import me.googas.reflect.wrappers.WrappedField;
import me.googas.reflect.wrappers.chat.WrappedChatComponent;
import me.googas.reflect.wrappers.inventory.WrappedBookMetaGeneration;
import me.googas.starbox.Starbox;
import me.googas.starbox.StarboxBukkitFiles;
import me.googas.starbox.Strings;
import me.googas.starbox.utility.Versions;
import me.googas.starbox.utility.items.ItemBuilder;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

/** Builds {@link BookMeta}. */
public class BookMetaBuilder extends ItemMetaBuilder {

  @NonNull
  private static final WrappedClass<?> CRAFT_META_BOOK =
      WrappedClass.forName("org.bukkit.craftbukkit." + Versions.NMS + ".inventory.CraftMetaBook");

  @NonNull
  private static final WrappedField<?> PAGES =
      BookMetaBuilder.CRAFT_META_BOOK.getDeclaredField("pages");

  @NonNull @Getter private List<BaseComponent[]> pages = new ArrayList<>();
  @Getter private String title = null;
  @Getter private String author = null;
  @Getter private WrappedBookMetaGeneration wrappedGeneration = null;

  /**
   * Create the builder.
   *
   * @param itemBuilder the item builder to which this meta will be built
   */
  public BookMetaBuilder(@NonNull ItemBuilder itemBuilder) {
    super(itemBuilder);
  }

  /** Create the builder. */
  public BookMetaBuilder() {
    super();
  }

  /**
   * Create the builder.
   *
   * @param other another meta builder to copy its values
   */
  public BookMetaBuilder(@NonNull ItemMetaBuilder other) {
    super(other);
  }

  /**
   * Add a page to the book.
   *
   * @param page the page to add.
   * @return this same instance
   */
  public BookMetaBuilder add(@NonNull BaseComponent[] page) {
    this.pages.add(page);
    return this;
  }

  /**
   * Add many page to the book.
   *
   * @param pages the collection of pages to add.
   * @return this same instance
   */
  public BookMetaBuilder addAll(@NonNull Collection<? extends BaseComponent[]> pages) {
    this.pages.addAll(pages);
    return this;
  }

  /**
   * Add a page to the book. The current page will be divided if it exceeds the '798' character
   * limit (Taken from <a href="https://minecraft.fandom.com/wiki/Book_and_Quill">Wiki</a>)
   *
   * @param page the page to add
   * @return this same instance
   */
  @NonNull
  public BookMetaBuilder add(@NonNull String page) {
    Strings.divide(page, 798).forEach(string -> this.pages.add(Components.getComponent(page)));
    return this;
  }

  /**
   * Add a collection of pages to the book. This will add each string using {@link #add(String)}
   *
   * @param pages the pages to add
   * @return this same instance
   */
  @NonNull
  public BookMetaBuilder add(@NonNull Collection<String> pages) {
    pages.forEach(this::add);
    return this;
  }

  /**
   * Add an array of pages to the book. This will add each string using {@link #add(String)}
   *
   * @param pages the pages to add
   * @return this same instance
   */
  @NonNull
  public BookMetaBuilder add(@NonNull String... pages) {
    for (String page : pages) {
      this.add(page);
    }
    return this;
  }

  /**
   * Set the title of the book.
   *
   * @param title the new title of the book
   * @return this same instance
   */
  @NonNull
  public BookMetaBuilder setTitle(String title) {
    this.title = title;
    return this;
  }

  /**
   * Set the author.
   *
   * @param author the author of the book
   * @return this same instance
   */
  @NonNull
  public BookMetaBuilder setAuthor(String author) {
    this.author = author;
    return this;
  }

  /**
   * Set the pages of the book.
   *
   * @param pages the new pages
   * @return this same instance
   */
  @NonNull
  public BookMetaBuilder setPages(List<BaseComponent[]> pages) {
    this.pages = pages;
    return this;
  }

  /**
   * Set the generation of the book.
   *
   * @param wrappedGeneration the generation of the book
   * @return this same instance
   */
  @NonNull
  @APIVersion(since = 9)
  public BookMetaBuilder setWrappedGeneration(WrappedBookMetaGeneration wrappedGeneration) {
    this.wrappedGeneration = wrappedGeneration;
    return this;
  }

  @Override
  public BookMeta build(@NonNull ItemStack stack) {
    ItemMeta itemMeta = super.build(stack);
    if (itemMeta instanceof BookMeta) {
      BookMeta bookMeta = (BookMeta) itemMeta;
      if (title != null) bookMeta.setTitle(this.title);
      if (author != null) bookMeta.setAuthor(this.author);
      if (Versions.BUKKIT >= 9 && wrappedGeneration != null) {
        bookMeta.setGeneration(wrappedGeneration.getGeneration());
      }
      if (Versions.BUKKIT > 11) {
        bookMeta.spigot().setPages(this.pages);
      } else {
        String json = StarboxBukkitFiles.Contexts.JSON.getGson().toJson(this.pages).trim();
        BookMetaBuilder.PAGES
            .set(
                bookMeta,
                WrappedChatComponent.Serializer.getGson()
                    .fromJson(json, BookMetaBuilder.PAGES.getField().getGenericType()))
            .handle(Starbox::severe)
            .run();
      }
      return bookMeta;
    }
    return null;
  }
}
