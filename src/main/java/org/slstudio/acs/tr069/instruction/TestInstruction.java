package org.slstudio.acs.tr069.instruction;

import org.slstudio.acs.tr069.instruction.context.InstructionContext;
import org.slstudio.acs.tr069.instruction.exception.InstructionFailException;
import org.slstudio.acs.tr069.instruction.exception.JobFailException;
import org.slstudio.acs.tr069.job.request.IJobRequest;

/**
 * Created with IntelliJ IDEA.
 * User: chandler
 * Date: 13-5-4
 * Time: ����3:39
 */
public class TestInstruction implements IInstruction {
    private String someValue = null;

    public TestInstruction( String someValue) {
        this.someValue = someValue;
    }

    public String getSomeValue() {
        return someValue;
    }

    public void setSomeValue(String someValue) {
        this.someValue = someValue;
    }

    public String getInstructionID() {
        return "c1";
    }

    public IJobRequest execute(InstructionContext cmdContext) throws InstructionFailException, JobFailException {
        cmdContext.getSymbolTable().put("test", someValue+"_test");
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
