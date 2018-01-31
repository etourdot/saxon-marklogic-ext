package fr.askjadev.xml.extfunctions.marklogic;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.FailedRequestException;
import com.marklogic.client.ForbiddenUserException;
import com.marklogic.client.eval.EvalResultIterator;
import com.marklogic.client.eval.ServerEvaluationCall;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.*;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.tiny.TinyElementImpl;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public abstract class AbstractMLExtensionFunction extends ExtensionFunctionDefinition {

    enum ExtentionType {
        XQUERY, MODULE;
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return new SequenceType[]{
                SequenceType.SINGLE_STRING,
                SequenceType.SINGLE_ITEM,
                SequenceType.OPTIONAL_STRING,
                SequenceType.OPTIONAL_STRING,
                SequenceType.OPTIONAL_STRING,
                SequenceType.OPTIONAL_STRING,
                SequenceType.OPTIONAL_STRING};
    }

    @Override
    public int getMinimumNumberOfArguments() {
        return 2;
    }

    @Override
    public int getMaximumNumberOfArguments() {
        return 7;
    }

    @Override
    public SequenceType getResultType(net.sf.saxon.value.SequenceType[] sts) {
        return SequenceType.ANY_SEQUENCE;
    }

    ExtensionFunctionCall constructExtensionFunctionCall(final ExtentionType type) {
        return new ExtensionFunctionCall() {
            @Override
            public Sequence call(XPathContext xpc, Sequence[] sqncs) throws XPathException {
                // Get and check args
                String[] args = checkArgs(sqncs);
                // Read args
                String moduleOrQuery = args[0];
                String server = args[1];
                // TODO: catch exception if non num !
                Integer port = Integer.parseInt(args[2]);
                String user = args[3];
                String password = args[4];
                String database = args[5];
                String authentication = args[6];
                // Launch
                Processor proc = new Processor(xpc.getConfiguration());
                DatabaseClient session = null;
                try {
                    session = createMarkLogicClient(authentication, user, password, database, server, port);
                    // Eval query and get result
                    DocumentBuilder builder = proc.newDocumentBuilder();
                    ServerEvaluationCall call = session.newServerEval();
                    if (type == ExtentionType.MODULE) {
                        call.modulePath(moduleOrQuery);
                    } else {
                        call.xquery(moduleOrQuery);
                    }
                    EvalResultIterator result = call.eval();
                    MarkLogicSequenceIterator it = new MarkLogicSequenceIterator(result, builder, session);
                    return new LazySequence(it);
                } catch (FailedRequestException | ForbiddenUserException ex) {
                    throw new XPathException(ex);
                }
            }
        };
    }

    private DatabaseClient createMarkLogicClient(String authentication, String user, String password,
                                                       String database, String server, Integer port) {
        DatabaseClientFactory.SecurityContext authContext;
        switch (authentication) {
            case "digest":
                authContext = new DatabaseClientFactory.DigestAuthContext(user, password);
                break;
            default:
                authContext = new DatabaseClientFactory.BasicAuthContext(user, password);
        }
        // Init session
        final DatabaseClient session;
        if (!(database == null)) {
            session = DatabaseClientFactory.newClient(server, port, database, authContext);
        } else {
            session = DatabaseClientFactory.newClient(server, port, authContext);
        }
        return session;
    }

    private String[] checkArgs(Sequence[] sqncs) throws XPathException {
        String server = null, port = null, user = null, password = null, database = null;
        String authentication = "basic";
        switch (sqncs.length) {
            case 2:
                try {
                    TinyElementImpl basexNode = ((TinyElementImpl) sqncs[1].head());
                    AxisIterator iterator = basexNode.iterateAxis(AxisInfo.CHILD);
                    for (NodeInfo ni = iterator.next(); ni != null; ni = iterator.next()) {
                        if (ni.getNodeKind() == Type.ELEMENT) {
                            switch (ni.getLocalPart()) {
                                case "server":
                                    server = ni.getStringValue();
                                    break;
                                case "port":
                                    port = ni.getStringValue();
                                    break;
                                case "user":
                                    user = ni.getStringValue();
                                    break;
                                case "password":
                                    password = ni.getStringValue();
                                    break;
                                case "database":
                                    database = ni.getStringValue();
                                    break;
                                case "authentication":
                                    authentication = ni.getStringValue();
                                    break;
                                default:
                                    throw new XPathException("Children elements of 'marklogic' must be 'server', 'port', 'user', 'password', 'database'? and 'authentication'?.");
                            }
                        }
                    }
                    return new String[]{
                            ((StringValue) sqncs[0].head()).getStringValue(),
                            server,
                            port,
                            user,
                            password,
                            database,
                            authentication
                    };
                } catch (ClassCastException ex) {
                    throw new XPathException("When using the 2 parameters signature, the second parameter must be of type: element(marklogic).");
                }
            case 5:
            case 6:
            case 7:
                try {
                    if (sqncs.length == 6) {
                        database = ((StringValue) sqncs[5].head()).getStringValue();
                    }
                    if (sqncs.length == 7) {
                        database = ((StringValue) sqncs[5].head()).getStringValue();
                        authentication = ((StringValue) sqncs[6].head()).getStringValue();
                    }
                    return new String[]{
                            ((StringValue) sqncs[0].head()).getStringValue(),
                            ((StringValue) sqncs[1].head()).getStringValue(),
                            ((StringValue) sqncs[2].head()).getStringValue(),
                            ((StringValue) sqncs[3].head()).getStringValue(),
                            ((StringValue) sqncs[4].head()).getStringValue(),
                            database,
                            authentication
                    };
                } catch (ClassCastException ex) {
                    throw new XPathException("When using the 5/6/7 parameters signature, all parameters must be of type: xs:string.");
                }
            default:
                throw new XPathException("Illegal number of arguments. "
                        + "Arguments are either ($query as xs:string, $config as element(marklogic)), "
                        + "($query as xs:string, $server as xs:string, $port as xs:string, $user as xs:string, $password as xs:string), "
                        + "($query as xs:string, $server as xs:string, $port as xs:string, $user as xs:string, $password as xs:string, $database as xs:string), "
                        + "or ($query as xs:string, $server as xs:string, $port as xs:string, $user as xs:string, $password as xs:string, $database as xs:string, $authentication as xs:string).");
        }
    }
}
