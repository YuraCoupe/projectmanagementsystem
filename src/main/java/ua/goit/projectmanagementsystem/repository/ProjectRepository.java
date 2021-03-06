package ua.goit.projectmanagementsystem.repository;

import ua.goit.projectmanagementsystem.config.DatabaseManager;
import ua.goit.projectmanagementsystem.exception.DeveloperNotFoundException;
import ua.goit.projectmanagementsystem.model.dao.DeveloperDao;
import ua.goit.projectmanagementsystem.model.dao.ProjectDao;
import ua.goit.projectmanagementsystem.model.dao.SkillDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ProjectRepository {

    private final static String GET_SALARY_SUM_BY_PROJECT_ID =
            "SELECT pr.project_id, pr.project_name, SUM(d.salary) as project_value\n" +
            "FROM projects pr\n" +
            "JOIN developersToProjects dtp ON dtp.project_id = pr.project_id \n" +
            "JOIN developers d ON dtp.developer_id = d.developer_id \n" +
            "WHERE pr.project_id = ?\n" +
            "GROUP BY pr.project_id, pr.project_name";

    private final static String FIND_DEVELOPERS_BY_PROJECT_ID =
            "SELECT pr.project_id, pr.project_name, d.developer_id, d.first_name, d.last_name, d.age, d.sex, d.company_id, d.salary\n" +
            "FROM projects pr\n" +
            "JOIN developersToProjects dtp ON dtp.project_id = pr.project_id \n" +
            "JOIN developers d ON dtp.developer_id = d.developer_id \n" +
            "WHERE pr.project_id = ?";

    private final static String FIND_ALL_PROJECTS =
            "SELECT dtp.project_id, p.project_name, p.company_id, p.customer_id, p.project_cost, COUNT(d.developer_id) as developers_number\n" +
            "FROM developerstoprojects dtp\n" +
            "JOIN developers d ON dtp.developer_id = d.developer_id\n" +
            "JOIN projects p ON dtp.project_id = p.project_id\n" +
            "GROUP BY dtp.project_id, p.project_name, p.company_id, p.customer_id, p.project_cost";

    private final DatabaseManager databaseManager;

    public ProjectRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Optional<Integer> getSalarySum(Integer projectId) {
        try (Connection connection = databaseManager.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(GET_SALARY_SUM_BY_PROJECT_ID)) {
            preparedStatement.setInt(1, projectId);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return Optional.ofNullable(resultSet.getInt("project_value"));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<Set<DeveloperDao>> findDevsByProjectId(Integer projectId) {
        SkillRepository skillRepository = new SkillRepository(databaseManager);
        try (Connection connection = databaseManager.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(FIND_DEVELOPERS_BY_PROJECT_ID)) {
            preparedStatement.setInt(1, projectId);
            ResultSet resultSet = preparedStatement.executeQuery();
            Set<DeveloperDao> developers = new HashSet<>();
            while (resultSet.next()) {
                DeveloperDao developerDao = new DeveloperDao();
                developerDao.setDeveloperId(resultSet.getInt("developer_id"));
                developerDao.setFirstname(resultSet.getString("first_name"));
                developerDao.setLastname(resultSet.getString("last_name"));
                developerDao.setAge(Integer.parseInt(resultSet.getString("age")));
                developerDao.setSex(resultSet.getString("sex"));
                developerDao.setCompanyId(Integer.parseInt(resultSet.getString("company_id")));
                developerDao.setSalary(Integer.parseInt(resultSet.getString("salary")));
                Set<SkillDao> skills = skillRepository.getSkillsByDeveloperId(developerDao.getDeveloperId()).orElseThrow(()
                        -> new DeveloperNotFoundException(String.format("Developer with ID %d does not exist", developerDao.getDeveloperId())));;
                developerDao.setSkills(skills);
                developers.add(developerDao);
            }
            return Optional.ofNullable(developers);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return Optional.empty();
    }
    public Optional<Set<ProjectDao>> findAllProjects() {
        try (Connection connection = databaseManager.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL_PROJECTS)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            Set<ProjectDao> projects = new HashSet<>();
            while (resultSet.next()) {
                ProjectDao projectDao = new ProjectDao();
                projectDao.setProjectId(resultSet.getInt("project_id"));
                projectDao.setProjectName(resultSet.getString("project_name"));
                projectDao.setCompanyId(resultSet.getInt("company_id"));
                projectDao.setCustomerId(resultSet.getInt("customer_id"));
                projectDao.setProjectCost(resultSet.getInt("project_cost"));
                projects.add(projectDao);
            }
            return Optional.ofNullable(projects);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<HashMap<ProjectDao, Integer>> findAllProjectsWithDevelopersNumber () {
        try (Connection connection = databaseManager.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL_PROJECTS)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            HashMap<ProjectDao, Integer> projects = new HashMap<>();
            while (resultSet.next()) {
                ProjectDao projectDao = new ProjectDao();
                projectDao.setProjectId(resultSet.getInt("project_id"));
                projectDao.setProjectName(resultSet.getString("project_name"));
                projectDao.setCompanyId(resultSet.getInt("company_id"));
                projectDao.setCustomerId(resultSet.getInt("customer_id"));
                projectDao.setProjectCost(resultSet.getInt("project_cost"));
                projects.put(projectDao, resultSet.getInt("developers_number"));
            }
            return Optional.ofNullable(projects);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return Optional.empty();
    }
}
