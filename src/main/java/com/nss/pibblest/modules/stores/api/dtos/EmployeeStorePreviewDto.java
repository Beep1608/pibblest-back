package com.nss.pibblest.modules.stores.api.dtos;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nss.pibblest.modules.tags.api.dto.TagDto;

public class EmployeeStorePreviewDto {

    private Long id;
    private String name;
    private String address;
    private String status;
    private String operatinTime;

    @JsonIgnore
    private ZonedDateTime createdAt;

    private List<TagDto> tags = new ArrayList<>();

    public EmployeeStorePreviewDto(Long id, String name, String address, String status, ZonedDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.status = status;
        this.createdAt = createdAt;
    }

    public EmployeeStorePreviewDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOperatinTime() { return operatinTime; }
    public void setOperatinTime(String operatinTime) { this.operatinTime = operatinTime; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public List<TagDto> getTags() { return tags; }
    public void setTags(List<TagDto> tags) { this.tags = tags; }
}
