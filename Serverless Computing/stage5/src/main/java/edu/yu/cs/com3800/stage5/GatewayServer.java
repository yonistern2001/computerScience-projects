package edu.yu.cs.com3800.stage5;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import edu.yu.cs.com3800.LoggingServer;
import edu.yu.cs.com3800.Message;
import edu.yu.cs.com3800.Message.MessageType;
import edu.yu.cs.com3800.Vote;
import edu.yu.cs.com3800.stage5.TCPSenderReceiver.DeadNodeException;

public class GatewayServer extends Thread implements LoggingServer {
	
	public static void main(String[] args) throws IOException {
		int portNum= Integer.parseInt(args[0]);
		long observerID= Long.parseLong(args[1]);
		Path addrsFilePath= Paths.get(args[2]);
		
		ConcurrentHashMap<Long, InetSocketAddress> peerIdToAddr = new ConcurrentHashMap<>(
				Files.readAllLines(addrsFilePath).stream().skip(1).map(l -> l.split(",")).collect(Collectors.toMap(a -> Long.valueOf(a[0]),
						a -> new InetSocketAddress(a[1], Integer.parseInt(a[2])))));

		InetSocketAddress observerAddr= peerIdToAddr.remove(observerID);
		
		GatewayPeerServerImpl observer= new GatewayPeerServerImpl(observerAddr.getPort(), 0, observerID, peerIdToAddr);
		GatewayServer server= new GatewayServer(portNum, observer);
		
		observer.start();
		server.start();
	}
	
	private final AtomicLong idRequest= new AtomicLong();
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
		this.server= createHttpServer(portNumber);
		this.server.setExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 3));
		this.server.createContext("/compileandrun", this::handleCompileAndRunRequest);
		this.server.createContext("/serversinfo", this::handleGetServersInfo);
		this.tcpService= observer.tcpService;
	}
	
	@Override
	public void run() {
		this.logger.fine("Starting server");
		this.server.start();
		this.logger.info("Listening on port " + server.getAddress().getPort());
	}
	
	public void shutdown() {
		this.logger.info("Shutting down server");
		this.server.stop(3);
		LoggingServer.closeHandlers(logger);
	}
	
	private void handleCompileAndRunRequest(HttpExchange exchange) throws IOException {
		if(!exchange.getRequestMethod().equals("POST")) {
			logger.info("Request type is not POST, request: " + exchange.getRequestMethod());
			sendHttpResponse(exchange, "Invalid request type", 405);
			return;
		}
		
		if(!exchange.getRequestHeaders().getFirst("Content-Type").equals("text/x-java-source")) {
			logger.info("Invalid Content-Type: " + exchange.getRequestHeaders().getFirst("Content-Type"));
			sendHttpResponse(exchange, "Invalid Content-Type", 400);
			return;
		}
		
		byte[] code = readAllBytes(exchange.getRequestBody());
		this.logger.fine("Sending code to leader");
		Message responseMsg;
		try {
			responseMsg = sendWorkToLeader(code);
		} catch (InterruptedException e) {
			return;
		}
		int rCode= responseMsg.getErrorOccurred() ? 400 : 200;
		sendHttpResponse(exchange, new String(responseMsg.getMessageContents()), rCode);
	}

	private Message sendWorkToLeader(byte[] code) throws InterruptedException {
		InetSocketAddress leader= getLeaderAddr();
		long requestId = this.idRequest.getAndIncrement();
		
		while(!Thread.currentThread().isInterrupted()) {
			try {
				return tcpService.sendAndReceive(MessageType.WORK, code, leader, requestId,
						logger);
			} catch (DeadNodeException e) {
				this.logger.info("Send unsuccessful, leader is down");
				leader = getLeaderAddr();
			} catch(IOException a) {
				this.logger.info("IOException occurred, send unsuccessful");
				Thread.sleep(5000);
			}
		}
		throw new InterruptedException();
	}

	private InetSocketAddress getLeaderAddr() {
		long leaderID = observer.getCurrentLeaderBlocking().getProposedLeaderID();
		return TCPSenderReceiver.udpAddrToTcpAddr(this.observer.getPeerByID(leaderID));
	}
	
	private void handleGetServersInfo(HttpExchange exchange) throws IOException {
		if(!exchange.getRequestMethod().equals("GET")) {
			logger.info("Request type is not GET, request: " + exchange.getRequestMethod());
			sendHttpResponse(exchange, "Invalid request type", 405);
			return;
		}
		
		Vote currentLeader = observer.getCurrentLeader();
		if(Objects.isNull(currentLeader)) {
			sendHttpResponse(exchange, "Leader's null", 503);
			return;
		}
		long leaderId= currentLeader.getProposedLeaderID();
		List<Long> aliveIds= observer.getIds().stream().filter(Predicate.not(observer::isPeerDead)).toList();
		
		StringBuilder builder= new StringBuilder();
		for(long id: aliveIds) {
			builder.append(id == leaderId ? "Leader" : "Follower");
			builder.append("-");
			builder.append(id);
			builder.append("\n");
		}
		
		this.logger.info("Server info requested:\n" + builder.toString());
		sendHttpResponse(exchange, builder.toString(), 200);
	}
	
	protected static void sendHttpResponse(HttpExchange exchange, String response, int rCode) throws IOException {
		byte[] responseBytes= response.getBytes();
		try {
			exchange.sendResponseHeaders(rCode, responseBytes.length);
			exchange.getResponseBody().write(responseBytes);
		} finally {
			exchange.close();
		}
	}
	
	protected static HttpServer createHttpServer(int portNumber) {
		try {
			return HttpServer.create(new InetSocketAddress(portNumber), 0);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	private static byte[] readAllBytes(InputStream in) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int numberRead;
		byte[] data = new byte[40960];
		while ((numberRead = in.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, numberRead);
		}
		
		return buffer.toByteArray();
	}
}
