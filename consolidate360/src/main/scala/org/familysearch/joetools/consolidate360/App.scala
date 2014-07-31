package org.familysearch.joetools.consolidate360

import org.apache.commons.cli._
import org.apache.commons.cli.Option

object App {
  def main(args: Array[String]) {

    try {
      val options: Options = new Options
      val outputFileOption: org.apache.commons.cli.Option = new Option("o", "output_file", true, "The name of the output file to which the spreadsheet will be saved.")
      options.addOption(outputFileOption)
      val planningDataFileOption: org.apache.commons.cli.Option = new Option("d", "directory_tree", true, "The tree with iteration named directories and excel files to be consolidated.")
      options.addOption(planningDataFileOption)
      val individualReportDirectoryOption: org.apache.commons.cli.Option = new Option("i", "individual_report_directory", true, "The directory where individual reports should be placed.")
      options.addOption(individualReportDirectoryOption)
      val commentsOption: org.apache.commons.cli.Option = new Option("c", "comments", false, "causes anonymous comments to show up on the individual reports in random order.")
      options.addOption(commentsOption)
      val noAnonymousOption: org.apache.commons.cli.Option = new Option("a", "no-anonymous", false, "removes anonymity from individual reports.")
      options.addOption(noAnonymousOption)
      val helpOption: org.apache.commons.cli.Option = new Option("h", "help", false, "Prints this message.")
      options.addOption(helpOption)
      val verboseOption: org.apache.commons.cli.Option = new Option("v", "verbose", false, "Verbose output.")
      options.addOption(verboseOption)
      val invertOption: org.apache.commons.cli.Option = new Option("I", "invert", false, "Invert spreadsheet axis (categories across the top, names going down).")
      options.addOption(invertOption)
      val columnShiftOption: org.apache.commons.cli.Option = new Option("C", "column_shift", true, "The number of columns to skip before reading consolidated date.")
      options.addOption(columnShiftOption)
      val rowShiftOption: org.apache.commons.cli.Option = new Option("R", "row_shift", true, "The number of rows to skip before reading consolidated date.")
      options.addOption(rowShiftOption)
      val parser: CommandLineParser = new PosixParser
      val cmd: CommandLine = parser.parse(options, args)
      if (cmd.getOptions.length < 1 || cmd.hasOption('h') || !cmd.hasOption('d')) {
        val formatter: HelpFormatter = new HelpFormatter
        formatter.printHelp("scala consolidate360.jar", options)
      }
      else {
        val outputFileName: String = cmd.getOptionValue('o')
        val inputDirectoryName: String = cmd.getOptionValue('d')
        val individualReportDirectoryName: String = cmd.getOptionValue('i')
        var columnShift: Int = 0
        if (cmd.hasOption('C')) {
          columnShift = Integer.parseInt(cmd.getOptionValue('C'))
        }
        var rowShift: Int = 0
        if (cmd.hasOption('R')) {
          rowShift = Integer.parseInt(cmd.getOptionValue('R'))
        }
        val consolidate: org.familysearch.joetools.consolidate360.Consolidate = new Consolidate(inputDirectoryName, columnShift, rowShift, cmd.hasOption('I'), cmd.hasOption('v'))
        consolidate.generate(outputFileName, individualReportDirectoryName, cmd.hasOption('c'), cmd.hasOption('a'))
      }
    }
    catch {
      case e: Exception =>
        System.out.println(e.getMessage)
        e.printStackTrace()
    }
  }
}
