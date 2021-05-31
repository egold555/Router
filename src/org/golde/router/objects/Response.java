package org.golde.router.objects;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.golde.router.Router;
import org.golde.router.enums.StatusCode;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;

import lombok.Getter;

/**
 * The response that we are going to send out to the sender.
 * @author Eric Golde
 *
 */
public class Response {

	private final Router router;
	private final HttpExchange exchange;
	private StatusCode statusCode = StatusCode.OK;

	/**
	 * The headers that are sent back in the response
	 * @return the headers class
	 */
	@Getter private final Headers headers;

	/**
	 * Creates a response. Not normally used by the end user.
	 * @param exchange the http exchange in the background
	 */
	public Response(Router router, HttpExchange exchange) {
		this.router = router;
		this.exchange = exchange;
		this.headers = new Headers(exchange.getResponseHeaders());
	}

	/**
	 * Send plain text back to the browser.
	 * @param text text to send to the browser
	 */
	public void sendText(String text) {
		send("text/plain", text);
	}

	/**
	 * Send HTML to the browser
	 * @param html html to send to the browser
	 */
	public void sendHTML(String html) {
		send("text/html", html);
	}

	/**
	 * Send a 204 Success with no content.
	 */
	public void sendSuccess() {
		setStatusCode(StatusCode.NO_CONTENT);
		send(null, "");
	}

	/**
	 * Sends raw json to the client
	 * @param json the json to send
	 */
	public void sendJSON(JsonObject json) {
		String text = router.getGson().toJson(json);
		send("application/json", text);
	}

	/**
	 * Send a String response
	 * @param contentType the content type to send
	 * @param response the response as a string. Decoded into bytes with UTF8
	 */
	public void send(String contentType, String response) {
		this.send(contentType, response.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Send a byte array response
	 * @param contentType the content type to send
	 * @param response the array of bytes to send to the client
	 */
	public void send(String contentType, byte[] response) {
		try {

			if(statusCode != StatusCode.NO_CONTENT) {
				exchange.getResponseHeaders().add("Content-Type", contentType);
				exchange.sendResponseHeaders(statusCode.getCode(), response.length);
			}
			else {
				exchange.sendResponseHeaders(statusCode.getCode(), -1);
			}

			OutputStream os = exchange.getResponseBody();

			if(statusCode != StatusCode.NO_CONTENT) {
				os.write(response);
			}

			os.close();
		}
		catch(IOException e) {
			e.printStackTrace();
			exchange.close();
		}
	}

	/**
	 * Set the status code for the data being set. Defaults to 200 OK.
	 * @param statusCode Status code being set
	 * @return Returns the Response, used for chaining
	 */
	public Response setStatusCode(StatusCode statusCode) {
		this.statusCode = statusCode;
		return this;
	}

	/**
	 * Get the Gson instance from the router
	 * @return the gson instance
	 */
	public Gson getGson() {
		return router.getGson();
	}
	
	/**
	 * Sends a file to the sender. If the file is not found, sends a 404 not found. If something else went wrong, it throws a 500 internal server error.
	 * @param file The file to send
	 * @param autoDownload should we auto download the file, or should we try to display it in the browser like images?
	 */
	public void sendFile(File file, boolean autoDownload) {
		if(!file.exists()) {
			setStatusCode(StatusCode.NOT_FOUND).sendText("File not found.");
			return;
		}
		
		try {
			Path path = file.toPath();
			byte[] data = Files.readAllBytes(path);
			String mime = Files.probeContentType(path);
			getHeaders().set("Accept-Ranges", "bytes");
			getHeaders().set("Content-Disposition", "" + (autoDownload ? "attachment" : "inline") + "; filename=\"" + file.getName() + "\"");
			send(mime, data);
		}
		catch(Exception e) {
			e.printStackTrace();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			pw.flush();
			sw.flush();
			setStatusCode(StatusCode.INTERNAL_SERVER_ERROR).sendText("An internal error occurred while processing this request.");
			pw.close();
			try {
				sw.close();
			} catch (IOException ignored) {
				
			}
		}
	}

}
