package main;

import java.awt.Color;

import javax.swing.*;

@SuppressWarnings("serial")
public class ColorPicker extends JFrame {

	private final ColorPanel colorPanel;

	// Interfaz para notificar la selección de color
	public interface ColorSelectionListener {
		void colorSelected(Color color);
	}

	public ColorPicker(ColorSelectionListener listener) {
		setTitle("Selector de Colores");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(600, 400);
		setLocationRelativeTo(null);

		colorPanel = new ColorPanel();
		colorPanel.setColorSelectionListener(listener);
		add(colorPanel);

		setVisible(true);
	}

	public ColorPicker() {
		this(null);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new ColorPicker(null));
	}

}