package com.company.repository;

import com.company.dto.DepartmentStats;
import com.company.dto.ManagerStats;
import com.company.dto.ProjectStats;
import com.company.model.Employee;
import com.company.model.Project;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CompanyRepository {

    private final JdbcTemplate jdbcTemplate;

    public CompanyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // --- 테이블 생성 (존재하면 DROP) ---
    public void initializeDatabase() {
        String dropDepartmentsTable = "DROP TABLE IF EXISTS departments";
        String dropEmployeesTable = "DROP TABLE IF EXISTS employees";
        String dropProjectsTable = "DROP TABLE IF EXISTS projects";
        String dropEmployeeProjectsTable = "DROP TABLE IF EXISTS employee_projects";

        String createDepartmentsTable = "CREATE TABLE departments (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(255) NOT NULL" +
                ")";

        String createEmployeesTable = "CREATE TABLE employees (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(255) NOT NULL," +
                "department_id BIGINT," +
                "salary DECIMAL(10, 2)," +
                "manager_id BIGINT," +
                "FOREIGN KEY (department_id) REFERENCES departments(id)," +
                "FOREIGN KEY (manager_id) REFERENCES employees(id)" +
                ")";

        String createProjectsTable = "CREATE TABLE projects (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(255) NOT NULL," +
                "budget DECIMAL(10, 2)" +
                ")";

        String createEmployeeProjectsTable = "CREATE TABLE employee_projects (" +
                "employee_id BIGINT," +
                "project_id BIGINT," +
                "role VARCHAR(255)," +
                "PRIMARY KEY (employee_id, project_id)," +
                "FOREIGN KEY (employee_id) REFERENCES employees(id)," +
                "FOREIGN KEY (project_id) REFERENCES projects(id)" +
                ")";


        jdbcTemplate.execute(dropDepartmentsTable);
        jdbcTemplate.execute(dropEmployeesTable);
        jdbcTemplate.execute(dropProjectsTable);
        jdbcTemplate.execute(dropEmployeeProjectsTable);

        jdbcTemplate.execute(createDepartmentsTable);
        jdbcTemplate.execute(createEmployeesTable);
        jdbcTemplate.execute(createProjectsTable);
        jdbcTemplate.execute(createEmployeeProjectsTable);
    }

    // --- 데이터 삽입 ---
    public void insertDepartment(String name) {
        String sql = "INSERT INTO departments (name) VALUES (?)";
        jdbcTemplate.update(sql, name);
    }

    public void insertEmployee(Employee employee) {
        String sql = "INSERT INTO employees ( name, department_id, salary, manager_id) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, employee.getName(), employee.getDepartmentId(), employee.getSalary(), employee.getManagerId());
    }

    public void insertProject(Project project) {
        String sql = "INSERT INTO projects (name, budget) VALUES (?, ?)";
        jdbcTemplate.update(sql,project.getName(),project.getBudget());
    }

    public void insertEmployeeProject(Long employeeId, Long projectId, String role) {
        //TODO: 구현
    }

    // 1. 급여가 일정 이상인 직원의 이름을 알파벳 순으로 조회
    public List<String> findEmployeeNamesBySalaryGreaterThanEqualOrderedByName(int salary) {
        //TODO: 구현
        return null;
    }

    // 2. 부서별 통계: 부서명, 평균 급여(반올림), 직원 수
    public List<DepartmentStats> findDepartmentStatistics() {
        //TODO: 구현
        return null;
    }

    // 3. 전체 최고 연봉 직원 조회
    public Employee findHighestSalaryEmployee() {
        //TODO: 구현
        return null;
    }

    // 4. 프로젝트별 통계: 프로젝트명, 참여 직원 수, 참여 직원 급여 합계
    public List<ProjectStats> findProjectStatistics() {
        //TODO: 구현
        return null;
    }

    // 5. 부서별 최고 연봉 직원 조회
    public List<Employee> findTopEmployeePerDepartment() {
        //TODO: 구현
        return null;
    }

    // 6. 어떤 프로젝트에도 참여하지 않은 직원 조회 (알파벳 순)
    public List<String> findEmployeesNotInAnyProject() {
        //TODO: 구현
        return null;
    }

    // 7. 매니저 통계: 자신을 상사로 갖는 직원 수 집계 (매니저 이름, 부하 수)
    public List<ManagerStats> findManagerStatistics() {
        //TODO: 구현
        return null;
    }
}
