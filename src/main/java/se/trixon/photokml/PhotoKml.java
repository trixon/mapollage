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
public class PhotoKml {

    private final ResourceBundle mBundle = BundleHelper.getBundle(PhotoKml.class, "Bundle");
    private Options mOptions;

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

                if (commandLine.hasOption("help")) {
                    displayHelp();
                    System.exit(0);
                } else if (commandLine.hasOption("version")) {
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
                }
            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
                System.out.println(mBundle.getString("parse_help"));
            }
        }
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
                .longOpt("help")
                .desc(mBundle.getString("opt_help_desc"))
                .build();

        Option version = Option.builder("v")
                .longOpt("version")
                .desc(mBundle.getString("opt_version_desc"))
                .build();

        Option recursive = Option.builder("r")
                .longOpt("recursive")
                .desc(mBundle.getString("opt_recursive_desc"))
                .build();

        Option links = Option.builder("l")
                .longOpt("links")
                .desc(mBundle.getString("opt_links_desc"))
                .build();

        Option rootName = Option.builder("rn")
                .longOpt("root-name")
                .desc(mBundle.getString("opt_root_name_desc"))
                .hasArg()
                //.required()
                .optionalArg(false)
                .build();

        Option rootDesc = Option.builder("rd")
                .longOpt("root-desc")
                .desc(mBundle.getString("opt_root_desc_desc"))
                .hasArg()
                .optionalArg(false)
                .build();

        Option folderName = Option.builder("fn")
                .longOpt("folder-name")
                .desc(mBundle.getString("opt_folder_name_desc"))
                .hasArg()
                .optionalArg(true)
                .build();

        Option folderDesc = Option.builder("fd")
                .longOpt("folder-desc")
                .desc(mBundle.getString("opt_folder_desc_desc"))
                .hasArg()
                .optionalArg(false)
                .build();

        Option placemarkName = Option.builder("pn")
                .longOpt("placemark-name")
                .desc(mBundle.getString("opt_placemark_name_desc"))
                .hasArg()
                .optionalArg(true)
                .build();

        Option placemarkDesc = Option.builder("pd")
                .longOpt("placemark-desc")
                .desc(mBundle.getString("opt_placemark_desc_desc"))
                .hasArg()
                .optionalArg(false)
                .argName("html")
                .build();

        Option coordinate = Option.builder("c")
                .longOpt("coordinate")
                .desc(mBundle.getString("opt_coordinate_desc"))
                .hasArg()
                .optionalArg(false)
                .argName("LAT,LON")
                .build();

        Option maxHeight = Option.builder("h")
                .longOpt("max-height")
                .desc(mBundle.getString("opt_max_height_desc"))
                .hasArg()
                .optionalArg(false)
                .argName("NUM")
                .build();

        Option maxWidth = Option.builder("w")
                .longOpt("max-width")
                .desc(mBundle.getString("opt_max_width_desc"))
                .hasArg()
                .optionalArg(false)
                .argName("NUM")
                .build();

        Option lowerCaseExt = Option.builder("e")
                .longOpt("lower-case-ext")
                .desc(mBundle.getString("opt_lower_case_ext_desc"))
                .build();

        Option absolutePath = Option.builder("a")
                .longOpt("absolute-path")
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
