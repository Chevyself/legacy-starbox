package me.googas.starbox.jda.responsive;

import java.util.Collection;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;

/** A responsive message is created for the user to react to the message with an. */
public interface ResponsiveMessage {

  /**
   * Get the reactions of the message matching an unicode.
   *
   * @return the reactions
   * @param unicode the unicode to match
   */
  @NonNull
  Collection<ReactionResponse> getReactions(@NonNull String unicode);

  /**
   * Add a reaction response.
   *
   * @param response the reaction response to add to the set
   * @return this same instance
   */
  @NonNull
  ResponsiveMessage addReactionResponse(@NonNull ReactionResponse response);

  /**
   * Add a reaction response. This will also add the reaction to the message
   *
   * @param response the reaction response to add to the set
   * @param message the message to add the reaction
   */
  default void addReactionResponse(@NonNull ReactionResponse response, @NonNull Message message) {
    this.addReactionResponse(response);
    response
        .getUnicode()
        .ifPresent(
            unicode -> {
              if (unicode.startsWith("U+") || unicode.startsWith("u+")) {
                message.addReaction(Emoji.fromUnicode(unicode)).queue();
              } else {
                message
                    .getGuild()
                    .getEmojisByName(unicode, true)
                    .forEach(emote -> message.addReaction(emote).queue());
              }
            });
  }

  /**
   * Get the id of the message.
   *
   * @return the id
   */
  long getId();
}
