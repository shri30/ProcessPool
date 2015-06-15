package org.sn.processpool;
import java.io.BufferedInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import org.sn.processpool.exceptions.ChildProcessExecutionException;
import org.sn.processpool.util.PIDFetcher;

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
 * This class used by ProccessPool.class internally to create the JVM instance.
 * This class takes the tasks submitted through ExecutorService.submit() and
 * executes it within the JVM as separate process.
 * 
 * @author nayaks4
 * @see ProcessPool
 * @see ExecutorService
 */
final class JProcess {

	private static final String JPROCESS_TERMINATED_FAILURE = "JPROCESS:TERMINATED:FAILURE";
	
	private static final String JPROCESS_TERMINATED_EXCEPTION = "JPROCESS:TERMINATED:EXCEPTION";
	
	private static final String JPROCESS_TERMINATED = "JPROCESS:TERMINATED";
	
	private static final String JPROCESS_TERMINATED_SUCCESS = "JPROCESS:TERMINATED:SUCCESS";
	
	private static Integer processId;
	
	public static void main(String args[]){

		if(processId==null){
			Field processIDField;
			processId =PIDFetcher.getProcessID();
			System.err.println(processId);
			try {
				processIDField = ProcessContext.class.getDeclaredField("processId");
				processIDField.setAccessible(true);
				processIDField.set(null, processId);
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
			
		final class TaskExecutor implements Callable<String>{


			private byte[] _bytes;
			
			public TaskExecutor(byte[] bytes){
				_bytes = bytes;
			}
			
			@Override
			public String call() throws ChildProcessExecutionException {
				//System.out.println(new String(bytes));
				String command[] = new String(_bytes).split(" ");
				String arguments[] = Arrays.copyOfRange(command, 1, command.length);
				try{
					Class<?> clazz = Class.forName(command[0]);
					Method m = clazz.getDeclaredMethod("main", String[].class);
					m.setAccessible(true);
					m.invoke(null, new Object[] { arguments });
				}catch(NoSuchMethodException nsm){
					throw new ChildProcessExecutionException("Can not find the main method to execute");
				}catch(Exception e){
					throw new ChildProcessExecutionException(e);
				}
				
				return JPROCESS_TERMINATED_SUCCESS;
			}			
		}
		try{
			while (true) {
				byte[] bytes = new byte[2];
				BufferedInputStream input = new BufferedInputStream(System.in);
				input.read(bytes);
				int commandBytes = Integer.parseInt(new String(bytes));
				bytes = new byte[commandBytes];
				input.read(bytes);
				TaskExecutor te = new TaskExecutor(bytes);
				java.util.concurrent.ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory(){

					@Override
					public Thread newThread(Runnable r) {
				        Thread thread = new Thread(r);
				        thread.setDaemon(true);
				        return thread;
					}
					
				});
				Future<String> future = executor.submit(te);
				String result =  null;
				try{
					result = future.get();
					System.err.println(JPROCESS_TERMINATED);
					System.out.println(JPROCESS_TERMINATED);
					System.err.println(result);
				}catch(Exception e){
					System.err.println(JPROCESS_TERMINATED);
					System.err.println(JPROCESS_TERMINATED_EXCEPTION);
					//e.printStackTrace();
					Throwable th =e;
					while(th.getCause()!=null){
						th = th.getCause();
					}
					System.err.println(th);
					//System.err.println(th +"\n" + th.getStackTrace()[0].toString());
					for(StackTraceElement st : th.getStackTrace())
						System.err.println(st.toString());
					//e.printStackTrace();
					System.err.println(JPROCESS_TERMINATED_FAILURE);
					System.out.println(JPROCESS_TERMINATED);
				}
			}
		}catch(Exception e){
			throw new ChildProcessExecutionException(e.getMessage());
		}
		
	}

}
