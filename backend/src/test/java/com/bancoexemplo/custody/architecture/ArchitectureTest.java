package com.bancoexemplo.custody.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bancoexemplo.custody.archfixture.ReachesIntoCalculatorDomain;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Optional;
import org.junit.jupiter.api.Test;

@AnalyzeClasses(packages = "com.bancoexemplo.custody", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final String ROOT = "com.bancoexemplo.custody.";

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
