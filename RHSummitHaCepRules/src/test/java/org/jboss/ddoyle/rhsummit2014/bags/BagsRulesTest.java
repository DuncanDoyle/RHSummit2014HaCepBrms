package org.jboss.ddoyle.rhsummit2014.bags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.drools.core.time.SessionPseudoClock;
import org.jboss.ddoyle.rhsummit2014.hacepbrms.model.BagScannedEvent;
import org.jboss.ddoyle.rhsummit2014.hacepbrms.model.BagTag;
import org.jboss.ddoyle.rhsummit2014.hacepbrms.model.Location;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.AgendaGroupPoppedEvent;
import org.kie.api.event.rule.AgendaGroupPushedEvent;
import org.kie.api.event.rule.BeforeMatchFiredEvent;
import org.kie.api.event.rule.MatchCancelledEvent;
import org.kie.api.event.rule.MatchCreatedEvent;
import org.kie.api.event.rule.RuleFlowGroupActivatedEvent;
import org.kie.api.event.rule.RuleFlowGroupDeactivatedEvent;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class BagsRulesTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(BagsRulesTest.class);

	/**
	 * Tests that the bag was not lost at sorting.
	 */
	@Test
	public void testBaggageNotLostAtSorting() {
		KieServices kieServices = KieServices.Factory.get();
		KieContainer kieContainer = kieServices.getKieClasspathContainer();
		KieSession kieSession = kieContainer.newKieSession();
		RulesFiredAgendaEventListener rfAel = new RulesFiredAgendaEventListener();
		kieSession.addEventListener(rfAel);

		SessionPseudoClock clock = kieSession.getSessionClock();

		long now = System.currentTimeMillis();
		clock.advanceTime(now, TimeUnit.MILLISECONDS);

		BagTag bagTag = new BagTag();

		BagScannedEvent bagScannedAtCheckin = new BagScannedEvent(bagTag, Location.CHECK_IN, new Date(clock.getCurrentTime()));
		kieSession.insert(bagScannedAtCheckin);
		kieSession.fireAllRules();
		// Nothing should happen. Advance the clock 5 minutes and insert bagScannedAtSorting event.
		clock.advanceTime(5, TimeUnit.MINUTES);

		BagScannedEvent bagScannedAtSorting = new BagScannedEvent(bagTag, Location.SORTING, new Date(clock.getCurrentTime()));
		kieSession.insert(bagScannedAtSorting);
		kieSession.fireAllRules();

		/*
		 * Advance Clock 6 minutes, if the bag would have been lost, the rule should fire. So we expect the rule not to fire.
		 */
		clock.advanceTime(6, TimeUnit.MINUTES);
		kieSession.fireAllRules();

		// No rules should have fired.
		assertEquals(0, rfAel.getAfterMatchFiredEvents().size());
	}

	@Test
	public void testBaggageLostAtSorting() {
		KieServices kieServices = KieServices.Factory.get();
		KieContainer kieContainer = kieServices.getKieClasspathContainer();
		KieSession kieSession = kieContainer.newKieSession();
		RulesFiredAgendaEventListener rfAel = new RulesFiredAgendaEventListener();
		kieSession.addEventListener(rfAel);

		SessionPseudoClock clock = kieSession.getSessionClock();

		long now = System.currentTimeMillis();
		clock.advanceTime(now, TimeUnit.MILLISECONDS);

		BagTag bagTag = new BagTag();

		BagScannedEvent bagScannedAtCheckin = new BagScannedEvent(bagTag, Location.CHECK_IN, new Date(clock.getCurrentTime()));
		kieSession.insert(bagScannedAtCheckin);
		kieSession.fireAllRules();
		// Nothing should happen. Advance the clock 11 minutes and fire the rules. Rule should fire.
		clock.advanceTime(11, TimeUnit.MINUTES);
		kieSession.fireAllRules();

		// 1 rule should have fired.
		assertEquals(1, rfAel.getAfterMatchFiredEvents().size());
	}
	
	private class RulesFiredAgendaEventListener implements AgendaEventListener {
		
		private List<AfterMatchFiredEvent> afterMatchFiredEvents = new ArrayList<>();
		
		public List<AfterMatchFiredEvent> getAfterMatchFiredEvents() {
			return Collections.unmodifiableList(afterMatchFiredEvents);
		}
		
		@Override
		public void matchCreated(MatchCreatedEvent event) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void matchCancelled(MatchCancelledEvent event) {
			// TODO Auto-generated method stub
		}

		@Override
		public void beforeMatchFired(BeforeMatchFiredEvent event) {
			// TODO Auto-generated method stub
		}

		@Override
		public void afterMatchFired(AfterMatchFiredEvent event) {
			afterMatchFiredEvents.add(event);
		}

		@Override
		public void agendaGroupPopped(AgendaGroupPoppedEvent event) {
			// TODO Auto-generated method stub
		}

		@Override
		public void agendaGroupPushed(AgendaGroupPushedEvent event) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
			// TODO Auto-generated method stub
			
		}
		
	}

}
