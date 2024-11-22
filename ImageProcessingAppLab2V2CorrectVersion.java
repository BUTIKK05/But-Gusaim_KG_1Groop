import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageProcessingAppLab2V2CorrectVersion {
    private JFrame window;
    private JLabel imageLabel;
    private BufferedImage currentImage;

    private JPanel histogramPanel;
    private int[] originalHistogram;
    private int[] equalizedHistogram;

    public ImageProcessingAppLab2V2CorrectVersion() {
        window = new JFrame("Обработка и сжатие изображений");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(1500, 900);

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JButton loadButton = new JButton("Загрузить");
        JButton highPassFilterButton = new JButton("Высокочастотный фильтр");
        JButton histogramButton = new JButton("Построение и эквализация гистограммы изображения");
        JButton contrastButton = new JButton("Линейное контрастирование");
        JButton compressButton = new JButton("Сжать изображение (JPEG)");

        panel.add(loadButton);
        panel.add(highPassFilterButton);
        panel.add(histogramButton);
        panel.add(contrastButton);
        panel.add(compressButton);

        window.add(panel, BorderLayout.NORTH);

        imageLabel = new JLabel();
        window.add(new JScrollPane(imageLabel), BorderLayout.CENTER);

        histogramPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawHistogram(g, originalHistogram, "До эквализации", 20, 20);
                drawHistogram(g, equalizedHistogram, "После эквализации", 10, getHeight() / 2 + 20);
            }
        };
        histogramPanel.setPreferredSize(new Dimension(500, 900));
        window.add(histogramPanel, BorderLayout.EAST);

        loadButton.addActionListener(e -> loadImage());
        highPassFilterButton.addActionListener(e -> sharpness());
        histogramButton.addActionListener(e -> histogramEqualization());
        contrastButton.addActionListener(e -> linearContrast());
        compressButton.addActionListener(e -> compressImage());

        window.setVisible(true);
    }

    private void loadImage() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "jpg", "png", "bmp");
        fileChooser.setFileFilter(filter);
        int result = fileChooser.showOpenDialog(window);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                currentImage = ImageIO.read(selectedFile);
                showImage(currentImage);
                originalHistogram = null;
                equalizedHistogram = null;
                histogramPanel.repaint();
            } catch (IOException ex) {
                showMessage("Ошибка при загрузке фотографии: " + ex.getMessage());
            }
        }
    }

    private void sharpness() {
        if (currentImage == null) {
            showMessage("Загрузите фотографию");
            return;
        }
        float[] kernel = {
                0, -1, 0,
                -1, 5, -1,
                0, -1, 0
        };
        currentImage = applyConvolution(currentImage, kernel, 3);
        showImage(currentImage);
    }

    private void histogramEqualization() {
        if (currentImage == null) {
            showMessage("Загрузите фотографию");
            return;
        }
        originalHistogram = computeHistogram(currentImage);

        currentImage = equalizeHistogram(currentImage);

        equalizedHistogram = computeHistogram(currentImage);

        histogramPanel.repaint();
        showImage(currentImage);
    }

    private void linearContrast() {
        if (currentImage == null) {
            showMessage("Загрузите фотографию");
            return;
        }
        currentImage = changeContrast(currentImage);
        showImage(currentImage);
    }

    private void compressImage() {
        if (currentImage == null) {
            showMessage("Загрузите фотографию");
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("JPEG Files", "jpg"));
        int result = fileChooser.showSaveDialog(window);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                ImageIO.write(currentImage, "jpg", selectedFile);
                showMessage("Фотография сохранена");
            } catch (IOException ex) {
                showMessage("Ошибка сохранения фотографии: " + ex.getMessage());
            }
        }
    }

    private void showImage(BufferedImage image) {
        ImageIcon imageIcon = new ImageIcon(image);
        imageLabel.setIcon(imageIcon);
        window.revalidate();
    }

    private BufferedImage applyConvolution(BufferedImage image, float[] kernel, int kernelSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, image.getType());

        int offset = kernelSize / 2;

        for (int y = offset; y < height - offset; y++) {
            for (int x = offset; x < width - offset; x++) {
                float[] rgb = new float[3];
                for (int ky = -offset; ky <= offset; ky++) {
                    for (int kx = -offset; kx <= offset; kx++) {
                        int pixel = image.getRGB(x + kx, y + ky);
                        int kernelIndex = (ky + offset) * kernelSize + (kx + offset);
                        rgb[0] += ((pixel >> 16) & 0xFF) * kernel[kernelIndex];
                        rgb[1] += ((pixel >> 8) & 0xFF) * kernel[kernelIndex];
                        rgb[2] += (pixel & 0xFF) * kernel[kernelIndex];
                    }
                }
                int r = Math.min(255, Math.max(0, Math.round(rgb[0])));
                int g = Math.min(255, Math.max(0, Math.round(rgb[1])));
                int b = Math.min(255, Math.max(0, Math.round(rgb[2])));
                result.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return result;
    }

    private int[] computeHistogram(BufferedImage image) {
        int[] histogram = new int[256];
        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int gray = (int) (0.3 * ((rgb >> 16) & 0xFF) +
                        0.59 * ((rgb >> 8) & 0xFF) +
                        0.11 * (rgb & 0xFF));
                histogram[gray]++;
            }
        }
        return histogram;
    }

    private BufferedImage equalizeHistogram(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int[] histogram = computeHistogram(image);
        int[] cumulative = new int[256];
        cumulative[0] = histogram[0];

        for (int i = 1; i < 256; i++) {
            cumulative[i] = cumulative[i - 1] + histogram[i];
        }

        int totalPixels = width * height;
        int[] equalized = new int[256];
        for (int i = 0; i < 256; i++) {
            equalized[i] = (cumulative[i] * 255) / totalPixels;
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int gray = (int) (0.3 * ((rgb >> 16) & 0xFF) +
                        0.59 * ((rgb >> 8) & 0xFF) +
                        0.11 * (rgb & 0xFF));
                int newGray = equalized[gray];
                res.setRGB(x, y, (newGray << 16) | (newGray << 8) | newGray);
            }
        }

        return res;
    }

    private void drawHistogram(Graphics g, int[] histogram, String title, int x, int y) {
        if (histogram == null) return;

        int width = 256;
        int height = 220;
        g.drawString(title, x + 10, y - 10);

        int max = 0;
        for (int value : histogram) {
            max = Math.max(max, value);
        }

        for (int i = 0; i < width; i++) {
            int height_ = (int) ((double) histogram[i] / max * height);
            g.fillRect(x + i, y + height - height_, 1, height_);
        }
    }
    private BufferedImage changeContrast(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage res = new BufferedImage(width, height, image.getType());

        int min = 255;
        int max = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int gray = (int)(0.3 * ((rgb >> 16) & 0xFF) + 0.59 * ((rgb >> 8) & 0xFF) + 0.11 * (rgb & 0xFF));
                min = Math.min(min, gray);
                max = Math.max(max, gray);
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = ((rgb >> 16) & 0xFF - min) * 255 / (max - min);
                int g = ((rgb >> 8) & 0xFF - min) * 255 / (max - min);
                int b = ((rgb & 0xFF - min) * 255 / (max - min));
                res.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }

        return res;
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(window, message);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ImageProcessingAppLab2V2CorrectVersion::new);
    }
}
