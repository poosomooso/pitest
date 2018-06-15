package reu.hom;

import com.example.triangle.src.Triangle;
import com.example.triangle.test.TriangleTest;
import org.junit.Test;
import org.mockito.Mockito;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.PathFilter;
import org.pitest.classpath.ProjectClassPaths;
import org.pitest.geneticAlgorithm.MutationContainer;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.config.SettingsFactory;
import org.pitest.mutationtest.engine.*;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.util.Glob;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class MutationGenerationTest {

    private ReportOptions getTriangleData() {
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

        return data;
    }

    private Location getTriangleLocation() {
        return Location.location(ClassName.fromClass(Triangle.class), MethodName.fromString("triangle"), "");
    }

    @Test
    public void genTriangleMutations() {
        ReportOptions data = getTriangleData();

        PluginServices plugins = PluginServices.makeForContextLoader();
        SettingsFactory settingsFactory = new SettingsFactory(data, plugins);
        assertFalse(MutationGeneration.getFOMs(data, settingsFactory).isEmpty());
    }

    @Test
    public void genHOMs() {
        MutationDetails m0 = new MutationDetails(new MutationIdentifier(getTriangleLocation(), 3, "a"), "file", "desc", 0, 0);
        MutationDetails m1 = new MutationDetails(new MutationIdentifier(getTriangleLocation(), 5, "a"), "file", "desc", 0, 0);
        MutationDetails m2 = new MutationDetails(new MutationIdentifier(getTriangleLocation(), 8, "a"), "file", "desc", 0, 0);
        MutationDetails m3 = new MutationDetails(new MutationIdentifier(getTriangleLocation(), 13, "a"), "file", "desc", 0, 0);

        List<MutationDetails> foms = new ArrayList<>();
        foms.add(m0);
        foms.add(m1);
        foms.add(m2);
        foms.add(m3);

        int maxOrder = 2;
        int numHOMs = 3;
        MutationContainer[] mutations = MutationGeneration.genHOMs(maxOrder, numHOMs, foms);

        assertEquals(numHOMs, mutations.length);
        for (MutationContainer m : mutations) {
            HigherOrderMutation higherOrderMutation = m.getMutation();
            assertTrue(higherOrderMutation.getOrder() <= maxOrder);
            if (higherOrderMutation.getOrder() > 1) {
                assertNotEquals(higherOrderMutation.getMutationDetail(0), higherOrderMutation.getMutationDetail(1));
            }
        }
    }
}
