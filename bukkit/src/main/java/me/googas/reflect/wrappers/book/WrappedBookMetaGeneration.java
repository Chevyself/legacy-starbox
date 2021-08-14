package me.googas.reflect.wrappers.book;

import lombok.NonNull;
import lombok.experimental.Delegate;
import me.googas.reflect.APIVersion;
import me.googas.reflect.StarboxWrapper;
import org.bukkit.inventory.meta.BookMeta;

@APIVersion(since = 9)
public class WrappedBookMetaGeneration extends StarboxWrapper<BookMeta.Generation> {

  /**
   * Create the wrapper.
   *
   * @param reference the reference of the wrapper
   */
  public WrappedBookMetaGeneration(@NonNull BookMeta.Generation reference) {
    super(reference);
  }

  @NonNull
  @Delegate
  public BookMeta.Generation getGeneration() {
    return this.get().orElseThrow(NullPointerException::new);
  }

  @Override
  public @NonNull WrappedBookMetaGeneration set(@NonNull BookMeta.Generation object) {
    return (WrappedBookMetaGeneration) super.set(object);
  }
}
