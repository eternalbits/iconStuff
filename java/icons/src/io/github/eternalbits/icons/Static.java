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
 * 
 * <p>
 */
public class Static {

	/**
	 * Returns a simple description of exception {@code e}, like {@link Throwable#toString()}
	 *  with exception simpleName instead of name.
	 * @param e	The exception object.
	 * @return	A simple representation of {@code e}.
	 */
	public static String simpleString(Exception e) {
		return e.getClass().getSimpleName()+": "+e.getLocalizedMessage();
	}

	/**
	 * 
	 * @param path	.
	 * @return	.
	 */
	public static String getExtension(File path) {
		String name = path.getName();
		String part = name.replaceFirst("([^.]+)[.][^.]+$", "$1");
		return part.equals(name)? "": name.substring(part.length()+1);
	}
	
}
