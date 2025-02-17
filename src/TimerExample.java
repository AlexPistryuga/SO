import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class TimerExample {
    private static JLabel timeLabel;
    private static JLabel controlTimeLabel;
    private static JLabel statusLabel;
    private static Timer mainTimer;
    private static Timer controlTimer;
    private static AtomicInteger mainSeconds = new AtomicInteger(0);
    private static AtomicInteger controlSeconds = new AtomicInteger(0);
    private static volatile int controlInterval = 3;
    private static int timeLimit = 5; // Лимит времени по умолчанию

    private static final Object mainLock = new Object(); // Объект синхронизации для основного таймера
    private static final Object controlLock = new Object(); // Объект синхронизации для контрольного таймера
    private static final Color[] COLORS = {
            Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.CYAN, Color.MAGENTA
    };

    public static void main(String[] args) {
        JFrame frame = new JFrame("Таймер с несколькими потоками");
        frame.setSize(500, 350);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.anchor = GridBagConstraints.CENTER;

        timeLabel = new JLabel("Таймер: 0 секунд");
        frame.add(timeLabel, gbc);

        gbc.gridy++;
        controlTimeLabel = new JLabel("Контрольный таймер: 0 секунд");
        frame.add(controlTimeLabel, gbc);

        gbc.gridy++;
        statusLabel = new JLabel("Статус: Таймер не запущен");
        frame.add(statusLabel, gbc);

        gbc.gridy++;
        JButton startMainButton = new JButton("Запустить основной таймер");
        frame.add(startMainButton, gbc);

        gbc.gridy++;
        JPanel intervalPanel = new JPanel(new FlowLayout());
        JTextField intervalField = new JTextField(1);
        intervalField.setPreferredSize(new Dimension(30, 25));
        JButton submitButton = new JButton("Установить интервал");
        intervalPanel.add(intervalField);
        intervalPanel.add(submitButton);
        frame.add(intervalPanel, gbc);

        gbc.gridy++;
        JPanel limitPanel = new JPanel(new FlowLayout());
        JButton limit5Button = new JButton("5 секунд");
        JButton limit15Button = new JButton("15 секунд");
        JButton limit50Button = new JButton("50 секунд");
        limitPanel.add(limit5Button);
        limitPanel.add(limit15Button);
        limitPanel.add(limit50Button);
        frame.add(limitPanel, gbc);

        // Обработчик для основного таймера
        startMainButton.addActionListener(e -> {
            synchronized (mainLock) {
                if (mainTimer == null) {
                    mainTimer = new Timer();
                    mainSeconds.set(0);

                    mainTimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            int currentSeconds = mainSeconds.incrementAndGet();
                            SwingUtilities.invokeLater(() -> {
                                timeLabel.setText("Таймер: " + currentSeconds + " секунд");
                                statusLabel.setText("Статус: Основной таймер работает");
                                // Если достигнут лимит времени
                                if (currentSeconds >= timeLimit) {
                                    timeLabel.setForeground(COLORS[new Random().nextInt(COLORS.length)]);
                                    stopMainTimer(); // Остановка основного таймера
                                    statusLabel.setText("Статус: Основной таймер остановлен (достигнут лимит)");
                                    startMainButton.setText("Запустить основной таймер");
                                    startMainButton.setEnabled(true); // Включаем кнопку после завершения основного таймера
                                }
                            });
                        }
                    }, 1000, 1000);

                    startMainButton.setText("Остановить основной таймер");
                    startMainButton.setEnabled(false); // Отключаем кнопку, чтобы она не могла быть нажата
                } else {
                    stopMainTimer();
                    startMainButton.setText("Запустить основной таймер");
                }
            }
        });

        // Обработчик для установки интервала
        submitButton.addActionListener(e -> {
            String input = intervalField.getText().trim();
            try {
                int newInterval = Integer.parseInt(input);
                if (newInterval <= 0) throw new NumberFormatException();

                synchronized (controlLock) {
                    controlInterval = newInterval;

                    // Если контрольный таймер существует, отменим его, а затем перезапустим с новым интервалом
                    if (controlTimer != null) {
                        controlTimer.cancel();
                    }
                    controlTimer = new Timer();
                    controlSeconds.set(0); // Сбрасываем счетчик секунд контрольного таймера

                    // Перезапускаем контрольный таймер с новым интервалом
                    controlTimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            int currentSeconds = controlSeconds.addAndGet(controlInterval);
                            SwingUtilities.invokeLater(() -> {
                                controlTimeLabel.setText("Контрольный таймер: " + currentSeconds + " секунд");
                            });
                        }
                    }, controlInterval * 1000, controlInterval * 1000);

                    // Переключаем состояние кнопки каждый интервал
                    new Timer().scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            SwingUtilities.invokeLater(() -> {
                                startMainButton.setEnabled(!startMainButton.isEnabled());
                            });
                        }
                    }, 0, newInterval * 1000); // Интервал для переключения состояния кнопки
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame,
                        "Введите положительное целое число",
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
            }
        });



        // Обработчики для установки лимита времени
        limit5Button.addActionListener(e -> timeLimit = 5);
        limit15Button.addActionListener(e -> timeLimit = 15);
        limit50Button.addActionListener(e -> timeLimit = 50);

        frame.setVisible(true);
    }

    // Метод для остановки основного таймера
    private static void stopMainTimer() {
        synchronized (mainLock) {
            if (mainTimer != null) {
                mainTimer.cancel();
                mainTimer = null;
            }
            mainSeconds.set(0);
            timeLabel.setText("Таймер: 0 секунд");
            statusLabel.setForeground(COLORS[new Random().nextInt(COLORS.length)]);
        }
    }

    // Метод для остановки контрольного таймера
    private static void stopControlTimer() {
        synchronized (controlLock) {
            if (controlTimer != null) {
                controlTimer.cancel();
                controlTimer = null;
            }
            controlSeconds.set(0);
            controlTimeLabel.setText("Контрольный таймер: 0 секунд");
        }
    }
}
