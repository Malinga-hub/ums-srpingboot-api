package ums_springboot_api.api.services;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ums_springboot_api.api.Pojos.*;
import ums_springboot_api.api.config.AppConfig;
import ums_springboot_api.api.repositories.PasswordResetCodeRepository;
import ums_springboot_api.api.repositories.UserRepository;
import ums_springboot_api.api.util.DateUtil;
import ums_springboot_api.api.util.PayloadUtil;
import ums_springboot_api.api.util.RandomCode;
import ums_springboot_api.api.util.UMS_SERVICE_CENTER;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private PasswordResetCodeRepository passwordResetCodeRepository;

    private UserModel user;

    private Logger logger = LoggerFactory.getLogger(UserService.class);

    //   register
    public ResponsePayload register(UserModel userModel, HttpServletResponse res) throws Exception {
        ResponsePayload resPayload;

        try {

            if (userModel.getPassword().isEmpty()) {
                throw new Exception(AppConfig.NULL_PASSWORD_MSG);
            }

            if (!(userModel.getPassword().length() >= AppConfig.MIN_PASSWORD_LENGTH)) {
                throw new Exception(AppConfig.MIN_LENGTH_PASSWORD_MSG);
            }

            userModel.setPassword(bCryptPasswordEncoder.encode(userModel.getPassword()));

//            SET DEFAULTS
            userModel.setUsername(userModel.getEmail());
            userModel.setActive(AppConfig.USER_DEFAULT_ISACTIVE);
            userModel.setGroupId(AppConfig.USER_DEFAULT_GROUP_ID);
            userModel.setRoleId(AppConfig.USER_DEFAULT_ROLE_ID);
            userModel.setCreatedAt(DateUtil.getCurrentDate());
            userModel.setUpdatedAt(DateUtil.getCurrentDate());

            if (userRepository.findByEmail(userModel.getEmail()) != null) {
                throw new Exception(AppConfig.EMAIL_ALREADY_EXISTS_MSG);
            }

            if (userRepository.findByIdNumber(userModel.getIdNumber()) != null) {
                throw new Exception(AppConfig.IDNUMBER_ALREADY_EXISTS_MSG);
            }

            UserModel newUser = userRepository.save(userModel);

            String content = "Hello "+newUser.getFirstName()+" "+newUser.getLastName()+"\n" +
                    "Your registration is complete. Click here "+AppConfig.APP_BASE_URL+"/login"+" to Login.\n\nBest Regards.";

            JSONObject payload = UMS_SERVICE_CENTER.setEmailParamters(newUser.getEmail(), "PASSWORD RESET", content);

            Boolean emailSent = UMS_SERVICE_CENTER.sendMail(AppConfig.SPRING_EMAIL_SERVICE_URL, payload.toJSONString());

            if(!emailSent){
                throw new Exception(AppConfig.MAIL_SEND_ERROR_MSG);
            }

            Map<String, String> records = new HashMap<>();
            records.put("record", AppConfig.RECORD_CREATED_WITH_ID_MSG + newUser.getId());

            Map<String, Object> recordObject = new HashMap<>();
            recordObject.put("created", records);

            resPayload = PayloadUtil.setPayload(AppConfig.CREATED_MSG, AppConfig.CREATED_CODE, "1", recordObject);
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("record", e.getMessage());

            Map<String, Object> errorObject = new HashMap<>();
            errorObject.put("error", errors);

            switch (e.getMessage()) {
                case AppConfig.EMAIL_ALREADY_EXISTS_MSG:
                case AppConfig.IDNUMBER_ALREADY_EXISTS_MSG:
                    res.setStatus(HttpServletResponse.SC_OK);
                    resPayload = PayloadUtil.setPayload(AppConfig.FAILED_MSG, AppConfig.SUCCESS_CODE, "0", errorObject);
                    break;
                case AppConfig.NULL_PASSWORD_MSG:
                case AppConfig.MIN_LENGTH_PASSWORD_MSG:
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resPayload = PayloadUtil.setPayload(AppConfig.FAILED_MSG, AppConfig.BAD_REQUEST_CODE, "0", errorObject);
                    break;
                case AppConfig.MAIL_SEND_ERROR_MSG:
                    res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resPayload = PayloadUtil.setPayload(AppConfig.MAIL_SEND_ERROR_MSG, AppConfig.INTERNAL_SERVER_ERROR_CODE, "0", errorObject);
                default:
                    res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resPayload = PayloadUtil.setPayload(AppConfig.INTERNAL_SERVER_ERROR_MSG, AppConfig.INTERNAL_SERVER_ERROR_CODE, "0", errorObject);
            }
        }
        return resPayload;
    }

    //    get one
    public ResponsePayload getUserById(String id, HttpServletResponse res) {
        ResponsePayload responsePayload;
        try {
            Optional<UserModel> userModel = userRepository.findById(id);

            if (userModel.isEmpty()) {
                throw new Exception(AppConfig.NOT_FOUND_MSG);
            }

            responsePayload = PayloadUtil.setPayload(AppConfig.SUCCESS_MSG, AppConfig.SUCCESS_CODE, "1", userModel);
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("record", e.getMessage());

            Map<String, Object> errorObject = new HashMap<>();
            errorObject.put("error", errors);

            switch (e.getMessage()) {
                case AppConfig.NOT_FOUND_MSG:
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    responsePayload = PayloadUtil.setPayload(AppConfig.FAILED_MSG, AppConfig.NOT_FOUND_CODE, "0", errorObject);
                    break;
                default:
                    responsePayload = PayloadUtil.setPayload(AppConfig.INTERNAL_SERVER_ERROR_MSG, AppConfig.INTERNAL_SERVER_ERROR_CODE, "0", errorObject);
            }
        }
        return responsePayload;
    }

    //    get all
    public ResponsePayload getAllUsers() {
        ResponsePayload responsePayload;
        try {
            List<UserModel> users = userRepository.findAllByOrderByCreatedAtDesc();

            responsePayload = PayloadUtil.setPayload(AppConfig.SUCCESS_MSG, AppConfig.SUCCESS_CODE, String.valueOf(users.size()), users);

        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("record", e.getMessage());

            Map<String, Object> errorObject = new HashMap<>();
            errorObject.put("error", errors);

            responsePayload = PayloadUtil.setPayload(AppConfig.INTERNAL_SERVER_ERROR_MSG, AppConfig.INTERNAL_SERVER_ERROR_CODE, "0", errorObject);
        }
        return responsePayload;
    }

    //    update user
    public ResponsePayload updateUser(UserModel userModel, HttpServletResponse res) {
        ResponsePayload responsePayload;
        try {
            Optional<UserModel> userExists = userRepository.findById(userModel.getId());

            if (userExists.isEmpty()) {
                throw new Exception(AppConfig.NOT_FOUND_MSG);
            }

            if (this.isUserEmailDifferent(userExists.get().getEmail(), userModel.getEmail())) {
                if (this.isUserEmailExists(userModel.getEmail())) {
                    throw new Exception(AppConfig.EMAIL_ALREADY_EXISTS_MSG);
                }
            }

            if (this.isUserIdNumberDifferent(userExists.get().getIdNumber(), userModel.getIdNumber())) {
                if (this.isUserIdNumberExists(userModel.getIdNumber())) {
                    throw new Exception(AppConfig.IDNUMBER_ALREADY_EXISTS_MSG);
                }
            }


            Query query = new Query();
            query.addCriteria(Criteria.where("id").is(userModel.getId()));

            Update update = new Update();
            update.set("firstName", userModel.getFirstName());
            update.set("lastName", userModel.getLastName());
            update.set("email", userModel.getEmail());
            update.set("idNUmber", userModel.getIdNumber());
            update.set("username", userModel.getEmail());
            update.set("phoneNumber", userModel.getPhoneNumber());
            update.set("address", userModel.getAddress());
            update.set("updatedAt", DateUtil.getCurrentDate());

            UserModel updatedUser = mongoTemplate.findAndModify(query, update, UserModel.class);

            Map<String, String> records = new HashMap<>();
            records.put("record", AppConfig.RECORD_UPDATED_WITH_ID_MSG + updatedUser.getId());

            Map<String, Object> recordObject = new HashMap<>();
            recordObject.put("updated", records);

            responsePayload = PayloadUtil.setPayload(AppConfig.SUCCESS_MSG, AppConfig.SUCCESS_CODE, "1", recordObject);
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("record", e.getMessage());

            Map<String, Object> errorObject = new HashMap<>();
            errorObject.put("error", errors);

            switch (e.getMessage()) {
                case AppConfig.EMAIL_ALREADY_EXISTS_MSG: case AppConfig.IDNUMBER_ALREADY_EXISTS_MSG:
                    res.setStatus(HttpServletResponse.SC_OK);
                    responsePayload = PayloadUtil.setPayload(AppConfig.FAILED_MSG, AppConfig.SUCCESS_CODE, "0", errorObject);
                    break;
                case AppConfig.NOT_FOUND_MSG:
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    responsePayload = PayloadUtil.setPayload(AppConfig.FAILED_MSG, AppConfig.NOT_FOUND_CODE, "0", errorObject);
                    break;
                default:
                    responsePayload = PayloadUtil.setPayload(AppConfig.INTERNAL_SERVER_ERROR_MSG, AppConfig.INTERNAL_SERVER_ERROR_CODE, "0", errorObject);
                    break;
            }
        }
        return responsePayload;
    }

    //    update password
    public ResponsePayload updateUserPassword(UpdatePassword updatePassword, HttpServletResponse res) {
        ResponsePayload responsePayload;

        try {
            Optional<UserModel> userModel = userRepository.findById(updatePassword.getId());

            if (userModel.isEmpty()) {
                throw new Exception(AppConfig.NOT_FOUND_MSG);
            }

            if (!(this.isPasswordMatch(userModel.get().getPassword(), updatePassword.getOldPassword()))) {
                throw new Exception(AppConfig.NOT_FOUND_MSG);
            }

            userModel.get().setPassword(bCryptPasswordEncoder.encode(updatePassword.getNewPassword()));
            userModel.get().setUpdatedAt(DateUtil.getCurrentDate());
            UserModel updatedUser = userRepository.save(userModel.get());

            Map<String, String> records = new HashMap<>();
            records.put("record", AppConfig.RECORD_UPDATED_WITH_ID_MSG + updatedUser.getId());

            Map<String, Object> recordObject = new HashMap<>();
            recordObject.put("updated", records);

            responsePayload = PayloadUtil.setPayload(AppConfig.SUCCESS_MSG, AppConfig.SUCCESS_CODE, "1", recordObject);

        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("record", e.getMessage());

            Map<String, Object> errorObject = new HashMap<>();
            errorObject.put("error", errors);

            switch (e.getMessage()) {
                case AppConfig.NOT_FOUND_MSG:
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    responsePayload = PayloadUtil.setPayload(AppConfig.FAILED_MSG, AppConfig.NOT_FOUND_CODE, "0", errorObject);
                    break;
                default:
                    responsePayload = PayloadUtil.setPayload(AppConfig.INTERNAL_SERVER_ERROR_MSG, AppConfig.INTERNAL_SERVER_ERROR_CODE, "0", errorObject);
                    break;
            }
        }

        return responsePayload;
    }

    //    delete user
    public ResponsePayload deleteUser(DeleteUser deletUser, HttpServletResponse res) {
        ResponsePayload responsePayload;

        try {
            Optional<UserModel> userModel = userRepository.findById(deletUser.getId());

            if (userModel.isEmpty()) {
                throw new Exception(AppConfig.NOT_FOUND_MSG);
            }

            userRepository.deleteById(userModel.get().getId());

            Map<String, String> records = new HashMap<>();
            records.put("record", AppConfig.RECORD_DELETED_WITH_ID_MSG + userModel.get().getId());

            Map<String, Object> recordObject = new HashMap<>();
            recordObject.put("deleted", records);

            responsePayload = PayloadUtil.setPayload(AppConfig.SUCCESS_MSG, AppConfig.SUCCESS_CODE, "1", recordObject);


        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("record", e.getMessage());

            Map<String, Object> errorObject = new HashMap<>();
            errorObject.put("error", errors);

            switch (e.getMessage()) {
                case AppConfig.NOT_FOUND_MSG:
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    responsePayload = PayloadUtil.setPayload(AppConfig.FAILED_MSG, AppConfig.NOT_FOUND_CODE, "0", errorObject);
                    break;
                default:
                    responsePayload = PayloadUtil.setPayload(AppConfig.INTERNAL_SERVER_ERROR_MSG, AppConfig.INTERNAL_SERVER_ERROR_CODE, "0", errorObject);
                    break;
            }
        }

        return responsePayload;
    }

    //    send password reset code
    public ResponsePayload sendPasswordResetCode(PasswordResetCode passwordResetCode, HttpServletResponse res) {
        ResponsePayload responsePayload;
        try {
            Optional<UserModel> userModel = Optional.ofNullable(userRepository.findByEmail(passwordResetCode.getEmail()));

            if (userModel.isEmpty()) {
                throw new Exception(AppConfig.NOT_FOUND_MSG);
            }

            String resetCode = RandomCode.generateOTP();

            passwordResetCode.setCode(resetCode);
            PasswordResetCode passwordResetCodeExists = passwordResetCodeRepository.findByEmail(passwordResetCode.getEmail());
            if (passwordResetCodeExists != null) {
                passwordResetCodeRepository.deleteById(passwordResetCodeExists.getId());
            }
            passwordResetCodeRepository.save(passwordResetCode);

            String mailTo = userModel.get().getEmail();
            String subject = "PASSWORD RESET CODE";
            String content = "Hello " + userModel.get().getFirstName() + " " + userModel.get().getLastName() + "," +
                    "\n\nYour password reset code is: " + resetCode + "\n\nBest Regards.";

            JSONObject payload = UMS_SERVICE_CENTER.setEmailParamters(mailTo, subject, content);

            Boolean emailSent = UMS_SERVICE_CENTER.sendMail(AppConfig.SPRING_EMAIL_SERVICE_URL, payload.toJSONString());

            if(!emailSent){
                throw new Exception(AppConfig.MAIL_SEND_ERROR_MSG);
            }

            Map<String, String> records = new HashMap<>();
            records.put("record", AppConfig.PASSWORD_RESET_CODE_EMAIL_SENT_MSG + userModel.get().getEmail());

            Map<String, Object> recordObject = new HashMap<>();
            recordObject.put("status", records);

            responsePayload = PayloadUtil.setPayload(AppConfig.SUCCESS_MSG, AppConfig.SUCCESS_CODE, "1", recordObject);
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("record", e.getMessage());

            Map<String, Object> errorObject = new HashMap<>();
            errorObject.put("error", errors);

            switch (e.getMessage()) {
                case AppConfig.NOT_FOUND_MSG:
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    responsePayload = PayloadUtil.setPayload(AppConfig.FAILED_MSG, AppConfig.NOT_FOUND_CODE, "0", errorObject);
                    break;
                case AppConfig.MAIL_SEND_ERROR_MSG:
                    res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    responsePayload = PayloadUtil.setPayload(AppConfig.MAIL_SEND_ERROR_MSG, AppConfig.INTERNAL_SERVER_ERROR_CODE, "0", errorObject);
                default:
                    responsePayload = PayloadUtil.setPayload(AppConfig.INTERNAL_SERVER_ERROR_MSG, AppConfig.INTERNAL_SERVER_ERROR_CODE, "0", errorObject);
            }
        }
        return responsePayload;
    }

    //    reset password
    public ResponsePayload resetPassword(ResetPassword resetPassword, HttpServletResponse res) {
        ResponsePayload responsePayload;

        try {
            PasswordResetCode passwordResetCode = passwordResetCodeRepository.findByEmail(resetPassword.getEmail());

            if (passwordResetCode == null) {
                throw new Exception(AppConfig.INVALID_RESET_CODE_MSG);
            }

            if (!(passwordResetCode.getCode().matches(resetPassword.getResetCode()))) {
                throw new Exception(AppConfig.INVALID_RESET_CODE_MSG);
            }

            UserModel userModel = userRepository.findByEmail(resetPassword.getEmail());

            userModel.setPassword(bCryptPasswordEncoder.encode(resetPassword.getNewPassword()));
            userModel.setUpdatedAt(DateUtil.getCurrentDate());
            UserModel updatedUser = userRepository.save(userModel);

            passwordResetCodeRepository.deleteById(passwordResetCode.getId());

            String content = "Hello "+userModel.getFirstName()+" "+userModel.getLastName()+"\n" +
                    "Your password was successfully reset. Click here "+AppConfig.APP_BASE_URL+"/login"+" to Login.\n\nBest Regards.";

            JSONObject payload = UMS_SERVICE_CENTER.setEmailParamters(resetPassword.getEmail(), "PASSWORD RESET", content);

            Boolean emailSent = UMS_SERVICE_CENTER.sendMail(AppConfig.SPRING_EMAIL_SERVICE_URL, payload.toJSONString());

            if(!emailSent){
                throw new Exception(AppConfig.MAIL_SEND_ERROR_MSG);
            }

            Map<String, String> records = new HashMap<>();
            records.put("record", AppConfig.RECORD_UPDATED_WITH_ID_MSG + updatedUser.getId());

            Map<String, Object> recordObject = new HashMap<>();
            recordObject.put("updated", records);

            responsePayload = PayloadUtil.setPayload(AppConfig.SUCCESS_MSG, AppConfig.SUCCESS_CODE, "1", recordObject);

        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("record", e.getMessage());

            Map<String, Object> errorObject = new HashMap<>();
            errorObject.put("error", errors);

            switch (e.getMessage()) {
                case AppConfig.INVALID_RESET_CODE_MSG:
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    responsePayload = PayloadUtil.setPayload(AppConfig.FAILED_MSG, AppConfig.BAD_REQUEST_CODE, "0", errorObject);
                    break;
                case AppConfig.MAIL_SEND_ERROR_MSG:
                    res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    responsePayload = PayloadUtil.setPayload(AppConfig.MAIL_SEND_ERROR_MSG, AppConfig.INTERNAL_SERVER_ERROR_CODE, "0", errorObject);
                default:
                    responsePayload = PayloadUtil.setPayload(AppConfig.INTERNAL_SERVER_ERROR_MSG, AppConfig.INTERNAL_SERVER_ERROR_CODE, "0", errorObject);
                    break;
            }
        }

        return responsePayload;
    }

//    activate or deactivate user
    public ResponsePayload activateOrDeactivateUser(UserActivationOrDeactivationModel userActivationOrDeactivationModel, HttpServletResponse res){
        ResponsePayload responsePayload;
        try{
            Optional<UserModel> userModel = userRepository.findByIdOrUsernameOrIdNumber(userActivationOrDeactivationModel.getId());

            if(userModel == null){
                throw new Exception(AppConfig.NOT_FOUND_MSG);
            }

            userModel.get().setActive(userActivationOrDeactivationModel.isActive());
            UserModel updatedUser = userRepository.save(userModel.get());

            Map<String, String> records = new HashMap<>();
            records.put("record", AppConfig.RECORD_UPDATED_WITH_ID_MSG + updatedUser.getId());

            Map<String, Object> recordObject = new HashMap<>();
            recordObject.put("updated", records);

            responsePayload = PayloadUtil.setPayload(AppConfig.SUCCESS_MSG, AppConfig.SUCCESS_CODE, "1", recordObject);
        }
        catch (Exception e){
            Map<String, String> errors = new HashMap<>();
            errors.put("record", e.getMessage());

            Map<String, Object> errorObject = new HashMap<>();
            errorObject.put("error", errors);

            switch (e.getMessage()) {
                case AppConfig.NOT_FOUND_MSG:
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    responsePayload = PayloadUtil.setPayload(AppConfig.FAILED_MSG, AppConfig.NOT_FOUND_CODE, "0", errorObject);
                    break;
                default:
                    responsePayload = PayloadUtil.setPayload(AppConfig.INTERNAL_SERVER_ERROR_MSG, AppConfig.INTERNAL_SERVER_ERROR_CODE, "0", errorObject);
                    break;
            }
        }
        return responsePayload;
    }

    //    is email exists
    public Boolean isUserEmailExists(String email) {
        Boolean result = false;

        UserModel userModel = userRepository.findByEmail(email);

        if (userModel != null) {
            result = true;
        }

        return result;
    }

    //    is email different
    public Boolean isUserEmailDifferent(String oldEmail, String newEmail) {
        Boolean result = true;

        if (oldEmail.equals(newEmail)) {
            return false;
        }

        return result;
    }

    //    is password match
    public Boolean isPasswordMatch(String oldHashedPassword, String newPasswordText) {
        Boolean result = false;

        if (bCryptPasswordEncoder.matches(newPasswordText, oldHashedPassword)) {
            result = true;
        }

        return result;
    }

    //    is ID number exists
    public Boolean isUserIdNumberExists(String idNumber) {
        Boolean result = false;

        UserModel userModel = userRepository.findByIdNumber(idNumber);

        if (userModel != null) {
            result = true;
        }

        return result;
    }

    //    is ID number different
    public Boolean isUserIdNumberDifferent(String oldIdNumber, String newIdNumber) {
        Boolean result = true;

        if (oldIdNumber.equals(newIdNumber)) {
            return false;
        }

        return result;
    }


}
