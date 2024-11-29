import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class ClippingVisualization extends JPanel {
    private static final int GRID_SIZE = 20;
    private final List<LineSegment> lineSegments = new ArrayList<>();
    private final Rectangle clippingWindow = new Rectangle();
    private boolean isClippingWindowSet = false;

    public ClippingVisualization() {
        JFrame frame = new JFrame("Clipping Algorithms Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.setLayout(new BorderLayout());

        JPanel controlPanel = createControlPanel();
        frame.add(controlPanel, BorderLayout.EAST);

        frame.add(this, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JTextField x1Field = new JTextField(5);
        JTextField y1Field = new JTextField(5);
        JTextField x2Field = new JTextField(5);
        JTextField y2Field = new JTextField(5);

        JTextField xminField = new JTextField(5);
        JTextField yminField = new JTextField(5);
        JTextField xmaxField = new JTextField(5);
        JTextField ymaxField = new JTextField(5);

        JButton addLineButton = new JButton("Добавить отрезок");
        JButton setWindowButton = new JButton("Установить окно");
        JButton clipButton = new JButton("Отсечь");

        panel.add(new JLabel("Отрезок: X1, Y1, X2, Y2"));
        panel.add(x1Field);
        panel.add(y1Field);
        panel.add(x2Field);
        panel.add(y2Field);
        panel.add(addLineButton);

        panel.add(new JLabel("Окно отсечения: Xmin, Ymin, Xmax, Ymax"));
        panel.add(xminField);
        panel.add(yminField);
        panel.add(xmaxField);
        panel.add(ymaxField);
        panel.add(setWindowButton);

        panel.add(clipButton);

        addLineButton.addActionListener(e -> {
            try {
                int x1 = Integer.parseInt(x1Field.getText());
                int y1 = Integer.parseInt(y1Field.getText());
                int x2 = Integer.parseInt(x2Field.getText());
                int y2 = Integer.parseInt(y2Field.getText());
                lineSegments.add(new LineSegment(x1, y1, x2, y2));
                repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Введите корректные координаты!");
            }
        });

        setWindowButton.addActionListener(e -> {
            try {
                int xmin = Integer.parseInt(xminField.getText());
                int ymin = Integer.parseInt(yminField.getText());
                int xmax = Integer.parseInt(xmaxField.getText());
                int ymax = Integer.parseInt(ymaxField.getText());
                clippingWindow.setBounds(xmin, ymin, xmax - xmin, ymax - ymin);
                isClippingWindowSet = true;
                repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Введите корректные координаты окна!");
            }
        });

        clipButton.addActionListener(e -> {
            if (!isClippingWindowSet) {
                JOptionPane.showMessageDialog(this, "Сначала задайте окно отсечения!");
                return;
            }
            List<LineSegment> clippedSegments = new ArrayList<>();
            for (LineSegment segment : lineSegments) {
                LineSegment clipped = cohenSutherlandClip(segment, clippingWindow);
                if (clipped != null) {
                    clippedSegments.add(clipped);
                }
            }
            lineSegments.clear();
            lineSegments.addAll(clippedSegments);
            repaint();
        });

        return panel;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.LIGHT_GRAY);
        for (int x = 0; x <= getWidth(); x += GRID_SIZE) {
            g2d.drawLine(x, 0, x, getHeight());
        }
        for (int y = 0; y <= getHeight(); y += GRID_SIZE) {
            g2d.drawLine(0, y, getWidth(), y);
        }

        g2d.setColor(Color.BLACK);
        g2d.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight());
        g2d.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);

        if (isClippingWindowSet) {
            g2d.setColor(Color.RED);
            g2d.drawRect(
                    getWidth() / 2 + clippingWindow.x * GRID_SIZE,
                    getHeight() / 2 - (clippingWindow.y + clippingWindow.height) * GRID_SIZE,
                    clippingWindow.width * GRID_SIZE,
                    clippingWindow.height * GRID_SIZE
            );
        }

        for (LineSegment segment : lineSegments) {
            g2d.setColor(Color.BLUE);
            g2d.drawLine(
                    getWidth() / 2 + segment.x1 * GRID_SIZE,
                    getHeight() / 2 - segment.y1 * GRID_SIZE,
                    getWidth() / 2 + segment.x2 * GRID_SIZE,
                    getHeight() / 2 - segment.y2 * GRID_SIZE
            );
        }
    }

    private LineSegment cohenSutherlandClip(LineSegment segment, Rectangle window) {
        int code1 = computeRegionCode(segment.x1, segment.y1, window);
        int code2 = computeRegionCode(segment.x2, segment.y2, window);

        while (true) {
            if ((code1 | code2) == 0) {
                return segment;
            } else if ((code1 & code2) != 0) {
                return null;
            } else {
                int codeOut = code1 != 0 ? code1 : code2;
                int x = 0, y = 0;

                if ((codeOut & 8) != 0) {
                    x = segment.x1 + (segment.x2 - segment.x1) * (window.y + window.height - segment.y1) / (segment.y2 - segment.y1);
                    y = window.y + window.height;
                } else if ((codeOut & 4) != 0) {
                    x = segment.x1 + (segment.x2 - segment.x1) * (window.y - segment.y1) / (segment.y2 - segment.y1);
                    y = window.y;
                } else if ((codeOut & 2) != 0) {
                    y = segment.y1 + (segment.y2 - segment.y1) * (window.x + window.width - segment.x1) / (segment.x2 - segment.x1);
                    x = window.x + window.width;
                } else if ((codeOut & 1) != 0) {
                    y = segment.y1 + (segment.y2 - segment.y1) * (window.x - segment.x1) / (segment.x2 - segment.x1);
                    x = window.x;
                }

                if (codeOut == code1) {
                    segment.x1 = x;
                    segment.y1 = y;
                    code1 = computeRegionCode(segment.x1, segment.y1, window);
                } else {
                    segment.x2 = x;
                    segment.y2 = y;
                    code2 = computeRegionCode(segment.x2, segment.y2, window);
                }
            }
        }
    }

    private int computeRegionCode(int x, int y, Rectangle window) {
        int code = 0;
        if (x < window.x) code |= 1;
        if (x > window.x + window.width) code |= 2;
        if (y < window.y) code |= 4;
        if (y > window.y + window.height) code |= 8;
        return code;
    }

    public static void main(String[] args) {
        new ClippingVisualization();
    }

    private static class LineSegment {
        int x1, y1, x2, y2;

        LineSegment(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
    }
}
