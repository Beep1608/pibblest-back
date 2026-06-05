package com.nss.pibblest.modules.employees.internal.mappers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nss.pibblest.modules.employees.api.dto.EmployeeDto;
import com.nss.pibblest.modules.employees.api.dto.ModulePermissionDto;
import com.nss.pibblest.modules.employees.api.dto.StoreAssignmentDto;
import com.nss.pibblest.modules.employees.internal.infrastructure.data.EmployeeEntity;
import com.nss.pibblest.modules.employees.internal.infrastructure.data.EmployeePermissionEntity;
import com.nss.pibblest.modules.employees.internal.infrastructure.data.ModuleEntity;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreEntity;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    // Mapeamos la lista desde la entidad misma usando un método personalizado
    @Mapping(target = "storeAssignments", expression = "java(mapPermissionsToDto(entity))")
    EmployeeDto toDto(EmployeeEntity entity);

    default List<StoreAssignmentDto> mapPermissionsToDto(EmployeeEntity entity) {
        if (entity.getGranularPermissions() == null) return List.of();

        // 1. Agrupar permisos por Tienda
        Map<StoreEntity, List<EmployeePermissionEntity>> byStore = entity.getGranularPermissions().stream()
                .collect(Collectors.groupingBy(EmployeePermissionEntity::getStore));

        return byStore.entrySet().stream().map(storeEntry -> {
            StoreEntity store = storeEntry.getKey();

            // 2. Dentro de la tienda, agrupar por Módulo
            Map<ModuleEntity, List<EmployeePermissionEntity>> byModule = storeEntry.getValue().stream()
                    .collect(Collectors.groupingBy(EmployeePermissionEntity::getModule));

            List<ModulePermissionDto> modules = byModule.entrySet().stream().map(modEntry -> {
                ModuleEntity module = modEntry.getKey();
                
                // 3. Extraer solo la lista de acciones (CREATE, READ, etc.)
                List<String> actions = modEntry.getValue().stream()
                        .map(EmployeePermissionEntity::getAction)
                        .collect(Collectors.toList());
                        
                return new ModulePermissionDto(module.getId(), module.getCode(), actions);
            }).collect(Collectors.toList());

            return new StoreAssignmentDto(store.getId(), store.getName(), modules);
        }).collect(Collectors.toList());
    }
}
