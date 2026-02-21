package model;

import java.util.List;

public class Section {
    private final String sectionName;
    private final int numberOfEmployees;
    private final List<Employee> employees;

    public Section(String sectionName, List<Employee> employees) {
        this.sectionName = sectionName;
        this.numberOfEmployees = employees.size();
        this.employees = employees;
        validateNumberOfEmployees(numberOfEmployees);
    }

    private void validateNumberOfEmployees(int numberOfEmployees) throws IllegalArgumentException {
        if (numberOfEmployees <= 0) {
            throw new IllegalArgumentException("Liczba pracowników musi być większa niż 0.");
        }
    }
}
