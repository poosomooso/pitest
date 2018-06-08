package reu.hom;

import com.example.triangle.src.Triangle;
import com.example.triangle.test.TriangleTest;
import org.junit.Test;
import org.mockito.Mockito;
import org.pitest.classpath.PathFilter;
import org.pitest.classpath.ProjectClassPaths;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.config.SettingsFactory;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.util.Glob;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

public class GeneticAlgorithmTest {
    @Test
    public void genTriangleMutations() {
        ReportOptions actual = new ReportOptions();
        ReportOptions data = Mockito.spy(actual);

        data.setReportDir("");
        data.setSourceDirs(Collections.<File> emptyList());
        data.setTargetClasses(Collections.singletonList(Triangle.class.getName()));
        data.setTargetTests(Collections.singleton(new Glob(TriangleTest.class.getName())));
        data.setTestPlugin("junit");
        data.setGroupConfig(new TestGroupConfig());

        ProjectClassPaths cps = new ProjectClassPaths(data.getClassPath(),
            data.createClassesFilter(), new PathFilter(p -> true, p -> true));
        when(data.getMutationClassPaths())
            .thenReturn(cps);

        PluginServices plugins = PluginServices.makeForContextLoader();
        SettingsFactory settingsFactory = new SettingsFactory(data, plugins);
        assertFalse(GeneticAlgorithm.getMutations(data, settingsFactory).isEmpty());
    }
}
