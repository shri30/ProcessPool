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
package org.sn.processpool;

import java.util.Date;



import org.junit.Assert;
import org.junit.Test;
import org.sn.processpool.statistics.PoolWatcher;

public class PoolTest {
	
	@Test
	public void testProcessPool() throws Exception {

		final ExecutorService pp = ProcessPool.createProcessPool("PoolTest",4,true);
		PoolWatcher.watchPool(pp, 8080);
		long starTime  = new Date().getTime();
		Thread t1= new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					ProcessFuture<TaskStatus> ts= pp.submit(SampleTask1.class, new String[]{"SampleTask1"});
					Assert.assertEquals(ts.get().getStatus(),Status.SUCESS);
					Thread.sleep(7000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});
		t1.setName("t1");
		
		Thread t2= new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					ProcessFuture<TaskStatus> ts = pp.submit(SampleTask2.class, new String[]{"SampleTask2"});
					Assert.assertEquals(ts.get().getStatus(),Status.SUCESS);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});
		t2.setName("t2");
		
		Thread t3= new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					ProcessFuture<TaskStatus> ts = pp.submit(SampleTask3.class, new String[]{"SampleTask3"});
					Assert.assertEquals(ts.get().getStatus(),Status.SUCESS);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});
		t3.setName("t3");
		
		Thread t4= new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					ProcessFuture<TaskStatus> ts = pp.submit(SampleTask4.class, new String[]{"SampleTask4"});
					Assert.assertEquals(ts.get().getStatus(),Status.SUCESS);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});
		t4.setName("t4");
		t1.start();t2.start();t3.start();t4.start();
		t1.join();t2.join();t3.join();t4.join();
		System.out.println(new Date().getTime() -starTime);
		pp.terminate(false,4000);

	
/*	
		Thread.sleep(3000);
		for(ProcessHolder ph : pp.getPoolStatistics())
		System.out.println(ph.toString());
		Thread.sleep(3000);
		t1.join();t2.join();t3.join();t4.join();
		for(ProcessHolder ph : pp.getPoolStatistics())
			System.out.println(ph.toString());
		pp.terminate(true, 17000);*/

	}

}
