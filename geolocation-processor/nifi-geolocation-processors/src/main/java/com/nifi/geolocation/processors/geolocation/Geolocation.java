/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nifi.geolocation.processors.geolocation;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.io.*;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;

import com.maxmind.geoip2.DatabaseReader;
import org.apache.nifi.components.resource.ResourceCardinality;
import org.apache.nifi.components.resource.ResourceType;
import org.apache.nifi.expression.ExpressionLanguageScope;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicReference;
import com.google.gson.Gson;
import java.util.*;
import java.io.*;

@Tags({ "geolocation,ip,maxmind" })
@CapabilityDescription("Provide a description")
@SeeAlso({})
@ReadsAttributes({ @ReadsAttribute(attribute = "", description = "") })
@WritesAttributes({ @WritesAttribute(attribute = "", description = "") })

public class Geolocation extends AbstractProcessor {

    public static final PropertyDescriptor GEO_DATABASE_FILE = new PropertyDescriptor.Builder()
            .name("Geo Database File")
            .displayName("MaxMind Database File")
            .description("Path to Maxmind IP Enrichment Database File")
            .required(true)
            .identifiesExternalResource(ResourceCardinality.SINGLE, ResourceType.FILE, ResourceType.DIRECTORY)
            .expressionLanguageSupported(ExpressionLanguageScope.VARIABLE_REGISTRY)
            .build();

    public static final Relationship REL_FOUND = new Relationship.Builder()
            .name("found")
            .description(
                    "Where to route flow files after successfully enriching attributes with data provided by database")
            .build();

    public static final Relationship REL_NOT_FOUND = new Relationship.Builder()
            .name("not found")
            .description(
                    "Where to route flow files after unsuccessfully enriching attributes because no data was found")
            .build();

    private Set<Relationship> relationships;
    private List<PropertyDescriptor> propertyDescriptors;
    final AtomicReference<DatabaseReader> databaseReaderRef = new AtomicReference<>(null);

    private volatile File dbFile;

    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return propertyDescriptors;
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) throws IOException {
        dbFile = context.getProperty(GEO_DATABASE_FILE).evaluateAttributeExpressions().asResource().asFile();
        loadDatabaseFile();
    }

    protected void loadDatabaseFile() throws IOException {
        final DatabaseReader reader = new DatabaseReader.Builder(dbFile).build();
        databaseReaderRef.set(reader);
    }

    @Override
    protected void init(final ProcessorInitializationContext context) {
        final Set<Relationship> rels = new HashSet<>();
        rels.add(REL_FOUND);
        rels.add(REL_NOT_FOUND);
        this.relationships = Collections.unmodifiableSet(rels);

        final List<PropertyDescriptor> props = new ArrayList<>();
        props.add(GEO_DATABASE_FILE);
        this.propertyDescriptors = Collections.unmodifiableList(props);
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) {
        FlowFile flowFile = session.get();

        DatabaseReader dbReader = databaseReaderRef.get();

        session.read(flowFile, new InputStreamCallback() {
            @Override
            public void process(InputStream inputStream) throws IOException {
                String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

                String[] records = content.split("\\r?\\n");
                String[] header = records[0].split(",");

                int appliance_id = Arrays.asList(header).indexOf("appliance_id");
                int time_stamp = Arrays.asList(header).indexOf("timestamp");
                int srcport = Arrays.asList(header).indexOf("src_port");
                int dstport = Arrays.asList(header).indexOf("dst_port");
                int tcpflags = Arrays.asList(header).indexOf("tcp_flags");
                int appliancename = Arrays.asList(header).indexOf("appliance_name");
                int profilename = Arrays.asList(header).indexOf("profile_name");
                int srcip = Arrays.asList(header).indexOf("src_ip");
                int dstip = Arrays.asList(header).indexOf("dst_ip");
                int protocol = Arrays.asList(header).indexOf("protocol");
                int bps = Arrays.asList(header).indexOf("bps");
                int pps = Arrays.asList(header).indexOf("pps");
                int profile_id = Arrays.asList(header).indexOf("profile_id");
                int app_tot_pps = Arrays.asList(header).indexOf("appliance_total_pps");
                int app_tot_bps = Arrays.asList(header).indexOf("appliance_total_bps");

                List<IPAddressInfo> ipAddressList = new ArrayList<>();

                for (String record : records) {
                    String[] fields = record.split(",");

                    String applianceid = fields[appliance_id];
                    String timestamp = fields[time_stamp];
                    String src_port = fields[srcport];
                    String dst_port = fields[dstport];
                    String tcp_flags = fields[tcpflags];
                    String appliance_name = fields[appliancename];
                    String profile_name = fields[profilename];
                    String src_ip = fields[srcip];
                    String dst_ip = fields[dstip];
                    String Protocol = fields[protocol];
                    String BPS = fields[bps];
                    String PPS = fields[pps];
                    String profileid = fields[profile_id];
                    String appliance_total_pps = fields[app_tot_pps];
                    String appliance_total_bps = fields[app_tot_bps];

                    try {
                        InetAddress ipAddress = InetAddress.getByName(src_ip);

                        CityResponse response = dbReader.city(ipAddress);
                        String CountryName = response.getCountry().getName();
                        String countryCode = response.getCountry().getIsoCode();

                        IPAddressInfo ipAddressInfo = new IPAddressInfo();
                        ipAddressInfo.setapplianceid(applianceid);
                        ipAddressInfo.setTimestamp(timestamp);
                        ipAddressInfo.setsrc_port(src_port);
                        ipAddressInfo.setdst_port(dst_port);
                        ipAddressInfo.settcp_flags(tcp_flags);
                        ipAddressInfo.setAppliance_name(appliance_name);
                        ipAddressInfo.setprofile_name(profile_name);
                        ipAddressInfo.setIpAddress(src_ip);
                        ipAddressInfo.setdst_ip(dst_ip);
                        ipAddressInfo.setProtocol(Protocol);
                        ipAddressInfo.setBPS(BPS);
                        ipAddressInfo.setPPS(PPS);
                        ipAddressInfo.setprofileid(profileid);
                        ipAddressInfo.setappliance_total_bps(appliance_total_bps);
                        ipAddressInfo.setappliance_total_pps(appliance_total_pps);
                        ipAddressInfo.setCountryName(CountryName);
                        ipAddressInfo.setcountryCode(countryCode);
                        ipAddressList.add(ipAddressInfo);

                    } catch (IOException | GeoIp2Exception e) {
                        getLogger().error("Error occurred during IP lookup: " + e.getMessage());
                    }
                }

                String ipAddressJson = new Gson().toJson(ipAddressList);

                FlowFile modifiedFlowFile = session.create(flowFile);
                modifiedFlowFile = session.write(modifiedFlowFile, new OutputStreamCallback() {
                    @Override
                    public void process(OutputStream outputStream) throws IOException {
                        outputStream.write(ipAddressJson.getBytes(StandardCharsets.UTF_8));
                    }
                });

                session.transfer(modifiedFlowFile, REL_FOUND);
            }
        });

        session.remove(flowFile);

    }
}