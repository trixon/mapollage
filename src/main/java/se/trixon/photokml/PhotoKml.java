/*
 * Copyright 2016 Patrik Karlsson.
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
package se.trixon.photokml;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ResourceBundle;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.SystemUtils;
import se.trixon.util.BundleHelper;
import se.trixon.util.SystemHelper;
import se.trixon.util.dictionary.Dict;

/**
 *
 * @author Patrik Karlsson
 */
public class PhotoKml implements OperationListener {

    private final ResourceBundle mBundle = BundleHelper.getBundle(PhotoKml.class, "Bundle");
    private Options mOptions;

    public static final String ABSOLUTE_PATH = "absolute-path";
    public static final String COORDINATE = "coordinate";
    public static final String FOLDER_DESC = "folder-desc";
    public static final String FOLDER_NAME = "folder-name";
    public static final String HELP = "help";
    public static final String LINKS = "links";
    public static final String LOWER_CASE_EXT = "lower-case-ext";
    public static final String MAX_HEIGHT = "max-height";
    public static final String MAX_WIDTH = "max-width";
    public static final String PLACEMARK_DESC = "placemark-desc";
    public static final String PLACEMARK_NAME = "placemark-name";
    public static final String RECURSIVE = "recursive";
    public static final String ROOT_DESC = "root-desc";
    public static final String ROOT_NAME = "root-name";
    public static final String VERSION = "version";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new PhotoKml(args);
    }

    public PhotoKml(String[] args) {
        initOptions();
        if (args.length == 0) {
            displayHelp();
        } else {
            try {
                CommandLineParser commandLineParser = new DefaultParser();
                CommandLine commandLine = commandLineParser.parse(mOptions, args);

                if (commandLine.hasOption(HELP)) {
                    displayHelp();
                    System.exit(0);
                } else if (commandLine.hasOption(VERSION)) {
                    displayVersion();
                    System.exit(0);
                } else {
                    OptionsHolder optionsHolder = new OptionsHolder(commandLine);

                    if (optionsHolder.isValid()) {
                        Operation operation = new Operation(this, optionsHolder);
                        operation.start();
                    } else {
                        System.out.println(optionsHolder.getValidationError());
                        System.out.println(Dict.ABORTING.toString());
                    }
                    //System.out.println(optionsHolder.toString());
                }
            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
                System.out.println(mBundle.getString("parse_help"));
            }
        }
    }

    @Override
    public void onOperationFailed(String message) {
        System.out.println("onOperationFailed "+message);
    }

    @Override
    public void onOperationFinished(String message) {
        System.out.println("onOperationFinished "+message);
    }

    @Override
    public void onOperationInterrupted() {
        System.out.println("onOperationInterrupted");
    }

    @Override
    public void onOperationLog(String message) {
        System.out.println("onOperationLog "+message);
    }

    @Override
    public void onOperationProcessingStarted() {
        System.out.println("onOperationProcessingStarted");
    }

    @Override
    public void onOperationStarted() {
        System.out.println("onOperationStarted");
    }

    private void displayHelp() {
        PrintStream defaultStdOut = System.out;
        StringBuilder sb = new StringBuilder()
                .append(mBundle.getString("usage")).append("\n\n");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        System.setOut(ps);

        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(79);
        formatter.setOptionComparator(null);
        formatter.printHelp("xxx", mOptions, false);
        System.out.flush();
        System.setOut(defaultStdOut);
        sb.append(baos.toString().replace("usage: xxx" + SystemUtils.LINE_SEPARATOR, "")).append("\n")
                .append(mBundle.getString("help_footer"));

        System.out.println(sb.toString());
    }

    private void displayVersion() {
        System.out.println(String.format(mBundle.getString("version_info"), SystemHelper.getJarVersion(PhotoKml.class)));
    }

    private void initOptions() {
        Option help = Option.builder("?")
                .longOpt(HELP)
                .desc(mBundle.getString("opt_help_desc"))
                .build();

        Option version = Option.builder("v")
                .longOpt(VERSION)
                .desc(mBundle.getString("opt_version_desc"))
                .build();

        Option recursive = Option.builder("r")
                .longOpt(RECURSIVE)
                .desc(mBundle.getString("opt_recursive_desc"))
                .build();

        Option links = Option.builder("l")
                .longOpt(LINKS)
                .desc(mBundle.getString("opt_links_desc"))
                .build();

        Option rootName = Option.builder("rn")
                .longOpt(ROOT_NAME)
                .desc(mBundle.getString("opt_root_name_desc"))
                .hasArg()
                .build();

        Option rootDesc = Option.builder("rd")
                .longOpt(ROOT_DESC)
                .desc(mBundle.getString("opt_root_desc_desc"))
                .hasArg()
                .build();

        Option folderName = Option.builder("fn")
                .longOpt(FOLDER_NAME)
                .desc(mBundle.getString("opt_folder_name_desc"))
                .hasArg()
                .optionalArg(true)
                .build();

        Option folderDesc = Option.builder("fd")
                .longOpt(FOLDER_DESC)
                .desc(mBundle.getString("opt_folder_desc_desc"))
                .hasArg()
                .build();

        Option placemarkName = Option.builder("pn")
                .longOpt(PLACEMARK_NAME)
                .desc(mBundle.getString("opt_placemark_name_desc"))
                .hasArg()
                .optionalArg(true)
                .build();

        Option placemarkDesc = Option.builder("pd")
                .longOpt(PLACEMARK_DESC)
                .desc(mBundle.getString("opt_placemark_desc_desc"))
                .hasArg()
                .argName("html")
                .build();

        Option coordinate = Option.builder("c")
                .longOpt(COORDINATE)
                .desc(mBundle.getString("opt_coordinate_desc"))
                .hasArgs()
                .numberOfArgs(2)
                .argName("LAT LON")
                .build();

        Option maxHeight = Option.builder("h")
                .longOpt(MAX_HEIGHT)
                .desc(mBundle.getString("opt_max_height_desc"))
                .hasArg()
                .optionalArg(false)
                .argName("NUM")
                .build();

        Option maxWidth = Option.builder("w")
                .longOpt(MAX_WIDTH)
                .desc(mBundle.getString("opt_max_width_desc"))
                .hasArg()
                .optionalArg(false)
                .argName("NUM")
                .build();

        Option lowerCaseExt = Option.builder("e")
                .longOpt(LOWER_CASE_EXT)
                .desc(mBundle.getString("opt_lower_case_ext_desc"))
                .build();

        Option absolutePath = Option.builder("a")
                .longOpt(ABSOLUTE_PATH)
                .desc(mBundle.getString("opt_absolute_path_desc"))
                .hasArg()
                .optionalArg(false)
                .argName("PATH")
                .build();

        mOptions = new Options();

        mOptions.addOption(rootName);
        mOptions.addOption(rootDesc);

        mOptions.addOption(folderName);
        mOptions.addOption(folderDesc);

        mOptions.addOption(placemarkName);
        mOptions.addOption(placemarkDesc);
        mOptions.addOption(maxHeight);
        mOptions.addOption(maxWidth);
        mOptions.addOption(coordinate);
        mOptions.addOption(lowerCaseExt);
        mOptions.addOption(absolutePath);

        mOptions.addOption(links);
        mOptions.addOption(recursive);

        mOptions.addOption(help);
        mOptions.addOption(version);
    }
}
