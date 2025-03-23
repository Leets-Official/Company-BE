## 회사

## 🎯 미션 요구사항
```
기업의 조직 및 프로젝트 데이터를 관리하는 시스템을 구현합니다.
주어진 데이터베이스 스키마를 기반으로 아래와 같은 여러 기능을 구현합니다.
필요한 경우 각 클래스에 필요한 메서드를 추가로 구현하여 해결할 수 있습니다. 
```

```
mySQL은 반드시 본인 환경에 맞게 application.yml 파일을 작성하여 세팅해주세요
```

### 핵심 기능

**데이터 초기화**

```
애플리케이션 실행 시, 필요한 모든 테이블(부서, 직원, 프로젝트, 직원-프로젝트)을 DROP 후 재생성하고, 샘플 데이터를 삽입합니다.
```

**SQL 조회**
```
급여 기준 직원 조회: 급여가 특정 금액 이상인 직원들의 이름을 알파벳 순으로 조회합니다.

부서별 통계: 각 부서의 평균 급여(반올림)와 직원 수를 구하고 부서명 순으로 정렬합니다.

전체 최고 연봉 직원 조회: 전체 직원 중 최고 연봉을 받는 직원을 조회합니다.

프로젝트별 통계: 각 프로젝트에 참여한 직원 수와 그들의 급여 합계를 집계합니다.

부서별 최고 연봉 직원 조회: 각 부서 내에서 최고 연봉을 받는 직원(예: Engineering 부서의 Bob)을 조회합니다.

프로젝트 미참여 직원 조회: 어떤 프로젝트에도 참여하지 않은 직원들을 조회합니다.

매니저 통계 조회: 각 매니저가 관리하는(자신을 상사로 가진) 부하 직원 수를 집계합니다.

```

### 테이블 간 관계
미션에서 사용되는 주요 테이블과 이들 사이의 관계에 대한 조건은 다음과 같습니다.

**부서 (departments)**
```

필드:
id: 부서의 고유 식별자
name: 부서 이름
조건:
각 직원은 반드시 하나의 부서에 소속되어 있습니다.
부서 정보는 수정할 수 없으며, 조회 및 통계의 기준 데이터로 활용됩니다.
```


**직원 (employees)**
```

필드:

id: 직원 고유 식별자

name: 직원 이름

department_id: 해당 직원이 소속된 부서의 식별자 (Foreign Key → departments.id)

salary: 직원 급여

manager_id: 해당 직원의 상사(매니저)의 식별자 (자기 참조, null 허용)


조건:

각 직원은 부서에 소속되며, 선택적으로 상사(manager)를 가질 수 있습니다.

자기참조 관계를 이용하여 매니저와 부하 직원 간의 관계를 구성합니다.

```

**프로젝트 (projects)**
```

필드:

id: 프로젝트 고유 식별자

name: 프로젝트 이름

budget: 프로젝트 예산


조건:

프로젝트는 독립적인 엔티티로, 여러 직원이 동시에 참여할 수 있습니다.
```

**직원-프로젝트 (employee_projects)**
```

필드:

employee_id: 참여한 직원의 식별자 (Foreign Key → employees.id)

project_id: 참여한 프로젝트의 식별자 (Foreign Key → projects.id)

role: 해당 직원의 프로젝트 내 역할

조건:

이 테이블은 직원과 프로젝트 사이의 다대다 관계를 나타냅니다.

한 직원은 여러 프로젝트에 참여할 수 있고, 하나의 프로젝트에는 여러 직원이 참여할 수 있습니다.

각 관계에는 추가로 역할(role) 정보를 저장할 수 있습니다.
```

### 구현 조건

**구현 조건**

환경:

JDBC 기반으로 **MySQL**과 연결하며, JdbcTemplate을 사용하여 SQL 쿼리를 실행

DataSource 및 기타 설정은 src/main/resources/application.yml에서 관리 (본인의 mySQL 설정에 맞게 설정)

**모든 코드(모델, Repository 등)는 클래스 이름을 변경하지 않고 구현**

### 메서드 구현 목록

**1. 데이터베이스 초기화 및 샘플 데이터 삽입**

**CompanyRepository**

```
initializeDatabase(): 모든 테이블(부서, 직원, 프로젝트, 직원-프로젝트)을 DROP 후 재생성

insertDepartment(String name): 부서 데이터 삽입

insertEmployee(Employee employee): 직원 데이터 삽입

insertProject(Project project): 프로젝트 데이터 삽입

insertEmployeeProject(Long employeeId, Long projectId, String role): 직원-프로젝트 관계 삽입
```

2. SQL 조회 기능 구현

**급여 기준 직원 조회:**

```
findEmployeeNamesBySalaryGreaterThanEqualOrderedByName(int salary)
```

**부서별 통계:**

```
findDepartmentStatistics()

반환 타입: DepartmentStats (부서명, 평균 급여, 직원 수)
```

**전체 최고 연봉 직원 조회:**

```
findHighestSalaryEmployee()
```

프로젝트별 통계:

```
findProjectStatistics()

반환 타입: ProjectStats (프로젝트명, 참여 직원 수, 총 급여)
```

부서별 최고 연봉 직원 조회:

```
findTopEmployeePerDepartment()
```

프로젝트 미참여 직원 조회:

```
findEmployeesNotInAnyProject()
```

매니저 통계 조회:

```
findManagerStatistics()
반환 타입: ManagerStats (매니저 이름, 부하 직원 수)
```



## 📢 미션 진행 요구사항
미션은 아래의 가이드 노션을 보고 진행합니다.

[미션 진행 가이드](https://leets-final.notion.site/BackEndZero100-1bd13059433780ce8f91cfe8ba54917a?pvs=4)

### 테스트 실행 가이드


- 터미널에서 Mac 또는 Linux 사용자의 경우 `./gradlew clean test` 명령을 실행한다.
- Windows 사용자의 경우 `gradlew.bat clean test` 명령을 실행한다.
- 모든 Task가 제대로 통과하는지 확인한다.

