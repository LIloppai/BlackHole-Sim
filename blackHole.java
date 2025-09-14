//made by: LilO

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class blackHole extends JPanel {
    private int blackHoleX = 400;
    private int blackHoleY = 300;
    private int blackHoleRadius = 70;
    private Timer timer;
    private Random random = new Random();
    
    // Star field
    private int[][] stars;
    private int numStars = 1000;
    
    // Accretion disk particles
    private double[][] accretionParticles;
    private int numParticles = 500;
    
    // Pixelation factor
    private int pixelSize = 2;
    
    // Buffer for pixelated rendering
    private BufferedImage pixelBuffer;
    private Graphics2D bufferGraphics;
    
    // Time counter for animations
    private float time = 0;
    
    // Color palettes for artistic effect
    private Color[] palette1 = {new Color(10, 10, 40), new Color(30, 20, 80), new Color(120, 30, 150), new Color(220, 50, 130), new Color(255, 100, 80)};
    private Color[] palette2 = {new Color(0, 0, 30), new Color(0, 30, 100), new Color(0, 150, 200), new Color(100, 200, 255), new Color(220, 220, 255)};
    private Color[] palette3 = {new Color(30, 0, 20), new Color(100, 0, 80), new Color(180, 40, 120), new Color(230, 100, 100), new Color(255, 200, 150)};
    
    private Color[][] palettes = {palette1, palette2, palette3};
    private int currentPalette = 0;
    
    public blackHole() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        
        // Initialize pixel buffer
        pixelBuffer = new BufferedImage(800/pixelSize, 600/pixelSize, BufferedImage.TYPE_INT_RGB);
        bufferGraphics = pixelBuffer.createGraphics();
        bufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        
        // Initialize star field
        stars = new int[numStars][4]; // x, y, brightness, twinkle speed
        for (int i = 0; i < numStars; i++) {
            stars[i][0] = random.nextInt(800/pixelSize);
            stars[i][1] = random.nextInt(600/pixelSize);
            stars[i][2] = 100 + random.nextInt(155); // brightness
            stars[i][3] = random.nextInt(5) + 1; // twinkle speed
        }
        
        // Initialize accretion disk particles
        accretionParticles = new double[numParticles][6]; // angle, distance, size, speed, temperature, life
        for (int i = 0; i < numParticles; i++) {
            accretionParticles[i][0] = random.nextDouble() * 2 * Math.PI;
            accretionParticles[i][1] = blackHoleRadius/pixelSize + 8 + random.nextDouble() * 60;
            accretionParticles[i][2] = 1 + random.nextDouble() * 4;
            accretionParticles[i][3] = 0.01 + random.nextDouble() * 0.05;
            accretionParticles[i][4] = 0.2 + random.nextDouble() * 0.8; // temperature (0-1)
            accretionParticles[i][5] = 0.5 + random.nextDouble() * 0.5; // life (0-1)
        }
        
        timer = new Timer(30, e -> {
            time += 0.05f;
            updateAnimation();
            repaint();
        });
        timer.start();
        
        // Change palette every 10 seconds
        Timer paletteTimer = new Timer(10000, e -> {
            currentPalette = (currentPalette + 1) % palettes.length;
        });
        paletteTimer.start();
    }
    
    private void updateAnimation() {
        // Update accretion disk particles
        for (int i = 0; i < numParticles; i++) {
            accretionParticles[i][0] += accretionParticles[i][3];
            
            // Pulsate particle life
            accretionParticles[i][5] = 0.5 + 0.5 * Math.sin(time * 0.5 + i * 0.1);
            
            // Occasionally change particle temperature
            if (random.nextInt(200) < 2) {
                accretionParticles[i][4] = 0.2 + random.nextDouble() * 0.8;
            }
        }
        
        // Update star twinkling
        for (int i = 0; i < numStars; i++) {
            stars[i][2] = (int) (100 + 55 * Math.sin(time * 0.1 * stars[i][3] + i));
            if (stars[i][2] < 0) stars[i][2] = 0;
            if (stars[i][2] > 255) stars[i][2] = 255;
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Clear buffer
        bufferGraphics.setColor(Color.BLACK);
        bufferGraphics.fillRect(0, 0, 800/pixelSize, 600/pixelSize);
        
        // Draw stars with lensing effect
        drawStars(bufferGraphics);
        
        // Draw accretion disk
        drawAccretionDisk(bufferGraphics);
        
        // Draw black hole
        drawBlackHole(bufferGraphics);
        
        // Draw the pixelated buffer to the screen
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(pixelBuffer, 0, 0, 800, 600, null);
        
        // Draw artistic filters
        drawArtisticFilters(g2d);
    }
    
    private void drawStars(Graphics2D g) {
        for (int i = 0; i < numStars; i++) {
            int x = stars[i][0];
            int y = stars[i][1];
            int brightness = stars[i][2];
            
            // Calculate distance to black hole (in buffer coordinates)
            double dx = x - blackHoleX/pixelSize;
            double dy = y - blackHoleY/pixelSize;
            double distance = Math.sqrt(dx*dx + dy*dy);
            
            // Apply gravitational lensing if star is near black hole
            if (distance < 100) {
                double angle = Math.atan2(dy, dx);
                double distortion = (100 - distance) / 100 * 20; // Max 20px distortion
                
                // Draw stretched star (pixelated)
                int distortedX = (int) (x + Math.cos(angle) * distortion);
                int distortedY = (int) (y + Math.sin(angle) * distortion);
                
                g.setColor(new Color(brightness, brightness, brightness, 150));
                g.drawLine(x, y, distortedX, distortedY);
                
                // Add secondary distortion for artistic effect
                if (distance < 70) {
                    int distortedX2 = (int) (x + Math.cos(angle + 0.5) * distortion * 0.7);
                    int distortedY2 = (int) (y + Math.sin(angle + 0.5) * distortion * 0.7);
                    g.drawLine(x, y, distortedX2, distortedY2);
                }
            } else {
                // Draw normal star (pixelated)
                g.setColor(new Color(brightness, brightness, brightness));
                g.fillRect(x, y, 1, 1);
                
                // Add occasional colorful stars
                if (brightness > 200 && random.nextInt(100) < 5) {
                    Color starColor = getColorFromPalette((brightness % 100) / 100.0f);
                    g.setColor(new Color(starColor.getRed(), starColor.getGreen(), starColor.getBlue(), 150));
                    g.fillRect(x, y, 2, 2);
                }
            }
        }
    }
    
    private void drawAccretionDisk(Graphics2D g) {
        Color[] palette = palettes[currentPalette];
        
        for (int i = 0; i < numParticles; i++) {
            double angle = accretionParticles[i][0];
            double distance = accretionParticles[i][1];
            double size = accretionParticles[i][2];
            double temperature = accretionParticles[i][4];
            double life = accretionParticles[i][5];
            
            // Calculate position (in buffer coordinates)
            int x = (int) (blackHoleX/pixelSize + Math.cos(angle) * distance);
            int y = (int) (blackHoleY/pixelSize + Math.sin(angle) * distance);
            
            // Get color from palette based on temperature and life
            float colorPos = (float) (temperature * life);
            Color color = getColorFromPalette(colorPos);
            
            g.setColor(color);
            
            // Draw pixelated particle with size based on life
            int particleSize = (int) Math.max(1, size * life);
            g.fillRect(x, y, particleSize, particleSize);
            
            // Draw Doppler effect (brighter on approaching side)
            if (Math.cos(angle) > 0) { // Approaching side
                g.setColor(new Color(255, 255, 255, (int)(100 * life)));
                g.fillRect(x, y, particleSize, particleSize);
            }
            
            // Draw particle trail for artistic effect
            if (life > 0.7) {
                int trailX = (int) (x - Math.cos(angle) * 3);
                int trailY = (int) (y - Math.sin(angle) * 3);
                g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(100 * life)));
                g.fillRect(trailX, trailY, particleSize/2, particleSize/2);
            }
        }
        
        // Draw spiral arms for artistic effect
        g.setColor(getColorFromPalette(0.8f));
        for (int i = 0; i < 3; i++) {
            double spiralStart = time * 0.1 + i * 2 * Math.PI / 3;
            for (int j = 0; j < 50; j++) {
                double distance = blackHoleRadius/pixelSize + 10 + j * 1.2;
                double angle = spiralStart + j * 0.1;
                int x = (int) (blackHoleX/pixelSize + Math.cos(angle) * distance);
                int y = (int) (blackHoleY/pixelSize + Math.sin(angle) * distance);
                g.fillRect(x, y, 2, 2);
            }
        }
    }
    
    private void drawBlackHole(Graphics2D g) {
        int centerX = blackHoleX/pixelSize;
        int centerY = blackHoleY/pixelSize;
        int radius = blackHoleRadius/pixelSize;
        
        // Draw photon sphere (pixelated with artistic colors)
        Color[] palette = palettes[currentPalette];
        for (int i = 0; i < 360; i += 3) {
            double angle = Math.toRadians(i);
            int x = (int) (centerX + Math.cos(angle) * radius);
            int y = (int) (centerY + Math.sin(angle) * radius);
            
            float colorPos = (float) (0.3 + 0.4 * Math.sin(time * 0.2 + angle * 2));
            g.setColor(getColorFromPalette(colorPos));
            g.fillRect(x, y, 2, 2);
        }
        
        // Draw event horizon (black hole with subtle color variations)
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int y = centerY - radius; y <= centerY + radius; y++) {
                double distance = Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2));
                if (distance <= radius) {
                    // Add subtle color variations
                    float noise = (float) (0.9 + 0.1 * Math.sin(x * 0.2 + y * 0.2 + time * 0.5));
                    g.setColor(new Color(
                        (int)(10 * noise),
                        (int)(5 * noise),
                        (int)(20 * noise)
                    ));
                    g.fillRect(x, y, 1, 1);
                }
            }
        }
        
        // Draw gravitational lensing effect (warped space)
        for (int i = radius; i < radius * 3; i += 2) {
            float alpha = (float) (1.0 - (i - radius) / (radius * 2.0));
            float colorPos = (float) (0.2 + 0.3 * Math.sin(time * 0.1 + i * 0.05));
            Color lensColor = getColorFromPalette(colorPos);
            g.setColor(new Color(lensColor.getRed(), lensColor.getGreen(), lensColor.getBlue(), (int)(alpha * 100)));
            
            for (int j = 0; j < 360; j += 8) {
                double angle = Math.toRadians(j);
                int x = (int) (centerX + Math.cos(angle) * i);
                int y = (int) (centerY + Math.sin(angle) * i);
                g.fillRect(x, y, 2, 2);
            }
        }
    }
    
    private void drawArtisticFilters(Graphics2D g) {
        // Add vignette effect
        GradientPaint vignette = new GradientPaint(
            0, 0, new Color(0, 0, 0, 0),
            800, 600, new Color(0, 0, 0, 150),
            true
        );
        g.setPaint(vignette);
        g.fillRect(0, 0, 800, 600);
        
        // Add scan lines for retro effect
        g.setColor(new Color(0, 0, 0, 20));
        for (int y = 0; y < 600; y += 2) {
            g.drawLine(0, y, 800, y);
        }
        
        // Add film grain
        g.setColor(new Color(255, 255, 255, 5));
        for (int i = 0; i < 500; i++) {
            int x = random.nextInt(800);
            int y = random.nextInt(600);
            g.fillRect(x, y, 1, 1);
        }
        
        // Add title
        g.setColor(new Color(200, 200, 255, 150));
        g.setFont(new Font("Monospaced", Font.BOLD, 24));
        g.drawString("COSMIC VOID", 300, 50);
        
        // Add palette name
        String[] paletteNames = {"NEBULA", "QUASAR", "SUPERNOVA"};
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g.drawString("PALETTE: " + paletteNames[currentPalette], 20, 580);
    }
    
    private Color getColorFromPalette(float position) {
        if (position < 0) position = 0;
        if (position > 1) position = 1;
        
        Color[] palette = palettes[currentPalette];
        int colorCount = palette.length;
        float segment = 1.0f / (colorCount - 1);
        
        int index = (int) (position / segment);
        if (index >= colorCount - 1) {
            return palette[colorCount - 1];
        }
        
        float localPosition = (position - index * segment) / segment;
        Color color1 = palette[index];
        Color color2 = palette[index + 1];
        
        int r = (int) (color1.getRed() + localPosition * (color2.getRed() - color1.getRed()));
        int g = (int) (color1.getGreen() + localPosition * (color2.getGreen() - color1.getGreen()));
        int b = (int) (color1.getBlue() + localPosition * (color2.getBlue() - color1.getBlue()));
        
        return new Color(r, g, b);
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Artistic Black Hole");
        blackHole simulation = new blackHole();
        
        frame.add(simulation);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}