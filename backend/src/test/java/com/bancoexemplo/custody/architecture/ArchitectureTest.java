package com.bancoexemplo.custody.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.bancoexemplo.custody", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    // Empty until the first business module exists; without this the rule fails for having nothing to check.
    @ArchTest
    static final ArchRule domainDoesNotDependOnFrameworks = noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta..")
            .allowEmptyShould(true);
}
