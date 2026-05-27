package me.googas.starbox;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Delegate;

/** This object represents the initial arguments of a program */
public class ProgramArguments {

  /** The initial flags */
  @NonNull @Getter private final Set<String> flags;

  /** The properties */
  @NonNull @Getter @Delegate private final Properties properties;

  public ProgramArguments(@NonNull Set<String> flags, @NonNull Properties properties) {
    this.flags = flags;
    this.properties = properties;
  }

  public ProgramArguments() {
    this(new HashSet<>(), new Properties());
  }

  /**
   * Construct the program arguments from the initial array of strings
   *
   * @param args the initial array of strings
   * @return the constructed program arguments
   */
  @NonNull
  public static ProgramArguments construct(String[] args) {
    if (args == null) return new ProgramArguments();
    Set<String> flags = new HashSet<>();
    Properties properties = new Properties();
    for (String arg : args) {
      if (arg.contains("=")) {
        String[] split = arg.split("=");
        properties.put(split[0], split[1]);
      } else {
        flags.add(arg);
      }
    }
    return new ProgramArguments(flags, properties);
  }

  /**
   * Construct the program arguments from a string that will be split using ' '
   *
   * @param args the initial string
   * @return the constructed program arguments
   */
  @NonNull
  public static ProgramArguments construct(String args) {
    if (args == null) return new ProgramArguments();
    return ProgramArguments.construct(args.split(" "));
  }

  /**
   * Check whether the program has the given flag
   *
   * @param flagToCheck the flag to check
   * @param ignoreCase whether to check the flag ignore case
   * @return true if the flag is in the set
   */
  public boolean containsFlag(String flagToCheck, boolean ignoreCase) {
    if (flagToCheck == null) return false;
    for (String flag : this.flags) {
      if (ignoreCase && flag.equalsIgnoreCase(flagToCheck)) return true;
      else if (flag.equals(flagToCheck)) return true;
    }
    return false;
  }
}
