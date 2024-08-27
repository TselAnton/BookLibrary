package com.tsel.home.project.booklibrary.javafx;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tsel.home.project.booklibrary.App;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@ExtendWith(ApplicationExtension.class)
class CreateBookTests extends AbstractJavaFxTest {

    //TODO: https://github.com/TestFX/TestFX
    // https://www.reddit.com/r/JavaFX/comments/sgjkir/javafx_unit_tests/
    // https://testfx.github.io/TestFX/

    @Start
    private void start(Stage stage) {
        initRepositories();


        new App.JavaFXRunner().start(stage);
    }

    @Test
    void shouldCreateTest(FxRobot robot) {
        robot.clickOn("#addBookButton");
        assertEquals(2, robot.listWindows().size());

//        FxAssert.verifyThat("#addBookButton", LabeledMatchers);
    }
}
