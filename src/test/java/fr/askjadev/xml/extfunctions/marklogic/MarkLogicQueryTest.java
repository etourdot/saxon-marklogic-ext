/*
 * The MIT License
 *
 * Copyright 2017 EXT-acourt.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.askjadev.xml.extfunctions.marklogic;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.Configuration;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.value.SequenceType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author EXT-acourt
 */
public class MarkLogicQueryTest {
    
    private final List<String> CONNECT;
    private final String CONNECT_ELT;
    
    public MarkLogicQueryTest() {
        super();
        this.CONNECT = new ArrayList();
        this.CONNECT.add(System.getProperty("testServer") == null ? "localhost" : System.getProperty("testServer"));
        this.CONNECT.add(System.getProperty("testPort") == null ? "8004" : System.getProperty("testPort"));
        this.CONNECT.add(System.getProperty("testUser") == null ? "admin" : System.getProperty("testUser"));
        this.CONNECT.add(System.getProperty("testPassword") == null ? "admin" : System.getProperty("testPassword"));
        this.CONNECT.add(System.getProperty("testDatabase") == null ? "Documents" : System.getProperty("testDatabase"));
        this.CONNECT.add(System.getProperty("testAuthentication") == null ? "basic" : System.getProperty("testAuthentication"));
        this.CONNECT_ELT =
            "<?xml version='1.0' encoding='UTF-8'?>" +
            "<marklogic>" +
                "<server>" + this.CONNECT.get(0) + "</server>" +
                "<port>" + this.CONNECT.get(1) + "</port>" +
                "<user>" + this.CONNECT.get(2) + "</user>" +
                "<password>" + this.CONNECT.get(3) + "</password>" +
                "<database>" + this.CONNECT.get(4) + "</database>" +
                "<authentication>" + this.CONNECT.get(5) + "</authentication>" +
            "</marklogic>";
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getFunctionQName method, of class MarkLogicQuery.
     */
    @Test
    public void testGetFunctionQName() {
        MarkLogicQuery instance = new MarkLogicQuery();
        StructuredQName expResult = new StructuredQName("mkl-ext", "fr:askjadev:xml:extfunctions", "marklogic-query");
        StructuredQName result = instance.getFunctionQName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getArgumentTypes method, of class MarkLogicQuery.
     */
    @Test
    public void testGetArgumentTypes() {
        MarkLogicQuery instance = new MarkLogicQuery();
        SequenceType[] expResult = new SequenceType[] { SequenceType.SINGLE_STRING, SequenceType.SINGLE_ITEM, SequenceType.OPTIONAL_STRING, SequenceType.OPTIONAL_STRING, SequenceType.OPTIONAL_STRING, SequenceType.OPTIONAL_STRING, SequenceType.OPTIONAL_STRING };
        SequenceType[] result = instance.getArgumentTypes();
        assertEquals(expResult.length, result.length);
        for (int i=0; i<expResult.length; i++) {
            assertEquals("Entry " + i + " differs from expected: ", expResult[i], result[i]);
        }
    }

    /**
     * Test of getMinimumNumberOfArguments method, of class MarkLogicQuery.
     */
    @Test
    public void testGetMinimumNumberOfArguments() {
        MarkLogicQuery instance = new MarkLogicQuery();
        int expResult = 2;
        int result = instance.getMinimumNumberOfArguments();
        assertEquals(expResult, result);
    }

    /**
     * Test of getMaximumNumberOfArguments method, of class MarkLogicQuery.
     */
    @Test
    public void testGetMaximumNumberOfArguments() {
        MarkLogicQuery instance = new MarkLogicQuery();
        int expResult = 7;
        int result = instance.getMaximumNumberOfArguments();
        assertEquals(expResult, result);
    }

    /**
     * Test of getResultType method, of class MarkLogicQuery.
     */
    @Test
    public void testGetResultType() {
        SequenceType[] sts = null;
        MarkLogicQuery instance = new MarkLogicQuery();
        SequenceType expResult = SequenceType.ANY_SEQUENCE;
        SequenceType result = instance.getResultType(sts);
        assertEquals(expResult, result);
    }

    /**
     * Test of makeCallExpression method, of class MarkLogicQuery.
     */
    @Test
    public void testMakeCallExpression2args() {
        Configuration config = new Configuration();
        config.registerExtensionFunction(new MarkLogicQuery());
        Processor proc = new Processor(config);
        XPathCompiler xpc = proc.newXPathCompiler();
        try {
            xpc.declareNamespace(MarkLogicQuery.EXT_NS_COMMON_PREFIX, MarkLogicQuery.EXT_NAMESPACE_URI);
            QName var = new QName("connect");
            xpc.declareVariable(var);
            XPathSelector xp = xpc.compile(MarkLogicQuery.EXT_NS_COMMON_PREFIX + ":" + MarkLogicQuery.FUNCTION_NAME + "('for $i in 1 to 10 return <test>{$i}</test>',$connect)").load();
            DocumentBuilder builder = proc.newDocumentBuilder();
            XdmNode docConnect = builder.build(new StreamSource(new ByteArrayInputStream(CONNECT_ELT.getBytes("UTF-8"))));
            XdmNode connect = (XdmNode) docConnect.axisIterator(Axis.DESCENDANT_OR_SELF,new QName("marklogic")).next();
            xp.setVariable(var,connect);
            xp.setContextItem(docConnect);
            XdmValue result = xp.evaluate();
            SequenceIterator it = result.getUnderlyingValue().iterate();
            Item item = it.next();
            int count = 1;
            while (item != null) {
                assertEquals(Integer.toString(count++), item.getStringValue());
                item = it.next();
            }
            it.close();
        }
        catch (SaxonApiException | UnsupportedEncodingException | XPathException ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }
    @Test
    public void testMakeCallExpression7args() {
        Configuration config = new Configuration();
        config.registerExtensionFunction(new MarkLogicQuery());
        Processor proc = new Processor(config);
        XPathCompiler xpc = proc.newXPathCompiler();
        try {
            xpc.declareNamespace(MarkLogicQuery.EXT_NS_COMMON_PREFIX, MarkLogicQuery.EXT_NAMESPACE_URI);
            XPathSelector xp = xpc.compile(
                MarkLogicQuery.EXT_NS_COMMON_PREFIX + ":" + MarkLogicQuery.FUNCTION_NAME +
                "('for $i in 1 to 10 return <test>{$i}</test>', " +
                "'" + CONNECT.get(0) + "', " +
                "'" + CONNECT.get(1) + "', " +
                "'" + CONNECT.get(2) + "', " +
                "'" + CONNECT.get(3) + "', " +
                "'" + CONNECT.get(4) + "', " +
                "'" + CONNECT.get(5) + "')")
            .load();
            DocumentBuilder builder = proc.newDocumentBuilder();
            XdmNode docConnect = builder.build(new StreamSource(new ByteArrayInputStream("<document/>".getBytes("UTF-8"))));
            xp.setContextItem(docConnect);
            XdmValue result = xp.evaluate();
            SequenceIterator it = result.getUnderlyingValue().iterate();
            Item item = it.next();
            int count = 1;
            while (item != null) {
                assertEquals(Integer.toString(count++), item.getStringValue());
                item = it.next();
            }
            it.close();
        }
        catch (SaxonApiException | UnsupportedEncodingException | XPathException ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage()); 
        }
    }
    @Test(expected = SaxonApiException.class)
    public void testMakeCallExpression3args() throws SaxonApiException, XPathException {
        Configuration config = new Configuration();
        config.registerExtensionFunction(new MarkLogicQuery());
        Processor proc = new Processor(config);
        XPathCompiler xpc = proc.newXPathCompiler();
        try {
            xpc.declareNamespace(MarkLogicQuery.EXT_NS_COMMON_PREFIX, MarkLogicQuery.EXT_NAMESPACE_URI);
            XPathSelector xp = xpc.compile(
                MarkLogicQuery.EXT_NS_COMMON_PREFIX + ":" + MarkLogicQuery.FUNCTION_NAME +
                "('for $i in 1 to 10 return <test>{$i}</test>', " +
                "'" + CONNECT.get(0) + "', " +
                "'" + CONNECT.get(1) + "')")
            .load();
            DocumentBuilder builder = proc.newDocumentBuilder();
            XdmNode docConnect = builder.build(new StreamSource(new ByteArrayInputStream("<document/>".getBytes("UTF-8"))));
            xp.setContextItem(docConnect);
            XdmValue result = xp.evaluate();
            SequenceIterator it = result.getUnderlyingValue().iterate();
            Item item = it.next();
            int count = 1;
            while (item != null) {
                assertEquals(Integer.toString(count++), item.getStringValue());
                item = it.next();
            }
            it.close();
        }
        catch (SaxonApiException | XPathException ex) {
            throw ex;
        }
        catch (UnsupportedEncodingException ex) {
            // Do nothing, it will never happen.
        }
    }
    @Test(expected = SaxonApiException.class)
    public void testMakeCallExpression4args() throws SaxonApiException, XPathException {
        Configuration config = new Configuration();
        config.registerExtensionFunction(new MarkLogicQuery());
        Processor proc = new Processor(config);
        XPathCompiler xpc = proc.newXPathCompiler();
        try {
            xpc.declareNamespace(MarkLogicQuery.EXT_NS_COMMON_PREFIX, MarkLogicQuery.EXT_NAMESPACE_URI);
            XPathSelector xp = xpc.compile(
                MarkLogicQuery.EXT_NS_COMMON_PREFIX + ":" + MarkLogicQuery.FUNCTION_NAME +
                "('for $i in 1 to 10 return <test>{$i}</test>', " +
                "'" + CONNECT.get(0) + "', " +
                "'" + CONNECT.get(1) + "', " +
                "'" + CONNECT.get(2) + "')")
            .load();
            DocumentBuilder builder = proc.newDocumentBuilder();
            XdmNode docConnect = builder.build(new StreamSource(new ByteArrayInputStream("<document/>".getBytes("UTF-8"))));
            xp.setContextItem(docConnect);
            XdmValue result = xp.evaluate();
            SequenceIterator it = result.getUnderlyingValue().iterate();
            Item item = it.next();
            int count = 1;
            while (item != null) {
                assertEquals(Integer.toString(count++), item.getStringValue());
                item = it.next();
            }
            it.close();
        }
        catch (SaxonApiException | XPathException ex) {
            throw ex;
        }
        catch (UnsupportedEncodingException ex) {
            // Do nothing, it will never happen.
        }
    }
    @Test
    public void testMakeCallExpression5args() {
        Configuration config = new Configuration();
        config.registerExtensionFunction(new MarkLogicQuery());
        Processor proc = new Processor(config);
        XPathCompiler xpc = proc.newXPathCompiler();
        try {
            xpc.declareNamespace(MarkLogicQuery.EXT_NS_COMMON_PREFIX, MarkLogicQuery.EXT_NAMESPACE_URI);
            XPathSelector xp = xpc.compile(
                MarkLogicQuery.EXT_NS_COMMON_PREFIX + ":" + MarkLogicQuery.FUNCTION_NAME +
                "('for $i in 1 to 10 return <test>{$i}</test>', " +
                "'" + CONNECT.get(0) + "', " +
                "'" + CONNECT.get(1) + "', " +
                "'" + CONNECT.get(2) + "', " +
                "'" + CONNECT.get(3) + "')")
            .load();
            DocumentBuilder builder = proc.newDocumentBuilder();
            XdmNode docConnect = builder.build(new StreamSource(new ByteArrayInputStream("<document/>".getBytes("UTF-8"))));
            xp.setContextItem(docConnect);
            XdmValue result = xp.evaluate();
            SequenceIterator it = result.getUnderlyingValue().iterate();
            Item item = it.next();
            int count = 1;
            while (item != null) {
                assertEquals(Integer.toString(count++), item.getStringValue());
                item = it.next();
            }
            it.close();
        }
        catch (SaxonApiException | UnsupportedEncodingException | XPathException ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage()); 
        }
    }
    @Test
    public void testMakeCallExpression6args() {
        Configuration config = new Configuration();
        config.registerExtensionFunction(new MarkLogicQuery());
        Processor proc = new Processor(config);
        XPathCompiler xpc = proc.newXPathCompiler();
        try {
            xpc.declareNamespace(MarkLogicQuery.EXT_NS_COMMON_PREFIX, MarkLogicQuery.EXT_NAMESPACE_URI);
            XPathSelector xp = xpc.compile(
                MarkLogicQuery.EXT_NS_COMMON_PREFIX + ":" + MarkLogicQuery.FUNCTION_NAME +
                "('for $i in 1 to 10 return <test>{$i}</test>', " +
                "'" + CONNECT.get(0) + "', " +
                "'" + CONNECT.get(1) + "', " +
                "'" + CONNECT.get(2) + "', " +
                "'" + CONNECT.get(3) + "', " +
                "'" + CONNECT.get(4) + "')")
            .load();
            DocumentBuilder builder = proc.newDocumentBuilder();
            XdmNode docConnect = builder.build(new StreamSource(new ByteArrayInputStream("<document/>".getBytes("UTF-8"))));
            xp.setContextItem(docConnect);
            XdmValue result = xp.evaluate();
            SequenceIterator it = result.getUnderlyingValue().iterate();
            Item item = it.next();
            int count = 1;
            while (item != null) {
                assertEquals(Integer.toString(count++), item.getStringValue());
                item = it.next();
            }
            it.close();
        }
        catch (SaxonApiException | UnsupportedEncodingException | XPathException ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage()); 
        }
    }
    
}
