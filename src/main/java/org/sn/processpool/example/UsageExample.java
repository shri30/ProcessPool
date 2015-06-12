package org.sn.processpool.example;
import org.sn.processpool.ExecutorService;
import org.sn.processpool.JTask;
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
