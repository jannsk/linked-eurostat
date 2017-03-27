package com.ontologycentral.estatwrap;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.ontologycentral.extatwrap.handler.DataHandler;
import com.ontologycentral.extatwrap.handler.DictionaryHandler;
import com.ontologycentral.extatwrap.handler.DsdHandler;

public class Main {
	public static void main(String[] args) throws IOException, XMLStreamException {
		Options options = new Options();
		
		Option outputO = new Option("o", "name of file to write, - for stdout");
		outputO.setArgs(1);
		options.addOption(outputO);

		Option idO = new Option("i", "name of Eurostat id (e.g., tsieb010)");
		idO.setArgs(1);
		options.addOption(idO);

		Option dicO = new Option("d", "name of Eurostat dic (e.g., geo)");
		dicO.setArgs(1);
		options.addOption(dicO);
		
		Option type = new Option("type", "type (e.g., data or dsd). Only works with i");
		type.setArgs(1);
		options.addOption(type);
		
		Option helpO = new Option("h", "help", false, "print help");
		options.addOption(helpO);
		
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);
			
			if (!(cmd.hasOption("i") || cmd.hasOption("d"))) {
				throw new ParseException("specify either -i or -d");
			}
			if (cmd.hasOption("i") && !(cmd.hasOption("type"))) {
				throw new ParseException("specify either -type");
			}
			if (cmd.hasOption("type") && !(cmd.getOptionValue("type").equals("data") || cmd.getOptionValue("type").equals("dsd"))) {
				throw new ParseException("-type does only support data and dsd");
			}
		} catch (ParseException e) {
			System.err.println("***ERROR: " + e.getClass() + ": " + e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("parameters:", options );
			return;
		}
		
		if (cmd.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("parameters:", options );
			return;
		}

		PrintStream out = System.out;
		
		if (cmd.hasOption("o")) {
			if (cmd.getOptionValue("o").equals("-")) {
				out = System.out;
			} else {
				out = new PrintStream(new FileOutputStream(cmd.getOptionValue("o")));
			}
		}
		
		String id = null;
		
		try {
			if (cmd.hasOption("i")) {
				id = cmd.getOptionValue("i");
				if (cmd.getOptionValue("type").equals("dsd")) {
					System.out.println("Running DsdHandler");
					new DsdHandler().perform(id, ".", out);
				}
				if (cmd.getOptionValue("type").equals("data")) {
					System.out.println("Running DataHandler");
					new DataHandler().perform(id, ".", out);
				}
			} else if (cmd.hasOption("d")) {
				id = cmd.getOptionValue("d");
				System.out.println("Running DictionaryHandler");
				new DictionaryHandler().perform(id, out);
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		out.close();
	}

}
