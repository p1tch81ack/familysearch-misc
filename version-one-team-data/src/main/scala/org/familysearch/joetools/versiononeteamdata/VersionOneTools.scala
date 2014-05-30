package org.familysearch.joetools.versiononeteamdata

import java.util.Date
import org.apache.commons.cli._

object VersionOneTools {
  def main(args: Array[String]) {
    val options: Options = new Options
    val userNameOption = new Option("u", "username", true, "The VersionOne user name.  (required)")
    userNameOption.setRequired(true)
    options.addOption(userNameOption)
    val passwordOption = new Option("p", "password", true, "The VersionOne password.  (required)")
    passwordOption.setRequired(true)
    options.addOption(passwordOption)
    val projectOption = new Option("P", "project", true, "A project to be included in the report.  (Multiple can be specified)")
    options.addOption(projectOption)
    val teamOption = new Option("T", "team", true, "A team to be included in the report.  (Multiple can be specified)")
    options.addOption(teamOption)
    val startDateOption = new Option("s", "start_date", true, "The start date for the date range used for including iterations based on their end date.")
    options.addOption(startDateOption)
    val endDateOption = new Option("e", "end_date", true, "The end date for the date range used for including iterations based on their end date.")
    options.addOption(endDateOption)
    val outputFileOption: Option = new Option("o", "output_file", true, "The name of the output file to which the spreadsheet will be saved. (default is 'report.xls'")
    options.addOption(outputFileOption)
    val helpOption: Option = new Option("h", "help", false, "Prints this message.")
    options.addOption(helpOption)
    val verboseOption: Option = new Option("v", "verbose", false, "Verbose output.")
    options.addOption(verboseOption)
    val parser: CommandLineParser = new PosixParser
    try {
      val commandLine: CommandLine = parser.parse(options, args)
      if (commandLine.getOptions.length < 1 || commandLine.hasOption('h')) {
        val formatter: HelpFormatter = new HelpFormatter
        formatter.printHelp("java -jar versiononetools.jar <options>", options)
      }
      else {
        var username: String = null
        if (commandLine.hasOption('u')){
          username = commandLine.getOptionValue('u')
        }
        var password: String = null
        if (commandLine.hasOption('p')){
          password = commandLine.getOptionValue('p')
        }
        var projectNames = List[String]()
        if (commandLine.hasOption('P')){
          for(projectName <- commandLine.getOptionValues('P')){
            projectNames ::= projectName
          }
        }

        var teamNames = List[String]()
        if (commandLine.hasOption('T')){
          for(teamName <- commandLine.getOptionValues('T')){
            teamNames ::= teamName
          }
        }

        if (teamNames.size == 0 && projectNames.size == 0){
          val formatter: HelpFormatter = new HelpFormatter
          println("There needs to be at least one team or project specified.")
          formatter.printHelp("java -jar versiononetools.jar <options>", options)
          System.exit(0)
        }
        var startDate: Date = null
        if (commandLine.hasOption('s')){
          startDate = new Date(commandLine.getOptionValue('s'))
        }
        var endDate: Date = null
        if (commandLine.hasOption('e')){
          endDate = new Date(commandLine.getOptionValue('e'))
        }
        val outputFileName = commandLine.getOptionValue('o', "report.xls")
        val connection: V1Connection = new V1Connection("https://www5.v1host.com/FH-V1/", username, password)
        PrintTeamData.generateSpreadsheet(connection, outputFileName, projectNames, teamNames, startDate, endDate)
      }
    }
    catch {
      case e: MissingOptionException => {
        System.out.println(e.getMessage)
        val formatter: HelpFormatter = new HelpFormatter
        formatter.printHelp("java -jar versiononetools.jar <options>", options)
      }
      case e: Exception => {
        System.out.println(e.getMessage)
        e.printStackTrace()
      }
    }

  }
}
