---
title: Cybersecurity risk mitigations (Linux)
---

# Security recommendations for Tomcat 9 deployments (Linux)

## Priority Summary

1. Run Tomcat as a non-root user and enforce strict file permissions.
2. Add TLS 1.2/1.3 with FIPS-validated ciphers and HSTS.
3. Harden cookie security (HttpOnly and Secure flags).
4. Enable AccessLogValve and log client IPs behind proxies.
5. Patch Tomcat regularly and enable LockOutRealm.
6. Remove unnecessary applications and connectors and harden the shutdown port.
7. Disable auto-deployment and stack tracing.
8. Audit file changes and protect the keystore.

## 1. Run Tomcat as a non-root user

Running Tomcat as a non-root user is a critical security practice. If Tomcat were to be compromised while running as root, an attacker would gain full control of the server. By running it under a limited, non-root user, the potential damage is restricted to the Tomcat environment, reducing the risk to the overall system.

- **Why it matters**: Minimizes privileges and limits the damage an attacker can inflict if they gain access to Tomcat.

**How-to**

1. Create a user and group for Tomcat:
```shell
sudo useradd -s /sbin/nologin -g tomcat -d $CATALINA_BASE tomcat
```

2. Set the ownership:
```shell
sudo chown -R tomcat:tomcat $CATALINA_BASE
```

3. Run Tomcat under the `tomcat` user:

    * Open `tomcat.service` (assuming Tomcat is managed by `systemd`):
```shell
sudo nano /etc/systemd/system/tomcat.service
```

    * Ensure the following lines are set to run Tomcat as the `tomcat` user:
```
[Unit]
Description=Apache Tomcat Web Application Container
After=syslog.target network.target

[Service]
Type=forking
User=tomcat
Group=tomcat

ReadWritePaths=/var/log/extract/

Environment=JAVA_HOME=/usr/lib/jvm/jdk-17.0.12-oracle-x64
Environment=CATALINA_PID=/opt/apache-tomcat-9.0.95/temp/tomcat.pid
Environment=CATALINA_HOME=/opt/apache-tomcat-9.0.95
Environment=CATALINA_BASE=/opt/apache-tomcat-9.0.95
Environment=CATALINA_OPTS=
Environment="JAVA_OPTS=-Dfile.encoding=UTF-8 -Dnet.sf.ehcache.skipUpdateCheck=true -Xms2g -Xmx4g"

ExecStart=/opt/apache-tomcat-9.0.95/bin/startup.sh
ExecStop=/opt/apache-tomcat-9.0.95/bin/shutdown.sh

[Install]
WantedBy=multi-user.target 
```

        The line `ReadWritePaths=/var/log/extract` is mandatory because of the new sandboxing imposed by the `systemd` service manager.

    * Reload `systemd` in order to discover and load the new Tomcat service file:
```shell
systemctl daemon-reload
```

    * Enable the service to start at boot:
```shell
systemctl enable tomcat.service
```

        To control the service:
```shell
service tomcat [start | stop | restart | status]
```

        Or with Systemd directly:
```shell
systemctl [start | stop | restart | status] tomcat
```

    * Reload systemd and restart Tomcat:
```shell
sudo systemctl daemon-reload
sudo systemctl restart tomcat
```

## 2. Set proper file permissions

- **$CATALINA_HOME folder** must be owned by the root user, group `tomcat`.
- **$CATALINA_HOME/conf folder** must be owned by the root user, group `tomcat`.
- **$CATALINA_BASE/logs folder** must be owned by the tomcat user, group `tomcat`.
- **$CATALINA_BASE/work folder** must be owned by the tomcat user, group `tomcat`.
- Permissions:
    - **$CATALINA_BASE/logs**: `750`
    - **$CATALINA_HOME/bin**: `750`
    - Files in **$CATALINA_BASE/logs**: `640`
    - JAR files in **$CATALINA_HOME/bin**: `640`

Proper file permissions ensure that only authorized users can access or modify critical Tomcat files and directories. Limiting access to configuration files (e.g., `server.xml`) to root, while allowing Tomcat-specific logs and runtime files to be owned by the `tomcat` user, strengthens the security by preventing unauthorized changes.

- **Why it matters**: Prevents unauthorized access or modification of sensitive configuration files and logs.

**How-to**

* Change the ownership:
```shell
sudo chown -R root:tomcat $CATALINA_HOME
sudo chown -R tomcat:tomcat $CATALINA_HOME/{logs,temp,webapps,work}
```

* Change file permissions:
```shell
sudo chmod -R 750 $CATALINA_HOME/bin
sudo chmod -R 750 $CATALINA_HOME/logs
sudo find $CATALINA_HOME/logs -type f -exec chmod 640 {} \;
sudo find $CATALINA_HOME/bin -type f -name "*.jar" -exec chmod 640 {} \;
```

## 3. Add TLS v1.2 or v1.3 with FIPS-validated ciphers

TLS (Transport Layer Security) is essential for encrypting data transmitted between clients and servers. By enforcing TLS 1.2 or 1.3 with strong, FIPS-validated ciphers, you ensure secure communication that meets strict compliance standards.

- **Why it matters**: Encrypts sensitive data in transit and complies with modern security standards like FIPS 140-2.

**How-to**

* Edit the `server.xml` file:
```shell
sudo nano /opt/tomcat/conf/server.xml
```

* Change file permissions:
```xml
<Connector port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol" maxThreads="150" SSLEnabled="true">
	<SSLHostConfig>
		<Protocols>
			<TLSv1.2 />
			<TLSv1.3 />
		</Protocols>
		<Cipher>TLS_AES_128_GCM_SHA256, TLS_AES_256_GCM_SHA384, TLS_CHACHA20_POLY1305_SHA256</Cipher> 
		<Certificate certificateKeyFile="conf/server.key" certificateFile="conf/server.crt" certificateChainFile="conf/chain.crt" type="RSA" />
	</SSLHostConfig> 
</Connector>
```

* Restart Tomcat:
```shell
sudo nano /opt/tomcat/conf/server.xml
```

## 4. Add HTTP Strict Transport Security (HSTS)

HSTS instructs browsers to only interact with your server using HTTPS. It prevents downgrade attacks where an attacker could force a user to connect via HTTP. Once a browser receives the HSTS header, it will automatically convert all future requests to HTTPS, ensuring secure communication.

- **Why it matters**: Protects against protocol downgrade attacks and ensures that all communications use HTTPS.

**How-to**

* Modify the `web.xml` file in the default web app:
```shell
sudo nano /opt/tomcat/conf/web.xml
```

* Change the filter values:
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

* Restart Tomcat:
```shell
sudo nano /opt/tomcat/conf/server.xml
```

## 5. Disable X-Powered-By Header

By default, Tomcat may expose the `X-Powered-By` header, which discloses information about the server and its version. Disabling this prevents attackers from easily identifying the version of Tomcat being used, reducing the risk of targeted attacks based on known vulnerabilities.

- **Why it matters**: Hides unnecessary information from potential attackers and reduces the risk of exploit-based attacks.

**How-to**

* Edit the `server.xml` file:
```shell
sudo nano /opt/tomcat/conf/server.xml
```

* Change file permissions:
```xml
<Valve className="org.apache.catalina.valves.ErrorReportValve" showReport="false" showServerInfo="false" />
```

* Restart Tomcat:
```shell
sudo nano /opt/tomcat/conf/server.xml
```

## 6. Harden Cookie Security (HttpOnly & Secure flags)

- **HttpOnly flag**: Ensures that cookies cannot be accessed via JavaScript, protecting them from cross-site scripting (XSS) attacks.
- **Secure flag**: Ensures that cookies are only transmitted over HTTPS, preventing them from being exposed during plaintext transmission.
- **Why it matters**: Protects session cookies from being stolen or accessed by malicious scripts.

**How-to**

* Edit the `web.xml` file:
```shell
sudo nano /opt/tomcat/conf/web.xml
```

* Set the cookie properties:
```xml
<session-config>
	<cookie-config>
		<http-only>true</http-only>
		<secure>true</secure>
	</cookie-config>
</session-config>
```

## 7. Enable AccessLogValve for each virtual host

Configuring **AccessLogValve** for each virtual host in Tomcat allows you to log incoming requests for auditing and troubleshooting purposes. These logs provide valuable insights into who is accessing the server and can help detect potential malicious activity.

- **Why it matters**: Provides detailed logs for auditing, forensics, and detecting suspicious activity.

**How-to**

* Edit the `server.xml` file:
```shell
sudo nano /opt/tomcat/conf/server.xml
```

* Change file permissions:
```xml
<Host name="localhost" appBase="webapps" unpackWARs="false" autoDeploy="false">
<Valve className="org.apache.catalina.valves.AccessLogValve" directory="logs"
prefix="localhost_access_log" suffix=".txt"
pattern="%h %l %u %t &quot;%r&quot; %s %b" />
</Host>
```

## 8. Regularly patch Tomcat for security vulnerabilities

Tomcat, like any other software, can have security vulnerabilities. Regularly applying patches and updates ensures that your installation is protected against known threats. It’s crucial to stay up to date with the latest security patches to mitigate risks.

- **Why it matters**: Fixes known vulnerabilities and prevents attackers from exploiting outdated software.

## 9. Use LockOutRealm to prevent brute-force attacks

The **LockOutRealm** helps prevent brute-force login attacks by locking out users after a set number of failed login attempts. Configuring this for admin accounts is particularly important.

- **Why it matters**: Prevents attackers from brute-forcing admin credentials.

**How-to**

* Edit the `server.xml` file:
```shell
sudo nano /opt/tomcat/conf/server.xml
```

* Add `failureCount` et `lockOutTime`:
```xml
<Realm className="org.apache.catalina.realm.LockOutRealm" failureCount="5" lockOutTime="600">
  <Realm className="org.apache.catalina.realm.UserDatabaseRealm"
         resourceName="UserDatabase"/>
</Realm>
```

* Restart Tomcat:
```shell
sudo nano /opt/tomcat/conf/server.xml
```

## 10. Secure the keystore file

The keystore contains sensitive information, such as private keys used for SSL/TLS. Ensuring that the keystore file is properly protected by strict file permissions and password protection prevents unauthorized access to critical security credentials.

- **Why it matters**: Protects sensitive private keys from unauthorized access or tampering.

**How-to**

* Apply the modifications:
```shell
sudo chmod 600 /path/to/keystore
sudo chown tomcat:tomcat /path/to/keystore
```

## 11. Audit file changes to critical directories

Changes to **$CATALINA_HOME/lib**, **$CATALINA_HOME/bin**, and **$CATALINA_BASE/conf** should be logged. Auditing these changes provides insight into potential tampering or unauthorized modifications to critical Tomcat files.

- **Why it matters**: Helps in detecting suspicious or unauthorized modifications that could compromise the server.

**How-to**

* Add the auditing rules:
```shell
sudo apt install auditd
```

* Add the auditing rules:
```shell
sudo auditctl -w /opt/tomcat/lib -p wa -k tomcat_lib_changes
sudo auditctl -w /opt/tomcat/bin -p wa -k tomcat_bin_changes
sudo auditctl -w /opt/tomcat/conf -p wa -k tomcat_conf_changes
sudo auditctl -l
```

* View logs:
```shell
sudo ausearch -k tomcat_lib_changes
```

## 12. Remove unnecessary applications and connectors

- **Remove folders**: Unused folders such as `documentation`, `examples`, `manager`, and `host-manager` should be deleted from the production environment to minimize the attack surface.
- **Remove unused connectors**: Disable and remove any unused connectors (e.g., HTTP connector) to reduce exposure to attacks.
- **Why it matters**: Minimizes the attack surface and reduces potential entry points for attackers.

**How-to**

* Remove example apps and manager apps:
```shell
sudo rm -rf /opt/tomcat/webapps/examples
sudo rm -rf /opt/tomcat/webapps/manager
sudo rm -rf /opt/tomcat/webapps/host-manager
```

* Disable unused connectors in `server.xml`:
```shell
sudo nano /opt/tomcat/conf/server.xml
```

* Comment out or remove unused connectors.

## 13. Log client IP behind proxies or load balancers

When Tomcat is behind a proxy or load balancer, it’s important to log the client’s actual IP address, not the proxy’s IP. This helps identify the true origin of requests.

- **Why it matters**: Enables accurate identification of the source of traffic, which is crucial for auditing and security monitoring.

**How-to**

* Edit `server.xml` to add `RemoteIpValve` within the `<Host>` section:
```shell
sudo nano /opt/tomcat/conf/server.xml
```

* Add the valve:
```xml
<Valve className="org.apache.catalina.valves.RemoteIpValve"
       remoteIpHeader="x-forwarded-for"
       protocolHeader="x-forwarded-proto"
       protocolHeaderHttpsValue="https" />
```

## 14. Disable auto-deployment and stack tracing

- **AutoDeploy** allows dynamic deployment of new applications while Tomcat is running, which can lead to unauthorized changes or the introduction of vulnerabilities. Disabling it ensures that deployments are controlled and reviewed.
- **Disable stack tracing**: Disabling stack traces prevents the disclosure of internal application details during errors, protecting sensitive information.
- **Why it matters**: Ensures controlled deployments and prevents information leakage.

**How-to**

* Edit `server`:
```shell
sudo nano /opt/tomcat/conf/server.xml
```

* Modify the `<Host>` block to disable `autoDeploy` and `deployOnStartup`:
```xml
<Host name="localhost"  appBase="webapps" unpackWARs="true" autoDeploy="false" deployOnStartup="false">
```

* Disable stack traces in error pages (`web.xml`):
```xml
<error-page>
		<exception-type>java.lang.Throwable</exception-type>
		<location>/error.jsp</location> 
</error-page>
```

## 15. Harden the shutdown port

The shutdown port allows Tomcat to be stopped remotely. Disabling this port or restricting access ensures that attackers cannot shut down your server unexpectedly.

- **Why it matters**: Prevents unauthorised shutdown of Tomcat, ensuring continuous availability.

**How-to**

* Edit `server.xml`:
```shell
sudo nano /opt/tomcat/conf/server.xml
```

* Set the shutdown port to `-1` to disable it:
```xml
<Server port="-1" shutdown="SHUTDOWN">
```

---

## Additional Recommendations

1. **Replace the ROOT web application**: It is a best practice to replace the default Tomcat ROOT web application with a custom one or remove it if not needed. The default ROOT application can expose unnecessary information.
2. **Use a Java Security Manager**: The Java Security Manager enforces restrictions on the permissions of Java code running within Tomcat. Enabling it adds an additional layer of security by limiting what code can do.
3. **Set `failureCount` to 5 failed login attempts** for admin accounts: This limits the number of attempts to brute-force credentials, protecting admin accounts from attacks.

## Sources
* https://www.stigviewer.com/stig/apache_tomcat_application_server_9/
* https://nvlpubs.nist.gov/nistpubs/SpecialPublications/NIST.SP.800-131Ar2.pdf
* https://tomcat.apache.org/tomcat-9.0-doc/security-howto.html
* https://www.openlogic.com/blog/apache-tomcat-security-best-practices
* https://knowledge.broadcom.com/external/article/226769/enable-http-strict-transport-security-hs.html*