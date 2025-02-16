import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;

public class TimerExample {
    private static JLabel timeLabel1;
    private static JLabel timeLabel2;
    private static JLabel statusLabel;
    private static Timer timer1;
    private static Timer timer2;
    private static Timer controlTimer;
    private static int seconds = 0;
    private static boolean canStop = true;

    private static final Object lock = new Object(); // Synchronization lock

    private static final Color[] COLORS = {
            Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.CYAN, Color.MAGENTA
    };

    public static void main(String[] args) {
        // Create window
        JFrame frame = new JFrame("Таймер с несколькими потоками");
        frame.setSize(300, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);

        // Labels for time display
        timeLabel1 = new JLabel("Таймер 1: 0 секунд");
        timeLabel1.setBounds(100, 30, 150, 30);
        frame.add(timeLabel1);

        timeLabel2 = new JLabel("Таймер 2: 0 секунд");
        timeLabel2.setBounds(100, 60, 150, 30);
        frame.add(timeLabel2);

        // Label for status
        statusLabel = new JLabel("Статус: Таймер не запущен");
        statusLabel.setBounds(75, 100, 200, 30);
        frame.add(statusLabel);

        // Button to start/stop timer
        JButton startButton = new JButton("Запустить таймер");
        startButton.setBounds(75, 150, 150, 30);
        frame.add(startButton);

        // Button action listener
        startButton.addActionListener(e -> {
            synchronized (lock) {
                if (timer1 == null && timer2 == null) {
                    // Start timers
                    timer1 = new Timer();
                    timer2 = new Timer();
                    controlTimer = new Timer();

                    // Main timers (count seconds)
                    timer1.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            synchronized (lock) {
                                seconds++;
                            }
                            SwingUtilities.invokeLater(() -> {
                                timeLabel1.setText("Таймер 1: " + seconds + " секунд");
                                timeLabel2.setText("Таймер 2: " + seconds + " секунд");
                                statusLabel.setText("Статус: Таймер работает");
                            });
                            System.out.println("Таймер 1 прошло: " + seconds + " секунд");
                            System.out.println("Таймер 2 прошло: " + seconds + " секунд");
                        }
                    }, 0, 1000);

                    // Control timer (7s toggle for stopping)
                    controlTimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            synchronized (lock) {
                                canStop = !canStop;
                            }
                            SwingUtilities.invokeLater(() -> {
                                startButton.setEnabled(canStop);
                            });
                        }
                    }, 3000, 3000);

                    startButton.setText("Остановить таймер");
                } else if (canStop) {
                    // Stop timers
                    timer1.cancel();
                    timer2.cancel();
                    controlTimer.cancel();
                    timer1 = null;
                    timer2 = null;
                    controlTimer = null;
                    synchronized (lock) {
                        seconds = 0;
                        canStop = true;
                    }
                    timeLabel1.setText("Таймер 1: 0 секунд");
                    timeLabel2.setText("Таймер 2: 0 секунд");

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
