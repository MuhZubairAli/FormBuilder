package pk.gov.pbs.formbuilder.database.dao;

import java.util.concurrent.Future;

import pk.gov.pbs.database.DatabaseUtils;
import pk.gov.pbs.formbuilder.database.FormBuilderRepository;
import pk.gov.pbs.formbuilder.meta.Constants;
import pk.gov.pbs.formbuilder.models.LoginPayload;
import pk.gov.pbs.utils.SystemUtils;

public class LoginDao {
    FormBuilderRepository mRepository;
    private static final String table = LoginPayload.class.getSimpleName();

    public LoginDao(FormBuilderRepository mRepository) {
        this.mRepository = mRepository;
    }

    public Long logout(){
        Future<Long> future = mRepository.getExecutorService().submit(() -> {
            LoginPayload loginPayload = getLoginPayload();
            if (loginPayload != null) {
                loginPayload.expire();
                loginPayload.ts_updated = SystemUtils.getUnixTs();
                return mRepository.getDatabase().replace(loginPayload);
            }
            return (long) Constants.INVALID_NUMBER;
        });
        return DatabaseUtils.getFutureValue(future);
    }

    public Long setLoginPayload(LoginPayload config){
        return DatabaseUtils.getFutureValue(
                mRepository.replaceOrThrow(config)
        );
    }

    public LoginPayload getLoginPayload(){
        LoginPayload payload = DatabaseUtils.getFutureValue(
            mRepository.selectRowAs(
                    LoginPayload.class,
                    "SELECT * FROM " +table +" ORDER BY "+ DatabaseUtils.getPrimaryKeyField(LoginPayload.class).getName() + " DESC LIMIT 1",
                    null
            )
        );
        if (payload == null && Constants.DEBUG_MODE){
            payload = new LoginPayload(
                    "1240103594123",
                    "Test User",
                    1,
                    SystemUtils.getUnixTs() + 999999,
                    null,
                    "empty token"
            );
            setLoginPayload(payload);
        }
        return payload;
    }

}
