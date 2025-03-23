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
        //TODO: 구현
    }

    // --- 데이터 삽입 ---
    public void insertDepartment(String name) {
        //TODO: 구현
    }

    public void insertEmployee(Employee employee) {
        //TODO: 구현
    }

    public void insertProject(Project project) {
        //TODO: 구현
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
