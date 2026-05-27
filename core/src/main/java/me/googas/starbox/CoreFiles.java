package me.googas.starbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import lombok.Getter;
import lombok.NonNull;

/** Core utilities for files */
public class CoreFiles {

  /** The class loader of this class to be used to load resources */
  @NonNull private static final ClassLoader LOADER = CoreFiles.class.getClassLoader();

  /**
   * Get a file from a directory
   *
   * @param parent the directory of the file
   * @param fileName the name of the file
   * @return the file
   */
  public static File getFile(String parent, @NonNull String fileName) {
    File file = new FileNameValidator(parent, fileName).getFile();
    if (file.exists()) return file;
    return null;
  }

  /**
   * Get a file
   *
   * @param fileName the path to the file
   * @return the file if found else null
   */
  public static File getFile(@NonNull String fileName) {
    return CoreFiles.getFile(null, fileName);
  }

  /**
   * Deletes the given directory
   *
   * @param directory the directory to delete
   */
  public static void delete(@NonNull File directory) {
    File[] files = directory.listFiles();
    if (files != null) {
      for (File file : files) {
        CoreFiles.delete(file);
      }
    }
    if (directory.exists()) directory.delete();
  }

  /**
   * Gets a file from a directory or creates it
   *
   * @param parent the directory of the file
   * @param fileName the name of the file
   * @return the file
   * @throws IOException if the directory or the file could not be created
   */
  @NonNull
  public static File getOrCreate(String parent, @NonNull String fileName) throws IOException {
    FileNameValidator validator = new FileNameValidator(parent, fileName);
    File file = validator.getFile();
    File directory = validator.getDirectory();
    if (directory != null && !directory.exists()) {
      if (!CoreFiles.createFileParents(file))
        throw new IOException("Directory " + directory + " could not be created");
    }
    if (file.exists()) return file;
    if (file.createNewFile()) return file;
    throw new IOException("The file " + file + " could not be created");
  }

  /**
   * Copies a resource to a file
   *
   * @param file the file to copy the resource to
   * @param stream the resource as stream
   * @return the file
   * @throws IOException if the resource could not be copied
   */
  @NonNull
  public static File copyResource(@NonNull File file, @NonNull InputStream stream)
      throws IOException {
    Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    return file;
  }

  /**
   * Copies the resource using the name of the resource
   *
   * @param file the file to copy the resource to
   * @param resourceName the name of the resource
   * @return the file
   * @throws IOException if the file could not be created/copied
   */
  public static File copyResource(@NonNull File file, @NonNull String resourceName)
      throws IOException {
    return CoreFiles.copyResource(file, CoreFiles.getResource(resourceName));
  }

  /**
   * Get a resource
   *
   * @param name the name of the resource
   * @return the resource as input stream
   */
  @NonNull
  public static InputStream getResource(@NonNull String name) {
    return Objects.requireNonNull(
        CoreFiles.LOADER.getResourceAsStream(name), "Resource " + name + " was not found!");
  }

  /**
   * Gets a file or copies a resource
   *
   * @param parent the directory of the file
   * @param fileName the name of the file
   * @param resource the resource
   * @return the file
   * @throws IOException if the file could not be created/copied in case that it is needed
   */
  @NonNull
  public static File getFileOrResource(
      String parent, @NonNull String fileName, @NonNull InputStream resource) throws IOException {
    File file = CoreFiles.getFile(parent, fileName);
    if (file == null || !file.exists()) {
      file = CoreFiles.getOrCreate(parent, fileName);
      CoreFiles.copyResource(file, resource);
    }
    return file;
  }

  /**
   * Get a file or copy the resource
   *
   * @param fileName the path to the file
   * @param resource the resource
   * @return the file
   * @throws IOException if the file could not be created/copied in case that it is needed
   */
  @NonNull
  public static File getFileOrResource(@NonNull String fileName, @NonNull InputStream resource)
      throws IOException {
    return CoreFiles.getFileOrResource(null, fileName, resource);
  }

  /**
   * Gets a file or copies a resource
   *
   * @param parent the directory of the file
   * @param fileName the name of the file
   * @return the file
   * @throws IOException if the file could not be created/copied in case that it is needed
   */
  @NonNull
  public static File getFileOrResource(String parent, @NonNull String fileName) throws IOException {
    File file = CoreFiles.getFile(parent, fileName);
    if (file == null || !file.exists()) {
      file = CoreFiles.getOrCreate(parent, fileName);
      CoreFiles.copyResource(file, CoreFiles.getResource(fileName));
    }
    return file;
  }

  /**
   * Get a file or copy the resource
   *
   * @param fileName the path to the file
   * @return the file
   * @throws IOException if the file could not be created/copied in case that it is needed
   */
  @NonNull
  public static File getFileOrResource(@NonNull String fileName) throws IOException {
    return CoreFiles.getFileOrResource(null, fileName, CoreFiles.getResource(fileName));
  }

  /**
   * Get a file or create it
   *
   * @param fileName the name of the file
   * @return the file
   * @throws IOException if the file could not be created
   */
  @NonNull
  public static File getOrCreate(@NonNull String fileName) throws IOException {
    return CoreFiles.getOrCreate(null, fileName);
  }

  /**
   * Get the current working directory
   *
   * @return the directory
   */
  @NonNull
  public static String currentDirectory() {
    return System.getProperty("user.dir");
  }

  /**
   * Validates the path by changing the '/' to the actual type of separator for each OS
   *
   * @param path the path to validate
   * @return the validated path
   */
  @NonNull
  public static String validatePath(@NonNull String path) {
    return path.replace("/", File.separator);
  }

  /**
   * Get a file reader
   *
   * @param file the file that needs to be read
   * @return the file reader
   * @throws FileNotFoundException if the file is not found
   */
  @NonNull
  public static FileReader getReader(@NonNull File file) throws FileNotFoundException {
    return new FileReader(file);
  }

  /**
   * Get a directory or create it
   *
   * @param parent the path of the directory
   * @return the directory
   * @throws IOException if the directory cannot be created
   */
  @NonNull
  public static File directoryOrCreate(@NonNull String parent) throws IOException {
    File file = new File(CoreFiles.validatePath(parent));
    if (!file.exists()) {
      if (CoreFiles.createDirectories(file)) return file;
      else throw new IOException("Directory could not be created");
    }
    if (file.isDirectory()) return file;
    throw new IOException("There's already a file with the name of the directory");
  }

  /**
   * Copies a directory
   *
   * @param source the source directory
   * @param destination the destination directory
   * @throws IOException if the destination directory could not be created
   */
  public static void copyDirectory(@NonNull File source, @NonNull File destination)
      throws IOException {
    String[] list = source.list();
    if (source.isDirectory() && list != null) {
      if (!destination.exists() && !destination.mkdir())
        throw new IOException(destination + " could not be created");
      for (String string : list) {
        File sourceFile = new File(source, string);
        File destinationFile = new File(destination, string);
        CoreFiles.copyDirectory(sourceFile, destinationFile);
      }
    } else {
      CoreFiles.copyFile(source, destination);
    }
  }

  /**
   * Copies a file
   *
   * @param source the source file
   * @param destination the destination file
   * @throws IOException if the files do not exist
   */
  public static void copyFile(@NonNull File source, @NonNull File destination) throws IOException {
    try (InputStream input = new FileInputStream(source);
        OutputStream output = new FileOutputStream(destination)) {
      byte[] buffer = new byte[1024];
      int length;
      while ((length = input.read(buffer)) > 0) {
        output.write(buffer, 0, length);
      }
    }
  }

  /**
   * Create the parents of the file
   *
   * @param file the file that needs the parent
   * @return true if the parents were created
   */
  public static boolean createFileParents(@NonNull File file) {
    return CoreFiles.createDirectories(file.getParentFile());
  }

  /**
   * Create the directories for certain directory
   *
   * @param directory the directory that needs its parents
   * @return true if the directories were created
   */
  public static boolean createDirectories(@NonNull File directory) {
    File toCreate = directory;
    while (!toCreate.exists()) {
      if (toCreate.getParentFile() != null && !toCreate.getParentFile().exists()) {
        toCreate = toCreate.getParentFile();
        continue;
      }
      if (toCreate.mkdir()) {
        toCreate = directory;
      } else {
        return false;
      }
    }
    return true;
  }

  /** Validates the name/directory of a file to be compatible with every os */
  private static class FileNameValidator {

    /** The file */
    @NonNull @Getter private final File file;
    /** The directory of the file */
    @Getter private final File directory;

    /**
     * Starts the validation
     *
     * @param parent the directory of the file
     * @param fileName the file
     */
    public FileNameValidator(String parent, @NonNull String fileName) {
      fileName = CoreFiles.validatePath(fileName);
      this.file =
          parent == null ? new File(fileName) : new File(CoreFiles.validatePath(parent), fileName);
      this.directory = this.file.getParentFile();
    }
  }
}
