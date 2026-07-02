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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.coap.MessageObserver;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.elements.EndpointContext;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class TbCoapMessageObserver implements MessageObserver {

    private final int msgId;
    private final Consumer<Integer> onAcknowledge;
    private final Consumer<Integer> onTimeout;
    private final String context;

    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    public void onRetransmission() {
        log.info("[{}][mid={}] Californium requested retransmission", context, msgId);
    }

    @Override
    public void onResponse(Response response) {

    }

    @Override
    public void onAcknowledgement() {
        log.info("[{}][mid={}] ACK received", context, msgId);
        onAcknowledge.accept(msgId);
    }

    @Override
    public void onReject() {
        log.info("[{}][mid={}] Message rejected", context, msgId);
    }

    @Override
    public void onTimeout() {
        log.info("[{}][mid={}] Message timed out", context, msgId);
        if (onTimeout != null) {
            onTimeout.accept(msgId);
        }
    }

    @Override
    public void onCancel() {
        log.info("[{}][mid={}] Message canceled", context, msgId);
    }

    @Override
    public void onReadyToSend() {

    }

    @Override
    public void onConnecting() {

    }

    @Override
    public void onDtlsRetransmission(int flight) {
        log.info("[{}][mid={}] DTLS retransmission flight {}", context, msgId, flight);
    }

    @Override
    public void onSent(boolean retransmission) {
        log.info("[{}][mid={}] Message sent (retransmission={})", context, msgId, retransmission);
    }

    @Override
    public void onSendError(Throwable error) {
        log.warn("[{}][mid={}] Send error", context, msgId, error);
    }

    @Override
    public void onResponseHandlingError(Throwable cause) {
        log.warn("[{}][mid={}] Response handling error", context, msgId, cause);
    }

    @Override
    public void onContextEstablished(EndpointContext endpointContext) {
        log.info("[{}][mid={}] Endpoint context established for peer {}", context, msgId,
                endpointContext != null ? endpointContext.getPeerAddress() : null);
    }

    @Override
    public void onTransferComplete() {
        log.info("[{}][mid={}] Transfer complete", context, msgId);
    }
}
