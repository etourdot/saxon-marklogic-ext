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

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.admin.ExtensionLibrariesManager;
import com.marklogic.client.admin.ExtensionLibraryDescriptor;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.InputStreamHandle;
import net.sf.saxon.Configuration;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.s9api.*;
import net.sf.saxon.trans.XPathException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertEquals;

/**
 * Query invoke test
 * NB: user need to be rest-admin in MarkLogic
 *
 * @author etourdot
 */
public class MarkLogicQueryInvokeTest {

    DatabaseClient client;
    ExtensionLibrariesManager librariesManager;
    String server;
    String port;
    String user;
    String password;

    @Before
    public void setup() {
        server = System.getProperty("testServer") == null ? "localhost" : System.getProperty("testServer");
        port = System.getProperty("testPort") == null ? "8004" : System.getProperty("testPort");
        user = System.getProperty("testUser") == null ? "admin" : System.getProperty("testUser");
        password = System.getProperty("testPassword") == null ? "admin" : System.getProperty("testPassword");
        ExtensionLibraryDescriptor moduleDescriptor = new ExtensionLibraryDescriptor();
        moduleDescriptor.setPath("/ext/test/evaltest.xqy");

        client = DatabaseClientFactory.newClient(server, Integer.parseInt(port),
                new DatabaseClientFactory.BasicAuthContext(user, password));
        librariesManager = client.newServerConfigManager().newExtensionLibrariesManager();
        InputStreamHandle xquery = new InputStreamHandle(
                this.getClass().getClassLoader().getResourceAsStream("evaltest.xqy"));
        xquery.setFormat(Format.TEXT);
        librariesManager.write(moduleDescriptor, xquery);
    }

    @After
    public void tearDown() {
        if (client != null) {
            librariesManager.delete("/ext/test/evaltest.xqy");
            client.release();
        }
    }

    @Test
    public void testInvokeModule() throws Exception {
        Configuration config = new Configuration();
        config.registerExtensionFunction(new MarkLogicQueryInvoke());
        Processor proc = new Processor(config);
        XPathCompiler xpc = proc.newXPathCompiler();
        try {
            xpc.declareNamespace(MarkLogicQueryInvoke.EXT_NS_COMMON_PREFIX, MarkLogicQueryInvoke.EXT_NAMESPACE_URI);
            XPathSelector xp = xpc.compile(
                    MarkLogicQueryInvoke.EXT_NS_COMMON_PREFIX + ":" + MarkLogicQueryInvoke.FUNCTION_NAME +
                            "('/ext/test/evaltest.xqy', '"+server+"', '"+port+"','"+user+"','"+password+"')")
                    .load();
            DocumentBuilder builder = proc.newDocumentBuilder();
            XdmNode docConnect = builder.build(new StreamSource(new ByteArrayInputStream("<document/>".getBytes("UTF-8"))));
            xp.setContextItem(docConnect);
            XdmValue result = xp.evaluate();
            SequenceIterator it = result.getUnderlyingValue().iterate();
            Item item = it.next();
            assertEquals("test", item.getStringValue());
            it.close();
        }
        catch (SaxonApiException | XPathException ex) {
            throw ex;
        }
        catch (UnsupportedEncodingException ex) {
            // Do nothing, it will never happen.
        }
    }

}
