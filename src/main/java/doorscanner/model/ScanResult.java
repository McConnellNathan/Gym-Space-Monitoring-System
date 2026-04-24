package doorscanner.model;

public class ScanResult {
    private boolean valid;
    private String rawValue;
    private String memberId;
    private String memberName;
    private String membershipStatus;
    private String notes;

    public ScanResult() {
    }

    public ScanResult(boolean valid, String rawValue, String memberId,
                      String memberName, String membershipStatus, String notes) {
        this.valid = valid;
        this.rawValue = rawValue;
        this.memberId = memberId;
        this.memberName = memberName;
        this.membershipStatus = membershipStatus;
        this.notes = notes;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getRawValue() {
        return rawValue;
    }

    public void setRawValue(String rawValue) {
        this.rawValue = rawValue;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMembershipStatus() {
        return membershipStatus;
    }

    public void setMembershipStatus(String membershipStatus) {
        this.membershipStatus = membershipStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "ScanResult{" +
                "valid=" + valid +
                ", rawValue='" + rawValue + '\'' +
                ", memberId='" + memberId + '\'' +
                ", memberName='" + memberName + '\'' +
                ", membershipStatus='" + membershipStatus + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}