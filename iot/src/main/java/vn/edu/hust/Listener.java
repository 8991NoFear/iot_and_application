package vn.edu.hust;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.cloud.firestore.Firestore;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.events.client.ClientLifecycleEventListener;
import com.hivemq.extension.sdk.api.events.client.parameters.AuthenticationSuccessfulInput;
import com.hivemq.extension.sdk.api.events.client.parameters.ConnectionStartInput;
import com.hivemq.extension.sdk.api.events.client.parameters.DisconnectEventInput;
import com.hivemq.extension.sdk.api.packets.general.MqttVersion;

import vn.edu.hust.firebase.MyFirestoreConnection;

public class Listener implements ClientLifecycleEventListener {

	private static final Logger log = LoggerFactory.getLogger(Main.class);
	private final Firestore db = MyFirestoreConnection.getConnection().getMyFirestore();

	@Override
	public void onMqttConnectionStart(final @NotNull ConnectionStartInput connectionStartInput) {
		final MqttVersion version = connectionStartInput.getConnectPacket().getMqttVersion();
		switch (version) {
		case V_5:
			log.info("MQTT 5 client connected with id: {} ", connectionStartInput.getClientInformation().getClientId());
			break;
		case V_3_1_1:
			log.info("MQTT 3.1.1 client connected with id: {} ",
					connectionStartInput.getClientInformation().getClientId());
			break;
		case V_3_1:
			log.info("MQTT 3.1 client connected with id: {} ",
					connectionStartInput.getClientInformation().getClientId());
			break;
		}
	}

	@Override
	public void onAuthenticationSuccessful(@NotNull AuthenticationSuccessfulInput authenticationSuccessfulInput) {
		db.collection("devices").document(authenticationSuccessfulInput.getClientInformation().getClientId()).update("status",
				true);
	}

	@Override
	public void onDisconnect(final @NotNull DisconnectEventInput disconnectEventInput) {
		log.info("client " + disconnectEventInput.getClientInformation().getClientId() + " : Disconnected");
		db.collection("devices").document(disconnectEventInput.getClientInformation().getClientId()).update("status",
				false);
	}

}
