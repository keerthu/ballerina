/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.test.expressions.literals;


import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.model.values.BXMLAttributes;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Local function invocation test.
 *
 * @since 0.8.0
 */
public class XMLAttributesTest {

    CompileResult xmlAttrProgFile;
    
    @BeforeClass
    public void setup() {
        xmlAttrProgFile = BTestUtils.compile("test-src/expressions/literals/xml/xml-attributes.bal");
    }

    @Test
    public void testAddAttributeWithString() {
        BValue[] returns = BTestUtils.invoke(xmlAttrProgFile, "testAddAttributeWithString");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns4=\"http://sample.com/wso2/f\" "
                + "xmlns:ns0Kf5j=\"http://sample.com/wso2/e\" foo1=\"bar1\" ns0Kf5j:foo2=\"bar2\" ns4:foo3=\"bar3\"/>");
    }
    
    @Test(expectedExceptions = {BLangRuntimeException.class}, 
            expectedExceptionsMessageRegExp = "error: error, message: localname of the attribute cannot be empty.*")
    public void testAddAttributeWithoutLocalname() {
        BValue[] returns = BTestUtils.invoke(xmlAttrProgFile, "testAddAttributeWithoutLocalname");
    }
    
    @Test
    public void testAddAttributeWithEmptyNamespace() {
        BValue[] returns = BTestUtils.invoke(xmlAttrProgFile, "testAddAttributeWithEmptyNamespace");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns3=\"http://sample.com/wso2/f\" foo1=\"bar\"/>");
    }
    
    @Test
    public void testAddNamespaceAsAttribute1() {
        BValue[] returns = BTestUtils.invoke(xmlAttrProgFile, "testAddNamespaceAsAttribute");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns3=\"http://sample.com/wso2/f\" " +
            "xmlns:ns4=\"http://wso2.com\"/>");
        
        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(), "<root xmlns=\"http://ballerinalang.org/\" " +
            "xmlns:ns3=\"http://sample.com/wso2/f\" xmlns:ns4=\"http://wso2.com\"/>");
    }
    
    @Test
    public void testAddAttributeWithQName() {
        BValue[] returns = BTestUtils.invoke(xmlAttrProgFile, "testAddAttributeWithQName");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns3=\"http://sample.com/wso2/f\" " +
                "xmlns:ns0=\"http://sample.com/wso2/a1\" ns0:foo1=\"bar1\"/>");
    }

    @Test
    public void testAddAttributeWithQName_1() {
        BValue[] returns = BTestUtils.invoke(xmlAttrProgFile, "testAddAttributeWithDiffQName_1");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns=\"http://sample.com/wso2/c1\" " +
                "xmlns:ns3=\"http://sample.com/wso2/f\" xmlns:ns4=\"http://sample.com/wso2/f/\" " +
                "xmlns:ns5=\"http://sample.com/wso2/f/\" xmlns:ns0=\"http://sample.com/wso2/a1\" " +
                "xmlns:ns1=\"http://sample.com/wso2/b1\" xmlns:pre=\"http://sample.com/wso2/f\" " +
                "ns4:diff=\"yes\" pre:foo1=\"bar1\"/>");
    }

    @Test
    public void testAddAttributeWithQName_2() {
        BValue[] returns = BTestUtils.invoke(xmlAttrProgFile, "testAddAttributeWithDiffQName_2");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns=\"http://sample.com/wso2/c1\" " +
                "xmlns:ns3=\"http://sample.com/wso2/f\" xmlns:ns4=\"http://sample.com/wso2/f/\" " +
                "xmlns:ns5=\"http://sample.com/wso2/f/\" xmlns:ns0=\"http://sample.com/wso2/a1\" " +
                "xmlns:ns1=\"http://sample.com/wso2/b1\" ns4:diff=\"yes\" ns5:foo1=\"bar1\"/>");
    }

    @Test
    public void testAddAttributeWithQName_3() {
        BValue[] returns = BTestUtils.invoke(xmlAttrProgFile, "testAddAttributeWithDiffQName_3");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns=\"http://sample.com/wso2/c1\" " +
                "xmlns:ns3=\"http://sample.com/wso2/f\" xmlns:ns4=\"http://sample.com/wso2/f/\" " + 
                "xmlns:ns5=\"http://sample.com/wso2/f/\" xmlns:ns0=\"http://sample.com/wso2/a1\" " +
                "xmlns:ns1=\"http://sample.com/wso2/b1\" ns4:diff=\"yes\" ns4:foo1=\"bar1\"/>");
    }

    @Test(expectedExceptions = { BLangRuntimeException.class }, 
            expectedExceptionsMessageRegExp = "error: error, message: failed to add attribute " +
            "'ns5:foo1'. prefix 'ns5' is already bound to namespace 'http://sample.com/wso2/f/'.*")
    public void testAddAttributeWithQName_4() {
        BValue[] returns = BTestUtils.invoke(xmlAttrProgFile, "testAddAttributeWithDiffQName_4");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns=\"http://sample.com/wso2/c1\" " +
                "xmlns:ns5=\"http://sample.com/wso2/f/\" xmlns:ns0=\"http://sample.com/wso2/a1\" " +
                "xmlns:ns1=\"http://sample.com/wso2/b1\" xmlns:ns4=\"http://sample.com/wso2/f/\" " +
                "xmlns:ns3=\"http://sample.com/wso2/f\" ns4:diff=\"yes\"/>");
    }

    @Test
    public void testAddAttributeWithQName_5() {
        BValue[] returns = BTestUtils.invoke(xmlAttrProgFile, "testAddAttributeWithDiffQName_5");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns=\"http://sample.com/wso2/c1\" " +
                "xmlns:ns3=\"http://sample.com/wso2/f\" xmlns:ns4=\"http://sample.com/wso2/f/\" " +
                "xmlns:ns5=\"http://sample.com/wso2/f/\" xmlns:ns0=\"http://sample.com/wso2/a1\" " +
                "xmlns:ns1=\"http://sample.com/wso2/b1\" xmlns:foo1=\"bar1\" " +
                "ns4:diff=\"yes\" foo2=\"bar2\" foo3=\"bar3\"/>");
    }


    
    @Test
    public void testUpdateAttributeWithString() {
        BValue[] returns = BTestUtils.invoke(xmlAttrProgFile, "testUpdateAttributeWithString");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns0=\"http://sample.com/wso2/e\" "
                + "foo1=\"newbar1\" ns0:foo2=\"newbar2\" foo3=\"newbar3\"/>");
    }

    @Test
    public void testUpdateAttributeWithString_1() {
        BValue[] returns = BTestUtils.invoke(xmlAttrProgFile, "testUpdateAttributeWithString_1");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns4=\"http://sample.com/wso2/f\" " +
                "xmlns:ns0Kf5j=\"http://sample.com/wso2/e\" xmlns:nsbrlwf=\"http://sample.com/wso2/f/t\" " +
                "foo1=\"bar1\" ns0Kf5j:foo2=\"bar2\" ns4:foo3=\"bar3\" nsbrlwf:foo3=\"newbar3\"/>");
    }
    
    @Test
    public void testUpdateNamespaceAsAttribute() {
        BValue[] returns = BTestUtils.invoke(xmlAttrProgFile, "testUpdateNamespaceAsAttribute");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns3=\"http://wso2.com\"/>");
        
        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(), "<root xmlns=\"http://ballerinalang.org/\" " +
            "xmlns:ns3=\"http://wso2.com\"/>");
    }
    
    @Test
    public void testUpdateAttributeWithQName() {
        BValue[] returns = BTestUtils.invoke(xmlAttrProgFile, "testUpdateAttributeWithQName");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns3=\"http://sample.com/wso2/f\" " +
                "xmlns:ns0=\"http://sample.com/wso2/a1\" ns0:foo1=\"newbar1\" ns3:foo2=\"newbar2\"/>");
    }

    @Test
    public void testUpdateAttributeWithQName_1() {
        BValue[] returns = BTestUtils.invoke(xmlAttrProgFile, "testUpdateAttributeWithQName_1");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns3=\"http://sample.com/wso2/f\" " +
                "xmlns:ns0=\"http://sample.com/wso2/a1\" xmlns:ns5=\"http://sample.com/wso2/a1\" " +
                "ns0:foo1=\"newaddedbar1\" ns3:foo2=\"bar2\"/>");
    }
    
    @Test
    public void testGetAttributeWithString() {
        BValue[] returns = BTestUtils.invoke(xmlAttrProgFile, "testGetAttributeWithString");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "bar1");
        
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "bar2");
        
        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "");
    }
    
    @Test
    public void testGetAttributeWithoutLocalname() {
        BValue[] returns = BTestUtils.invoke(xmlAttrProgFile, "testGetAttributeWithoutLocalname");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "");
    }
    
    @Test
    public void testGetAttributeWithEmptyNamespace() {
        BValue[] returns = BTestUtils.invoke(xmlAttrProgFile, "testGetAttributeWithEmptyNamespace");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "bar1");
    }

    @Test
    public void testGetNamespaceAsAttribute() {
        BValue[] returns = BTestUtils.invoke(xmlAttrProgFile, "testGetNamespaceAsAttribute");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "http://sample.com/wso2/f");
    }

    @Test
    public void testGetAttributeWithQName() {
        BValue[] returns = BTestUtils.invoke(xmlAttrProgFile, "testGetAttributeWithQName");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "bar1");

        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "bar2");

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "");
    }

    @Test
    public void testUsingQNameAsString() {
        BValue[] returns = BTestUtils.invoke(xmlAttrProgFile, "testUsingQNameAsString");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "{http://sample.com/wso2/a1}wso2");

        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "{http://sample.com/wso2/a1}ballerina");
    }

    @Test
    public void testGetAttributesAsMap() {
        BValue[] returns = BTestUtils.invoke(xmlAttrProgFile, "testGetAttributesAsMap");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].stringValue(), "{\"{http://www.w3.org/2000/xmlns/}ns0\":"
                + "\"http://sample.com/wso2/a1\", \"{http://sample.com/wso2/a1}foo1\":\"bar1\", \"foo2\":\"bar2\"}");

        Assert.assertTrue(returns[1] instanceof BMap);
        Assert.assertEquals(returns[1].stringValue(), "{\"{http://sample.com/default/namepsace}ns0\":"
                + "\"http://sample.com/wso2/a1\", \"{http://sample.com/wso2/a1}foo1\":\"bar1\", \"foo2\":\"bar2\"}");

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "bar1");

        Assert.assertTrue(returns[3] instanceof BString);
        Assert.assertEquals(returns[3].stringValue(), "bar1");
    }
    
    @Test
    public void testXMLAttributesToAny() {
        BValue[] returns = BTestUtils.invoke(xmlAttrProgFile, "testXMLAttributesToAny");
        Assert.assertTrue(returns[0] instanceof BXMLAttributes);
        Assert.assertEquals(returns[0].stringValue(), "{\"{http://www.w3.org/2000/xmlns/}ns0\":" +
            "\"http://sample.com/wso2/a1\", \"{http://sample.com/wso2/a1}foo1\":\"bar1\", \"foo2\":\"bar2\"}");
    }
    
    @Test
    public void testRuntimeNamespaceLookup() {
        BValue[] returns = BTestUtils.invoke(xmlAttrProgFile, "testRuntimeNamespaceLookup");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns401=\"http://sample.com/wso2/a1\" xmlns:ns1=" +
            "\"http://sample.com/wso2/b1\" xmlns:ns403=\"http://sample.com/wso2/e3\" xmlns:nsn7xFP=" +
            "\"http://sample.com/wso2/f3\" ns401:foo1=\"bar1\" ns1:foo2=\"bar2\" ns403:foo3=\"bar3\" " +
            "nsn7xFP:foo4=\"bar4\"/>");
    }
    
    @Test
    public void testRuntimeNamespaceLookupPriority() {
        BValue[] returns = BTestUtils.invoke(xmlAttrProgFile, "testRuntimeNamespaceLookupPriority");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:p1=\"http://wso2.com\" " +
            "xmlns:p2=\"http://sample.com/wso2/a1\" xmlns:ns401=\"http://sample.com/wso2/a1\" ns401:foo1=\"bar1\" " +
            "p1:foo2=\"bar2\"/>");
    }

    @Test
    public void testSetAttributes() {
        BValue[] returns = BTestUtils.invoke(xmlAttrProgFile, "testSetAttributes");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:nsRJUck=\"http://wso2.com\" xmlns:nsn7xDi="
                + "\"http://sample.com/wso2/a1\" nsRJUck:foo2=\"bar2\" nsn7xDi:foo3=\"bar3\" foo1=\"bar1\"/>");
    }

    @Test
    public void testGetAttributeFromSingletonSeq() {
        BValue[] returns = BTestUtils.invoke(xmlAttrProgFile, "testGetAttributeFromSingletonSeq");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "bar");
    }
    
    
    @Test
    public void testGetAttributeFromLiteral() {
        BValue[] returns = BTestUtils.invoke(xmlAttrProgFile, "testGetAttributeFromLiteral");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "5");
    }
}
