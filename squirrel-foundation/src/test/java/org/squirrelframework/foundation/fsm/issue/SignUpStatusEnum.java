package org.squirrelframework.foundation.fsm.issue;

public enum SignUpStatusEnum {
    START(9999, "初始态"),
    /** 主状态 **/
    CREATING(1, "创建中"),
    VALID(2, "生效"),
    SUSPENDED(3, "暂停"),
    INVALID(4, "失效"),

    /** 子状态 **/
    DRAFT(101, "待提交"),
    COMMITTED(102, "已提交"),

    SUB_VALID(200,"生效"),

    AUDIT_REJECT_INVALID(401, "失效-审核拒绝"),
    CREATOR_CLEAR_INVALID(402, "失效-创建者清退"),
    QUIT_INVALID(403, "失效-主动退出"),
    OFFICIAL_CLEAR_INVALID(404, "失效-官方清退"),
    CAMPAIGN_INVALID(405, "失效-计划失效");

    private int value;
    private String desc;

    SignUpStatusEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static SignUpStatusEnum of(Integer value) {
        if (value == null) {
            return null;
        }
        for (SignUpStatusEnum status : SignUpStatusEnum.values()) {
            if (status.value == value.intValue()) {
                return status;
            }
        }
        return null;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
