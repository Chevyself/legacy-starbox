package me.googas.starbox.commands;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.NonNull;
import me.googas.commands.annotations.Free;
import me.googas.commands.annotations.Multiple;
import me.googas.commands.annotations.Required;
import me.googas.commands.bukkit.CommandManager;
import me.googas.commands.bukkit.annotations.Command;
import me.googas.commands.bukkit.result.Result;
import me.googas.commands.bukkit.utils.BukkitUtils;
import me.googas.starbox.BukkitLine;
import me.googas.starbox.Starbox;
import me.googas.starbox.modules.scoreboard.MultiBoard;
import me.googas.starbox.modules.scoreboard.ScoreboardLine;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Commands to debug scoreboards. Javadoc's warnings are suppressed as commands already have a
 * description and usage.
 */
@SuppressWarnings("JavaDoc")
public class ScoreboardCommands {

  @NonNull private final MultiBoard scoreboard;
  @NonNull private final Set<Integer> refreshIndexes;
  private long rate;
  private BukkitTask task;

  public ScoreboardCommands(
      @NonNull MultiBoard scoreboard, @NonNull Set<Integer> refreshIndexes, long rate) {
    this.scoreboard = scoreboard;
    this.refreshIndexes = refreshIndexes;
    this.rate = rate;
  }

  /** Start the task to update lines in the board */
  public ScoreboardCommands() {
    this(new MultiBoard(), new HashSet<>(), 1);
  }

  @NonNull
  public ScoreboardCommands start(@NonNull Plugin plugin) {
    if (this.task != null) {
      this.task.cancel();
    }
    this.task =
        Bukkit.getScheduler()
            .runTaskTimer(
                plugin,
                () -> this.refreshIndexes.forEach(this.scoreboard::update),
                this.rate,
                this.rate);
    return this;
  }

  @Command(
      aliases = "rate",
      description = "Set the rate in which the scoreboard updates",
      permission = "starbox.scoreboard")
  public Result rate(
      CommandSender sender,
      @Required(name = "rate", description = "The new rate in Minecraft ticks") int rate) {
    this.rate = rate;
    this.start(Starbox.getPlugin());
    return BukkitLine.localized(sender, "scoreboard.rate").format(this.rate).asResult();
  }

  @Command(
      aliases = "title",
      description = "Set the title of the scoreboard",
      permission = "starbox.scoreboard")
  public Result title(
      CommandSender sender,
      @Multiple @Required(name = "title", description = "The new title of the scoreboard")
          String title) {
    String formatted = BukkitUtils.format(title);
    this.scoreboard.setTitle(formatted);
    return BukkitLine.localized(sender, "scoreboard.title").format(formatted).asResult();
  }

  @Command(
      aliases = "insert",
      description = "Add a player to this scoreboard",
      permission = "starbox.scoreboard")
  public Result insert(
      CommandSender sender,
      @Required(name = "player", description = "The player to add to the scoreboard")
          Player player) {
    String name = player.getName();
    if (this.scoreboard.contains(player)) {
      return BukkitLine.localized(sender, "scoreboard.result.contains").format(name).asResult();
    } else {
      this.scoreboard.add(player);
      return BukkitLine.localized(sender, "scoreboard.result.added").format(name).asResult();
    }
  }

  @Command(
      aliases = "eject",
      description = "Eject a player from this scoreboard",
      permission = "starbox.scoreboard")
  public Result eject(
      CommandSender sender,
      @Required(name = "player", description = "The player to eject from the scoreboard")
          Player player) {
    this.scoreboard.remove(player);
    return BukkitLine.localized(sender, "scoreboard.eject").format(player.getName()).asResult();
  }

  @Command(aliases = "add", description = "Add a new line", permission = "starbox.scoreboard")
  public Result add(
      CommandSender sender,
      @Multiple @Required(name = "line", description = "The line to add") BukkitLine line) {
    scoreboard.add(line);
    return BukkitLine.localized(sender, "scoreboard.add").asResult();
  }

  @Command(aliases = "set", description = "Set a line", permission = "starbox.scoreboard")
  public Result set(
      CommandSender sender,
      @Required(
              name = "position",
              description = "The position to set the line in",
              suggestions = {
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14"
              })
          int position,
      @Multiple @Required(name = "line", description = "The line to set") BukkitLine line) {
    if (position < 0) return BukkitLine.localized(sender, "scoreboard.set.less-0").asResult();
    if (position > 14) return BukkitLine.localized(sender, "scoreboard.set.more-14").asResult();
    scoreboard.set(new ScoreboardLine(line, position));
    return BukkitLine.localized(sender, "scoreboard.set.done").format(position).asResult();
  }

  @Command(
      aliases = "update",
      description = "Updates the scoreboard",
      permission = "starbox.scoreboard")
  public Result update(
      CommandSender sender,
      @Free(
              name = "position",
              description = "The position to update",
              suggestions = {
                "-1", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14"
              })
          int position) {
    if (position < 0) {
      scoreboard.update();
      return BukkitLine.localized(sender, "scoreboard.update.all").asResult();
    } else {
      scoreboard.update(position);
      return BukkitLine.localized(sender, "scoreboard.update.unique").format(position).asResult();
    }
  }

  @Command(
      aliases = "refresh",
      description = "Make an position refresh automatically",
      permission = "starbox.scoreboard")
  public Result refresh(
      CommandSender sender,
      @Required(name = "position", description = "The position to refresh automatically")
          int position) {
    if (position < 0) return BukkitLine.localized(sender, "scoreboard.set.less-0").asResult();
    if (position > 14) return BukkitLine.localized(sender, "scoreboard.set.more-14").asResult();
    if (this.refreshIndexes.contains(position))
      return BukkitLine.localized(sender, "scoreboard.refresh.contains")
          .format(position)
          .asResult();
    this.refreshIndexes.add(position);
    return BukkitLine.localized(sender, "scoreboard.refresh.added").format(position).asResult();
  }

  @Command(
      aliases = "fix",
      description = "Make an position stay fixed",
      permission = "starbox.scoreboard")
  public Result fix(
      CommandSender sender,
      @Required(name = "position", description = "The position to fix") int position) {
    if (this.refreshIndexes.remove(position)) {
      return BukkitLine.localized(sender, "scoreboard.fix.added").format(position).asResult();
    } else {
      return BukkitLine.localized(sender, "scoreboard.fix.not").format(position).asResult();
    }
  }

  public static class Parent extends StarboxParentCommand {

    public Parent(@NonNull CommandManager manager) {
      super(
          "scoreboard",
          "Set the scoreboard to a player",
          "scoreboard|sb <subcommand>",
          Collections.singletonList("sb"),
          false,
          manager);
    }

    @Override
    public String getPermission() {
      return "starbox.scoreboard";
    }
  }
}
