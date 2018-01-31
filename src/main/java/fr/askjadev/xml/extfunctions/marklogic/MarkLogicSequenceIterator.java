package fr.askjadev.xml.extfunctions.marklogic;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.eval.EvalResultIterator;
import com.marklogic.client.io.BytesHandle;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

class MarkLogicSequenceIterator implements SequenceIterator, AutoCloseable {

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
            Logger.getLogger(MarkLogicQueryInvoke.class.getName()).log(Level.SEVERE, null, ex);
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
