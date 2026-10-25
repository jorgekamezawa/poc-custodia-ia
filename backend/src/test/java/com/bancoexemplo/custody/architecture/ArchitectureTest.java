package com.bancoexemplo.custody.architecture;

import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.nameEndingWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bancoexemplo.custody.archfixture.ReachesIntoCalculatorDomain;
import com.bancoexemplo.custody.archfixture.adapter.out.PartlyCoveredAdapter;
import com.bancoexemplo.custody.archfixture.adapter.out.UntestedAdapter;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
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
            if (!adapter.getPackageName().contains(".adapter.out")
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

    private static Optional<String> module(String packageName) {
        if (!packageName.startsWith(ROOT)) {
            return Optional.empty();
        }
        String rest = packageName.substring(ROOT.length());
        int dot = rest.indexOf('.');
        return Optional.of(dot < 0 ? rest : rest.substring(0, dot));
    }
}
