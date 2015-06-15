package org.sn.processpool;

import java.util.List;



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
 * An Executor that provides methods to manage termination and methods that can
 * produce a Process Future for tracking progress of one or more asynchronous
 * tasks in the process pool.
 * <P>
 * An ExecutorService can be shut down, which will cause it to reject new tasks
 * submitted to the process pool. The method provided for shutting down an
 * ExecutorService is terminate(). The terminate() method will allow previously
 * submitted tasks to execute before terminating or until the timeout period
 * expires. The ExecutorService is designed to run as concurrent as possible and hence
 * can be used to access the instance within a multi-threaded environment.
 * 
 * {@code}<pre>
 *  public class UsageExampleBasic{
 *
 *	public static void main(String args[]) throws Exception{
 *		ExecutorService pool = ProcessPool.createProcessPool("UsageExamplePool", 2);
 *		ProcessFuture<TaskStatus> pf1 =pool.submit(TaskSuccess.class, args);
 *		ProcessFuture<TaskStatus> pf2 =pool.submit(TaskSuccess.class, args);
 *		ProcessFuture<TaskStatus> pf3 =pool.submit(TaskFailure.class, args);
 *		//the get method blocks the call to get the result
 *		TaskStatus ts1= pf1.get();
 *		Status status1 = ts1.getStatus();
 *		if(status1 == Status.SUCESS){
 *			System.out.println("task1 completed Successfully");
 *		}
 *		
 *		TaskStatus ts2= pf2.get();
 *		Status status2 = ts2.getStatus();
 *		if(status2 == Status.SUCESS){
 *			System.out.println("task2 completed Successfully");
 *		}
 *		
 *		TaskStatus ts3= pf3.get();
 *		Status status3 = ts3.getStatus();
 *		if(status3 == Status.FAILURE){
 *			ts3.getException().printStackTrace();
 *			System.out.println("task3 Failed");
 *		}
 *		
 *		//terminate the pool - pass true for graceful termination, the second parameter is minutes
 *		//wait for task completion
 *		pool.terminate(3000);
 *		
 *	}
 *
 * </pre>
 * 
 * 
 * @see ProcessPool
 */
public interface ExecutorService {


	/**
	 * Submit the task to the pool to execute it asynchronously
	 * @param clazz Class that has main method as the entry point for execution. 
	 * @param args Arguments to the main class
	 * @return ProcessFuture<TaskStatus> - Returns the Output of the executed task. 
	 */
	public ProcessFuture<TaskStatus> submit(Class<?> clazz, String args[]);
	
	/**
	 * Terminates the pool. Waits till the supplied timeout (timeOutInMillis)
	 * expires. setting the timeout to 0 will terminate all the processes
	 * abruptly. The currently running tasks will also be terminated without
	 * producing the required output.
	 * @param timeOutInMillis timeout in milliseconds
	 */
	public void terminate(long timeOutInMillis);

	/**
	 * Terminates the pool abruptly. All the running tasks will be killed.
	 */
	public void terminateNow();

	/**
	 * Returns the list of ProcessHolder objects with the details about each of
	 * the processes within the pool.
	 * 
	 * @return the list of {@link ProcessHolder}
	 */
	public List<ProcessHolder> getPoolStatistics();

	/**
	 * Returns the name of the pool. This name is supplied at the time of the
	 * pool creation.
	 * 
	 * @return Name of the Pool.
	 */
	public String getPoolName();

}
