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
package org.hisp.dhis.webapi.controller.tracker.export;

import static org.hisp.dhis.webapi.controller.tracker.TrackerControllerSupport.RESOURCE_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.StringUtils;
import org.hisp.dhis.category.CategoryOptionCombo;
import org.hisp.dhis.common.AssignedUserSelectionMode;
import org.hisp.dhis.common.IdSchemes;
import org.hisp.dhis.common.OrganisationUnitSelectionMode;
import org.hisp.dhis.common.PagerUtils;
import org.hisp.dhis.commons.util.TextUtils;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dxf2.common.OrderParams;
import org.hisp.dhis.dxf2.events.event.Event;
import org.hisp.dhis.dxf2.events.event.EventSearchParams;
import org.hisp.dhis.dxf2.events.event.EventService;
import org.hisp.dhis.dxf2.events.event.Events;
import org.hisp.dhis.dxf2.util.InputUtils;
import org.hisp.dhis.dxf2.webmessage.WebMessageException;
import org.hisp.dhis.dxf2.webmessage.WebMessageUtils;
import org.hisp.dhis.event.EventStatus;
import org.hisp.dhis.node.Preset;
import org.hisp.dhis.program.ProgramStageInstanceService;
import org.hisp.dhis.program.ProgramStatus;
import org.hisp.dhis.query.Order;
import org.hisp.dhis.schema.Schema;
import org.hisp.dhis.schema.SchemaService;
import org.hisp.dhis.tracker.domain.mapper.EventMapper;
import org.hisp.dhis.tracker.domain.web.PagingWrapper;
import org.hisp.dhis.webapi.controller.event.mapper.RequestToSearchParamsMapper;
import org.hisp.dhis.webapi.service.ContextService;
import org.hisp.dhis.webapi.utils.ContextUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@RestController
@RequestMapping( value = RESOURCE_PATH + "/" + TrackerEventsExportController.EVENTS )
@RequiredArgsConstructor
public class TrackerEventsExportController
{
    protected static final String EVENTS = "events";

    private static final EventMapper EVENTS_MAPPER = Mappers.getMapper( EventMapper.class );

    private final EventService eventService;

    private final ContextService contextService;

    private final InputUtils inputUtils;

    private final DataElementService dataElementService;

    private final RequestToSearchParamsMapper requestToSearchParamsMapper;

    private final SchemaService schemaService;

    private final ProgramStageInstanceService programStageInstanceService;

    private Schema schema;

    @PostConstruct
    void setupSchema()
    {
        schema = schemaService.getDynamicSchema( Event.class );
    }

    @GetMapping( produces = APPLICATION_JSON_VALUE )
    public PagingWrapper<org.hisp.dhis.tracker.domain.Event> getEvents(
        @RequestParam( required = false ) String program,
        @RequestParam( required = false ) String programStage,
        @RequestParam( required = false ) ProgramStatus programStatus,
        @RequestParam( required = false ) Boolean followUp,
        @RequestParam( required = false ) String trackedEntityInstance,
        @RequestParam( required = false ) String orgUnit,
        @RequestParam( required = false ) OrganisationUnitSelectionMode ouMode,
        @RequestParam( required = false ) AssignedUserSelectionMode assignedUserMode,
        @RequestParam( required = false ) String assignedUser,
        @RequestParam( required = false ) Date startDate,
        @RequestParam( required = false ) Date endDate,
        @RequestParam( required = false ) Date dueDateStart,
        @RequestParam( required = false ) Date dueDateEnd,
        @RequestParam( required = false ) Date lastUpdated,
        @RequestParam( required = false ) Date lastUpdatedStartDate,
        @RequestParam( required = false ) Date lastUpdatedEndDate,
        @RequestParam( required = false ) String lastUpdatedDuration,
        @RequestParam( required = false ) EventStatus status,
        @RequestParam( required = false ) String attributeCc,
        @RequestParam( required = false ) String attributeCos,
        @RequestParam( required = false ) boolean skipMeta,
        @RequestParam( required = false ) Integer page,
        @RequestParam( required = false ) Integer pageSize,
        @RequestParam( required = false ) boolean totalPages,
        @RequestParam( required = false ) Boolean skipPaging,
        @RequestParam( required = false ) Boolean paging,
        @RequestParam( required = false ) String order,
        @RequestParam( required = false ) String attachment,
        @RequestParam( required = false, defaultValue = "false" ) boolean includeDeleted,
        @RequestParam( required = false ) String event,
        @RequestParam( required = false ) Boolean skipEventId,
        @RequestParam( required = false ) Set<String> filter,
        @RequestParam Map<String, String> parameters, IdSchemes idSchemes,
        HttpServletRequest request )
        throws WebMessageException
    {
        List<String> fields = Lists.newArrayList( contextService.getParameterValues( "fields" ) );

        if ( fields.isEmpty() )
        {
            fields.addAll( Preset.ALL.getFields() );
        }

        CategoryOptionCombo attributeOptionCombo = inputUtils.getAttributeOptionCombo( attributeCc, attributeCos,
            true );

        Set<String> eventIds = TextUtils.splitToArray( event, TextUtils.SEMICOLON );

        Set<String> assignedUserIds = TextUtils.splitToArray( assignedUser, TextUtils.SEMICOLON );

        Map<String, String> dataElementOrders = getDataElementsFromOrder( order );

        lastUpdatedStartDate = lastUpdatedStartDate != null ? lastUpdatedStartDate : lastUpdated;

        skipPaging = PagerUtils.isSkipPaging( skipPaging, paging );

        EventSearchParams params = requestToSearchParamsMapper.map( program, programStage, programStatus, followUp,
            orgUnit, ouMode, trackedEntityInstance, startDate, endDate, dueDateStart, dueDateEnd, lastUpdatedStartDate,
            lastUpdatedEndDate, lastUpdatedDuration, status, attributeOptionCombo, idSchemes, page, pageSize,
            totalPages, skipPaging, getOrderParams( order ), getGridOrderParams( order, dataElementOrders ),
            false, eventIds, skipEventId, assignedUserMode, assignedUserIds, filter, dataElementOrders.keySet(),
            false, includeDeleted );

        Events events = eventService.getEvents( params );

        if ( hasHref( fields, skipEventId ) )
        {
            events.getEvents().forEach( e -> e.setHref( getUri( e.getEvent(), request ) ) );
        }

        PagingWrapper<org.hisp.dhis.tracker.domain.Event> eventPagingWrapper = new PagingWrapper<>();

        if ( events.getPager() != null )
        {
            eventPagingWrapper = eventPagingWrapper.withPager( events.getPager() );
        }

        return eventPagingWrapper.withInstances( EVENTS_MAPPER.fromCollection( events.getEvents() ) );

    }

    private String getUri( String eventUid, HttpServletRequest request )
    {
        return UriComponentsBuilder.fromUriString( ContextUtils.getRootPath( request ) )
            .pathSegment( RESOURCE_PATH, EVENTS, eventUid )
            .build()
            .toString();
    }

    protected boolean hasHref( List<String> fields, Boolean skipEventId )
    {
        return (skipEventId == null || !skipEventId) && fieldsContainsHref( fields );
    }

    private boolean fieldsContainsHref( List<String> fields )
    {
        for ( String field : fields )
        {
            // For now assume href/access if * or preset is requested
            if ( field.contains( "href" ) || field.equals( "*" ) || field.startsWith( ":" ) )
            {
                return true;
            }
        }

        return false;
    }

    private List<Order> getOrderParams( String order )
    {
        if ( order != null && !StringUtils.isEmpty( order ) )
        {
            OrderParams op = new OrderParams( Sets.newLinkedHashSet( Arrays.asList( order.split( "," ) ) ) );
            return op.getOrders( schema );
        }

        return null;
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private Map<String, String> getDataElementsFromOrder( String allOrders )
    {
        Map<String, String> dataElements = new HashMap<>();

        if ( allOrders != null )
        {
            for ( String order : TextUtils.splitToArray( allOrders, TextUtils.SEMICOLON ) )
            {
                String[] orderParts = order.split( ":" );
                DataElement de = dataElementService.getDataElement( orderParts[0] );
                if ( de != null )
                {
                    String direction = "asc";
                    if ( orderParts.length == 2 && orderParts[1].equalsIgnoreCase( "desc" ) )
                    {
                        direction = "desc";
                    }
                    dataElements.put( de.getUid(), direction );
                }
            }
        }
        return dataElements;
    }

    private List<String> getGridOrderParams( String order, Map<String, String> dataElementOrders )
    {
        List<String> dataElementOrderList = new ArrayList<>();

        if ( !StringUtils.isEmpty( order ) && dataElementOrders != null && dataElementOrders.size() > 0 )
        {
            String[] orders = order.split( ";" );

            for ( String orderItem : orders )
            {
                String dataElementCandidate = orderItem.split( ":" )[0];
                if ( dataElementOrders.containsKey( dataElementCandidate ) )
                {
                    dataElementOrderList
                        .add( dataElementCandidate + ":" + dataElementOrders.get( dataElementCandidate ) );
                }
            }
        }

        return dataElementOrderList;
    }

    @RequestMapping( value = "/{uid}", method = RequestMethod.GET )
    public org.hisp.dhis.tracker.domain.Event getEvent(
        @PathVariable( "uid" ) String uid,
        @RequestParam Map<String, String> parameters,
        HttpServletRequest request )
        throws Exception
    {
        Event event = eventService.getEvent( programStageInstanceService.getProgramStageInstance( uid ) );

        if ( event == null )
        {
            throw new WebMessageException( WebMessageUtils.notFound( "Event not found for ID " + uid ) );
        }

        event.setHref( getUri( uid, request ) );

        return EVENTS_MAPPER.from( event );
    }

}
