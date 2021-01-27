package ums_springboot_api.api.Pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import ums_springboot_api.api.config.AppConfig;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Document(collection = "users")
public class UserModel implements Serializable {

    //    variables
    @ApiModelProperty(notes = "auto-generated")
    @Id
    private String id;

    @ApiModelProperty(required = true)
    @NotEmpty(message = "firstName is required")
    private String firstName;

    @ApiModelProperty(required = true)
    @NotEmpty(message = "lastName is required")
    private String lastName;

    @ApiModelProperty(notes = "auto-generated")
    @Indexed(unique = true)
    private String username;

    @ApiModelProperty(required = true)
    @NotEmpty(message = "email is required")
    @Email(message = "invalid email")
    @Pattern(regexp = AppConfig.EMAIL_REGEX, message = "invalid email")
    @Indexed(unique = true)
    private String email;

    @ApiModelProperty(required = true)
    @NotEmpty(message = "phoneNumber is required")
    @Size(max = 12, min = 9, message = "invalid number of characters")
    @Pattern(regexp = "^[0-9]+", message = "invalid phone number")
    private String phoneNumber;

    private String address;

    @ApiModelProperty(required=true)
    @NotEmpty(message = "idNumber is required")
    @Pattern(regexp = "^[0-9]{6}[/][0-9]{2}[/][0-9]{1}", message="invalid Id number")
    @Indexed(unique=true)
    private String idNumber;

    @ApiModelProperty(notes = "default: groupId object reference")
    @JsonProperty(value ="group", access=JsonProperty.Access.READ_ONLY)
    private Object group;

    @ApiModelProperty(notes = "default: user-group")
    @JsonProperty(value="groupId", access=JsonProperty.Access.WRITE_ONLY)
    private int groupId;

    @ApiModelProperty(notes = "default: roleId object reference")
    @JsonProperty(value ="roleId", access=JsonProperty.Access.READ_ONLY)
    private Object role;

    @ApiModelProperty(notes = "default: 1")
    @JsonProperty(value ="roleId", access=JsonProperty.Access.WRITE_ONLY)
    private String roleId;

    @ApiModelProperty(notes = "default: false")
    private boolean isActive;

    @ApiModelProperty(required = true)
    @JsonProperty(value = "password", access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @ApiModelProperty(notes = "auto-generated")
    private String createdAt;

    @ApiModelProperty(notes = "auto-generated")
    private String updatedAt;

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String firstName, String lastName) {
        this.username = firstName + " " + lastName;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public void setRole(Object role) {
        this.role = role;
    }

    public Object getRole(){
        return this.role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }


    public Object getGroup() {
        return group;
    }

    public void setGroup(Object group) {
        this.group = group;
    }

    public String getRoleId() {
        return roleId;
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", address='" + address + '\'' +
                ", idNumber='" + idNumber + '\'' +
                ", groupId=" + groupId +
                ", role='" + role + '\'' +
                ", isActive=" + isActive +
                ", password='" + password + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}
