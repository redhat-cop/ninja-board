package com.redhat.sso.ninja;

import com.redhat.sso.ninja.utils.DownloadFile;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoogleDrive2 {

    private static Logger log = Logger.getLogger(GoogleDrive2.class);

    //  public static final String DEFAULT_PULL_COMMAND=DEFAULT_EXECUTABLE+" pull -export xls -quiet=true --id %s"; //worked with 0.3.1
    private static final String DEFAULT_PULL_COMMAND = " pull -export xls -no-prompt --id %s"; // 0.3.7+ changed its output that we parse
    private static final String DEFAULT_WORKING_FOLDER = "google_drive";

    private String googleSheetPullCommand;
    private String googleSheetWorkingFolder;

    private static String getDefaultExecutable() {
        return Paths.get(SystemUtils.getUserHome().getAbsolutePath(), determineGDriveExecutable()).toString();
    }

    private static String getDefaultPullCommand() {
        return getDefaultExecutable() + DEFAULT_PULL_COMMAND;
    }

    private static String getDefaultWorkingFolder() {
        return Paths.get(SystemUtils.getUserHome().getAbsolutePath(), DEFAULT_WORKING_FOLDER).toString();
    }

    GoogleDrive2() {
        this.googleSheetPullCommand = DEFAULT_PULL_COMMAND;
        this.googleSheetWorkingFolder = DEFAULT_WORKING_FOLDER;
    }

    public GoogleDrive2(String googleSheetPullCommand, String googleSheetWorkingFolder) {
        this.googleSheetPullCommand = googleSheetPullCommand;
        this.googleSheetWorkingFolder = googleSheetWorkingFolder;
    }

    File downloadFile(String fileId) throws IOException, InterruptedException {
        String command = String.format(googleSheetPullCommand, System.getProperty("user.name"), fileId);

        String googleDrivePath = String.format(googleSheetWorkingFolder, System.getProperty("user.name"));
        File workingFolder = new File(googleDrivePath);
        if (!workingFolder.mkdirs()) { // just in case it's not there
            throw new IllegalStateException("Unable to create working folder: " + workingFolder.getAbsolutePath());
        }

        log.debug("Using working folder: " + workingFolder.getAbsolutePath());
        log.debug("Downloading google file: " + fileId);
        log.debug("Command: " + command);

        if (googleDrivePath.contains("google")) {
            recursivelyDelete(workingFolder);
        } else {
            log.warn("Not cleaning working folder unless it contains the name 'google' - for safety reasons");
        }

        Process exec = Runtime.getRuntime().exec(command, null, workingFolder);

        exec.waitFor();
        String syserr = IOUtils.toString(exec.getErrorStream());
        String sysout = IOUtils.toString(exec.getInputStream());
        System.out.println("sysout=\"" + sysout + "\"; syserr=\"" + syserr + "\"");
        if (!sysout.contains("Resolving...") && !sysout.contains("Everything is up-to-date"))
            throw new RuntimeException("Error running google drive script: " + sysout);
        if (!sysout.contains("Everything is up-to-date")) {
            // System.out.println("Do Nothing");
            // return null;
            // } else {
            Pattern p = Pattern.compile("to '(.+)'$");
            Matcher matcher = p.matcher(sysout);
            if (matcher.find()) {
                String preFilePath = matcher.group(1);
                // System.out.println("Process the file: " + preFilePath);
                return new File(preFilePath);
            }
        }
        return null;
        // System.out.println(exec.exitValue());
    }

    private int getHeaderRow() {
        return 0;
    }

    private boolean valid(Map<String, String> entry) {
        return true;
    }

    List<Map<String, String>> parseExcelDocument(File file) throws FileNotFoundException, IOException {
        // parse excel file using apache poi
        // read out "tasks" and create/update solutions
        // use timestamp (column A) as the unique identifier (if in doubt i'll hash it with the requester's username)
        List<Map<String, String>> entries = new ArrayList<>();
        FileInputStream in = null;
        if (file == null || !file.exists()) return new ArrayList<>();
        try {
            in = new FileInputStream(file);
            XSSFWorkbook wb = new XSSFWorkbook(in);
            XSSFSheet s = wb.getSheetAt(0);
            int maxColumns = 20;

            for (int iRow = getHeaderRow() + 1; iRow <= s.getLastRowNum(); iRow++) {
                Map<String, String> e = new HashMap<>();
                for (int iColumn = 0; iColumn <= maxColumns; iColumn++) {
                    if (s.getRow(getHeaderRow()).getCell(iColumn) == null) continue;
                    String header = s.getRow(getHeaderRow()).getCell(iColumn).getStringCellValue();
                    XSSFRow row = s.getRow(iRow);
                    if (row == null) break; // next line/row
                    XSSFCell cell = row.getCell(iColumn);
                    if (cell == null) continue; // try next cell/column

                    try {
                        e.put(header, cell.getStringCellValue());
                    } catch (Exception ignored) {
                    }
                    if (!e.containsKey(header))
                        try {
                            e.put(header, cell.getDateCellValue().toString());
                        } catch (Exception ignored) {
                        }

                }

                if (valid(e)) {
                    e.put("ROW_#", String.valueOf(iRow));
                    entries.add(e);
                }
            }
        } finally {
            IOUtils.closeQuietly(in);
        }
        return entries;
    }

    private void recursivelyDelete(File file) {
        File[] files = file.listFiles();
        if (ArrayUtils.isNotEmpty(files)) {
            for (File f : files) {
                if (!f.getName().startsWith(".") && f.isDirectory())
                    recursivelyDelete(f);
                if (!f.getName().startsWith(".")) {
                    boolean result = f.delete();
                    if (!result) {
                        log.warn("Could not delete file: " + f.getName());
                    }
                }
            }
        }
    }

    /**
     * Determines the appropriate gdrive executable to utilize, based on OS
     * @return The appropriate gdrive executable
     */
    private static String determineGDriveExecutable() {
        if (SystemUtils.IS_OS_MAC_OSX) {
            return "drive_darwin";
        } else if (SystemUtils.IS_OS_LINUX) {
            return "drive_linux";
        } else {
            throw new IllegalStateException("Unable to determine OS from os.name: " + System.getProperty("os.name"));
        }
    }

    /**
     * Loads the environment credentials, either directly from the string or from the file if specified
     * @param credsFile The credentials file string
     * @return True if the credentials string was not empty, false otherwise
     * @throws IOException If the credentials fail to write locally
     */
    private static boolean getEnvironmentCredentials(File credsFile) throws IOException {
        String credentials = Config.get().getEnvOption("GD_CREDENTIALS");
        if (StringUtils.isNotEmpty(credentials)) {
            if (credentials.startsWith("file:")) {
                IOUtils.write(Files.readAllBytes(Paths.get(credentials.substring(5))), new FileOutputStream(credsFile));
            } else {
                IOUtils.write(credentials.getBytes(), new FileOutputStream(credsFile));
            }
        }
        return StringUtils.isNotEmpty(credentials);
    }

    static void initialise() { //ie. download gdrive executable if necessary
        if (!new File(GoogleDrive2.getDefaultExecutable()).exists()) {
            // attempt to download it
            File credsFile = Paths.get(GoogleDrive2.getDefaultWorkingFolder(), ".gd", "credentials.json").toFile();
            try {

                String executable = determineGDriveExecutable();
                String releaseUrl = Config.get().getOption("gdrive.releases.url");
                Validate.isTrue(releaseUrl != null);
                String url = Config.get().getOption("gdrive.releases.url") + executable;

                log.info("Downloading gdrive from: " + url);
                new DownloadFile().get(url, new File(GoogleDrive2.getDefaultExecutable()).getParentFile(), PosixFilePermission.OTHERS_EXECUTE);

                log.debug("Creating directory " + credsFile.getParentFile() + " if necessary.");
                Files.createDirectories(Paths.get(credsFile.getParent()));
                log.info("Deploying credentials.json in: " + credsFile);

                InputStream is = GoogleDrive2.class.getClassLoader().getResourceAsStream("/gd_credentials.json");
                if (null != is) {
                    log.info("... from internal classloader path of '/gd_credentials.json'");
                    IOUtils.copy(is, new FileOutputStream(credsFile));
                } else if (null != Config.get().getEnvOption("GD_CREDENTIALS")) {
                    log.info("... from env variable 'GD_CREDENTIALS'");
                    getEnvironmentCredentials(credsFile);
                } else {
                    log.error("no gdrive creds specified in either resources, or system props");
                    throw new IllegalStateException("Must specify gdrive creds on either classpath or environment.");
                }
                log.info("drive credentials file contains: " + IOUtils.toString(new FileInputStream(credsFile)));

            } catch (Exception e) {
                log.error("Failed to initialise gdrive and/or credentials, cleaning up exe and creds: ", e);
                if (credsFile.exists()) {
                    Validate.isTrue(credsFile.delete());
                }
                File gd = new File(GoogleDrive2.getDefaultExecutable());
                if (gd.exists()) {
                    Validate.isTrue(gd.delete());
                }
            }
        } else {
            log.info("gdrive already initialised. Existing binary is here: " + GoogleDrive2.getDefaultExecutable());
        }
    }

}
