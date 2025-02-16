import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;

public class TimerExample {
    private static JLabel timeLabel;
    private static Timer timer;
    private static Timer messageTimer;
    private static int seconds = 0;

    private static final Color[] COLORS = {
            Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.CYAN, Color.MAGENTA
    };

    public static void main(String[] args) {
        // Создаем окно
        JFrame frame = new JFrame("Таймер с несколькими потоками");
        frame.setSize(300, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);

        // Создаем метку для отображения времени
        timeLabel = new JLabel("Время: 0 секунд");
        timeLabel.setBounds(100, 30, 150, 30);
        frame.add(timeLabel);

        // Добавим еще одну метку для изменения цвета текста
        JLabel statusLabel = new JLabel("Статус: Таймер не запущен");
        statusLabel.setBounds(75, 100, 200, 30);
        frame.add(statusLabel);

        // Кнопка для старта таймеров
        JButton startButton = new JButton("Запустить таймер");
        startButton.setBounds(75, 150, 150, 30);
        frame.add(startButton);

        // Обработчик нажатия кнопки
        startButton.addActionListener(e -> {
            if (seconds == 0) {
                // Создаем новый экземпляр Timer каждый раз при запуске
                timer = new Timer();
                messageTimer = new Timer();

                // Запуск основного таймера (отсчет времени)
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        seconds++;  // Увеличиваем счетчик секунд
                        SwingUtilities.invokeLater(() -> {
                            timeLabel.setText("Время: " + seconds + " секунд");
                        });
                    }
                }, 0, 1000); // Начать немедленно и повторять каждые 1000 миллисекунд (1 секунда)

                // Запуск второго таймера для вывода сообщения в консоль
                messageTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        System.out.println("Прошло " + seconds + " секунд");
                    }
                }, 0, 1000); // Каждую секунду

                startButton.setText("Остановить таймер");
            } else {
                timer.cancel();  // Останавливаем основной таймер
                messageTimer.cancel();  // Останавливаем второй таймер
                startButton.setText("Запустить таймер");
                seconds = 0;  // Сбросить секундомер
                timeLabel.setText("Время: 0 секунд");

                // Выбор случайного цвета из массива
                Random random = new Random();
                Color randomColor = COLORS[random.nextInt(COLORS.length)];

                // Изменяем цвет текста на случайный
                statusLabel.setText("Статус: Таймер остановлен");
                statusLabel.setForeground(randomColor);
            }
        });

        // Отображаем окно
        frame.setVisible(true);
    }
}
