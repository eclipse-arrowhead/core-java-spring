package eu.arrowhead.core.mscv.service;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreEventHandlerConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.database.entity.mscv.Target;
import eu.arrowhead.common.database.entity.mscv.VerificationEntryList;
import eu.arrowhead.common.database.view.mscv.SshTargetView;
import eu.arrowhead.common.drivers.EventDriver;
import eu.arrowhead.common.dto.shared.DeviceRequestDTO;
import eu.arrowhead.common.dto.shared.EventDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.SubscriptionRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.mscv.OS;
import eu.arrowhead.core.mscv.MscvDefaults;
import eu.arrowhead.core.mscv.quartz.VerificationJobFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
@Api(tags = {CoreCommonConstants.SWAGGER_TAG_ALL})
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
        allowedHeaders = {HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT}
)
@RestController(CommonConstants.MSCV_URI)
public class EventHandlerListener {

    private static final String PUBLISH_URI = "/publish";
    private static final int MAX_ATTEMPTS = 5;
    private final Logger logger = LogManager.getLogger();

    private final MscvDefaults mscvDefaults;
    private final MscvExecutionService executionService;
    private final MscvTargetService targetService;
    private final VerificationJobFactory jobFactory;
    private final EventDriver eventDriver;

    @Autowired
    public EventHandlerListener(final MscvDefaults mscvDefaults,
                                final MscvExecutionService executionService,
                                final MscvTargetService targetService,
                                final VerificationJobFactory jobFactory,
                                final EventDriver eventDriver) {
        super();
        this.mscvDefaults = mscvDefaults;
        this.executionService = executionService;
        this.targetService = targetService;
        this.jobFactory = jobFactory;
        this.eventDriver = eventDriver;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Event Handler callback controller", tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = "Success"),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Bad Request"),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(PUBLISH_URI)
    @ResponseBody
    public void publish(@RequestBody final EventDTO event) throws IOException, MscvException, SchedulerException {
        logger.debug("publish started ...");

        switch (event.getEventType()) {
            case CoreEventHandlerConstants.REGISTER_DEVICE_EVENT:
                final var registerDevice = eventDriver.convert(event.getPayload(), DeviceRequestDTO.class);
                createJob(convert(registerDevice));
                break;
            case CoreEventHandlerConstants.UNREGISTER_DEVICE_EVENT:
                final var unregisterDevice = eventDriver.convert(event.getPayload(), DeviceRequestDTO.class);
                deleteJob(convert(unregisterDevice));
                break;
            case CoreEventHandlerConstants.REGISTER_SYSTEM_EVENT:
                final var registerSystem = eventDriver.convert(event.getPayload(), SystemRequestDTO.class);
                createJob(convert(registerSystem));
                break;
            case CoreEventHandlerConstants.UNREGISTER_SYSTEM_EVENT:
                final var unregisterSystem = eventDriver.convert(event.getPayload(), SystemRequestDTO.class);
                deleteJob(convert(unregisterSystem));
                break;
            case CoreEventHandlerConstants.REGISTER_SERVICE_EVENT:
                final var registerService = eventDriver.convert(event.getPayload(), ServiceRegistryRequestDTO.class);
                createJob(convert(registerService));
                break;
            case CoreEventHandlerConstants.UNREGISTER_SERVICE_EVENT:
                final var unregisterService = eventDriver.convert(event.getPayload(), ServiceRegistryRequestDTO.class);
                deleteJob(convert(unregisterService));
                break;
            default:
                logger.warn("Unknown event type {} with payload: {}", event.getEventType(), event.getPayload());
        }
    }

    @PostConstruct
    public void subscribe() {
        boolean success = false;
        int count = 0;

        do {
            try {
                eventDriver.subscribe(createSubscriptionRequest(CoreEventHandlerConstants.REGISTER_DEVICE_EVENT));
                eventDriver.subscribe(createSubscriptionRequest(CoreEventHandlerConstants.UNREGISTER_DEVICE_EVENT));
                eventDriver.subscribe(createSubscriptionRequest(CoreEventHandlerConstants.REGISTER_SYSTEM_EVENT));
                eventDriver.subscribe(createSubscriptionRequest(CoreEventHandlerConstants.UNREGISTER_SYSTEM_EVENT));
                eventDriver.subscribe(createSubscriptionRequest(CoreEventHandlerConstants.REGISTER_SERVICE_EVENT));
                eventDriver.subscribe(createSubscriptionRequest(CoreEventHandlerConstants.UNREGISTER_SERVICE_EVENT));
                success = true;
            } catch (final Exception e) {
                logger.warn("Unable to subscribe to Event Handler: {}", e.getMessage());
                sleep(TimeUnit.SECONDS.toMillis(15));
            } finally {
                count++;
            }
        } while (!success && count <= MAX_ATTEMPTS);
    }

    @PreDestroy
    public void unsubscribe() {
        final SystemRequestDTO requester = eventDriver.getRequesterSystem();
        try {
            eventDriver.unsubscribe(CoreEventHandlerConstants.REGISTER_DEVICE_EVENT, requester.getSystemName(), requester.getAddress(), requester.getPort());
            eventDriver.unsubscribe(CoreEventHandlerConstants.UNREGISTER_DEVICE_EVENT, requester.getSystemName(), requester.getAddress(), requester.getPort());
            eventDriver.unsubscribe(CoreEventHandlerConstants.REGISTER_SYSTEM_EVENT, requester.getSystemName(), requester.getAddress(), requester.getPort());
            eventDriver.unsubscribe(CoreEventHandlerConstants.UNREGISTER_SYSTEM_EVENT, requester.getSystemName(), requester.getAddress(), requester.getPort());
            eventDriver.unsubscribe(CoreEventHandlerConstants.REGISTER_SERVICE_EVENT, requester.getSystemName(), requester.getAddress(), requester.getPort());
            eventDriver.unsubscribe(CoreEventHandlerConstants.UNREGISTER_SERVICE_EVENT, requester.getSystemName(), requester.getAddress(), requester.getPort());
        } catch (final Exception e) {
            logger.warn("Unable to unsubscribe from Event Handler: {}", e.getMessage());
        }
    }

    private void createJob(final SshTargetView targetView) throws MscvException, SchedulerException {
        final Target target = targetService.findOrCreateTarget(targetView);
        final VerificationEntryList entryList = executionService.findSuitableList(target);
        jobFactory.createVerificationJob(entryList, target);
    }

    private void deleteJob(final SshTargetView targetView) throws MscvException {
        final Target target = targetService.findOrCreateTarget(targetView);
        final VerificationEntryList entryList = executionService.findSuitableList(target);
        jobFactory.removeVerificationJob(entryList, target);
    }

    private SshTargetView convert(final DeviceRequestDTO dto) {
        return new SshTargetView() {
            @Override
            public String getAddress() {
                return dto.getAddress();
            }

            @Override
            public Integer getPort() {
                return mscvDefaults.getSshPort();
            }

            @Override
            public String getName() {
                return dto.getDeviceName();
            }

            @Override
            public OS getOs() {
                return mscvDefaults.getOs();
            }
        };
    }

    private SshTargetView convert(final SystemRequestDTO dto) {
        return new SshTargetView() {
            @Override
            public String getAddress() {
                return dto.getAddress();
            }

            @Override
            public Integer getPort() {
                return mscvDefaults.getSshPort();
            }

            @Override
            public String getName() {
                return dto.getSystemName();
            }

            @Override
            public OS getOs() {
                return mscvDefaults.getOs();
            }
        };
    }

    private SshTargetView convert(final ServiceRegistryRequestDTO dto) {
        return new SshTargetView() {
            @Override
            public String getAddress() {
                return dto.getProviderSystem().getAddress();
            }

            @Override
            public Integer getPort() {
                return mscvDefaults.getSshPort();
            }

            @Override
            public String getName() {
                return dto.getProviderSystem().getSystemName();
            }

            @Override
            public OS getOs() {
                return mscvDefaults.getOs();
            }
        };
    }

    private SubscriptionRequestDTO createSubscriptionRequest(final String eventType) {
        final SubscriptionRequestDTO dto = new SubscriptionRequestDTO();
        dto.setSubscriberSystem(eventDriver.getRequesterSystem());
        dto.setEventType(eventType);
        dto.setMatchMetaData(false);
        dto.setNotifyUri(CommonConstants.MSCV_URI + PUBLISH_URI);
        return dto;
    }

    private void sleep(final long sleepTime) {
        try {
            logger.info("Sleeping for {}s", TimeUnit.MILLISECONDS.toSeconds(sleepTime));
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            logger.warn(e.getMessage(), e);
        }
    }
}
