package com.nss.pibblest.modules.employees.internal.core;

import java.security.SecureRandom;
import java.text.Normalizer;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nss.pibblest.modules.employees.internal.infrastructure.data.EmployeeEntity;
import com.nss.pibblest.modules.employees.internal.infrastructure.data.EmployeeRepository;
import com.nss.pibblest.modules.employees.internal.web.requests.createEmployee.CreateEmployeeRequest;
import com.nss.pibblest.modules.employees.internal.web.requests.createEmployee.CreateEmployeeResponse;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;

    private static final SecureRandom random = new SecureRandom();
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";

    public EmployeeService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder, MessageSource messageSource) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.messageSource = messageSource;
    }

    public ResponseEntity<CreateEmployeeResponse> createEmployee(CreateEmployeeRequest request) {
        String baseUsername = generateBaseUserName(request.getName(), request.getLastName());
        String finalUsername = ensureUniqueUserName(baseUsername);
        String plainPassword = generateRandomPassword(10);
        
        EmployeeEntity employeeEntity = new EmployeeEntity();
        employeeEntity.setUsername(finalUsername);
        employeeEntity.setPassword(plainPassword);

        EmployeeEntity savedEmployee = employeeRepository.save(employeeEntity);

        CreateEmployeeResponse response = new CreateEmployeeResponse(messageSource.getMessage("employee.created", new Object[]{savedEmployee.getUsername()} ,
        LocaleContextHolder.getLocale()));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private String generateBaseUserName(String name, String lastName){
        String firstName = name.trim().split("\\s+")[0];

        String firstLastName = lastName.trim().split("\\s+")[0];

        String rawUserName = firstName.substring(0,1) + firstLastName;

        return stripAccents(rawUserName).toLowerCase();
    }

    private String ensureUniqueUserName(String baseUsername){
        String username = baseUsername;
        int counter = 1;

        while(employeeRepository.existsByUsername(username)){
            username = baseUsername+counter;
            counter++;
        }

        return username;
    }


    private String stripAccents(String str){
        str = Normalizer.normalize(str, Normalizer.Form.NFD);

        str = str.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");

        return str.replaceAll("[^a-zA-Z0-9]","");
    }


    private String generateRandomPassword(int length){
        StringBuilder sb = new StringBuilder(length);

        for(int i = 0; i< length; i++){
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));

        }

        return sb.toString();
    }
}
