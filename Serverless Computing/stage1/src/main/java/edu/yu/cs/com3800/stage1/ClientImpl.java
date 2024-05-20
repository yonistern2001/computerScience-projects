package edu.yu.cs.com3800.stage1;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class ClientImpl implements Client {
	
	private final HttpClient client;
	private Response prevResponse;
	private URI uri;
	
    public ClientImpl(String hostName, int hostPort) throws MalformedURLException {
    	this.client= HttpClient.newHttpClient();
    	try {
			this.uri= new URL("http", hostName, hostPort, "/compileandrun").toURI();
		} catch (URISyntaxException e) {
			throw new MalformedURLException(e.getMessage());
		}
    }

	public void sendCompileAndRunRequest(String src) throws IOException {
		HttpRequest request= HttpRequest.newBuilder().header("Content-Type", "text/x-java-source").uri(this.uri).POST(BodyPublishers.ofString(src)).build();
		HttpResponse<String> response= null;
		try {
			response= client.send(request, BodyHandlers.ofString());
		} catch(InterruptedException e) {
			System.err.println("Interrupted");
			return;
		}
		this.prevResponse= new Client.Response(response.statusCode(), response.body());
	}

	public Response getResponse() throws IOException {
		return prevResponse;
	}
}