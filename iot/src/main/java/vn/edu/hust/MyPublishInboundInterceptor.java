
/*
 * Copyright 2018-present HiveMQ GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package vn.edu.hust;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.interceptor.publish.PublishInboundInterceptor;
import com.hivemq.extension.sdk.api.interceptor.publish.parameter.PublishInboundInput;
import com.hivemq.extension.sdk.api.interceptor.publish.parameter.PublishInboundOutput;
import com.hivemq.extension.sdk.api.packets.publish.AckReasonCode;
import com.hivemq.extension.sdk.api.packets.publish.PublishPacket;

import vn.edu.hust.firebase.MyFirestoreConnection;


public class MyPublishInboundInterceptor implements PublishInboundInterceptor {
	
	private static final Logger log = LoggerFactory.getLogger(Main.class);
	
    @Override
    public void onInboundPublish(final @NotNull PublishInboundInput publishInboundInput, final @NotNull PublishInboundOutput publishInboundOutput) {
    	PublishPacket publishPacket = publishInboundInput.getPublishPacket();
    	String clientId = publishInboundInput.getClientInformation().getClientId();
    	
    	if (publishPacket.getTopic().equals(clientId)) {
			 pushToFirestore(publishPacket, clientId);
		} else {
			publishInboundOutput.preventPublishDelivery(AckReasonCode.TOPIC_NAME_INVALID, "publish to unauthorized topic");

			log.warn("Not Authorized - TOPIC NAME INVALID - Client with id " + clientId);
		}
    }
    
    private void pushToFirestore(PublishPacket publishPacket, String clientId) {
		try {
			Optional<ByteBuffer> unModPayloadOpt = publishPacket.getPayload();
			ByteBuffer unmodPayload = unModPayloadOpt.get();
			final byte[] bytes = new byte[unmodPayload.capacity()];
			unmodPayload.get(bytes);
			String payload = new String(bytes, "UTF-8");
			JSONObject obj = new JSONObject(payload);
			
			MyFirestoreConnection conn = MyFirestoreConnection.getConnection();
	        Firestore db = conn.getMyFirestore();
	        
	        String[] keys = {"PM10", "PM25", "GAS", "NH3", "CO2"};
	        Map<String, Object> docData = new HashMap<>();
	        for (String key : keys) {
	        	if (obj.has(key)) {
		        	docData.put(key, obj.get(key));
		        }
	        }

	        String collectionPath = "devices/" + clientId + "/data";
	        long currentTimeMillis = System.currentTimeMillis();
	        ApiFuture<WriteResult> future = db.collection(collectionPath).document(String.valueOf(currentTimeMillis)).set(docData);
	        log.info("Push data of client with id " + clientId + " to firestore at: " + future.get().getUpdateTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}