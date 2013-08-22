/** -----------------------------------------------------------------
 *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
 *    Copyright (C) 2011 Jérôme Wagener & Paul Bicheler
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ** ----------------------------------------------------------------- */

package org.sammelbox;

import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.eclipse.swt.SWT;
import org.sammelbox.controller.filesystem.FileSystemAccessWrapper;
import org.sammelbox.controller.filesystem.FileSystemLocations;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.controller.managers.BuildInformationManager;
import org.sammelbox.controller.managers.ConnectionManager;
import org.sammelbox.controller.managers.WelcomePageManager;
import org.sammelbox.controller.settings.SettingsManager;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.various.ComponentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sammelbox {
	private final static Logger LOGGER = LoggerFactory.getLogger(Sammelbox.class);
	
	/**
	 * This method initializes the file structure and opens the database connections.
	 * @throws Exception Either a class not found exception if the JDBC driver could not be initialized or
	 * an exception if the database connection could not be established.
	 */
	private static void setupConnectionAndFilesystem() throws Exception {		
		Class.forName("org.sqlite.JDBC");
		
		try {
			ConnectionManager.openConnection();
		} catch (DatabaseWrapperOperationException ex) {
			try {
				LOGGER.warn("Couldn't open a database connection. Will try to open a clean connection instead.");
				ConnectionManager.openCleanConnection();	
			} catch (DatabaseWrapperOperationException ex2) {
				LOGGER.error("The database is corrupt since opening a connection failed. " +
						"Recent autosaves of the database can be found in: " + FileSystemLocations.getBackupDir());
				
				ComponentFactory.getMessageBox(ApplicationUI.getShell(), 
						Translator.get(DictKeys.DIALOG_TITLE_SAMMELBOX_CANT_BE_LAUNCHED), 
						Translator.get(DictKeys.DIALOG_CONTENT_SAMMELBOX_CANT_BE_LAUNCHED), SWT.ERROR).open();
			}			
		}
	}
	
	/** The main method initializes the database (using the constructor) and establishes the user interface */
	public static void main(String[] args) throws ClassNotFoundException {
		LOGGER.info("Sammelbox (build: " + BuildInformationManager.instance().getVersion() + 
				" build on " + BuildInformationManager.instance().getBuildTimeStamp() + ") started");
		try {
			// Ensure that the folder structure including the lock file exists before locking
			FileSystemLocations.setActiveHomeDir(FileSystemLocations.DEFAULT_SAMMELBOX_HOME);
			FileSystemAccessWrapper.updateSammelboxFileStructure();

			// Load available files
			SettingsManager.initializeFromSettingsFile();
			WelcomePageManager.initializeFromWelcomeFile();
			Translator.setLanguageFromSettingsOrSystem();
			
			RandomAccessFile lockFile = new RandomAccessFile(FileSystemLocations.getLockFile(), "rw");
			FileChannel fileChannel = lockFile.getChannel();

			if (fileChannel.tryLock() != null) {
				// Initialize the Database connection
				setupConnectionAndFilesystem();

				// create the shell and show the user interface. This blocks until the shell is closed
				ApplicationUI.initialize(ApplicationUI.getShell());

				// close the database connection if the the shell is closed
				ConnectionManager.closeConnection();

				// close file & channel
				fileChannel.close();
				lockFile.close();
			} else {
				ComponentFactory.getMessageBox(ApplicationUI.getShell(), 
						Translator.get(DictKeys.DIALOG_TITLE_PROGRAM_IS_RUNNING), 
						Translator.get(DictKeys.DIALOG_TITLE_PROGRAM_IS_RUNNING), 
						SWT.ICON_INFORMATION).open();
			}
		} catch (Exception ex) {
			LOGGER.error("Sammelbox crashed", ex);
		} finally {
			LOGGER.info("Sammelbox stopped");
		}
	}
}
