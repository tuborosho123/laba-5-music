import java.io.BufferedInputStream; // поток, который читает байты из сети пачками
import java.io.FileOutputStream;    // поток, который пишет байты в файл на диске
import java.io.IOException;         // ошибка ввода-вывода: нет сети, 403, диск и т.д.
import java.net.URL;                // класс для работы со ссылкой http/https
public class mp3 {

    public static void main(String[] args) {

        // s — прямая ссылка на песню, скопированная со страницы mp3xa
        // (кнопка «скачать» / плеер отдаёт именно такой адрес)
        String s = "https://cdnproxy3.mp3xa.me/1XhT3tDzQ-ETJK-Urb29hw/1793398782/L21wMy8yMDIwLzAyL9Cd0LXRgNCy0YsgLSDQndC10YDQstGLLm1wMw";

        // f — имя файла, который появится в папке проекта после скачивания
        String f = "song.mp3";

        // t — поток скачивания
        // new Runnable() { run() { ... } } — это работа, которую поток выполнит
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // этот метод вызовется УЖЕ внутри нового потока, не в main

                System.out.println("Подождите, идет скачивание...");

                try {
                    // вызываем метод из статьи: скачать s в f
                    saveUrl(f, s);

                    // если дошли сюда — файл записался без исключения
                    System.out.println("Скачивание завершено");

                } catch (IOException x) {
                    // сюда попадём, если:
                    // - нет интернета
                    // - ссылка битая / протухла
                    // - сайт вернул 403 Forbidden
                    // - нельзя создать файл song.mp3
                    System.out.println("Ошибка при скачивании: " + x.getMessage());
                }
            }
        });

        // start() НЕ качает сам. Он только запускает поток.
        // Реальная работа начинается в run() чуть позже, параллельно с main.
        t.start();

        // join() — main встаёт и ждёт, пока поток t закончит run()
        // без join программа может закрыться раньше, чем файл докачается
        try {
            t.join();
        } catch (InterruptedException x) {
            // кто-то прервал ожидание (редко, но обработать надо)
            x.printStackTrace();
        }
    }

    /**
     * Метод из статьи. Качает файл по ссылке и сохраняет на диск.
     *
     * a — куда писать, например song.mp3
     * u — откуда качать
     *
     * throws IOException — если сеть или файл сломались, ошибку бросаем наверх,
     * её ловит catch в run().
     */
    public static void saveUrl(final String a, final String u) throws IOException {

        // in — читаем из интернета
        BufferedInputStream in = null;

        // out — пишем в файл
        FileOutputStream out = null;

        try {
            // 1) открываем соединение по ссылке и читаем байты
            // new URL(u) — делаем объект ссылки
            // openStream() — подключаемся к серверу и получаем входящий поток
            // BufferedInputStream — читаем не по 1 байту, а пачками, так быстрее
            in = new BufferedInputStream(new URL(u).openStream());

            // 2) создаём (или перезаписываем) файл song.mp3
            out = new FileOutputStream(a);

            // 3) b — буфер на 1024 байта = 1 КБ
            // в статье так. Можно 8192 — меньше обращений к диску, чуть быстрее
            final byte[] b = new byte[1024];

            // n — сколько байт реально прочитали за один заход
            int n;

            // 4) крутимся, пока файл не кончится
            // read кладёт байты в b и возвращает число прочитанных
            // если вернул -1 — на сервере больше нечего читать, выходим
            while ((n = in.read(b, 0, 1024)) != -1) {
                // пишем в файл ровно n байт, не весь массив
                // (последняя порция обычно короче 1024)
                out.write(b, 0, n);
            }

        } finally {
            // finally выполнится ВСЕГДА: и при успехе, и при ошибке
            // потоки надо закрыть, иначе файл может остаться «занятым»
            if (in != null) in.close();   // закрыли интернет-поток
            if (out != null) out.close(); // закрыли файл на диске
        }
    }
}
