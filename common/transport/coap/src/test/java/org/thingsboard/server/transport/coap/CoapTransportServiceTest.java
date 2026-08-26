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
package org.thingsboard.server.transport.coap;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.thingsboard.server.transport.coap.client.CoapClientContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CoapTransportServiceTest {

    @Test
    void givenTransportShutdown_whenStopping_thenSubscriptionTerminationIsRequested() {
        CoapTransportService service = new CoapTransportService();
        CoapTransportContext transportContext = mock(CoapTransportContext.class);
        CoapClientContext clientContext = mock(CoapClientContext.class);
        when(transportContext.getClientContext()).thenReturn(clientContext);
        ReflectionTestUtils.setField(service, "coapTransportContext", transportContext);

        service.shutdown();

        verify(clientContext).sendBestEffortSubscriptionTerminationNotifications();
    }

}
