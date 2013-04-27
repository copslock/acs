package org.slstudio.acs.tr069.session.context;

import org.slstudio.acs.kernal.endpoint.IProtocolEndPoint;
import org.slstudio.acs.kernal.exception.SessionException;
import org.slstudio.acs.kernal.session.context.AbstractSessionContext;
import org.slstudio.acs.tr069.TR069Constants;
import org.slstudio.acs.tr069.session.factory.TR069MessageContextFactory;

/**
 * Created with IntelliJ IDEA.
 * User: chandler
 * Date: 13-4-24
 * Time: ����12:01
 */
public class TR069SessionContext extends AbstractSessionContext implements ITR069SessionContext {

    public TR069SessionContext() {
        super(new TR069MessageContextFactory());
    }

    public String getClientIP(){
        return (String)getProperty(TR069Constants.SESSIONCONTEXT_KEY_CLIENTIP);
    }

    public void setClientIP(String clientIP){
        setProperty(TR069Constants.SESSIONCONTEXT_KEY_CLIENTIP, clientIP);
    }

    public int getClientPort(){
        int port = -1;
        Object portObj = getProperty(TR069Constants.SESSIONCONTEXT_KEY_CLIENTPORT);
        if(portObj != null){
            try{
                port = ((Integer)portObj).intValue();
            }catch (Exception exp){
                port = -1;
            }
        }
        return port;
    }

    public void setClientPort(int port){
        setProperty(TR069Constants.SESSIONCONTEXT_KEY_CLIENTPORT, new Integer(port));
    }

    @Override
    public void init(IProtocolEndPoint endPoint) throws SessionException {
        String clientIP = endPoint.getProperty(TR069Constants.SESSIONCONTEXT_KEY_CLIENTIP);
        setClientIP(clientIP);
        String clientPort = endPoint.getProperty(TR069Constants.SESSIONCONTEXT_KEY_CLIENTPORT);
        int port = -1;
        try{
            port = Integer.parseInt(clientPort);
        }catch (Exception exp){
            port = -1;
        }
        setClientPort(-1);
    }
}