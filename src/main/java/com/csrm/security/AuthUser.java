package com.csrm.security;

import com.csrm.dto.Enums;

public record AuthUser(Long id, String username, Enums.Role role) {}
