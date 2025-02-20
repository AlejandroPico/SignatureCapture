package main;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.filechooser.*;

@SuppressWarnings("serial")
public class SignatureCapture extends JFrame { private DrawPanel drawPanel; 

	private JButton saveButton, openButton, newButton;
	private JButton bgButton, undoButton, clearButton;
	private JButton colorButton;
	private JPanel colorHistoryPanel;
	private List<Color> colorHistory = new ArrayList<>();
	private JToggleButton textModeToggle;
	private JToggleButton boldButton, italicButton, underlineButton;
	private JComboBox<Integer> fontSizeCombo;
	private Integer buttonSize = 20;

    public SignatureCapture() {
        setTitle("Capturador de Firmas");
        setSize(1000, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel de dibujo
        drawPanel = new DrawPanel();
        // Usamos layout absoluto en el drawPanel para posicionar los JTextArea de texto
        drawPanel.setLayout(null);
        add(drawPanel, BorderLayout.CENTER);

        // ───────── Grupo Archivo ─────────
        saveButton = createIconButton("/icons/save.png", "Guardar", "Guardar firma");
        openButton = createIconButton("/icons/open.png", "Abrir", "Abrir firma guardada");
        newButton = createIconButton("/icons/new.png", "Nuevo", "Nueva firma (limpia todo)");
        
        // ───────── Grupo Acciones ─────────
        bgButton = createIconButton("/icons/background.png", "Fondo", "Seleccionar fondo");
        undoButton = createIconButton("/icons/undo.png", "Deshacer", "Deshacer último trazo");
        clearButton = createIconButton("/icons/clear.png", "Borrar", "Borrar todo");
        
        // ───────── Grupo Texto ─────────
        // Botón toggle para modo texto
        textModeToggle = new JToggleButton();
        {
            JButton temp = createIconButton("/icons/text.png", "Txt", "Modo Texto");
            Icon txtIcon = temp.getIcon();
            if (txtIcon != null) {
                textModeToggle.setIcon(txtIcon);
            } else {
                textModeToggle.setText("Modo Texto");
            }
            textModeToggle.setToolTipText("Activar/Desactivar modo texto");
            textModeToggle.setMargin(new Insets(2,2,2,2));
        }
        boldButton = createToggleIconButton("/icons/bold.png", "B", "Negrita");
        italicButton = createToggleIconButton("/icons/italic.png", "I", "Cursiva");
        underlineButton = createToggleIconButton("/icons/underline.png", "U", "Subrayado");
        // Selector de tamaño (más ancho para visualizar el valor)
        Integer[] sizes = {10, 12, 14, 16, 18, 20, 24, 28, 32, 48};
        fontSizeCombo = new JComboBox<>(sizes);
        fontSizeCombo.setEditable(true);
        fontSizeCombo.setPreferredSize(new Dimension(80, fontSizeCombo.getPreferredSize().height));
        colorButton = createIconButton("/icons/color.png", "Color", "Seleccionar color");
        // Historial de color (con recuadros ligeramente más pequeños)
        colorHistoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        colorHistoryPanel.setBorder(new TitledBorder("Historial de color"));
        colorHistoryPanel.setPreferredSize(new Dimension(150, 40));
        
        // ───────── Agrupaciones de la barra de controles ─────────
        // Grupo Archivo: Guardar, Abrir y Nuevo
        JPanel archivoGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        archivoGroup.add(saveButton);
        archivoGroup.add(openButton);
        archivoGroup.add(newButton);
        archivoGroup.setBorder(new TitledBorder("Archivo"));
        
        // Grupo Acciones: Fondo, Deshacer y Borrar
        JPanel accionesGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        accionesGroup.add(bgButton);
        accionesGroup.add(undoButton);
        accionesGroup.add(clearButton);
        accionesGroup.setBorder(new TitledBorder("Acciones"));
        
        // Grupo Texto: Modo Texto, Negrita, Cursiva, Subrayado, Tamaño, Color y Historial
        JPanel textoGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        textoGroup.add(textModeToggle);
        textoGroup.add(boldButton);
        textoGroup.add(italicButton);
        textoGroup.add(underlineButton);
        textoGroup.add(fontSizeCombo);
        textoGroup.add(colorButton);
        textoGroup.add(colorHistoryPanel);
        textoGroup.setBorder(new TitledBorder("Texto"));
        
        // Panel contenedor superior con los tres grupos ordenados de izquierda a derecha
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel groupsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        groupsPanel.add(archivoGroup);
        groupsPanel.add(accionesGroup);
        groupsPanel.add(textoGroup);
        topPanel.add(groupsPanel, BorderLayout.CENTER);
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(topPanel, BorderLayout.NORTH);
        
        // ───────── Configuración de eventos ─────────
        // Grupo Texto: formato
        boldButton.addActionListener(e -> {});
        italicButton.addActionListener(e -> {});
        underlineButton.addActionListener(e -> {});
        fontSizeCombo.addActionListener(e -> {});
        colorButton.addActionListener(e -> showColorPicker());
        // Al activar/desactivar el modo texto, se inicia o se comitea el texto en el drawPanel
        textModeToggle.addActionListener(e -> {
            boolean mode = textModeToggle.isSelected();
            if (!mode) {
                drawPanel.commitActiveText();
            }
            drawPanel.setTextMode(mode);
        });
        
        // Grupo Acciones
        bgButton.addActionListener(e -> selectBackgroundImage());
        undoButton.addActionListener(e -> drawPanel.undoLastStrokeGroup());
        clearButton.addActionListener(e -> drawPanel.clearAll());
        
        // Grupo Archivo
        saveButton.addActionListener(e -> saveImage());
        openButton.addActionListener(e -> openImage());
        newButton.addActionListener(e -> newSignature());
    }

    // Crea un botón con icono cargado desde la ruta indicada; si falla, usa un texto de respaldo
    private JButton createIconButton(String iconPath, String fallbackText, String tooltip) {
        JButton button = new JButton();
        java.net.URL url = getClass().getResource(iconPath);
        if (url != null) {
            ImageIcon icon = new ImageIcon(url);
            // Escalar la imagen a 32x32 píxeles (ajusta el tamaño según lo necesites)
            Image scaledImage = icon.getImage().getScaledInstance(buttonSize, buttonSize, Image.SCALE_SMOOTH);
            icon = new ImageIcon(scaledImage);
            button.setIcon(icon);
        } else {
            button.setText(fallbackText);
        }
        button.setToolTipText(tooltip);
        button.setMargin(new Insets(2, 2, 2, 2));
        return button;
    }

    // Crea un JToggleButton con icono
    private JToggleButton createToggleIconButton(String iconPath, String fallbackText, String tooltip) {
        JToggleButton button = new JToggleButton();
        java.net.URL url = getClass().getResource(iconPath);
        if (url != null) {
            ImageIcon icon = new ImageIcon(url);
            Image scaledImage = icon.getImage().getScaledInstance(buttonSize, buttonSize, Image.SCALE_SMOOTH);
            icon = new ImageIcon(scaledImage);
            button.setIcon(icon);
        } else {
            button.setText(fallbackText);
        }
        button.setToolTipText(tooltip);
        button.setMargin(new Insets(2, 2, 2, 2));
        return button;
    }

    // Retorna la fuente actual configurada en los controles de Texto
    public Font getCurrentTextFont() {
        int style = Font.PLAIN;
        if (boldButton.isSelected()) style |= Font.BOLD;
        if (italicButton.isSelected()) style |= Font.ITALIC;
        // Para subrayado no existe un estilo en Font, se omite en el JTextArea
        int size = Integer.parseInt(fontSizeCombo.getSelectedItem().toString());
        return new Font("SansSerif", style, size);
    }

    private void showColorPicker() {
        new ColorPicker(new ColorPicker.ColorSelectionListener() {
            @Override
            public void colorSelected(Color color) {
                // Se actualiza el color de dibujo y se agrega al historial
                drawPanel.setDrawingColor(color);
                addColorToHistory(color);
            }
        });
    }

    private void addColorToHistory(Color color) {
        if (!colorHistory.contains(color)) {
            colorHistory.add(color);
            updateColorHistoryPanel();
        }
    }

    private void updateColorHistoryPanel() {
        colorHistoryPanel.removeAll();
        for (Color c : colorHistory) {
            JButton btn = new JButton();
            btn.setBackground(c);
            btn.setPreferredSize(new Dimension(18, 18));
            btn.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            btn.addActionListener(e -> {
                drawPanel.setDrawingColor(c);
            });
            colorHistoryPanel.add(btn);
        }
        colorHistoryPanel.revalidate();
        colorHistoryPanel.repaint();
    }

    private void selectBackgroundImage() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png", "gif");
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Image bg = new ImageIcon(chooser.getSelectedFile().getPath()).getImage();
                drawPanel.clearAll();
                drawPanel.setBackgroundImage(bg);
                drawPanel.repaint();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al cargar la imagen");
            }
        }
    }

    private void openImage() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png", "gif");
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedImage img = ImageIO.read(chooser.getSelectedFile());
                drawPanel.clearAll();
                drawPanel.setBackgroundImage(img);
                drawPanel.repaint();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al abrir la imagen");
            }
        }
    }

    private void newSignature() {
        if (drawPanel.isModified()) {
            int resp = JOptionPane.showConfirmDialog(this, "¿Desea guardar la firma actual antes de crear una nueva?", "Confirmar", JOptionPane.YES_NO_CANCEL_OPTION);
            if (resp == JOptionPane.CANCEL_OPTION || resp == JOptionPane.CLOSED_OPTION) {
                return;
            } else if (resp == JOptionPane.YES_OPTION) {
                saveImage();
            }
        }
        drawPanel.clearAll();
        drawPanel.setBackgroundImage(null);
        drawPanel.repaint();
    }

    private void saveImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar firma");
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                // Se pinta el panel completo en una imagen
                BufferedImage fullImage = new BufferedImage(drawPanel.getWidth(), drawPanel.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D gFull = fullImage.createGraphics();
                drawPanel.paint(gFull);
                gFull.dispose();
                
                // Se calcula el rectángulo mínimo que contiene la firma
                Rectangle bounds = drawPanel.getSignatureBounds();
                BufferedImage finalImage = fullImage.getSubimage(bounds.x, bounds.y, bounds.width, bounds.height);
                
                File file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".png")) {
                    file = new File(file.getParentFile(), file.getName() + ".png");
                }
                ImageIO.write(finalImage, "PNG", file);
                JOptionPane.showMessageDialog(this, "Firma guardada exitosamente");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al guardar: " + e.getMessage());
            }
        }
    }

    // ───────── Panel de dibujo con trazos y texto (modo texto in situ) ─────────
    class DrawPanel extends JPanel {
        private List<List<ColoredShape>> strokeGroups = new ArrayList<>();
        private List<ColoredShape> currentStroke = null;
        private List<TextItem> texts = new ArrayList<>();
        private Point lastPoint;
        private Image bgImage;
        private Color drawingColor = Color.BLACK;
        private boolean textMode = false;
        // Variables para selección y entrada de texto in situ
        private boolean selectingText = false;
        private Point textStartPoint;
        private Rectangle currentTextRect;
        private JTextArea activeTextArea;
        
        public DrawPanel() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(Color.GRAY));
            
            // Mouse listener unificado para dibujo y modo texto
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    if (textMode) {
                        // Si no hay un cuadro activo, iniciar selección de área
                        if (activeTextArea == null) {
                            textStartPoint = e.getPoint();
                            selectingText = true;
                        }
                    } else {
                        // Dibujo de trazos
                        currentStroke = new ArrayList<>();
                        lastPoint = e.getPoint();
                    }
                }
                public void mouseReleased(MouseEvent e) {
                    if (textMode && selectingText) {
                        selectingText = false;
                        if (currentTextRect != null && currentTextRect.width > 10 && currentTextRect.height > 10) {
                            activeTextArea = new JTextArea();
                            activeTextArea.setLineWrap(true);
                            activeTextArea.setWrapStyleWord(true);
                            activeTextArea.setFont(SignatureCapture.this.getCurrentTextFont());
                            activeTextArea.setForeground(drawingColor);
                            activeTextArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                            activeTextArea.setBounds(currentTextRect);
                            DrawPanel.this.add(activeTextArea);
                            activeTextArea.requestFocusInWindow();
                            repaint();
                        }
                        currentTextRect = null;
                    } else if (!textMode && currentStroke != null && !currentStroke.isEmpty()) {
                        strokeGroups.add(currentStroke);
                        currentStroke = null;
                    }
                }
            });
            
            addMouseMotionListener(new MouseAdapter() {
                public void mouseDragged(MouseEvent e) {
                    if (textMode && selectingText) {
                        Point current = e.getPoint();
                        int x = Math.min(textStartPoint.x, current.x);
                        int y = Math.min(textStartPoint.y, current.y);
                        int w = Math.abs(current.x - textStartPoint.x);
                        int h = Math.abs(current.y - textStartPoint.y);
                        currentTextRect = new Rectangle(x, y, w, h);
                        repaint();
                    } else if (!textMode && lastPoint != null) {
                        Line2D.Float line = new Line2D.Float(lastPoint.x, lastPoint.y, e.getX(), e.getY());
                        currentStroke.add(new ColoredShape(line, drawingColor));
                        lastPoint = e.getPoint();
                        repaint();
                    }
                }
            });
        }
        
        public void setTextMode(boolean mode) {
            this.textMode = mode;
            // Si se desactiva el modo texto, se comitea cualquier cuadro activo
            if (!mode) {
                commitActiveText();
            }
        }
        
        public void setBackgroundImage(Image img) {
            this.bgImage = img;
        }
        
        public void setDrawingColor(Color color) {
            this.drawingColor = color;
        }
        
        public void undoLastStrokeGroup() {
            if (!strokeGroups.isEmpty()) {
                strokeGroups.remove(strokeGroups.size() - 1);
                repaint();
            }
        }
        
        public void clearAll() {
            strokeGroups.clear();
            texts.clear();
            // Eliminar cualquier cuadro de texto activo
            if (activeTextArea != null) {
                remove(activeTextArea);
                activeTextArea = null;
            }
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            if (bgImage != null) {
                g2d.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            }
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Dibujar cada grupo de trazos
            for (List<ColoredShape> group : strokeGroups) {
                for (ColoredShape cs : group) {
                    g2d.setColor(cs.color);
                    g2d.draw(cs.shape);
                }
            }
            // Dibujar trazos en curso
            if (currentStroke != null) {
                for (ColoredShape cs : currentStroke) {
                    g2d.setColor(cs.color);
                    g2d.draw(cs.shape);
                }
            }
            // Dibujar textos ya comprometidos
            for (TextItem ti : texts) {
                g2d.setFont(ti.font);
                g2d.setColor(ti.color);
                g2d.drawString(ti.text, ti.x, ti.y);
            }
            // Si se está seleccionando un área para texto, dibujar el rectángulo de selección
            if (textMode && selectingText && currentTextRect != null) {
                g2d.setColor(Color.GRAY);
                Stroke oldStroke = g2d.getStroke();
                g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0));
                g2d.draw(currentTextRect);
                g2d.setStroke(oldStroke);
            }
            g2d.dispose();
        }
        
        // Calcula el rectángulo mínimo que contiene todos los trazos y textos, con un margen máximo de 50 px
        public Rectangle getSignatureBounds() {
            Rectangle bounds = null;
            for (List<ColoredShape> group : strokeGroups) {
                for (ColoredShape cs : group) {
                    Rectangle r = cs.shape.getBounds();
                    if (bounds == null) bounds = r;
                    else bounds = bounds.union(r);
                }
            }
            if (currentStroke != null) {
                for (ColoredShape cs : currentStroke) {
                    Rectangle r = cs.shape.getBounds();
                    if (bounds == null) bounds = r;
                    else bounds = bounds.union(r);
                }
            }
            // Incluir textos comprometidos
            BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics g = tmp.getGraphics();
            for (TextItem ti : texts) {
                FontMetrics fm = g.getFontMetrics(ti.font);
                int width = fm.stringWidth(ti.text);
                int height = fm.getHeight();
                Rectangle textRect = new Rectangle(ti.x, ti.y - fm.getAscent(), width, height);
                if (bounds == null) bounds = textRect;
                else bounds = bounds.union(textRect);
            }
            g.dispose();
            if (bounds == null) {
                bounds = new Rectangle(0, 0, getWidth(), getHeight());
            }
            int margin = 50;
            int x = Math.max(bounds.x - margin, 0);
            int y = Math.max(bounds.y - margin, 0);
            int w = Math.min(bounds.width + 2 * margin, getWidth() - x);
            int h = Math.min(bounds.height + 2 * margin, getHeight() - y);
            return new Rectangle(x, y, w, h);
        }
        
        // Si hay un JTextArea activo, lo compromete como texto definitivo y lo elimina del panel
        public void commitActiveText() {
            if (activeTextArea != null) {
                String txt = activeTextArea.getText();
                if (txt != null && !txt.trim().isEmpty()) {
                    Font f = activeTextArea.getFont();
                    Color c = activeTextArea.getForeground();
                    Rectangle bounds = activeTextArea.getBounds();
                    texts.add(new TextItem(txt, bounds.x, bounds.y, f, c));
                }
                remove(activeTextArea);
                activeTextArea = null;
                repaint();
            }
        }
        
        // Indica si hay modificaciones en el panel
        public boolean isModified() {
            return !strokeGroups.isEmpty() || (currentStroke != null && !currentStroke.isEmpty()) || !texts.isEmpty() || bgImage != null || activeTextArea != null;
        }
    }

    // Clase para almacenar cada segmento trazado con su color
    class ColoredShape {
        Shape shape;
        Color color;
        ColoredShape(Shape shape, Color color) {
            this.shape = shape;
            this.color = color;
        }
    }

    // Clase para almacenar cada texto comprometido
    class TextItem {
        String text;
        int x, y;
        Font font;
        Color color;
        TextItem(String text, int x, int y, Font font, Color color) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.font = font;
            this.color = color;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SignatureCapture().setVisible(true));
    }
}