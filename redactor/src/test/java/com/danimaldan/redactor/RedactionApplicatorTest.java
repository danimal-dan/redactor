package com.danimaldan.redactor;

import lombok.Value;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RedactionApplicatorTest {
    private static final RedactionApplicator.HasAuthorityPredicate ALWAYS_PASS_HAS_AUTHORITY_PREDICATE = authority -> true;

    @Test
    void redact_removesUnauthorizedSimpleProp() {
        @Value
        class User {
            String username;

            @RedactAuthorize("password:read")
            Redactable<String> password;
        }

        String username = "bananas";
        var user = new User(username, Redactable.of("foster"));

        var applicator = new RedactionApplicator(user);

        applicator.redact();

        assertEquals(username, user.getUsername());
        assertNull(user.getPassword().getValue());
        assertTrue(user.getPassword().isRedacted());
    }

    @Test
    void redact_authorizedSimplePropPassesThroughUntouched() {
        @Value
        class User {
            String username;

            @RedactAuthorize("password:read")
            Redactable<String> password;
        }

        String username = "bananas";
        String password = "foster";
        var user = new User(username, Redactable.of(password));

        var applicator = new RedactionApplicator(user, ALWAYS_PASS_HAS_AUTHORITY_PREDICATE);

        applicator.redact();

        assertEquals(username, user.getUsername());
        assertEquals(password, user.getPassword().getValue());
        assertFalse(user.getPassword().isRedacted());
    }

    @Test
    void redact_redactNestedProp() {
        @Value
        class EmploymentContract {
            String company;

            @RedactAuthorize("employment-contract:salary:read")
            Redactable<Double> salary;
        }

        @Value
        class Employee {
            String name;

            @RedactAuthorize
            EmploymentContract contract;
        }

        String name = "John Doe";
        String companyName = "ACME";
        double salary = 100_000.0;
        var contract = new EmploymentContract(companyName, Redactable.of(salary));
        var employee = new Employee(name, contract);

        var applicator = new RedactionApplicator(employee);

        applicator.redact();

        assertEquals(name, employee.getName());
        assertEquals(companyName, employee.getContract().getCompany());
        assertNull(employee.getContract().getSalary().getValue());
        assertTrue(employee.getContract().getSalary().isRedacted());
    }

    @Test
    void redact_redactToplevelProp() {
        @Value
        class EmploymentContract {
            String company;

            @RedactAuthorize("employment-contract:salary:read")
            Redactable<Double> salary;
        }

        @Value
        class Employee {
            String name;

            @RedactAuthorize("employment-contract:read")
            Redactable<EmploymentContract> contract;
        }

        String name = "John Doe";
        String companyName = "ACME";
        double salary = 100_000.0;
        var contract = new EmploymentContract(companyName, Redactable.of(salary));
        var employee = new Employee(name, Redactable.of(contract));

        var applicator = new RedactionApplicator(employee);

        applicator.redact();

        assertEquals(name, employee.getName());
        assertTrue(employee.getContract().isRedacted());
        assertNull(employee.getContract().getValue());
    }

    @Test
    void redact_allowToplevelRedactableButRedactAPropertyOfIt() {
        @Value
        class EmploymentContract {
            String company;

            @RedactAuthorize("employment-contract:salary:read")
            Redactable<Double> salary;
        }

        @Value
        class Employee {
            String name;

            @RedactAuthorize("employment-contract:read")
            Redactable<EmploymentContract> contract;
        }

        String name = "John Doe";
        String companyName = "ACME";
        double salary = 100_000.0;
        var contract = new EmploymentContract(companyName, Redactable.of(salary));
        var employee = new Employee(name, Redactable.of(contract));

        var applicator = new RedactionApplicator(employee, authority -> "employment-contract:read".equals(authority));

        applicator.redact();

        assertEquals(name, employee.getName());
        assertEquals(companyName, employee.getContract().getValue().getCompany());
        assertFalse(employee.getContract().isRedacted());
        assertNull(employee.getContract().getValue().getSalary().getValue());
        assertTrue(employee.getContract().getValue().getSalary().isRedacted());
    }
}
