/*
*   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.ballerinalang.test.expressions.literals;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.model.values.BXMLSequence;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.xml.namespace.QName;

/**
 * Test Native function in ballerina.model.xml.
 */
public class XMLNativeFunctionTest {

    private CompileResult result;
    private static final String s1 = "<persons><person><name>Jack</name><address>wso2</address></person></persons>";
    private static final String s2 = "<person><name>Jack</name></person>";
    private static String l1;

    @BeforeClass
    public void setup() {
        result = BTestUtils.compile("test-src/expressions/literals/xml/xml-native-functions.bal");
    }

    @Test
    public void testGetString() {
        BValue[] args = {new BXMLItem(s1), new BString("/persons/person/name/text()")};
        BValue[] returns = BTestUtils.invoke(result, "getString", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "Jack";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }
    
    @Test
    public void testGetStringFromSeq() {
        BRefValueArray seq = new BRefValueArray();
        seq.add(0, new BXMLItem(s1));
        BValue[] args = { new BXMLSequence(seq), new BString("/persons/person/name/text()") };
        BValue[] returns = BTestUtils.invoke(result, "getString", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "Jack";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }
    

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testGetNonExistingString() {
        BValue[] args = {new BXMLItem(s1), new BString("/xxx/text()")};
        BValue[] returns = BTestUtils.invoke(result, "getString", args);

        Assert.assertEquals(returns[0], null);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class})
    public void testGetStringFromMalformedXpath() {
        BValue[] args = {new BXMLItem(s1), new BString("$worng#path")};
        BTestUtils.invoke(result, "getString", args);
    }

    @Test
    public void testGetXML() {
        BValue[] args = {new BXMLItem(s1), new BString("/persons/person")};
        BValue[] returns = BTestUtils.invoke(result, "getXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), "<person><name>Jack</name>" +
                "<address>wso2</address></person>");
    }

    @Test
    public void testGetXMLFromSingletonSequence() {
        BRefValueArray seq = new BRefValueArray();
        seq.add(0, new BXMLItem(s1));
        BValue[] args = { new BXMLSequence(seq), new BString("/persons/person") };
        BValue[] returns = BTestUtils.invoke(result, "getXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), "<person><name>Jack</name>" +
                "<address>wso2</address></person>");
    }
    
    @Test(expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = "error: error, message: failed to get element " +
                  "from xml: cannot execute xpath on a xml sequence.*")
    public void testGetXMLFromSequence() {
        BRefValueArray seq = new BRefValueArray();
        seq.add(0, new BXMLItem(s1));
        seq.add(1, new BXMLItem(s2));
        BValue[] args = { new BXMLSequence(seq), new BString("/persons/person") };
        BValue[] returns = BTestUtils.invoke(result, "getXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), "<person><name>Jack</name>" +
                "<address>wso2</address></person>");
    }
    
    @Test
    public void testGetXMLLarge() {
        // Load large xml
        l1 = BTestUtils.readFileAsString("test-src/expressions/literals/xml/message13k.xml");
        BValue[] args = {new BXMLItem(l1),
                new BString("/persons/person[160]")};
        BValue[] returns = BTestUtils.invoke(result, "getXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), "<person>" +
                "<name>Jill</name>" +
                "<address>wso2</address>" +
                "</person>");
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testGetNonExistingXML() {
        BValue[] args = {new BXMLItem(s1), new BString("/xxx")};
        BValue[] returns = BTestUtils.invoke(result, "getXML", args);

        Assert.assertEquals(returns[0], null);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class})
    public void testGetXMLFromMalformedXpath() {
        BValue[] args = {new BXMLItem(s1), new BString("$worng#path")};
        BTestUtils.invoke(result, "getXML", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testGetXMLFromText() {
        BValue[] args = {new BXMLItem(s1), new BString("/persons/person/name/text()")};
        BTestUtils.invoke(result, "getXML", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testGetXMLFromDocumentElement() {
        String d1 = BTestUtils.readFileAsString("test-src/expressions/literals/xml/xmlDocumentSample.xml");
        BValue[] args = {new BXMLItem(d1), new BString("/")};
        BTestUtils.invoke(result, "getXML", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testGetXMLFromAttribute() {
        String a1 = BTestUtils.readFileAsString("test-src/expressions/literals/xml/messageComplex.xml");
        BValue[] args = {new BXMLItem(a1), new BString("/employees/employee/@id")};
        BTestUtils.invoke(result, "getXML", args);
    }

    @Test
    public void testSetString() {
        BValue[] args = {new BXMLItem(s1), new BString("/persons/person/name/text()"), new BString("Peter")};
        BValue[] returns = BTestUtils.invoke(result, "setString", args);

        Assert.assertEquals(returns[0].stringValue(), "<persons><person><name>Peter" +
                "</name><address>wso2</address></person></persons>");
    }

    @Test
    public void testSetStringToSingletonSequence() {
        BRefValueArray seq = new BRefValueArray();
        seq.add(0, new BXMLItem(s1));
        BValue[] args = { new BXMLSequence(seq), new BString("/persons/person/name/text()"), new BString("Peter")};
        BValue[] returns = BTestUtils.invoke(result, "setString", args);

        Assert.assertEquals(returns[0].stringValue(), "<persons><person><name>Peter" +
                "</name><address>wso2</address></person></persons>");
    }
    
    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: error, message: failed to set " +
                    "string in xml: cannot execute xpath on a xml sequence.*")
    public void testSetStringToSequence() {
        BRefValueArray seq = new BRefValueArray();
        seq.add(0, new BXMLItem(s1));
        seq.add(1, new BXMLItem(s2));
        BValue[] args = { new BXMLSequence(seq), new BString("/persons/person/name/text()"), new BString("Peter")};
        BValue[] returns = BTestUtils.invoke(result, "setString", args);

        Assert.assertEquals(returns[0].stringValue(), "<persons><person><name>Peter" +
                "</name><address>wso2</address></person></persons>");
    }
    
    @Test
    public void testSetStringToNonExistingElement() {
        BValue[] args = {new BXMLItem(s1), new BString("/xxx/text()"), new BString("Peter")};
        BValue[] returns = BTestUtils.invoke(result, "setString", args);

        Assert.assertEquals(returns[0].stringValue(), s1);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class})
    public void testSetStringToMalformedXpath() {
        BValue[] args = {new BXMLItem(s1), new BString("$worng#path"), new BString("Peter")};
        BTestUtils.invoke(result, "setString", args);
    }

    @Test
    public void testSetStringToAttribute() {
        String a1 = BTestUtils.readFileAsString("test-src/expressions/literals/xml/messageSimple.xml");
        BValue[] args = {new BXMLItem(a1), new BString("/employee/@id"), new BString("0")};
        BValue[] returns = BTestUtils.invoke(result, "setString", args);

        Assert.assertEquals(returns[0].stringValue(), "<employee id=\"0\">\n" +
                "    <name>Parakum</name>\n" +
                "    <age>32</age>\n" +
                "</employee>");
    }

    @Test
    public void testSetXML() {
        BValue[] args = {new BXMLItem(s2), new BString("/person/name"),
                new BXMLItem("<name><fname>Jack</fname><lname>Peter</lname></name>")};
        BValue[] returns = BTestUtils.invoke(result, "setXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), "<person><name><fname>Jack" +
                "</fname><lname>Peter</lname></name></person>");
    }

    @Test
    public void testSetXMLToNonExistingElement() {
        BValue[] args = {new BXMLItem(s2), new BString("/xxx"),
                new BXMLItem("<name><fname>Jack</fname><lname>Peter</lname></name>")};
        BValue[] returns = BTestUtils.invoke(result, "setXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), s2);
    }


    @Test(expectedExceptions = {BLangRuntimeException.class})
    public void testSetXMLToMalformedXpath() {
        BValue[] args = {new BXMLItem(s2), new BString("$worng#path"),
                new BXMLItem("<name><fname>Jack</fname><lname>Peter</lname></name>")};
        BTestUtils.invoke(result, "setXML", args);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class})
    public void testSetXMLNullValue() {
        BValue[] args = {new BXMLItem(s2), new BString("/person/name"),
                null};
        BTestUtils.invoke(result, "setXML", args);
    }

    @Test
    public void testAddElement() {
        BValue[] args = {new BXMLItem(s2), new BString("/person"), new BXMLItem("<address>wso2</address>")};
        BValue[] returns = BTestUtils.invoke(result, "addElement", args);

        Assert.assertEquals(returns[0].stringValue(), "<person><name>Jack</name>" +
                "<address>wso2</address></person>");
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testAddElementToNonExistingElement() {
        BValue[] args = {new BXMLItem(s2), new BString("/xxx"), new BXMLItem("<address>wso2</address>")};
        BValue[] returns = BTestUtils.invoke(result, "addElement", args);

        Assert.assertEquals(returns[0].stringValue(), s2);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class})
    public void testAddElementToMalformedXpath() {
        BValue[] args = {new BXMLItem(s2), new BString("$worng#path"), new BXMLItem("<address>wso2</address>")};
        BTestUtils.invoke(result, "addElement", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testAddElementToText() {
        BValue[] args = {new BXMLItem(s1), new BString("/persons/person/name/text()"), 
            new BXMLItem("<address>wso2</address>")};
        BTestUtils.invoke(result, "addElement", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testAddElementToDocumentElement() {
        String d1 = BTestUtils.readFileAsString("test-src/expressions/literals/xml/xmlDocumentSample.xml");
        BValue[] args = {new BXMLItem(d1), new BString("/"), new BXMLItem("<address>wso2</address>")};
        BTestUtils.invoke(result, "addElement", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testAddElementToAttribute() {
        String a1 = BTestUtils.readFileAsString("test-src/expressions/literals/xml/messageComplex.xml");
        BValue[] args = {new BXMLItem(a1), new BString("/employees/employee/@id"), 
                new BXMLItem("<address>wso2</address>")};
        BTestUtils.invoke(result, "addElement", args);
    }

    @Test
    public void testAddAttributeWithXPath() {
        BValue[] args = {new BXMLItem(s2), new BString("/person/name"), new BString("id"), new BString("person123")};
        BValue[] returns = BTestUtils.invoke(result, "addAttribute", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), "<person><name id=\"person123\">Jack</name></person>");
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testAddAttributeToNonExistingElement() {
        BValue[] args = {new BXMLItem(s2), new BString("/xxx"), new BString("id"), new BString("person123")};
        BValue[] returns = BTestUtils.invoke(result, "addAttribute", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), s2);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class})
    public void testAddAttributeToMalformedXpath() {
        BValue[] args = {new BXMLItem(s2), new BString("$worng#path"), new BString("id"), new BString("person123")};
        BTestUtils.invoke(result, "addAttribute", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testAddAttributeToText() {
        BValue[] args = {new BXMLItem(s2), new BString("/persons/person/name/text()"), new BString("id"),
                new BString("person123")};
        BTestUtils.invoke(result, "addAttribute", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testAddAttributeToDocumentElement() {
        String d1 = BTestUtils.readFileAsString("test-src/expressions/literals/xml/xmlDocumentSample.xml");
        BValue[] args = {new BXMLItem(d1), new BString("/"), new BString("id"), new BString("person123")};
        BTestUtils.invoke(result, "addAttribute", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testAddAttributeToAttribute() {
        String a1 = BTestUtils.readFileAsString("test-src/expressions/literals/xml/messageComplex.xml");
        BValue[] args = {new BXMLItem(a1), new BString("/employees/employee/@id"), new BString("id"),
                new BString("person123")};
        BTestUtils.invoke(result, "addAttribute", args);
    }

    @Test
    public void testRemove() {
        BValue[] args = {new BXMLItem(s1), new BString("/persons/person/address")};
        BValue[] returns = BTestUtils.invoke(result, "remove", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), "<persons><person><name>Jack" +
                "</name></person></persons>");
    }

    @Test
    public void testRemoveNonExistingElement() {
        BValue[] args = {new BXMLItem(s1), new BString("/xxx")};
        BValue[] returns = BTestUtils.invoke(result, "remove", args);
        Assert.assertEquals(returns[0].stringValue(), s1);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class})
    public void testRemoveFromMalformedXpath() {
        BValue[] args = {new BXMLItem(s1), new BString("$worng#path")};
        BTestUtils.invoke(result, "remove", args);
    }

    @Test(description = "Test xml element string value replacement")
    public void testSetXmlElementText() {
        BValue[] returns = BTestUtils.invoke(result, "xmlSetString1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        OMElement xmlMessage = (OMElement) ((BXMLItem) returns[0]).value();
        String actualDName = xmlMessage.getFirstChildWithName(new QName("doctorName")).getText();
        Assert.assertEquals(actualDName, "DName1", "XML Element text not set properly");
    }

    @Test(description = "Test xml text value replacement")
    public void testSetXmlText() {
        BValue[] returns = BTestUtils.invoke(result, "xmlSetString2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        OMElement xmlMessage = (OMElement) ((BXMLItem) returns[0]).value();
        String actualDName = xmlMessage.getFirstChildWithName(new QName("doctorName")).getText();
        Assert.assertEquals(actualDName, "DName2", "XML Element text not set properly");
    }

    @Test
    public void testIsSingleton() {
        BValue[] returns = BTestUtils.invoke(result, "testIsSingleton");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), false);
        
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), true);
    }

    @Test
    public void testIsSingletonWithMultipleChildren() {
        BValue[] returns = BTestUtils.invoke(result, "testIsSingletonWithMultipleChildren");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), true);

    }
    
    @Test
    public void testIsEmpty() {
        BValue[] returns = BTestUtils.invoke(result, "testIsEmpty");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), false);
    }

    @Test
    public void testIsEmptyWithNoElementTextValue() {
        BValue[] returns = BTestUtils.invoke(result, "testIsEmptyWithNoElementTextValue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), false);
    }

    @Test
    public void testIsEmptyWithMultipleChildren() {
        BValue[] returns = BTestUtils.invoke(result, "testIsEmptyWithMultipleChildren");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), false);
    }
    
    @Test
    public void testGetItemType() {
        BValue[] returns = BTestUtils.invoke(result, "testGetItemType");
        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "element");
        
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertEquals(returns[1].stringValue(), "element");

        Assert.assertSame(returns[2].getClass(), BString.class);
        Assert.assertEquals(returns[2].stringValue(), "");
    }

    @Test
    public void testGetItemTypeForElementWithPrefix() {
        BValue[] returns = BTestUtils.invoke(result, "testGetItemTypeForElementWithPrefix");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "element");
    }

    @Test
    public void testGetItemTypeForElementWithDefaultNamespace() {
        BValue[] returns = BTestUtils.invoke(result, "testGetItemTypeForElementWithDefaultNamespace");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "element");
    }
    
    @Test
    public void testGetElementName() {
        BValue[] returns = BTestUtils.invoke(result, "testGetElementName");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "{http://sample.com/test}name");
    }

    @Test
    public void testGetElementNameWithDefaultNamespace() {
        BValue[] returns = BTestUtils.invoke(result, "testGetElementNameForElementWithDefaultNamespace");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "{http://sample.com/test}name");
    }

     @Test
    public void testGetElementNameWithoutNamespace() {
        BValue[] returns = BTestUtils.invoke(result, "testGetElementNameForElementWithoutNamespace");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "{http://sample.com/test/core}name");
    }
    
    @Test
    public void testGetTextValue() {
        BValue[] returns = BTestUtils.invoke(result, "testGetTextValue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "supun");
    }

    @Test
    public void testGetTextValueDefaultNamespace() {
        BValue[] returns = BTestUtils.invoke(result, "testGetTextValueDefaultNamespace");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "supun");
    }
    
    @Test
    public void testGetElements() {
        BValue[] returns = BTestUtils.invoke(result, "testGetElements");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        
        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        
        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);
    }

    @Test
    public void testGetElementsFromSequence() {
        BValue[] returns = BTestUtils.invoke(result, "testGetElementsFromSequence");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);

        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }
    
    @Test
    public void testGetElementsByName() {
        BValue[] returns = BTestUtils.invoke(result, "testGetElementsByName");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);
        
        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        
        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testGetElementsByNameWithDefaultNamespace() {
        BValue[] returns = BTestUtils.invoke(result, "testGetElementsByNameWithDefaultNamespace");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testGetElementsByNameWithPrefix() {
        BValue[] returns = BTestUtils.invoke(result, "testGetElementsByNameByPrefix");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testGetElementsByNameWithDifferentPrefix() {
        BValue[] returns = BTestUtils.invoke(result, "testGetElementsByNameByDifferentPrefix");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testGetElementsByNameEmptyNamespace() {
        //related issue 3062
        BValue[] returns = BTestUtils.invoke(result, "testGetElementsByNameEmptyNamespace");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testGetElementsByNameWithPrefixForDefaultNamespace() {
        BValue[] returns = BTestUtils.invoke(result, "testGetElementsByNamePrefixForDefaultNamespace");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testGetElementsByNameWithDifferentNamespaces() {
        BValue[] returns = BTestUtils.invoke(result, "testGetElementsByNameDifferentNamespaces");
        Assert.assertEquals(returns.length, 6);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 1);

        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[1]).value().size(), 1);

        // is element seq one is empty?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);

        // is element seq one is singleton?
        Assert.assertSame(returns[3].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[3]).booleanValue(), true);

        // is element seq two is empty?
        Assert.assertSame(returns[4].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[4]).booleanValue(), false);

        // is element seq two is singleton?
        Assert.assertSame(returns[5].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[5]).booleanValue(), true);
    }
    
    @Test
    public void testGetChildren() {
        BValue[] returns = BTestUtils.invoke(result, "testGetChildren");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        
        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        
        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testGetChildrenFromComplexXml() {
        BValue[] returns = BTestUtils.invoke(result, "testGetChildrenFromComplexXml");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }
    
    @Test
    public void testGetNonExistingChildren() {
        BValue[] returns = BTestUtils.invoke(result, "testGetNonExistingChildren");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        
        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), true);
        
        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }
    
    @Test
    public void testSelectChildren() {
        BValue[] returns = BTestUtils.invoke(result, "testSelectChildren");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);
        
        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        
        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testSelectChildrenWithDefaultNamespace() {
        BValue[] returns = BTestUtils.invoke(result, "testSelectChildrenWithDefaultNamespace");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testSelectChildrenPrefixedDefaultNamespace() {
        BValue[] returns = BTestUtils.invoke(result, "testSelectChildrenPrefixedDefaultNamespace");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testSelectChildrenWtihSamePrefix() {
        BValue[] returns = BTestUtils.invoke(result, "testSelectChildrenWithSamePrefix");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testSelectChildrenWtihDifferentPrefix() {
        BValue[] returns = BTestUtils.invoke(result, "testSelectChildrenWithDifferentPrefix");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testSelectChildrenWtihDifferentNamespaces() {
        BValue[] returns = BTestUtils.invoke(result, "testSelectChildrenWithDifferentNamespaces");
        Assert.assertEquals(returns.length, 6);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 1);

        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[1]).value().size(), 1);

        // is children seq one is empty?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);

        // is children seq one is singleton?
        Assert.assertSame(returns[3].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[3]).booleanValue(), true);

        // is children seq two is empty?
        Assert.assertSame(returns[4].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[4]).booleanValue(), false);

        // is children seq two is singleton?
        Assert.assertSame(returns[5].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[5]).booleanValue(), true);
    }
    
    @Test
    public void testConcat() {
        BValue[] returns = BTestUtils.invoke(result, "testConcat");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);
        
        Assert.assertEquals(returns[0].stringValue(), "<ns0:name xmlns:ns0=\"http://sample.com/test\"><fname>supun" +
                "</fname><lname>setunga</lname></ns0:name><ns1:address xmlns:ns1=\"http://sample.com/test\">" +
                "<country>SL</country><city>Colombo</city></ns1:address>");
        
        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        
        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }
    
    @Test
    public void testSetChildren() {
        BValue[] returns = BTestUtils.invoke(result, "testSetChildren");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXML);
        
        Assert.assertEquals(returns[0].stringValue(), "<ns0:name xmlns:ns0=\"http://sample.com/test\"><newFname>" +
                "supun-new</newFname><newMname>thilina-new</newMname><newLname>setunga-new</newLname></ns0:name>");
        
        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        
        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);
        
        // Check children
        Assert.assertTrue(returns[3] instanceof BXML);
        BRefValueArray children = ((BXMLSequence) returns[3]).value();
        Assert.assertEquals(children.size(), 3);
        Assert.assertEquals(children.get(0).stringValue(), "<newFname>supun-new</newFname>");
        Assert.assertEquals(children.get(1).stringValue(), "<newMname>thilina-new</newMname>");
        Assert.assertEquals(children.get(2).stringValue(), "<newLname>setunga-new</newLname>");
    }

    @Test
    public void testSetChildrenWithDefaultNamespace() {
        BValue[] returns = BTestUtils.invoke(result, "testSetChildrenDefaultNamespace");
        Assert.assertEquals(returns.length, 5);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(returns[0].stringValue(), "<name xmlns=\"http://sample.com/test\"><fname>supun</fname>"
                + "<lname>setunga</lname><residency xmlns=\"\" citizen=\"true\">true</residency></name>");

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);

        // Check children
        Assert.assertTrue(returns[3] instanceof BXML);
        BRefValueArray children = ((BXMLSequence) returns[3]).value();
        Assert.assertEquals(children.size(), 3);
        Assert.assertEquals(children.get(0).stringValue(), "<fname xmlns=\"http://sample.com/test\">supun</fname>");
        Assert.assertEquals(children.get(1).stringValue(), "<lname xmlns=\"http://sample.com/test\">setunga</lname>");
        Assert.assertEquals(children.get(2).stringValue(), "<residency citizen=\"true\">true</residency>");

        // Check attribute value
        Assert.assertSame(returns[4].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[4]).stringValue(), "true");
    }

    @Test
    public void testSetChildrenWithDifferentNamespaceForAttribute() {
        BValue[] returns = BTestUtils.invoke(result, "testSetChildrenWithDifferentNamespaceForAttribute");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXML);

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);

        // Check attribute value
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[3]).stringValue(), "true");
    }

    @Test
    public void testSetChildrenWithPrefixedAttribute() {
        BValue[] returns = BTestUtils.invoke(result, "testSetChildrenWithPrefixedAttribute");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(returns[0].stringValue(), "<name xmlns=\"http://sample.com/test\">" +
                "<fname>supun</fname><lname>setunga</lname><residency xmlns=\"\" " + 
                "xmlns:pre=\"http://sample.com/test/code\" pre:citizen=\"true\">true</residency></name>");

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);

        // Check attribute value
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[3]).stringValue(), "true");
    }

    @Test
    public void testSetChildrenSameNamespace() {
        BValue[] returns = BTestUtils.invoke(result, "testSetChildrenWithSameNamespace");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(returns[0].stringValue(), "<ns0:name xmlns:ns0=\"http://sample.com/test\">" +
                "<ns0:fname>supun</ns0:fname><ns0:lname>setunga</ns0:lname>" +
                "<ns0:residency ns0:citizen=\"yes\">true</ns0:residency></ns0:name>");

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);

        // Check attribute value
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[3]).stringValue(), "yes");
    }

    @Test
    public void testSetChildrenDifferentNamespace() {
        BValue[] returns = BTestUtils.invoke(result, "testSetChildrenWithDifferentNamespace");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(returns[0].stringValue(), "<ns0:name xmlns:ns0=\"http://sample.com/test\">" +
                "<ns0:fname>supun</ns0:fname><ns0:lname>setunga</ns0:lname>" +
                "<ns0:residency xmlns:ns0=\"http://sample.com/test/code\" " +
                "ns0:citizen=\"yes\">true</ns0:residency></ns0:name>");

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);

        // Check attribute value
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[3]).stringValue(), "yes");
    }

    @Test
    public void testSetChildrenDiffNamespaceWithoutPrefix() {
        //related issue 3074
        BValue[] returns = BTestUtils.invoke(result, "testSetChildrenWithDiffNamespaceWithoutPrefix");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(returns[0].stringValue(), "<ns0:name xmlns:ns0=\"http://sample.com/test\" " +
                "xmlns=\"http://sample.com/test/code\"><ns0:fname>supun</ns0:fname><ns0:lname>setunga</ns0:lname>" +
                "<residency xmlns:citizen=\"yes\">true</residency></ns0:name>");

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);

        // Check attribute value
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[3]).stringValue(), "yes");
    }

    @Test
    public void testSetChildrenDiffAttribute() {
        BValue[] returns = BTestUtils.invoke(result, "testSetChildrenWithAttributeDiffNamespace");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(returns[0].stringValue(), "<ns0:name xmlns:ns0=\"http://sample.com/test\" " +
                "xmlns:pre=\"http://sample.com/test/code\">" +
                "<ns0:fname>supun</ns0:fname><ns0:lname>setunga</ns0:lname>" +
                "<ns0:residency pre:citizen=\"yes\">true</ns0:residency></ns0:name>");

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);

        // Check attribute value
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[3]).stringValue(), "yes");
    }

    @Test
    public void testSetChildrenDiffElement() {
        BValue[] returns = BTestUtils.invoke(result, "testSetChildrenWithElementDiffNamespace");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(returns[0].stringValue(), "<ns0:name xmlns:ns0=\"http://sample.com/test\" " +
                "xmlns:pre=\"http://sample.com/test/code\"><ns0:fname>supun</ns0:fname><ns0:lname>setunga</ns0:lname>" +
                "<pre:residency ns0:citizen=\"yes\">true</pre:residency></ns0:name>");

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);

        // Check attribute value
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[3]).stringValue(), "yes");
    }

    @Test
    public void testCopy() {
        BValue[] returns = BTestUtils.invoke(result, "testCopy");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXMLItem);
        
        Assert.assertEquals(returns[0].stringValue(), "<ns0:name xmlns:ns0=\"http://sample.com/test\"><newFname>" +
                "supun-new</newFname><newMname>thilina-new</newMname><newLname>setunga-new</newLname></ns0:name>");
        
        // Check children of the copied xml
        Assert.assertTrue(returns[3] instanceof BXML);
        BRefValueArray children = ((BXMLSequence) ((BXML) returns[0]).children()).value();
        Assert.assertEquals(children.size(), 3);
        Assert.assertEquals(children.get(0).stringValue(), "<newFname>supun-new</newFname>");
        Assert.assertEquals(children.get(1).stringValue(), "<newMname>thilina-new</newMname>");
        Assert.assertEquals(children.get(2).stringValue(), "<newLname>setunga-new</newLname>");
        
        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        
        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);
        
        // Check children of the original xml
        Assert.assertTrue(returns[3] instanceof BXML);
        BRefValueArray originalChildren = ((BXMLSequence) returns[3]).value();
        Assert.assertEquals(originalChildren.size(), 2);
        Assert.assertEquals(originalChildren.get(0).stringValue(), "<fname>supun</fname>");
        Assert.assertEquals(originalChildren.get(1).stringValue(), "<lname>setunga</lname>");
    }
    
    @Test
    public void testToString() {
        BValue[] returns = BTestUtils.invoke(result, "testToString");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BString);
        
        Assert.assertEquals(returns[0].stringValue(), "<!-- comment about the book--><bookName>Book1</bookName>" +
                "<bookId>001</bookId><bookAuthor>Author01</bookAuthor><?word document=\"book.doc\" ?>");
    }
    
    @Test
    public void testStrip() {
        BValue[] returns = BTestUtils.invoke(result, "testStrip");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertTrue(returns[1] instanceof BXML);
        
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 5);
        Assert.assertEquals(returns[0].stringValue(), "<!-- comment about the book-->     <bookId>001</bookId>" +
                "<?word document=\"book.doc\" ?>");
        
        Assert.assertEquals(((BXMLSequence) returns[1]).value().size(), 3);
        Assert.assertEquals(returns[1].stringValue(), "<!-- comment about the book--><bookId>001</bookId>" +
                "<?word document=\"book.doc\" ?>");
    }
    
    @Test
    public void testStripSingleton() {
        BValue[] returns = BTestUtils.invoke(result, "testStripSingleton");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertTrue(returns[1] instanceof BXML);
        
        Assert.assertEquals(returns[0].stringValue(), "<bookId>001</bookId>");
        Assert.assertEquals(returns[1].stringValue(), "<bookId>001</bookId>");
    }
    
    @Test
    public void testStripEmptySingleton() {
        BValue[] returns = BTestUtils.invoke(result, "testStripEmptySingleton");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "");
        Assert.assertEquals(returns[1].stringValue(), "");
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);
    }
    
    @Test
    public void testSlice() {
        BValue[] returns = BTestUtils.invoke(result, "testSlice");
        Assert.assertTrue(returns[0] instanceof BXML);
        
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 3);
        Assert.assertEquals(returns[0].stringValue(), "<bookName>Book1</bookName><bookId>001</bookId><bookAuthor>" +
                "Author01</bookAuthor>");
    }
    
    @Test
    public void testSliceAll() {
        BValue[] returns = BTestUtils.invoke(result, "testSliceAll");
        Assert.assertTrue(returns[0] instanceof BXML);
        
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 5);
        Assert.assertEquals(returns[0].stringValue(), "<!-- comment about the book--><bookName>Book1</bookName>" +
                "<bookId>001</bookId><bookAuthor>Author01</bookAuthor><?word document=\"book.doc\" ?>");
    }
    
    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: error, message: failed to slice xml: " +
                    "invalid indices: 4 < 1.*")
    public void testSliceInvalidIndex() {
        BTestUtils.invoke(result, "testSliceInvalidIndex");
    }
    
    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: error, message: failed to slice xml: " +
                    "index out of range: \\[4,10\\].*")
    public void testSliceOutOfRangeIndex() {
        BTestUtils.invoke(result, "testSliceOutOfRangeIndex");
    }
    
    @Test
    public void testSliceSingleton() {
        BValue[] returns = BTestUtils.invoke(result, "testSliceSingleton");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<bookName>Book1</bookName>");
    }
    
    @Test
    public void testXPathOnCopiedXML() {
        BValue[] returns = BTestUtils.invoke(result, "testXPathOnCopiedXML");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root><bookId>001</bookId><bookAuthor>Author01</bookAuthor>" +
                "</root>");
        Assert.assertEquals(returns[1].stringValue(), "<root><bookName>Book1</bookName><bookId>001</bookId></root>");
    }
    
    @Test
    public void testSeqCopy() {
        BValue[] returns = BTestUtils.invoke(result, "testSeqCopy");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<!-- comment about the book--><bookName>Book1</bookName>" +
                "<bookId>Updated Book ID</bookId><bookAuthor>Author01</bookAuthor><?word document=\"book.doc\" ?>");
        Assert.assertEquals(returns[1].stringValue(), "<!-- comment about the book--><bookName>Book1</bookName>" +
                "<bookId>001</bookId><bookAuthor>Author01</bookAuthor><?word document=\"book.doc\" ?>");
    }

    @Test
    public void testSetChildrenToElemntInDefaultNameSpace() {
        BValue[] returns = BTestUtils.invoke(result, "testSetChildrenToElemntInDefaultNameSpace");
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(returns[0].stringValue(),
                "<name xmlns=\"http://sample.com/test\"><newFname xmlns=\"\">supun-new</newFname></name>");
    }

    @Test
    public void testToJsonForValue() {
        BValue[] returns = BTestUtils.invoke(result, "testToJsonForValue");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "value");
    }

    @Test
    public void testToJsonForEmptyValue() {
        BValue[] returns = BTestUtils.invoke(result, "testToJsonForEmptyValue");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "");
    }

    @Test
    public void testToJsonForComment() {
        BValue[] returns = BTestUtils.invoke(result, "testToJsonForComment");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{}");
    }

    @Test
    public void testToJsonForPI() {
        BValue[] returns = BTestUtils.invoke(result, "testToJsonForPI");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{}");
    }

    @Test
    public void testToJsonForSingleElement() {
        String xmlStr = "<key>value</key>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BTestUtils.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"key\":\"value\"}");
    }

    @Test
    public void testToJsonForEmptySingleElement() {
        String xmlStr = "<key/>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BTestUtils.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"key\":\"\"}");
    }

    @Test
    public void testToJsonForSimpleXML() {
        String xmlStr = "<person><name>Jack</name><age>40</age></person>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BTestUtils.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"person\":{\"name\":\"Jack\",\"age\":\"40\"}}");
    }

    @Test
    public void testToJsonForXMLWithTwoLevels() {
        String xmlStr = "<persons><person><name>Jack</name><address>wso2</address></person></persons>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BTestUtils.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "{\"persons\":{\"person\":{\"name\":\"Jack\",\"address\":\"wso2\"}}}");
    }

    @Test
    public void testToJsonForXMLWithThreeLevels() {
        String xmlStr = "<persons><person><test><name>Jack</name><address>wso2</address></test></person></persons>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BTestUtils.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "{\"persons\":{\"person\":{\"test\":{\"name\":\"Jack\",\"address\":\"wso2\"}}}}");
    }

    @Test
    public void testToJsonXMLWithSingleElementAndAttributes() {
        String xmlStr = "<name test=\"5\">Jack</name>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BTestUtils.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"name\":{\"@test\":\"5\",\"#text\":\"Jack\"}}");
    }

    @Test
    public void testToJsonXMLWithSingleElementAttributesNamespace() {
        String xmlStr = "<ns0:name test=\"5\" xmlns:ns0=\"http://sample0.com/test\">Jack</ns0:name>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BTestUtils.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"ns0:name\":{\"@xmlns:ns0\":\"http://sample0.com/test\","
                + "\"@test\":\"5\",\"#text\":\"Jack\"}}");
    }

    @Test
    public void testToJsonXMLWithSingleEmptyElementAndAttributes() {
        String xmlStr = "<ns0:name test=\"5\" xmlns:ns0=\"http://sample0.com/test\"></ns0:name>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BTestUtils.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"ns0:name\":{\"@xmlns:ns0\":\"http://sample0.com/test\","
                + "\"@test\":\"5\"}}");
    }

    @Test
    public void testToJsonWithComplexObject() {
        String xmlStr = "<bookStore><storeName>foo</storeName><postalCode>94</postalCode><isOpen>true</isOpen><address>"
                + "<street>foo</street><city>94</city><country>true</country></address><codes><item>4</item>"
                + "<item>8</item><item>9</item></codes></bookStore>\n";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BTestUtils.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"bookStore\":{\"storeName\":\"foo\",\"postalCode\":\"94\","
                + "\"isOpen\":\"true\",\"address\":{\"street\":\"foo\",\"city\":\"94\",\"country\":\"true\"},"
                + "\"codes\":{\"item\":[\"4\",\"8\",\"9\"]}}}");
    }

    @Test
    public void testToJsonWithXMLInMiddle() {
        String xmlStr = "<person><name>Jack</name><age>40</age><!-- some comment --></person>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BTestUtils.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"person\":{\"name\":\"Jack\",\"age\":\"40\"}}");
    }

    @Test
    public void testToJsonWithSimpleXMLAndAttributes() {
        String xmlStr = "<person id = \"5\"><name>Jack</name><age>40</age></person>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BTestUtils.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"person\":{\"@id\":\"5\",\"name\":\"Jack\",\"age\":\"40\"}}");
    }

    @Test
    public void testToJsonWithMultipleAttributes() {
        String xmlStr = "<person id = \"5\"><name cat = \"A\">Jack</name><age>40</age></person>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BTestUtils.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "{\"person\":{\"@id\":\"5\",\"age\":\"40\",\"name\":{\"@cat\":\"A\",\"#text\":\"Jack\"}}}");
    }

    @Test
    public void testToJsonWithComplexObjectAttributes() {
        String xmlStr = "<bookStore status = \"online\"><storeName>foo</storeName><postalCode>94</postalCode>"
                + "<isOpen>true</isOpen><address><street>foo</street><city code = \"A\">94</city><country>true"
                + "</country></address><codes quality=\"b\"><item>4</item><item>8</item><item>9</item></codes>"
                + "</bookStore>\n";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BTestUtils.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "{\"bookStore\":{\"@status\":\"online\",\"storeName\":\"foo\",\"postalCode\":\"94\","
                        + "\"isOpen\":\"true\",\"address\":{\"street\":\"foo\",\"country\":\"true\","
                        + "\"city\":{\"@code\":\"A\",\"#text\":\"94\"}},\"codes\":{\"@quality\":\"b\","
                        + "\"item\":[\"4\",\"8\",\"9\"]}}}");
    }

    @Test
    public void testToJsonWithComplexObjectWithMultipleAttributes() {
        String xmlStr = "<bookStore status = \"online\" id = \"5\"><storeName>foo</storeName><postalCode>94"
                + "</postalCode><isOpen>true</isOpen><address><street>foo</street>"
                + "<city code = \"A\" reg = \"C\">94</city><country>true</country></address>"
                + "<codes quality=\"b\" type = \"0\"><item>4</item><item>8</item><item>9</item></codes></bookStore>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BTestUtils.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"bookStore\":{\"@status\":\"online\",\"@id\":\"5\","
                + "\"storeName\":\"foo\",\"postalCode\":\"94\",\"isOpen\":\"true\",\"address\":{\"street\":\"foo\","
                + "\"country\":\"true\",\"city\":{\"@code\":\"A\",\"@reg\":\"C\",\"#text\":\"94\"}},"
                + "\"codes\":{\"@quality\":\"b\",\"@type\":\"0\",\"item\":[\"4\",\"8\",\"9\"]}}}");
    }

    @Test
    public void testToJsonWithDifferentAttributeTag() {
        String xmlStr = "<bookStore status = \"online\" id = \"5\"><storeName>foo</storeName><postalCode>94"
                + "</postalCode><isOpen>true</isOpen><address><street>foo</street>"
                + "<city code = \"A\" reg = \"C\">94</city><country>true</country></address>"
                + "<codes quality=\"b\" type = \"0\"><item>4</item><item>8</item><item>9</item></codes></bookStore>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BTestUtils.invoke(result, "testToJSONWithOptions", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"bookStore\":{\"#status\":\"online\",\"#id\":\"5\","
                + "\"storeName\":\"foo\",\"postalCode\":\"94\",\"isOpen\":\"true\",\"address\":{\"street\":\"foo\","
                + "\"country\":\"true\",\"city\":{\"#code\":\"A\",\"#reg\":\"C\",\"#text\":\"94\"}},"
                + "\"codes\":{\"#quality\":\"b\",\"#type\":\"0\",\"item\":[\"4\",\"8\",\"9\"]}}}");
    }

    @Test
    public void testToJsonWithMultipleAttributesNamespaces() {
        String xmlStr = "<ns0:bookStore status=\"online\" xmlns:ns0=\"http://sample0.com/test\" "
                + "xmlns:ns1=\"http://sample1.com/test\"><ns0:storeName>foo</ns0:storeName>"
                + "<ns3:postalCode xmlns:ns3=\"http://sample3.com/test\">94</ns3:postalCode>"
                + "<ns0:isOpen>true</ns0:isOpen><ns2:address xmlns:ns2=\"http://sample2.com/test\">"
                + "<ns2:street>foo</ns2:street><ns2:city>111</ns2:city><ns2:country>true</ns2:country>"
                + "</ns2:address><ns4:codes xmlns:ns4=\"http://sample4.com/test\"><ns4:item>4</ns4:item><ns4:item>8"
                + "</ns4:item><ns4:item>9</ns4:item></ns4:codes></ns0:bookStore>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BTestUtils.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"ns0:bookStore\":{\"@xmlns:ns0\":\"http://sample0.com/test\","
                + "\"@xmlns:ns1\":\"http://sample1.com/test\",\"@status\":\"online\",\"ns0:storeName\":\"foo\","
                + "\"ns0:isOpen\":\"true\",\"ns3:postalCode\":{\"@xmlns:ns3\":\"http://sample3.com/test\","
                + "\"#text\":\"94\"},\"ns2:address\":{\"@xmlns:ns2\":\"http://sample2.com/test\","
                + "\"ns2:street\":\"foo\",\"ns2:city\":\"111\",\"ns2:country\":\"true\"},"
                + "\"ns4:codes\":{\"@xmlns:ns4\":\"http://sample4.com/test\",\"ns4:item\":[\"4\",\"8\",\"9\"]}}}");
    }

    @Test
    public void testToJsonWithMultipleAttributesNamespacesWithOptions() {
        String xmlStr = "<ns0:bookStore status=\"online\" xmlns:ns0=\"http://sample0.com/test\" "
                + "xmlns:ns1=\"http://sample1.com/test\"><ns0:storeName>foo</ns0:storeName>"
                + "<ns3:postalCode xmlns:ns3=\"http://sample3.com/test\">94</ns3:postalCode>"
                + "<ns0:isOpen>true</ns0:isOpen><ns2:address xmlns:ns2=\"http://sample2.com/test\">"
                + "<ns2:street>foo</ns2:street><ns2:city>111</ns2:city><ns2:country>true</ns2:country>"
                + "</ns2:address><ns4:codes xmlns:ns4=\"http://sample4.com/test\"><ns4:item>4</ns4:item><ns4:item>8"
                + "</ns4:item><ns4:item>9</ns4:item></ns4:codes></ns0:bookStore>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BTestUtils.invoke(result, "testToJSONWithoutNamespace", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"bookStore\":{\"@status\":\"online\","
                + "\"storeName\":\"foo\",\"postalCode\":\"94\",\"isOpen\":\"true\",\"address\":{\"street\":\"foo\","
                + "\"city\":\"111\",\"country\":\"true\"},\"codes\":{\"item\":[\"4\",\"8\",\"9\"]}}}");
    }

    @Test
    public void testToJsonWithObjectArray() {
        String xmlStr = "<books><item><bookName>book1</bookName><bookId>101</bookId></item><item>"
                + "<bookName>book2</bookName><bookId>102</bookId></item><item><bookName>book3</bookName>"
                + "<bookId>103</bookId></item></books>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BTestUtils.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "{\"books\":{\"item\":[{\"bookName\":\"book1\",\"bookId\":\"101\"},{\"bookName\":\"book2\","
                        + "\"bookId\":\"102\"},{\"bookName\":\"book3\",\"bookId\":\"103\"}]}}");
    }

    @Test
    public void testToJsonWithChildElementsWithSameKey() {
        String xmlStr = "<books><item><item><bookName>book1</bookName><bookId>101</bookId></item></item><item><item>"
                + "<bookName>book2</bookName><bookId>102</bookId></item></item><item><item><bookName>book3</bookName>"
                + "<bookId>103</bookId></item></item></books>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BTestUtils.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "{\"books\":{\"item\":[{\"item\":{\"bookName\":\"book1\",\"bookId\":\"101\"}},{\"item\":"
                        + "{\"bookName\":\"book2\",\"bookId\":\"102\"}},{\"item\":{\"bookName\":\"book3\","
                        + "\"bookId\":\"103\"}}]}}");
    }

    @Test
    public void testToJsonWithArray() {
        String xmlStr = "<books><item>book1</item><item>book2</item><item>book3</item></books>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BTestUtils.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"books\":{\"item\":[\"book1\",\"book2\",\"book3\"]}}");
    }

    @Test
    public void testToJSONWithSequenceDistinctKeys() {
        BValue[] returns = BTestUtils.invoke(result, "testToJSONWithSequenceDistinctKeys");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"key1\":\"value1\",\"key2\":\"value2\"}");
    }

    @Test
    public void testToJSONWithSequenceSimilarKeys() {
        BValue[] returns = BTestUtils.invoke(result, "testToJSONWithSequenceSimilarKeys");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"key\":[\"value1\",\"value2\",\"value3\"]}");
    }

    @Test
    public void testToJSONWithSequenceWithValueArray() {
        BValue[] returns = BTestUtils.invoke(result, "testToJSONWithSequenceWithValueArray");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "[\"a\",\"b\",\"c\"]");
    }

    @Test
    public void testToJSONWithSequenceWithMultipleElements() {
        BValue[] returns = BTestUtils.invoke(result, "testToJSONWithSequenceWithMultipleElements");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"person\":{\"name\":\"Jack\",\"age\":\"40\"},"
                + "\"metadata\":\"5\"}");
    }

    @Test
    public void testToJSONWithSequenceWithElementAndText() {
        BValue[] returns = BTestUtils.invoke(result, "testToJSONWithSequenceWithElementAndText");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "[\"a\",\"b\",{\"key\":\"value3\"}]");
    }

    @Test
    public void testToJSONWithSequenceWithElementAndTextArray() {
        BValue[] returns = BTestUtils.invoke(result, "testToJSONWithSequenceWithElementAndTextArray");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "[\"a\",\"b\",{\"key\":[\"value3\",\"value4\",\"value4\"]}]");
    }

    @Test
    public void testToJSONWithSequenceWithDifferentElements() {
        BValue[] returns = BTestUtils.invoke(result, "testToJSONWithSequenceWithDifferentElements");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "[\"a\",\"b\",{\"key\":[\"value3\",\"value4\",\"value4\"],"
                + "\"bookName\":\"Book1\",\"bookId\":[\"001\",\"001\"]}]");
    }

    @Test
    public void testToJSONWithSequenceWithDifferentComplexElements() {
        BValue[] returns = BTestUtils.invoke(result, "testToJSONWithSequenceWithDifferentComplexElements");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"bookStore\":{\"@status\":\"online\",\"storeName\":\"foo\","
                + "\"postalCode\":\"94\",\"isOpen\":\"true\",\"address\":{\"street\":\"foo\",\"city\":\"94\","
                + "\"country\":\"true\"},\"codes\":{\"item\":[\"4\",\"8\",\"9\"]}},\"metaInfo\":\"some info\"}");
    }

    @Test
    public void testToJSONWithAttributeNamespacesAndPreserveNamespace() {
        String xmlStr = "<ns0:bookStore xmlns:ns0=\"http://sample0.com/test\" status=\"online\" ns0:id = \"10\">"
                + "<ns0:storeName>foo</ns0:storeName><ns0:isOpen>true</ns0:isOpen>"
                + "<ns2:address xmlns:ns2=\"http://sample2.com/test\" status=\"online\" ns0:id = \"10\" "
                + "ns2:code= \"test\">srilanka</ns2:address></ns0:bookStore>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BTestUtils.invoke(result, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"ns0:bookStore\":{\"@xmlns:ns0\":"
                + "\"http://sample0.com/test\",\"@status\":\"online\",\"@ns0:id\":\"10\",\"ns0:storeName\":\"foo\","
                + "\"ns0:isOpen\":\"true\",\"ns2:address\":{\"@xmlns:ns2\":\"http://sample2.com/test\","
                + "\"@status\":\"online\",\"@ns0:id\":\"10\",\"@ns2:code\":\"test\",\"#text\":\"srilanka\"}}}");
    }

    @Test
    public void testToJSONWithAttributeNamespacesAndNoPreserveNamespace() {
        String xmlStr = "<ns0:bookStore xmlns:ns0=\"http://sample0.com/test\" status=\"online\" "
                + "ns0:id = \"10\"><ns0:storeName>foo</ns0:storeName><ns0:isOpen>true</ns0:isOpen><ns2:address "
                + "xmlns:ns2=\"http://sample2.com/test\" status=\"online\" ns0:id = \"10\" ns2:code= \"test\">"
                + "srilanka</ns2:address></ns0:bookStore>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BTestUtils.invoke(result, "testToJSONWithoutNamespace", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"bookStore\":{\"@status\":\"online\",\"@id\":\"10\","
                + "\"storeName\":\"foo\",\"isOpen\":\"true\",\"address\":{\"@status\":\"online\",\"@id\":\"10\","
                + "\"@code\":\"test\",\"#text\":\"srilanka\"}}}");
    }

    @Test
    public void testSelectChildrenWithEmptyNs() {
        BValue[] returns = BTestUtils.invoke(result, "testSelectChildrenWithEmptyNs");
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        Assert.assertEquals(returns[0].stringValue(), "<fname>supun</fname><fname>thilina</fname>");
    }

    @Test
    public void testSelectElementsWithEmptyNs() {
        BValue[] returns = BTestUtils.invoke(result, "testSelectElementsWithEmptyNs");
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        Assert.assertEquals(returns[0].stringValue(), "<fname>supun</fname><fname>thilina</fname>");
    }

    @Test
    public void testSelectDescendants() {
        BValue[] returns = BTestUtils.invoke(result, "testSelectDescendants");
        Assert.assertTrue(returns[0] instanceof BXML);
        BXMLSequence seq = (BXMLSequence) returns[0];
        Assert.assertEquals(seq.value().size(), 2);

        Assert.assertEquals(seq.stringValue(), "<name xmlns=\"http://ballerinalang.org/\" "
                + "xmlns:ns0=\"http://ballerinalang.org/aaa\"><name>Supun</name><lname>Setunga</lname></name>"
                + "<name xmlns=\"http://ballerinalang.org/\" xmlns:ns0=\"http://ballerinalang.org/aaa\">John</name>");
    }

    @Test
    public void testSelectDescendantsWithEmptyNs() {
        BValue[] returns = BTestUtils.invoke(result, "testSelectDescendantsWithEmptyNs");
        Assert.assertTrue(returns[0] instanceof BXML);
        BXMLSequence seq = (BXMLSequence) returns[0];
        Assert.assertEquals(seq.value().size(), 2);

        Assert.assertEquals(seq.stringValue(), "<name xmlns:ns0=\"http://ballerinalang.org/aaa\"><name>Supun</name>"
                + "<lname>Setunga</lname></name><name xmlns:ns0=\"http://ballerinalang.org/aaa\">John</name>");
    }

    @Test
    public void testSelectDescendantsFromSeq() {
        BValue[] returns = BTestUtils.invoke(result, "testSelectDescendantsFromSeq");
        Assert.assertTrue(returns[0] instanceof BXML);
        BXMLSequence seq = (BXMLSequence) returns[0];
        Assert.assertEquals(seq.value().size(), 3);

        Assert.assertEquals(seq.stringValue(), "<name xmlns=\"http://ballerinalang.org/\" "
                + "xmlns:ns0=\"http://ballerinalang.org/aaa\"><name>Supun</name><lname>Setunga</lname></name>"
                + "<name xmlns=\"http://ballerinalang.org/\" xmlns:ns0=\"http://ballerinalang.org/aaa\">John</name>"
                + "<name xmlns=\"http://ballerinalang.org/\" xmlns:ns0=\"http://ballerinalang.org/aaa\">Doe</name>");
    }

    @Test(expectedExceptions = { BLangRuntimeException.class }, 
            expectedExceptionsMessageRegExp = "error: error, message: failed to add attribute " +
            "'a:text'. prefix 'a' is already bound to namespace 'yyy'.*")
    public void testUpdateAttributeWithDifferentUri() {
        BValue[] returns = BTestUtils.invoke(result, "testUpdateAttributeWithDifferentUri");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<name xmlns:a=\"yyy\" a:text=\"hello\"/>");
    }
}
