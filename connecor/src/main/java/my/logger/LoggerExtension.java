package my.logger;

import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.web.spi.WebService;

public class LoggerExtension implements ServiceExtension {

    @Inject
    WebService webService;

    @Override
    public void initialize(ServiceExtensionContext context) {
        webService.registerResource(new LoggerController(context.getMonitor()));

    }
}