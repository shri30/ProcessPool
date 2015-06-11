# ProcessPool 1.0
Framework to create a Pool of processes in Java. Ideal for memory intensive operations for your application. 
The process pooler works by creating pool of processes and excutes each of the submitted task in a seperate JVM there by 
increasing the throughput of the application. The process pool also provides Web UI for monitoring the status of the process in 
the pool.

Features:

-nterprocess communication managed internally by the framework.
-Access to procesID for each of the executing tasks.
-Parent classpath inheritence.
-Monitor running process through Web UI
-Easy to use API.


Refer to example package on how to use the API.

Sample code:

package org.sn.processpool.example;
import org.sn.processpool.ExecutorService;
import org.sn.processpool.JTask;
import org.sn.processpool.ProcessFuture;
import org.sn.processpool.ProcessPool;
import org.sn.processpool.Status;
import org.sn.processpool.TaskStatus;

public class UsageExample implements JTask{

	
	public static void main(String args[]) throws Exception{
		//Create a pool with name : UsageExample-Process and number of processes : 3
		ExecutorService pool = ProcessPool.createProcessPool("UsageExample-Process", 3);
		ProcessFuture<TaskStatus> pf1 = pool.submit(Task.class, args);
		ProcessFuture<TaskStatus> pf2 =pool.submit(Task.class, args);
		ProcessFuture<TaskStatus> pf3 =pool.submit(Task.class, args);
		ProcessFuture<TaskStatus> pf4 =pool.submit(Task.class, args);
		ProcessFuture<TaskStatus> pf5 =pool.submit(Task.class, args);
		//the get method blocks the call to get the result
		TaskStatus ts= pf1.get();
		Status status = ts.getStatus();
		if(status == Status.SUCESS){
			//code goes here
		}
		
		TaskStatus ts2= pf2.get();
		Status status2 = ts.getStatus();
		if(status == Status.SUCESS){
			//code goes here
		}
		
		
		TaskStatus ts3= pf3.get();
		Status status3 = ts.getStatus();
		if(status == Status.SUCESS){
			//code goes here
		}
		
		
		TaskStatus ts4= pf4.get();
		Status status4 = ts.getStatus();
		if(status == Status.SUCESS){
			//code goes here
		}
		//terminate the pool - pass true for graceful termination, the second parameter is minutes
		//wait for task completion
		pool.terminate(true, 3000);
	}


}

class Task implements JTask{

	public static void main(String[] args) {
		System.out.println("Executing the sample task");
	}
	
}
