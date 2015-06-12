package org.sn.processpool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.sn.processpool.exceptions.ProcessPoolException;

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
final public class ProcessPool implements ExecutorService {

	final private Queue<ProcessHolder> processQueue = new ConcurrentLinkedQueue<ProcessHolder>();
	
	final private Queue<Future<TaskStatus>> futureList = new ConcurrentLinkedQueue<Future<TaskStatus>>() ;
	
	final private Set<ProcessHolder> _processHolderSet = new HashSet<ProcessHolder>();

	final private java.util.concurrent.ExecutorService executorService ;
	
	final private boolean _isTerminated = false;
	
	private String _poolName;
	
	private boolean _isPipedOutput = false;
	
	private ProcessPool(int poolSize){
		executorService = Executors.newFixedThreadPool(poolSize);
	}

	public static ExecutorService createProcessPool(String poolName,int poolSize) {
		return createProcessPool(poolName,poolSize,false);
	}
	
	// Queue<Process> = new Vector()<Process>;
	public static ExecutorService createProcessPool(String poolName, int poolSize, boolean isPipedOutput) {
		
		try{
		ProcessPool processPool = new ProcessPool(poolSize);
		processPool._isPipedOutput = isPipedOutput;
		processPool._poolName = poolName;
		URLClassLoader urlclass = ((URLClassLoader)(Thread.currentThread().getContextClassLoader()));
		URL[] urls = urlclass.getURLs();
		StringBuilder urlBuilder = new StringBuilder();
		for(URL url: urls){

			urlBuilder.append(url.toURI().getPath()+";");
		}
		urlBuilder.deleteCharAt(urlBuilder.length()-1);
		System.out.println(urlBuilder.toString());
		for (int i = 0; i < poolSize; i++) {
			ProcessHolder processHolder = new ProcessHolder();
			Process process = new ProcessBuilder(System.getProperty("java.home") + "/bin/java", "-classpath", urlBuilder.toString(),"org.sn.processpool.JProcess").redirectOutput(Redirect.PIPE).start();
			processHolder.setProcess(process);
			
			BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String line = null;
			if((line = br.readLine()) != null) {
				processHolder.setProcessID(Integer.parseInt(line));
			}
			processPool.getProcessQueue().add(processHolder);
			processPool._processHolderSet.add(processHolder);
		}
		for (int i = 0; i < poolSize; i++) {
			processPool.submit(DummyTask.class, null).get();
		}
		       // server.join();		
		return processPool;
		}catch(Exception e){
			e.printStackTrace();
			throw new ProcessPoolException(e.getMessage());
		}

	}

	@Override
	public ProcessFuture<TaskStatus> submit(Class<? extends JTask> clazz,
			String args[]) {
		if (_isTerminated) {
			throw new ProcessPoolException(
					"Pool is inactive or has been terminated");
		}
		if (clazz == null) {
			throw new RuntimeException("Clazz argument can not be null");
		}
		PoolKeeper keeper = new PoolKeeper(clazz, args);
		Future<TaskStatus> ts = executorService.submit(keeper);
		futureList.add(ts);
		ProcessFuture<TaskStatus> pf = new ProcessFuture<TaskStatus>();
		Field future;
		try {
			future = pf.getClass().getDeclaredField("future");
			future.setAccessible(true);
			future.set(pf, ts);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return pf;

	}

	public Queue<ProcessHolder> getProcessQueue() {
		return processQueue;
	}

	public List<ProcessHolder> getPoolStatistics(){
		StringBuilder sb = new StringBuilder();
		List<ProcessHolder> list = new ArrayList<ProcessHolder>();
		for(ProcessHolder ph: _processHolderSet){
			list.add(ph);
		}
		return list;
	}

	public java.util.concurrent.ExecutorService getExecutorService() {
		return executorService;
	}

	public void terminate(boolean isGraceful, final long timeout){
		if (_isTerminated)
			return;
		
		java.util.concurrent.ExecutorService esTimeOut = Executors
				.newFixedThreadPool(futureList.size());
		
		try {

			List<Future<String>> futureTimeoutList = new ArrayList<Future<String>>();
			for (Future<TaskStatus> future : futureList) {
				final Future<TaskStatus> _future = future;

				if (isGraceful) {

					Future<String> futureTimeout = esTimeOut
							.submit(new Callable<String>() {

								@Override
								public String call() throws Exception {
									_future.get(timeout, TimeUnit.MILLISECONDS);
									return "Success";
								}

							});

					futureTimeoutList.add(futureTimeout);
				} else {
					future.cancel(true);
				}
			}
			for (Future<String> futureTimeout : futureTimeoutList) {
				try {
					futureTimeout.get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				} catch (ExecutionException e) {
					if (e.getCause() instanceof TimeoutException) {
						futureTimeout.cancel(true);
					}
					// e.printStackTrace();
				}
			}
			esTimeOut.shutdown();
			for (ProcessHolder processHolder : _processHolderSet) {
				processHolder.getProcess().destroy();
			}
		} finally {
			esTimeOut.shutdown();
			executorService.shutdown();
		}

	}
	
	
	private class PoolKeeper implements Callable<TaskStatus> {

		Class<? extends JTask> _jTaskClazz;
		String _args[];

		public PoolKeeper(Class<? extends JTask> jTaskClazz, String args[]) {
			_jTaskClazz = jTaskClazz;
			_args = args;
		}

		
		@Override
		public TaskStatus call() throws Exception {
			AtomicBoolean sopFlag = new AtomicBoolean(true);
			ProcessHolder childProcessHolder  =processQueue.poll();
			Process childProcess = childProcessHolder.getProcess();
			OutputStream out = childProcess.getOutputStream();
			String clazzName = _jTaskClazz.getName();
			childProcessHolder.setMainClass(clazzName);
			StringBuilder arguments = new StringBuilder();
			if(_args!=null && _args.length !=0){
				for (int i = 0; i < _args.length; i++) {
					arguments.append(" " + _args[i]);
				}
			}
			String command = clazzName + arguments.toString();
			childProcessHolder.setArguments(arguments.toString());
			int commandLength = command.getBytes().length;
			out.write(String.valueOf(commandLength).getBytes());
			out.flush();
			out.write(command.toString().getBytes());
			out.flush();
			BufferedReader br = new BufferedReader(new InputStreamReader(childProcess.getErrorStream()));
			String line = null;
			//StringBuilder sb = new StringBuilder();
			TaskStatus ts = new TaskStatus();
			Thread sopPrinter = null;
			Lock lock = new ReentrantLock();
			if(_isPipedOutput){
				sopPrinter = new Thread(new SOPPrinter(childProcessHolder));
				//sopPrinter.setDaemon(true);
				sopPrinter.setName("SOPPrinter");
				sopPrinter.start();
				
				
				//
			}
			
			while ((line = br.readLine()) != null) {
				
				if (line.equals("JPROCESS:TERMINATED")) {
					processQueue.add(childProcessHolder);
					line = br.readLine();
					if (line.equals("JPROCESS:TERMINATED:EXCEPTION")){
						//System.err.println("{PROCESS_ID="+childProcessHolder.getProcessID()+" : TASK="+childProcessHolder.getMainClass()+"}-->"+line);
						StringBuilder exceptionBuilder = new StringBuilder("{PROCESS_ID="+childProcessHolder.getProcessID()+" : TASK="+childProcessHolder.getMainClass()+"}-->");
						while((line = br.readLine()) != null && !line.equals("JPROCESS:TERMINATED:FAILURE")){
							exceptionBuilder.append(line+"\n");

							Thread.sleep(200);
						}
						System.err.println(exceptionBuilder.toString());
						ts.setStatus(Status.FAILURE);
						Throwable th = new Throwable();
						//th.
						StackTraceElement se = new java.lang.StackTraceElement("org.sn.processpool.SampleTask", "run", "SampleTask.java", 19);
						StackTraceElement[] seArray = new StackTraceElement[1];
						seArray[0]= se;
						th.setStackTrace(seArray);
						ts.setException(th);
						break;
					}
					else if (line.equals("JPROCESS:TERMINATED:SUCCESS")){
						ts.setStatus(Status.SUCESS);
						sopFlag.set(false);
						break;
						}
				}
				System.err.println("{PROCESS_ID="+childProcessHolder.getProcessID()+" : TASK="+childProcessHolder.getMainClass()+"}-->"+line);
			}
			
			if(sopPrinter!=null && sopPrinter.isAlive()) {
				sopPrinter.join();
				
			}
			childProcessHolder.reset();
			return ts;

		}

	}
	
	class SOPPrinter implements Runnable{
		ProcessHolder _p;
		private SOPPrinter(ProcessHolder p){
			_p =p;
		}
		@Override
		public void run() {
			BufferedReader br = new BufferedReader(new InputStreamReader(_p.getProcess().getInputStream()));
			String line = null;
			try {
				while ((line=br.readLine())!=null) {
					if(!line.equals("JPROCESS:TERMINATED")){
						System.out.println("{PROCESS_ID="+_p.getProcessID()+" : TASK="+_p.getMainClass()+"}-->" +line);
					}else{
						break;
					}
				}
				//System.out.println("{PROCESS_ID="+_p.getProcessID()+" : TASK="+_p.getMainClass()+"}"+"SOPPrinter Thread Terminating");
			} catch (IOException e) {
				e.printStackTrace();
			} 
			
		}
		
	}

	@Override
	public String getPoolName() {
		return _poolName;
	}


	
	
	
}
