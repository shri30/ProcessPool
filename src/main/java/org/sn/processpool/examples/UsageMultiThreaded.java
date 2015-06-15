package org.sn.processpool.examples;

import org.sn.processpool.ExecutorService;
import org.sn.processpool.ProcessFuture;
import org.sn.processpool.ProcessPool;
import org.sn.processpool.Status;
import org.sn.processpool.TaskStatus;

public class UsageMultiThreaded {

	public static void main(String args[]) throws Exception{
		//Create a pool with name : UsageExample-Process and number of processes : 3
		final ExecutorService  pool = ProcessPool.createProcessPool("UsageExamplePool", 2,true);
		
		Thread t1 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				ProcessFuture<TaskStatus> pf1 =pool.submit(TaskSuccess.class, new String[]{"SampleTask1"});
				TaskStatus ts1 = null;
				try {
					ts1 = pf1.get();
				} catch (Exception e) {
					e.printStackTrace();
				}
				Status status = ts1.getStatus();
				if(status == Status.SUCESS){
					System.out.println("task1 completed Successfully");
				}
			}
		});
		
		Thread t2 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				ProcessFuture<TaskStatus> pf2 =pool.submit(TaskSuccess.class, new String[]{"SampleTask2"});
				TaskStatus ts2 =null;
				try {
					ts2 = pf2.get();
				} catch (Exception e) {
					e.printStackTrace();
				}
				Status status = ts2.getStatus();
				if(status == Status.SUCESS){
					System.out.println("task2 completed Successfully");
				}
				
			}
		});
		
		
		Thread t3 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				ProcessFuture<TaskStatus> pf3 =pool.submit(TaskFailure.class, new String[]{"SampleTask3"});
				TaskStatus ts3 = null;
				try {
					ts3 = pf3.get();
				} catch (Exception e) {
					e.printStackTrace();
				}
				Status status = ts3.getStatus();
				if(status == Status.FAILURE){
					ts3.getException().printStackTrace();
					System.out.println("task3 Failed");
				}
				
			}
		});
		t1.start();t2.start();
		Thread.sleep(300);
		t3.start();
		pool.terminate(3000);
		t1.join();
		t2.join();
		t3.join();
		//terminate the pool - pass true for graceful termination, the second parameter is minutes
		//wait for task completion
		//pool.terminate(3000);
	}


}


	

