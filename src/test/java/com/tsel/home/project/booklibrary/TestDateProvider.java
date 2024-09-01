package com.tsel.home.project.booklibrary;

import com.tsel.home.project.booklibrary.helper.DateProvider;
import java.time.LocalDateTime;
import lombok.Setter;

@Setter
public class TestDateProvider extends DateProvider {

    private LocalDateTime fakeNow;

    @Override
    public LocalDateTime getNow() {
        return this.fakeNow;
    }
}
