package vn.edu.hust.auth;

import java.nio.ByteBuffer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.auth.SimpleAuthenticator;
import com.hivemq.extension.sdk.api.auth.parameter.SimpleAuthInput;
import com.hivemq.extension.sdk.api.auth.parameter.SimpleAuthOutput;
import com.hivemq.extension.sdk.api.auth.parameter.TopicPermission;
import com.hivemq.extension.sdk.api.packets.auth.ModifiableDefaultPermissions;
import com.hivemq.extension.sdk.api.packets.connect.ConnackReasonCode;
import com.hivemq.extension.sdk.api.packets.connect.ConnectPacket;
import com.hivemq.extension.sdk.api.services.builder.Builders;

import vn.edu.hust.Main;
import vn.edu.hust.firebase.MyFirestoreConnection;

public class MySimpleAuthenticator implements SimpleAuthenticator {
	private static final @NotNull Logger log = LoggerFactory.getLogger(Main.class);
	
	@Override
	public void onConnect(@NotNull SimpleAuthInput simpleAuthInput, @NotNull SimpleAuthOutput simpleAuthOutput) {
        final ConnectPacket connectPacket = simpleAuthInput.getConnectPacket();
        
        try {
			MyFirestoreConnection conn = MyFirestoreConnection.getConnection();
	        Firestore db = conn.getMyFirestore();

	        String username = connectPacket.getUserName().get();
	        ByteBuffer bb = connectPacket.getPassword().get();
	        byte[] bytes = new byte[bb.capacity()];
	        bb.get(bytes);
	        String password = new String(bytes, "UTF-8");
	        String clientId = connectPacket.getClientId();
	        
	        ApiFuture<QuerySnapshot> future = db.collection("devices").whereEqualTo("username", username).get();
	        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
	        
	        if (documents.isEmpty()) {
	        	simpleAuthOutput.failAuthentication(ConnackReasonCode.BAD_USER_NAME_OR_PASSWORD, "wrong username or password");
	        	log.warn("Failed Authentication - BAD USERNAME - Client with id " + clientId);
	        } else {
	        	boolean founded = false;
	        	for (QueryDocumentSnapshot document : documents) {
					if (document.getId().equals(clientId)) {
						founded = true;
						if (password.equals(document.get("password").toString())) {
							// set default permission for this client --- DENY ALL SUBSCRIBE
					        final ModifiableDefaultPermissions defaultPermissions = simpleAuthOutput.getDefaultPermissions();
					        final TopicPermission permissions = Builders.topicPermission()
					                .topicFilter(clientId)
					                .qos(TopicPermission.Qos.ALL)
					                .activity(TopicPermission.MqttActivity.PUBLISH)
					                .type(TopicPermission.PermissionType.ALLOW)
					                .retain(TopicPermission.Retain.ALL)
					                .build();
					        defaultPermissions.add(permissions);

					        //authenticate the client successfully
							simpleAuthOutput.authenticateSuccessfully();
						} else {
							simpleAuthOutput.failAuthentication(ConnackReasonCode.BAD_USER_NAME_OR_PASSWORD, "wrong client indentifier");
							log.warn("Failed Authentication - BAD PASSWORD - Client with id " + clientId);
						}
	  	          }
	  	        }
	        	if (!founded) {
	        		simpleAuthOutput.failAuthentication(ConnackReasonCode.CLIENT_IDENTIFIER_NOT_VALID, "wrong client indentifier");
		        	log.warn("Failed Authentication - BAD CLIENT IDENTIFIER - Client with id " + clientId);
	        	}
	        }
	        
		} catch (Exception ex) {
			ex.printStackTrace();
			simpleAuthOutput.failAuthentication(ConnackReasonCode.BAD_USER_NAME_OR_PASSWORD, "wrong username or password");
			log.warn("Failed Authentication - MISSING CREDENTIALS - Client with id " + connectPacket.getClientId());
		}
	}
}