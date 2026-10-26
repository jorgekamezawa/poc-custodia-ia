package com.bancoexemplo.custody.architecture;

import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.nameEndingWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.constructors;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bancoexemplo.custody.archfixture.ReachesIntoCalculatorDomain;
import com.bancoexemplo.custody.archfixture.adapter.in.RefusesInTheController;
import com.bancoexemplo.custody.archfixture.adapter.out.BadlyNamedRepository;
import com.bancoexemplo.custody.archfixture.adapter.out.PartlyCoveredAdapter;
import com.bancoexemplo.custody.archfixture.adapter.out.UntestedAdapter;
import com.bancoexemplo.custody.archfixture.api.LogicInTheApi;
import com.bancoexemplo.custody.archfixture.domain.EntityAsRecord;
import com.bancoexemplo.custody.archfixture.domain.EntityWithPublicConstructor;
import com.bancoexemplo.custody.archfixture.domain.RefusalOutsideTheHierarchy;
import com.bancoexemplo.custody.shared.archfixture.SharedReachingIntoAModule;
import com.bancoexemplo.custody.shared.domain.exception.DomainException;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaConstructorCall;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;

@AnalyzeClasses(packages = "com.bancoexemplo.custody", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final String ROOT_PACKAGE = "com.bancoexemplo.custody";

    private static final String ROOT = ROOT_PACKAGE + ".";

    private static final String SHARED = "shared";

    @ArchTest
    static final ArchRule domainDoesNotDependOnFrameworks = noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta..");

    @ArchTest
    static final ArchRule modulesTalkOnlyThroughApi = classes()
            .that()
            .resideInAPackage("com.bancoexemplo.custody.(*)..")
            .should(onlyReachOtherModulesThroughTheirApi());

    @Test
    void reachingIntoAnotherModulesDomainBreaksTheRule() {
        var violating = new ClassFileImporter().importClasses(ReachesIntoCalculatorDomain.class);

        assertThatThrownBy(() -> modulesTalkOnlyThroughApi.check(violating))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("calculator.domain.PriceSchedule");
    }

    /** Entities and domain services are built through their factories; records, enums and exceptions are values. */
    @ArchTest
    static final ArchRule domainClassesAreBuiltThroughFactories = constructors()
            .that()
            .areDeclaredInClassesThat()
            .resideInAPackage("..domain..")
            .and()
            .areDeclaredInClassesThat()
            .areNotAssignableTo(Record.class)
            .and()
            .areDeclaredInClassesThat()
            .areNotEnums()
            .and()
            .areDeclaredInClassesThat()
            .areNotAssignableTo(Throwable.class)
            .should()
            .notBePublic();

    /** A record compares every field, so a domain record carrying an identity is an entity in the wrong shape. */
    @ArchTest
    static final ArchRule entitiesAreNotRecords = noFields()
            .that()
            .areDeclaredInClassesThat()
            .resideInAPackage("..domain..")
            .and()
            .areDeclaredInClassesThat()
            .areAssignableTo(Record.class)
            .should()
            .haveName("id");

    /** The controller validates the request; refusing by business rule is the domain's, through its own types. */
    @ArchTest
    static final ArchRule inboundAdaptersDoNotRefuseWithDomainExceptions = noClasses()
            .that()
            .resideInAPackage("..adapter.in..")
            .should()
            .callConstructorWhere(new DescribedPredicate<>("the constructor of a domain exception") {
                @Override
                public boolean test(JavaConstructorCall call) {
                    JavaClass created = call.getTargetOwner();
                    return created.getPackageName().contains(".domain") && created.isAssignableTo(Throwable.class);
                }
            });

    /** An outbound adapter is named after the port it implements and its technology: PaymentRepositoryJpaImpl. */
    @ArchTest
    static final ArchRule outboundAdaptersAreNamedAfterTheirPort =
            classes().that().resideInAPackage("..adapter.out..").should(beNamedAfterTheirPort());

    @Test
    void outboundAdapterNotNamedAfterItsPortBreaksTheRule() {
        var violating = new ClassFileImporter().importClasses(BadlyNamedRepository.class);

        assertThatThrownBy(() -> outboundAdaptersAreNamedAfterTheirPort.check(violating))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("BadlyNamedRepository");
    }

    /** A module api is a contract: what neighbours call and the types they pass, never the implementation. */
    @ArchTest
    static final ArchRule apisHoldOnlyContracts =
            classes().that().resideInAPackage("..api..").should(beAContract());

    /** A module api exposes only what another module uses; anything else stays inside the module. */
    @ArchTest
    static final ArchRule apiTypesAreUsedByAnotherModule =
            classes().that().resideInAPackage("..api..").should(beUsedByAnotherModule());

    @Test
    void logicInAnApiBreaksTheRules() {
        var violating = new ClassFileImporter().importClasses(LogicInTheApi.class);

        assertThatThrownBy(() -> apisHoldOnlyContracts.check(violating))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("LogicInTheApi");
        assertThatThrownBy(() -> apiTypesAreUsedByAnotherModule.check(violating))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("LogicInTheApi");
    }

    /** Shared kernel types are used by every module, so they cannot know any of them. */
    @ArchTest
    static final ArchRule sharedDependsOnNoModule =
            classes().that().resideInAPackage(ROOT + SHARED + "..").should(notDependOnAnyModule());

    /** Every refusal is a domain exception, kept in an exception package, so the edge maps it by category. */
    @ArchTest
    static final ArchRule exceptionsAreDomainExceptionsInTheirPackage = classes()
            .that()
            .resideInAnyPackage("..domain..", "..application..")
            .and()
            .areAssignableTo(Throwable.class)
            .should()
            .resideInAPackage("..exception..")
            .andShould()
            .beAssignableTo(DomainException.class);

    @Test
    void sharedCodeKnowingAModuleBreaksTheRule() {
        var violating = new ClassFileImporter().importClasses(SharedReachingIntoAModule.class);

        assertThatThrownBy(() -> sharedDependsOnNoModule.check(violating))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("contracts.domain.contract.ContractNumber");
    }

    @Test
    void refusalOutsideTheHierarchyBreaksTheRule() {
        var violating = new ClassFileImporter().importClasses(RefusalOutsideTheHierarchy.class);

        assertThatThrownBy(() -> exceptionsAreDomainExceptionsInTheirPackage.check(violating))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("RefusalOutsideTheHierarchy");
    }

    @Test
    void publicConstructorOfADomainClassBreaksTheRule() {
        var violating = new ClassFileImporter().importClasses(EntityWithPublicConstructor.class);

        assertThatThrownBy(() -> domainClassesAreBuiltThroughFactories.check(violating))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("EntityWithPublicConstructor");
    }

    @Test
    void domainRecordWithAnIdBreaksTheRule() {
        var violating = new ClassFileImporter().importClasses(EntityAsRecord.class);

        assertThatThrownBy(() -> entitiesAreNotRecords.check(violating))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("EntityAsRecord.id");
    }

    @Test
    void domainExceptionCreatedInAnInboundAdapterBreaksTheRule() {
        var violating = new ClassFileImporter().importClasses(RefusesInTheController.class);

        assertThatThrownBy(() -> inboundAdaptersDoNotRefuseWithDomainExceptions.check(violating))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("InvalidContractException");
    }

    @Test
    void everyOutboundAdapterMethodIsCalledByTheAdaptersOwnTest() {
        JavaClasses production = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(ROOT_PACKAGE);
        JavaClasses tests = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.ONLY_INCLUDE_TESTS)
                .importPackages(ROOT_PACKAGE);

        assertThat(uncoveredAdapterMethods(production, tests)).isEmpty();
    }

    @Test
    void adapterMethodLeftOutOfItsTestBreaksTheRule() {
        JavaClasses adapters = new ClassFileImporter().importClasses(PartlyCoveredAdapter.class, UntestedAdapter.class);
        JavaClasses tests = new ClassFileImporter()
                .importPackages(PartlyCoveredAdapter.class.getPackageName())
                .that(nameEndingWith("Test"));

        assertThat(uncoveredAdapterMethods(adapters, tests))
                .containsExactlyInAnyOrder(
                        PartlyCoveredAdapter.class.getName() + ".untested() is not called by its test",
                        UntestedAdapter.class.getName() + " has no test class " + UntestedAdapter.class.getName()
                                + "Test");
    }

    /**
     * Each public method of an outbound adapter must be called, on the adapter or on its port, by the test class named
     * after the adapter: a fake or mock elsewhere never proves what the real adapter does.
     */
    private static List<String> uncoveredAdapterMethods(JavaClasses production, JavaClasses tests) {
        List<String> uncovered = new ArrayList<>();
        for (JavaClass adapter : production) {
            // Spring Data interfaces carry no code of ours; the adapter that uses them is the one under test.
            if (!adapter.getPackageName().contains(".adapter.out")
                    || adapter.isInterface()
                    || adapter.isNestedClass()
                    || adapter.isAnnotatedWith(Configuration.class)) {
                continue;
            }
            List<JavaMethod> methods = adapter.getMethods().stream()
                    .filter(method -> method.getModifiers().contains(JavaModifier.PUBLIC))
                    .filter(method -> !method.getModifiers().contains(JavaModifier.STATIC))
                    .toList();
            if (methods.isEmpty()) {
                continue;
            }
            String testName = adapter.getName() + "Test";
            if (!tests.contain(testName)) {
                uncovered.add(adapter.getName() + " has no test class " + testName);
                continue;
            }
            Set<String> owners = new HashSet<>();
            owners.add(adapter.getName());
            adapter.getAllRawInterfaces().forEach(port -> owners.add(port.getName()));
            Set<JavaMethodCall> calls = tests.get(testName).getMethodCallsFromSelf();
            for (JavaMethod method : methods) {
                List<String> parameters = names(method.getRawParameterTypes());
                boolean called = calls.stream()
                        .anyMatch(call -> owners.contains(call.getTargetOwner().getName())
                                && call.getName().equals(method.getName())
                                && names(call.getTarget().getRawParameterTypes())
                                        .equals(parameters));
                if (!called) {
                    uncovered.add(adapter.getName() + "." + method.getName() + "("
                            + method.getRawParameterTypes().stream()
                                    .map(JavaClass::getSimpleName)
                                    .collect(Collectors.joining(", "))
                            + ") is not called by its test");
                }
            }
        }
        return uncovered;
    }

    // Production and test classes come from separate imports, so their JavaClass instances never compare equal.
    private static List<String> names(List<JavaClass> types) {
        return types.stream().map(JavaClass::getName).toList();
    }

    private static ArchCondition<JavaClass> onlyReachOtherModulesThroughTheirApi() {
        return new ArchCondition<>("only reach other modules through their api package") {
            @Override
            public void check(JavaClass origin, ConditionEvents events) {
                Optional<String> originModule = module(origin.getPackageName());
                for (Dependency dependency : origin.getDirectDependenciesFromSelf()) {
                    String target = dependency.getTargetClass().getPackageName();
                    Optional<String> targetModule = module(target);
                    boolean otherModule = targetModule.isPresent() && !targetModule.equals(originModule);
                    if (otherModule && !target.startsWith(ROOT + targetModule.get() + ".api")) {
                        events.add(SimpleConditionEvent.violated(dependency, dependency.getDescription()));
                    }
                }
            }
        };
    }

    /** The business module a package belongs to; {@code shared} is the kernel every module may use, not a module. */
    private static Optional<String> module(String packageName) {
        if (!packageName.startsWith(ROOT)) {
            return Optional.empty();
        }
        String rest = packageName.substring(ROOT.length());
        int dot = rest.indexOf('.');
        String first = dot < 0 ? rest : rest.substring(0, dot);
        return first.equals(SHARED) ? Optional.empty() : Optional.of(first);
    }

    private static ArchCondition<JavaClass> beNamedAfterTheirPort() {
        return new ArchCondition<>("be named <Port><Technology>Impl after the port it implements") {
            @Override
            public void check(JavaClass adapter, ConditionEvents events) {
                adapter.getRawInterfaces().stream()
                        .filter(port -> port.getPackageName().contains(".application.port")
                                || port.getPackageName().contains(".api"))
                        .forEach(port -> {
                            String name = adapter.getSimpleName();
                            if (!name.startsWith(port.getSimpleName()) || !name.endsWith("Impl")) {
                                events.add(SimpleConditionEvent.violated(
                                        adapter,
                                        adapter.getName() + " implements " + port.getSimpleName() + " but is not named "
                                                + port.getSimpleName() + "<Technology>Impl"));
                            }
                        });
            }
        };
    }

    private static ArchCondition<JavaClass> beAContract() {
        return new ArchCondition<>("be an interface, record, enum or exception") {
            @Override
            public void check(JavaClass type, ConditionEvents events) {
                boolean contract = type.isInterface()
                        || type.isEnum()
                        || type.isAssignableTo(Record.class)
                        || type.isAssignableTo(Throwable.class);
                if (!contract) {
                    events.add(SimpleConditionEvent.violated(type, type.getName() + " is a concrete class in an api"));
                }
            }
        };
    }

    private static ArchCondition<JavaClass> beUsedByAnotherModule() {
        return new ArchCondition<>("be used by another module") {
            @Override
            public void check(JavaClass type, ConditionEvents events) {
                Optional<String> owner = module(type.getPackageName());
                boolean used = type.getDirectDependenciesToSelf().stream()
                        .map(dependency -> module(dependency.getOriginClass().getPackageName()))
                        .anyMatch(origin -> origin.isPresent() && !origin.equals(owner));
                if (!used) {
                    events.add(SimpleConditionEvent.violated(type, type.getName() + " is used by no other module"));
                }
            }
        };
    }

    private static ArchCondition<JavaClass> notDependOnAnyModule() {
        return new ArchCondition<>("not depend on any business module") {
            @Override
            public void check(JavaClass origin, ConditionEvents events) {
                for (Dependency dependency : origin.getDirectDependenciesFromSelf()) {
                    if (module(dependency.getTargetClass().getPackageName()).isPresent()) {
                        events.add(SimpleConditionEvent.violated(dependency, dependency.getDescription()));
                    }
                }
            }
        };
    }
}
