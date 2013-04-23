package org.slstudio.acs.kernal.endpoint;

import org.slstudio.acs.kernal.context.ISessionContext;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: chandler
 * Date: 13-4-23
 * Time: ����11:56
 * To change this template use File | Settings | File Templates.
 */
public interface IProtocolEndPoint {
    public Object getProperty(String name);
    public InputStream getInputStream() throws IOException;
    public void writeResponse(ISessionContext context) throws IOException;
}
