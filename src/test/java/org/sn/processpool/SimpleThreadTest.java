package org.sn.processpool;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
public class SimpleThreadTest {
	public static void main(String args[]){
		long starTime  = new Date().getTime();
		java.util.concurrent.ExecutorService es = Executors.newFixedThreadPool(4);
		Future<String> f1 = es.submit(new SampleTask1());
		Future<String> f2 =es.submit(new SampleTask2());
		Future<String> f3 =es.submit(new SampleTask3());
		Future<String> f4 =es.submit(new SampleTask4());
		try {
			f1.get();f2.get();f3.get();f4.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(new Date().getTime() -starTime);
	}
}
