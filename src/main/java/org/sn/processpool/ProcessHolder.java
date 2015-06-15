package org.sn.processpool;

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
 * Holds the process related information. It also provides the information about
 * the mainClass that is currently running on the process.
 * 
 *
 */
public class ProcessHolder {

	private volatile Integer processID;

	private volatile String mainClass;

	private volatile String arguments;

	private Process process;

	public Integer getProcessID() {
		return processID;
	}

	public void setProcessID(Integer processID) {
		this.processID = processID;
	}

	public Process getProcess() {
		return process;
	}

	public void setProcess(Process process) {
		this.process = process;
	}

	public String getMainClass() {
		return mainClass;
	}

	public void setMainClass(String mainClass) {
		this.mainClass = mainClass;
	}

	public String getArguments() {
		return arguments;
	}

	public void setArguments(String arguments) {
		this.arguments = arguments;
	}

	public void reset() {
		mainClass = null;
		arguments = null;
	}

	@Override
	public String toString() {
		return "ProcessHolder [processID=" + processID + ", mainClass="
				+ mainClass + ", arguments=" + arguments + "]";
	}

}
