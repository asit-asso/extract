import ch.asit_asso.extract.connectors.common.IConnector;
import ch.asit_asso.extract.plugins.common.ITaskProcessor;

module ch.asit_asso.extract.core {
    uses IConnector;
    uses ITaskProcessor;

    requires ch.asit_asso.extract.commonInterface;

    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.google.gson;
    requires jakarta.mail;
    requires jakarta.xml.bind;
    requires java.scripting;
    requires java.transaction;
    requires java.validation;
    requires nz.net.ultraq.thymeleaf.layoutdialect;
    requires org.apache.commons.lang3;
    requires org.apache.tomcat.embed.core;
    requires org.hibernate.orm.core;
    requires org.locationtech.jts;
    requires org.joda.time;
    requires org.slf4j;
    requires spring.batch.core;
    requires spring.batch.infrastructure;
    requires spring.beans;
    requires spring.boot.autoconfigure;
    requires spring.boot;
    requires spring.context;
    requires spring.core;
    requires spring.data.jpa;
    requires spring.data.commons;
    requires spring.ldap.core;
    requires spring.security.config;
    requires spring.security.core;
    requires spring.security.crypto;
    requires spring.security.ldap;
    requires spring.security.web;
    requires spring.tx;
    requires spring.web;
    requires spring.webmvc;
    requires thymeleaf.extras.springsecurity4;
    requires thymeleaf.spring.data.dialect;
    requires thymeleaf.spring5;
    requires thymeleaf;
    requires java.persistence;
    requires org.jetbrains.annotations;
    requires java.desktop;
    requires micrometer.core;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires org.apache.commons.validator;
    requires org.seleniumhq.selenium.json;
}
