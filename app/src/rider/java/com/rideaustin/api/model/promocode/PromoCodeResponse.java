package com.rideaustin.api.model.promocode;

import com.google.gson.annotations.SerializedName;

/**
 * Created by vokol on 03.08.2016.
 */
public class PromoCodeResponse {
    @SerializedName("codeLiteral")
    private String codeLiteral;

    @SerializedName("codeValue")
    private double codeValue;

    /**
     * <b>Deprecated</b>: check https://issue-tracker.devfactory.com/browse/RA-6733
     */
    @Deprecated
    @SerializedName("detailText")
    private String detailText;

    /**
     * <b>Deprecated</b>: check https://issue-tracker.devfactory.com/browse/RA-6733
     */
    @Deprecated
    @SerializedName("emailBody")
    private String emailBody;

    /**
     * <b>Deprecated</b>: check https://issue-tracker.devfactory.com/browse/RA-6733
     */
    @Deprecated
    @SerializedName("smsBody")
    private String smsBody;

    public PromoCodeResponse() {
    }

    public String getCodeLiteral() {
        return codeLiteral;
    }

    public void setCodeLiteral(String codeLiteral) {
        this.codeLiteral = codeLiteral;
    }

    public double getCodeValue() {
        return codeValue;
    }

    public void setCodeValue(double codeValue) {
        this.codeValue = codeValue;
    }

    @Deprecated
    public String getDetailText() {
        return detailText;
    }

    public void setDetailText(String detailText) {
        this.detailText = detailText;
    }

    @Deprecated
    public String getEmailBody() {
        return emailBody;
    }

    public void setEmailBody(String emailBody) {
        this.emailBody = emailBody;
    }

    @Deprecated
    public String getSmsBody() {
        return smsBody;
    }

    public void setSmsBody(String smsBody) {
        this.smsBody = smsBody;
    }
}
