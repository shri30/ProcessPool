package org.sn.processpool.examples;

import org.sn.processpool.ExecutorService;
import org.sn.processpool.ProcessFuture;
import org.sn.processpool.ProcessPool;
import org.sn.processpool.Status;
import org.sn.processpool.TaskStatus;

/**
 * Output from the each of the JVM is piped to the main process which creates the pool.
 *
 */
public class UsageExampleWithPipedOutput {

	public static void main(String args[]) throws Exception{
		//Create a pool with name : UsageExample-Process and number of processes : 3 and piped output =true
		ExecutorService pool = ProcessPool.createProcessPool("UsageExamplePool", 2,true);
		ProcessFuture<TaskStatus> pf1 =pool.submit(TaskSuccess.class, args);
		ProcessFuture<TaskStatus> pf2 =pool.submit(TaskSuccess.class, args);
		ProcessFuture<TaskStatus> pf3 =pool.submit(TaskFailure.class, args);
		//the get method blocks the call to get the result
		TaskStatus ts1= pf1.get();
		Status status1 = ts1.getStatus();
		if(status1 == Status.SUCESS){
			System.out.println("task1 completed Successfully");
		}
		
		TaskStatus ts2= pf2.get();
		Status status2 = ts2.getStatus();
		if(status2 == Status.SUCESS){
			System.out.println("task2 completed Successfully");
		}
		
		TaskStatus ts3= pf3.get();
		Status status3 = ts3.getStatus();
		if(status3 == Status.FAILURE){
			ts3.getException().printStackTrace();
			System.out.println("task3 Failed");
		}
		
		//terminate the pool - pass true for graceful termination, the second parameter is minutes
		//wait for task completion
		pool.terminate(3000);

	}
	
}
