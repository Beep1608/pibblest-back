package com.nss.pibblest.modules.groups.internal.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nss.pibblest.modules.groups.internal.core.GroupService;
import com.nss.pibblest.modules.groups.internal.web.requests.createGroup.CreateGroupRequest;
import com.nss.pibblest.modules.groups.internal.web.requests.createGroup.CreateGroupResponse;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/groups")
public class GroupController {
    
    private final GroupService groupService;

    public GroupController(GroupService groupService){
        this.groupService = groupService;
    }

    @PostMapping()
    public ResponseEntity<CreateGroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request){
        
        return groupService.createGroup(request);
    }


}
