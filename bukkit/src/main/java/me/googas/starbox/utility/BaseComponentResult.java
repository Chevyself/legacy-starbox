package me.googas.starbox.utility;

import com.github.chevyself.starbox.result.Result;
import lombok.NonNull;
import net.md_5.bungee.api.chat.BaseComponent;

public class BaseComponentResult implements Result {
  @NonNull private final BaseComponent[] components;

  public BaseComponentResult(@NonNull BaseComponent[] components) {
    this.components = components;
  }
}
