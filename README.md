# JBoss BRMS HA Complex Event Processing Demo

This is a demo application that demonstrates the implementation of a Highly Available Complex Event Processing system with JBoss BRMS.

## Architecture
The architecture of the demo application is explained in the following [presentation](http://www.slideshare.net/DuncanDoyle/doyle-h-0945highavailablitycepwithredhatjbossbrms3).

Basically the demo consists of 5 parts:
* [Event Producer](RHSummitHaCepEventProducer): This component reads events from a file and sends them to a JBoss HornetQ topic.
* Messaging System: The events are distributed to the Complex Event Processing Engines via JMS Messaging. In this demo we use JBoss HornetQ deployed inside JBoss EAP 6.3.3.
* [CEP Node](RHSummitHaCepApp): The JBoss BRMS CEP application. The CEP engine receives the events from the (durable) topic and sends them to the BRMS engine for processing. In this demo we run 2 engines in parallel.
* Infinispan/JBoss DataGrid Cache: This component is embedded inside the CEP nodes (i.e. Infinispan is used in replication mode) to store the Commands that are emitted by the CEP engine when a rule fires.

The basic idea is that, in order to achieve high availabality of the CEP engine, we run 2 CEP engine nodes in parallel. Both engines process the same events in the same order. The engines' pseudo-clocks are 
advanced based on the timestamp of the events, and because these events are processed by both engines in the same order, the clocks advance deterministically in both engines. This is one of the concepts used
to keep the memory and the agenda of the engines in sync.

Second, all events have a UUID. This UUID is, among other things, used to compute a unique UUID for the Command that is created in the Right Hand Side of the rules. Because the 2 engines process the same data,
they will fire the same rules and create the same Commands. By being able to deterministically compute a Command-id, we can correlate the Commands created by both engines.

Next, the CommandDispatcher uses the Infinispan/JBoss DataGrid cache to store the Commands. If a Command with a specific Command-id (which we use as the key of the entry) already exists, we know that the other
engine has already executed this Command and the Command gets discarded by the engine. If the Command does not yet exist, the Command is stored in the Infinispan Cache, and the Command is executed.

A presentation that explains these concepts can be found [here](https://access.redhat.com/videos/875833)

## The demo.
### Prerequisit
In order to able to run the demo, your system needs to be multi-homed. The provided startup scripts bind JBoss EAP to address 127.0.0.1, the CEP node 1 to 127.0.0.3 and the CEP node 2 to 127.0.0.4.

### Running the demo

To run the demo, we first need to build the EventProducer, the KJAR (which contains the CEP rules) and the CEP Engine application. This requires Apache Maven to be installed on your system.
* Run *mvn clean install* on the [root POM](pom.xml). This will build all the sub-projects, including the datamodel, the KJAR, the Commands, the CEP Engine App and the EventProducer.
* The demo requires JBoss EAP 6.3.3, as we require a JBoss HornetQ messaging system. Download the *jboss-eap-6.3.0.zip* and *jboss-eap-6.3.3-patch.zip* from the [Red Hat Customer Support Portal](https://access.redhat.com/jbossnetwork/restricted/listSoftware.html?downloadType=distributions&product=appplatform&version=6.3.0) and place them in the [demo/installation_zips](demo/installation_zips) directory. 
If you don't have access to these platforms, you can download JBoss EAP 6.3 from [jboss.org](http://www.jboss.org/products/eap/download/) and alter 
the [buildJBossEap-HaCepBrms-Demo-Environment.sh](demo/buildJBossEap-HaCepBrms-Demo-Environment.sh) to skip the patching process to JBoss EAP 6.3.3.
* In the [demo](demo/) directory, run the [buildJBossEap-HaCepBrms-Demo-Environment.sh](demo/buildJBossEap-HaCepBrms-Demo-Environment.sh) script. This will setup JBoss EAP 6.3.3 in the *demo/target* directory.
* In the [demo/bin](demo/bin) directory, run the [startJBossEAP.sh](demo/bin/startJBossEAP.sh) script to fire up JBoss EAP (which starts the HornetQ Messaging platform).
* In the [demo/bin](demo/bin) directory, run the [startNodeOne.sh](demo/bin/startNodeOne.sh) and [startNodeTwo.sh](demo/bin/startNodeTwo.sh) scripts to start the 2 CEP engines. Notice that the 2 engines
create an Infinispan cluster.
* In the [demo/bin](demo/bin) directory, run the [startEventProducer.sh](demo/bin/startEventProducer.sh) script to start the EventProducer. This will send the events to the HornetQ system, from where they will
be picked up by the CEP engines.

Notice that the 2 CEP engines are running in parallel. For each rule fired, the output (in this case the SystemOutCommand) will only be processed by one engine. This can be either one of the engines, whichever one
is first. In the engine that executes the command, you will see a log-line similar like this:

*2015-02-19 17:28:50,027 [DEBUG] [pool-5-thread-1] [org.jboss.ddoyle.brms.cep.ha.command.dispatch.InfinispanIdempotantCommandDispatcher] DEBUG INSERTED COMMAND with ID: 'org.jboss.ddoyle.rhsummit2014.bags:BaggageLostAtSorting:BaggageLostAtSorting-[3]' into cache.

2015-02-19 17:28:50,027 [DEBUG] [pool-5-thread-1] [org.jboss.ddoyle.brms.cep.ha.command.executor.SimpleCommandExecutionService] DEBUG EXECUTING COMMAND: org.jboss.ddoyle.rhsummit2014.bags:BaggageLostAtSorting:BaggageLostAtSorting-[3]

Command-ID: org.jboss.ddoyle.rhsummit2014.bags:BaggageLostAtSorting:BaggageLostAtSorting-[3]
Message: Baggage lost at sorting. BagTag ID: 3*

In the engine that discards the same command, you will see something like this:

*2015-02-19 17:28:50,028 [DEBUG] [pool-5-thread-1] [org.jboss.ddoyle.brms.cep.ha.command.dispatch.InfinispanIdempotantCommandDispatcher] DEBUG DISCARDING COMMAND with ID: 'org.jboss.ddoyle.rhsummit2014.bags:BaggageLostAtSorting:BaggageLostAtSorting-[3]' as it has already been executed earlier.*

## Interesting links:
* [The Drools project](http://www.drools.org)
* [The JBoss BRMS platform](http://www.redhat.com/en/technologies/jboss-middleware/business-rules)
* [HA CEP with JBoss BRMS presentation @ Red Hat Summit 2014](https://access.redhat.com/videos/875833)
* [HA CEP with JBoss BRMS presentation on SlideShare](http://www.slideshare.net/DuncanDoyle/doyle-h-0945highavailablitycepwithredhatjbossbrms3)
* [JBoss EAP downloads on the Red Hat Customer Support Portal](https://access.redhat.com/jbossnetwork/restricted/listSoftware.html?downloadType=distributions&product=appplatform&version=6.3.0)
* [JBoss EAP downloads on the JBoss.org community site](http://www.jboss.org/products/eap/download/)
