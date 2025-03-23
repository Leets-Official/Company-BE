package com.company;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.company.dto.DepartmentStats;
import com.company.dto.ManagerStats;
import com.company.dto.ProjectStats;
import com.company.model.Employee;
import com.company.model.Project;
import com.company.repository.CompanyRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CompanyApplicationTests {

	@Autowired
	private CompanyRepository repository;

	@BeforeEach
	public void setUp() {
		repository.initializeDatabase();

		// 부서 삽입
		repository.insertDepartment("HR");          // id 1
		repository.insertDepartment("Engineering");   // id 2
		repository.insertDepartment("Sales");         // id 3

		// 직원 삽입
		repository.insertEmployee(new Employee("Alice", 1L, 5000, null));
		repository.insertEmployee(new Employee("Bob", 2L, 7000, null));
		repository.insertEmployee(new Employee("Charlie", 2L, 6000, 2L));
		repository.insertEmployee(new Employee("David", 1L, 5500, 1L));
		repository.insertEmployee(new Employee("Eve", 3L, 4500, null));
		repository.insertEmployee(new Employee("Frank", 2L, 6200, 2L));
		repository.insertEmployee(new Employee("Grace", 3L, 4800, null));

		// 프로젝트 삽입
		repository.insertProject(new Project("Project A", 100000)); // id 1
		repository.insertProject(new Project("Project B", 150000)); // id 2
		repository.insertProject(new Project("Project C", 120000)); // id 3

		// 직원-프로젝트 관계 삽입
		repository.insertEmployeeProject(2L, 1L, "Lead");
		repository.insertEmployeeProject(3L, 1L, "Member");
		repository.insertEmployeeProject(6L, 1L, "Member");

		repository.insertEmployeeProject(1L, 2L, "Member");
		repository.insertEmployeeProject(4L, 2L, "Member");
		repository.insertEmployeeProject(5L, 2L, "Lead");

		repository.insertEmployeeProject(3L, 3L, "Lead");
		repository.insertEmployeeProject(6L, 3L, "Member");
		repository.insertEmployeeProject(7L, 3L, "Member");
	}

	// 테스트 1: 급여가 5500 이상인 직원 이름 알파벳 순 조회
	@Test
	public void testFindEmployeeNamesBySalary() {
		List<String> names = repository.findEmployeeNamesBySalaryGreaterThanEqualOrderedByName(5500);
		// 예상: [Bob, Charlie, David, Frank] (급여가 5500 이상)
		assertEquals(4, names.size());
		assertEquals("Bob", names.get(0));
		assertEquals("Charlie", names.get(1));
		assertEquals("David", names.get(2));
		assertEquals("Frank", names.get(3));
	}

	// 테스트 2: 부서별 통계 (부서명, 평균 급여, 직원 수)
	@Test
	public void testFindDepartmentStatistics() {
		List<DepartmentStats> stats = repository.findDepartmentStatistics();
		// 예상 (부서명 알파벳 순):
		// Engineering: (7000+6000+6200)/3 ≒ 6400, 직원수 3
		// HR: (5000+5500)/2 = 5250, 직원수 2
		// Sales: (4500+4800)/2 = 4650, 직원수 2
		assertEquals(3, stats.size());

		DepartmentStats eng = stats.get(0);
		assertEquals("Engineering", eng.getDepartment());
		assertEquals(6400, eng.getAvgSalary());
		assertEquals(3, eng.getEmployeeCount());

		DepartmentStats hr = stats.get(1);
		assertEquals("HR", hr.getDepartment());
		assertEquals(5250, hr.getAvgSalary());
		assertEquals(2, hr.getEmployeeCount());

		DepartmentStats sales = stats.get(2);
		assertEquals("Sales", sales.getDepartment());
		assertEquals(4650, sales.getAvgSalary());
		assertEquals(2, sales.getEmployeeCount());
	}

	// 테스트 3: 전체 최고 연봉 직원 조회
	@Test
	public void testFindHighestSalaryEmployee() {
		Employee top = repository.findHighestSalaryEmployee();
		// 예상: Bob (7000)
		assertEquals("Bob", top.getName());
		assertEquals(7000, top.getSalary());
	}

	// 테스트 4: 프로젝트별 통계 (프로젝트명, 참여 직원 수, 참여 직원 급여 합)
	@Test
	public void testFindProjectStatistics() {
		List<ProjectStats> stats = repository.findProjectStatistics();
		// Project A: Bob(7000), Charlie(6000), Frank(6200) → 인원 3, 총급여 7000+6000+6200 = 19200
		// Project B: Alice(5000), David(5500), Eve(4500) → 인원 3, 총급여 5000+5500+4500 = 15000
		// Project C: Charlie(6000), Frank(6200), Grace(4800) → 인원 3, 총급여 6000+6200+4800 = 17000
		assertEquals(3, stats.size());

		ProjectStats pa = stats.get(0);
		assertEquals("Project A", pa.getProject());
		assertEquals(3, pa.getEmployeeCount());
		assertEquals(19200, pa.getTotalSalary());

		ProjectStats pb = stats.get(1);
		assertEquals("Project B", pb.getProject());
		assertEquals(3, pb.getEmployeeCount());
		assertEquals(15000, pb.getTotalSalary());

		ProjectStats pc = stats.get(2);
		assertEquals("Project C", pc.getProject());
		assertEquals(3, pc.getEmployeeCount());
		assertEquals(17000, pc.getTotalSalary());
	}

	// 테스트 5: 부서별 최고 연봉 직원 조회
	@Test
	public void testFindTopEmployeePerDepartment() {
		List<Employee> tops = repository.findTopEmployeePerDepartment();
		// 예상:
		// Engineering: Bob (7000)
		// HR: David (5500) vs. Alice (5000) → David 최고
		// Sales: Grace (4800) vs. Eve (4500) → Grace 최고
		// 총 3건, 부서별로 정렬된 결과
		assertEquals(3, tops.size());
		assertEquals("Bob", tops.get(0).getName());
		assertEquals("David", tops.get(1).getName());
		assertEquals("Grace", tops.get(2).getName());
	}

	// 테스트 6: 프로젝트에 참여하지 않은 직원 (알파벳 순)
	@Test
	public void testFindEmployeesNotInAnyProject() {
		List<String> names = repository.findEmployeesNotInAnyProject();
		// 샘플 데이터에서 모든 직원은 적어도 한 프로젝트에 참여했으므로 빈 리스트가 예상되거나,
		// 만약 일부 직원(예: 매니저 역할만 수행하는 경우)이 제외된다면 그에 맞게 테스트 데이터를 조정할 수 있음.
		// 여기서는 모든 직원이 참여했다고 가정.
		assertEquals(0, names.size());
	}

	// 테스트 7: 매니저 통계 (매니저 이름, 부하 수)
	@Test
	public void testFindManagerStatistics() {
		List<ManagerStats> stats = repository.findManagerStatistics();
		// 예상: Engineering 매니저인 Bob는 두 명(Bob가 매니저인 직원: Charlie, Frank), HR 매니저인 Alice는 한 명(David)
		// 매니저가 없는 직원은 집계되지 않음.
		// 결과는 부하 수 내림차순 정렬됨.
		assertEquals(2, stats.size());
		ManagerStats mgr1 = stats.get(0);
		assertEquals("Bob", mgr1.getManager());
		assertEquals(2, mgr1.getSubordinateCount());
		ManagerStats mgr2 = stats.get(1);
		assertEquals("Alice", mgr2.getManager());
		assertEquals(1, mgr2.getSubordinateCount());
	}

}
