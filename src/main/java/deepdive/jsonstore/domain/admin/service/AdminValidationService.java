package deepdive.jsonstore.domain.admin.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import deepdive.jsonstore.domain.admin.exception.AdminException;
import deepdive.jsonstore.domain.admin.entity.Admin;
import deepdive.jsonstore.domain.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminValidationService {

	private final AdminRepository adminRepository;

	public Admin getAdminById(UUID uid) {
		return adminRepository.findByUidAndDeletedIsFalse(uid).orElseThrow(AdminException.AdminNotFoundException::new);
	}
}
