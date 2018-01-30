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
import com.marklogic.client.eval.EvalResultIterator;
import com.marklogic.client.io.BytesHandle;
import java.io.ByteArrayInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;

/**
 * Utility class MarkLogicSequenceIterator / Query result iteration
 * @author AxelC
 */
public class MarkLogicSequenceIterator implements SequenceIterator, AutoCloseable {

    private final EvalResultIterator result;
    private final DocumentBuilder builder;
    private final DatabaseClient session;
    private Integer resultCount;
    private boolean closed = false;

    public MarkLogicSequenceIterator(EvalResultIterator result, DocumentBuilder builder, DatabaseClient session) {
        super();
        this.result = result;
        this.builder = builder;
        this.session = session;
        this.resultCount = 0;
    }

    @Override
    public Item next() throws XPathException {
        try {
            if (result.hasNext()) {
                resultCount++;
                StreamSource source = new StreamSource(new ByteArrayInputStream(result.next().get(new BytesHandle()).toBuffer()));
                XdmNode node = builder.build(source);
                // Logger.getLogger(MarkLogicQuery.class.getName()).log(Level.INFO, node.toString());
                return node.getUnderlyingNode();
            } else {
                close();
                return null;
            }
        } catch (SaxonApiException ex) {
            throw new XPathException(ex);
        }
    }

    @Override
    public void close() {
        // Logger.getLogger(MarkLogicQuery.class.getName()).log(Level.INFO, "Total result(s): {0}", resultCount);
        if (closed) {
            return;
        }
        try {
            // Logger.getLogger(MarkLogicQuery.class.getName()).log(Level.INFO, "Closing sequence iterator.");
            closed = true;
            result.close();
            session.release();
        } catch (Exception ex) {
            Logger.getLogger(MarkLogicQuery.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public SequenceIterator getAnother() throws XPathException {
        return null;
    }

    @Override
    public int getProperties() {
        return 0;
    }

}