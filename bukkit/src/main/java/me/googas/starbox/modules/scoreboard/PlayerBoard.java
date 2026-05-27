package me.googas.starbox.modules.scoreboard;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.github.chevyself.starbox.bukkit.utils.BukkitUtils;
import lombok.Getter;
import lombok.NonNull;
import me.googas.reflect.APIVersion;
import me.googas.reflect.wrappers.WrappedClass;
import me.googas.reflect.wrappers.WrappedMethod;
import me.googas.starbox.BukkitLine;
import me.googas.starbox.Starbox;
import me.googas.starbox.Strings;
import me.googas.starbox.builders.MapBuilder;
import me.googas.starbox.utility.Versions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/** The custom scoreboard for {@link Player}. */
public class PlayerBoard implements Board {

  @NonNull
  private static final WrappedClass<Scoreboard> SCOREBOARD = WrappedClass.of(Scoreboard.class);

  @NonNull
  private static final WrappedMethod<Objective> REGISTER_NEW_OBJECTIVE =
      PlayerBoard.SCOREBOARD.getMethod(
          Objective.class, "registerNewObjective", String.class, String.class, String.class);

  @NonNull
  @APIVersion(since = 8, max = 13)
  private static final WrappedMethod<Objective> REGISTER_NEW_OBJECTIVE_NO_DISPLAY =
      PlayerBoard.SCOREBOARD.getMethod(
          Objective.class, "registerNewObjective", String.class, String.class);

  @NonNull
  private static final Map<Integer, String> characters =
      MapBuilder.of(10, "a").put(11, "b").put(12, "c").put(13, "d").put(14, "e").build();

  @NonNull private final UUID player;
  @NonNull @Getter private final Scoreboard scoreboard;
  @NonNull @Getter private final Objective objective;
  @NonNull private List<ScoreboardLine> layout;

  private PlayerBoard(
      @NonNull UUID player,
      @NonNull Scoreboard scoreboard,
      @NonNull Objective objective,
      @NonNull List<ScoreboardLine> layout) {
    this.player = player;
    this.scoreboard = scoreboard;
    this.objective = objective;
    this.layout = layout;
  }

  /**
   * Create the scoreboard for a player.
   *
   * @param player the player to create the scoreboard to
   * @param layout the layout of the scoreboard
   * @return the scoreboard
   */
  public static PlayerBoard create(@NonNull Player player, @NonNull List<ScoreboardLine> layout) {
    Scoreboard bukkitScoreboard =
        Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
    return new PlayerBoard(
        player.getUniqueId(),
        bukkitScoreboard,
        PlayerBoard.createObjective(bukkitScoreboard, player.getName(), "dummy", null),
        layout);
  }

  /**
   * This is just for creating the scoreboard nothing special. Empty string
   *
   * @param position the amount of spaces that the string should have
   * @return an empty string with spaces
   */
  @NonNull
  private static String getEntryName(int position) {
    return BukkitUtils.format(
        "&" + (position <= 9 ? position : PlayerBoard.characters.get(position)) + "&r");
  }

  @NonNull
  private static Objective createObjective(
      @NonNull Scoreboard scoreboard,
      @NonNull String name,
      @NonNull String criteria,
      String display) {
    Objective objective;
    if (Versions.BUKKIT <= 13) {
      objective =
          PlayerBoard.REGISTER_NEW_OBJECTIVE_NO_DISPLAY
              .prepare(scoreboard, name, criteria)
              .handle(Starbox::severe)
              .provide()
              .orElse(null);
    } else {
      objective =
          PlayerBoard.REGISTER_NEW_OBJECTIVE
              .prepare(scoreboard, name, criteria, display == null ? name : display)
              .handle(Starbox::severe)
              .provide()
              .orElse(null);
    }
    return Objects.requireNonNull(objective, "Objective could not be created");
  }

  /**
   * Initialize the scoreboard. This will set the title and display slot also update the layout.
   *
   * @param title the title of the scoreboard if null it will be empty
   * @return this same instance
   */
  @NonNull
  public PlayerBoard initialize(String title) {
    if (title != null) this.objective.setDisplayName(title);
    this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    this.setLayout(this.layout);
    this.getPlayer().ifPresent(player -> player.setScoreboard(this.scoreboard));
    return this;
  }

  /**
   * Apply this scoreboard to the player.
   *
   * @return this same instance
   */
  public PlayerBoard apply() {
    this.getPlayer().ifPresent(player -> player.setScoreboard(this.scoreboard));
    return this;
  }

  /**
   * Adds a new line to the scoreboard.
   *
   * @param line the line to be added
   * @return the created minecraft team
   */
  @NonNull
  private Team newLine(@NonNull ScoreboardLine line) {
    String entryName = PlayerBoard.getEntryName(line.getPosition());
    Team team = this.getLineTeam(line.getPosition(), entryName);
    String current = team.getPrefix() + team.getSuffix();
    String build = line.build(this.getOfflinePlayer());
    if (!current.equalsIgnoreCase(build)) {
      List<String> divide = Strings.divide(build, 16);
      String lastColor = "";
      if (divide.isEmpty()) {
        team.setPrefix("");
        team.setSuffix("");
      } else if (divide.size() == 1) {
        team.setSuffix("");
      }
      for (int i = 0; i < divide.size(); i++) {
        String string = divide.get(i);
        switch (i) {
          case 0:
            team.setPrefix(lastColor + string);
            break;
          case 1:
            team.setSuffix(lastColor + string);
            break;
        }
        lastColor = ChatColor.getLastColors(string);
      }
      this.objective.getScore(entryName).setScore(line.getPosition());
    }
    return team;
  }

  /**
   * Gets the line in a position.
   *
   * @param position the position to get the line
   * @return a minecraft team representing a line if it exists in the position
   */
  @NonNull
  private Team getLineTeam(int position, @NonNull String entryName) {
    Team team = this.scoreboard.getTeam("line_" + position);
    if (team == null) {
      team = this.scoreboard.registerNewTeam("line_" + position);
      team.addEntry(entryName);
    }
    return team;
  }

  private Optional<ScoreboardLine> getLine(int position) {
    return this.layout.stream().filter(line -> line.getPosition() == position).findFirst();
  }

  @NonNull
  public Board update() {
    this.layout.forEach(this::newLine);
    return this;
  }

  @Override
  public @NonNull Board update(int position) {
    this.getLine(position).ifPresent(this::newLine);
    return this;
  }

  @NonNull
  private Optional<Player> getPlayer() {
    return Optional.ofNullable(Bukkit.getPlayer(this.player));
  }

  @NonNull
  private OfflinePlayer getOfflinePlayer() {
    return Bukkit.getOfflinePlayer(this.player);
  }

  /**
   * Get the unique id of the player in this scoreboard.
   *
   * @return the unique id
   */
  public UUID getUniqueId() {
    return player;
  }

  @Override
  public @NonNull Board set(@NonNull ScoreboardLine line) {
    int position = line.getPosition();
    if (position > 14) throw new IllegalArgumentException("Line entry out of bounds! > 14");
    for (int i = this.layout.size(); i < position + 1; i++) {
      this.layout.add(new ScoreboardLine(BukkitLine.of(" "), i));
      this.update(i);
    }
    this.layout.set(position, line);
    this.update(position);
    return this;
  }

  @Override
  public @NonNull Board add(@NonNull BukkitLine line) {
    if (this.layout.size() <= 14) {
      int position = this.layout.isEmpty() ? 0 : this.layout.size() - 1;
      return this.set(new ScoreboardLine(line, position));
    } else {
      throw new IllegalStateException("Scoreboard is full!");
    }
  }

  public void destroy() {
    this.scoreboard.getTeams().forEach(Team::unregister);
    this.objective.unregister();
    this.layout.clear();
  }

  @Override
  public @NonNull Board setLayout(@NonNull List<ScoreboardLine> layout) {
    Set<Team> edited = layout.stream().map(this::newLine).collect(Collectors.toSet());
    this.layout = layout;
    this.scoreboard.getTeams().stream()
        .filter(team -> !edited.contains(team))
        .forEach(Team::unregister);
    this.update();
    return this;
  }

  @NonNull
  public Board setTitle(String title) {
    this.objective.setDisplayName(title == null ? "" : title);
    return this;
  }
}
