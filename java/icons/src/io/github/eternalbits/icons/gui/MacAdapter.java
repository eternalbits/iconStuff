/*
 * Copyright 2016 Rui Baptista
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

package io.github.eternalbits.icons.gui;

import java.awt.Desktop;
import java.awt.Taskbar;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.AboutHandler;
import java.awt.desktop.PreferencesEvent;
import java.awt.desktop.PreferencesHandler;
import java.awt.desktop.QuitStrategy;

import javax.swing.ImageIcon;

/**
 * A minimal adapter to delegate the default macOS Menu requests to
 *  the Front End object.
 * <p>
 */
class MacAdapter {

	private final FrontEnd mainFrame;
	
	/**
	 * Delegates the default macOS Menu requests to the {@code FrontEnd}.
	 */
	MacAdapter(FrontEnd frontEnd) {
		mainFrame = frontEnd;
		mainFrame.setIconImage(new ImageIcon(FrontEnd.getResource("macos.png")).getImage());
		
		Desktop macApp = Desktop.getDesktop();
		
		Taskbar.getTaskbar().setIconImage(mainFrame.getIconImage());
		macApp.setQuitStrategy(QuitStrategy.CLOSE_ALL_WINDOWS);
		
		macApp.setAboutHandler(new AboutHandler() {
			@Override
			public void handleAbout(AboutEvent arg0) {
				mainFrame.showAboutDialog();
			}
		});
		
		macApp.setPreferencesHandler(new PreferencesHandler() {
			@Override
			public void handlePreferences(PreferencesEvent arg0) {
				mainFrame.editSettings();
			}
		});
		
	}

}
