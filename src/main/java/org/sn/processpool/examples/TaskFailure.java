package org.sn.processpool.examples;

/**
 * Sample Failure Task
 * @author nayaks4
 *
 */
class TaskFailure{

	public static void main(String[] args){
		throw new RuntimeException("Task Exception");
	}
	
}