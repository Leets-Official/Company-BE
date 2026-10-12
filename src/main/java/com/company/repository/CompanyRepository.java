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
        // DROP & CREATE TABLES
        jdbcTemplate.execute("DROP TABLE IF EXISTS employee_projects");
        jdbcTemplate.execute("DROP TABLE IF EXISTS employees");
        jdbcTemplate.execute("DROP TABLE IF EXISTS projects");
        jdbcTemplate.execute("DROP TABLE IF EXISTS departments");

        jdbcTemplate.execute("CREATE TABLE departments (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL)");

        jdbcTemplate.execute("CREATE TABLE employees (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "department_id BIGINT NOT NULL, " +
                "salary INT NOT NULL, " +
                "manager_id BIGINT, " +
                "FOREIGN KEY (department_id) REFERENCES departments(id), " +
                "FOREIGN KEY (manager_id) REFERENCES employees(id))");

        jdbcTemplate.execute("CREATE TABLE projects (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "budget INT NOT NULL)");

        jdbcTemplate.execute("CREATE TABLE employee_projects (" +
                "employee_id BIGINT NOT NULL, " +
                "project_id BIGINT NOT NULL, " +
                "role VARCHAR(100), " +
                "PRIMARY KEY (employee_id, project_id), " +
                "FOREIGN KEY (employee_id) REFERENCES employees(id), " +
                "FOREIGN KEY (project_id) REFERENCES projects(id))");
    }

    // --- 데이터 삽입 ---
    public void insertDepartment(String name) {
        String sql = "INSERT INTO departments (name) VALUES (?)";
        jdbcTemplate.update(sql, name);
    }

    public void insertEmployee(Employee employee) {
        String sql = "INSERT INTO employees (name, department_id, salary, manager_id) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                employee.getName(),
                employee.getDepartmentId(),
                employee.getSalary(),
                employee.getManagerId());
    }

    public void insertProject(Project project) {
        String sql = "INSERT INTO projects (name, budget) VALUES (?, ?)";
        jdbcTemplate.update(sql,
                project.getName(),
                project.getBudget());
    }

    public void insertEmployeeProject(Long employeeId, Long projectId, String role) {
        String sql = "INSERT INTO employee_projects (employee_id, project_id, role) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, employeeId, projectId, role);
    }

    // 1. 급여가 일정 이상인 직원의 이름을 알파벳 순으로 조회
    public List<String> findEmployeeNamesBySalaryGreaterThanEqualOrderedByName(int salary) {
        String sql = "SELECT name FROM employees WHERE salary >= ? ORDER BY name";
        return jdbcTemplate.queryForList(sql, String.class, salary);
    }

    // 2. 부서별 통계: 부서명, 평균 급여(반올림), 직원 수
    public List<DepartmentStats> findDepartmentStatistics() {
        String sql = """
        SELECT
            d.name AS department,
            ROUND(AVG(e.salary)) AS avgSalary,
            COUNT(e.id) AS employeeCount
        FROM employees e
        JOIN departments d ON e.department_id = d.id
        GROUP BY d.id, d.name
        ORDER BY d.name
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new DepartmentStats(
                rs.getString("department"),
                rs.getInt("avgSalary"),
                rs.getInt("employeeCount")
        ));
    }

    // 3. 전체 최고 연봉 직원 조회
    public Employee findHighestSalaryEmployee() {
        String sql = """
        SELECT * FROM employees
        WHERE salary = (SELECT MAX(salary) FROM employees)
        LIMIT 1
        """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new Employee(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getLong("department_id"),
                rs.getInt("salary"),
                rs.getObject("manager_id") != null ? rs.getLong("manager_id") : null
        ));
    }

    // 4. 프로젝트별 통계: 프로젝트명, 참여 직원 수, 참여 직원 급여 합계
    public List<ProjectStats> findProjectStatistics() {
        String sql = """
        SELECT
            p.name AS project,
            COUNT(ep.employee_id) AS employeeCount,
            IFNULL(SUM(e.salary), 0) AS totalSalary
        FROM projects p
        LEFT JOIN employee_projects ep ON p.id = ep.project_id
        LEFT JOIN employees e ON ep.employee_id = e.id
        GROUP BY p.id, p.name
        ORDER BY p.name
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
        FROM employees e
        JOIN (
            SELECT department_id, MAX(salary) AS max_salary
            FROM employees
            GROUP BY department_id
        ) max_salaries
        ON e.department_id = max_salaries.department_id
        AND e.salary = max_salaries.max_salary
        JOIN departments d ON e.department_id = d.id
        ORDER BY d.name ASC, e.name ASC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new Employee(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getLong("department_id"),
                rs.getInt("salary"),
                rs.getObject("manager_id") != null ? rs.getLong("manager_id") : null
        ));
    }

    // 6. 어떤 프로젝트에도 참여하지 않은 직원 조회 (알파벳 순)
    public List<String> findEmployeesNotInAnyProject() {
        String sql = """
        SELECT name
        FROM employees e
        WHERE NOT EXISTS (
            SELECT 1
            FROM employee_projects ep
            WHERE ep.employee_id = e.id
        )
        ORDER BY name
        """;

        return jdbcTemplate.queryForList(sql, String.class);
    }

    // 7. 매니저 통계: 자신을 상사로 갖는 직원 수 집계 (매니저 이름, 부하 수)
    public List<ManagerStats> findManagerStatistics() {
        String sql = """
        SELECT m.name AS manager, COUNT(e.id) AS subordinateCount
        FROM employees m
        JOIN employees e ON m.id = e.manager_id
        GROUP BY m.id, m.name
        ORDER BY subordinateCount DESC, manager ASC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new ManagerStats(
                rs.getString("manager"),
                rs.getInt("subordinateCount")
        ));
    }
}
