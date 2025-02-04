package com.tsel.home.project.booklibrary.utils;

import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Timer {

   private final String description;
   private final long startTime;

   private Timer(@Nullable String description) {
      this.startTime = System.nanoTime();
      this.description = description;
   }

   public static Timer start(@Nullable String description) {
      return new Timer(description);
   }

   public static Timer start() {
      return new Timer(null);
   }

   public void stop() {
      long endTime = System.nanoTime() - this.startTime;
      String message = this.description == null ? "Timer" : (this.description + ". Timer");
      log.warn("[Timer] {}: {} m / {} s / {} ms / {} ns", message, (endTime / 60000000000L), (endTime / 1000000000L), (endTime / 1000000L), endTime);
   }
}
