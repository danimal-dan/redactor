package com.danimaldan.redactor.domain;

import com.danimaldan.redactor.Redact;
import com.danimaldan.redactor.Redactable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class UserService {
    @Redact
    public User getUser() {
        var driversLicense = DriversLicense.builder()
                .driversLicenseNumber("123456")
                .issuingState("SC")
                .build();

        var employmentDetail = EmploymentDetail.builder()
                .company("ACME")
                .startDate(LocalDate.now())
                .salary(Redactable.of(120_000.0))
                .build();

        return User.builder()
                .name("John Doe")
                .password(Redactable.of("catsaredumb"))
                .driversLicense(Redactable.of(driversLicense))
                .emailAddress("johndoe@acme.com")
                .employmentDetails(employmentDetail)
                .build();
    }
}
