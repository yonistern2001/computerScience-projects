package edu.yu.cs.com3800;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public interface LoggingServer {

    default Logger initializeLogging(String fileNamePreface) throws IOException {
        return initializeLogging(fileNamePreface, false);
    }
    
    default Logger initializeLogging(String fileNamePreface, boolean disableParentHandlers) throws IOException {
    	String loggerName= fileNamePreface;
        return createLogger(loggerName, fileNamePreface, disableParentHandlers);
    }

    static Logger createLogger(String loggerName, String fileNamePreface, boolean disableParentHandlers) throws IOException {
    	Logger logger= Logger.getLogger(loggerName);
    	logger.setUseParentHandlers(!disableParentHandlers);
    	
		File dir= new File("logging");
		dir.mkdir();
    	if(fileNamePreface != null) {
    		FileHandler handler= createFileHandler("logging/"+fileNamePreface);
    		logger.addHandler(handler);
    	}
    	logger.setLevel(Level.ALL);
    	return logger;
    }
    
    private static FileHandler createFileHandler(String fileName) {
		FileHandler fileHandler= null;
		try {
			fileHandler = new FileHandler(fileName, true);
		} catch (SecurityException e) {
			throw new IllegalAccessError();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		fileHandler.setFormatter(new SimpleFormatter());
		return fileHandler;
    }
}
