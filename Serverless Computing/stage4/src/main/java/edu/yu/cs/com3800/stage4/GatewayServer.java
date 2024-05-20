package edu.yu.cs.com3800.stage4;


import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import edu.yu.cs.com3800.LoggingServer;
import edu.yu.cs.com3800.Message;
import edu.yu.cs.com3800.Message.MessageType;
import edu.yu.cs.com3800.Util;

public class GatewayServer extends Thread implements LoggingServer {
	
	private final GatewayPeerServerImpl observer;
	private final TCPSenderReceiver tcpService;
	private final HttpServer server;
	private final Logger logger;
	
	public GatewayServer(int portNumber, GatewayPeerServerImpl observer) {
		try {
			this.logger= initializeLogging(this.getClass().getCanonicalName() + "-on-port-" + portNumber);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		this.observer= observer;
		this.server= createServer(portNumber);
		this.server.setExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 3));
		this.server.createContext("/compileandrun", this::handleRequest);
		this.tcpService= new TCPSenderReceiver(observer.getAddress(), observer.getTcpPort(), logger);	
	}
	
	@Override
	public void run() {
		this.logger.fine("Starting server");
		this.server.start();
		this.logger.info("Listening on port " + server.getAddress().getPort());
	}
	
	public void shutdown() {
		this.logger.info("Shutting down server");
		this.server.stop(10);
		LoggingServer.closeHandlers(logger);
	}
	
	private void handleRequest(HttpExchange exchange) throws IOException {
		if(!exchange.getRequestMethod().equals("POST")) {
			logger.info("Request type is not POST, request: " + exchange.getRequestMethod());
			sendResponse(exchange, "Invalid request type", 405);
			return;
		}
		
		if(!exchange.getRequestHeaders().getFirst("Content-Type").equals("text/x-java-source")) {
			logger.info("Invalid Content-Type: " + exchange.getRequestHeaders().getFirst("Content-Type"));
			sendResponse(exchange, "Invalid Content-Type", 400);
			return;
		}
		
		long leaderID = observer.getCurrentLeader().getProposedLeaderID();		
		InetSocketAddress address = observer.getPeerByID(leaderID);
		byte[] code= Util.readAllBytes(exchange.getRequestBody());
		this.logger.fine("Sending code to leader");
		Message responseMsg= tcpService.sendAndReceive(MessageType.WORK, code, address, -1);
		int rCode= responseMsg.getErrorOccurred() ? 400 : 200;
		sendResponse(exchange, new String(responseMsg.getMessageContents()), rCode);
	}
	
	private static void sendResponse(HttpExchange exchange, String response, int rCode) throws IOException {
		byte[] responseBytes= response.getBytes();
		try {
			exchange.sendResponseHeaders(rCode, responseBytes.length);
			exchange.getResponseBody().write(responseBytes);
		} finally {
			exchange.close();
		}
	}
	
	private static HttpServer createServer(int portNumber) {
		try {
			return HttpServer.create(new InetSocketAddress(portNumber), 0);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
