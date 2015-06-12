package org.sn.processpool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
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
public class SampleTask2 implements JTask,Callable<String>{

	
	public static void main(String args[]) throws InterruptedException{

		List<String> list= new ArrayList<String>();
		
		for(int i=0;i< 10000000;i ++){
			list.add("SimpleTest");

		}
	Thread.sleep(5000);
		System.out.println("Sample Task 2 Completed");
	
}


	@Override
	public String call() throws InterruptedException {
		main(null);
		return "SUCCESS";

	}
}