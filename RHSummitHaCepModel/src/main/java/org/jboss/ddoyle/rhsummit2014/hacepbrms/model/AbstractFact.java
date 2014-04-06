package org.jboss.ddoyle.rhsummit2014.hacepbrms.model;

public class AbstractFact implements Fact {

	/**
	 * SerialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	private final String id;
	
	public AbstractFact(String id) {
		this.id = id;
	}
	
	@Override
	public String getId() {
		return id;
	}

}
