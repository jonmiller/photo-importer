import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class PhotoImporter {
  private static final String exiflistLocation = "C:/Users/Jon/Programs/EXIFutils/exiflist.exe";

  private static final String tempFileName = "temp.txt";

  public static void main(String[] args) {
    PhotoImporter photoImporter = new PhotoImporter();
    photoImporter.importPhotos();
  }

  private void importPhotos() {
    File sourceFolder = new File("F:/DCIM/100_____");
    File destinationFolder = new File("C:/Users/Jon/Pictures/2/");

    File[] photos = sourceFolder.listFiles();
    Arrays.sort(photos);

    System.out.println(photos.length);

    ThreeDigitSerialInteger i = new ThreeDigitSerialInteger();
    for (File photo : photos) {
      String extension = photo.getAbsolutePath().substring(photo.getAbsolutePath().lastIndexOf('.'));

      if (extension.equalsIgnoreCase(".NEF")) {
        String dateTaken = findDate(photo.getAbsolutePath());

        File dateDirectory = new File(destinationFolder, dateTaken);
        if (!dateDirectory.exists()) {
          i.reset();
          dateDirectory.mkdir();
        }

        File destinationFile = new File(dateDirectory, dateDirectory.getName() + "-" + i.get() + extension);
        if (!copyFile(photo, destinationFile)) {
          System.out.println("Failure");
          return;
        }
        i.increment();
      }
    }

  }

  private String findDate(String photoFilePath) {
    String tempFilePath =
        photoFilePath.substring(0, photoFilePath.lastIndexOf(File.separatorChar)) + File.separatorChar + tempFileName;

    Runtime runtime = Runtime.getRuntime();
    String command =
        "cmd /c \"" + exiflistLocation + "\"" + " /o l /f date-taken " + photoFilePath + " > " + tempFilePath;

    try {
      Process process = runtime.exec(command);
      process.waitFor();
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }

    String date = null;
    try {
      BufferedReader bufferedReader = new BufferedReader(new FileReader(tempFilePath));
      date = bufferedReader.readLine();
      date = date.substring(0, date.indexOf(' ')).replace(':', '-');
    } catch (IOException e) {
      e.printStackTrace();
    }

    return date;
  }

  private boolean copyFile(File source, File destination) {
    Runtime runtime = Runtime.getRuntime();
    try {
      Process process =
          runtime.exec("cmd /C copy \"" + source.getAbsolutePath() + "\" \"" + destination.getAbsolutePath() + "\" /V");
      process.waitFor();
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  private static class ThreeDigitSerialInteger {
    private Integer i = 0;

    String get() {
      if (i < 10) {
        return "00" + i.toString();
      } else if (i < 100) {
        return "0" + i.toString();
      } else if (i < 1000) {
        return i.toString();
      } else {
        return null;
      }
    }

    void reset() {
      i = 0;
    }

    void increment() {
      i++;
    }
  }
}
