package com.danimaldan.redactor.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {
    @Autowired
    private UserService userService;

    @Test
    void getUser() {
        var user = userService.getUser();

        assertNotNull(user);
        assertEquals("John Doe", user.getName());
        assertEquals("johndoe@acme.com", user.getEmailAddress());

        assertNull(user.getPassword().getValue());
        assertTrue(user.getPassword().isRedacted());

        assertNull(user.getDriversLicense().getValue());
        assertTrue(user.getDriversLicense().isRedacted());

        var employmentDetails = user.getEmploymentDetails();
        assertNotNull(employmentDetails);
        assertEquals("ACME", employmentDetails.getCompany());
        assertNotNull(employmentDetails.getStartDate());
        assertNull(employmentDetails.getSalary().getValue());
        assertTrue(employmentDetails.getSalary().isRedacted());
    }
}
