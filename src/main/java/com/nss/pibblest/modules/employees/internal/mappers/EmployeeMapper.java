package com.nss.pibblest.modules.employees.internal.mappers;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.nss.pibblest.modules.employees.api.dto.EmployeeDto;
import com.nss.pibblest.modules.employees.internal.infrastructure.data.EmployeeEntity;
import com.nss.pibblest.modules.employees.internal.infrastructure.data.EmployeeStoreEntity;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(target = "storeIds", source = "employeeStores", qualifiedByName = "mapStoreIds")
    EmployeeDto toDto(EmployeeEntity entity);

    @Named("mapStoreIds")
    default List<Long> mapStoreIds(List<EmployeeStoreEntity> employeeStores) {
        if (employeeStores == null) return List.of();
        return employeeStores.stream()
                .filter(EmployeeStoreEntity::isActive)
                .map(es -> es.getStore().getId())
                .collect(Collectors.toList());
    }
}
