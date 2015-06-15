package org.sn.processpool.examples;
import org.sn.processpool.ExecutorService;
import org.sn.processpool.ProcessFuture;
import org.sn.processpool.ProcessPool;
import org.sn.processpool.Status;
import org.sn.processpool.TaskStatus;


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
 * Example class shows the usage of the Pool
 * @author nayaks4
 *
 */
public class UsageExampleBasic{

	
	public static void main(String args[]) throws Exception{
		//Create a pool with name : UsageExample-Process and number of processes : 3
		ExecutorService pool = ProcessPool.createProcessPool("UsageExamplePool", 2);
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
		//pool.submit(TaskSuccess.class, args);
		//pool.submit(TaskSuccess.class, args);
		//pool.submit(TaskSuccess.class, args);
		//pool.submit(TaskSuccess.class, args);
	}


}
