package com.redhat.services.ninja.user;

import java.util.Map;
import java.util.function.Function;

public class RedHatUser {
    private String uid;
    private String name;
    private String country;
    private String employeeId;
    private String mail;
    private String mobile;
    private String geo;
    private String costCenter;
    private String costCenterDescription;
    private String jobTitle;
    private String title;
    private String location;
    private String hireDate;
    private String jobCode;

    public static Function<Map<String, String>, RedHatUser> newMapper() {
        return RED_HAT_USER_MAPPER;
    }

    private RedHatUser() {
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getMail() {
        return mail;
    }

    public String getMobile() {
        return mobile;
    }

    public String getGeo() {
        return geo;
    }

    public String getCostCenter() {
        return costCenter;
    }

    public String getCostCenterDescription() {
        return costCenterDescription;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public String getHireDate() {
        return hireDate;
    }

    public String getJobCode() {
        return jobCode;
    }

    private static final Function<Map<String, String>, RedHatUser> RED_HAT_USER_MAPPER = map -> {
        RedHatUser user = new RedHatUser();

        user.uid = map.getOrDefault("uid", "");
        user.name = map.getOrDefault("cn", "");
        user.country = map.getOrDefault("co", "");
        user.employeeId = map.getOrDefault("employeeNumber", "");
        user.mail = map.getOrDefault("mail", "");
        user.mobile = map.getOrDefault("mobile", "");
        user.geo = map.getOrDefault("rhatLocation", "");
        user.costCenter = map.getOrDefault("rhatCostCenter", "");
        user.costCenterDescription = map.getOrDefault("rhatCostCenterDesc", "");
        user.jobTitle = map.getOrDefault("rhatJobTitle", "");
        user.title = map.getOrDefault("title", "");
        user.location = map.getOrDefault("rhatGeo", "");
        user.hireDate = map.getOrDefault("rhatHireDate", "");
        user.jobCode = map.getOrDefault("rhatJobCode", "");
        
        return user;
    };
}
