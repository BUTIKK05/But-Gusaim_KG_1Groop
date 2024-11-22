import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

public class CoordinatePlane extends JPanel {
    private static final int GRID_SIZE = 20;
    private static final Set<Point> filledSquares = new HashSet<>();

    public class SettingsPanel extends JPanel {
        public SettingsPanel() {
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JButton btnBrasenham = new JButton("Бразенхем");
            JButton btnLinear = new JButton("Пошаговый метод");
            JButton btnDDA = new JButton("Алгоритм ЦДА");
            JButton btnDrawCircle = new JButton("Бразенхем окружность");
            JButton btnClear = new JButton("Очистить");

            JTextField startXField = new JTextField(10);
            JTextField startYField = new JTextField(10);
            JTextField endXField = new JTextField(10);
            JTextField endYField = new JTextField(10);

            JTextField centerXField = new JTextField(10);
            JTextField centerYField = new JTextField(10);
            JTextField radiusField = new JTextField(10);

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            add(btnBrasenham, gbc);

            gbc.gridy++;
            add(btnLinear, gbc);

            gbc.gridy++;
            add(btnDDA, gbc);

            gbc.gridy++;
            add(btnDrawCircle, gbc);

            gbc.gridy++;
            add(btnClear, gbc);

            gbc.gridwidth = 1;
            gbc.gridy++;
            gbc.gridx = 0;
            add(new JLabel("Начало (X, Y):"), gbc);

            gbc.gridx = 1;
            add(startXField, gbc);

            gbc.gridy++;
            gbc.gridx = 1;
            add(startYField, gbc);

            gbc.gridy++;
            gbc.gridx = 0;
            add(new JLabel("Конец (X, Y):"), gbc);

            gbc.gridx = 1;
            add(endXField, gbc);

            gbc.gridy++;
            gbc.gridx = 1;
            add(endYField, gbc);

            gbc.gridy++;
            gbc.gridx = 0;
            add(new JLabel("Центр (X, Y):"), gbc);

            gbc.gridx = 1;
            add(centerXField, gbc);

            gbc.gridy++;
            gbc.gridx = 1;
            add(centerYField, gbc);

            gbc.gridy++;
            gbc.gridx = 0;
            add(new JLabel("Радиус:"), gbc);

            gbc.gridx = 1;
            add(radiusField, gbc);

            btnBrasenham.addActionListener(e -> {
                try {
                    int x1 = Integer.parseInt(startXField.getText());
                    int y1 = Integer.parseInt(startYField.getText());
                    int x2 = Integer.parseInt(endXField.getText());
                    int y2 = Integer.parseInt(endYField.getText());
                    brasenhamLine(x1, y1, x2, y2);
                    repaint();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Введите корректные координаты!");
                }
            });

            btnDDA.addActionListener(e -> {
                try {
                    int x1 = Integer.parseInt(startXField.getText());
                    int y1 = Integer.parseInt(startYField.getText());
                    int x2 = Integer.parseInt(endXField.getText());
                    int y2 = Integer.parseInt(endYField.getText());
                    drawLineDDA(x1, y1, x2, y2);
                    repaint();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Введите корректные координаты!");
                }
            });

            btnLinear.addActionListener(e -> {
                try {
                    int x1 = Integer.parseInt(startXField.getText());
                    int y1 = Integer.parseInt(startYField.getText());
                    int x2 = Integer.parseInt(endXField.getText());
                    int y2 = Integer.parseInt(endYField.getText());
                    linearMethod(x1, x2, y1, y2);
                    repaint();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Введите корректные координаты!");
                }
            });

            btnDrawCircle.addActionListener(e -> {
                try {
                    int cx = Integer.parseInt(centerXField.getText());
                    int cy = Integer.parseInt(centerYField.getText());
                    int r = Integer.parseInt(radiusField.getText());
                    drawCircleBresenham(cx, cy, r);
                    repaint();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Введите корректные данные!");
                }
            });

            btnClear.addActionListener(e -> {
                clear();
            });
        }
    }

    public CoordinatePlane() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = (e.getX() / GRID_SIZE) * GRID_SIZE;
                int y = (e.getY() / GRID_SIZE) * GRID_SIZE;
                Point square = new Point(x, y);

                if (filledSquares.contains(square)) {
                    filledSquares.remove(square);
                } else {
                    //filledSquares.add(square);
                }
                repaint();
            }
        });

        JFrame frame2 = new JFrame("Coordinate Plane");
        SettingsPanel controlPanel = new SettingsPanel();

        frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame2.setSize(400, 600);
        frame2.add(controlPanel);
        frame2.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        int xOffset = (width / 2) / GRID_SIZE * GRID_SIZE;
        int yOffset = (height / 2) / GRID_SIZE * GRID_SIZE;

        g2d.setColor(Color.LIGHT_GRAY);
        for (int x = 0; x <= width; x += GRID_SIZE) {
            g2d.drawLine(x, 0, x, height);
        }
        for (int y = 0; y <= height; y += GRID_SIZE) {
            g2d.drawLine(0, y, width, y);
        }

        //оси
        g2d.setColor(Color.BLACK);
        g2d.drawLine(xOffset, 0, xOffset, height);
        g2d.drawLine(0, yOffset, width, yOffset);

        //единичные отрезки
        drawTicks(g2d, width, height, xOffset, yOffset);

        //закрашиваем квадраты
        g2d.setColor(Color.CYAN);
        for (Point p : filledSquares) {
            g2d.fillRect(p.x, p.y, GRID_SIZE, GRID_SIZE);
        }

        //границы квадратов
        g2d.setColor(Color.BLACK);
        for (Point p : filledSquares) {
            g2d.drawRect(p.x, p.y, GRID_SIZE, GRID_SIZE);
        }
    }

    private void drawTicks(Graphics2D g2d, int width, int height, int xOffset, int yOffset) {
        int tickSize = 5;

        //горизонтальные тики
        for (int x = 0; x <= width; x += GRID_SIZE) {
            if (x == xOffset) continue;
            g2d.drawLine(x, yOffset - tickSize, x, yOffset + tickSize);
        }

        //вертикальные тики
        for (int y = 0; y <= height; y += GRID_SIZE) {
            if (y == yOffset) continue;
            g2d.drawLine(xOffset - tickSize, y, xOffset + tickSize, y);
        }

        //числовые метки
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        for (int x = 0; x <= width; x += GRID_SIZE) {
            if (x == xOffset) continue;
            int unit = (x - xOffset) / GRID_SIZE;
            g2d.drawString(Integer.toString(unit), x - 10, yOffset - 10);
        }
        for (int y = 0; y <= height; y += GRID_SIZE) {
            if (y == yOffset) continue;
            int unit = (yOffset - y) / GRID_SIZE;
            g2d.drawString(Integer.toString(unit), xOffset + 10, y + 5);
        }
    }

    private void clear(){
        filledSquares.clear();
        repaint();
    }

    private void linearMethod(int x1, int x2, int y1, int y2) {
        double k = ((double) (y2 - y1)) / (x2 - x1);
        double b = y1 - k * x1;
        for (int x = x1; x <= x2; ++x) {
            double y = k * x + b;
            drawPoint(x, y);
        }
    }

    private void drawLineDDA(int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dy = y2 - y1;

        int steps = Math.max(Math.abs(dx), Math.abs(dy));

        double xStep = (double) dx / steps;
        double yStep = (double) dy / steps;

        double x = x1;
        double y = y1;

        for (int i = 0; i <= steps; i++) {
            drawPoint(x, y);
            x += xStep;
            y += yStep;
        }
    }

    private void brasenhamLine(int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dy = y2 - y1;

        int sx = dx >= 0 ? 1 : -1;
        int sy = dy >= 0 ? 1 : -1;

        dx = Math.abs(dx);
        dy = Math.abs(dy);

        boolean isSteep = dy > dx;

        if (isSteep) {
            int temp = dx;
            dx = dy;
            dy = temp;
        }

        double e = (double) dy / dx - 0.5;
        int x = x1;
        int y = y1;

        for (int i = 0; i <= dx; i++) {
            drawPoint(x, y);

            if (e >= 0) {
                if (isSteep) {
                    x += sx;
                } else {
                    y += sy;
                }
                e -= 1;
            }

            if (isSteep) {
                y += sy;
            } else {
                x += sx;
            }
            e += (double) dy / dx;
        }
    }

    private void drawCircleBresenham(int centerX, int centerY, int r) {
        int x = 0;
        int y = r;
        int d = 3 - 2 * r;

        while (x <= y) {
            drawSymmetricPoints(centerX, centerY, x, y);
            if (d >= 0) {

                d += 4 * (x - y) + 10;
                y--;
            } else {
                d += 4 * x + 6;
            }

            x++;
        }
    }

    private void drawSymmetricPoints(int centerX, int centerY, int x, int y) {
        drawPoint(centerX + x, centerY + y);
        drawPoint(centerX - x, centerY + y);
        drawPoint(centerX + x, centerY - y);
        drawPoint(centerX - x, centerY - y);
        drawPoint(centerX + y, centerY + x);
        drawPoint(centerX - y, centerY + x);
        drawPoint(centerX + y, centerY - x);
        drawPoint(centerX - y, centerY - x);
    }

    private void drawPoint(double x, double y) {
        int roundedY = (int) Math.round(y);
        int screenX = ((int) x) * GRID_SIZE + getWidth() / 2;
        int screenY = getHeight() / 2 - (roundedY + 1) * (GRID_SIZE);

        filledSquares.add(new Point(screenX / GRID_SIZE * GRID_SIZE, screenY / GRID_SIZE * GRID_SIZE));
        repaint();
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame("Coordinate Plane");
        CoordinatePlane plane = new CoordinatePlane();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.add(plane);
        frame.setVisible(true);
    }
}