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

    cd camel-agile-integration-homework/fuse7-homework/inbound
    mvn spring-boot:run

By default, the Inbound App will start a web server in **8080** port with Spring Boot Actuator in  **8081** port. 

## Xlate Route

    cd camel-agile-integration-homework/fuse7-homework/xlate
    mvn spring-boot:run

## Outbound Route

    cd camel-agile-integration-homework/fuse7-homework/outbound
    mvn spring-boot:run

# Trigger the Routes

    curl -X POST \
      http://localhost:8080/rest/addPerson \
      -H 'Content-Type: application/xml' \
      -d '<?xml version="1.0" encoding="UTF-8"?>
    <p:Person xmlns:p="http://www.app.customer.com"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:schemaLocation="http://www.app.customer.com PatientDemographics.xsd ">
        <p:identifier>
            <p:identifier>p:identifier</p:identifier>
            <p:idtype>p:idtype</p:idtype>
            <p:assigningauthority>p:assigningauthority</p:assigningauthority>
        </p:identifier>
        <p:PartiesAlternateId>
            <p:alternateidsrole xsi:type="p:Code"/>
            <p:alternateid>
                <p:identifier>p:identifier</p:identifier>
            </p:alternateid>
        </p:PartiesAlternateId>
        <p:age>0</p:age>
        <p:agegroup>p:agegroup</p:agegroup>
        <p:dateofdeath>
            <p:literal>p:literal</p:literal>
        </p:dateofdeath>
        <p:isdeceased>true</p:isdeceased>
        <p:deathcertificatenumber>p:deathcertificatenumber</p:deathcertificatenumber>
        <p:isorgandonor>true</p:isorgandonor>
        <p:multiplebirthorder>0</p:multiplebirthorder>
        <p:ageatdeath>
            <p:unit xsi:type="p:Code"/>
            <p:value/>
        </p:ageatdeath>
        <p:wasmultiplebirth>true</p:wasmultiplebirth>
        <p:dateofbirth>
            <p:literal>p:literal</p:literal>
        </p:dateofbirth>
        <p:Patient>
            <p:begindate>2001-12-31T12:00:00</p:begindate>
            <p:enddate>2001-12-31T12:00:00</p:enddate>
            <p:mothersidentifier>p:mothersidentifier</p:mothersidentifier>
            <p:patientid>p:patientid</p:patientid>
            <p:sourcesystemidentifier>p:sourcesystemidentifier</p:sourcesystemidentifier>
            <p:locationid>p:locationid</p:locationid>
            <p:facilitynumber>p:facilitynumber</p:facilitynumber>
            <p:locationtype xsi:type="p:Code"/>
            <p:status xsi:type="p:Code"/>
            <p:HealthcareProvider/>
            <p:HealthRecord/>
            <p:PatientInformation/>
        </p:Patient>
        <p:CauseOfDeath>
            <p:causeofdeath xsi:type="p:Code"/>
            <p:timeinterval/>
            <p:precedence>0</p:precedence>
        </p:CauseOfDeath>
        <p:systemcode>p:systemcode</p:systemcode>
        <p:updatetimestamp>p:updatetimestamp</p:updatetimestamp>
        <p:localid>p:localid</p:localid>
        <p:birthname>p:birthname</p:birthname>
        <p:legalname>
            <p:prefix>p:prefix</p:prefix>
            <p:given>p:given</p:given>
            <p:nickname>p:nickname</p:nickname>
            <p:family>p:family</p:family>
            <p:suffix>p:suffix</p:suffix>
            <p:initials>p:initials</p:initials>
            <p:middle>p:middle</p:middle>
        </p:legalname>
        <p:alternatename>
            <p:prefix>p:prefix</p:prefix>
            <p:given>p:given</p:given>
            <p:nickname>p:nickname</p:nickname>
            <p:family>p:family</p:family>
            <p:suffix>p:suffix</p:suffix>
            <p:initials>p:initials</p:initials>
            <p:middle>p:middle</p:middle>
        </p:alternatename>
        <p:spousename>
            <p:prefix>p:prefix</p:prefix>
            <p:given>p:given</p:given>
            <p:nickname>p:nickname</p:nickname>
            <p:family>p:family</p:family>
            <p:suffix>p:suffix</p:suffix>
            <p:initials>p:initials</p:initials>
            <p:middle>p:middle</p:middle>
        </p:spousename>
        <p:mothersmaidenname>p:mothersmaidenname</p:mothersmaidenname>
        <p:fathername>p:fathername</p:fathername>
        <p:mothername>p:mothername</p:mothername>
        <p:ssn>
            <p:identifier>p:identifier</p:identifier>
            <p:idtype>p:idtype</p:idtype>
            <p:assigningauthority>p:assigningauthority</p:assigningauthority>
        </p:ssn>
        <p:race xsi:type="p:Code">
            <p:code>p:code</p:code>
            <p:displaytext>p:displaytext</p:displaytext>
            <p:codesystem>p:codesystem</p:codesystem>
            <p:codesystemversion>p:codesystemversion</p:codesystemversion>
        </p:race>
        <p:religion xsi:type="p:Code">
            <p:code>p:code</p:code>
            <p:displaytext>p:displaytext</p:displaytext>
            <p:codesystem>p:codesystem</p:codesystem>
            <p:codesystemversion>p:codesystemversion</p:codesystemversion>
        </p:religion>
        <p:birthaddress>
            <p:city>p:city</p:city>
            <p:state>p:state</p:state>
            <p:country>p:country</p:country>
        </p:birthaddress>
        <p:vipflag>true</p:vipflag>
        <p:veteranstatus>true</p:veteranstatus>
        <p:driverslicensestate>p:driverslicensestate</p:driverslicensestate>
        <p:repatriationnumber>p:repatriationnumber</p:repatriationnumber>
        <p:districtofresidence>p:districtofresidence</p:districtofresidence>
        <p:lgacode>p:lgacode</p:lgacode>
        <p:commentvalue>p:commentvalue</p:commentvalue>
        <p:gender xsi:type="p:Code">
            <p:code>p:code</p:code>
            <p:displaytext>p:displaytext</p:displaytext>
            <p:codesystem>p:codesystem</p:codesystem>
            <p:codesystemversion>p:codesystemversion</p:codesystemversion>
        </p:gender>
        <p:maritalstatus xsi:type="p:Code">
            <p:code>p:code</p:code>
            <p:displaytext>p:displaytext</p:displaytext>
            <p:codesystem>p:codesystem</p:codesystem>
            <p:codesystemversion>p:codesystemversion</p:codesystemversion>
        </p:maritalstatus>
        <p:ethnicity xsi:type="p:Code">
            <p:code>p:code</p:code>
            <p:displaytext>p:displaytext</p:displaytext>
            <p:codesystem>p:codesystem</p:codesystem>
            <p:codesystemversion>p:codesystemversion</p:codesystemversion>
        </p:ethnicity>
        <p:administrativegender xsi:type="p:NullableCode">
            <p:code>p:code</p:code>
            <p:displaytext>p:displaytext</p:displaytext>
            <p:codesystem>p:codesystem</p:codesystem>
            <p:codesystemversion>p:codesystemversion</p:codesystemversion>
            <p:originaltext>p:originaltext</p:originaltext>
            <p:nullflavor>askedbutunknown</p:nullflavor>
        </p:administrativegender>
        <p:religiousaffiliation>p:religiousaffiliation</p:religiousaffiliation>
        <p:preferredcontactmethod>p:preferredcontactmethod</p:preferredcontactmethod>
        <p:primaryhomeaddress xsi:type="p:Address">
            <p:addresstype>postal</p:addresstype>
            <p:effectivedaterange/>
            <p:line1>p:line1</p:line1>
            <p:line2>p:line2</p:line2>
            <p:line3>p:line3</p:line3>
            <p:line4>p:line4</p:line4>
            <p:city>p:city</p:city>
            <p:county xsi:type="p:Code"/>
            <p:postalcode>p:postalcode</p:postalcode>
            <p:state xsi:type="p:Code"/>
            <p:country xsi:type="p:Code"/>
        </p:primaryhomeaddress>
        <p:primaryhomeemail>
            <p:contactmechanismtype>p:contactmechanismtype</p:contactmechanismtype>
            <p:effectivedaterange/>
            <p:istextmessageenabled>true</p:istextmessageenabled>
            <p:universalrecordid>p:universalrecordid</p:universalrecordid>
        </p:primaryhomeemail>
        <p:primaryhomephone>
            <p:contactmechanismtype>p:contactmechanismtype</p:contactmechanismtype>
            <p:effectivedaterange/>
            <p:istextmessageenabled>true</p:istextmessageenabled>
            <p:universalrecordid>p:universalrecordid</p:universalrecordid>
        </p:primaryhomephone>
        <p:primaryhomephoneextension>
            <p:contactmechanismtype>p:contactmechanismtype</p:contactmechanismtype>
            <p:effectivedaterange/>
            <p:istextmessageenabled>true</p:istextmessageenabled>
            <p:universalrecordid>p:universalrecordid</p:universalrecordid>
        </p:primaryhomephoneextension>
        <p:primarycellphone>
            <p:contactmechanismtype>p:contactmechanismtype</p:contactmechanismtype>
            <p:effectivedaterange/>
            <p:istextmessageenabled>true</p:istextmessageenabled>
            <p:universalrecordid>p:universalrecordid</p:universalrecordid>
        </p:primarycellphone>
        <p:primaryhomepager>
            <p:contactmechanismtype>p:contactmechanismtype</p:contactmechanismtype>
            <p:effectivedaterange/>
            <p:istextmessageenabled>true</p:istextmessageenabled>
            <p:universalrecordid>p:universalrecordid</p:universalrecordid>
        </p:primaryhomepager>
        <p:workaddress xsi:type="p:Address">
            <p:addresstype>postal</p:addresstype>
            <p:effectivedaterange/>
            <p:line1>p:line1</p:line1>
            <p:line2>p:line2</p:line2>
            <p:line3>p:line3</p:line3>
            <p:line4>p:line4</p:line4>
            <p:city>p:city</p:city>
            <p:county xsi:type="p:Code"/>
            <p:postalcode>p:postalcode</p:postalcode>
            <p:state xsi:type="p:Code"/>
            <p:country xsi:type="p:Code"/>
        </p:workaddress>
        <p:workemail>
            <p:contactmechanismtype>p:contactmechanismtype</p:contactmechanismtype>
            <p:effectivedaterange/>
            <p:istextmessageenabled>true</p:istextmessageenabled>
            <p:universalrecordid>p:universalrecordid</p:universalrecordid>
        </p:workemail>
        <p:workphone>
            <p:contactmechanismtype>p:contactmechanismtype</p:contactmechanismtype>
            <p:effectivedaterange/>
            <p:istextmessageenabled>true</p:istextmessageenabled>
            <p:universalrecordid>p:universalrecordid</p:universalrecordid>
        </p:workphone>
        <p:workphoneextension>
            <p:contactmechanismtype>p:contactmechanismtype</p:contactmechanismtype>
            <p:effectivedaterange/>
            <p:istextmessageenabled>true</p:istextmessageenabled>
            <p:universalrecordid>p:universalrecordid</p:universalrecordid>
        </p:workphoneextension>
        <p:workpager>
            <p:contactmechanismtype>p:contactmechanismtype</p:contactmechanismtype>
            <p:effectivedaterange/>
            <p:istextmessageenabled>true</p:istextmessageenabled>
            <p:universalrecordid>p:universalrecordid</p:universalrecordid>
        </p:workpager>
        <p:temporaryaddress xsi:type="p:Address">
            <p:addresstype>postal</p:addresstype>
            <p:effectivedaterange/>
            <p:line1>p:line1</p:line1>
            <p:line2>p:line2</p:line2>
            <p:line3>p:line3</p:line3>
            <p:line4>p:line4</p:line4>
            <p:city>p:city</p:city>
            <p:county xsi:type="p:Code"/>
            <p:postalcode>p:postalcode</p:postalcode>
            <p:state xsi:type="p:Code"/>
            <p:country xsi:type="p:Code"/>
        </p:temporaryaddress>
        <p:temporaryemail>
            <p:contactmechanismtype>p:contactmechanismtype</p:contactmechanismtype>
            <p:effectivedaterange/>
            <p:istextmessageenabled>true</p:istextmessageenabled>
            <p:universalrecordid>p:universalrecordid</p:universalrecordid>
        </p:temporaryemail>
        <p:temporaryphone>
            <p:contactmechanismtype>p:contactmechanismtype</p:contactmechanismtype>
            <p:effectivedaterange/>
            <p:istextmessageenabled>true</p:istextmessageenabled>
            <p:universalrecordid>p:universalrecordid</p:universalrecordid>
        </p:temporaryphone>
        <p:temporaryphoneextension>
            <p:contactmechanismtype>p:contactmechanismtype</p:contactmechanismtype>
            <p:effectivedaterange/>
            <p:istextmessageenabled>true</p:istextmessageenabled>
            <p:universalrecordid>p:universalrecordid</p:universalrecordid>
        </p:temporaryphoneextension>
        <p:temporarypager>
            <p:contactmechanismtype>p:contactmechanismtype</p:contactmechanismtype>
            <p:effectivedaterange/>
            <p:istextmessageenabled>true</p:istextmessageenabled>
            <p:universalrecordid>p:universalrecordid</p:universalrecordid>
        </p:temporarypager>
        <p:handicap>p:handicap</p:handicap>
        <p:identityrealibilitycode>p:identityrealibilitycode</p:identityrealibilitycode>
        <p:isidentityunknown>true</p:isidentityunknown>
        <p:istranslatorneeded>true</p:istranslatorneeded>
        <p:lastupdatefacility>
            <p:identifier>p:identifier</p:identifier>
            <p:idtype>p:idtype</p:idtype>
            <p:assigningauthority>p:assigningauthority</p:assigningauthority>
        </p:lastupdatefacility>
        <p:livingarrangement xsi:type="p:Code">
            <p:code>p:code</p:code>
            <p:displaytext>p:displaytext</p:displaytext>
            <p:codesystem>p:codesystem</p:codesystem>
            <p:codesystemversion>p:codesystemversion</p:codesystemversion>
        </p:livingarrangement>
        <p:livingdependency>p:livingdependency</p:livingdependency>
        <p:billingaddress xsi:type="p:Address">
            <p:addresstype>postal</p:addresstype>
            <p:effectivedaterange/>
            <p:line1>p:line1</p:line1>
            <p:line2>p:line2</p:line2>
            <p:line3>p:line3</p:line3>
            <p:line4>p:line4</p:line4>
            <p:city>p:city</p:city>
            <p:county xsi:type="p:Code"/>
            <p:postalcode>p:postalcode</p:postalcode>
            <p:state xsi:type="p:Code"/>
            <p:country xsi:type="p:Code"/>
        </p:billingaddress>
        <p:billingemail>
            <p:contactmechanismtype>p:contactmechanismtype</p:contactmechanismtype>
            <p:effectivedaterange/>
            <p:istextmessageenabled>true</p:istextmessageenabled>
            <p:universalrecordid>p:universalrecordid</p:universalrecordid>
        </p:billingemail>
        <p:billingphone>
            <p:contactmechanismtype>p:contactmechanismtype</p:contactmechanismtype>
            <p:effectivedaterange/>
            <p:istextmessageenabled>true</p:istextmessageenabled>
            <p:universalrecordid>p:universalrecordid</p:universalrecordid>
        </p:billingphone>
        <p:billingphoneextension>
            <p:contactmechanismtype>p:contactmechanismtype</p:contactmechanismtype>
            <p:effectivedaterange/>
            <p:istextmessageenabled>true</p:istextmessageenabled>
            <p:universalrecordid>p:universalrecordid</p:universalrecordid>
        </p:billingphoneextension>
        <p:billingpager>
            <p:contactmechanismtype>p:contactmechanismtype</p:contactmechanismtype>
            <p:effectivedaterange/>
            <p:istextmessageenabled>true</p:istextmessageenabled>
            <p:universalrecordid>p:universalrecordid</p:universalrecordid>
        </p:billingpager>
        <p:empistatus xsi:type="p:Code">
            <p:code>p:code</p:code>
            <p:displaytext>p:displaytext</p:displaytext>
            <p:codesystem>p:codesystem</p:codesystem>
            <p:codesystemversion>p:codesystemversion</p:codesystemversion>
        </p:empistatus>
        <p:PersonalRelationship>
            <p:relationshipcategorycode xsi:type="p:Code"/>
            <p:effectivedaterange/>
            <p:Person/>
        </p:PersonalRelationship>
        <p:Citizenship>
            <p:effectivedaterange/>
            <p:Country/>
        </p:Citizenship>
        <p:ContactParty>
            <p:contactcategory xsi:type="p:Code"/>
            <p:contactreason>p:contactreason</p:contactreason>
            <p:isprimarycontact>true</p:isprimarycontact>
            <p:effectivedaterange/>
            <p:enddate>2001-12-31T12:00:00</p:enddate>
            <p:Person/>
        </p:ContactParty>
        <p:OccupationalHistoryObservation>
            <p:currentindustryclassification xsi:type="p:CodeWithOriginalText"/>
            <p:currentoccupation xsi:type="p:CodeWithOriginalText"/>
            <p:dateobserved/>
            <p:usualindustry xsi:type="p:CodeWithOriginalText"/>
            <p:usualoccupation xsi:type="p:CodeWithOriginalText"/>
            <p:yearsincurrentoccupation/>
            <p:yearsinusualoccupation/>
        </p:OccupationalHistoryObservation>
        <p:PersonPension>
            <p:pensionnumber>p:pensionnumber</p:pensionnumber>
            <p:pensionexpirationdate>2001-12-31T12:00:00</p:pensionexpirationdate>
        </p:PersonPension>
        <p:USUniformedServicesPerson>
            <p:militarybranch xsi:type="p:Code"/>
            <p:militaryrank xsi:type="p:Code"/>
            <p:militarystatus xsi:type="p:Code"/>
            <p:militarygrade xsi:type="p:Code"/>
            <p:effectivedaterange/>
        </p:USUniformedServicesPerson>
        <p:LanguageCapability>
            <p:ispreferred>true</p:ispreferred>
            <p:language xsi:type="p:Code"/>
            <p:methodofexpression xsi:type="p:Code"/>
            <p:proficiencylevel xsi:type="p:Code"/>
        </p:LanguageCapability>
        <p:AdvanceDirective xsi:type="p:AdvanceDirective">
            <p:category xsi:type="p:Code"/>
            <p:comments>p:comments</p:comments>
            <p:datelastverified>0</p:datelastverified>
            <p:effectivedaterange/>
            <p:image/>
            <p:paperlocator>p:paperlocator</p:paperlocator>
            <p:status xsi:type="p:Code"/>
        </p:AdvanceDirective>
    </p:Person> '
