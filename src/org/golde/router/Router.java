package org.golde.router;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import org.golde.router.annotations.Route;
import org.golde.router.enums.StatusCode;
import org.golde.router.objects.Request;
import org.golde.router.objects.Response;
import org.golde.router.routes.Route404;
import org.golde.router.routes.RouteDefault404;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Main router class, extend this class to get started.
 * @author Eric Golde
 *
 */
public class Router {

	/**
	 * Internal com.sun http server
	 */
	private HttpServer server;

	private List<MethodHolder> allMethods = new ArrayList<MethodHolder>();

	/**
	 * Gson that is used for any JSON parsing endpoints, or endpoints that return json.
	 * By default, we disable html escaping, pretty print, and serialize nulls.
	 * Routes can access this by using {@link Response#getRouter().getGson()} or {@link Request#getRouter().getGson()}
	 */
	@Getter
	@Setter
	private Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().serializeNulls().create();

	@Setter
	@Getter
	private Route404 route404 = new RouteDefault404();

	/**
	 * Create a router on a specific port
	 * @param port port for http server to be on
	 * @throws IOException if it fails to create a InetSocketAddress
	 */
	public Router(int port) throws IOException {
		server = HttpServer.create(new InetSocketAddress(port), 0);
	}

	/**
	 * Register a class that contains functions @Route annotated functions
	 * @param clazz Class to register
	 */
	public void register(Class<?> clazz) {
		allMethods.addAll(getMethods(clazz));;
	}

	/**
	 * Start up the http server
	 */
	public void start() {

		final Router routerTempInstance = this;
		server.createContext("/", new HttpHandler() {

			@Override
			public void handle(HttpExchange exchange) throws IOException {
				try {


					//get rid of first argument in the array, then split it up into a list of arguments

					boolean invoked = false;
					for(MethodHolder holder : allMethods) {

						if(doesMatch(exchange, holder.route)){
							//execute
							invokeMethod(exchange, holder);
							invoked = true;
						}
					}

					if(!invoked) {
						//handle 404
						route404.send404(new Request(routerTempInstance, exchange, new String[0]), new Response(routerTempInstance, exchange).setStatusCode(StatusCode.NOT_FOUND));
					}

				}
				catch(Throwable t) {
					System.err.println("An error occurred while processing the given request:");
					t.printStackTrace();
				}

			}
		});

		server.setExecutor(Executors.newFixedThreadPool(20));
		server.start();
	}

	private List<MethodHolder> getMethods(final Class<?> type) {
		final List<MethodHolder> methods = new ArrayList<MethodHolder>();
		Class<?> clazz = type;
		while (clazz != Object.class) {

			for (final Method method : clazz.getDeclaredMethods()) {
				if (method.isAnnotationPresent(Route.class)) {
					Route annotInstance = method.getAnnotation(Route.class);
					//System.out.println(annotInstance.value() + " - " + annotInstance.method() + " - " + type.getSimpleName() + " - " + method.getName());

					MethodHolder holder = new MethodHolder(annotInstance, method, clazz);
					if(!checkForDuplicates(methods, holder)) {
						methods.add(holder);
					}

				}
			}
			clazz = clazz.getSuperclass();
		}
		return methods;
	}

	private boolean checkForDuplicates(List<MethodHolder> inClass, MethodHolder holder) {
		String holderValue = holder.route.value();
		if(holderValue.length() != 0 && holderValue.charAt(holderValue.length() - 1) != '/') {
			holderValue += "/";
		}

		List<MethodHolder> allHolder = new ArrayList<MethodHolder>();
		allHolder.addAll(inClass);
		allHolder.addAll(allMethods);

		for(MethodHolder mh : allHolder) {
			String testValue = mh.route.value();
			if(testValue.length() != 0 && testValue.charAt(testValue.length() - 1) != '/') {
				testValue += "/";
			}

			if(holderValue.equals(testValue)) {

				if(mh.route.method() == holder.route.method()) {
					System.err.print("Duplicate found: " + holder.clazz.getName() + "#" + holder.method.getName() + " AND " + mh.clazz.getName() + "#" + mh.method.getName() + ". ");
					System.err.println("Ignoring method: " + mh.clazz.getName() + "#" + mh.method.getName());

					return true;
				}

			}

		}

		return false;
	}

	@AllArgsConstructor
	private static class MethodHolder {
		private final Route route;
		private final Method method;
		private final Class<?> clazz;
	}

	private static boolean doesMatch(HttpExchange exchange, Route route) {

		if(!exchange.getRequestMethod().equalsIgnoreCase(route.method().name())) {
			return false;
		}

		//Make sure when comparing urls, we ignore query paramaters
		String[] stripQueryParams = exchange.getRequestURI().toString().toLowerCase().split("\\?");
		//System.out.println(Arrays.toString(stripQueryParams));

		String[] split = stripQueryParams[0].substring(1).toLowerCase().split("/");

		//incase we fuck up, and add a / at the begining out of habit
		String routeValue = route.value().toLowerCase();
		if(routeValue.length() != 0 && routeValue.charAt(0) == '/') {
			routeValue = routeValue.substring(1);
		}

		String[] routeSplit = routeValue.toLowerCase().split("/");

		if(split.length != routeSplit.length) {
			return false;
		}

		boolean matches = true;
		for(int i = 0; i < split.length; i++) {
			String annString = routeSplit[i];
			String urlString = split[i];
			
			//System.out.println(annString + " --- " + urlString);
			
			//Fix an issue with race condition with root
			if(!annString.contains("{") && !annString.contains("}") && !annString.equals(urlString)) {
				return false;
			}


			if(annString.length() != 0 && annString.charAt(0) != '{' && annString.charAt(annString.length() - 1) != '}') {
				//is not a wildcard, it must match
				//System.out.println(annString + " - " + urlString);
				if(annString.length() != 0 && !annString.equals(urlString)) {
					matches = false;
				}
			}
			else if(
					(annString.length() != 0 && annString.charAt(0) != '{' && annString.charAt(annString.length() - 1) == '}') || 
					(annString.length() != 0 && annString.charAt(0) == '{' && annString.charAt(annString.length() - 1) != '}')

					) {

				//Must have closing {}
				return false;
			}
		}

		return matches;

	}

	private void invokeMethod(HttpExchange exchange, MethodHolder holder) {
		try {
			holder.method.invoke(holder.clazz.newInstance(), new Request(this, exchange, holder.route.value().split("/")), new Response(this, exchange));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
			e.printStackTrace();
		}
	}

}
