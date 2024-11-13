# Security recommendations for Tomcat 9 deployments (Windows 10/11)
## **Priority Summary**:

1. Run Tomcat as a non-administrative user and enforce strict file permissions.
2. Add TLS 1.2/1.3 with FIPS-validated ciphers and HSTS. 
3. Harden cookie security (HttpOnly and Secure flags).
4. Enable AccessLogValve and log client IPs behind proxies.
5. Patch Tomcat regularly and enable LockOutRealm.
6. Remove unnecessary applications and connectors and harden the shutdown port.
7. Disable auto-deployment and stack tracing.
8. Audit file changes and protect the keystore.

The rest of this guide assumes that you installed Tomcat9 as a service (https://tomcat.apache.org/download-90.cgi).

## 1. **Run Tomcat as a Non-Administrative User**

Running Tomcat as a non-administrative user on Windows limits the damage that can occur if the Tomcat instance is compromised. By default, Tomcat runs as a system service on Windows. You should configure it to run under a less privileged user account.

- **Why it matters**: Minimizes privileges and limits the damage an attacker can inflict if they gain access to Tomcat.

### How-to

1. Create a new local user called `Tomcat` with limited privileges.
2. Ensure that the user can `Log as a Service` (Local Security Policy > Logal Policies > User Rights Assignment > Log on as a service)
2. Open the **Services** management console (`services.msc`).
3. Locate the `Apache Tomcat` service.
4. Right-click on the service and select **Properties**.
5. Go to the **Log On** tab.
6. Select **This account**, then enter the `Tomcat` account credentials.
7. Restart the service.

## 2. **Set proper file permissions**

Proper file permissions ensure only authorized users can access or modify critical Tomcat files. On Windows, this involves setting file permissions using the Security tab in the file properties.

- **Why it matters**: Prevents unauthorized access or modification of Tomcat’s critical configuration files.

### How-to

1. Navigate to the Tomcat installation directory (`%CATALINA_HOME%`), typically C:\Program Files\Apache Software Foundation\Tomcat 9.0.
2. Right-click on the conf, bin, logs, webapps, and temp folders and select Properties.
3. Go to the Security tab and assign the following permissions:
   - TomcatUser: Full Control for logs, temp, work, and webapps. 
   - Administrators: Full Control for conf and bin. 
   - Others: Read and execute only.

- **Why it matters**: Prevents unauthorized access or modification of sensitive configuration files and logs.

## 3. **Add TLS v1.2 or v1.3 with FIPS-validated ciphers**

TLS (Transport Layer Security) is essential for encrypting data transmitted between clients and servers. By enforcing TLS 1.2 or 1.3 with strong, FIPS-validated ciphers, you ensure secure communication that meets strict compliance standards.

1. Open the server.xml file located in %CATALINA_HOME%\conf\server.xml.
2. Add or modify the SSL connector:
```xml
<Connector port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol"
           maxThreads="150" SSLEnabled="true">
   <SSLHostConfig>
      <Protocols>
         <TLSv1.2 />
         <TLSv1.3 />
      </Protocols>
      <Cipher>TLS_AES_128_GCM_SHA256, TLS_AES_256_GCM_SHA384, TLS_CHACHA20_POLY1305_SHA256</Cipher>
      <Certificate certificateKeyFile="conf/server.key"
                   certificateFile="conf/server.crt"
                   certificateChainFile="conf/chain.crt"
                   type="RSA" />
   </SSLHostConfig>
</Connector>
```
3. Restart the service

## 4. **Add HTTP Strict Transport Security (HSTS)**

HSTS instructs browsers to only interact with your server using HTTPS. It prevents downgrade attacks where an attacker could force a user to connect via HTTP. Once a browser receives the HSTS header, it will automatically convert all future requests to HTTPS, ensuring secure communication.

- **Why it matters**: Protects against protocol downgrade attacks and ensures that all communications use HTTPS.
### How-to
**Open the `web.xml` file in `%CATALINA_HOME%\conf\web.xml`**:

**Change the filter values**:
```xml
<filter>
	<filter-name>HSTSFilter</filter-name>
	<filter-class>org.apache.catalina.filters.HttpHeaderSecurityFilter</filter-class> 
	<init-param> 
		<param-name>hstsEnabled</param-name>
		<param-value>true</param-value>
	</init-param>
	<init-param> 
		<param-name>hstsMaxAgeSeconds</param-name> 
		<param-value>31536000</param-value> 
	</init-param> 
</filter> 
<filter-mapping>
	<filter-name>HSTSFilter</filter-name>
	<url-pattern>/*</url-pattern>
</filter-mapping>
```

Restart the `Tomcat` service

## 5. **Disable X-Powered-By Header**

By default, Tomcat may expose the `X-Powered-By` header, which discloses information about the server and its version. Disabling this prevents attackers from easily identifying the version of Tomcat being used, reducing the risk of targeted attacks based on known vulnerabilities.

- **Why it matters**: Hides unnecessary information from potential attackers and reduces the risk of exploit-based attacks.
### How-to
**Open the `server.xml` file in `%CATALINA_HOME%\conf\server.xml`**:


**Change file permissions**:
```xml
<Valve className="org.apache.catalina.valves.ErrorReportValve" showReport="false" showServerInfo="false" />
```

And restart the `Tomcat` service.

## 6. **Harden Cookie Security (HttpOnly & Secure flags)**

- **HttpOnly flag**: Ensures that cookies cannot be accessed via JavaScript, protecting them from cross-site scripting (XSS) attacks.
- **Secure flag**: Ensures that cookies are only transmitted over HTTPS, preventing them from being exposed during plaintext transmission.
- **Why it matters**: Protects session cookies from being stolen or accessed by malicious scripts.
### How-to
1. Open `web.xml` in `%CATALINA_HOME%\conf\web.xml`.

2. **Set the cookie properties**:
```xml
<session-config>
	<cookie-config>
		<http-only>true</http-only>
		<secure>true</secure>
	</cookie-config>
</session-config>
```

## 7. **Enable AccessLogValve for each virtual host**

Configuring **AccessLogValve** for each virtual host in Tomcat allows you to log incoming requests for auditing and troubleshooting purposes. These logs provide valuable insights into who is accessing the server and can help detect potential malicious activity.

- **Why it matters**: Provides detailed logs for auditing, forensics, and detecting suspicious activity.

### How-to

1. Open `server.xml` in `%CATALINA_HOME%\conf\server.xml`.

2. **Change file permissions**:
```xml
<Host name="localhost" appBase="webapps" unpackWARs="false" autoDeploy="false">
<Valve className="org.apache.catalina.valves.AccessLogValve" directory="logs"
prefix="localhost_access_log" suffix=".txt"
pattern="%h %l %u %t &quot;%r&quot; %s %b" />
</Host>
```

## 8. **Regularly patch Tomcat for security vulnerabilities**

Tomcat, like any other software, can have security vulnerabilities. Regularly applying patches and updates ensures that your installation is protected against known threats. It’s crucial to stay up to date with the latest security patches to mitigate risks.

- **Why it matters**: Fixes known vulnerabilities and prevents attackers from exploiting outdated software.

## 9. **Use LockOutRealm to prevent brute-force attacks**

The **LockOutRealm** helps prevent brute-force login attacks by locking out users after a set number of failed login attempts. Configuring this for admin accounts is particularly important.

- **Why it matters**: Prevents attackers from brute-forcing admin credentials.
### How-to
1. Open `server.xml` in `%CATALINA_HOME%\conf\server.xml`.
2. **Add `failureCount` et `lockOutTime`**:
```xml
<Realm className="org.apache.catalina.realm.LockOutRealm" failureCount="5" lockOutTime="600">
  <Realm className="org.apache.catalina.realm.UserDatabaseRealm"
         resourceName="UserDatabase"/>
</Realm>
```
3. Restart the `Tomcat` service

## 10. **Secure the keystore file**

The keystore contains sensitive information, such as private keys used for SSL/TLS. Ensuring that the keystore file is properly protected by strict file permissions and password protection prevents unauthorized access to critical security credentials.

- **Why it matters**: Protects sensitive private keys from unauthorized access or tampering.
### How-to
1. Right-click on the keystore file and select **Properties**.
2. Go to the **Security** tab.
3. Ensure only the `Tomcat` user has access, and set the permissions to **Read** and **Execute**.

## 11. **Disable Auto-Deployment and Stack Tracing**

Auto-deployment introduces security risks by allowing new applications to be deployed without manual intervention. Disabling stack traces prevents attackers from seeing sensitive system information.
- **Why it matters**: Prevents unauthorized deployments and hides sensitive details during errors.

### How-to
1. Open `server.xml` in `%CATALINA_HOME%\conf\server.xml`.
2. Modify the `<Host>` block:
**Add the auditing rules**:
```xml
<Host name="localhost" appBase="webapps" unpackWARs="true" autoDeploy="false" deployOnStartup="false">
```
3. To disable stack traces, open `web.xml` in `%CATALINA_HOME%\conf\web.xml` and add:
```xml
<error-page>
   <exception-type>java.lang.Throwable</exception-type>
   <location>/error.jsp</location>
</error-page>
```

## 12. **Remove unnecessary applications and connectors**

- **Remove folders**: Unused folders such as `documentation`, `examples`, `manager`, and `host-manager` should be deleted from the production environment to minimize the attack surface.
- **Remove unused connectors**: Disable and remove any unused connectors (e.g., HTTP connector) to reduce exposure to attacks.
- **Why it matters**: Minimizes the attack surface and reduces potential entry points for attackers.
- **Disable unused connectors** in `server.xml`

## 13. **Harden the shutdown port**

The shutdown port allows Tomcat to be stopped remotely. Disabling this port or restricting access ensures that attackers cannot shut down your server unexpectedly.

- **Why it matters**: Prevents unauthorised shutdown of Tomcat, ensuring continuous availability.

### How-to
1. Open `server.xml` in `%CATALINA_HOME%\conf\server.xml`.
2. **Set the shutdown port to `-1` to disable it:**
```xml
<Server port="-1" shutdown="SHUTDOWN">
</Server>
```
You can also set the port to `-1` during the installation of the service.

---

## **Additional Recommendations**:

1. **Replace the ROOT web application**: It is a best practice to replace the default Tomcat ROOT web application with a custom one or remove it if not needed. The default ROOT application can expose unnecessary information.
2. **Use a Java Security Manager**: The Java Security Manager enforces restrictions on the permissions of Java code running within Tomcat. Enabling it adds an additional layer of security by limiting what code can do.
3. **Set `failureCount` to 5 failed login attempts** for admin accounts: This limits the number of attempts to brute-force credentials, protecting admin accounts from attacks.
## Sources
https://www.stigviewer.com/stig/apache_tomcat_application_server_9/
https://nvlpubs.nist.gov/nistpubs/SpecialPublications/NIST.SP.800-131Ar2.pdf
https://tomcat.apache.org/tomcat-9.0-doc/security-howto.html
https://www.openlogic.com/blog/apache-tomcat-security-best-practices
https://knowledge.broadcom.com/external/article/226769/enable-http-strict-transport-security-hs.html