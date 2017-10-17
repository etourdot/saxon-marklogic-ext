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


Or the alternative "<tt>xs:string+</tt> signature":
<pre>declare namespace els-ext = 'fr:askjadev:xml:extfunctions';
mkl-ext:marklogic-query(
  "for $i in 1 to 10 return&lt;test&gt;{$i}&lt;/test&gt;",
  "host", "8999", "user", "password"
);</pre>


You can supply 2 additionnal parameters:

- <tt>&lt;database&gt;database name&lt;/database&gt;</tt> : alternative database name, if not using the one associated with the HTTP server.
- <tt>&lt;authentication&gt;authentication method&lt;/authentication&gt;</tt> : authentication method. Authorized values: "digest", "basic" (default).

When using the alternative "<tt>xs:string+</tt> signature", <tt>$database</tt> and <tt>$authentication</tt> must be supplied as the 6th and 7th arguments respectively.


/!\ The query must return a valid XML document (or a sequence of XML documents). If you need to return an atomic value, wrap it in a dummy XML element.


Many thanks to Christophe Marchand for the base code!

Go there for a BaseX similar extension function: <a href="https://github.com/cmarchand/xpath-basex-ext">https://github.com/cmarchand/xpath-basex-ext</a>.


## Current version: 1.0.3

Maven support:

<pre>
&lt;dependency&gt;
  &lt;groupId&gt;fr.askjadev.xml.extfunctions&lt;/groupId&gt;
  &lt;artifactId&gt;marklogic&lt;/artifactId&gt;
  &lt;version&gt;1.0.3&lt;/version&gt;
&lt;/dependency&gt;
</pre>


## Build

To build the project from the sources, follow these steps:

<pre>
$ git clone https://github.com/AxelCourt/saxon-marklogic-ext.git
$ cd saxon-marklogic-ext
$ mvn clean package -DskipTests=true
</pre>

Please note that you need to deactivate the tests using the parameter `-DskipTests=true` to be able to build the project, unless you have correctly configured your MarkLogic Server environment.


## Testing

The tests require a running MarkLogic Server instance. By default, they are run under the following MarkLogic Server configuration:

* MarkLogic Server runs on `localhost`.
* There is a `Documents` database associated with a HTTP Server on port `8000`.
* Username/password are `admin`/`admin`.
* The HTTP Server authentication scheme is `basic`.

If you wish to change this behaviour, you can add additional parameters to the test command-line which values will be used instead of the default ones.

|Parameter|Default values|Usage|Description|
|----|----|----|----|
|testServer|localhost|`-DtestServer=10.11.12.90`|The server on which to run the tests.|
|testPort|8000|`-DtestPort=8999`|The port to use to talk to the HTTP Server.|
|testUser|admin|`-DtestUser=myUser`|An authorised user.|
|testPassword|admin|`-DtestPassword=myPassword`|The user password.|
|testDatabase|Documents|`-DtestDatabase=myDb`|The HTTP Server default database name.|
|testAuthentication|basic|`-DtestAuthentication=digest`|The HTTP Server authentication scheme. Authorized values: `basic` or `digest`.|
