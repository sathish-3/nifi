package com.nifi.geolocation.processors.geolocation;

public class IPAddressInfo {
    private String ipAddress;
    private String CountryName;
    private String appliance_name;
    private String timestamp;
    private String applianceid;
    private String src_port;
    private String dst_port;
    private String tcp_flags;
    private String profile_name;
    private String dst_ip;
    private String protocol;
    private String bps;
    private String pps;
    private String profileid;
    private String appliance_total_pps;
    private String appliance_total_bps;
    private String countryCode;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getapplianceid() {
        return applianceid;
    }

    public void setapplianceid(String applianceid) {
        this.applianceid = applianceid;
    }

    public String getsrc_port() {
        return src_port;
    }

    public void setsrc_port(String src_port) {
        this.src_port = src_port;
    }

    public String getdst_port() {
        return dst_port;
    }

    public void setdst_port(String dst_port) {
        this.dst_port = dst_port;
    }

    public String gettcp_flags() {
        return tcp_flags;
    }

    public void settcp_flags(String tcp_flags) {
        this.tcp_flags = tcp_flags;
    }

    public String getprofile_name() {
        return profile_name;
    }

    public void setprofile_name(String profile_name) {
        this.profile_name = profile_name;
    }

    public String getdst_ip() {
        return dst_ip;
    }

    public void setdst_ip(String dst_ip) {
        this.dst_ip = dst_ip;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String Protocol) {
        this.protocol = Protocol;
    }

    public String getAppliance_name() {
        return appliance_name;
    }

    public void setAppliance_name(String appliance_name) {
        this.appliance_name = appliance_name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getCountryName() {
        return CountryName;
    }

    public void setCountryName(String CountryName) {
        this.CountryName = CountryName;
    }

    public String getBPS() {
        return bps;
    }

    public void setBPS(String BPS) {
        this.bps = BPS;
    }

    public String getPPS() {
        return pps;
    }

    public void setPPS(String PPS) {
        this.pps = PPS;
    }

    public String getappliance_total_pps() {
        return appliance_total_pps;
    }

    public void setappliance_total_pps(String appliance_total_pps) {
        this.appliance_total_pps = appliance_total_pps;
    }

    public String getprofileid() {
        return profileid;
    }

    public void setprofileid(String profileid) {
        this.profileid = profileid;
    }

    public String getappliance_total_bps() {
        return appliance_total_bps;
    }

    public void setappliance_total_bps(String appliance_total_bps) {
        this.appliance_total_bps = appliance_total_bps;
    }

    public String getcountryCode() {
        return countryCode;
    }

    public void setcountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}
