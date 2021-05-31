package org.golde.router.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * An enum that contains HTTP status codes, and a brief description of what it means.
 * @author Eric Golde
 *
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum StatusCode {

	//200s
	OK(200, "OK"),
	CREATED(201, "Created"),
	NO_CONTENT(204, "No Content"),
	
	//300s
	MOVED_PERMANENTLY(301, "Moved Permanently"),
	
	//400s
	BAD_REQUEST(400, "Bad Request"),
	UNAUTHORIZED(401, "Unauthorized"),
	FORBIDDEN(403, "Forbidden"),
	NOT_FOUND(404, "Not Found"),
	METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
	REQUEST_TIMEOUT(408, "Request Timeout"),
	GONE(410, "Gone"),
	TOO_MANY_REQUESTS(429, "Too Many Requests"),
	
	//500s
	INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
	NOT_IMPLEMENTED(501, "Not Implemented"),
	SERVICE_UNAVAILABLE(503, "Service Unavailable"),
	
	;
	
	/**
	 * The status code
	 * @return The status code for the enum
	 */
	private final int code;
	private final String meaning;
	
}
