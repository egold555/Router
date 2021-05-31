package org.golde.router.objects;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.golde.router.Router;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;

import lombok.Getter;

public class Request {

	/**
	 * The instance of the router
	 * @Router the router instance
	 */
	private final Router router;
	private final HttpExchange exchange;

	@Getter private final Map<String, String> wildcards = new HashMap<String, String>();
	@Getter private final Map<String, String> queryParameters = new HashMap<String, String>();

	/**
	 * Get the headers from the request. Setting headers here will do nothing.
	 * @return the headers object
	 */
	@Getter private final Headers headers;

	public Request(Router router, HttpExchange exchange, String[] rawWildcards) {
		this.router = router;
		this.exchange = exchange;
		parseWildcards(exchange, rawWildcards);
		parseQueryParams(exchange);
		this.headers = new Headers(exchange.getRequestHeaders());
	}

	private void parseQueryParams(HttpExchange exchange) {
		String[] stripQueryParams = exchange.getRequestURI().toString().split("\\?");

		if(stripQueryParams.length == 2) {
			queryParameters.putAll(parseParamaters(stripQueryParams[1]));
		}
	}

	private void parseWildcards(HttpExchange exchange, String[] rawWildcards) {
		String[] split = exchange.getRequestURI().toString().substring(1).split("/");

		for(int i = 0; i < rawWildcards.length; i++) {
			String rawStr = rawWildcards[i];
			String splitStr = split[i];
			if(rawStr.length() !=0 && rawStr.charAt(0) == '{' && rawStr.charAt(rawStr.length() - 1) == '}') {
				System.out.println(rawStr + " -- " + splitStr);
				wildcards.put(rawStr.replace("{", "").replace("}", ""), splitStr);
			}
		}
	}

	/**
	 * Parses the body as a Map(Key, Value).
	 * This only works if the forum was submitted as application/x-www-form-urlencoded. You should be using this anyway.
	 * @return a map of key value pairs, of the given forum input.
	 */
	public Map<String, String> getBodyAsForm() {
		return parseParamaters(getBodyAsText());
	}

	private static Map<String, String> parseParamaters(String in) {
		String[] pairs = in.split("\\&");
		Map<String, String> toReturn = new HashMap<String, String>();

		try {
			for (int i = 0; i < pairs.length; i++) {
				String[] fields = pairs[i].split("=");
				if(fields.length != 2) {
					return null;
				}
				String key = URLDecoder.decode(fields[0], "UTF-8");
				String value = URLDecoder.decode(fields[1], "UTF-8");
				toReturn.put(key, value);
			}
		}
		catch(UnsupportedEncodingException e) {
			return null;
		}

		return toReturn;
	}

	/**
	 * Returns the body is the submitted request into a string of text, separated by new line characters
	 * @return the body as a string. 
	 */
	public String getBodyAsText() {
		InputStream inputStream = exchange.getRequestBody();

		String text = new BufferedReader(
				new InputStreamReader(inputStream, StandardCharsets.UTF_8))
				.lines()
				.collect(Collectors.joining("\n"));

		return text;
	}

	/**
	 * Return the body field as a JSON object. 
	 * @return A JSON object of the body. Returns null if it failed to parse the JSON
	 */
	public JsonObject getBodyAsJson() {
		try {
			return router.getGson().fromJson(getBodyAsText(), JsonObject.class);
		}
		catch(JsonSyntaxException e) {
			System.err.println("Malformed JSON recieved!");
			e.printStackTrace();
			return null;
		}
	}

	//May be added, but for the time being, this method is useless
	//	public RequestMethod getRequestMethod() {
	//		RequestMethod method = RequestMethod.valueOf(exchange.getRequestMethod());
	//		if(method == null) {
	//			System.err.println("Request method '" + exchange.getRequestMethod() + "' was not found in the RequestMethod Enum!. \nReturning null, please stand by for everything to crash!");
	//		}
	//		return method;
	//	}

	/**
	 * Return a wildcard from the given url.
	 * @param name name of the wildcard
	 * @return the value as a string. Null if it doesn't exist.
	 */
	public String getWildcard(String name) {
		name = name.replace("{", "").replace("}", "");
		return wildcards.get(name);
	}

	/**
	 * Parse a wildcard as a integer.
	 * @param name name of the wildcard
	 * @return the value parsed as a integer. Null if it failed to parse.
	 */
	public Integer getWildcardAsInteger(String name) {
		try {
			return Integer.parseInt(getWildcard(name));
		}
		catch(NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Parse a wildcard as a long.
	 * @param name name of the wildcard
	 * @return the value parsed as a long. Null if it failed to parse.
	 */
	public Long getWildcardAsLong(String name) {
		try {
			return Long.parseLong(getWildcard(name));
		}
		catch(NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Get the Gson instance from the router
	 * @return the gson instance
	 */
	public Gson getGson() {
		return router.getGson();
	}

}
