package com.tsel.home.project.booklibrary.ui;

import java.util.List;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Affine;
import javafx.util.Duration;

/** Затемняет окно и проигрывает бросок кубика перед открытием случайной книги. */
public final class RandomBookAnimation extends StackPane {

    private static final Interpolator FALL = Interpolator.SPLINE(0.33, 0, 0.67, 0.33);
    private static final Interpolator RISE = Interpolator.SPLINE(0.33, 0.67, 0.67, 1);

    private final Timeline animation = new Timeline();
    private final Group dice = new Group();
    private final Ellipse shadow = new Ellipse(120, 146, 48, 9);
    private Node previousFocus;

    public RandomBookAnimation() {
        setId("randomBookAnimation");
        getStyleClass().add("random-book-overlay");
        setVisible(false);
        setManaged(false);
        setFocusTraversable(true);
        addEventFilter(KeyEvent.ANY, KeyEvent::consume);
        dice.getStyleClass().add("random-book-dice");
        dice.getChildren().addAll(
            face(3, "random-book-face-left", new Affine(46.0 / 64, 0, -46, 24.0 / 64, 52.0 / 64, -22)),
            face(5, "random-book-face-right", new Affine(46.0 / 64, 0, 0, -24.0 / 64, 52.0 / 64, 2)),
            face(1, "random-book-face-top", new Affine(46.0 / 64, -46.0 / 64, 0, 24.0 / 64, 24.0 / 64, -46))
        );
        dice.setLayoutX(120);
        dice.setLayoutY(90);
        shadow.getStyleClass().add("random-book-shadow");
        shadow.setEffect(new GaussianBlur(5));
        Ellipse surface = new Ellipse(120, 146, 90, 19);
        surface.getStyleClass().add("random-book-surface");
        Pane stage = new Pane(surface, shadow, dice);
        stage.setMinSize(240, 180);
        stage.setPrefSize(240, 180);
        stage.setMaxSize(240, 180);
        Label caption = new Label("Выбираем книгу…");
        caption.getStyleClass().add("random-book-caption");
        VBox content = new VBox(8, stage, caption);
        content.setAlignment(Pos.CENTER);
        content.setMouseTransparent(true);
        getChildren().add(content);
        setAccessibleText("Выбираем случайную непрочитанную книгу");
    }

    public void play(Runnable onFinished) {
        if (isVisible()) {
            return;
        }
        previousFocus = getScene().getFocusOwner();
        setManaged(true);
        setVisible(true);
        toFront();
        requestFocus();
        dice.setTranslateY(0);
        shadow.setScaleX(1);
        shadow.setScaleY(1);
        shadow.setOpacity(0.4);
        animation.getKeyFrames().setAll(
            new KeyFrame(Duration.ZERO, new KeyValue(opacityProperty(), 0)),
            new KeyFrame(Duration.millis(70), new KeyValue(opacityProperty(), 1)),
            new KeyFrame(Duration.millis(750), new KeyValue(opacityProperty(), 1)),
            new KeyFrame(Duration.millis(850), new KeyValue(opacityProperty(), 0))
        );
        if (!Boolean.getBoolean("booklibrary.reducedMotion")) {
            animation.getKeyFrames().addAll(
                bounceFrame(0, -44, 0.65, FALL),
                bounceFrame(230, 0, 1, FALL),
                bounceFrame(370, -26, 0.75, RISE),
                bounceFrame(520, 0, 1, FALL),
                bounceFrame(610, -9, 0.9, RISE),
                bounceFrame(710, 0, 1, FALL)
            );
        }
        animation.setOnFinished(event -> {
            stop();
            onFinished.run();
        });
        animation.playFromStart();
    }

    public void stop() {
        animation.stop();
        animation.setOnFinished(null);
        setVisible(false);
        setManaged(false);
        if (previousFocus != null) {
            previousFocus.requestFocus();
            previousFocus = null;
        }
    }

    private KeyFrame bounceFrame(double milliseconds, double height, double shadowScale, Interpolator interpolator) {
        return new KeyFrame(Duration.millis(milliseconds),
            new KeyValue(dice.translateYProperty(), height, interpolator),
            new KeyValue(shadow.scaleXProperty(), shadowScale, interpolator),
            new KeyValue(shadow.scaleYProperty(), shadowScale, interpolator),
            new KeyValue(shadow.opacityProperty(), 0.4 * shadowScale, interpolator));
    }

    private Group face(int value, String styleClass, Affine projection) {
        Polygon surface = new Polygon(0, 0, 64, 0, 64, 64, 0, 64);
        surface.getStyleClass().addAll("random-book-face", styleClass);
        Group face = new Group(surface);
        List<Circle> pips = List.of(
            new Circle(16, 16, 5), new Circle(48, 16, 5),
            new Circle(16, 32, 5), new Circle(32, 32, 5), new Circle(48, 32, 5),
            new Circle(16, 48, 5), new Circle(48, 48, 5)
        );
        List<Integer> visiblePips = switch (value) {
            case 1 -> List.of(3);
            case 2 -> List.of(0, 6);
            case 3 -> List.of(0, 3, 6);
            case 4 -> List.of(0, 1, 5, 6);
            case 5 -> List.of(0, 1, 3, 5, 6);
            default -> List.of(0, 1, 2, 4, 5, 6);
        };
        for (int index : visiblePips) {
            Circle pip = pips.get(index);
            pip.getStyleClass().add("random-book-pip");
            face.getChildren().add(pip);
        }
        face.getTransforms().add(projection);
        return face;
    }
}
