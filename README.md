# SGPA and CGPA calculator

Spring Boot + Thymeleaf, built with Maven. Two pages: input form and result.

Requirements: JDK 17+, Maven 3.9+, Google Chrome (for the Selenium UI tests in `mvn test`)

    mvn test
    mvn spring-boot:run

Open http://localhost:8080

Grade scale lives in `CgpaService.GRADE_POINTS`; edit it to match your university.
SGPA = sum(credits x grade points) / sum(credits) for one semester. CGPA weights every semester by its credits.
