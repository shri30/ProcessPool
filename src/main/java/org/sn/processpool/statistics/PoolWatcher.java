package org.sn.processpool.statistics;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.sn.processpool.ExecutorService;
import org.sn.processpool.ProcessHolder;

/*Copyright 2015 Shrinivas Nayak

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

/**
 * Creates a http server on the specified port using PoolWater.watchPool method.
 * All the processes can be monitored through web UI located at
 * http://localhost:<PortNumber>
 * 
 * This uses Jetty server internally.
 *
 */
public class PoolWatcher extends AbstractHandler {
	private static ExecutorService _es;
	private static int _port;

	@SuppressWarnings("unchecked")
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		Method method = null;
		List<ProcessHolder> content = null;
		try {
			method = _es.getClass().getMethod("getPoolStatistics", null);
			content = (List<ProcessHolder>) method.invoke(_es, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		response.getWriter().println("<html>");
		response.getWriter()
				.println(
						"<div>PROCESS STATUS FOR POOL :" + _es.getPoolName()
								+ "</div>");
		response.getWriter().println("<table>");
		for (ProcessHolder processHolder : content) {
			response.getWriter().println("<tr>");
			response.getWriter().println(
					"<td><div >" + processHolder.toString() + "</div></td>");
			response.getWriter().println("</tr>");
		}
		response.getWriter().println("</table>");
		response.getWriter().println("</html>");
	}

	/**
	 * Watch the created pool through web UI
	 * @param executorService  the instance of the pool which needs to be monitored.
	 * @param portNumber   PortNumber where the http Server will start
	 * @throws Exception   
	 */
	public static void watchPool(ExecutorService executorService, int portNumber) throws Exception {
		_es = executorService;
		_port = portNumber;
		startServer();
	}

	/**
	 * Start the Jetty Server on the specified port
	 * @throws Exception
	 */
	private static void startServer() throws Exception {
		HttpConfiguration http_config = new HttpConfiguration();
		http_config.setSecureScheme("https");
		http_config.setSecurePort(8443);
		http_config.setOutputBufferSize(32768);
		Server server = new Server();
		ServerConnector http = new ServerConnector(server,
				new HttpConnectionFactory(http_config));
		http.setPort(_port);
		http.setIdleTimeout(30000);
		server.setConnectors(new Connector[] { http });

		// Set a handler
		server.setHandler(new PoolWatcher());

		// Start the server
		server.start();
	}
}