package name.jurgenei.gradle.antlr;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class SasMacroProcessorTest {

    @Test
    public void expandsLetAndReferences() {
        final SasMacroProcessor processor = new SasMacroProcessor(Map.of());
        final SasMacroProcessor.MacroExpansionResult result = processor.expand("""
                %let target = work.sales;
                proc sql;
                  create table &target. as
                  select amount from work.input;
                quit;
                """);

        Assert.assertFalse(result.expandedSource().contains("%let target"));
        Assert.assertTrue(result.expandedSource().contains("create table work.sales as"));
        Assert.assertTrue(result.unresolvedMacros().isEmpty());
    }

    @Test
    public void keepsUnknownReferenceAndReportsIt() {
        final SasMacroProcessor processor = new SasMacroProcessor(Map.of());
        final SasMacroProcessor.MacroExpansionResult result = processor.expand("data x; y=&missing.; run;");

        Assert.assertTrue(result.expandedSource().contains("&missing."));
        Assert.assertTrue(result.unresolvedMacros().contains("missing"));
    }

    @Test
    public void supportsPredefinedMacros() {
        final SasMacroProcessor processor = new SasMacroProcessor(Map.of("env", "dev"));
        final SasMacroProcessor.MacroExpansionResult result = processor.expand("data out_&ENV.; run;");

        Assert.assertTrue(result.expandedSource().contains("out_dev"));
        Assert.assertTrue(result.unresolvedMacros().isEmpty());
    }

    @Test
    public void expandsMacroDefinitionAndInvocation() {
        final SasMacroProcessor processor = new SasMacroProcessor(Map.of());
        final SasMacroProcessor.MacroExpansionResult result = processor.expand("""
                %macro mk_sales;
                proc sql;
                  create table work.sales as
                  select amount from work.input;
                quit;
                %mend mk_sales;

                %mk_sales;
                """);

        Assert.assertFalse(result.expandedSource().contains("%macro mk_sales"));
        Assert.assertFalse(result.expandedSource().contains("%mk_sales;"));
        Assert.assertTrue(result.expandedSource().contains("create table work.sales as"));
        Assert.assertTrue(result.unresolvedMacros().isEmpty());
    }

    @Test
    public void expandsNestedMacros() {
        final SasMacroProcessor processor = new SasMacroProcessor(Map.of());
        final SasMacroProcessor.MacroExpansionResult result = processor.expand("""
                %macro inner;
                data out;
                  amount = 2 + 3;
                run;
                %mend inner;

                %macro outer;
                %inner;
                %mend outer;

                %outer;
                """);

        Assert.assertFalse(result.expandedSource().contains("%outer;"));
        Assert.assertFalse(result.expandedSource().contains("%inner;"));
        Assert.assertTrue(result.expandedSource().contains("amount = 2 + 3;"));
        Assert.assertTrue(result.unresolvedMacros().isEmpty());
    }
}

