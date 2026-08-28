package name.jurgenei.gradle.antlr;

import name.jurgenei.gradle.antlr.semantic.CaslLanguageModule;
import name.jurgenei.gradle.antlr.semantic.SasLanguageModule;
import org.junit.Assert;
import org.junit.Test;

public class SemanticModuleTest {

    @Test
    public void reportsSasLanguageDescriptor() {
        final SasLanguageModule module = new SasLanguageModule();
        Assert.assertEquals("sas", module.languageId());
        Assert.assertEquals("program", module.startRule());
    }

    @Test
    public void reportsCaslLanguageDescriptor() {
        final CaslLanguageModule module = new CaslLanguageModule();
        Assert.assertEquals("casl", module.languageId());
        Assert.assertEquals("program", module.startRule());
    }
}

