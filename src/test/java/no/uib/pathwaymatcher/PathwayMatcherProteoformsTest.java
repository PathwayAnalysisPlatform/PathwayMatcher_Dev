package no.uib.pathwaymatcher;

import no.uib.pathwaymatcher.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import java.util.List;

import static no.uib.pathwaymatcher.model.Error.COULD_NOT_READ_CONF_FILE;
import static org.junit.Assert.*;

public class PathwayMatcherProteoformsTest {

    @Before
    public void setUp() throws Exception {
        Conf.setDefaultValues();
    }

    @After
    public void tearDown() throws Exception {
    }

//    @Rule
//    public final ExpectedSystemExit exit = ExpectedSystemExit.none();


}