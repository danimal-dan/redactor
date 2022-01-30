package com.danimaldan.redactor.domain;

import com.danimaldan.redactor.RedactAuthorize;
import com.danimaldan.redactor.Redactable;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class User {
    String name;

    @RedactAuthorize("user:password:read")
    Redactable<String> password;

    @RedactAuthorize("user:DriversLicense:read")
    Redactable<DriversLicense> driversLicense;

    String emailAddress;

    @RedactAuthorize
    EmploymentDetail employmentDetails;
}
