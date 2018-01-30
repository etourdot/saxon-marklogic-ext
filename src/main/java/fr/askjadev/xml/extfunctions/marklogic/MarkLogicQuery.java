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
import com.marklogic.client.FailedRequestException;
import com.marklogic.client.ForbiddenUserException;
import com.marklogic.client.eval.EvalResultIterator;
import com.marklogic.client.eval.ServerEvaluationCall;
import fr.askjadev.xml.extfunctions.marklogic.util.ConfigValue;
import java.util.HashMap;
import java.util.Iterator;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.ma.map.HashTrieMap;
import net.sf.saxon.ma.map.KeyValuePair;
import net.sf.saxon.om.LazySequence;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.value.BooleanValue;

/**
 * This class is an extension function for Saxon. It must be declared by
 * <tt>configuration.registerExtensionFunction(new MarkLogicQuery());</tt>, or
 * via Saxon Configuration file
 * (<a href=http://www.saxonica.com/documentation9.7/index.html#!configuration/configuration-file>Saxon
 * documentation</a>). In gaulois-pipe, it just has to be in the classpath.
 *
 * Use as :
 * <tt>declare namespace els-ext = 'fr:askjadev:xml:extfunctions';
 * mkl-ext:marklogic-query("for $i in 1 to 10 return
 * &lt;test&gt;{$i}&lt;/test&gt;",&lt;marklogic&gt;
 * &lt;server&gt;localhost&lt;/server&gt;&lt;port&gt;8999&lt;/port&gt;
 * &lt;user&gt;user&lt;/user&gt;&lt;password&gt;password&lt;/password&gt;&lt;/marklogic&gt;
 * );</tt>
 * Or :
 * <tt>declare namespace els-ext = 'fr:askjadev:xml:extfunctions';
 * mkl-ext:marklogic-query("for $i in 1 to 10 return
 * &lt;test&gt;{$i}&lt;/test&gt;", "localhost", "8999", "user", "password");</tt>
 *
 * Many thanks to Christophe Marchand for the base code!
 *
 * @author Axel Court
 */
public class MarkLogicQuery extends ExtensionFunctionDefinition {

    public static final String EXT_NAMESPACE_URI = "fr:askjadev:xml:extfunctions";
    public static final String FUNCTION_NAME = "marklogic-query";
    public static final String EXT_NS_COMMON_PREFIX = "mkl-ext";

    @Override
    public StructuredQName getFunctionQName() {
        return new StructuredQName(EXT_NS_COMMON_PREFIX, EXT_NAMESPACE_URI, FUNCTION_NAME);
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return new SequenceType[] {
            SequenceType.SINGLE_ATOMIC,
            MapType.OPTIONAL_MAP_ITEM,
            MapType.OPTIONAL_MAP_ITEM
        };
    }

    @Override
    public int getMinimumNumberOfArguments() {
        return 2;
    }

    @Override
    public int getMaximumNumberOfArguments() {
        return 3;
    }

    @Override
    public SequenceType getResultType(net.sf.saxon.value.SequenceType[] sts) {
        return SequenceType.ANY_SEQUENCE;
    }

    @Override
    public ExtensionFunctionCall makeCallExpression() {
        
        return new ExtensionFunctionCall() {
            
            @Override
            public Sequence call(XPathContext xpc, Sequence[] sqncs) throws XPathException {
                // Check and get the configuration
                HashMap<String,ConfigValue> config = getConfig(xpc, sqncs);
                // Launch
                Processor proc = new Processor(xpc.getConfiguration());
                DatabaseClient session;
                try {
                    // Get SecurityContext -> Digest or Basic (default)
                    DatabaseClientFactory.SecurityContext authContext;
                    switch (config.get("authentication").strVal()) {
                        case "digest":
                            authContext = new DatabaseClientFactory.DigestAuthContext(config.get("user").strVal(), config.get("password").strVal());
                            break;
                        default:
                            authContext = new DatabaseClientFactory.BasicAuthContext(config.get("user").strVal(), config.get("password").strVal());
                    }
                    // Init session
                    if (!(config.get("database") == null)) {
                        session = DatabaseClientFactory.newClient(config.get("server").strVal(), config.get("port").intVal(), config.get("database").strVal(), authContext);
                    } else {
                        session = DatabaseClientFactory.newClient(config.get("server").strVal(), config.get("port").intVal(), authContext);
                    }
                    //Get the XQuery (URI or String query)
                    

                    // Eval query and get result
                    DocumentBuilder builder = proc.newDocumentBuilder();
                    ServerEvaluationCall call = session.newServerEval();
                    
                    // TO 
                    
                    //call.xquery(xquery);
                    EvalResultIterator result = call.eval();
                    MarkLogicSequenceIterator it = new MarkLogicSequenceIterator(result, builder, session);
                    return new LazySequence(it);
                } catch (FailedRequestException | ForbiddenUserException ex) {
                    throw new XPathException(ex);
                }
            }

            private HashMap<String,ConfigValue> getConfig(XPathContext xpc, Sequence[] sqncs) throws XPathException {
                HashMap<String,ConfigValue> config = initBlankConfig();
                try {
                    HashTrieMap configMap = (HashTrieMap) sqncs[2].head();
                    Iterator<KeyValuePair> iterator = configMap.iterator();
                    for (KeyValuePair kv = iterator.next(); kv != null; kv = iterator.next()) {
                        String k = kv.key.getStringValue();
                        switch (k) {
                            case "server":
                            case "user":
                            case "password":
                            case "database":
                            case "authentication":
                                ConfigValue<String> strval = new ConfigValue<>();
                                strval.set(kv.value.head().getStringValue());
                                config.put(k, strval);
                                break;
                            case "port":
                                ConfigValue<Integer> intval = new ConfigValue<>();
                                intval.set(Integer.parseInt(kv.value.head().getStringValue()));
                                config.put(k, intval);
                                break;
                            case "isXQueryOnServer":
                                ConfigValue<Boolean> boolval = new ConfigValue<>();
                                boolval.set(((BooleanValue) kv.value.head()).getBooleanValue());
                                config.put(k, boolval);
                                break;
                            default:
                                throw new XPathException("Configuration entries must be 'server', 'port', 'user', 'password', 'database'?, 'authentication'? and 'isXQueryOnServer'?.");
                        }
                    }
                    if (config.get("server") == null ||
                        config.get("user") == null ||
                        config.get("password") == null ||
                        config.get("port") == null) {
                        throw new XPathException("Some mandatory configuration values are missing. 'server', 'port', 'user' and 'password' must be specified.");
                    }
                    return config;
                }
                catch (ClassCastException ex) {
                    throw new XPathException("The 2nd argument must be an XPath 3.0 map defining the server and query configuration.");
                }
            }
                
            private HashMap<String,ConfigValue> initBlankConfig() {
                HashMap<String,ConfigValue> blankConfig = new HashMap<>();
                blankConfig.put("server", null);
                blankConfig.put("user", null);
                blankConfig.put("password", null);
                blankConfig.put("database", null);
                blankConfig.put("port", null);
                ConfigValue<String> authentication = new ConfigValue<>();
                authentication.set("basic");
                blankConfig.put("authentication", authentication);
                ConfigValue<Boolean> isXQueryOnServer = new ConfigValue<>();
                isXQueryOnServer.set(false);
                blankConfig.put("isXQueryOnServer", isXQueryOnServer);
                return blankConfig;
            }
                
                
                
                
//                switch (sqncs.length) {
//                    case 2:
//                        try {
//                            TinyElementImpl basexNode = ((TinyElementImpl) sqncs[1].head());
//                            AxisIterator iterator = basexNode.iterateAxis(AxisInfo.CHILD);
//                            for (NodeInfo ni = iterator.next(); ni != null; ni = iterator.next()) {
//                                if (ni.getNodeKind() == Type.ELEMENT) {
//                                    switch (ni.getLocalPart()) {
//                                        case "server":
//                                            server = ni.getStringValue();
//                                            break;
//                                        case "port":
//                                            port = ni.getStringValue();
//                                            break;
//                                        case "user":
//                                            user = ni.getStringValue();
//                                            break;
//                                        case "password":
//                                            password = ni.getStringValue();
//                                            break;
//                                        case "database":
//                                            database = ni.getStringValue();
//                                            break;
//                                        case "authentication":
//                                            authentication = ni.getStringValue();
//                                            break;
//                                        default:
//                                            throw new XPathException("Children elements of 'marklogic' must be 'server', 'port', 'user', 'password', 'database'? and 'authentication'?.");
//                                    }
//                                }
//                            }
//                            return new String[]{
//                                ((StringValue) sqncs[0].head()).getStringValue(),
//                                server,
//                                port,
//                                user,
//                                password,
//                                database,
//                                authentication
//                            };
//                        } catch (ClassCastException ex) {
//                            throw new XPathException("When using the 2 parameters signature, the second parameter must be of type: element(marklogic).");
//                        }
//                    case 5:
//                    case 6:
//                    case 7:
//                        try {
//                            if (sqncs.length == 6) {
//                                database = ((StringValue) sqncs[5].head()).getStringValue();
//                            }
//                            if (sqncs.length == 7) {
//                                database = ((StringValue) sqncs[5].head()).getStringValue();
//                                authentication = ((StringValue) sqncs[6].head()).getStringValue();
//                            }
//                            return new String[]{
//                                ((StringValue) sqncs[0].head()).getStringValue(),
//                                ((StringValue) sqncs[1].head()).getStringValue(),
//                                ((StringValue) sqncs[2].head()).getStringValue(),
//                                ((StringValue) sqncs[3].head()).getStringValue(),
//                                ((StringValue) sqncs[4].head()).getStringValue(),
//                                database,
//                                authentication
//                            };
//                        } catch (ClassCastException ex) {
//                            throw new XPathException("When using the 5/6/7 parameters signature, all parameters must be of type: xs:string.");
//                        }
//                    default:
//                        throw new XPathException("Illegal number of arguments. "
//                                + "Arguments are either ($query as xs:string, $config as element(marklogic)), "
//                                + "($query as xs:string, $server as xs:string, $port as xs:string, $user as xs:string, $password as xs:string), "
//                                + "($query as xs:string, $server as xs:string, $port as xs:string, $user as xs:string, $password as xs:string, $database as xs:string), "
//                                + "or ($query as xs:string, $server as xs:string, $port as xs:string, $user as xs:string, $password as xs:string, $database as xs:string, $authentication as xs:string).");
//                }
//            }
            
        };
    
    }

}