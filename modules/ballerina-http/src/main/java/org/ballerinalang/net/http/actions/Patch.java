/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ballerinalang.net.http.actions;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.ConnectorFuture;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.Constants;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * {@code Patch} is the PATCH action implementation of the HTTP Connector.
 */
@BallerinaAction(
        packageName = "ballerina.net.http",
        actionName = "patch",
        connectorName = Constants.CONNECTOR_NAME,
        args = {
                @Argument(name = "c", type = TypeKind.CONNECTOR),
                @Argument(name = "path", type = TypeKind.STRING),
                @Argument(name = "req", type = TypeKind.STRUCT, structType = "Request",
                        structPackage = "ballerina.net.http")
        },
        returnType = {@ReturnType(type = TypeKind.STRUCT, structType = "Response",
                structPackage = "ballerina.net.http")},
        connectorArgs = {
                @Argument(name = "serviceUri", type = TypeKind.STRING),
                @Argument(name = "options", type = TypeKind.STRUCT, structType = "Options",
                          structPackage = "ballerina.net.http")
        }
)
public class Patch extends AbstractHTTPAction {

    private static final Logger logger = LoggerFactory.getLogger(Patch.class);

    @Override
    public ConnectorFuture execute(Context context) {

        if (logger.isDebugEnabled()) {
            logger.debug("Executing Native Action : {}", this.getName());
        }

        try {
            // Execute the operation
            return executeNonBlockingAction(context, createCarbonMsg(context));
        } catch (Throwable t) {
            throw new BallerinaException("Failed to invoke 'patch' action in " + Constants.CONNECTOR_NAME
                    + ". " + t.getMessage(), context);
        }
    }

    protected HTTPCarbonMessage createCarbonMsg(Context context) {
        HTTPCarbonMessage cMsg = super.createCarbonMsg(context);
        cMsg.setProperty(Constants.HTTP_METHOD, Constants.HTTP_METHOD_PATCH);
        return cMsg;
    }

}
