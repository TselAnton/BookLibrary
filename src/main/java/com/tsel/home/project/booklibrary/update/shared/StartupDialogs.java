package com.tsel.home.project.booklibrary.update.shared;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicScrollBarUI;

/** Сообщения запуска и самостоятельного помощника, доступные без JavaFX. */
public final class StartupDialogs {

    private static final Color CANVAS = new Color(0xF3F6F2);
    private static final Color HEADER = new Color(0xE8F0E9);
    private static final Color TEXT = new Color(0x20372C);
    private static final Color BORDER = new Color(0xC4CFC7);
    private static final Color PRIMARY = new Color(0x2F6B4F);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    private StartupDialogs() {}

    public static void message(String title, String message) {
        show(title, message, null);
    }

    public static boolean confirm(String title, String message, String action) {
        return show(title, message, action);
    }

    private static boolean show(String title, String message, String action) {
        boolean[] accepted = {false};
        Runnable display = () -> {
            JDialog dialog = new JDialog();
            dialog.setTitle(title);
            dialog.setModal(true);
            dialog.setUndecorated(true);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(CANVAS);
            root.setBorder(BorderFactory.createLineBorder(new Color(0x8DA696)));
            JPanel header = new JPanel(new BorderLayout(12, 0));
            header.setBackground(HEADER);
            header.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 8));
            JLabel heading = new JLabel(title);
            heading.setFont(BODY_FONT.deriveFont(Font.BOLD, 20));
            heading.setForeground(TEXT);
            header.add(heading, BorderLayout.CENTER);
            JButton close = button("", false);
            close.setBorder(BorderFactory.createEmptyBorder());
            close.setBackground(HEADER);
            close.getModel().addChangeListener(event -> close.setBackground(
                close.getModel().isRollover() || close.hasFocus() ? new Color(0xDFE6E0) : HEADER
            ));
            close.setPreferredSize(new Dimension(32, 32));
            close.getAccessibleContext().setAccessibleName("Закрыть");
            close.setIcon(new Icon() {
                @Override
                public int getIconWidth() {
                    return 18;
                }

                @Override
                public int getIconHeight() {
                    return 18;
                }

                @Override
                public void paintIcon(Component component, Graphics graphics, int x, int y) {
                    graphics.setColor(TEXT);
                    graphics.drawLine(x + 4, y + 4, x + 14, y + 14);
                    graphics.drawLine(x + 14, y + 4, x + 4, y + 14);
                }
            });
            close.addActionListener(event -> dialog.dispose());
            header.add(close, BorderLayout.EAST);
            MouseAdapter drag = new MouseAdapter() {
                private Point start;

                @Override
                public void mousePressed(MouseEvent event) {
                    start = event.getLocationOnScreen();
                }

                @Override
                public void mouseDragged(MouseEvent event) {
                    Point next = event.getLocationOnScreen();
                    dialog.setLocation(dialog.getX() + next.x - start.x, dialog.getY() + next.y - start.y);
                    start = next;
                }
            };
            header.addMouseListener(drag);
            header.addMouseMotionListener(drag);
            root.add(header, BorderLayout.NORTH);

            JTextArea text = new JTextArea(message);
            text.setEditable(false);
            text.setLineWrap(true);
            text.setWrapStyleWord(true);
            text.setFont(BODY_FONT);
            text.setForeground(TEXT);
            text.setBackground(CANVAS);
            text.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
            text.setSize(518, Short.MAX_VALUE);
            JScrollPane scroll = new JScrollPane(text);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
            scroll.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
                @Override
                protected JButton createDecreaseButton(int orientation) {
                    JButton button = new JButton();
                    button.setPreferredSize(new Dimension(0, 0));
                    return button;
                }

                @Override
                protected JButton createIncreaseButton(int orientation) {
                    return createDecreaseButton(orientation);
                }

                @Override
                protected void paintTrack(Graphics graphics, JComponent component, Rectangle bounds) {
                    graphics.setColor(CANVAS);
                    graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
                }

                @Override
                protected void paintThumb(Graphics graphics, JComponent component, Rectangle bounds) {
                    graphics.setColor(BORDER);
                    graphics.fillRoundRect(bounds.x + 2, bounds.y, bounds.width - 4, bounds.height, 8, 8);
                }
            });
            scroll.setPreferredSize(new Dimension(518, Math.min(300, Math.max(120, text.getPreferredSize().height))));
            root.add(scroll, BorderLayout.CENTER);

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            footer.setBackground(CANVAS);
            footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                BorderFactory.createEmptyBorder(16, 8, 16, 8)
            ));
            JButton accept = button(action == null ? "Понятно" : action, true);
            accept.addActionListener(event -> {
                accepted[0] = true;
                dialog.dispose();
            });
            footer.add(accept);
            JButton cancel = button("Отмена", false);
            cancel.addActionListener(event -> dialog.dispose());
            if (action != null) {
                footer.add(cancel);
            }
            root.add(footer, BorderLayout.SOUTH);
            dialog.setContentPane(root);
            dialog.getRootPane().setDefaultButton(action == null ? accept : cancel);
            root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
            root.getActionMap().put("close", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    dialog.dispose();
                }
            });
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            SwingUtilities.invokeLater(() -> (action == null ? accept : cancel).requestFocusInWindow());
            dialog.setVisible(true);
        };
        if (SwingUtilities.isEventDispatchThread()) {
            display.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(display);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Отображение сообщения прервано", e);
            } catch (InvocationTargetException e) {
                throw new IllegalStateException("Не удалось показать сообщение", e.getCause());
            }
        }
        return accepted[0];
    }

    private static JButton button(String text, boolean primary) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D canvas = (Graphics2D) graphics.create();
                canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color background = getBackground();
                if (getModel().isRollover() && !getText().isEmpty()) {
                    background = primary ? new Color(0x285D44) : new Color(0xEEF4EF);
                }
                canvas.setColor(background);
                canvas.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                if (!getText().isEmpty()) {
                    canvas.setColor(hasFocus() ? PRIMARY : primary ? PRIMARY : BORDER);
                    canvas.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    if (hasFocus()) {
                        canvas.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
                    }
                }
                canvas.dispose();
                super.paintComponent(graphics);
            }
        };
        button.setUI(new BasicButtonUI());
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setFont(BODY_FONT);
        button.setBackground(primary ? PRIMARY : Color.WHITE);
        button.setForeground(primary ? Color.WHITE : TEXT);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        return button;
    }
}
