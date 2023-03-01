/**
 * @author  Michael Cuison
 * 
 * @since September 5, 2018
 */
package org.rmj.payment.base;

import com.mysql.jdbc.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.rmj.appdriver.GCrypt;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.iface.GEntity;
import org.rmj.appdriver.iface.GTransaction;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.CommonUtils;
import org.rmj.payment.pojo.UnitCreditCardTrans;

public class CreditCardTrans implements GTransaction{
    @Override
    public UnitCreditCardTrans newTransaction() {
        UnitCreditCardTrans loObject = new UnitCreditCardTrans();
        
        Connection loConn = null;
        loConn = setConnection();       
        
        //assign the primary values
        loObject.setTransNo(MiscUtil.getNextCode(loObject.getTable(), "sTransNox", true, loConn, psBranchCd + System.getProperty("pos.clt.trmnl.no")));
        
        return loObject;
    }

    @Override
    public UnitCreditCardTrans loadTransaction(String string) {
        UnitCreditCardTrans loObject = new UnitCreditCardTrans();
        
        Connection loConn = null;
        loConn = setConnection();   
        
        String lsSQL = MiscUtil.addCondition(getSQ_Master(), "sTransNox = " + SQLUtil.toSQL(string));
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        
        try {
            if (!loRS.next()){
                setMessage("No Record Found");
            }else{
                //load each column to the entity
                for(int lnCol=1; lnCol<=loRS.getMetaData().getColumnCount(); lnCol++){
                    loObject.setValue(lnCol, loRS.getObject(lnCol));
                }
            }              
        } catch (SQLException ex) {
            setErrMsg(ex.getMessage());
        } finally{
            MiscUtil.close(loRS);
            if (!pbWithParent) MiscUtil.close(loConn);
        }
        
        return loObject;
    }
    
    public UnitCreditCardTrans loadTransaction(String fsSourceNo, String fsSourceCd) {
        UnitCreditCardTrans loObject = new UnitCreditCardTrans();
        
        Connection loConn = null;
        loConn = setConnection();   
        
        String lsSQL = MiscUtil.addCondition(getSQ_Master(), "sSourceNo = " + SQLUtil.toSQL(fsSourceNo) + 
                                                                " AND sSourceCd = " + SQLUtil.toSQL(fsSourceCd));
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        
        try {
            if (!loRS.next()){
                setMessage("No Record Found");
            }else{
                //load each column to the entity
                for(int lnCol=1; lnCol<=loRS.getMetaData().getColumnCount(); lnCol++){
                    loObject.setValue(lnCol, loRS.getObject(lnCol));
                }
            }              
        } catch (SQLException ex) {
            setErrMsg(ex.getMessage());
        } finally{
            MiscUtil.close(loRS);
            if (!pbWithParent) MiscUtil.close(loConn);
        }
        
        return loObject;
    }

    @Override
    public UnitCreditCardTrans saveUpdate(Object o, String string) {
        String lsSQL = "";
        UnitCreditCardTrans loOldEnt = null;
        UnitCreditCardTrans loNewEnt = null;
        UnitCreditCardTrans loResult = null;
        
        // Check for the value of foEntity
        if (!(o instanceof UnitCreditCardTrans)) {
            setErrMsg("Invalid Entity Passed as Parameter");
            return loResult;
        }
        
        // Typecast the Entity to this object
        loNewEnt = (UnitCreditCardTrans) o;
        
        
        // Test if entry is ok
        if (loNewEnt.getBranchCd()== null || loNewEnt.getBranchCd().isEmpty()){
            setMessage("Invalid branch detected.");
            return loResult;
        }
        
        if (loNewEnt.getTermnlID()== null || loNewEnt.getTermnlID().isEmpty()){
            setMessage("Invalid terminal detected.");
            return loResult;
        }
        
        if (loNewEnt.getBankCode()== null || loNewEnt.getBankCode().isEmpty()){
            setMessage("Invalid terminal detected.");
            return loResult;
        }
        
        if (loNewEnt.getCardNoxx()== null || loNewEnt.getCardNoxx().isEmpty()){
            setMessage("Invalid card number detected.");
            return loResult;
        }
        
        if (loNewEnt.getApprovNo()== null || loNewEnt.getApprovNo().isEmpty()){
            setMessage("Invalid approval code detected.");
            return loResult;
        }
        
        if (loNewEnt.getBatchNox()== null || loNewEnt.getBatchNox().isEmpty()){
            setMessage("Invalid batch number detected.");
            return loResult;
        }
        
        /*if (loNewEnt.getTermCode()== null || loNewEnt.getTermCode().isEmpty()){
            setMessage("Invalid term detected.");
            return loResult;
        }*/
        
        if (loNewEnt.getSourceCd()== null || loNewEnt.getSourceCd().isEmpty()){
            setMessage("Invalid source code detected.");
            return loResult;
        }
        
        if (loNewEnt.getSourceNo()== null || loNewEnt.getSourceNo().isEmpty()){
            setMessage("Invalid source number detected.");
            return loResult;
        }
       
        loNewEnt.setDateModified(poGRider.getServerDate());
        
        // Generate the SQL Statement
        if (string.equals("")){
            Connection loConn = null;
            loConn = setConnection();   
            
            loNewEnt.setTransNo(MiscUtil.getNextCode(loNewEnt.getTable(), "sTransNox", true, loConn, psBranchCd + System.getProperty("pos.clt.trmnl.no")));
            
            if (!pbWithParent) MiscUtil.close(loConn);
            
            //Generate the SQL Statement
            lsSQL = MiscUtil.makeSQL((GEntity) loNewEnt);
        }else{
            //Load previous transaction
            loOldEnt = loadTransaction(string);
            
            //Generate the Update Statement
            lsSQL = MiscUtil.makeSQL((GEntity) loNewEnt, (GEntity) loOldEnt, "sTransNox = " + SQLUtil.toSQL(loNewEnt.getValue(1)));
        }
        
        //No changes have been made
        if (lsSQL.equals("")){
            setMessage("Record is not updated");
            return loResult;
        }
        
        if (!pbWithParent) poGRider.beginTrans();
        
        if(poGRider.executeQuery(lsSQL, loNewEnt.getTable(), "", "") == 0){
            if(!poGRider.getErrMsg().isEmpty())
                setErrMsg(poGRider.getErrMsg());
            else
            setMessage("No record updated");
        } else loResult = loNewEnt;
        
        if (!pbWithParent) {
            if (!getErrMsg().isEmpty()){
                poGRider.rollbackTrans();
            } else poGRider.commitTrans();
        }        
        
        return loResult;
    }

    @Override
    public boolean deleteTransaction(String string) {
        String lsSQL = "";
        boolean lbUpdated = false;
        UnitCreditCardTrans loOldEnt = null;
        
        loOldEnt = loadTransaction(string);
        
        if (loOldEnt != null){
            lsSQL = "DELETE FROM " + loOldEnt.getTable() + 
                    " WHERE sTransNox = " + SQLUtil.toSQL(string);
            
            if (!pbWithParent) poGRider.beginTrans();

            if(poGRider.executeQuery(lsSQL, loOldEnt.getTable(), "", "") == 0){
                if(!poGRider.getErrMsg().isEmpty()) setErrMsg(poGRider.getErrMsg());
                else setMessage("Unable to delete transaction.");
            } else lbUpdated = true;

            if (!pbWithParent) {
                if (!getErrMsg().isEmpty()){
                    poGRider.rollbackTrans();
                } else poGRider.commitTrans();
            }     
        }
        
        return lbUpdated;
    }

    @Override
    public boolean closeTransaction(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean postTransaction(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean voidTransaction(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean cancelTransaction(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public String getMessage() {
        return psWarnMsg;
    }

    @Override
    public void setMessage(String fsMessage) {
        this.psWarnMsg = fsMessage;
    }

    @Override
    public String getErrMsg() {
        return psErrMsgx;
    }

    @Override
    public void setErrMsg(String fsErrMsg) {
        this.psErrMsgx = fsErrMsg;
    }

    @Override
    public void setBranch(String foBranchCD) {
        this.psBranchCd = foBranchCD;
    }

    @Override
    public void setWithParent(boolean fbWithParent) {
        this.pbWithParent = fbWithParent;
    }

    @Override
    public String getSQ_Master() {
        return (MiscUtil.makeSelect(new UnitCreditCardTrans()));
    }
    
    //Added methods
    public void setGRider(GRider foGRider){
        this.poGRider = foGRider;
        this.psTransNox = foGRider.getUserID();
        
        if (psBranchCd.isEmpty()) psBranchCd = foGRider.getBranchCode();
    }
    
    public void setUserID(String fsUserID){
        this.psTransNox  = fsUserID;
    }
    
    private Connection setConnection(){
        Connection foConn;
        
        if (pbWithParent){
            foConn = (Connection) poGRider.getConnection();
            if (foConn == null) foConn = (Connection) poGRider.doConnect();
        }else foConn = (Connection) poGRider.doConnect();
        
        return foConn;
    }
    
    //Member Variables
    private GRider poGRider = null;
    private String psTransNox = "";
    private String psBranchCd = "";
    private String psWarnMsg = "";
    private String psErrMsgx = "";
    private boolean pbWithParent = false;
    private final GCrypt poCrypt = new GCrypt();
}
