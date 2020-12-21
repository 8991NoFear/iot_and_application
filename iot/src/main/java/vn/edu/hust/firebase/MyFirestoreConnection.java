package vn.edu.hust.firebase;

import java.io.IOException;
import java.io.InputStream;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

public class MyFirestoreConnection {
	private final String ACCOUNT_PATH = "credentials.json";

	private static MyFirestoreConnection connect = null;
	
	private MyFirestoreConnection() {
		try {
			ClassLoader classLoader = getClass().getClassLoader();
	        InputStream serviceAccount = classLoader.getResourceAsStream(ACCOUNT_PATH);

//	         Khong hoat dong do file code va file resource cung nam trong file nen jar
//	         URL url = getClass().getResource(ACCOUNT_PATH);
//	         FileInputStream serviceAccount = new FileInputStream(url.getFile());

	        FirebaseOptions options;
			options = FirebaseOptions.builder()
				    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
				    .build();
			
			FirebaseApp.initializeApp(options);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static MyFirestoreConnection getConnection() {
		if (connect == null) {
			connect = new MyFirestoreConnection();
		}
		return connect;
	}
	
	public Firestore getMyFirestore() {
        return FirestoreClient.getFirestore();
	}
	
}
