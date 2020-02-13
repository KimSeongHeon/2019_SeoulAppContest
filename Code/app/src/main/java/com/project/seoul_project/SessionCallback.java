package com.project.seoul_project;

import android.content.Context;
import android.util.Log;
import com.kakao.auth.ISessionCallback;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.exception.KakaoException;

import java.util.ArrayList;
import java.util.List;

public class SessionCallback implements ISessionCallback {
    String nickname;
    Long id;
    String thumbnailurl = "";
    // 로그인에 성공한 상태
    private Context mContext = null;

    public SessionCallback(Context context) {
//        super(context);
        this.mContext = context;
    }


    @Override
    public void onSessionOpened() {
        requestMe();
    }

    // 로그인에 실패한 상태
    @Override
    public void onSessionOpenFailed(KakaoException exception) {
        Log.v("SessionCallback :: ", "onSessionOpenFailed : " + exception.getMessage());
    }



    private void requestMe() {

        List<String> keys = new ArrayList<>();
        keys.add("properties.nickname");
        keys.add("properties.profile_image");
        keys.add("kakao_account.email");
        keys.add("properties.thumbnail_image");


        UserManagement.getInstance().me(keys, new MeV2ResponseCallback() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                super.onFailure(errorResult);
            }

            @Override
            public void onFailureForUiThread(ErrorResult errorResult) {
                super.onFailureForUiThread(errorResult);
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
            }

            @Override
            public void onSuccess(MeV2Response result) {

                Log.v("SessionCallback :: ", "onSuccess ");
                Log.v("SessionCallback :: ", result.toString());
                nickname = result.getNickname();
                id = result.getId();
                if(result.getThumbnailImagePath()!=null) {
                    thumbnailurl = result.getThumbnailImagePath();
                }
             
               // imageurl = result.getProfileImagePath();
                Log.v("Profile nick : ", nickname + "");
                Log.v("Profile id : ", id + "");
                Log.v("Profile image : ", thumbnailurl + "");
               // Log.v("Profile image : ", imageurl + "");

                redirectMainActivity();
            }

        });
    }

    /** 로그아웃시 **/
    private void onClickLogout() {

        UserManagement.getInstance().requestUnlink(new UnLinkResponseCallback() {
            @Override
            public void onSessionClosed(ErrorResult errorResult) {
            }

            @Override
            public void onNotSignedUp() {
            }

            @Override
            public void onSuccess(Long result) {
            }
        });
    }

    // 사용자 정보 요청
//    public void requestMe() {
//        UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
//            @Override
//            public void onCompleteLogout() {
////                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
////                startActivity(intent);
//            }
//        });
//        // 사용자정보 요청 결과에 대한 Callback
//        UserManagement.getInstance().requestMe(new MeResponseCallback() {
//            // 세션 오픈 실패. 세션이 삭제된 경우,
//            @Override
//            public void onSessionClosed(ErrorResult errorResult) {
//                Log.v("SessionCallback :: ", "onSessionClosed : " + errorResult.getErrorMessage());
//                redirectLoginActivity();
//            }
//
//            // 회원이 아닌 경우,
//            @Override
//            public void onNotSignedUp() {
//                Log.e("SessionCallback :: ", "onNotSignedUp");
//            }
//
//            // 사용자정보 요청에 성공한 경우,
//            @Override
//            public void onSuccess(UserProfile userProfile) {
//
//                Log.v("SessionCallback :: ", "onSuccess");
//
//                nickname = userProfile.getNickname();
//                id = userProfile.getId();
//
//                Log.v("Profile nick : ", nickname + "");
//                Log.v("Profile id : ", id + "");
//
//
//                redirectMainActivity();
//            }
//
//            // 사용자 정보 요청 실패
//            @Override
//            public void onFailure(ErrorResult errorResult) {
//                Log.v("SessionCallback :: ", "onFailure : " + errorResult.getErrorMessage());
//            }
//        });
//    }

    private void redirectMainActivity() {
        ((Login_Acitivity) mContext).checkAccount(id, nickname,thumbnailurl);
    }

}