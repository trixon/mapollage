/*
 * Copyright 2021 Patrik Karlström.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.trixon.mapollage;

import java.awt.GraphicsEnvironment;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.SystemUtils;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.PomInfo;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.Xlog;
import se.trixon.mapollage.profile.Profile;

/**
 *
 * @author Patrik Karlström
 */
public class AppStart implements OperationListener {

    public static final String HELP = "help";
    public static final String VERSION = "version";
    private static String[] sArgs;
    private static final ResourceBundle sBundle = SystemHelper.getBundle(AppStart.class, "Bundle");
    private static Options sOptions;
    private final ProfileManager mProfileManager = ProfileManager.getInstance();

    public static String getHelp() {
        PrintStream defaultStdOut = System.out;
        StringBuilder sb = new StringBuilder()
                .append(sBundle.getString("usage")).append("\n\n");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        System.setOut(ps);

        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(79);
        formatter.setOptionComparator(null);
        formatter.printHelp("xxx", sOptions, false);
        System.out.flush();
        System.setOut(defaultStdOut);
        sb.append(baos.toString().replace("usage: xxx" + System.lineSeparator(), "")).append("\n")
                .append(sBundle.getString("help_footer"));

        return sb.toString();
    }

    public static String getVersionInfo() {
        var pomInfo = new PomInfo(AppStart.class, "se.trixon", "mapollage");

        return String.format(sBundle.getString("version_info"), pomInfo.getVersion());
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        sArgs = args;
        new AppStart();
    }

    public AppStart() {
        initOptions();
        if (sArgs.length == 0) {
            System.out.println(sBundle.getString("hint_tui"));
            displayGui();
        } else {
            try {
                CommandLineParser commandLineParser = new DefaultParser();
                CommandLine commandLine = commandLineParser.parse(sOptions, sArgs);

                if (commandLine.hasOption(HELP)) {
                    displayHelp();
                    System.exit(0);
                } else if (commandLine.hasOption(VERSION)) {
                    displayVersion();
                    System.exit(0);
                } else if (commandLine.hasOption("list-profiles")) {
                    displayProfiles();
                } else if (commandLine.hasOption("view-profile")) {
                    loadProfiles();
                    Profile profile = mProfileManager.getProfile(commandLine.getOptionValue("view-profile"));
                    if (profile == null) {
                        System.err.println(Dict.Dialog.ERROR_PROFILE_NOT_FOUND.toString());
                        System.exit(1);
                    } else {
                        profile.isValid();
                        System.out.println(profile.toDebugString());
                    }
                } else if (commandLine.hasOption("gui")) {
                    displayGui();
                } else {
                    Profile profile = null;

                    if (commandLine.hasOption("run-profile")) {
                        String[] rpArgs = commandLine.getOptionValues("run-profile");
                        File destFile = new File(rpArgs[1]);
                        if (destFile.isAbsolute()) {
                            if (!destFile.getParentFile().isDirectory()) {
                                System.err.println(Dict.INVALID_DESTINATION.toString());
                                System.exit(1);
                            }
                        } else {
                            destFile = new File(SystemUtils.USER_DIR + File.separator + rpArgs[1]);
                        }

                        loadProfiles();
                        profile = mProfileManager.getProfile(commandLine.getOptionValue("run-profile"));
                        if (profile == null) {
                            System.err.println(Dict.Dialog.ERROR_PROFILE_NOT_FOUND.toString());
                            System.exit(1);
                        } else if (destFile.isDirectory()) {
                            System.err.println(Dict.INVALID_DESTINATION.toString());
                            System.exit(1);
                        }

                        profile.setDestinationFile(destFile);
                        if (profile.isValid()) {
                            if (profile.hasValidRelativeSourceDest()) {
                                Operation operation = new Operation(this, profile);
                                operation.run();
                            } else {
                                System.err.println(sBundle.getString("invalid_relative_source_dest"));
                                System.err.println(Dict.ABORTING.toString());
                            }
                        } else {
                            System.out.println(profile.getValidationError());
                            System.err.println(Dict.ABORTING.toString());
                        }
                    }
                }
            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
                System.out.println(sBundle.getString("parse_help"));
            }
        }
    }

    @Override
    public void onOperationError(String message) {
        System.err.println(message);
    }

    @Override
    public void onOperationFailed(String message) {
        System.err.println(message);
    }

    @Override
    public void onOperationFinished(String message, int placemarkCount) {
        // nvm
    }

    @Override
    public void onOperationInterrupted() {
        // nvm
    }

    @Override
    public void onOperationLog(String message) {
        System.out.println(message);
    }

    @Override
    public void onOperationProcessingStarted() {
        // nvm
    }

    @Override
    public void onOperationProgress(String message) {
    }

    @Override
    public void onOperationProgress(int value, int max) {
        // nvm
    }

    @Override
    public void onOperationStarted() {
        // nvm
    }

    private void displayGui() {
        if (GraphicsEnvironment.isHeadless()) {
            Xlog.timedErr(Dict.Dialog.ERROR_NO_GUI_IN_HEADLESS.toString());
            System.exit(1);

            return;
        }

        new Thread(() -> {
            App.main(sArgs);
        }).start();
    }

    private void displayHelp() {
        System.out.println(getHelp());
    }

    private void displayProfiles() {
        loadProfiles();
        if (mProfileManager.hasProfiles()) {
            for (Profile profile : mProfileManager.getProfiles()) {
                System.out.println(profile.getName());
            }
        } else {
            System.out.println(Dict.Dialog.MESSAGE_NO_PROFILES_FOUND.toString());
        }
    }

    private void displayVersion() {
        System.out.println(getVersionInfo());
    }

    private void initOptions() {
        Option help = Option.builder("h")
                .longOpt(HELP)
                .desc(sBundle.getString("opt_help_desc"))
                .build();

        Option version = Option.builder("v")
                .longOpt(VERSION)
                .desc(sBundle.getString("opt_version_desc"))
                .build();

        Option gui = Option.builder("g")
                .longOpt("gui")
                .desc(sBundle.getString("opt_gui_desc"))
                .build();

        Option profile = Option.builder("rp")
                .longOpt("run-profile")
                .hasArg()
                .numberOfArgs(2)
                .desc(sBundle.getString("opt_profile_desc"))
                .build();

        Option listProfiles = Option.builder("lp")
                .longOpt("list-profiles")
                .desc(sBundle.getString("opt_list_profiles_desc"))
                .build();

        Option viewProfile = Option.builder("vp")
                .longOpt("view-profile")
                .hasArg()
                .numberOfArgs(1)
                .desc(sBundle.getString("opt_view_profile_desc"))
                .build();

        sOptions = new Options();

        sOptions.addOption(listProfiles);
        sOptions.addOption(viewProfile);
        sOptions.addOption(profile);

//        sOptions.addOption(gui);
        sOptions.addOption(help);
        sOptions.addOption(version);
    }

    private void loadProfiles() {
        try {
            mProfileManager.load();
        } catch (IOException ex) {
            Logger.getLogger(AppStart.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
