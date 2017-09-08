# saxon-marklogic-ext

This is an extension function for Saxon allowing XQueries to be sent to MarkLogic Server.


The extension may be registered:
* through <tt>configuration.registerExtensionFunction(new MarkLogicQuery());</tt>
* via Saxon Configuration file (<a href=http://www.saxonica.com/documentation9.7/index.html#!configuration/configuration-file>Saxon documentation</a>).


It is also a <a href=https://github.com/cmarchand/gaulois-pipe>gaulois-pipe</a> service. It just has to be in the classpath to be used with gaulois-pipe.


Usage:

<pre>declare namespace els-ext = 'fr:askjadev:xml:extfunctions';
mkl-ext:marklogic-query(
  "for $i in 1 to 10 return&lt;test&gt;{$i}&lt;/test&gt;",
  &lt;marklogic&gt;&lt;server&gt;host&lt;/server&gt;&lt;port&gt;8999&lt;/port&gt;&lt;user&gt;user&lt;/user&gt;&lt;password&gt;password&lt;/password&gt;&lt;/marklogic&gt;
);</pre>


Or the alternative "xs:string+ signature":
<pre>declare namespace els-ext = 'fr:askjadev:xml:extfunctions';
mkl-ext:marklogic-query(
  "for $i in 1 to 10 return&lt;test&gt;{$i}&lt;/test&gt;",
  "host", "8999", "user", "password"
);</pre>


You can supply 2 additionnal parameters:

- <tt>&lt;database&gt;database name&lt;/database&gt;</tt> : alternative database name, if not using the one associated with the HTTP server.
- <tt>&lt;authentication&gt;authentication method&lt;/authentication&gt;</tt> : authentication method. Authorized values: "digest", "basic" (default).

When using the alternative "xs:string+ signature", <tt>$database</tt> and <tt>$authentication</tt> must be supplied as the 6th and 7th arguments respectively.


Many thanks to Christophe Marchand for the base code!
