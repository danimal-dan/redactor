package com.danimaldan.redactor.domain;

import com.danimaldan.redactor.RedactAuthorize;
import com.danimaldan.redactor.Redactable;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class EmploymentDetail {
    String company;

    LocalDate startDate;

    @RedactAuthorize("employment:salary:read")
    Redactable<Double> salary;
}
