package org.sn.processpool;

import org.sn.processpool.util.PIDFetcher;

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
public class ProcessContext {

	private static Integer processId;

	public static Integer getProcessId() {
		if(processId == null){
			processId = PIDFetcher.getProcessID();
		}
		return new Integer(processId);
	}
}
