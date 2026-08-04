package hu.smartinventory.user.service;

import hu.smartinventory.user.dto.RoleResponse;
import hu.smartinventory.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public List<RoleResponse> findAll() {
        return roleRepository.findAllByOrderByIdAsc()
                .stream()
                .map(RoleResponse::from)
                .toList();
    }
}