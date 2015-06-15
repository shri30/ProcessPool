package org.sn.processpool.examples;

import org.sn.processpool.ProcessContext;

/**
 * Sample Success Task
 * @author nayaks4
 *
 */
class TaskSuccess{

	public static void main(String[] args) {
		System.out.println(ProcessContext.getProcessId() +"Executing the sample task");
	}
	
}