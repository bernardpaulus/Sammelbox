package collector.desktop.filesystem;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuildInformation {
	private static final Logger LOGGER = LoggerFactory.getLogger(BuildInformation.class);
	
	public enum BuildType {
		/** 
		 * A SNAPSHOT build has no restrictions and everything is allowed. Nothing is guaranteed within such a build.
		 */
		SNAPSHOT,
		
		/**
		 * A DEVELOPMENT build generally works but might contain minor bugs and issues
		 */
	    DEVELOPMENT,
	    
	    /**
	     * A TESTING build is similar to a DEVELOPMENT build, but must have passed all Unit tests
	     */
	    TESTING,
	    
	    /**
	     * A RELEASE build is similar to a TESTING build, but all known issues and/or bugs (with regard to the current release) must have been fixed
	     */
	    RELEASE			
	}
	
	private static ResourceBundle buildInfoBundle = null;
	private static BuildInformation instance;
	
	private BuildInformation() {		
		try {
			buildInfoBundle = ResourceBundle.getBundle("information/buildinfo");
		} catch (MissingResourceException mre) {
			LOGGER.error("The properties file with the build information could be found");
		}
	}
	
	public static BuildInformation instance() {
		if (instance == null) {
			instance = new BuildInformation();
		}
		
		return instance;
	}
	
	public String getVersion() {
		return buildInfoBundle.getString("buildVersion");
	}
	
	public String getBuildTimeStamp() {
		return buildInfoBundle.getString("buildTime");
	}
	
	public BuildType getBuildType() {
		return BuildType.valueOf(buildInfoBundle.getString("buildType"));
	}
}
