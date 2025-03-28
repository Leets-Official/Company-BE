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
        // 이미 존재하는 테이블 DROP 후 재생성
        jdbcTemplate.execute("DROP TABLE IF EXISTS EmployeeProject");
        jdbcTemplate.execute("DROP TABLE IF EXISTS Employee");
        jdbcTemplate.execute("DROP TABLE IF EXISTS Department");
        jdbcTemplate.execute("DROP TABLE IF EXISTS Project");

        // id는 1부터 시작하는 auto-increment 요소
        // Department 테이블 생성
        jdbcTemplate.execute("""
        CREATE TABLE Department (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            name NVARCHAR(255) NOT NULL
        )
     """);

        // Employee 테이블 생성
        jdbcTemplate.execute("""
        CREATE TABLE Employee (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            name NVARCHAR(255) NOT NULL,
            departmentId BIGINT NOT NULL,
            salary INT NOT NULL,
            managerId BIGINT,
            FOREIGN KEY (departmentId) REFERENCES Department(id),
            FOREIGN KEY (managerId) REFERENCES Employee(id)
        )
    """);

        // Project 테이블 생성
        jdbcTemplate.execute("""
        CREATE TABLE Project (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            name NVARCHAR(255) NOT NULL,
            budget INT NOT NULL
        )
    """);

        // EmployeeProject 테이블 생성
        jdbcTemplate.execute("""
        CREATE TABLE EmployeeProject (
            employeeId BIGINT NOT NULL,
            projectId BIGINT NOT NULL,
            role NVARCHAR(255) NOT NULL,
            PRIMARY KEY (employeeId, projectId),
            FOREIGN KEY (employeeId) REFERENCES Employee(id),
            FOREIGN KEY (projectId) REFERENCES Project(id)
        )
    """);
    }

    // --- 데이터 삽입 ---
    public void insertDepartment(String name) {
        String sql = "INSERT INTO Department (name) VALUES (?)";
        jdbcTemplate.update(sql, name);
    }

    public void insertEmployee(Employee employee) {
        String sql = "INSERT INTO Employee (name, departmentId, salary, managerId) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                employee.getName(),
                employee.getDepartmentId(),
                employee.getSalary(),
                employee.getManagerId());
    }

    public void insertProject(Project project) {
        String sql = "INSERT INTO Project (name, budget) VALUES (?, ?)";
        jdbcTemplate.update(sql, project.getName(), project.getBudget());
    }

    public void insertEmployeeProject(Long employeeId, Long projectId, String role) {
        String sql = "INSERT INTO EmployeeProject (employeeId, projectId, role) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, employeeId, projectId, role);
    }

    // 1. 급여가 일정 이상인 직원의 이름을 알파벳 순으로 조회
    public List<String> findEmployeeNamesBySalaryGreaterThanEqualOrderedByName(int salary) {
        // 연봉이 ? 이상인 Employee 의 name 을 오름차순으로 정렬한 뒤, List 형태로 name 들을 반환
        String sql = "SELECT name FROM Employee WHERE salary >= ? ORDER BY name ASC";
        return jdbcTemplate.queryForList(sql, String.class, salary);
    }

    // 2. 부서별 통계: 부서명, 평균 급여(반올림), 직원 수
    public List<DepartmentStats> findDepartmentStatistics() {
        // ID를 기준으로 Employee(e),Department(d) 테이블 join 후 d.name을 기준으로 그룹화하는 쿼리
        String sql = """
        SELECT d.name AS department,
               ROUND(AVG(e.salary), 0) AS avgSalary,
               COUNT(e.id) AS employeeCount
        FROM Department d
        JOIN Employee e ON e.departmentId = d.id
        GROUP BY d.name
        ORDER BY d.name ASC
    """;

        //여러행을 List<DepartmentStats> 로 반환
        return jdbcTemplate.query(sql, (rs, rowNum) -> new DepartmentStats(
                rs.getString("department"),
                rs.getInt("avgSalary"),
                rs.getInt("employeeCount")
        ));
    }

    // 3. 전체 최고 연봉 직원 조회
    public Employee findHighestSalaryEmployee() {
        String sql = """
        SELECT * FROM Employee
        WHERE salary = (SELECT MAX(salary) FROM Employee)
    """;
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new Employee(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getLong("departmentId"),
                rs.getInt("salary"),
                rs.getLong("managerId")
        ));
    }

    // 4. 프로젝트별 통계: 프로젝트명, 참여 직원 수, 참여 직원 급여 합계
    public List<ProjectStats> findProjectStatistics() {
        String sql = """
        SELECT p.name AS project,
               COUNT(ep.employeeId) AS employeeCount,
               SUM(e.salary) AS totalSalary
        FROM Project p
        JOIN EmployeeProject ep ON ep.projectId = p.id
        JOIN Employee e ON ep.employeeId = e.id
        GROUP BY p.name
    """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ProjectStats(
                rs.getString("project"),
                rs.getInt("employeeCount"),
                rs.getInt("totalSalary")
        ));
    }

    // 5. 부서별 최고 연봉 직원 조회
    public List<Employee> findTopEmployeePerDepartment() {
        String sql = """
        SELECT e.*
        FROM Employee e
        WHERE e.salary = (
            SELECT MAX(salary)
            FROM Employee
            WHERE departmentId = e.departmentId
        )
    """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Employee(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getLong("departmentId"),
                rs.getInt("salary"),
                rs.getLong("managerId")
        ));
    }

    // 6. 어떤 프로젝트에도 참여하지 않은 직원 조회 (알파벳 순)
    public List<String> findEmployeesNotInAnyProject() {
        String sql = """
        SELECT e.name
        FROM Employee e
        WHERE NOT EXISTS (
            SELECT 1
            FROM EmployeeProject ep
            WHERE ep.employeeId = e.id
        )
        ORDER BY e.name
    """;
        return jdbcTemplate.queryForList(sql, String.class);
    }

    // 7. 매니저 통계: 자신을 상사로 갖는 직원 수 집계 (매니저 이름, 부하 수)
    public List<ManagerStats> findManagerStatistics() {
        String sql = """
        SELECT e.name AS manager, COUNT(emp.id) AS subordinateCount
        FROM Employee e
        LEFT JOIN Employee emp ON e.id = emp.managerId
        GROUP BY e.name
        HAVING COUNT(emp.id) > 0
        ORDER BY subordinateCount DESC
    """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ManagerStats(
                rs.getString("manager"),
                rs.getInt("subordinateCount")
        ));
    }
}
