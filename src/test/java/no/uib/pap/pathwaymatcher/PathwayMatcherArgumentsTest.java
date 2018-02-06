package no.uib.pap.pathwaymatcher;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import no.uib.pap.pathwaymatcher.PathwayMatcher;

public class PathwayMatcherArgumentsTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Test
    public void mainWithNoArgumentsTest() throws Exception {
        exit.expectSystemExitWithStatus(no.uib.pap.model.Error.NO_ARGUMENTS.getCode());
        PathwayMatcher.main(new String[0]);
    }

    @Test
    public void missingRequiredOption_t_Test(){
        exit.expectSystemExitWithStatus(no.uib.pap.model.Error.COMMAND_LINE_ARGUMENTS_PARSING_ERROR.getCode());
        String[] args = { "-i", "input.txt"};
        PathwayMatcher.main(args);
    }

    @Test
    public void missingArgumentForOption_t_Test(){
        exit.expectSystemExitWithStatus(no.uib.pap.model.Error.COMMAND_LINE_ARGUMENTS_PARSING_ERROR.getCode());
        String[] args = {"-t" ,"-i", "input.txt"};
        PathwayMatcher.main(args);
    }

    @Test
    public void cofigurationFileNotFoundTest(){
        // There should be a configuration file specified and file does not exist
        // Invalid
        exit.expectSystemExitWithStatus(no.uib.pap.model.Error.COULD_NOT_READ_CONF_FILE.getCode());
        String[] args = { "-t", "uniprotList", "-c", "config.txt"};
        PathwayMatcher.main(args);
    }

    @Test
    public void noCofigurationFileTest(){
        // There should not be a configuration file speficied and the file is not there
        // Fails because the input file can not be read, not because of configuration
        // Valid
        exit.expectSystemExitWithStatus(no.uib.pap.model.Error.COULD_NOT_READ_INPUT_FILE.getCode());
        String[] args = { "-t", "uniprotList"};
        PathwayMatcher.main(args);
    }

    @Test
    public void missingRequiredOption_i_Test(){
        // Fails because the input file can not be read, not because of configuration
        exit.expectSystemExitWithStatus(no.uib.pap.model.Error.COULD_NOT_READ_INPUT_FILE.getCode());
        String[] args = { "-t", "uniprotList", "-o", "output.txt"};
        PathwayMatcher.main(args);
    }

    @Test
    public void missingArgumentForOption_i_Test(){
        exit.expectSystemExitWithStatus(no.uib.pap.model.Error.COMMAND_LINE_ARGUMENTS_PARSING_ERROR.getCode());
        String[] args = {"-t", "rsidList","-i"};
        PathwayMatcher.main(args);
    }

    @Test
    public void missingArgumentForOption_o_Test(){
        exit.expectSystemExitWithStatus(no.uib.pap.model.Error.COMMAND_LINE_ARGUMENTS_PARSING_ERROR.getCode());
        String[] args = {"-t", "rsidList", "-o" ,"-i"};
        PathwayMatcher.main(args);
    }

    @Test
    public void missingArgumentForOption_c_Test(){
        exit.expectSystemExitWithStatus(no.uib.pap.model.Error.COMMAND_LINE_ARGUMENTS_PARSING_ERROR.getCode());
        String[] args = {"-t", "rsidList", "-c"};
        PathwayMatcher.main(args);
    }

    @Test
    public void missingArgumentForOption_r_Test(){
        exit.expectSystemExitWithStatus(no.uib.pap.model.Error.COMMAND_LINE_ARGUMENTS_PARSING_ERROR.getCode());
        String[] args = {"-t", "rsidList", "-r"};
        PathwayMatcher.main(args);
    }

    @Test
    public void missingArgumentForOption_h_Test(){
        exit.expectSystemExitWithStatus(no.uib.pap.model.Error.COMMAND_LINE_ARGUMENTS_PARSING_ERROR.getCode());
        String[] args = {"-t", "rsidList", "-h"};
        PathwayMatcher.main(args);
    }

    @Test
    public void missingArgumentForOption_u_Test(){
        exit.expectSystemExitWithStatus(no.uib.pap.model.Error.COMMAND_LINE_ARGUMENTS_PARSING_ERROR.getCode());
        String[] args = {"-t", "rsidList", "-u"};
        PathwayMatcher.main(args);
    }

    @Test
    public void missingArgumentForOption_p_Test(){
        exit.expectSystemExitWithStatus(no.uib.pap.model.Error.COMMAND_LINE_ARGUMENTS_PARSING_ERROR.getCode());
        String[] args = {"-t", "rsidList", "-p"};
        PathwayMatcher.main(args);
    }

    @Test
    public void missingArgumentForOption_vep_Test(){
        exit.expectSystemExitWithStatus(no.uib.pap.model.Error.COMMAND_LINE_ARGUMENTS_PARSING_ERROR.getCode());
        String[] args = {"-t", "rsidList", "-vep"};
        PathwayMatcher.main(args);
    }

    @Test
    public void missingArgumentForOption_f_Test(){
        exit.expectSystemExitWithStatus(no.uib.pap.model.Error.COMMAND_LINE_ARGUMENTS_PARSING_ERROR.getCode());
        String[] args = {"-t", "rsidList", "-f"};
        PathwayMatcher.main(args);
    }
}