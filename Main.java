import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;


public class Main {

    private static final int BUFFER_SIZE = 8192;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите ссылку на файл: ");
        String urlString = scanner.nextLine().trim();
        System.out.print("Введите имя файла для сохранения: ");
        String filename = scanner.nextLine().trim();
        scanner.close();


        if (urlString.isEmpty() || filename.isEmpty()) {
            System.out.println("Ссылка и имя файла не должны быть пустыми");
            return;
        }

        Thread downloadThread = new Thread(() -> {
            System.out.println("Подождите, идет скачивание...");
            long start = System.currentTimeMillis();
            try {
                saveUrl(filename, urlString);
                long time = System.currentTimeMillis() - start;
                System.out.println("Скачивание завершено за " + time + " мс");
            } catch (MalformedURLException | IllegalArgumentException ex) {
                System.out.println("Неправильная ссылка: " + urlString);
            } catch (IOException ex) {
                System.out.println("Ошибка при скачивании: " + ex.getMessage());
                new File(filename).delete();
            }
        });

        downloadThread.start();

        try {
            downloadThread.join();
        } catch (InterruptedException ex) {
            System.out.println("Ожидание было прервано");
        }
    }


    public static void saveUrl(String filename, String urlString) throws IOException {

        URL url = URI.create(urlString).toURL();


        try (BufferedInputStream in = new BufferedInputStream(url.openStream());
             FileOutputStream fout = new FileOutputStream(filename)) {

            byte[] data = new byte[BUFFER_SIZE];
            int count;
            while ((count = in.read(data)) != -1) {
                fout.write(data, 0, count);
            }
        }
    }
}