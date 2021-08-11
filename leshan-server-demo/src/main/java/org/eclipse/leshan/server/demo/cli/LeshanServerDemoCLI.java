/*******************************************************************************
 * Copyright (c) 2021 Sierra Wireless and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.leshan.server.demo.cli;

import java.io.File;
import java.net.URI;

import org.eclipse.leshan.core.LwM2m;
import org.eclipse.leshan.core.demo.cli.converters.PortConverter;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Option;
import redis.clients.jedis.JedisPool;

/**
 * This is the class defining the Command Line Interface of Leshan Server Demo.
 */
@Command(name = "leshan-server-demo", sortOptions = false)
public class LeshanServerDemoCLI implements Runnable {

    /* ********************************** General Section ******************************** */
    @ArgGroup(validate = false,
              heading = "%n"//
                      + "@|italic " //
                      + "This is a LWM2M Server demo implemented with Leshan library.%n" //
                      + "You can launch it without any option.%n" //
                      + "%n" //
                      + "Californium is used as CoAP library and some CoAP parameters can be tweaked in 'Californium.properties' file." //
                      + "|@%n%n")
    public GeneralSection main = new GeneralSection();

    public static class GeneralSection {
        @Option(names = { "-h", "--help" }, description = "Display help information.", usageHelp = true)
        private boolean help;

        @Option(names = { "-lh", "--coap-host" },
                description = { //
                        "Set the local CoAP address of the Server.", //
                        "Default: any local address." })
        public String localAddress;

        @Option(names = { "-lp", "--coap-port" },
                description = { //
                        "Set the local CoAP port of the Server.", //
                        "Default: COAP_PORT value from 'Californium.properties' file or ${DEFAULT-VALUE} if absent" },
                converter = PortConverter.class)
        public Integer localPort = LwM2m.DEFAULT_COAP_PORT;

        @Option(names = { "-slh", "--coaps-host" },
                description = { //
                        "Set the secure local CoAP address of the Server.", //
                        "Default: any local address." })
        public String secureLocalAddress;

        @Option(names = { "-slp", "--coaps-port" },
                description = { //
                        "Set the secure local CoAP port of the Server.", //
                        "Default: COAP_SECURE_PORT value from 'Californium.properties' file or ${DEFAULT-VALUE} if absent" },
                converter = PortConverter.class)
        public Integer secureLocalPort = LwM2m.DEFAULT_COAP_SECURE_PORT;

        @Option(names = { "-wh", "--web-host" },
                description = { //
                        "Set the HTTP address for web server.", //
                        "Default: any local address." })
        public String webhost;

        @Option(names = { "-wp", "--web-port" },
                defaultValue = "8080",
                description = { //
                        "Set the HTTP port for web server.", //
                        "Default: ${DEFAULT-VALUE}" },
                converter = PortConverter.class)
        public Integer webPort;

        @Option(names = { "-m", "--models-folder" },
                description = { //
                        "A folder which contains object models in OMA DDF(xml)format." })
        public File modelsFolder;

        @Option(names = { "-r", "--redis" },
                description = { //
                        "Use redis to store registration and securityInfo.", //
                        "The URL of the redis server should be given using this format :", //
                        "     redis://:password@hostname:port/db_number", //
                        "Example without DB and password: ", //
                        "   redis://localhost:6379", //
                        "Default: redis is not used." },
                converter = JedisPoolConverter.class)
        public JedisPool redis;

        private static class JedisPoolConverter implements ITypeConverter<JedisPool> {
            @Override
            public JedisPool convert(String value) throws Exception {
                return new JedisPool(new URI(value));
            }
        }

        @Option(names = { "-mdns", "--publish-DNS-SD-services" },
                description = { //
                        "Publish leshan's services to DNS Service discovery." })
        public Boolean mdns;
    }

    /* ********************************** DTLS Section ******************************** */
    @ArgGroup(validate = false,
              heading = "%n@|bold,underline DTLS Options|@ %n%n"//
                      + "@|italic " //
                      + "Here some options aiming to configure the server behavior when it uses CoAP over DTLS." //
                      + "%n" //
                      + "Scandium is used as DTLS library and some DTLS parameters can be tweaked in 'Californium.properties' file." //
                      + "|@%n%n")
    public DTLSSection dtls = new DTLSSection();

    public static class DTLSSection {

        @Option(names = { "-cid", "--connection-id" },
                defaultValue = "on",
                description = { //
                        "Control usage of DTLS connection ID.", //
                        "- 'on' to activate Connection ID support ", //
                        "  (same as -cid 6)", //
                        "- 'off' to deactivate it", //
                        "- Positive value define the size in byte of CID generated.", //
                        "- 0 value means we accept to use CID but will not generated one for foreign peer.", //
                        "Default: on" },
                converter = CIDConverter.class)
        public Integer cid;

        private static class CIDConverter implements ITypeConverter<Integer> {
            @Override
            public Integer convert(String cid) {
                if ("off".equals(cid)) {
                    return null;
                } else if ("on".equals(cid)) {
                    return 6;
                } else {
                    Integer res = Integer.parseInt(cid);
                    return res < 0 ? null : res;
                }
            }
        };

        @Option(names = { "-oc", "--support-deprecated-ciphers" },
                description = { //
                        "Activate support of old/deprecated cipher suites." })
        public boolean supportDeprecatedCiphers;
    }

    /* ********************************** Identity Section ******************************** */
    @ArgGroup(exclusive = true)
    public IdentitySection identity = new IdentitySection();

    @Override
    public void run() {
        // Some post-validation which imply several options. or input consolidation
        identity.build();
    }
}