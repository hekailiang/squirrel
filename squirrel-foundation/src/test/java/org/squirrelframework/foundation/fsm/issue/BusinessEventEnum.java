package org.squirrelframework.foundation.fsm.issue;


import jdk.nashorn.internal.objects.annotations.Getter;

public enum BusinessEventEnum {

    EVENT_CREATE_SIGN_UP("活动报名"),
    EVENT_BATCH_SIGN_UP("批量活动报名"),
    EVENT_STASH_SIGN_UP("暂存报名"),
    EVENT_COMMIT_SIGN_UP("提交报名"),
    EVENT_AUDIT_SIGN_UP("审核报名"),
    EVENT_DELETE_SIGN_UP("删除报名"),
    EVENT_QUIT_SIGN_UP("退出活动"),
    EVENT_CLEAR_PARTICIPANT("清退参与者"),
    EVENT_SUSPEND_PARTICIPATE("暂停参与"),
    EVENT_RESUME_PARTICIPATE("恢复参与"),
    EVENT_EDIT_SIGN_UP("编辑报名"),

    EVENT_EXIT_PUBLISH("退出推广"),
    EVENT_UPDATE_SIGN_UP("更新报名"),
    ;

    private String desc;

    BusinessEventEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
