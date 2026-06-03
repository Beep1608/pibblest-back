package com.nss.pibblest.modules.tags.api.dto;

public class TagForProductDto {
    private String name; 
    private Long id;
    private Long usageCount; // Nuevo campo

    public TagForProductDto() {}

    public TagForProductDto(String name, Long id) {
        this.name = name;
        this.id = id;
        this.usageCount = 0L;
    }

    public TagForProductDto(String name, Long id, Long usageCount) {
        this.name = name;
        this.id = id;
        this.usageCount = usageCount != null ? usageCount : 0L;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUsageCount() { return usageCount; }
    public void setUsageCount(Long usageCount) { this.usageCount = usageCount; }
}
