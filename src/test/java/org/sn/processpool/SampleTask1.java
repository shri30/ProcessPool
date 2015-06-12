package org.sn.processpool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.sn.processpool.JTask;

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
public class SampleTask1 implements JTask, Callable<String> {

	public static void main(String args[]){
			List<String> list= new ArrayList<String>();
			
			for(int i=0;i< 1000000;i ++){
				list.add("SimpleTest");

			}
		
			for(int i=0;i< 1000000;i ++){
				list.add("SimpleTest");

			}
			for(int i=0;i< 1000000;i ++){
				list.add("SimpleTest");

			}
			for(int i=0;i< 1000000;i ++){
				list.add("SimpleTest");

			}
			for(int i=0;i< 4000000;i ++){
				list.add("SimpleTest");

			}
		//throw new StackOverflowError();
		
		System.out.println("Sample Task 1 Completed");
		
	}

	@Override
	public String call() {
		main(null);
		return "SUCCESS";

	}
}
