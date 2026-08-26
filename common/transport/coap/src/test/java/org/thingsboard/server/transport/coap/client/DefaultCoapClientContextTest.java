/**
 * Copyright © 2016-2026 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.transport.coap.client;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.transport.TransportDeviceProfileCache;
import org.thingsboard.server.common.transport.TransportService;
import org.thingsboard.server.coapserver.CoapServerContext;
import org.thingsboard.server.queue.discovery.PartitionService;
import org.thingsboard.server.transport.coap.CoapTransportContext;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class DefaultCoapClientContextTest {

    @Test
    void givenRpcAndAttributeSubscriptions_whenSendingTerminationNotifications_thenBothAreNonConfirmableDeletedResponses() {
        TransportService transportService = mock(TransportService.class);
        DefaultCoapClientContext clientContext = new DefaultCoapClientContext(
                mock(CoapServerContext.class),
                mock(CoapTransportContext.class),
                transportService,
                mock(TransportDeviceProfileCache.class),
                mock(PartitionService.class));
        DeviceId deviceId = new DeviceId(UUID.randomUUID());
        TbCoapClientState state = new TbCoapClientState(deviceId);
        CoapExchange attrsExchange = mock(CoapExchange.class);
        CoapExchange rpcExchange = mock(CoapExchange.class);
        state.setAttrs(new TbCoapObservationState(attrsExchange, "attrs-token"));
        state.setRpc(new TbCoapObservationState(rpcExchange, "rpc-token"));

        @SuppressWarnings("unchecked")
        Map<DeviceId, TbCoapClientState> clients = (Map<DeviceId, TbCoapClientState>) ReflectionTestUtils.getField(clientContext, "clients");
        clients.put(deviceId, state);

        clientContext.sendBestEffortSubscriptionTerminationNotifications();

        var attrsResponse = org.mockito.ArgumentCaptor.forClass(Response.class);
        var rpcResponse = org.mockito.ArgumentCaptor.forClass(Response.class);
        verify(attrsExchange).respond(attrsResponse.capture());
        verify(rpcExchange).respond(rpcResponse.capture());
        assertDeletedNonResponse(attrsResponse.getValue());
        assertDeletedNonResponse(rpcResponse.getValue());
        verifyNoInteractions(transportService);
        assertTrue(clients.isEmpty());
    }

    @Test
    void givenOneInvalidSubscriptionExchange_whenSendingTerminationNotifications_thenRemainingExchangeIsStillNotified() {
        DefaultCoapClientContext clientContext = new DefaultCoapClientContext(
                mock(CoapServerContext.class),
                mock(CoapTransportContext.class),
                mock(TransportService.class),
                mock(TransportDeviceProfileCache.class),
                mock(PartitionService.class));
        DeviceId deviceId = new DeviceId(UUID.randomUUID());
        TbCoapClientState state = new TbCoapClientState(deviceId);
        CoapExchange invalidExchange = mock(CoapExchange.class);
        CoapExchange validExchange = mock(CoapExchange.class);
        doThrow(new IllegalStateException()).when(invalidExchange).respond(any(Response.class));
        state.setAttrs(new TbCoapObservationState(invalidExchange, "attrs-token"));
        state.setRpc(new TbCoapObservationState(validExchange, "rpc-token"));

        @SuppressWarnings("unchecked")
        Map<DeviceId, TbCoapClientState> clients = (Map<DeviceId, TbCoapClientState>) ReflectionTestUtils.getField(clientContext, "clients");
        clients.put(deviceId, state);

        clientContext.sendBestEffortSubscriptionTerminationNotifications();

        verify(validExchange).respond(any(Response.class));
    }

    private void assertDeletedNonResponse(Response response) {
        assertEquals(CoAP.ResponseCode.DELETED, response.getCode());
        assertEquals(CoAP.Type.NON, response.getType());
    }

}
