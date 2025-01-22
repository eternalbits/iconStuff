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

import java.io.File;

/**
 * Utility static functions for Icons.
 */
public class Static {

	/**
	 * Returns a simple description of exception {@code e}, like {@link Throwable#toString()}
	 *  with exception simpleName instead of name.
	 *  
	 * @param e	The exception object.
	 * @return	A simple representation of {@code e}.
	 */
	public static String simpleString(Exception e) {
		return e.getClass().getSimpleName()+": "+e.getLocalizedMessage();
	}

	/**
	 * Accepts a {@code path} and returns the extension, if extension does not exist
	 *  returns {@code ""}.
	 *
	 * @param path	The file path.
	 * @return	The extension or {@code ""}.
	 */
	public static String getExtension(File path) {
		String name = path.getName();
		String part = name.replaceFirst("([^.]+)[.][^.]+$", "$1");
		return part.equals(name)? "": name.substring(part.length()+1);
	}
	
	/**
	 * A {@code String} {@code item} is a number possibly followed by several letters,
	 *  from which you simply want to extract the number.
	 *
	 * @param item	Number followed by several letters and numbers.
	 * @return	Just the number or {@code -1}.
	 */
	public static int getInteger(String item) {
		try {	// tries to return a number, if it fails returns -1
			return Integer.parseInt(item.split(" ")[0]);
		} catch (NumberFormatException e) {
			return -1;
		}
	}
	
	/**
	 * Returns an integer {@code num} rounded up. Does not work with dividend 0 (zero).
	 *
	 * @param num	An integer value that you want rounded up.
	 * @param div	The number of digits to which you want to round number.
	 * @return	Rounds a number up.
	 */
	public static int roundUp(int num, int div) {
		return (num + div - 1) / div * div;
	}
	
	/**
	 * Returns the roundness of the Apple as a function of a number 3 or 4 and
	 *  the size of the side of the square.
	 *
	 * @param num	Number to be rounded up, usually a 3 or a 4.
	 * @param width	The number of digits to which you want to round number.
	 * @return	Result of expression.
	 */
	public static int appleRound(int num, int width) {
		return num * width * width + Static.roundUp(num * width * width, 0x7F) / 0x7F;
	}
	
}
