---
title: Integrate Extract in your network
---
 
## Web interface - Internal vs exposed
 
Extract _can_ be exposed to the Internet.
But if all Operators and Administrators are on your internal network, Extract can resides on your LAN and does not need to be exposed on the Internet.
 
## Connectors (Get order and send results)
 
To get orders and return responses with files, extract uses "Connectors".
The default connector (easySDI v4) is used to interact with [viageo.ch API](https://viageo.ch/api/legacyOrder/doc){target="_blank"}.
The connector uses `HTTP GET` to get orders, and `HTTP POST` to sent responses with files and remarks. It always acts as an HTTP client, and does not need to be exposed to internet.
Authentication is done by BASIC AUTH. All traffic to viageo is TLS encrypted.
 
## Use network shares
 
If you store files on network drives (Scripts, archive files, etc..) you must run Tomcat service with a user that has access rights on the desired share.
See [Tomcat user access rights](./configure.md#tomcat-user-access-rights)