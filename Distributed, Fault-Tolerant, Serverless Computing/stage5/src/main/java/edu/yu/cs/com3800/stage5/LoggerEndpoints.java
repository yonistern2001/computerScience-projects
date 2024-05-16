package edu.yu.cs.com3800.stage5;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class LoggerEndpoints extends Thread {
	
	private final HttpServer server;
	private final Path verboseLogFilePath, summaryLogFilePath;

	public LoggerEndpoints(String verboseLogFile, String summaryLogFile, int portNum) {
		super.setDaemon(true);
		super.setName("LoggerEndpoints-on-port-" + portNum);
		this.server= GatewayServer.createHttpServer(portNum);
		this.verboseLogFilePath= Paths.get("logging", verboseLogFile);
		this.summaryLogFilePath= Paths.get("logging", summaryLogFile);
		
		this.server.createContext("/summary", this::handleGetSummaryLogFile);
		this.server.createContext("/verbose", this::handleGetVerboseLogFile);
	}

	@Override
	public void run() {
		this.server.start();
	}
	
	public void shutdown() {
		this.server.stop(2);
	}

	private void handleGetSummaryLogFile(HttpExchange exchange) throws IOException {
		if(!exchange.getRequestMethod().equals("GET")) {
			GatewayServer.sendHttpResponse(exchange, "Invalid request type", 405);
			return;
		}
		
		GatewayServer.sendHttpResponse(exchange, Files.readString(summaryLogFilePath), 200);
	}

	private void handleGetVerboseLogFile(HttpExchange exchange) throws IOException {		
		if(!exchange.getRequestMethod().equals("GET")) {
			GatewayServer.sendHttpResponse(exchange, "Invalid request type", 405);
			return;
		}
		
		GatewayServer.sendHttpResponse(exchange, Files.readString(verboseLogFilePath), 200);
	}
}
