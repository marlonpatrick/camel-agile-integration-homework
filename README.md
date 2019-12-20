# Agile Integration Homework

- Apache Camel 2.23
- Spring Boot 2
- Red Hat Fuse 7.5
- Red Hat AMQ Broker 7.5
- Red Hat Fuse Karaf 7.5.0

# Basic Setup

## Configure maven settings.xml

In this repository there is an settings.xml example file which configures several Red Hat repositories to download the required dependencies.

Put it in the ${user.home}/.m2 folder.

## Install Nextgate Service dependencies

After download this repository:

    cd  camel-agile-integration-homework/legacy-nextgate-service/parent
    mvn clean install
    
    cd  camel-agile-integration-homework/legacy-nextgate-service/core
    mvn clean install

# Running Tests

All tests are mocked, so you don't need to install anything else.

    cd  camel-agile-integration-homework/fuse7-homework
    mvn clean test

# Configure dev enviroment


## Create Queues in Red Hat AMQ Broker 7.5.0

After download/unzip AMQ Broker 7.5.0: 

    cd amq-broker-7.5.0
    
    ./bin/artemis create --user admin --password admin --role admin --require-login instances/agile-integration-homework
    
    instances/agile-integration-homework/bin/artemis run
    
    instances/agile-integration-homework/bin/artemis queue create --user admin --password admin --auto-create-address --address deim.in --name deim.in --preserve-on-no-consumers --durable --anycast --url tcp://localhost:61616
    
    instances/agile-integration-homework/bin/artemis queue create --user admin --password admin --auto-create-address --address nextgate.out --name nextgate.out --preserve-on-no-consumers --durable --anycast --url tcp://localhost:61616
    
    instances/agile-integration-homework/bin/artemis queue create --user admin --password admin --auto-create-address --address xlate.converter.dlq --name xlate.converter.dlq --preserve-on-no-consumers --durable --anycast --url tcp://localhost:61616
    
    instances/agile-integration-homework/bin/artemis queue create --user admin --password admin --auto-create-address --address nextgate.dlq --name nextgate.dlq --preserve-on-no-consumers --durable --anycast --url tcp://localhost:61616

By default, the main port that will be used by the application is **5672** (AMQ protocol).

## Deploy Nextgate Legacy Service in Red Hat Karaf 7.5

After download/unzip/start Red Hat Karaf 7.5, go to Karaf cli console and execute:


    osgi:install mvn:com.customer.app/integration-test-server/1.0-SNAPSHOT
    
    osgi:install mvn:com.customer.app/artifacts/1.0-SNAPSHOT
    
    osgi:start [bundle-id for integration-test-server]
    
    osgi:start [bundle-id for artifacts]
    
    osgi:list


By default, the Nextgate Service will running in **8181** port.

# Running Routes

## Inbound Route

You can export the current file by clicking **Export to disk** in the menu. You can choose to export the file as plain Markdown, as HTML using a Handlebars template or as a PDF.
