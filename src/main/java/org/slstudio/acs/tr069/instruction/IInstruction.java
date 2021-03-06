package org.slstudio.acs.tr069.instruction;

import org.slstudio.acs.tr069.instruction.context.InstructionContext;
import org.slstudio.acs.tr069.instruction.exception.InstructionFailException;
import org.slstudio.acs.tr069.instruction.exception.JobCompleteException;
import org.slstudio.acs.tr069.instruction.exception.JobFailException;

/**
 * Created with IntelliJ IDEA.
 * User: chandler
 * Date: 13-5-4
 * Time: ����1:57
 */
public interface IInstruction {
    public String getInstructionID();
    public String getInstructionName();
    public void execute(InstructionContext cmdContext) throws InstructionFailException, JobFailException, JobCompleteException;
}
