package org.sn.processpool.examples;

import org.sn.processpool.ExecutorService;
import org.sn.processpool.ProcessPool;
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
 * Example on how to print the pool statistics
 *
 */
public class UsageExamplePoolStatistics {

	public static void main(String args[]) throws Exception {

		ExecutorService pp = ProcessPool.createProcessPool("UsageExamplePoolStatistics-Pool",2);

		pp.submit(TaskSuccess.class, new String[] { "TASK" + 1 });
				
		// Thread.sleep(17000);
		System.out.println("Printing pool Statistics");
		System.out.println(pp.getPoolStatistics());
		pp.terminate(4000);

	}

}
