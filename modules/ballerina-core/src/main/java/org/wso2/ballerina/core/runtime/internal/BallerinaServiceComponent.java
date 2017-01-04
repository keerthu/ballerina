/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.core.runtime.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.connectors.http.server.HTTPListenerManager;
import org.wso2.ballerina.core.runtime.BalProgramExecutor;
import org.wso2.ballerina.core.runtime.Constants;
import org.wso2.ballerina.core.runtime.MessageProcessor;
import org.wso2.ballerina.core.runtime.deployer.BalDeployer;
import org.wso2.ballerina.core.runtime.errors.handler.ServerConnectorErrorHandler;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.carbon.kernel.utils.Utils;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.TransportListenerManager;
import org.wso2.carbon.messaging.TransportSender;

import java.io.File;


/**
 * Service component for Ballerina.
 */
@Component(
        name = "org.wso2.ballerina.BallerinaService",
        immediate = true
)
public class BallerinaServiceComponent {

    private static final Logger log = LoggerFactory.getLogger(BallerinaServiceComponent.class);

    @Activate
    protected void start(BundleContext bundleContext) {

        //Creating the processor and registering the service
        bundleContext.registerService(CarbonMessageProcessor.class, new MessageProcessor(), null);

        // Registering HTTP Listener Manager with transport framework
        bundleContext.registerService(TransportListenerManager.class, HTTPListenerManager.getInstance(), null);

        ServiceContextHolder.getInstance().setBundleContext(bundleContext);

        //Determine the runtime mode

        String runtimeMode = System.getProperty(Constants.SYSTEM_PROP_RUN_MODE);
        String runningFileName = System.getProperty(Constants.SYSTEM_PROP_RUN_FILE);
        if (runtimeMode == null) {
            // TODO : This is temporary fix, to run in pax exam mode. Fix this properly.
            runtimeMode = Constants.SYSTEM_PROP_RUN_MODE_SERVER;
            runningFileName = Utils.getCarbonHome().toString();
            log.warn("Ballerina is running in carbon server mode. You SHOULDN'T run in this mode...!");
        }
        File runningFile;
        if (runningFileName != null) {
            runningFile = new File(runningFileName);
            ServiceContextHolder.getInstance().setRunningFile(runningFile);
        } else {
            // Can't Continue. We shouldn't be here. that means there is a bug in the startup script.
            log.error("Can't get target file or directory to run. System property {} is not set.",
                    Constants.SYSTEM_PROP_RUN_FILE);
            ServiceContextHolder.getInstance().setRuntimeMode(Constants.RuntimeMode.ERROR);
            return;
        }

        if (runtimeMode.equalsIgnoreCase(Constants.SYSTEM_PROP_RUN_MODE_RUN)) {
            ServiceContextHolder.getInstance().setRuntimeMode(Constants.RuntimeMode.RUN_FILE);
        } else if (runtimeMode.equalsIgnoreCase(Constants.SYSTEM_PROP_RUN_MODE_SERVER)) {
            ServiceContextHolder.getInstance().setRuntimeMode(Constants.RuntimeMode.SERVER);
        } else {
            log.error("Can't identify Runtime mode.");
            ServiceContextHolder.getInstance().setRuntimeMode(Constants.RuntimeMode.ERROR);
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Runtime mode is set to : " + ServiceContextHolder.getInstance().getRuntimeMode());
        }

        if (Constants.RuntimeMode.SERVER == ServiceContextHolder.getInstance().getRuntimeMode()) {
            BalDeployer.deployBalFiles(runningFile);
        } else if (Constants.RuntimeMode.RUN_FILE == ServiceContextHolder.getInstance().getRuntimeMode()) {
            if (!runningFile.exists()) {
                log.error("File " + runningFile.getName() + " not found in the given location.");
                ServiceContextHolder.getInstance().setRuntimeMode(Constants.RuntimeMode.ERROR);
                return;
            }
            BalDeployer.deployBalFile(runningFile);
        }
        if (log.isDebugEnabled()) {
            log.debug("Ballerina runtime started...!");
        }
    }

    @Reference(
               name = "transport-sender",
               service = TransportSender.class,
               cardinality = ReferenceCardinality.OPTIONAL,
               policy = ReferencePolicy.DYNAMIC,
               unbind = "removeTransportSender"
    )
    protected void addTransportSender(TransportSender transportSender) {
        ServiceContextHolder.getInstance().addTransportSender(transportSender);
    }

    protected void removeTransportSender(TransportSender transportSender) {
        ServiceContextHolder.getInstance().removeTransportSender(transportSender);
    }

    @Reference(
            name = "global-symbolic-scope",
            service = SymScope.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeSymbolicScope"
    )
    protected void addSymbolicScope(SymScope symScope) {
//        ServiceContextHolder.getInstance().addTransportSender(transportSender);
    }

    protected void removeSymbolicScope(SymScope symScope) {
//        ServiceContextHolder.getInstance().removeTransportSender(transportSender);
    }

    @Reference(
            name = "error-handler",
            service = ServerConnectorErrorHandler.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeErrorHandler"
    )
    protected void addErrorHandler(ServerConnectorErrorHandler errorHandler) {
        ServiceContextHolder.getInstance().registerErrorHandler(errorHandler);
    }

    protected void removeErrorHandler(ServerConnectorErrorHandler errorHandler) {
        ServiceContextHolder.getInstance().unregisterErrorHandler(errorHandler.getProtocol());
    }

    @Reference(
            name = "carbon-service-info",
            service = CarbonServerInfo.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeCarbonServiceInfo"
    )
    protected void addCarbonServiceInfo(CarbonServerInfo info) {
        // We have to wait until Carbon Capability Provider completes its job to shutdown the OSGi runtime properly.
        if (Constants.RuntimeMode.ERROR == ServiceContextHolder.getInstance().getRuntimeMode()) {
            RuntimeUtils.shutdownRuntime();
        } else if (Constants.RuntimeMode.RUN_FILE == ServiceContextHolder.getInstance().getRuntimeMode()) {
            BallerinaFunction mainFunction = ServiceContextHolder.getInstance().getMainFunctionToExecute();
            if (mainFunction != null) {
                BalProgramExecutor.execute(mainFunction);
                RuntimeUtils.shutdownRuntime();
            }
        }
    }

    protected void removeCarbonServiceInfo(CarbonServerInfo info) {
        // Do Nothing.
    }

}
