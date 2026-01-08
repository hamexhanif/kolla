// VOLLSTÄNDIGER, FINALER CODE FÜR DataSeeder.java MIT LOGGING

package team5.prototype.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import team5.prototype.role.Role;
import team5.prototype.role.RoleRepository;
import team5.prototype.task.TaskDto;
import team5.prototype.task.TaskService;
import team5.prototype.tenant.Tenant;
import team5.prototype.tenant.TenantRepository;
import team5.prototype.user.User;
import team5.prototype.user.UserRepository;
import team5.prototype.workflow.definition.WorkflowDefinition;
import team5.prototype.workflow.definition.WorkflowDefinitionRepository;
import team5.prototype.workflow.step.WorkflowStep;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final WorkflowDefinitionRepository workflowDefinitionRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final TaskService taskService;

    public DataSeeder(RoleRepository roleRepository, UserRepository userRepository,
                      WorkflowDefinitionRepository workflowDefinitionRepository, TenantRepository tenantRepository,
                      PasswordEncoder passwordEncoder, TaskService taskService) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.workflowDefinitionRepository = workflowDefinitionRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.taskService = taskService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (tenantRepository.count() == 0) {
            logger.info("Datenbank ist leer. Erstelle Dummy-Daten...");
            createDummyData();
        } else {
            logger.info("Datenbank enthält bereits Daten. Überspringe das Seeding.");
        }
    }

    private void createDummyData() {
        // == Schritt 0: Den Mandanten (Tenant) erstellen ==
        Tenant defaultTenant = Tenant.builder().name("Default Tenant").subdomain("default").active(true).build();
        tenantRepository.save(defaultTenant);

        // == Schritt 1: Einen Admin-Benutzer erstellen ==
        User adminUser = User.builder().username("admin").email("admin@kolla.com").passwordHash(passwordEncoder.encode("adminpassword")).firstName("Admin").lastName("User").tenant(defaultTenant).active(true).build();
        userRepository.save(adminUser);

        // == Schritt 2: Rollen erstellen ==
        Role managerRole = Role.builder().name("WORKFLOW_MANAGER").description("...").tenant(defaultTenant).createdBy(adminUser).build();
        Role developerRole = Role.builder().name("DEVELOPER").description("...").tenant(defaultTenant).createdBy(adminUser).build();
        Role testerRole = Role.builder().name("TESTER").description("...").tenant(defaultTenant).createdBy(adminUser).build();
        roleRepository.saveAll(List.of(managerRole, developerRole, testerRole));
        adminUser.setRoles(Set.of(managerRole));
        userRepository.save(adminUser);

        // == Schritt 3: Weitere Benutzer (Akteure) erstellen ==
        User developerUser = User.builder().username("developer").email("dev@kolla.com").passwordHash(passwordEncoder.encode("devpassword")).firstName("Dev").lastName("Eloper").tenant(defaultTenant).roles(Set.of(developerRole)).active(true).build();
        User testerUser = User.builder().username("tester").email("tester@kolla.com").passwordHash(passwordEncoder.encode("testpassword")).firstName("Test").lastName("Er").tenant(defaultTenant).roles(Set.of(testerRole)).active(true).build();
        userRepository.saveAll(List.of(developerUser, testerUser));

        // == Schritt 4: Eine Workflow-Vorlage (WorkflowDefinition) erstellen ==
        WorkflowDefinition newFeatureWorkflow = WorkflowDefinition.builder().name("Neues Feature entwickeln").description("...").tenant(defaultTenant).createdBy(adminUser).build();
        WorkflowStep step1 = WorkflowStep.builder().name("Analyse und Design").durationHours(8).sequenceOrder(0).requiredRole(developerRole).workflowDefinition(newFeatureWorkflow).build();
        WorkflowStep step2 = WorkflowStep.builder().name("Implementierung").durationHours(24).sequenceOrder(1).requiredRole(developerRole).workflowDefinition(newFeatureWorkflow).build();
        WorkflowStep step3 = WorkflowStep.builder().name("Qualitätssicherung").durationHours(16).sequenceOrder(2).requiredRole(testerRole).workflowDefinition(newFeatureWorkflow).build();
        newFeatureWorkflow.setSteps(Arrays.asList(step1, step2, step3));
        workflowDefinitionRepository.save(newFeatureWorkflow);

        // == Schritt 5: Einen echten Task aus der Vorlage erstellen ==
        logger.info(">>>> DATENSEEDER: Erstelle jetzt einen echten Test-Task... <<<<");
        TaskDto taskRequestDto = new TaskDto();
        taskRequestDto.setWorkflowDefinitionId(newFeatureWorkflow.getId());
        taskRequestDto.setTitle("Erstes Feature implementieren");
        taskRequestDto.setDescription("Dies ist der erste Task, der vom DataSeeder erstellt wurde.");
        taskRequestDto.setDeadline(LocalDateTime.now().plusDays(10));
        taskRequestDto.setCreatorUserId(adminUser.getId());

        try {
            taskService.createTaskFromDefinition(taskRequestDto);
            logger.info(">>>> DATENSEEDER: Test-Task erfolgreich erstellt! <<<<");
        } catch (Exception e) {
            logger.error(">>>> FEHLER BEIM ERSTELLEN DES TEST-TASKS: {}", e.getMessage(), e);
        }

        logger.info("-----------------------------------------");
        logger.info("Dummy-Daten erfolgreich erstellt!");
        logger.info("Tenants: {}", tenantRepository.count());
        logger.info("Benutzer: {}", userRepository.count());
        logger.info("Rollen: {}", roleRepository.count());
        logger.info("Workflow-Vorlagen: {}", workflowDefinitionRepository.count());
        logger.info("-----------------------------------------");
    }
}
