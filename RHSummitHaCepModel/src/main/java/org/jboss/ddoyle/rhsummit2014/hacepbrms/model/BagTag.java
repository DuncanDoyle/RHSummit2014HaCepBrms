package org.jboss.ddoyle.rhsummit2014.hacepbrms.model;

import java.util.UUID;

/**
 * Tag of a Bag. Uniquely identifies the bag within the system.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class BagTag {

	private final String id;
	
	public BagTag() {
		id = (UUID.randomUUID().toString());
	}

	public String getId() {
		return id;
	}
	
}

