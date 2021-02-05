/*
 * Copyright (c) 2004-2021, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.notification;

/*
 * Copyright (c) 2004-2021, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import org.hisp.dhis.DhisSpringTest;
import org.hisp.dhis.analytics.AggregationType;
import org.hisp.dhis.common.DeliveryChannel;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementDomain;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.eventdatavalue.EventDataValue;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramInstance;
import org.hisp.dhis.program.ProgramInstanceService;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.program.ProgramStage;
import org.hisp.dhis.program.ProgramStageDataElement;
import org.hisp.dhis.program.ProgramStageDataElementService;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.program.ProgramStageInstanceService;
import org.hisp.dhis.program.ProgramStageService;
import org.hisp.dhis.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.program.ProgramTrackedEntityAttributeStore;
import org.hisp.dhis.program.notification.NotificationTrigger;
import org.hisp.dhis.program.notification.ProgramNotificationTemplate;
import org.hisp.dhis.program.notification.ProgramNotificationTemplateStore;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.trackedentity.TrackedEntityAttributeService;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceService;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValue;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValueService;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;

/**
 * @author Zubair Asghar
 */
public class ProgramNotificationMessageRendererTest extends DhisSpringTest
{
    private Program programA;

    private ProgramStage programStageA;

    private DataElement dataElementA;

    private DataElement dataElementB;

    private TrackedEntityAttribute trackedEntityAttributeA;

    private TrackedEntityAttribute trackedEntityAttributeB;

    private TrackedEntityAttributeValue trackedEntityAttributeValueA;

    private ProgramTrackedEntityAttribute programTrackedEntityAttributeA;

    private ProgramTrackedEntityAttribute programTrackedEntityAttributeB;

    private ProgramStageDataElement programStageDataElementA;

    private ProgramStageDataElement programStageDataElementB;

    private TrackedEntityInstance trackedEntityInstanceA;

    private ProgramInstance programInstanceA;

    private ProgramStageInstance programStageInstanceA;

    private EventDataValue eventDataValueA;

    private EventDataValue eventDataValueB;

    private OrganisationUnit organisationUnitA;

    private ProgramNotificationTemplate programNotificationTemplate;

    @Autowired
    private ProgramService programService;

    @Autowired
    private DataElementService dataElementService;

    @Autowired
    private TrackedEntityAttributeService attributeService;

    @Autowired
    private ProgramTrackedEntityAttributeStore programTrackedEntityAttributeStore;

    @Autowired
    private ProgramStageService programStageService;

    @Autowired
    private ProgramStageDataElementService programStageDataElementService;

    @Autowired
    private TrackedEntityInstanceService entityInstanceService;

    @Autowired
    private TrackedEntityAttributeValueService trackedEntityAttributeValueService;

    @Autowired
    private ProgramInstanceService programInstanceService;

    @Autowired
    private ProgramStageInstanceService programStageInstanceService;

    @Autowired
    private ProgramNotificationTemplateStore programNotificationTemplateStore;

    @Autowired
    private OrganisationUnitService organisationUnitService;

    @Autowired
    private ProgramNotificationMessageRenderer subject;

    @Override
    protected void setUpTest()
        throws Exception
    {
        DateTime testDate1 = DateTime.now();
        testDate1.withTimeAtStartOfDay();
        testDate1 = testDate1.minusDays( 70 );
        Date incidentDate = testDate1.toDate();

        DateTime testDate2 = DateTime.now();
        testDate2.withTimeAtStartOfDay();
        Date enrollmentDate = testDate2.toDate();

        dataElementA = createDataElement( 'A', ValueType.TEXT, AggregationType.NONE, DataElementDomain.TRACKER );
        dataElementA.setUid( "DEA-UID" );
        dataElementB = createDataElement( 'B', ValueType.TEXT, AggregationType.NONE, DataElementDomain.TRACKER );
        dataElementB.setUid( "DEB-UID" );

        dataElementService.addDataElement( dataElementA );
        dataElementService.addDataElement( dataElementB );

        trackedEntityAttributeA = createTrackedEntityAttribute( 'A' );
        trackedEntityAttributeB = createTrackedEntityAttribute( 'B' );

        attributeService.addTrackedEntityAttribute( trackedEntityAttributeA );
        attributeService.addTrackedEntityAttribute( trackedEntityAttributeB );

        organisationUnitA = createOrganisationUnit( 'A' );
        organisationUnitService.addOrganisationUnit( organisationUnitA );

        programA = createProgram( 'A', new HashSet<>(), organisationUnitA );
        programService.addProgram( programA );

        programTrackedEntityAttributeA = createProgramTrackedEntityAttribute( programA, trackedEntityAttributeA );
        programTrackedEntityAttributeA.setUid( "ATTRA-UID" );

        programTrackedEntityAttributeB = createProgramTrackedEntityAttribute( programA, trackedEntityAttributeB );
        programTrackedEntityAttributeB.setUid( "ATTRB-UID" );

        programTrackedEntityAttributeStore.save( programTrackedEntityAttributeA );
        programTrackedEntityAttributeStore.save( programTrackedEntityAttributeB );

        programA
            .setProgramAttributes( Arrays.asList( programTrackedEntityAttributeA, programTrackedEntityAttributeB ) );
        programService.updateProgram( programA );

        programStageA = createProgramStage( 'A', programA );
        programStageService.saveProgramStage( programStageA );

        programStageDataElementA = createProgramStageDataElement( programStageA, dataElementA, 1 );
        programStageDataElementB = createProgramStageDataElement( programStageA, dataElementB, 2 );

        programStageDataElementService.addProgramStageDataElement( programStageDataElementA );
        programStageDataElementService.addProgramStageDataElement( programStageDataElementB );

        programStageA
            .setProgramStageDataElements( Sets.newHashSet( programStageDataElementA, programStageDataElementB ) );
        programStageService.updateProgramStage( programStageA );

        programA.setProgramStages( Sets.newHashSet( programStageA ) );
        programService.updateProgram( programA );

        trackedEntityInstanceA = createTrackedEntityInstance( organisationUnitA );
        entityInstanceService.addTrackedEntityInstance( trackedEntityInstanceA );

        trackedEntityAttributeValueA = new TrackedEntityAttributeValue( trackedEntityAttributeA, trackedEntityInstanceA,
            "test" );
        trackedEntityAttributeValueService.addTrackedEntityAttributeValue( trackedEntityAttributeValueA );

        trackedEntityInstanceA.setTrackedEntityAttributeValues( Sets.newHashSet( trackedEntityAttributeValueA ) );
        entityInstanceService.updateTrackedEntityInstance( trackedEntityInstanceA );

        // ProgramInstance to be provided in message renderer
        programInstanceA = programInstanceService.enrollTrackedEntityInstance( trackedEntityInstanceA,
            programA, enrollmentDate, incidentDate, organisationUnitA );
        programInstanceA.setUid( "PI-UID" );
        programInstanceService.updateProgramInstance( programInstanceA );

        // ProgramStageInstance to be provided in message renderer
        programStageInstanceA = new ProgramStageInstance( programInstanceA, programStageA );
        programStageInstanceA.setDueDate( enrollmentDate );
        programStageInstanceA.setExecutionDate( new Date() );
        programStageInstanceA.setUid( "-PSI-UID" );

        eventDataValueA = new EventDataValue();
        eventDataValueA.setDataElement( dataElementA.getUid() );
        eventDataValueA.setAutoFields();
        eventDataValueA.setValue( "dataElementA-Text" );

        eventDataValueB = new EventDataValue();
        eventDataValueB.setDataElement( dataElementB.getUid() );
        eventDataValueB.setAutoFields();
        eventDataValueB.setValue( "dataElementB-Text" );

        programStageInstanceA.setEventDataValues( Sets.newHashSet( eventDataValueA, eventDataValueB ) );
        programStageInstanceService.addProgramStageInstance( programStageInstanceA );
        programInstanceA.getProgramStageInstances().add( programStageInstanceA );

        programInstanceService.updateProgramInstance( programInstanceA );

        programNotificationTemplate = new ProgramNotificationTemplate();
        programNotificationTemplate.setName( "Test-PNT" );
        programNotificationTemplate.setMessageTemplate( "message_template" );
        programNotificationTemplate.setDeliveryChannels( Sets.newHashSet( DeliveryChannel.SMS ) );
        programNotificationTemplate.setSubjectTemplate( "subject_template" );
        programNotificationTemplate.setNotificationTrigger( NotificationTrigger.PROGRAM_RULE );
        programNotificationTemplate.setAutoFields();
        programNotificationTemplate.setUid( "PNT-1" );

        programNotificationTemplateStore.save( programNotificationTemplate );
    }

    @Test
    public void testRendererForSimpleMessage()
    {
        NotificationMessage notificationMessage = subject.render( programInstanceA, programNotificationTemplate );
        assertEquals( "message_template", notificationMessage.getMessage() );
        assertEquals( "subject_template", notificationMessage.getSubject() );
    }

    @Test
    public void testRendererForMessageWithAttribute()
    {
    }

    @Test
    public void testRendererForMessageWithDataElement()
    {
    }
}
