package com.danimaldan.redactor.applicator;

import com.danimaldan.redactor.RedactAuthorize;
import com.danimaldan.redactor.Redactable;
import lombok.Value;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class RedactionCollectionApplicatorTest {
    private static final HasAuthorityPredicate ALWAYS_PASS_HAS_AUTHORITY_PREDICATE = authority -> true;
    private static final HasAuthorityPredicate NOTHING_PASSES_HAS_AUTHORITY_PREDICATE = authority -> false;

    @Test
    void redact_removesUnauthorizedSimplePropFromList() {
        var user1 = new User("bananas", Redactable.of("foster"));
        var user2 = new User("eggs", Redactable.of("benedict"));

        var users = List.of(user1.copy(), user2.copy());

        var applicator = new RedactionCollectionApplicator<>(users, NOTHING_PASSES_HAS_AUTHORITY_PREDICATE);

        applicator.redact();

        assertEquals(2, users.size());

        var user1Redacted = users.get(0);
        assertEquals(user1.getUsername(), user1Redacted.getUsername());
        assertNull(user1Redacted.getPassword().getValue());
        assertTrue(user1Redacted.getPassword().isRedacted());

        var user2Redacted = users.get(1);
        assertEquals(user2.getUsername(), user2Redacted.getUsername());
        assertNull(user2Redacted.getPassword().getValue());
        assertTrue(user2Redacted.getPassword().isRedacted());
    }

    @Test
    void redact_authorizedSimplePropPassesThroughUntouched() {
        var user1 = new User("bananas", Redactable.of("foster"));
        var user2 = new User("eggs", Redactable.of("benedict"));

        var users = List.of(user1.copy(), user2.copy());

        var applicator = new RedactionCollectionApplicator<>(users, ALWAYS_PASS_HAS_AUTHORITY_PREDICATE);

        applicator.redact();

        var user1Redacted = users.get(0);
        assertEquals(user1.getUsername(), user1Redacted.getUsername());
        assertEquals(user1.getPassword().getValue(), user1Redacted.getPassword().getValue());
        assertFalse(user1Redacted.getPassword().isRedacted());

        var user2Redacted = users.get(1);
        assertEquals(user2.getUsername(), user2Redacted.getUsername());
        assertEquals(user1.getPassword().getValue(), user1Redacted.getPassword().getValue());
        assertFalse(user2Redacted.getPassword().isRedacted());
    }

    @Test
    void redact_redactNestedArrayProp() {
        var contract = new EmploymentContract("ACME", Redactable.of(100_000.0));
        var employee = new Employee("John Doe", List.of(contract));
        var employeeRedacted = employee.copy();

        var applicator = new RedactionObjectApplicator(employeeRedacted, NOTHING_PASSES_HAS_AUTHORITY_PREDICATE);

        applicator.redact();

        assertEquals(employee.getName(), employeeRedacted.getName());

        var contractRedacted = CollectionUtils.getOnlyElement(employeeRedacted.getContracts());
        assertEquals(contract.getCompany(), contractRedacted.getCompany());
        assertNull(contractRedacted.getSalary().getValue());
        assertTrue(contractRedacted.getSalary().isRedacted());
    }

    @Test
    void redact_redactToplevelListProp() {
        var contract = new EmploymentContract("ACME", Redactable.of(100_000.0));
        var employee = new EmployeeRedactableContract("John Doe", Redactable.of(List.of(contract)));
        var employeeRedacted = employee.copy();

        var applicator = new RedactionObjectApplicator(employeeRedacted, NOTHING_PASSES_HAS_AUTHORITY_PREDICATE);

        applicator.redact();

        assertEquals(employee.getName(), employeeRedacted.getName());
        assertTrue(employeeRedacted.getContracts().isRedacted());
        assertNull(employeeRedacted.getContracts().getValue());
    }

    @Test
    void redact_allowToplevelRedactableButRedactAPropertyOfIt() {
        var contract = new EmploymentContract("ACME", Redactable.of(100_000.0));
        var employee = new EmployeeRedactableContract("John Doe", Redactable.of(List.of(contract)));
        var employeeRedacted = employee.copy();

        var applicator = new RedactionObjectApplicator(employeeRedacted, "employment-contract:read"::equals);

        applicator.redact();

        assertEquals(employee.getName(), employeeRedacted.getName());

        assertFalse(employee.getContracts().isRedacted());
        var contractRedacted = CollectionUtils.getOnlyElement(employeeRedacted.getContracts().getValue());
        assertEquals(contract.getCompany(), contractRedacted.getCompany());
        assertNull(contractRedacted.getSalary().getValue());
        assertTrue(contractRedacted.getSalary().isRedacted());
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
        List<EmploymentContract> contracts;

        public Employee copy() {
            var contractCopies = contracts.stream()
                    .map(EmploymentContract::copy)
                    .collect(Collectors.toList());

            return new Employee(name, contractCopies);
        }
    }

    @Value
    private static class EmployeeRedactableContract {
        String name;

        @RedactAuthorize("employment-contract:read")
        Redactable<List<EmploymentContract>> contracts;

        public EmployeeRedactableContract copy() {
            var contractCopies = contracts.getValue().stream()
                    .map(EmploymentContract::copy)
                    .collect(Collectors.toList());

            return new EmployeeRedactableContract(name, Redactable.of(contractCopies));
        }
    }
}
