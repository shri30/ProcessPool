package org.sn.processpool.example;

import org.sn.processpool.ExecutorService;
import org.sn.processpool.ProcessPool;

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
public class Test {

	public static void main(String args[]) throws Exception {

		ExecutorService pp = ProcessPool.createProcessPool("Test",1);

		pp.submit(UsageExample.class, new String[] { "TASK" + 1 }).get().getException().printStackTrace();
				
		// Thread.sleep(17000);
		System.out.println(pp.getPoolStatistics());
		pp.terminate(true, 4000);

	}

}
