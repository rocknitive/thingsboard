/**
 * Copyright © 2016-2022 The Thingsboard Authors
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
package org.thingsboard.server.transport.coap.adaptors;

import com.google.gson.JsonElement;
import org.eclipse.californium.core.coap.Request;
import org.thingsboard.server.common.data.StringUtils;
import org.thingsboard.server.common.transport.adaptor.AdaptorException;
import org.thingsboard.server.gen.transport.TransportProtos;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CoapAdaptorUtils {

    /**
     * If the json object has only one key of type array which is called "telemetry" then use the array as the root
     * element and drop the surrounding container. This will allow multiple updates in one request just like in the
     * pure json device api.
     */
    public static JsonElement convertProtoJsonTelemetry(JsonElement json) throws AdaptorException {
        if (json.isJsonObject()) {
            var root = json.getAsJsonObject();
            if (root.has("telemetry") && root.get("telemetry").isJsonArray() && root.entrySet().size() == 1) {
                return root.get("telemetry");
            }
        }
        return json;
    }

    public static TransportProtos.GetAttributeRequestMsg toGetAttributeRequestMsg(Request inbound) throws AdaptorException {
        List<String> queryElements = inbound.getOptions().getUriQuery();
        TransportProtos.GetAttributeRequestMsg.Builder result = TransportProtos.GetAttributeRequestMsg.newBuilder();
        if (queryElements != null && queryElements.size() > 0) {
            Set<String> clientKeys = toKeys(queryElements, "clientKeys", "c");
            Set<String> sharedKeys = toKeys(queryElements, "sharedKeys", "s");
            if (clientKeys != null) {
                result.addAllClientAttributeNames(clientKeys);
            }
            if (sharedKeys != null) {
                result.addAllSharedAttributeNames(sharedKeys);
            }
        }
        result.setOnlyShared(false);
        return result.build();
    }

    private static Set<String> toKeys(List<String> queryElements, String ...attributeNames) {
        String keys = null;
        for (String queryElement : queryElements) {
            String[] queryItem = queryElement.split("=", 2);
            if (Arrays.asList(attributeNames).contains(queryItem[0])) {
                if (queryItem.length == 2 && !StringUtils.isEmpty(queryItem[1])) {
                    keys = queryItem[1];
                } else {
                    keys = "*";
                }
            }
        }
        if (keys != null) {
            return new HashSet<>(Arrays.asList(keys.split(",")));
        } else {
            return null;
        }
    }
}
