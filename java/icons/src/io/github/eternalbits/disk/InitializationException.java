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

package io.github.eternalbits.disk;

import java.io.IOException;

/**
 * This exception is thrown by an object when there are problems reading the file.
 */
public class InitializationException extends IOException {
	private static final long serialVersionUID = 8905233065966537122L;

	public InitializationException(Class<?> err, String src) {
		super(String.format(DiskImage.CANT_INITIALIZE, err.getSimpleName(), src));
	}
	
	public InitializationException(String msg) {
		super(msg);
	}
	
}
