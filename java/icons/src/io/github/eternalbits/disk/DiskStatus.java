/*
 * Copyright 2026 Rui Baptista
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

import java.util.ArrayList;

/**
 * A simple undo/redo for actions in java.
 * <p>
 */
public class DiskStatus {
	private ArrayList<Status> edits = new ArrayList<>();
	private int index = 0;
	
	public DiskStatus() {}
	
	public void clear() {
		edits.clear();
		index = 0;
	}
	
	public boolean canUndo() {
		return index > 0;
	}
	
	public boolean canRedo() {
		return edits.size() > 0 && index < edits.size();
	}
	
	public int setStatus(int i, int copy, String previous, String current) {
		edits.add(index++, new Status(i, copy, previous, current));
		for (int m = edits.size() - 1; m >= index; m--) 
			edits.remove(m);
		return edits.size();
	}
	
	public Status setUndo() {
		int m = index - 1;
		if (m >= 0 && m < edits.size())
			return edits.get(index = m);
		return null;
	}
	
	public Status setRedo() {
		int m = index + 1;
		if (m > 0 && m <= edits.size())
			return edits.get((index = m) - 1);
		return null;
	}
	
	public class Status {
		public final int	i;			// Index of the genuine item
		public final int	copy;		// COPY_FULL, COPY_DELETE, COPY_TYPE, COPY_SIZE, COPY_ICON
		public final String	previous;	// Value of the item after the respective change
		public final String	current;	// Value of the item before the respective change
		private Status(int i, int copy, String previous, String current) {
			this.i			= i;
			this.copy		= copy;
			this.previous	= previous;
			this.current	= current;
		}
	}
}
