package org.sn.processpool;

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

/**
 * Returns the future object of the submitted task that runs within a JVM process.
 * @param <T> 
 */
public class ProcessFuture<T> {

	private  Future<T> future;
	
	/**
	 * Blocks till the result of the task is retrieved.
	 * @return
	 * @throws Exception
	 */
	public T get() throws Exception{
		return future.get();
	}
	
}
