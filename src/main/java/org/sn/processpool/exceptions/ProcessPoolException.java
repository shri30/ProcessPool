package org.sn.processpool.exceptions;

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
 * Indicates the exception within created a pool. This exception is specific to the creation of the pool
 *
 */
public class ProcessPoolException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	public ProcessPoolException(String msg){
		super(msg);
	}
}
