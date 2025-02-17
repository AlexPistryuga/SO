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
    private static Timer timer;
    private static Timer controlTimer;
    private static AtomicInteger seconds = new AtomicInteger(0);
    private static boolean canStop = true;
    private static volatile int controlInterval = 3;
    private static AtomicInteger controlSeconds = new AtomicInteger(0);

    private static final Object lock = new Object();
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
        JButton startButton = new JButton("Запустить таймер");
        frame.add(startButton, gbc);

        gbc.gridy++;
        JPanel intervalPanel = new JPanel(new FlowLayout());
        JTextField intervalField = new JTextField(1);
        intervalField.setPreferredSize(new Dimension(30, 25));
        JButton submitButton = new JButton("Установить интервал");
        intervalPanel.add(intervalField);
        intervalPanel.add(submitButton);
        frame.add(intervalPanel, gbc);

        startButton.addActionListener(e -> {
            synchronized (lock) {
                if (timer == null) {
                    timer = new Timer();
                    controlTimer = new Timer();
                    seconds.set(0);
                    controlSeconds.set(0);

                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            int currentSeconds = seconds.incrementAndGet();
                            SwingUtilities.invokeLater(() -> {
                                timeLabel.setText("Таймер: " + currentSeconds + " секунд");
                                statusLabel.setText("Статус: Таймер работает");
                            });
                        }
                    }, 1000, 1000);

                    controlTimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            synchronized (lock) {
                                canStop = !canStop;
                                controlSeconds.addAndGet(controlInterval);
                            }
                            SwingUtilities.invokeLater(() -> {
                                controlTimeLabel.setText("Контрольный таймер: " + controlSeconds.get() + " секунд");
                                startButton.setEnabled(canStop);
                            });
                        }
                    }, controlInterval * 1000, controlInterval * 1000);

                    startButton.setText("Остановить таймер");
                } else if (canStop) {
                    timer.cancel();
                    controlTimer.cancel();
                    timer = null;
                    controlTimer = null;
                    seconds.set(0);
                    controlSeconds.set(0);
                    canStop = true;
                    timeLabel.setText("Таймер: 0 секунд");
                    controlTimeLabel.setText("Контрольный таймер: 0 секунд");
                    statusLabel.setText("Статус: Таймер остановлен");
                    statusLabel.setForeground(COLORS[new Random().nextInt(COLORS.length)]);
                    startButton.setText("Запустить таймер");
                }
            }
        });

        submitButton.addActionListener(e -> {
            String input = intervalField.getText().trim();
            try {
                int newInterval = Integer.parseInt(input);
                if (newInterval <= 0) throw new NumberFormatException();

                synchronized (lock) {
                    controlInterval = newInterval;
                    if (controlTimer != null) {
                        controlTimer.cancel();
                        controlTimer = new Timer();
                        controlTimer.scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                                synchronized (lock) {
                                    canStop = !canStop;
                                    controlSeconds.addAndGet(controlInterval);
                                }
                                SwingUtilities.invokeLater(() -> {
                                    controlTimeLabel.setText("Контрольный таймер: " + controlSeconds.get() + " секунд");
                                    startButton.setEnabled(canStop);
                                });
                            }
                        }, controlInterval * 1000, controlInterval * 1000);
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame,
                        "Введите положительное целое число",
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setVisible(true);
    }
}