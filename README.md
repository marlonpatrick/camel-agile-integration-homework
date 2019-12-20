# Agile Integration Homework

- Red Hat Fuse 7.5
- AMQ Broker 7.5
- Red Hat Fuse Karaf 7.5.0
- Apache Camel 2.23

Hi! I'm your first Markdown file in **StackEdit**. If you want to learn about StackEdit, you can read me. If you want to play with Markdown, you can edit me. Once you have finished with me, you can create new files by opening the **file explorer** on the left corner of the navigation bar.

# Basic Setup

## Configure maven settings.xml

In this repository there is an settings.xml example file which configures several Red Hat repositories to download the required dependencies.

Put it in the ${user.home}/.m2 folder.

## Install Nextgate Service dependencies

After download this repository:

    cd  camel-agile-integration-homework/legacy-nextgate-service/parent
    mvn clean install


    cd  camel-agile-integration-homework/fuse7-homework
    mvn clean test

# Running Tests

All tests are mocked, so you don't need to install anything else.

    cd  camel-agile-integration-homework/legacy-nextgate-service/parent
    mvn clean install

# Running dev enviroment


## Configure AMQ Broker 7.5.0

After download/unzip AMQ Broker 7.5.0: 

    cd amq-broker-7.5.0
    
    ./bin/artemis create --user admin --password admin --role admin --require-login instances/agile-integration-homework
    
    instances/agile-integration-homework/bin/artemis run
    
    instances/agile-integration-homework/bin/artemis queue create --user admin --password admin --auto-create-address --address deim.in --name deim.in --preserve-on-no-consumers --durable --anycast --url tcp://localhost:61616
    
    instances/agile-integration-homework/bin/artemis queue create --user admin --password admin --auto-create-address --address nextgate.out --name nextgate.out --preserve-on-no-consumers --durable --anycast --url tcp://localhost:61616
    
    instances/agile-integration-homework/bin/artemis queue create --user admin --password admin --auto-create-address --address xlate.converter.dlq --name xlate.converter.dlq --preserve-on-no-consumers --durable --anycast --url tcp://localhost:61616
    
    instances/agile-integration-homework/bin/artemis queue create --user admin --password admin --auto-create-address --address nextgate.dlq --name nextgate.dlq --preserve-on-no-consumers --durable --anycast --url tcp://localhost:61616
