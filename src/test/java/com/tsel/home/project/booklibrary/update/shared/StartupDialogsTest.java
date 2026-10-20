package com.tsel.home.project.booklibrary.update.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.junit.jupiter.api.Test;

class StartupDialogsTest {

    @Test
    void confirmationDefaultsToCancelAndSupportsAcceptAndCaptionClose() throws Exception {
        for (String action : new String[] {"Отмена", "Установить", "Закрыть"}) {
            AtomicReference<Throwable> failure = new AtomicReference<>();
            SwingUtilities.invokeAndWait(() -> {
                Timer timer = new Timer(100, event -> {
                    JDialog dialog = Arrays.stream(Window.getWindows())
                        .filter(window -> window instanceof JDialog candidate && candidate.isShowing()
                            && "Тест обновления".equals(candidate.getTitle()))
                        .map(JDialog.class::cast).findFirst().orElseThrow();
                    try {
                        assertTrue(dialog.isUndecorated());
                        assertEquals("Отмена", dialog.getRootPane().getDefaultButton().getText());
                        assertTrue(dialog.getHeight() < 500);
                        if (Boolean.getBoolean("booklibrary.captureFilters")) {
                            BufferedImage image = new BufferedImage(
                                dialog.getWidth(), dialog.getHeight(), BufferedImage.TYPE_INT_RGB
                            );
                            Graphics2D graphics = image.createGraphics();
                            dialog.printAll(graphics);
                            graphics.dispose();
                            ImageIO.write(image, "png", new File("target/startup-dialog.png"));
                        }
                        JButton button = findButton(dialog, action);
                        assertNotNull(button);
                        button.doClick();
                    } catch (Throwable e) {
                        failure.set(e);
                    } finally {
                        dialog.dispose();
                    }
                });
                timer.setRepeats(false);
                timer.start();
            });
            boolean accepted = StartupDialogs.confirm("Тест обновления", "Длинное сообщение. ".repeat(100), "Установить");
            if (failure.get() != null) {
                throw new AssertionError(failure.get());
            }
            if ("Установить".equals(action)) {
                assertTrue(accepted);
            } else {
                assertFalse(accepted);
            }
        }
    }

    private JButton findButton(Container parent, String text) {
        for (Component component : parent.getComponents()) {
            if (component instanceof JButton button && (text.equals(button.getText())
                || text.equals(button.getAccessibleContext().getAccessibleName()))) {
                return button;
            }
            if (component instanceof Container container) {
                JButton found = findButton(container, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
