package me.googas.starbox.jda.responsive;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

/** The controller to use the responsive messages. */
public interface ResponsiveMessageController {

  /**
   * Listen for the reaction being added to a message.
   *
   * @param event the event of a reaction being added to a message
   */
  default void onMessageReactionAdd(MessageReactionAddEvent event) {
    User user = event.getUser();
    if (user != null && (!user.isBot() || user.isBot() && this.acceptBots())) {
      this.getResponsiveMessage(
              event.isFromGuild() ? event.getGuild() : null, event.getMessageIdLong())
          .ifPresent(
              message -> {
                AtomicBoolean removed = new AtomicBoolean();
                message
                    .getReactions(this.getUnicode(event.getReaction().getEmoji()))
                    .forEach(
                        reaction -> {
                          if (reaction.onReaction(event) && !removed.get()) {
                            event.getReaction().removeReaction(user).queue();
                            removed.set(true);
                          }
                        });
              });
    }
  }

  /**
   * Get the responsive message matching the id.
   *
   * @param guild the guild to get the responsive message
   * @param messageId the id to match
   * @return the message if found else null
   */
  @NonNull
  Optional<? extends ResponsiveMessage> getResponsiveMessage(Guild guild, long messageId);

  /**
   * Get the unicode or the name of the emote from a reaction event.
   *
   * @param emote the emote of the reaction that was added
   * @return the unicode
   */
  @NonNull
  default String getUnicode(@NonNull EmojiUnion emote) {
    if (emote.getType() == EmojiUnion.Type.CUSTOM) {
      return emote.getName();
    } else {
      return emote.toString().replace("RE:", "");
    }
  }

  /**
   * Remove the message from certain guild. This will make that the controller will not listen to it
   *
   * @param guild the guild where the message is from
   * @param message the message to remove
   */
  void removeMessage(Guild guild, @NonNull ResponsiveMessage message);

  /**
   * Whether bots can use this responsive message.
   *
   * @return true if they can
   */
  boolean acceptBots();
}
