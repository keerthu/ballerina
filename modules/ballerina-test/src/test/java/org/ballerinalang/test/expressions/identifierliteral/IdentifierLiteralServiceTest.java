/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.expressions.identifierliteral;

import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.test.services.testutils.EnvironmentInitializer;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * identifier literals in service and resource names.
 */
public class IdentifierLiteralServiceTest {

    private CompileResult application;

    @BeforeClass
    public void setup() {
        application = EnvironmentInitializer
                .setupProgramFile("test-src/expressions/identifierliteral/identifier-literal-service.bal");
    }

    @Test(description = "Test using identifier literals in service and resource names")
    public void testUsingIdentifierLiteralsInServiceAndResourceNames() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/identifierLiteral/resource", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("key").asText(), "keyVal");
        Assert.assertEquals(bJson.value().get("value").asText(), "valueOfTheString");
    }

    @Test(description = "Test identifier literals payload")
    public void testIdentifierLiteralsInPayload() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/identifierLiteral/resource2", "GET");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNotNull(response);
        String payload = response.getMessageDataSource().getMessageAsString();
        Assert.assertEquals(payload, "hello");
    }

    @AfterClass
    public void tearDown() {
         EnvironmentInitializer.cleanup(application);
    }

}
