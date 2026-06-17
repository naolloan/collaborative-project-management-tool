package com.collabpm.backend.auth;

import com.collabpm.backend.auth.dto.EmployeeDirectoryEntryResponse;
import com.collabpm.backend.user.User;
import com.collabpm.backend.user.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class EmployeeDirectoryController {

    private final UserRepository userRepository;

    public EmployeeDirectoryController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/employees")
    public List<EmployeeDirectoryEntryResponse> listEmployees() {
        return userRepository.findAll().stream()
            .sorted(Comparator.comparing(User::getFullName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(User::getEmail, String.CASE_INSENSITIVE_ORDER))
            .map(user -> new EmployeeDirectoryEntryResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getSystemRole().name()))
            .toList();
    }
}
