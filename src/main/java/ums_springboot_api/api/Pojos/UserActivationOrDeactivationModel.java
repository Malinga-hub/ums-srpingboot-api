package ums_springboot_api.api.Pojos;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;

public class UserActivationOrDeactivationModel {

    @ApiModelProperty(required = true, notes="user_id/email/idNumber")
    @NotEmpty(message="id is required")
    private String id;
    @ApiModelProperty(required = true, notes="true/false")
    @NotEmpty(message="isActive is required")
    private boolean isActive;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
