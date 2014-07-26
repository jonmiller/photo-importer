package com.jonamiller.photoimporter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class PhotoImporter {

    private static final String EXIF_LIST_EXE = "C:\\PROGRA~2\\EXIFutils\\exiflist.exe";
    private static final String TEMP_FILE_NAME = "temp.txt";

    private static final String DEFAULT_SOURCE = "F:/DCIM/100_____";
    private static final String DEFAULT_DEST = "D:/Users/Jon/Pictures/D40 Photos/";

    public static void main(String[] args) {
        PhotoImporter photoImporter = new PhotoImporter();
        photoImporter.importPhotos();
    }

    private void importPhotos() {

        Path sourcePath = Paths.get(DEFAULT_SOURCE);
        Path destinationPath = Paths.get(DEFAULT_DEST);

        try {

            List<Path> photoPaths = Files.list(sourcePath)
                    .filter(foundPath -> foundPath.toString().toLowerCase().endsWith(".nef"))
                    .sorted()
                    .collect(Collectors.toList());

            System.out.println(photoPaths.size());

            int counter = 0;
            for (Path photoPath : photoPaths) {

                String dateTaken = findDate(photoPath);

                Path dateFolderPath = destinationPath.resolve(dateTaken);

                if (Files.notExists(dateFolderPath)) {
                    counter = 0;
                    dateFolderPath = Files.createDirectory(dateFolderPath);
                }

                String number = String.format("%03d", counter);

                Path destinationPhotoPath = dateFolderPath.resolve(
                        dateFolderPath.getFileName() + "-" + number + ".NEF");

                Files.copy(photoPath, destinationPhotoPath);

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private String findDate(Path photoPath) {

        Path tempFilePath = photoPath.getParent().resolve(TEMP_FILE_NAME);

        Runtime runtime = Runtime.getRuntime();
        String command = "cmd /c \"" + EXIF_LIST_EXE + "\"" +
                " /o l /f date-taken " + photoPath + " > " + tempFilePath;

        try {
            Process process = runtime.exec(command);
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        String date;
        try {
            date = Files.readAllLines(tempFilePath).get(0);
            date = date.substring(0, date.indexOf(' ')).replace(':', '-');
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return date;
    }

}
