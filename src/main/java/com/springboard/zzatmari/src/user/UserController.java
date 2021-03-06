package com.springboard.zzatmari.src.user;

import com.springboard.zzatmari.src.user.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.springboard.zzatmari.config.BaseException;
import com.springboard.zzatmari.config.BaseResponse;
import com.springboard.zzatmari.utils.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


import static com.springboard.zzatmari.config.BaseResponseStatus.*;
import static com.springboard.zzatmari.utils.ValidationRegex.isRegexEmail;

@RestController
@RequestMapping("/users")
public class UserController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final UserProvider userProvider;
    @Autowired
    private final UserService userService;
    @Autowired
    private final JwtService jwtService;




    public UserController(UserProvider userProvider, UserService userService, JwtService jwtService){
        this.userProvider = userProvider;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    /**
     * 임시 JWT 발급용 API
     * [POST] /users/jwt
     * @return BaseResponse<PostJWTRes>
     */
    @ResponseBody
    @PostMapping("/jwt")
    public BaseResponse<PostJWTRes> getJWT(@RequestBody PostJWTReq postJWTReq) {

        int userIdx = postJWTReq.getUserIdx();
        if(userIdx == 0) userIdx = 1;

        String jwt = jwtService.createJwt(userIdx);
        PostJWTRes response = new PostJWTRes(jwt, userIdx);
        return new BaseResponse<PostJWTRes>(response);

    }

    /**
     * 하루 시작시간 등록 API
     * [POST] /users/time
     * @return BaseResponse<PostUserRes>
     */
    @ResponseBody
    @PostMapping("/time")
    public BaseResponse<PostUserTimeRes> updateUserTime(@RequestBody PostUserTimeReq postUserTimeReq) throws BaseException {

        int userIdx = jwtService.getUserIdx();

        //시간 형식체크
        if(postUserTimeReq.getHour() >= 24 || postUserTimeReq.getHour() < 0 || postUserTimeReq.getMinute() >= 60 || postUserTimeReq.getMinute() < 0){
            return new BaseResponse<>(USERS_TIME_INVALID);
        }
        try{
            int isSuccess = userService.updateUserTime(postUserTimeReq, userIdx);

            if(isSuccess == 1){
                PostUserTimeRes response = new PostUserTimeRes(Integer.toString(postUserTimeReq.getHour()) + ":" + Integer.toString(postUserTimeReq.getMinute()));
                return new BaseResponse<PostUserTimeRes>(response);
            }
            else
                return new BaseResponse<>(REQUEST_FAIL);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     *
     * [GET] /users/:userIdx/seed
     * 씨앗창고 조회
     * @return BaseResponse<List<GetUserSeedRes>>
     */
    //Query String
    @ResponseBody
    @GetMapping("/{userIdx}/seed")
    public BaseResponse<List<GetUserSeedRes>> getUserSeeds(@PathVariable int userIdx) {
        try{
            if(userIdx <= 0){
                return new BaseResponse<>(USERS_ID_EMPTY);
            }
            // Get Users
            List<GetUserSeedRes> getUsersRes = userProvider.getUserSeeds(userIdx);
            return new BaseResponse<>(getUsersRes);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 회원 조회 API
     * [GET] /users
     * 회원 번호 및 이메일 검색 조회 API
     * [GET] /users? Email=
     * @return BaseResponse<List<GetUserRes>>
     */
    //Query String
    @ResponseBody
    @GetMapping("") // (GET) 127.0.0.1:9000/app/users
    public BaseResponse<List<GetUserRes>> getUsers(@RequestParam(required = false) String Email) {
        try{
            if(Email == null){
                List<GetUserRes> getUsersRes = userProvider.getUsers();
                return new BaseResponse<>(getUsersRes);
            }
            // Get Users
            List<GetUserRes> getUsersRes = userProvider.getUsersByEmail(Email);
            return new BaseResponse<>(getUsersRes);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 사용자 정보 조회 API
     * [GET] /users/:userIdx
     * @return BaseResponse<GetUserRes>
     */
    @ResponseBody
    @GetMapping("/{userIdx}") // (GET) 127.0.0.1:9000/app/users/:userIdx
    public BaseResponse<GetUserRes> getUser(@PathVariable("userIdx") int userIdx) {
        try{
            int userIdxFromJWT = jwtService.getUserIdx();
            if(userIdx != userIdxFromJWT)
                return new BaseResponse<>(USERS_ID_JWT_NOT_MATCH);

            GetUserRes getUserRes = userProvider.getUser(userIdx);
            return new BaseResponse<>(getUserRes);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }

    }


    /**
     * 로그인 API
     * [POST] /users?type=?
     * @return BaseResponse<PostUserRes>
     */
    @ResponseBody
    @PostMapping("")
    public BaseResponse<PostUserRes> createUser(@RequestParam String type, @RequestBody PostUserReq postUserReq) {

        try{
            if(type == null)
                return new BaseResponse<>(USERS_TYPE_EMPTY);

            int t = Integer.parseInt(type);

        if(t == 0){ //비회원 로그인
            if(postUserReq.getToken() == null){
                return new BaseResponse<>(USERS_TOKEN_EMPTY);
            }

            PostUserRes postUserRes = userService.createUnknownUser(postUserReq);
            return new BaseResponse<>(postUserRes);
        }
        else if(t == 1){ //이메일 로그인
            if(postUserReq.getEmail() == null)
                return new BaseResponse<>(POST_USERS_EMPTY_EMAIL);
            if(!isRegexEmail(postUserReq.getEmail()))
                return new BaseResponse<>(POST_USERS_INVALID_EMAIL);

            if(postUserReq.getPassword() == null)
                return new BaseResponse<>(POST_USERS_PASSWORD_EMPTY);
            if(postUserReq.getPassword().length() < 6 || postUserReq.getPassword().length() > 15)
                return new BaseResponse<>(POST_USERS_PASSWORD_LENGTH);

            PostUserRes postUserRes = userService.loginEmailUser(postUserReq);
            return new BaseResponse<>(postUserRes);
        }
        else if(t == 2){ //카카오 로그인
            if(postUserReq.getToken() == null){
                return new BaseResponse<>(USERS_TOKEN_EMPTY);
            }

            PostUserRes postUserRes = userService.createKakaoUser(postUserReq);
            return new BaseResponse<>(postUserRes);
        }

        return new BaseResponse<>(USERS_TYPE_ERROR_TYPE);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 비밀번호 변경
     * [PATCH] /users/:userIdx/password
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("{userIdx}/password")
    public BaseResponse<String> modifyPassword(@PathVariable("userIdx") int userIdx, @RequestBody PatchUserPasswordReq patchUserPasswordReq){
        try {
            //jwt에서 idx 추출.
            int userIdxByJwt = jwtService.getUserIdx();
            //userIdx와 접근한 유저가 같은지 확인
            if(userIdx != userIdxByJwt){
                return new BaseResponse<>(USERS_ID_JWT_NOT_MATCH);
            }

            if(patchUserPasswordReq.getNowPassword() == null)
                return new BaseResponse<>(USERS_NOW_PASSWORD_EMPTY);
            if(patchUserPasswordReq.getNewPassword() == null)
                return new BaseResponse<>(USERS_NEW_PASSWORD_EMPTY);
            if(patchUserPasswordReq.getNowPassword().length() < 6 || patchUserPasswordReq.getNowPassword().length()>15 || patchUserPasswordReq.getNewPassword().length()<6 || patchUserPasswordReq.getNewPassword().length()>15)
                return new BaseResponse<>(POST_USERS_PASSWORD_LENGTH);


            userService.modifyPassword(userIdx, patchUserPasswordReq);

            String result = "";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 비밀번호 찾기 (메일 전송)
     * [POST] /users/password
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PostMapping("/password")
    public BaseResponse<String> forgotPassword(){
        try {
            //jwt에서 idx 추출.
            int userIdx = jwtService.getUserIdx();

            userService.sendEmail(userIdx);

            String result = "";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     *
     * [POST] /users/logIn
     * @return BaseResponse<PostLoginRes>
     */
    @ResponseBody
    @PostMapping("/logIn")
    public BaseResponse<PostLoginRes> logIn(@RequestBody PostLoginReq postLoginReq){
        try{
            // TODO: 로그인 값들에 대한 형식적인 validatin 처리해주셔야합니다!
            // TODO: 유저의 status ex) 비활성화된 유저, 탈퇴한 유저 등을 관리해주고 있다면 해당 부분에 대한 validation 처리도 해주셔야합니다.
            PostLoginRes postLoginRes = userProvider.logIn(postLoginReq);
            return new BaseResponse<>(postLoginRes);
        } catch (BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }


}
