package edu.yu.cs.com3800;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public interface LoggingServer {

    public static final Level FILE_LOG_LEVEL= Level.ALL;
    public static final Level STDOUT_LOG_LEVEL= Level.WARNING;

	default Logger initializeLogging(String fileNamePreface) throws IOException {
        return initializeLogging(fileNamePreface, false);
    }
    
    default Logger initializeLogging(String fileNamePreface, boolean disableParentHandlers) throws IOException {
    	String loggerName= fileNamePreface;
        return createLogger(loggerName, fileNamePreface, disableParentHandlers);
    }

    static Logger createLogger(String loggerName, String fileNamePreface, boolean disableParentHandlers) throws IOException {
    	Logger logger= Logger.getLogger(loggerName);
    	logger.setLevel(FILE_LOG_LEVEL);
    	logger.getParent().setLevel(STDOUT_LOG_LEVEL);
    	logger.getParent().getHandlers()[0].setLevel(STDOUT_LOG_LEVEL);
    	logger.setUseParentHandlers(!disableParentHandlers);
    	
		File dir= new File("logging");
		dir.mkdir();
    	if(fileNamePreface != null) {
    		FileHandler handler= createFileHandler("logging/"+fileNamePreface+".log");
    		logger.addHandler(handler);
    	}
    	return logger;
    }
    
    public static void closeHandlers(Logger logger) {
    	Arrays.stream(logger.getHandlers()).forEach(Handler::close);
    }
    
    private static FileHandler createFileHandler(String fileName) throws IOException {
		FileHandler fileHandler= null;
		try {
			fileHandler = new FileHandler(fileName, true);
		} catch (SecurityException e) {
			throw new IllegalAccessError();
		}
		fileHandler.setFormatter(new SimpleFormatter());
		return fileHandler;
    }
}
