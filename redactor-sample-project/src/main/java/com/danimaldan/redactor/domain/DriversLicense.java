package com.danimaldan.redactor.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DriversLicense {
    String driversLicenseNumber;
    String issuingState;
}
