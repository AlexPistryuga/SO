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

    private static final Object lock = new Object(); // Synchronization lock

    private static final Color[] COLORS = {
            Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.CYAN, Color.MAGENTA
    };

    public static void main(String[] args) {
        // Create window
        JFrame frame = new JFrame("Таймер с несколькими потоками");
        frame.setSize(500, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.anchor = GridBagConstraints.CENTER;

        // Label for time display
        timeLabel = new JLabel("Таймер: 0 секунд");
        frame.add(timeLabel, gbc);

        // Label for control timer display
        gbc.gridy++;
        controlTimeLabel = new JLabel("Контрольный таймер: 0 секунд");
        frame.add(controlTimeLabel, gbc);

        // Label for status
        gbc.gridy++;
        statusLabel = new JLabel("Статус: Таймер не запущен");
        frame.add(statusLabel, gbc);

        // Button to start/stop timer
        gbc.gridy++;
        JButton startButton = new JButton("Запустить таймер");
        frame.add(startButton, gbc);

        // Button action listener
        startButton.addActionListener(e -> {
            synchronized (lock) {
                if (timer == null) {
                    // Start timer
                    timer = new Timer();
                    controlTimer = new Timer();

                    // Main timer (count seconds) - starts after 1 second to align correctly
                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            int currentSeconds = seconds.incrementAndGet();
                            SwingUtilities.invokeLater(() -> {
                                timeLabel.setText("Таймер: " + currentSeconds + " секунд");
                                statusLabel.setText("Статус: Таймер работает");
                            });
                            System.out.println("Прошло: " + currentSeconds + " секунд (Таймер 1)");
                            Thread.yield();
                        }
                    }, 1000, 1000); // Adjusted initial delay to 1000ms

                    // Control timer (3s toggle for stopping)
                    controlTimer.scheduleAtFixedRate(new TimerTask() {
                        private int controlSeconds = 0;

                        @Override
                        public void run() {
                            synchronized (lock) {
                                canStop = !canStop;
                                controlSeconds += 3;
                            }
                            SwingUtilities.invokeLater(() -> {
                                controlTimeLabel.setText("Контрольный таймер: " + controlSeconds + " секунд");
                                startButton.setEnabled(canStop);
                            });
                            System.out.println("Прошло: " + controlSeconds + " секунд (Таймер 2)");
                        }
                    }, 3000, 3000);

                    startButton.setText("Остановить таймер");
                } else if (canStop) {
                    // Stop timers
                    timer.cancel();
                    controlTimer.cancel();
                    timer = null;
                    controlTimer = null;
                    synchronized (lock) {
                        seconds.set(0);
                        canStop = true;
                    }
                    timeLabel.setText("Таймер: 0 секунд");
                    controlTimeLabel.setText("Контрольный таймер: 0 секунд");

                    // Random color selection
                    Random random = new Random();
                    Color randomColor = COLORS[random.nextInt(COLORS.length)];
                    statusLabel.setText("Статус: Таймер остановлен");
                    statusLabel.setForeground(randomColor);
                    startButton.setText("Запустить таймер");
                }
            }
        });

        // Show window
        frame.setVisible(true);
    }
}