package edu.indiana.soic.spidal.damds.flink;

import com.google.common.base.Optional;
import edu.indiana.soic.spidal.configuration.ConfigurationMgr;
import edu.indiana.soic.spidal.configuration.section.DAMDSSection;
import edu.indiana.soic.spidal.damds.flink.io.ShortMatrixInputFormat;
import org.apache.commons.cli.*;
import scala.xml.PrettyPrinter;

import java.nio.ByteOrder;
import java.util.Date;

public class Program {
    private static Options programOptions = new Options();

    static {
        programOptions.addOption(
            String.valueOf(Constants.CMD_OPTION_SHORT_C),
            Constants.CMD_OPTION_LONG_C, true,
            Constants.CMD_OPTION_DESCRIPTION_C);
        programOptions.addOption(
            String.valueOf(Constants.CMD_OPTION_SHORT_N),
            Constants.CMD_OPTION_LONG_N, true,
            Constants.CMD_OPTION_DESCRIPTION_N);
        programOptions.addOption(
            String.valueOf(Constants.CMD_OPTION_SHORT_T),
            Constants.CMD_OPTION_LONG_T, true,
            Constants.CMD_OPTION_DESCRIPTION_T);

        programOptions.addOption(Constants.CMD_OPTION_SHORT_MMAPS, true, Constants.CMD_OPTION_DESCRIPTION_MMAPS);
        programOptions.addOption(
            Constants.CMD_OPTION_SHORT_MMAP_SCRATCH_DIR, true,
            Constants.CMD_OPTION_DESCRIPTION_MMAP_SCRATCH_DIR);
    }

    //Config Settings
    public static DAMDSSection config;
    public static ByteOrder byteOrder;
    public static int BlockSize;

    /**
     * Weighted SMACOF based on Deterministic Annealing algorithm
     *
     * @param args command line arguments to the program, which should include
     *             -c path to config file
     *             -t number of threads
     *             -n number of nodes
     *             The options may also be given as longer names
     *             --configFile, --threadCount, and --nodeCount respectively
     */
    public static void main(String[] args) {
        Optional<CommandLine> parserResult =
            parseCommandLineArguments(args, programOptions);

        if (!parserResult.isPresent()) {
            System.out.println(Constants.ERR_PROGRAM_ARGUMENTS_PARSING_FAILED);
            new HelpFormatter()
                .printHelp(Constants.PROGRAM_NAME, programOptions);
            return;
        }

        CommandLine cmd = parserResult.get();
        if (!(cmd.hasOption(Constants.CMD_OPTION_LONG_C) &&
              cmd.hasOption(Constants.CMD_OPTION_LONG_N) &&
              cmd.hasOption(Constants.CMD_OPTION_LONG_T))) {
            System.out.println(Constants.ERR_INVALID_PROGRAM_ARGUMENTS);
            new HelpFormatter()
                .printHelp(Constants.PROGRAM_NAME, programOptions);
            return;
        }

        //  Read Metadata using this as source of other metadata
        readConfiguration(cmd);

        Utils.printMessage("\n== DAMDS run started on " + new Date() + " ==\n");
        Utils.printMessage(config.toString(false));

        ShortMatrixInputFormat dInF = new ShortMatrixInputFormat();
        dInF.setFilePath(config.distanceMatrixFile);
        dInF.setBigEndian(config.isBigEndian);
        dInF.setGlobalColumnCount(config.numberDataPoints);


    }

    private static void readConfiguration(CommandLine cmd) {
        config = ConfigurationMgr.LoadConfiguration(
            cmd.getOptionValue(Constants.CMD_OPTION_LONG_C)).damdsSection;
        ParallelOps.nodeCount =
            Integer.parseInt(cmd.getOptionValue(Constants.CMD_OPTION_LONG_N));
        ParallelOps.threadCount =
            Integer.parseInt(cmd.getOptionValue(Constants.CMD_OPTION_LONG_T));
        ParallelOps.mmapsPerNode = cmd.hasOption(Constants.CMD_OPTION_SHORT_MMAPS) ? Integer.parseInt(cmd.getOptionValue(Constants.CMD_OPTION_SHORT_MMAPS)) : 1;
        ParallelOps.mmapScratchDir = cmd.hasOption(Constants.CMD_OPTION_SHORT_MMAP_SCRATCH_DIR) ? cmd.getOptionValue(Constants.CMD_OPTION_SHORT_MMAP_SCRATCH_DIR) : ".";

        byteOrder =
            config.isBigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
        BlockSize = config.blockSize;
    }


    /**
     * Parse command line arguments
     *
     * @param args Command line arguments
     * @param opts Command line options
     * @return An <code>Optional&lt;CommandLine&gt;</code> object
     */
    private static Optional<CommandLine> parseCommandLineArguments(
        String[] args, Options opts) {

        CommandLineParser optParser = new GnuParser();

        try {
            return Optional.fromNullable(optParser.parse(opts, args));
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return Optional.fromNullable(null);
    }
}
