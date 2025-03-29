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
        jdbcTemplate.execute("DROP TABLE IF EXISTS departments, employees, projects, employees_projects;");

        jdbcTemplate.execute("""
                    CREATE TABLE departments (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(255) NOT NULL
                    );
                """);
        jdbcTemplate.execute("""
                    CREATE TABLE employees (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        department_id BIGINT REFERENCES departments(id),
                        salary INTEGER NOT NULL,
                        manager_id BIGINT REFERENCES employees(id)
                    );
                """);
        jdbcTemplate.execute("""
                    CREATE TABLE projects (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        budget INTEGER NOT NULL
                    );
                """);
        jdbcTemplate.execute("""
                    CREATE TABLE employees_projects (
                        employee_id BIGINT REFERENCES employees(id),
                        project_id BIGINT REFERENCES projects(id),
                        role VARCHAR(255)
                    );
                """);
    }

    // --- 데이터 삽입 ---
    public void insertDepartment(String name) {
        jdbcTemplate.update("INSERT INTO departments (name) VALUES (?)", name);
    }

    public void insertEmployee(Employee employee) {
        jdbcTemplate.update("INSERT INTO employees (name, department_id, salary, manager_id) VALUES (?, ?, ?, ?)",
                employee.getName(), employee.getDepartmentId(), employee.getSalary(), employee.getManagerId()
        );
    }

    public void insertProject(Project project) {
        jdbcTemplate.update("INSERT INTO projects (name, budget) VALUES (?, ?)", project.getName(), project.getBudget());
    }

    public void insertEmployeeProject(Long employeeId, Long projectId, String role) {
        jdbcTemplate.update("INSERT INTO employees_projects (employee_id, project_id, role) VALUES (?, ?, ?)", employeeId, projectId, role);
    }

    // 1. 급여가 일정 이상인 직원의 이름을 알파벳 순으로 조회
    public List<String> findEmployeeNamesBySalaryGreaterThanEqualOrderedByName(int salary) {
        return jdbcTemplate.queryForList("""
                    SELECT name FROM employees
                    WHERE salary >= ?
                    ORDER BY name ASC;
                """, String.class, salary);
    }

    // 2. 부서별 통계: 부서명, 평균 급여(반올림), 직원 수
    public List<DepartmentStats> findDepartmentStatistics() {
        return jdbcTemplate.query("""
                    SELECT d.name AS department, ROUND(AVG(e.salary)) AS avg_salary, COUNT(*) AS employee_count FROM departments d 
                    INNER JOIN employees e ON d.id = e.department_id
                    GROUP BY d.id
                    ORDER BY d.name;
                """, (rs, rowNum) -> new DepartmentStats(
                        rs.getString("department"),
                        rs.getInt("avg_salary"),
                        rs.getInt("employee_count")
                ));
    }

    // 3. 전체 최고 연봉 직원 조회
    public Employee findHighestSalaryEmployee() {
        return jdbcTemplate.queryForObject("""
                    SELECT * FROM employees
                    WHERE salary = (
                            SELECT MAX(salary) FROM employees
                    );
                """, (rs, rowNum) -> new Employee(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getLong("department_id"),
                        rs.getInt("salary"),
                        rs.getLong("manager_id")
                ));
    }

    // 4. 프로젝트별 통계: 프로젝트명, 참여 직원 수, 참여 직원 급여 합계
    public List<ProjectStats> findProjectStatistics() {
        return jdbcTemplate.query("""
                    SELECT p.name AS project, COUNT(ep.employee_id) AS employee_count, SUM(e.salary) AS total_salary
                    FROM employees_projects ep
                    INNER JOIN projects p ON ep.project_id = p.id
                    INNER JOIN employees e ON ep.employee_id = e.id
                    GROUP BY p.id;
                """, (rs, rowNum) -> new ProjectStats(
                        rs.getString("project"),
                        rs.getInt("employee_count"),
                        rs.getInt("total_salary")
                ));
    }

    // 5. 부서별 최고 연봉 직원 조회
    public List<Employee> findTopEmployeePerDepartment() {
        return jdbcTemplate.query("""
                SELECT * FROM employees
                WHERE salary IN (
                    SELECT MAX(salary)
                    FROM employees
                    GROUP BY department_id
                );
            """, (rs, rowNum) -> new Employee(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getLong("department_id"),
                    rs.getInt("salary"),
                    rs.getLong("manager_id")
            ));
    }

    // 6. 어떤 프로젝트에도 참여하지 않은 직원 조회 (알파벳 순)
    public List<String> findEmployeesNotInAnyProject() {
        return jdbcTemplate.queryForList("""
                SELECT e.name FROM employees_projects ep
                INNER JOIN employees e ON ep.employee_id = e.id
                WHERE project_id = null; 
            """, String.class);
    }

    // 7. 매니저 통계: 자신을 상사로 갖는 직원 수 집계 (매니저 이름, 부하 수)
    public List<ManagerStats> findManagerStatistics() {
        return jdbcTemplate.query("""
                SELECT e1.name AS manager, count(*) AS subordinate_count FROM employees e1
                INNER JOIN employees e2 ON e1.id = e2.manager_id
                GROUP BY e1.id;
            """, (rs, rowNum) -> new ManagerStats(
                    rs.getString("manager"),
                    rs.getInt("subordinate_count")
            ));
    }
}
