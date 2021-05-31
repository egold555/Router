package org.golde.router.routes;

import org.golde.router.objects.Request;
import org.golde.router.objects.Response;

/**
 * Route 404 interface. Used to create global 404 pages.
 * Implement this interface to create your own custom global 404 handler.
 * @author Eric Golde
 *
 */
public interface Route404 {

	/**
	 * This is called when a global 404 happens.
	 * @param req The incomming request object
	 * @param res The outgoing response object
	 */
	public abstract void send404(Request req, Response res);
	
}
