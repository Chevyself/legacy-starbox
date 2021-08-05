package me.googas.reflect.wrappers.block;

import lombok.NonNull;
import me.googas.reflect.APIVersion;
import org.bukkit.Material;

@APIVersion(14)
@Deprecated
public class WrappedBlockDataMeta {

  private WrappedBlockData data;

  public WrappedBlockDataMeta(WrappedBlockData data) {
    this.data = data;
  }

  public WrappedBlockDataMeta() {
    this(null);
  }

  public boolean hasBlockData() {
    return this.data != null;
  }

  @NonNull
  public WrappedBlockData getBlockData(@NonNull Material material) {
    return this.data;
  }

  public void setBlockData(@NonNull WrappedBlockData data) {
    this.data = data;
  }
}
