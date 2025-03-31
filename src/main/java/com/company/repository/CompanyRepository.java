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
        String sql = "SELECT d.name AS department_name, " +
                "ROUND(AVG(e.salary), 2) AS average_salary, " +
                "COUNT(e.id) AS employee_count " +
                "FROM departments d " +
                "JOIN employees e ON d.id = e.department_id " +
                "GROUP BY d.id";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            DepartmentStats stats = new DepartmentStats(
                    rs.getString("department_name"),
                    rs.getInt("average_salary"),
                    rs.getInt("employee_count")
            );
            return stats;
        });
    }

    // 3. 전체 최고 연봉 직원 조회
    public Employee findHighestSalaryEmployee() {
        String sql = "SELECT * FROM employees ORDER BY salary DESC LIMIT 1";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            Employee employee = new Employee();
            employee.setId(rs.getLong("id"));
            employee.setName(rs.getString("name"));
            employee.setSalary(rs.getInt("salary"));
            employee.setDepartmentId(rs.getLong("department_id"));
            employee.setManagerId(rs.getLong("manager_id"));
            return employee;
        });
    }

    // 4. 프로젝트별 통계: 프로젝트명, 참여 직원 수, 참여 직원 급여 합계
    public List<ProjectStats> findProjectStatistics() {
        String sql = "SELECT p.name AS project_name, " +
                "COUNT(ep.employee_id) AS employee_count, " +
                "SUM(e.salary) AS total_salary " +
                "FROM projects p " +
                "JOIN employee_projects ep ON p.id = ep.project_id " +
                "JOIN employees e ON ep.employee_id = e.id " +
                "GROUP BY p.id";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ProjectStats stats = new ProjectStats(
                    rs.getString("project_name"),
                    rs.getInt("employee_count"),
                    rs.getInt("total_salary")
            );
            return stats;
        });
    }

    // 5. 부서별 최고 연봉 직원 조회
    public List<Employee> findTopEmployeePerDepartment() {
        String sql = "SELECT * FROM employees e " +
                "WHERE e.salary = (SELECT MAX(salary) FROM employees WHERE department_id = e.department_id)";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Employee employee = new Employee();
            employee.setId(rs.getLong("id"));
            employee.setName(rs.getString("name"));
            employee.setSalary(rs.getInt("salary"));
            employee.setDepartmentId(rs.getLong("department_id"));
            employee.setManagerId(rs.getLong("manager_id"));
            return employee;
        });
    }

    // 6. 어떤 프로젝트에도 참여하지 않은 직원 조회 (알파벳 순)
    public List<String> findEmployeesNotInAnyProject() {
        String sql = "SELECT e.name FROM employees e " +
                "WHERE NOT EXISTS (SELECT 1 FROM employee_projects ep WHERE ep.employee_id = e.id) " +
                "ORDER BY e.name";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    // 7. 매니저 통계: 자신을 상사로 갖는 직원 수 집계 (매니저 이름, 부하 수)
    public List<ManagerStats> findManagerStatistics() {
        String sql = "SELECT e.name AS manager_name, COUNT(m.id) AS subordinate_count " +
                "FROM employees e " +
                "JOIN employees m ON e.id = m.manager_id " +
                "GROUP BY e.id";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            return new ManagerStats(
                    rs.getString("manager_name"),
                    rs.getInt("subordinate_count")
            );
        });
    }
}
