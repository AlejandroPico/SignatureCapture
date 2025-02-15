package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

class ColorPanel extends JPanel {
    
	private BufferedImage colorSpectrum;
	private JLabel infoLabel;
	private Color selectedColor;
	private final int labelWidth = 120;
	private final int labelHeight = 40;
	private ColorPicker.ColorSelectionListener colorSelectionListener;

	public ColorPanel() {
	    setLayout(null);
	    initInfoLabel();
	    setupMouseListeners();
	}

	public void setColorSelectionListener(ColorPicker.ColorSelectionListener listener) {
	    this.colorSelectionListener = listener;
	}

	private void initInfoLabel() {
	    infoLabel = new JLabel("", SwingConstants.CENTER);
	    infoLabel.setOpaque(true);
	    infoLabel.setBackground(new Color(255, 255, 255, 200));
	    infoLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
	    infoLabel.setVisible(false);
	    infoLabel.setSize(labelWidth, labelHeight);
	    add(infoLabel);
	}

	private void setupMouseListeners() {
	    addMouseMotionListener(new MouseAdapter() {
	        @Override
	        public void mouseMoved(MouseEvent e) {
	            updateColorInfo(e.getX(), e.getY());
	            updateLabelPosition(e.getX(), e.getY());
	        }
	    });
	    
	    addMouseListener(new MouseAdapter() {
	        @Override
	        public void mouseExited(MouseEvent e) {
	            infoLabel.setVisible(false);
	        }
	        
	        @Override
	        public void mouseEntered(MouseEvent e) {
	            infoLabel.setVisible(true);
	        }
	        
	        @Override
	        public void mouseClicked(MouseEvent e) {
	            if (colorSpectrum != null && e.getX() < colorSpectrum.getWidth() && e.getY() < colorSpectrum.getHeight()) {
	                selectedColor = new Color(colorSpectrum.getRGB(e.getX(), e.getY()));
	                if (colorSelectionListener != null) {
	                    colorSelectionListener.colorSelected(selectedColor);
	                }
	                if (getTopLevelAncestor() instanceof JFrame) {
	                    ((JFrame)getTopLevelAncestor()).dispose();
	                }
	            }
	        }
	    });
	}

	@Override
	protected void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    createColorSpectrum();
	    g.drawImage(colorSpectrum, 0, 0, getWidth(), getHeight(), this);
	}

	private void createColorSpectrum() {
	    if (colorSpectrum == null || 
	        colorSpectrum.getWidth() != getWidth() || 
	        colorSpectrum.getHeight() != getHeight()) {
	        
	        colorSpectrum = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
	        
	        for (int x = 0; x < getWidth(); x++) {
	            float hue = (float) x / (getWidth() - 1);
	            for (int y = 0; y < getHeight(); y++) {
	                float brightness = 1.0f - (float) y / (getHeight() - 1);
	                Color color = Color.getHSBColor(hue, 1.0f, brightness);
	                colorSpectrum.setRGB(x, y, color.getRGB());
	            }
	        }
	    }
	}

	private void updateColorInfo(int x, int y) {
	    if (colorSpectrum != null && x >= 0 && x < getWidth() && y >= 0 && y < getHeight()) {
	        Color color = new Color(colorSpectrum.getRGB(x, y));
	        String hex = String.format("#%02X%02X%02X", 
	            color.getRed(), 
	            color.getGreen(), 
	            color.getBlue());
	        
	        String text = String.format("<html>RGB: %d, %d, %d<br>HEX: %s</html>",
	            color.getRed(),
	            color.getGreen(),
	            color.getBlue(),
	            hex);
	        
	        infoLabel.setText(text);
	    }
	}

	private void updateLabelPosition(int x, int y) {
	    int posX = x + 15;
	    int posY = y + 15;
	    
	    if (posX + labelWidth > getWidth()) {
	        posX = x - labelWidth - 5;
	    }
	    if (posY + labelHeight > getHeight()) {
	        posY = y - labelHeight - 5;
	    }
	    
	    infoLabel.setLocation(posX, posY);
	}

	public Color getSelectedColor() {
	    return selectedColor;
	}
	
}