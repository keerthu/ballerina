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
package org.ballerinalang.test.types.map;

import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Test map initializer expression.
 *
 * @since 0.8.0
 */
public class MapInitializerExprTest {

    private CompileResult compileResult;

    @BeforeTest
    public void setup() {
        compileResult = BTestUtils.compile("test-src/types/map/map-initializer-expr.bal");
    }

    @Test(description = "Test map initializer expression")
    public void testMapInitExpr() {
        BValue[] args = {};
        BValue[] returns = BTestUtils.invoke(compileResult, "mapInitTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BMap.class);

        BMap<String, BString> mapValue = (BMap<String, BString>) returns[0];
        Assert.assertEquals(mapValue.size(), 4);

        Assert.assertEquals(mapValue.get("animal1").stringValue(), "Lion");
        Assert.assertEquals(mapValue.get("animal2").stringValue(), "Cat");
        Assert.assertEquals(mapValue.get("animal4").stringValue(), "Dog");
    }

    @Test(description = "Test map initializing with different types")
    public void testMultiTypeMapInit() {
        BTestUtils.compile("test-src/types/map/multi-type-map-initializer.bal");
    }
    
    @Test
    public void testNestedMapInit() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testNestedMapInit", new BValue[] {});

        Assert.assertTrue(returns[0] instanceof BMap<?, ?>);
        BMap<String, BValue> outerMap = (BMap<String, BValue>) returns[0];
        Assert.assertEquals(outerMap.get("name"), new BString("Supun"));

        BValue info = outerMap.get("info");
        Assert.assertTrue(info instanceof BMap<?, ?>);
        BMap<String, BValue> infoMap = (BMap<String, BValue>) info;
        Assert.assertEquals(infoMap.get("city"), new BString("Colombo"));
        Assert.assertEquals(infoMap.get("country"), new BString("SriLanka"));
    }
    
    @Test
    public void testMapInitWithJson() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testMapInitWithJson", new BValue[] {});

        Assert.assertTrue(returns[0] instanceof BMap<?, ?>);
        BMap<String, BValue> outerMap = (BMap<String, BValue>) returns[0];
        Assert.assertEquals(outerMap.get("name"), new BString("Supun"));

        BValue info = outerMap.get("info");
        Assert.assertTrue(info instanceof BJSON);
        BJSON infoJson = (BJSON) info;
        Assert.assertEquals(infoJson.stringValue(), "{\"city\":\"Colombo\",\"country\":\"SriLanka\"}");
    }
    
    @Test
    public void testComplexMapInit() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testComplexMapInit", new BValue[] {});

        Assert.assertTrue(returns[0] instanceof BMap<?, ?>);
        BMap<String, BValue> outerMap = (BMap<String, BValue>) returns[0];
        Assert.assertEquals(outerMap.get("name").stringValue(), "Supun");

        BValue adrsArray = outerMap.get("addressArray");
        Assert.assertTrue(adrsArray instanceof BRefValueArray);
        BRefValueArray addressArray = (BRefValueArray) adrsArray;

        BValue adrs1 = addressArray.get(0);
        Assert.assertTrue(adrs1 instanceof BMap<?, ?>);
        BValue address = ((BMap) adrs1).get("address");
        Assert.assertTrue(address instanceof BMap<?, ?>);
        Assert.assertEquals(((BMap) address).get("city").stringValue(), "Colombo");

        BValue adrs2 = addressArray.get(1);
        Assert.assertTrue(adrs2 instanceof BMap<?, ?>);
        address = ((BMap) adrs2).get("address");
        Assert.assertTrue(address instanceof BMap<?, ?>);
        Assert.assertEquals(((BMap) address).get("city").stringValue(), "Kandy");

        BValue adrs3 = addressArray.get(2);
        Assert.assertTrue(adrs3 instanceof BMap<?, ?>);
        address = ((BMap) adrs3).get("address");
        Assert.assertTrue(address instanceof BMap<?, ?>);
        Assert.assertEquals(((BMap) address).get("city").stringValue(), "Galle");
    }
}
