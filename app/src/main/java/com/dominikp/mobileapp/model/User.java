package com.dominikp.mobileapp.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private String email;
    private String createdAt;
    private String displayName;

}
