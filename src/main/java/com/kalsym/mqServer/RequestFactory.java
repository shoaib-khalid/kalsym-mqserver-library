package com.kalsym.mqServer;

import com.kalsym.utility.XMLReader;

/**
 *
 * @author admin
 */
public class RequestFactory implements Runnable {

    private final String xml;

    /**
     * Parameterized constructor: takes XML as input
     *
     * @param xml
     */
    public RequestFactory(String xml) {
        this.xml = xml;
    }

    @Override
    public void run() {
        //LogProperties.WriteLog("Incoming request - connected...");
        String refId = "";
        try {
            XMLReader xmlReq = new XMLReader(xml);
            xmlReq.load();
            try {
                refId = xmlReq.readOneElement("refId");
                if (refId.equals("")) {
                    refId = xmlReq.readOneElement("refID");
                    if (refId.equals("")) {
                        refId = xmlReq.readOneElement("NOREFID");
                    }
                }
            } catch (Exception exp) {
                //LogProperties.WriteLog("[RequestFactory]" + exp);
                refId = "ERROR-NorefId";
            }

            //LogProperties.WriteLog("[" + refId + "] Recieved XML [" + xml + "]");

            String functionName = xmlReq.readOneElement("function");

            final JeraRequest reqFactoryObj = (JeraRequest) loadClass(functionName);
            reqFactoryObj.RawIncomingXml = xml;

            //LogProperties.WriteLog("[" + refId + "] Forking a new thread");
            new Thread() {
                public void run() {
                    reqFactoryObj.run();
                }
            }.start();
        } catch (Exception e) {
            //LogProperties.WriteLog(e.getMessage() + e);
        } finally {
        }
    }

    /**
     * Loads class from path at runtime using Reflection technology
     *
     * @param completeClassName
     * @return
     */
    private Object loadClass(String completeClassName) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        Class aClass = null;
        try {
            aClass = classLoader.loadClass(completeClassName);
            System.out.println("aClass.getName() = " + aClass.getName());
        } catch (ClassNotFoundException e) {
            //LogProperties.WriteLog("Could Not Load Request Class" + e);
        }
        JeraRequest reqFactoryObj = null;
        try {
            reqFactoryObj = (JeraRequest) aClass.newInstance();
        } catch (InstantiationException ex) {
            //LogProperties.WriteLog("Error in instantiating request " + ex);
        } catch (IllegalAccessException ex) {
            //LogProperties.WriteLog("Error in accessing request class " + ex);
        }
        return reqFactoryObj;
    }
}
