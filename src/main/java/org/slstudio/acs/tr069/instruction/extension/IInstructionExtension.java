package org.slstudio.acs.tr069.instruction.extension;

import org.slstudio.acs.tr069.instruction.IInstruction;

/**
 * Created with IntelliJ IDEA.
 * User: chandler
 * Date: 13-5-15
 * Time: ����12:18
 */
public interface IInstructionExtension {
    IInstruction createInstruction(String args, int commandID);
}