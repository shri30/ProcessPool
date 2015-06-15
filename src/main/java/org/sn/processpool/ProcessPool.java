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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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


/**
 *An implementation of the ExecutorService. To create a processPool, call ProcessPool.createProcessPool() method.
 * <P>Creates a pool of JVM processes to execute the tasks in parallel. The submit method takes the class with main method as one of the parameters.
 * This class can be used in thread safe manner to create and terminate the pool.
 * @see ExecutorService
 */
final public class ProcessPool implements ExecutorService {

	final private Queue<ProcessHolder> processQueue = new ConcurrentLinkedQueue<ProcessHolder>();
	
	final private Queue<Future<TaskStatus>> futureList = new ConcurrentLinkedQueue<Future<TaskStatus>>() ;
	
	final private Set<ProcessHolder> _processHolderSet = new HashSet<ProcessHolder>();

	final private java.util.concurrent.ExecutorService executorService ;
	
	volatile private boolean _isTerminated = false;
	
	private String _poolName;
	
	private boolean _isPipedOutput = false;
	
	private ReadWriteLock _lock  = new ReentrantReadWriteLock();
	
	private ProcessPool(int poolSize){
		executorService = Executors.newFixedThreadPool(poolSize);
	}

	/**
	 * Create the process Pool
	 * 
	 * @param poolName
	 *            Name of the process Pool
	 * @param poolSize
	 *            Size of the process Pool
	 * @return ExecutorService
	 */
	public static ExecutorService createProcessPool(String poolName,int poolSize) {
		return createProcessPool(poolName,poolSize,false);
	}
	
	/**
	 * Create the process Pool
	 * 
	 * @param poolName
	 *            Name of the process Pool
	 * @param poolSize
	 *            Size of the process Pool
	 * @param pipedOutput
	 *            PipedOutput when set to true redirects the output of pooled
	 *            processes to main process which creates the pool. Set the flag
	 *            to false for better performance.
	 * @return ExecutorService
	 */
	public static ExecutorService createProcessPool(String poolName, int poolSize, boolean pipedOutput) {
		
		try{
		ProcessPool processPool = new ProcessPool(poolSize);
		processPool._isPipedOutput = pipedOutput;
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
			processPool.submit(DoNothingTask.class, null).get();
		}
		       // server.join();		
		return processPool;
		}catch(Exception e){
			e.printStackTrace();
			throw new ProcessPoolException(e.getMessage());
		}

	}
	
	/**
	 * Submit the task to the pool to execute it asynchronously
	 * @param clazz Class that has main method as the entry point for execution. 
	 * @param args Arguments to the main class
	 * @return ProcessFuture<TaskStatus> - Returns the Output of the executed task. 
	 */
	@Override
	public ProcessFuture<TaskStatus> submit(Class<?> clazz,
			String args[]) {
		Lock readLock = _lock.readLock();
		ProcessFuture<TaskStatus> pf = null;
		try{
			readLock.lock();
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
			pf = new ProcessFuture<TaskStatus>();
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
		}finally{
			readLock.unlock();
		}
		return pf;

	}

	
	private Queue<ProcessHolder> getProcessQueue() {
		return processQueue;
	}

	/**
	 * Returns the list of ProcessHolder objects with the details about each of
	 * the processes within the pool.
	 * 
	 * @return the list of {@link ProcessHolder}
	 */
	public List<ProcessHolder> getPoolStatistics(){
		StringBuilder sb = new StringBuilder();
		List<ProcessHolder> list = new ArrayList<ProcessHolder>();
		for(ProcessHolder ph: _processHolderSet){
			list.add(ph);
		}
		return list;
	}

	/**
	 * Terminates the pool abruptly. All the running tasks will be killed.
	 */
	@Override
	public void terminateNow() {
		terminate(0);
	}

	
	/**
	 * Terminates the pool. Waits till the supplied timeout (timeOutInMillis)
	 * expires. setting the timeout to 0 will terminate all the processes
	 * abruptly. The currently running tasks will also be terminated without
	 * producing the required output.
	 * @param timeOutInMillis timeout in milliseconds
	 */
	@Override
	public void terminate(final long timeout){
		java.util.concurrent.ExecutorService esTimeOut = null;
		Lock writeLock =null;
		try 
		{
			writeLock = _lock.writeLock();
			writeLock.lock();
			if (_isTerminated)
				throw new ProcessPoolException("The pool has already been terminated or is inactive");
		
			esTimeOut = Executors
					.newFixedThreadPool(futureList.size());
		
		

			List<Future<String>> futureTimeoutList = new ArrayList<Future<String>>();
			for (Future<TaskStatus> future : futureList) {
				final Future<TaskStatus> _future = future;
				Future<String> futureTimeout = esTimeOut
						.submit(new Callable<String>() {

							@Override
							public String call() throws Exception {
								_future.get(timeout, TimeUnit.MILLISECONDS);
								return "Success";
							}

						});

				futureTimeoutList.add(futureTimeout);
				
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
			for (ProcessHolder processHolder : _processHolderSet) {
				processHolder.getProcess().destroy();
			}
			_isTerminated = true;
		} finally {
			writeLock.unlock();
			if(esTimeOut!=null)esTimeOut.shutdown();
			executorService.shutdown();
		}

	}
	
	
	private class PoolKeeper implements Callable<TaskStatus> {

		private static final String JPROCESS_TERMINATED_SUCCESS = "JPROCESS:TERMINATED:SUCCESS";
		private static final String JPROCESS_TERMINATED_FAILURE = "JPROCESS:TERMINATED:FAILURE";
		private static final String JPROCESS_TERMINATED_EXCEPTION = "JPROCESS:TERMINATED:EXCEPTION";
		private static final String JPROCESS_TERMINATED = "JPROCESS:TERMINATED";
		Class<?> _clazz;
		String _args[];

		public PoolKeeper(Class<?> jTaskClazz, String args[]) {
			_clazz = jTaskClazz;
			_args = args;
		}

		
		@Override
		public TaskStatus call() throws Exception {
			AtomicBoolean sopFlag = new AtomicBoolean(true);
			ProcessHolder childProcessHolder  =processQueue.poll();
			Process childProcess = childProcessHolder.getProcess();
			OutputStream out = childProcess.getOutputStream();
			String clazzName = _clazz.getName();
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
				
				if (line.equals(JPROCESS_TERMINATED)) {
					processQueue.add(childProcessHolder);
					line = br.readLine();
					if (line.equals(JPROCESS_TERMINATED_EXCEPTION)){
						//System.err.println("{PROCESS_ID="+childProcessHolder.getProcessID()+" : TASK="+childProcessHolder.getMainClass()+"}-->"+line);
						StringBuilder exceptionBuilder = new StringBuilder();
						while((line = br.readLine()) != null && !line.equals(JPROCESS_TERMINATED_FAILURE)){
							exceptionBuilder.append(line+"\n");
						}
						//System.err.println("{PROCESS_ID="+childProcessHolder.getProcessID()+" : TASK="+childProcessHolder.getMainClass()+"}-->"+exceptionBuilder.toString());
						ts.setStatus(Status.FAILURE);
						Throwable th = new Throwable(exceptionBuilder.toString());
						//StackTraceElement se = new java.lang.StackTraceElement("org.sn.processpool.SampleTask", "run", "SampleTask.java", 19);
						//StackTraceElement[] seArray = new StackTraceElement[1];
						//seArray[0]= se;
						//th.setStackTrace(seArray);
						ts.setException(th);
						break;
					}
					else if (line.equals(JPROCESS_TERMINATED_SUCCESS)){
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
	
	/**
	 * 
	 * Print the piped output from the pooled processes to the process which created the pool.
	 *
	 */
	class SOPPrinter implements Runnable{
		private static final String JPROCESS_TERMINATED = "JPROCESS:TERMINATED";
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
					if(!line.equals(JPROCESS_TERMINATED)){
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
