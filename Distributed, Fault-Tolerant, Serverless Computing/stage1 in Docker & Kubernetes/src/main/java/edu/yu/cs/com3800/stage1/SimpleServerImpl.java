package edu.yu.cs.com3800.stage1;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import edu.yu.cs.com3800.JavaRunner;
import edu.yu.cs.com3800.SimpleServer;

public class SimpleServerImpl implements SimpleServer {
	private final static String PATH = "/compileandrun";
	private final static Logger logger= Logger.getLogger(SimpleServerImpl.class.getName());

	public static void main(String[] args) {
		configureLogger();
		
		int port = 9000;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		}
		SimpleServer myserver = null;
		try {
			myserver = new SimpleServerImpl(port);
			myserver.start();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			myserver.stop();
		}
	}


	private final HttpServer server;
	private final int portNumber;

	public SimpleServerImpl(int port) throws IOException {
		this.portNumber= port;
		this.server = HttpServer.create(new InetSocketAddress(port), 0);
		this.server.createContext(PATH, SimpleServerImpl::handleRequest);
	}

	private static void handleRequest(HttpExchange exchange) throws IOException {
		logger.info("received request from "+exchange.getRemoteAddress().getHostName());
		String body = "";
		int rCode = 200;
		if (!exchange.getRequestMethod().equals("POST")) {
			rCode = 405;
		} else if (!"text/x-java-source".equals(exchange.getRequestHeaders().getFirst("Content-Type"))) {
			rCode = 400;
		} else {
			InputStream in = exchange.getRequestBody();
			JavaRunner runner = new JavaRunner();

			try {
				body = runner.compileAndRun(in);
			} catch (IllegalArgumentException | ReflectiveOperationException e) {
				body = e.getMessage() + "\n" + getStackTrace(e);
				rCode = 400;
			}
		}
		byte[] bodyBytes = body.getBytes();
		exchange.sendResponseHeaders(rCode, bodyBytes.length);
		OutputStream out = exchange.getResponseBody();
		out.write(bodyBytes);
		exchange.close();
	}

	private static String getStackTrace(Exception e) throws IOException {
		StringWriter s = new StringWriter();
		PrintWriter w = new PrintWriter(s);
		e.printStackTrace(w);
		String output = s.toString();
		w.close();
		s.close();
		return output;
	}

	@Override
	public void start() {
		logger.log(Level.INFO, "starting server on port " + portNumber);
		server.start();
	}

	@Override
	public void stop() {
		logger.log(Level.INFO, "shutting down server");
		Arrays.stream(logger.getHandlers()).forEach(Handler::close);
		server.stop(0);
	}

	private static void configureLogger() {
		File dir= new File("logging");
		dir.mkdir();
		FileHandler fileHandler= null;
		try {
			fileHandler = new FileHandler("logging/server.log", true);
		} catch (SecurityException e) {
			throw new IllegalAccessError();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		fileHandler.setFormatter(new SimpleFormatter());
		logger.addHandler(fileHandler);
		logger.setUseParentHandlers(true);
	}
}
