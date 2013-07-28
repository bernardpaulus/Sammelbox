package collector.desktop.controller.filesystem;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import collector.desktop.controller.settings.ApplicationSettingsManager.ApplicationSettings;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.model.database.exceptions.ExceptionHelper;
import collector.desktop.model.database.operations.DatabaseOperations;
import collector.desktop.view.internationalization.Language;
import collector.desktop.view.managers.AlbumViewManager.AlbumView;

public class FileSystemAccessWrapper {
	public static final String DATABASE_NAME					= "collector.db";
	public static final String DATABASE_TO_RESTORE_NAME			= "collector.restore.db";
	public static final String TEMP_DIR 						= System.getProperty("java.io.tmpdir");
	public static final String USER_HOME 						= System.getProperty("user.home");
	public static final String COLLECTOR_HOME 					= System.getProperty("user.home") + File.separatorChar + ".collector";
	public static final String COLLECTOR_HOME_APPDATA 			= COLLECTOR_HOME + File.separatorChar + "app-data";
	public static final String COLLECTOR_HOME_THUMBNAILS_FOLDER = COLLECTOR_HOME + File.separatorChar + "thumbnails";
	public static final String COLLECTOR_HOME_BACKUPS			= COLLECTOR_HOME + File.separatorChar + "backups"; 
	public static final String COLLECTOR_HOME_ALBUM_PICTURES 	= COLLECTOR_HOME + File.separatorChar + "album-pictures";
	public static final String PLACEHOLDERIMAGE 				= COLLECTOR_HOME_APPDATA + File.separatorChar + "placeholder.png";
	public static final String PLACEHOLDERIMAGE2 				= COLLECTOR_HOME_APPDATA + File.separatorChar + "placeholder2.png";
	public static final String PLACEHOLDERIMAGE3 				= COLLECTOR_HOME_APPDATA + File.separatorChar + "placeholder3.png";
	public static final String ZERO_STARS_IMAGE 				= COLLECTOR_HOME_APPDATA + File.separatorChar + "zerostars.png";
	public static final String ONE_STAR_IMAGE 					= COLLECTOR_HOME_APPDATA + File.separatorChar + "onestar.png";
	public static final String TWO_STARS_IMAGE 					= COLLECTOR_HOME_APPDATA + File.separatorChar + "twostars.png";
	public static final String THREE_STARS_IMAGE 				= COLLECTOR_HOME_APPDATA + File.separatorChar + "threestars.png";
	public static final String FOUR_STARS_IMAGE 				= COLLECTOR_HOME_APPDATA + File.separatorChar + "fourstars.png";
	public static final String FIVE_STARS_IMAGE 				= COLLECTOR_HOME_APPDATA + File.separatorChar + "fivestars.png";
	public static final String LOGO 							= COLLECTOR_HOME_APPDATA + File.separatorChar + "logo.png";
	public static final String LOGO_SMALL 						= COLLECTOR_HOME_APPDATA + File.separatorChar + "logo-small.png";
	public static final String DATABASE 						= COLLECTOR_HOME + File.separatorChar + DATABASE_NAME;
	public static final String DATABASE_TO_RESTORE				= COLLECTOR_HOME + File.separatorChar + DATABASE_TO_RESTORE_NAME;
	public static final String VIEW_FILE						= COLLECTOR_HOME_APPDATA + File.separatorChar + "views.xml";
	public static final String ALBUM_FILE            			= COLLECTOR_HOME_APPDATA + File.separatorChar + "albums.xml";
	public static final String SETTINGS_FILE					= COLLECTOR_HOME_APPDATA + File.separatorChar + "settings.xml";
	public static final String WELCOME_PAGE_FILE				= COLLECTOR_HOME_APPDATA + File.separatorChar + "welcome.xml";
	public static final String LOCK_FILE						= COLLECTOR_HOME_APPDATA + File.separatorChar + ".lock";
	
	private static final boolean OVERWRITE_EXISITING_FILES = true;
	private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemAccessWrapper.class);
	/**
	 *  A simple regex to prevent album names whose folders of the same name cause problems on the filesystem
	 * Minimum length is 3 alphanumeric characters possibly white spaces, underscores (u005F) hyphen_minuses (u002D). 
	 * */
	private static final String albumNameRegex = "^(\\w|\\u005F|\\s|\\u002D){3,}$";
	
	/**
	 * Instance to itself used in the process to located stored resources. 
	 */
	private static FileSystemAccessWrapper instance = new FileSystemAccessWrapper();

	/** This method is used to create and update the file-structure for the collector application.
	 *  During this process no existing directory or file is overwritten.
	 *  This method must be called during initialization of the program.*/
	public static boolean updateCollectorFileStructure() {
		File collectorDirectory = new File(COLLECTOR_HOME);

		if (!collectorDirectory.exists()) {
			if (!collectorDirectory.mkdir()) {
				System.err.println("Cannot create "+ COLLECTOR_HOME+ " directory although it seems that it does not exist");
				return false;
			}
		}

		// Create Album home directory containing all albums		
		File albumHomeDirectory = new File(COLLECTOR_HOME_ALBUM_PICTURES);

		if (!albumHomeDirectory.exists()) {
			if (!albumHomeDirectory.mkdir()) {
				System.err.println("Cannot create " + COLLECTOR_HOME_ALBUM_PICTURES + " although it seems that it does not exist");
				return false;
			}
		}

		// Create thumbnail directory containing all thumbnails		
		File thumbnailsDirectory = new File(COLLECTOR_HOME_THUMBNAILS_FOLDER);

		if (!thumbnailsDirectory.exists()) {
			if (!thumbnailsDirectory.mkdir()) {
				System.err.println("Cannot create " + COLLECTOR_HOME_THUMBNAILS_FOLDER + " although it seems that it does not exist");
				return false;
			}
		}
		
		// Create the application data directory
		File appData = new File(COLLECTOR_HOME_APPDATA);
		if (!appData.exists()) {
			if (!appData.mkdir()) {
				System.err.println("Cannot create collector app-data directory although it seems that it does not exist");
				return false;
			}
		}
		
		// Create the backup directory
		File backups = new File(COLLECTOR_HOME_BACKUPS);
		if (!backups.exists()) {
			if (!backups.mkdir()) {
				System.err.println("Cannot create collector backup directory although it seems that it does not exist");
				return false;
			}
		}
		
		// Add the lock file
		File lockFile = new File(LOCK_FILE);
		try {
			lockFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Add presentation files to application data directory
		File placeholderPNG = new File(PLACEHOLDERIMAGE);
		if (!placeholderPNG.exists() || OVERWRITE_EXISITING_FILES) {		
			copyResourceToFile("graphics/placeholder.png", placeholderPNG);
		}
		
		File placeholder2PNG = new File(PLACEHOLDERIMAGE2);
		if (!placeholder2PNG.exists() || OVERWRITE_EXISITING_FILES) {		
			copyResourceToFile("graphics/placeholder2.png", placeholder2PNG);
		}
		
		File placeholder3PNG = new File(PLACEHOLDERIMAGE3);
		if (!placeholder3PNG.exists() || OVERWRITE_EXISITING_FILES) {		
			copyResourceToFile("graphics/placeholder3.png", placeholder3PNG);
		}

		File zeroStarsImage = new File(ZERO_STARS_IMAGE);
		if (!zeroStarsImage.exists() || OVERWRITE_EXISITING_FILES) {		
			copyResourceToFile("graphics/zerostars.png", zeroStarsImage);
		}
		
		File oneStarImage = new File(ONE_STAR_IMAGE);
		if (!oneStarImage.exists() || OVERWRITE_EXISITING_FILES) {		
			copyResourceToFile("graphics/onestar.png", oneStarImage);
		}
		
		File twoStarsImage = new File(TWO_STARS_IMAGE);
		if (!twoStarsImage.exists() || OVERWRITE_EXISITING_FILES) {		
			copyResourceToFile("graphics/twostars.png", twoStarsImage);
		}
		
		File threeStarsImage = new File(THREE_STARS_IMAGE);
		if (!threeStarsImage.exists() || OVERWRITE_EXISITING_FILES) {		
			copyResourceToFile("graphics/threestars.png", threeStarsImage);
		}
		
		File fourStarsImage = new File(FOUR_STARS_IMAGE);
		if (!fourStarsImage.exists() || OVERWRITE_EXISITING_FILES) {		
			copyResourceToFile("graphics/fourstars.png", fourStarsImage);
		}
		
		File fiveStarsImage = new File(FIVE_STARS_IMAGE);
		if (!fiveStarsImage.exists() || OVERWRITE_EXISITING_FILES) {		
			copyResourceToFile("graphics/fivestars.png", fiveStarsImage);
		}
		
		File logoPNG = new File(LOGO);
		if (!logoPNG.exists() || OVERWRITE_EXISITING_FILES) {		
			copyResourceToFile("graphics/logo.png", logoPNG);
		}
		
		File logoSmallPNG = new File(LOGO_SMALL);
		if (!logoSmallPNG.exists() || OVERWRITE_EXISITING_FILES) {		
			copyResourceToFile("graphics/logo-small.png", logoSmallPNG);
		}
		
		File effectsJS = new File(COLLECTOR_HOME_APPDATA + File.separatorChar + "effects.js");
		if (!effectsJS.exists() || OVERWRITE_EXISITING_FILES) {		
			copyResourceToFile("javascript/effects.js", effectsJS);
		}

		File styleCSS = new File(COLLECTOR_HOME_APPDATA + File.separatorChar + "style.css");
		if (!styleCSS.exists() || OVERWRITE_EXISITING_FILES) {		
			copyResourceToFile("stylesheets/style.css", styleCSS);
		}

		return true;
	}

	public static void copyResourceToFile(String resource, File outputFile) {
		try {
			InputStream istream = instance.getClass().getClassLoader().getResourceAsStream(resource);
			OutputStream ostream = new FileOutputStream(outputFile);

			byte buffer[] = new byte[1024];
			int length;
			
			while((length=istream.read(buffer)) > 0) {
				ostream.write(buffer,0,length);
			}
			
			ostream.close();
			istream.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** This method is used to create album picture folders if the album is set to contain pictures and the folder does not exist yet.
	 *  During this process no existing directory or file is overwritten.
	 *  This method must be called after a new album is created or altered.
	 *  It should be also called during initialization of the program after the collector file structure has been updated. */
	public static boolean updateAlbumFileStructure(Connection databaseConnection) {
		// Create inside the album home directory all album directories
		try {
			for (String albumName : DatabaseOperations.getListOfAllAlbums()) {
				File albumDirectory = new File(getFilePathForAlbum(albumName));

				if (DatabaseOperations.isPictureAlbum(albumName) && !albumDirectory.exists()) {
					if (!albumDirectory.mkdir()) {
						LOGGER.error("Cannot create collector album directory although it seems that it does not exist");
						return false;
					}
				}
			}
			return true;
		} catch (DatabaseWrapperOperationException e) {
			LOGGER.error("Updating the album file structure failed.", e);
			return false;
		}		
	}

	/**
	 * Gets the path to the respective picture folders.
	 * @param albumName The name of the album whose picture folder is requested.
	 * @return The path to the picture directory.
	 */
	public static String getFilePathForAlbum(String albumName) {
		return COLLECTOR_HOME_ALBUM_PICTURES + File.separatorChar + albumName;
	}

	/**
	 * Recursively copies a directory and its content from the source to the target location.
	 * @param sourceLocation The file handle to the source directory.
	 * @param targetLocation The file handle to the target directory.
	 * @throws IOException Exception raised if a problem is encountered during the copy process.
	 */
	public static void copyDirectory(File sourceLocation , File targetLocation)
			throws IOException {

		copyDirectory(sourceLocation, targetLocation, null);
	}
	
	/**
	 * Recursively copies a directory and its content from the source to the target location.
	 * @param sourceLocation The file handle to the source directory.
	 * @param targetLocation The file handle to the target directory.
	 * @param excludeFileRegex Excludes files from being copied which match this regex. Null or empty string will match no file.
	 * @throws IOException Exception raised if a problem is encountered during the copy process.
	 */
	public static void copyDirectory(File sourceLocation , File targetLocation, String excludeFileRegex) throws IOException {

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i=0; i<children.length; i++) {
				copyDirectory(	new File(sourceLocation, children[i]), new File(targetLocation, children[i]), excludeFileRegex);
			}
		} else {
			if (excludeFileRegex != null && !excludeFileRegex.isEmpty() && sourceLocation.getCanonicalFile().getName().matches(excludeFileRegex)) {
				return;
			}
			copyFile(sourceLocation, targetLocation);
		}
	}

	public static void copyFile(File sourceLocation , File targetLocation) throws IOException {
		InputStream in = new FileInputStream(sourceLocation);
		OutputStream out = new FileOutputStream(targetLocation);

		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}
	
	/**
	 * Provides a file which points to the picture location within the application folder.
	 * @param fileNameWithExtension The name including file type extension of the picture.
	 * @param albumName The name of the album to which the picture
	 * @return The file which points to the physical location of the picture.
	 */
	public static File getFileInAppHome(String fileNameWithExtension, String albumName) {
		return new File(COLLECTOR_HOME_ALBUM_PICTURES + File.separatorChar + albumName + File.separatorChar + fileNameWithExtension);
	}

	/**
	 * Rename the old picture folder of the oldAlbumName to the new name. If it did no exist create the new folder anyway.
	 * @param oldAlbumName The old name of the album
	 * @param newAlbumName The new name of the album.
	 * @return True if either the folder was succesfully renamed or the new folder was successfully create. False if either operation failed.
	 */
	public static boolean renameAlbumPictureFolder(String oldAlbumName, String newAlbumName) {
		// Get the standard old picturefolderpath
		File oldalbumPicDir = new File (getFilePathForAlbum(oldAlbumName));
		// Get the standard new picturefolderpath
		File newalbumPicDir = new File(getFilePathForAlbum(newAlbumName));
		// Test if the old exists
		if (!oldalbumPicDir.exists()) {
			// Create the new picture folder anyway
			return newalbumPicDir.mkdir();
		}
		return oldalbumPicDir.renameTo(newalbumPicDir);
	}

	/**
	 * Internal method to add folders to the zip file.
	 * @param zipOutputStream The output stream to which the zip file is written.
	 * @param parentName The name of the parent directory.
	 * @param locationFile The file handle to the source files.
	 */
	private static void addDirectory(ZipOutputStream zipOutputStream, String parentName, File locationFile) {
		// retrieve all sub directories and files from the file/folder
		File[] files = locationFile.listFiles();

		for (int i=0; i < files.length; i++) {
			// if the file is directory, call the function recursively
			if (files[i].isDirectory()) {
				String zipFolderPath = parentName;
				if (parentName.isEmpty()) {
					zipFolderPath = files[i].getName() + "/";//File.separatorChar;	
				} else {
					zipFolderPath += files[i].getName() + "/";// File.separatorChar;
				}
				addDirectory(zipOutputStream, zipFolderPath, files[i]);
				continue;
			}

			// being here means that we have a file and not a directory
			try {
				byte[] buffer = new byte[1024];
				FileInputStream fileInputStream = new FileInputStream(files[i]);
				zipOutputStream.putNextEntry(new ZipEntry(parentName + files[i].getName()));

				// Store the file inside the zip
				int length;
				while((length = fileInputStream.read(buffer)) > 0) {
					zipOutputStream.write(buffer, 0, length);
				}

				// close current zip entry and the input stream of the current file
				zipOutputStream.closeEntry();
				fileInputStream.close();
			}
			catch(IOException ioe){
				LOGGER.error("Adding folder {} to zip file failed.", ioe);				
			}
		}
	}

	/**
	 * Zips a folder recursively into a file.
	 * @param folderLocation The path to the source directory.
	 * @param zipLocation The path to the target zip file.
	 */
	public static void zipFolderToFile(String folderLocation, String zipLocation) {
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(zipLocation);
			ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

			File folderLocationFile = new File(folderLocation);
			addDirectory(zipOutputStream, "", folderLocationFile);

			zipOutputStream.close();
		}
		catch(IOException ioe) {
			LOGGER.error("Zipping a folder into a zip file failed " , ioe);
		}
	}

	/**
	 * Unzips a file to the specified location, recreating the original file structure within it
	 * @param zipLocation The path to the future location of the zip file.
	 * @param folderLocation The path to the source directory.
	 */
	public static void unzipFileToFolder(String zipLocation, String folderLocation) {		
		try {
			ZipFile zipFile = new ZipFile(new File(zipLocation), ZipFile.OPEN_READ);

			Enumeration<? extends ZipEntry> zipFileEntries = zipFile.entries();

			while (zipFileEntries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();

				if (!entry.isDirectory()) {
					File destFile = new File(folderLocation, entry.getName());
					File destinationParent = destFile.getParentFile();

					destinationParent.mkdirs();

					FileOutputStream fileOutputStream = new FileOutputStream(destFile);
					InputStream inputStream = zipFile.getInputStream(entry);

					// Copy the bits from inputStream to fileOutputStream
					byte[] buf = new byte[1024];
					int len;

					while ((len = inputStream.read(buf)) > 0) {
						fileOutputStream.write(buf, 0, len);
					}

					fileOutputStream.close();
				}
			}
			
			zipFile.close();
		} catch (IOException ioe) {
			System.err.println("FileSystemAccessWrapper: " + ioe.getMessage());
		}

	}

	/**
	 * Deletes the specified file
	 * @param pathToFile The path to the file which is to be deleted
	 * @return true if the directory was successfully deleted, false otherwise
	 */
	public static boolean deleteFile(String pathToFile) {
		return deleteFile(new File(pathToFile));
	}
	
	/**
	 * Deletes the specified file
	 * @param file The file which is to be deleted
	 * @return true if the directory was successfully deleted, false otherwise
	 */
	public static boolean deleteFile(File file) {
		return file.delete();
	}
	
	/**
	 * Deletes all content within the specified directory.
	 * @param directory The file to the directory which is to be deleted.
	 * @return true if the directory was successfully deleted, false otherwise
	 */
	public static boolean deleteDirectoryRecursively(File directory) {
		if( directory.exists() ) {
			if (directory.isDirectory()) {
				File[] files = directory.listFiles();
				for(int i=0; i<files.length; i++) {
					if(files[i].isDirectory()) {
						deleteDirectoryRecursively(files[i]);
					} else {
						files[i].delete();
					}
				}
			}
		}
		
		return(directory.delete());
	}

	/**
	 * Convenience method to erase all content from the default application home directory.
	 * The .collector directory as well as the database will still exist after calling this method. 
	 */
	public static void clearCollectorHome() {	
		File[] files = new File(COLLECTOR_HOME).listFiles();

		for (int i=0; i<files.length; i++) {
			if (files[i].isDirectory()) {
				deleteDirectoryRecursively(files[i]);
			} else if (!files[i].getName().equals(DATABASE_NAME)) {
				files[i].delete();
			}
		}
	}

	/**
	 * Convenience method to remove the default application home directory. 
	 */
	public static void removeCollectorHome() {
		File collectorHome = new File(COLLECTOR_HOME);

		if (collectorHome.exists()) {
			FileSystemAccessWrapper.clearCollectorHome();

			new File(FileSystemAccessWrapper.DATABASE).delete();

			collectorHome.delete();
		}
	}

	/**
	 * Convenience method to delete the temporary database file {@link #DATABASE_TO_RESTORE}
	 * @return true if and only if the file or directory is successfully deleted; false otherwise 
	 */
	public static boolean deleteDatabaseRestoreFile() {
		File file = new File(DATABASE_TO_RESTORE);
		return file.delete();
	}
	
	/**
	 * Retrieves a list of files whose file name matches the provided regex. Only files in the appdate folder (no directories) are matched.
	 * @param fileNameRegex The regular expression to match files in the collector home against.
	 * @return A list of file handles to the matching files. Empty list if none are found.
	 */
	public static List<File> getAllMatchingFilesInCollectorHome(String fileNameRegex) {
		List<File> matchingFiles = new ArrayList<File>();
		File path = new File(COLLECTOR_HOME_APPDATA);
		File[] files = path.listFiles();
		for(int i=0; i<files.length; i++) {
			if(!files[i].isDirectory()) {
				File currentFile;
				try {
					currentFile = files[i].getCanonicalFile();
				} catch (IOException ex) {
					LOGGER.error("An error occured while processing files \n Stacktrace: " + ExceptionHelper.toString(ex));
					continue;
				}
				if (currentFile.getName().matches(fileNameRegex)){
					matchingFiles.add(currentFile);
				}
			}
		}		
		return matchingFiles;		
	}

	public static void writeToFile(String content, String filepath) {
		try {
			PrintWriter out = new PrintWriter(filepath);

			out.write(content);

			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void storeViews(Map<String, List<AlbumView>> albumNamesToAlbumViews) {
		StringBuilder xmlOutput = new StringBuilder();
		
		xmlOutput.append("<views>\n");
		
		for (String albumName : albumNamesToAlbumViews.keySet()) {
			for (AlbumView albumView : albumNamesToAlbumViews.get(albumName)) {
				xmlOutput.append("\t<view>\n");
				xmlOutput.append("\t\t<name><![CDATA[" + albumView.getName() + "]]></name>\n");
				xmlOutput.append("\t\t<album><![CDATA[" + albumView.getAlbum() + "]]></album>\n");
				xmlOutput.append("\t\t<sqlQuery><![CDATA[" + albumView.getSqlQuery() + "]]></sqlQuery>\n");
				xmlOutput.append("\t</view>\n");
			}
		}
		
		xmlOutput.append("</views>\n");
		
		writeToFile(xmlOutput.toString(), VIEW_FILE);
	}

	public static Map<String, List<AlbumView>> loadViews() {
		String albumViewsAsXml = readFileAsString(VIEW_FILE);
		
		Map<String, List<AlbumView>> albumNamesToAlbumViews = new HashMap<String, List<AlbumView>>();
		
		if (albumViewsAsXml.isEmpty()) {
			return albumNamesToAlbumViews;
		}
		
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			InputSource inputSource = new InputSource();
			inputSource.setCharacterStream(new StringReader(albumViewsAsXml));

			Document document = documentBuilder.parse(inputSource);
			Node root = document.getFirstChild();

			if (!root.getNodeName().equals("views")) {
				throw new Exception("Invalid Album View File");
			} else {
				NodeList viewNodes = document.getElementsByTagName("view");
				
				String name = "";
				String album = "";
				String sqlQuery = "";
				
				for (int i = 0; i < viewNodes.getLength(); i++) {
					Node node = viewNodes.item(i);

					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) node;
						
						name = getValue("name", element);
						album = getValue("album", element);
						sqlQuery = getValue("sqlQuery", element);
						
						if (albumNamesToAlbumViews.get(album) == null) {
							List<AlbumView> albumViews = new LinkedList<>();
							albumViews.add(new AlbumView(name, album, sqlQuery));
							albumNamesToAlbumViews.put(album, albumViews);
						} else {
							List<AlbumView> albumViews = albumNamesToAlbumViews.get(album);
							albumViews.add(new AlbumView(name, album, sqlQuery));
							albumNamesToAlbumViews.put(album, albumViews);
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return albumNamesToAlbumViews;
	}

	private static String getValue(String tag, Element element) {
		NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
		Node node = (Node) nodes.item(0);
		return node.getNodeValue();
	}

	
	/** Reads the specified file into a string
	 * @param filePath the path to the file that should be read
	 * @return the content of the specified file as a string or an empty string if the file does not exist */
	public static String readFileAsString(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			return "";
		}

		byte[] buffer = new byte[(int) new File(filePath).length()];

		try {
			BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(filePath));
			bufferedInputStream.read(buffer);
			bufferedInputStream.close();

			return new String(buffer);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new String(buffer);
	}

	public static void storeWelcomePageManagerInformation(
			Map<String, Integer> albumAndViewsToClicks,
			Map<String, Long> albumToLastModified) {
		
		StringBuilder xmlOutput = new StringBuilder();
		
		xmlOutput.append("<welcomePageInformation>\n");
		
		for (String albumOrViewName : albumAndViewsToClicks.keySet()) {
			xmlOutput.append("\t<albumAndViewsToClicks>\n");
			xmlOutput.append("\t\t<name><![CDATA[" + albumOrViewName + "]]></name>\n");
			xmlOutput.append("\t\t<clicks><![CDATA[" + albumAndViewsToClicks.get(albumOrViewName) + "]]></clicks>\n");
			xmlOutput.append("\t</albumAndViewsToClicks>\n");
		}
		
		for (String albumName : albumToLastModified.keySet()) {
			xmlOutput.append("\t<albumToLastModified>\n");
			xmlOutput.append("\t\t<albumName><![CDATA[" + albumName + "]]></albumName>\n");
			xmlOutput.append("\t\t<lastModified><![CDATA[" + albumToLastModified.get(albumName) + "]]></lastModified>\n");
			xmlOutput.append("\t</albumToLastModified>\n");
		}
		
		xmlOutput.append("</welcomePageInformation>\n");
		
		writeToFile(xmlOutput.toString(), WELCOME_PAGE_FILE);
	}

	public static Map<String, Integer> getAlbumAndViewsToClicks() {
		String welcomePageInformationXml = readFileAsString(WELCOME_PAGE_FILE);
		
		Map<String, Integer> albumAndViewsToClicks = new HashMap<String, Integer>();
		
		if (welcomePageInformationXml.isEmpty()) {
			return albumAndViewsToClicks;
		}
		
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			InputSource inputSource = new InputSource();
			inputSource.setCharacterStream(new StringReader(welcomePageInformationXml));

			Document document = documentBuilder.parse(inputSource);
			Node root = document.getFirstChild();

			if (!root.getNodeName().equals("welcomePageInformation")) {
				throw new Exception("Invalid Welcome Page File");
			} else {
				NodeList viewNodes = document.getElementsByTagName("albumAndViewsToClicks");
				
				String name = "";
				Integer clicks = 0;

				for (int i = 0; i < viewNodes.getLength(); i++) {
					Node node = viewNodes.item(i);

					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) node;
						
						name = getValue("name", element);
						clicks = Integer.parseInt(getValue("clicks", element));
						
						albumAndViewsToClicks.put(name, clicks);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return albumAndViewsToClicks;
	}

	public static Map<String, Long> getAlbumToLastModified() {
		String welcomePageInformationXml = readFileAsString(WELCOME_PAGE_FILE);
		
		Map<String, Long> albumToLastModified = new HashMap<String, Long>();
		
		if (welcomePageInformationXml.isEmpty()) {
			return albumToLastModified;
		}
		
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			InputSource inputSource = new InputSource();
			inputSource.setCharacterStream(new StringReader(welcomePageInformationXml));

			Document document = documentBuilder.parse(inputSource);
			Node root = document.getFirstChild();

			if (!root.getNodeName().equals("welcomePageInformation")) {
				throw new Exception("Invalid Welcome Page File");
			} else {
				NodeList viewNodes = document.getElementsByTagName("albumToLastModified");
				
				String name = "";
				Long lastModified = 0L;

				for (int i = 0; i < viewNodes.getLength(); i++) {
					Node node = viewNodes.item(i);

					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) node;
						
						name = getValue("albumName", element);
						lastModified = Long.parseLong(getValue("lastModified", element));
						
						albumToLastModified.put(name, lastModified);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return albumToLastModified;
	}
	
	public static String readInputStreamIntoString(InputStream fileInputStream) {
		StringBuilder stringBuilder = new StringBuilder();

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
			String line = null;
			String ls = System.getProperty("line.separator");
			while((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}
		} catch (IOException ioe) {
			System.err.println(ioe.getMessage());
		}

		return stringBuilder.toString();
	}
	
	public static void storeAlbums(Collection<String> albums) {
		StringBuilder xmlOutput = new StringBuilder();
		
		xmlOutput.append("<albums>\n");
		
		for (String album : albums) {
			xmlOutput.append("\t<album>\n");
			xmlOutput.append("\t\t<name><![CDATA[" + album + "]]></name>\n");
			xmlOutput.append("\t</album>\n");
		}
		
		xmlOutput.append("</albums>\n");
		
		writeToFile(xmlOutput.toString(), ALBUM_FILE);
	}

	public static List<String> loadAlbums() {
		String albumsAsXml = readFileAsString(ALBUM_FILE);
		
		List<String> albumToPosition = new LinkedList<String>();
		
		if (albumsAsXml.isEmpty()) {
			return albumToPosition;
		}
		
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			InputSource inputSource = new InputSource();
			inputSource.setCharacterStream(new StringReader(albumsAsXml));

			Document document = documentBuilder.parse(inputSource);
			Node root = document.getFirstChild();

			if (!root.getNodeName().equals("albums")) {
				throw new Exception("Invalid Album File");
			} else {
				NodeList viewNodes = document.getElementsByTagName("album");
				
				String name = "";
				
				for (int i = 0; i < viewNodes.getLength(); i++) {
					Node node = viewNodes.item(i);

					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) node;
						
						name = getValue("name", element);
						
						albumToPosition.add(name);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return albumToPosition;
	}
	
	public static String getFileExtension(String fileNameOrPath) {
		String extension = "";

		int i = fileNameOrPath.lastIndexOf('.');
		if (i > 0) {
		    extension = fileNameOrPath.substring(i + 1);
		}
		
		return extension;
	}
	
	public static boolean isNameFileSystemCompliant(String name) {
		if (!name.matches(albumNameRegex)) {
			return false;
		}
		
		// Tests if an album with 'name' could actually create a valid albumPictureFolder		
		File testFile = new File (getFilePathForAlbum(name));
		try {
			testFile.getCanonicalPath();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public static ApplicationSettings loadSettings() {
		String applicationSettingsAsXml = readFileAsString(SETTINGS_FILE);
		
		ApplicationSettings applicationSettings = new ApplicationSettings();
		
		if (applicationSettingsAsXml.isEmpty()) {
			return applicationSettings;
		}
		
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			InputSource inputSource = new InputSource();
			inputSource.setCharacterStream(new StringReader(applicationSettingsAsXml));

			Document document = documentBuilder.parse(inputSource);
			Node root = document.getFirstChild();

			if (!root.getNodeName().equals("settings")) {
				throw new Exception("Invalid Settings File");
			} else {
				NodeList settingNodes = root.getChildNodes();
								
				for (int i = 0; i < settingNodes.getLength(); i++) {
					Node node = settingNodes.item(i);

					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) node;
						
						if (element.getNodeName().equals("userDefinedLanguage")) {
							applicationSettings.setUserDefinedLanguage(Language.valueOf(element.getFirstChild().getNodeValue()));
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return applicationSettings;
	}

	public static void storeSettings(ApplicationSettings applicationSettings) {
		StringBuilder xmlOutput = new StringBuilder();
		
		xmlOutput.append("<settings>\n");
		xmlOutput.append("\t<userDefinedLanguage>" + applicationSettings.getUserDefinedLanguage().toString() + "</userDefinedLanguage>\n");
		xmlOutput.append("</settings>\n");
		
		writeToFile(xmlOutput.toString(), SETTINGS_FILE);
	}
}
