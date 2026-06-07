package me.googas.starbox.builders;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import lombok.Getter;
import lombok.NonNull;

/**
 * Helps with single line {@link Map} building. This builder will be provided until Starbox is
 * updated to Java 9 in which more methods to create map will be given.
 */
public class MapBuilder<K, V> implements Builder<Map<K, V>> {

  @NonNull private final Map<K, V> map;

  /**
   * Create a map builder.
   *
   * @param map the map that is being built
   */
  protected MapBuilder(@NonNull Map<K, V> map) {
    this.map = map;
  }

  /** Create a map builder. */
  public MapBuilder() {
    this.map = new HashMap<>();
  }

  /**
   * Create a map builder with an initial key and value.
   *
   * @param key the initial key
   * @param value the initial value
   * @param <K> the type of the key
   * @param <V> the type of the value
   * @return a new builder instance
   */
  @NonNull
  public static <K, V> MapBuilder<K, V> of(@NonNull K key, V value) {
    MapBuilder<K, V> mapBuilder = new MapBuilder<>();
    mapBuilder.put(key, value);
    return mapBuilder;
  }

  /**
   * Create a map builder with initial entries.
   *
   * @param entries the initial entries to add in the map
   * @param <K> the type of the keys
   * @param <V> the type of the values
   * @return a new builder instance
   */
  @SafeVarargs
  @NonNull
  public static <K, V> MapBuilder<K, V> ofEntries(@NonNull MapBuilderEntry<K, V>... entries) {
    MapBuilder<K, V> builder = new MapBuilder<>();
    for (MapBuilderEntry<K, V> entry : entries) {
      builder.put(entry.getKey(), entry.getValue());
    }
    return builder;
  }

  /**
   * Create a builder entry.
   *
   * @param key the key of the entry
   * @param value the value of the entry
   * @param <K> the type of the key
   * @param <V> the type of the value
   * @return the new entry
   */
  @NonNull
  public static <K, V> MapBuilderEntry<K, V> entry(@NonNull K key, V value) {
    return new MapBuilderEntry<>(key, value);
  }

  /**
   * Appends a property to the map.
   *
   * @param key the key of hte property
   * @param value the value associated with the key
   * @return this same instance
   */
  @NonNull
  public MapBuilder<K, V> put(@NonNull K key, V value) {
    this.map.put(key, value);
    return this;
  }

  /**
   * Appends another map to the builder.
   *
   * @param map mappings to be stored in this map
   * @return this same instance
   */
  @NonNull
  public MapBuilder<K, V> appendAll(@NonNull Map<? extends K, ? extends V> map) {
    this.map.putAll(map);
    return this;
  }

  /**
   * Removes an entry from this map by its key.
   *
   * @param key the key of the entry to remove
   * @return this same instance
   */
  @NonNull
  public MapBuilder<K, V> remove(@NonNull K key) {
    this.map.remove(key);
    return this;
  }

  @NonNull
  @Override
  public Map<K, V> build() {
    return this.map;
  }

  public String toString() {
    return new StringJoiner(", ", MapBuilder.class.getSimpleName() + "[", "]")
        .add("map=" + map)
        .toString();
  }

  /**
   * Represents an entry to be used in the map builder.
   *
   * @param <K> the type of the key
   * @param <V> the type of the value
   */
  public static class MapBuilderEntry<K, V> {

    @NonNull @Getter private final K key;
    @Getter private final V value;

    private MapBuilderEntry(@NonNull K key, V value) {
      this.key = key;
      this.value = value;
    }
  }
}
