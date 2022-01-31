package com.danimaldan.redactor.applicator;

import com.danimaldan.redactor.RedactAuthorize;
import com.danimaldan.redactor.Redactable;
import lombok.Value;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RedactionObjectApplicatorTest {
    private static final HasAuthorityPredicate ALWAYS_PASS_HAS_AUTHORITY_PREDICATE = authority -> true;
    private static final HasAuthorityPredicate NOTHING_PASSES_HAS_AUTHORITY_PREDICATE = authority -> false;

    @Test
    void redact_removesUnauthorizedSimpleProp() {
        var user = new User("bananas", Redactable.of("foster"));
        var userRedacted = user.copy();

        var applicator = new RedactionObjectApplicator(userRedacted, NOTHING_PASSES_HAS_AUTHORITY_PREDICATE);

        applicator.redact();

        assertEquals(user.getUsername(), userRedacted.getUsername());
        assertNull(userRedacted.getPassword().getValue());
        assertTrue(userRedacted.getPassword().isRedacted());
    }

    @Test
    void redact_authorizedSimplePropPassesThroughUntouched() {
        var user = new User("bananas", Redactable.of("foster"));
        var userRedacted = user.copy();

        var applicator = new RedactionObjectApplicator(userRedacted, ALWAYS_PASS_HAS_AUTHORITY_PREDICATE);

        applicator.redact();

        assertEquals(user.getUsername(), userRedacted.getUsername());
        assertEquals(user.getPassword().getValue(), userRedacted.getPassword().getValue());
        assertFalse(userRedacted.getPassword().isRedacted());
    }

    @Test
    void redact_redactNestedProp() {
        var contract = new EmploymentContract("ACME", Redactable.of(100_000.0));
        var employee = new Employee("John Doe", contract);
        var employeeRedacted = employee.copy();

        var applicator = new RedactionObjectApplicator(employeeRedacted, NOTHING_PASSES_HAS_AUTHORITY_PREDICATE);

        applicator.redact();

        assertEquals(employee.getName(), employeeRedacted.getName());
        assertEquals(contract.getCompany(), employeeRedacted.getContract().getCompany());
        assertNull(employeeRedacted.getContract().getSalary().getValue());
        assertTrue(employeeRedacted.getContract().getSalary().isRedacted());
    }

    @Test
    void redact_redactToplevelProp() {
        var contract = new EmploymentContract("ACME", Redactable.of(100_000.0));
        var employee = new EmployeeRedactableContract("John Doe", Redactable.of(contract));
        var employeeRedacted = employee.copy();

        var applicator = new RedactionObjectApplicator(employeeRedacted, NOTHING_PASSES_HAS_AUTHORITY_PREDICATE);

        applicator.redact();

        assertEquals(employee.getName(), employeeRedacted.getName());
        assertTrue(employeeRedacted.getContract().isRedacted());
        assertNull(employeeRedacted.getContract().getValue());
    }

    @Test
    void redact_allowToplevelRedactableButRedactAPropertyOfIt() {
        var contract = new EmploymentContract("ACME", Redactable.of(100_000.0));
        var employee = new EmployeeRedactableContract("John Doe", Redactable.of(contract));
        var employeeRedacted = employee.copy();

        var applicator = new RedactionObjectApplicator(employeeRedacted, "employment-contract:read"::equals);

        applicator.redact();

        assertEquals(employee.getName(), employeeRedacted.getName());
        assertEquals(contract.getCompany(), employeeRedacted.getContract().getValue().getCompany());
        assertFalse(employeeRedacted.getContract().isRedacted());
        assertNull(employeeRedacted.getContract().getValue().getSalary().getValue());
        assertTrue(employeeRedacted.getContract().getValue().getSalary().isRedacted());
    }

    @Value
    private static class User {
        String username;

        @RedactAuthorize("password:read")
        Redactable<String> password;

        public User copy() {
            return new User(username, Redactable.of(password.getValue()));
        }
    }

    @Value
    private static class EmploymentContract {
        String company;

        @RedactAuthorize("employment-contract:salary:read")
        Redactable<Double> salary;

        public EmploymentContract copy() {
            return new EmploymentContract(company, Redactable.of(salary.getValue()));
        }
    }

    @Value
    private static class Employee {
        String name;

        @RedactAuthorize
        EmploymentContract contract;

        public Employee copy() {
            return new Employee(name, contract.copy());
        }
    }

    @Value
    private static class EmployeeRedactableContract {
        String name;

        @RedactAuthorize("employment-contract:read")
        Redactable<EmploymentContract> contract;

        public EmployeeRedactableContract copy() {
            return new EmployeeRedactableContract(name, Redactable.of(contract.getValue().copy()));
        }
    }
}
