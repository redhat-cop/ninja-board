package com.redhat.sso.ninja;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class Backup {

  private static final Logger log = Logger.getLogger(Backup.class);
  private static Timer t;

  public static void main(String[] asd) {
    try {
      Backup.runOnce();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void runOnce() {
    new BackupRunnable(new String[]{Database2.STORAGE_AS_FILE.getAbsolutePath()}).run();
  }

  public static void start(long intervalInMs, String... paths) {
    t = new Timer(Backup.class.getSimpleName() + "-timer", false);
    t.scheduleAtFixedRate(new BackupRunnable(paths), 180000L, intervalInMs);
  }

  public static void stop() {
    t.cancel();
  }

  static class BackupRunnable extends TimerTask {

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private String[] paths;

    BackupRunnable(String[] paths) {
      this.paths = paths;
    }

    @Override
    public void run() {
      log.info(Backup.class.getSimpleName() + " fired");

      for (String path : paths) {
        File source = new File(path);
        formatter.format(LocalDate.now());
        String newName = FilenameUtils.getBaseName(source.getName()) + "-" + formatter.format(LocalDate.now()) + "." + FilenameUtils.getExtension(source.getName());
        File newFile = new File(source.getParentFile(), newName);

        try {
          log.info("Copying from [" + source.getAbsolutePath() + "] to [" + newFile.getAbsolutePath() + "]");
          IOUtils.copy(new FileInputStream(source), new FileOutputStream(newFile));
        } catch (Exception e) {
          log.error("Could not back up file: ", e);
        }

      }

    }
  }

}
