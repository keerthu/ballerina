/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.net.ws;

import org.ballerinalang.connector.api.AnnAttrValue;
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.model.values.BStruct;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket service for service dispatching.
 */
public class WebSocketService implements Service {

    private final Service service;
    private final String[] negotiableSubProtocols;
    private final int idleTimeoutInSeconds;
    private final Map<String, Resource> resourceMap = new ConcurrentHashMap<>();

    public WebSocketService(Service service) {
        this.service = service;
        for (Resource resource : service.getResources()) {
            resourceMap.put(resource.getName(), resource);
        }

        Annotation configAnnotation =
                service.getAnnotation(Constants.WEBSOCKET_PACKAGE_NAME, Constants.ANNOTATION_CONFIGURATION);
        negotiableSubProtocols = findNegotiableSubProtocols(configAnnotation);
        idleTimeoutInSeconds = findIdleTimeoutInSeconds(configAnnotation);
    }

    @Override
    public String getName() {
        return service.getName();
    }

    @Override
    public String getPackage() {
        return service.getPackage();
    }

    @Override
    public Annotation getAnnotation(String pkgPath, String name) {
        return service.getAnnotation(pkgPath, name);
    }

    @Override
    public Resource[] getResources() {
        return service.getResources();
    }

    public Resource getResourceByName(String resourceName) {
        return resourceMap.get(resourceName);
    }

    public String[] getNegotiableSubProtocols() {
        return negotiableSubProtocols;
    }

    public int getIdleTimeoutInSeconds() {
        return idleTimeoutInSeconds;
    }

    public BStruct createHandshakeConnectionStruct() {
        return ConnectorUtils.createStruct(service.getResources()[0], Constants.WEBSOCKET_PACKAGE_NAME,
                                           Constants.STRUCT_WEBSOCKET_HANDSHAKE_CONNECTION);
    }

    public BStruct createConnectionStruct() {
        return ConnectorUtils.createStruct(service.getResources()[0], Constants.WEBSOCKET_PACKAGE_NAME,
                                    Constants.STRUCT_WEBSOCKET_CONNECTION);
    }

    public BStruct createTextFrameStruct() {
        return ConnectorUtils.createStruct(service.getResources()[0], Constants.WEBSOCKET_PACKAGE_NAME,
                                           Constants.STRUCT_WEBSOCKET_TEXT_FRAME);
    }

    public BStruct createBinaryFrameStruct() {
        return ConnectorUtils.createStruct(service.getResources()[0], Constants.WEBSOCKET_PACKAGE_NAME,
                                           Constants.STRUCT_WEBSOCKET_BINARY_FRAME);
    }

    public BStruct createCloseFrameStruct() {
        return ConnectorUtils.createStruct(service.getResources()[0], Constants.WEBSOCKET_PACKAGE_NAME,
                                           Constants.STRUCT_WEBSOCKET_CLOSE_FRAME);
    }

    private String[] findNegotiableSubProtocols(Annotation configAnnotation) {
        if (configAnnotation == null) {
            return null;
        }
        AnnAttrValue annAttrsubProtocols = configAnnotation.getAnnAttrValue(Constants.ANNOTATION_ATTR_SUB_PROTOCOLS);
        if (annAttrsubProtocols == null) {
            return null;
        }

        AnnAttrValue[] subProtocolsInAnnotation = annAttrsubProtocols.getAnnAttrValueArray();
        String[] negotiableSubProtocols = new String[subProtocolsInAnnotation.length];
        for (int i = 0; i < subProtocolsInAnnotation.length; i++) {
            negotiableSubProtocols[i] = subProtocolsInAnnotation[i].getStringValue();
        }
        return negotiableSubProtocols;
    }

    private int findIdleTimeoutInSeconds(Annotation configAnnotation) {
        if (configAnnotation == null) {
            return 0;
        }
        AnnAttrValue annAttrIdleTimeout = configAnnotation.getAnnAttrValue(Constants.ANNOTATION_ATTR_IDLE_TIMEOUT);
        if (annAttrIdleTimeout == null) {
            return 0;
        }
        return new Long(annAttrIdleTimeout.getIntValue()).intValue();
    }
}
