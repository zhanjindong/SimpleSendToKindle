/**
 *  Copyright (c) 2014 zhanjindong. All rights reserved.
 */
package so.zjd.sstk;

/**
 * 
 * sstk custom exception.
 * 
 * @author jdzhan,2014-12-14
 * 
 */
public class SstkException extends RuntimeException {

	private static final long serialVersionUID = 2661720921334730612L;

	public SstkException(String message, Throwable cause) {
		super(message, cause);
	}
}
