package com.nss.pibblest.modules.groups.internal.core;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.nss.pibblest.modules.groups.internal.infrastructure.data.GroupEntity;
import com.nss.pibblest.modules.groups.internal.infrastructure.data.GroupRepository;
import com.nss.pibblest.modules.groups.internal.web.requests.createGroup.CreateGroupRequest;
import com.nss.pibblest.modules.groups.internal.web.requests.createGroup.CreateGroupResponse;

@Service
public class GroupService {
    
    private final GroupRepository groupRepository;

    public GroupService(GroupRepository groupRepository){
        this.groupRepository = groupRepository;
    }

    public ResponseEntity<CreateGroupResponse> createGroup(CreateGroupRequest request){

        GroupEntity groupEntityRequest = new GroupEntity(request.getName());

       GroupEntity newGroupEntity =  groupRepository.save(groupEntityRequest);

       CreateGroupResponse response = new CreateGroupResponse(groupRepository.findAll());

       return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

}
