package vn.edu.hust;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hivemq.extension.sdk.api.ExtensionMain;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.events.EventRegistry;
import com.hivemq.extension.sdk.api.parameter.ExtensionInformation;
import com.hivemq.extension.sdk.api.parameter.ExtensionStartInput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStartOutput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStopInput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStopOutput;
import com.hivemq.extension.sdk.api.services.Services;
import com.hivemq.extension.sdk.api.services.auth.SecurityRegistry;
import com.hivemq.extension.sdk.api.services.intializer.InitializerRegistry;

import vn.edu.hust.auth.MyAuthenticatorProvider;

public class Main implements ExtensionMain {

    private static final @NotNull Logger log = LoggerFactory.getLogger(Main.class);

    @Override
    public void extensionStart(final @NotNull ExtensionStartInput extensionStartInput, final @NotNull ExtensionStartOutput extensionStartOutput) {
        try {
            final ExtensionInformation extensionInformation = extensionStartInput.getExtensionInformation();
            log.info("Start " + extensionInformation.getName() + ":" + extensionInformation.getVersion());

            addEventListener();
            addAuthenticator();
            addPublishInterceptor();
            
        } catch (Exception e) {
            log.error("Exception thrown at extension start: ", e);
        }
    }

    @Override
    public void extensionStop(final @NotNull ExtensionStopInput extensionStopInput, final @NotNull ExtensionStopOutput extensionStopOutput) {

        final ExtensionInformation extensionInformation = extensionStopInput.getExtensionInformation();
        log.info("Stopped " + extensionInformation.getName() + ":" + extensionInformation.getVersion());

    }

    private void addEventListener() {
        final EventRegistry eventRegistry = Services.eventRegistry();
        final Listener listener = new Listener();
        eventRegistry.setClientLifecycleEventListener(input -> listener);
    }

    private void addPublishInterceptor() {
    	final InitializerRegistry initializerRegistry = Services.initializerRegistry();
    	final MyPublishInboundInterceptor interceptor = new MyPublishInboundInterceptor();
    	initializerRegistry.setClientInitializer((initializerInput, clientContext) -> clientContext.addPublishInboundInterceptor(interceptor));
    }
    
    private void addAuthenticator() {
    	SecurityRegistry securityRegistry = Services.securityRegistry();
        MyAuthenticatorProvider authenticatiorProvider = new MyAuthenticatorProvider();
        securityRegistry.setAuthenticatorProvider(authenticatiorProvider);
    }
}
