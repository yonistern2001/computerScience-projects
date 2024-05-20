package edu.yu.cs.com3800.stage1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.yu.cs.com3800.SimpleServer;
import edu.yu.cs.com3800.stage1.Client.Response;

public class Stage1Test {
	private static SimpleServer server;
	private static int PORTNUM= 9000;
	
	@BeforeClass
	public static void initialize() throws IOException {
		server= new SimpleServerImpl(PORTNUM);
		server.start();
	}

	@Test
	public void testSendCompileAndRunRequest() throws IOException {
		Client client= new ClientImpl("localhost", PORTNUM);
		client.sendCompileAndRunRequest(code);
		Response responce= client.getResponse();
		assertEquals("code ran\n", responce.getBody());
		assertEquals(200, responce.getCode());
				
		client.sendCompileAndRunRequest(code);
		responce= client.getResponse();
		assertEquals("code ran\n", responce.getBody());
		assertEquals(200, responce.getCode());
	}
	
	@Test
	public void testInvalidURL() throws MalformedURLException {
		Client client= new ClientImpl("madeUpHost", PORTNUM);
		try {
			client.sendCompileAndRunRequest(code);
			fail();
		} catch (IOException e) {
			
		}
		
		client= new ClientImpl("localhost", 10000);
		try {
			client.sendCompileAndRunRequest(code);
			fail();
		} catch(IOException e) {
			
		}
	}
	
	@Test
	public void testInvalidCode() throws IOException {
		Client client= new ClientImpl("localhost", PORTNUM);
		client.sendCompileAndRunRequest(code+"}");
		Response response= client.getResponse();
		assertEquals(400, response.getCode());
		assertEquals("response body= "+response.getBody(), true, response.getBody().startsWith("Code did not compile:"));
	}

	@AfterClass
	public static void shutdown() {
		server.stop();
	}
	
	/*
	public class Code {
		
		public Code() {
			
		}
		
		public String run() {
			int a= 0;
			for(int i= 0; i < 20000; i++) {
				a+= i*2;
			}
			return "code ran\n";
		}
	}
	*/
	
	private static String code= ""
			+ "public class Code {\r\n"
			+ "		\r\n"
			+ "		public Code() {\r\n"
			+ "			\r\n"
			+ "		}\r\n"
			+ "		\r\n"
			+ "		public String run() {\r\n"
			+ "			int a= 0;\r\n"
			+ "			for(int i= 0; i < 20000; i++) {\r\n"
			+ "				a+= i*2;\r\n"
			+ "			}\r\n"
			+ "			return \"code ran\\n\";\r\n"
			+ "		}\r\n"
			+ "	}";
}
