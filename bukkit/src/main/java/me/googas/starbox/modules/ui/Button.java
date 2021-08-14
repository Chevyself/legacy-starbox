package me.googas.starbox.modules.ui;

import lombok.NonNull;
import me.googas.starbox.utility.items.ItemBuilder;
import org.bukkit.inventory.ItemStack;

/**
 * This interface must be implemented to an object that represents a button inside the {@link UI}.
 *
 * <p>The button has two basic elements:
 *
 * <p>The listener {@link #getListener()} which is called when a player clicks on the button and
 * makes it do an action The item {@link #getItem()} which is what is shown to the player in the
 * inventory
 */
public interface Button {

  /**
   * Get the listener of the button.
   *
   * @return the listener of the button
   */
  @NonNull
  ButtonListener getListener();

  /**
   * Get the item that represents the button.
   *
   * @return the item
   */
  @NonNull
  ItemStack getItem();

  /**
   * Start a new builder.
   *
   * @return the new builder
   */
  @NonNull
  static ButtonBuilder builder() {
    return new ButtonBuilder();
  }

  /**
   * Start a new builder with an initial item.
   *
   * @param item the initial item
   * @return the new builder
   */
  @NonNull
  static ButtonBuilder builder(@NonNull ItemBuilder item) {
    return new ButtonBuilder(item);
  }
}
