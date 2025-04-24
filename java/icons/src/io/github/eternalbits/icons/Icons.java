/*
 * Copyright 2024 Rui Baptista
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.eternalbits.icons;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import io.github.eternalbits.disk.DiskIcons;
import io.github.eternalbits.disk.DiskImage;
import io.github.eternalbits.disk.DiskImageShow;
import io.github.eternalbits.disk.WrongHeaderException;
import io.github.eternalbits.icons.gui.FrontEnd;

/**
 * Convert ICO and ICNS disk images line interface. Without parameters
 * the GUI takes precedence, use {@code --help} to get the command list.
 * <p>
 */
public class Icons {

	private final static String IMAGE_CREATED = "The '%s' was created in the '%s' directory.";
	private final static String IMAGE_NOT_CREATED = "No icons were created.";
	
	/**
	 * Outputs --dump to a file as described in {@link DiskImageShow}.
	 * 
	 * @param file	File we want to get a --dump of.
	 */
	private void showView(File file) throws IOException {
		try (DiskIcons image = DiskImage.open(file, "r")) {
			dump(image.getShow());
		}
	}
	
	/**
	 * Copies a file (ICO, ICNS or PNG) to another file, replacing everything.
	 * 
	 * @param from	File to be copied.
	 * @param to	File we want to overlay.
	 * @param type	Extension type: ico, icns or png.
	 * @param icon	A list with the icon and output.
	 */
	private void copy(File from, File to, String type, String icon) throws IOException, WrongHeaderException {
		File copy = null;
		try (DiskIcons image = DiskImage.open(from, "r")) {
			try (DiskIcons clone = DiskImage.create(type, to, image, icon)) {
				copy = to; // copy open by DiskImage
			}
		}
		finally {
			if (copy != null) {
				if (copy.isFile() && copy.length() == 0) 
					copy.delete();
				System.out.println(to != null && to.isFile()? String.format(IMAGE_CREATED, 
						to.getName(), to.getAbsoluteFile().getParent()): IMAGE_NOT_CREATED);
			}
		}
	}

	private final static String[] DEFAULT_FILE_FILTER = {null, "icns", "ico", "png"};
	private final static String FILES_ARE_DUPLICATED = "File \"%s\" is the same as the old image!";
	private final static String FILE_ALREADY_EXISTS = "File \"%s\" already exists";
	private final static String INCORRECT_COMMAND = "The syntax of the command is incorrect.";
	private final static String TOO_MANY_OPTIONS = "There are too many options: %s.";
	
	private static final String version = "1.4";
	private static final String year = "-2025";
	private static final String jar = new java.io.File(Icons.class.getProtectionDomain()
			.getCodeSource().getLocation().getPath()).getName();

	/**
	 * Java main method. Here it all begins!
	 * 
	 * @param args	Parameters as described in --help.
	 */
	public static void main(String[] args) throws Exception {
		
		if (args.length == 0 && !GraphicsEnvironment.isHeadless()) {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					new FrontEnd();
				}
			});
			return;
		}
		
		new Icons().commandLine(args);
		
	}

	/**
	 * Parameter descriptions.
	 */
	private static Options buildHelpers() {
		Options helpers = new Options();
		helpers.addOption(Option.builder("h").longOpt("help").desc("print this help and exit").build());
		helpers.addOption(Option.builder("v").longOpt("version").desc("print version information and exit").build());
		return helpers;
	}

	private static Options buildOptions() {
		Options options = new Options();
		OptionGroup source = new OptionGroup();
		source.addOption(Option.builder("c").longOpt("copy").desc("copy <src> to a new image").hasArgs().argName("src").build());
		source.addOption(Option.builder("d").longOpt("dump").desc("print <src> disk image details").hasArgs().argName("src").build());
		source.setRequired(true);
		options.addOptionGroup(source);
		options.addOption(Option.builder("w").longOpt("write").desc("set <out> as destination file for copy").hasArgs().argName("out").build());
		options.addOption(Option.builder("f").longOpt("format").desc("copy output format: ICNS, ICO or PNG").hasArgs().argName("fmt").build());
		options.addOption(Option.builder("i").longOpt("icon").desc("a list with the icon and output").hasArgs().argName("ico").build());
		options.addOption(Option.builder("o").longOpt("overwrite").desc("overwrite existing file on copy").build());
		return options;
	}
	
	/**
	 * The cmd command accepts only one opt line.
	 * 
	 * @param cmd	Command line.
	 * @param opt	Like "c" or "copy".
	 * @return	File pointed to by opt.
	 */
	private File getOptionValues(CommandLine cmd, String opt) throws ParseException {
		if (cmd.getOptionValues(opt).length != 1)
			throw new ParseException(String.format(TOO_MANY_OPTIONS, opt));
		return new File(cmd.getOptionValue(opt));
	}
	
	/**
	 * The program was called with parameters.
	 * 
	 * @param args	Parameters as described in --help.
	 */
	private void commandLine(String[] args) {
		
		Options options = buildOptions();
		Options helpers = buildHelpers();
		for (Option opt: helpers.getOptions()) {
			options.addOption(opt);
		}
		try {
			if (args.length == 1) {
				File file = new File(args[0]);
				if (file.exists() && file.isFile()) {
					showView(file);
					return;
				}
			}
			if (args.length == 2) {
				File from = new File(args[0]);
				File to = new File(args[1]);
				if ((from.exists() && from.isFile())
						&& !to.exists()) {
					String f = Static.getExtension(to).toLowerCase();
					if (!Arrays.asList(DEFAULT_FILE_FILTER).contains(f))
						throw new ParseException(INCORRECT_COMMAND);
					copy(from, to, f, null);
					return;
				}
			}
			
			CommandLine cmd = new DefaultParser().parse(helpers, args, true);
			if (cmd.hasOption("version")) {
				printAbout();
				return;
			}
			if (cmd.hasOption("help")) {
				printHelp(options);
				return;
			}
			
			cmd = new DefaultParser().parse(options, args);
			
			if (cmd.hasOption("c")) {
				
				if (!cmd.hasOption("w"))
					throw new ParseException(INCORRECT_COMMAND);
				
				File from = getOptionValues(cmd, "c");
				File to = getOptionValues(cmd, "w");
				
				if (!cmd.hasOption("o") && to.exists())
					throw new ParseException(String.format(FILE_ALREADY_EXISTS, to));
				
				if (from.equals(to))
					throw new ParseException(String.format(FILES_ARE_DUPLICATED, to));
				
				String f = cmd.hasOption("f")? cmd.getOptionValue("f").toLowerCase(): null;
				if (cmd.hasOption("f") && cmd.getOptionValues("f").length != 1)
					throw new ParseException(String.format(TOO_MANY_OPTIONS, "f"));
				if (f == null) 
					f = Static.getExtension(to).toLowerCase();
				if (!Arrays.asList(DEFAULT_FILE_FILTER).contains(f))
					throw new ParseException(INCORRECT_COMMAND);
				
				if (cmd.hasOption("i") && cmd.getOptionValues("i").length != 1)
					throw new ParseException(String.format(TOO_MANY_OPTIONS, "i"));
								
				copy(from, to, f, cmd.getOptionValue("i"));
				return;
			}
			
			if (cmd.hasOption("w") || cmd.hasOption("o") || cmd.hasOption("f") || cmd.hasOption("i"))
				throw new ParseException(INCORRECT_COMMAND);
			
			if (cmd.hasOption("d")) {
				showView(getOptionValues(cmd, "d"));
				return;
			}
			
		} catch (ParseException | IOException | WrongHeaderException e) {
			printHelp(options);
			System.out.println("\n\n"+Static.simpleString(e));
			System.exit(1);
		}

	}

	/**
	 * Called with "--version" or "--help" parameters.
	 */
	private static void printAbout() {
		System.out.println("Icons version "+version+" copyright 2024"+year+" Rui Baptista");
		System.out.println("Licensed under the Apache License, Version 2.0.");
	}
	
	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.setSyntaxPrefix("Usage: ");
		final String prefix = "--";
		String header = "\nTo convert ICO to ICNS disk icons. Version "+version+"\n\n";
		String footer = ("\nOne of ^copy or ^dump is required\n^icon can be a phrase like\n"
				+ "2=bit;3=*;4=64:bit;?=48:bit;?=24:bit for ICO or\n"
				+ "1=ic05:png;2=ic04:bit;3=*;?=128:png for ICNS\n\n").replace("^", prefix);
		formatter.setLongOptPrefix(" "+prefix);
		formatter.printHelp("java -jar "+jar, header, options, footer, true);
	}
	
	/**
	 * Outputs --dump to a file as described in {@link DiskImageShow}.
	 * 
	 * @param obj	Output object.
	 */
	public static void dump(Object obj) { dump(obj, ""); }
	private static void dump(Object obj, String in) {
		for (Field fld: obj.getClass().getDeclaredFields()) {
			try {
				if (!fld.getName().startsWith("this$")) {
					if (!Modifier.isPrivate(fld.getModifiers())) {
						if (fld.getAnnotation(Deprecated.class) == null) {
							if (!fld.getType().isAssignableFrom(List.class)) {
								System.out.println(in+fld.getName()+": "+fld.get(obj));
							} else {
								int i = 0;
								for (Object item: (List<?>)fld.get(obj)) {
									System.out.println(in+fld.getName()+"["+i+"]");
									dump(item, in+"    ");
									i++;
								}
							}
						}
					}
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

}
